@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.gestorgastos.ui.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.gestorgastos.data.local.entity.CategoryEntity
import com.example.gestorgastos.data.local.entity.ExpenseEntity
import com.example.gestorgastos.data.local.model.CurrencyCode
import com.example.gestorgastos.data.local.model.PaymentMethod
import com.example.gestorgastos.media.createReceiptImageUri
import com.example.gestorgastos.media.copyReceiptToInternal
import com.example.gestorgastos.media.openReceiptImage
import com.example.gestorgastos.ui.receipt.ReceiptThumbnail
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding

@Composable
fun EditExpenseScreen(
    expense: ExpenseEntity,
    categories: List<CategoryEntity>,
    onBack: () -> Unit,
    onSave: (
        amount: String,
        currency: CurrencyCode,
        concept: String,
        merchant: String?,
        address: String?,
        description: String?,
        method: PaymentMethod,
        receiptUri: String?,
        categoryId: Long?
    ) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // inicializar campos desde expense
    var amount by remember { mutableStateOf("") } // lo prellenamos abajo
    var concept by remember { mutableStateOf(expense.concept) }
    var merchant by remember { mutableStateOf(expense.merchant.orEmpty()) }
    var address by remember { mutableStateOf(expense.address.orEmpty()) }
    var description by remember { mutableStateOf(expense.description.orEmpty()) }

    var currency by remember { mutableStateOf(runCatching { CurrencyCode.valueOf(expense.currency) }.getOrDefault(CurrencyCode.USD)) }
    var method by remember { mutableStateOf(runCatching { PaymentMethod.valueOf(expense.paymentMethod) }.getOrDefault(PaymentMethod.CASH)) }

    var selectedCategoryId by remember { mutableStateOf<Long?>(expense.categoryId) }
    var catExpanded by remember { mutableStateOf(false) }

    var receipt: Uri? by remember { mutableStateOf(expense.receiptUri?.let { Uri.parse(it) }) }
    var pendingCameraUri: Uri? by remember { mutableStateOf(null) }

    // Convertir amountMinor a texto inicial (simple)
    LaunchedEffect(expense.id) {
        val decimals = currency.decimals
        val bd = expense.amountMinor.toBigDecimal().movePointLeft(decimals)
        amount = bd.toPlainString()
    }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                receipt = copyReceiptToInternal(context, uri)
            }
        }
    }

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        if (ok) receipt = pendingCameraUri else pendingCameraUri = null
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar gasto") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } },
                actions = { TextButton(onClick = { showDeleteConfirm = true }) { Text("Borrar") } }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 6.dp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .navigationBarsPadding()
                ) {
                    Button(
                        onClick = {
                            onSave(
                                amount, currency, concept,
                                merchant.ifBlank { null },
                                address.ifBlank { null },
                                description.ifBlank { null },
                                method,
                                receipt?.toString(),
                                selectedCategoryId
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Guardar cambios") }
                }
            }
        }
    ) { padding ->
        val scrollState = rememberScrollState()

        Column(
            Modifier
                .padding(padding)
                .padding(12.dp)
                .verticalScroll(scrollState)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Monto") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = concept,
                onValueChange = { concept = it },
                label = { Text("Concepto") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DropdownEnum("Moneda", CurrencyCode.entries.toList(), currency) { currency = it }
                DropdownEnum("Método", PaymentMethod.entries.toList(), method) { method = it }
            }

            // Categoría
            val selectedCategoryName = categories.firstOrNull { it.id == selectedCategoryId }?.name ?: "Sin categoría"
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column {
                    Text("Categoría", style = MaterialTheme.typography.labelMedium)
                    OutlinedButton(onClick = { catExpanded = true }) { Text(selectedCategoryName) }
                    DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Sin categoría") },
                            onClick = { selectedCategoryId = null; catExpanded = false }
                        )
                        categories.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.name) },
                                onClick = { selectedCategoryId = c.id; catExpanded = false }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(value = merchant, onValueChange = { merchant = it }, label = { Text("Comercio") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                // Fila 1: capturar / elegir
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val uri = createReceiptImageUri(context)
                            pendingCameraUri = uri
                            takePicture.launch(uri)
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Tomar foto") }

                    OutlinedButton(
                        onClick = {
                            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Elegir foto") }
                }

                // Fila 2: acciones sobre el recibo actual
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { receipt = null },
                        enabled = receipt != null,
                        modifier = Modifier.weight(1f)
                    ) { Text("Quitar") }

                    OutlinedButton(
                        onClick = { receipt?.let { openReceiptImage(context, it) } },
                        enabled = receipt != null,
                        modifier = Modifier.weight(1f)
                    ) { Text("Abrir") }
                }
            }

            // Si quieres thumbnail también aquí (recomendado), agrega esto:
            receipt?.let { uri ->
                ReceiptThumbnail(
                    uri = uri,
                    onClick = { openReceiptImage(context, uri) }
                )
            }

            // Espacio extra para que el contenido final no quede tapado por el bottomBar
            Spacer(Modifier.height(90.dp))
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Borrar gasto") },
            text = { Text("¿Seguro que quieres borrar este gasto?") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDelete() }) { Text("Sí, borrar") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun <T : Enum<T>> DropdownEnum(
    label: String,
    options: List<T>,
    selected: T,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        OutlinedButton(onClick = { expanded = true }) { Text(selected.name) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt.name) },
                    onClick = { onSelected(opt); expanded = false }
                )
            }
        }
    }
}