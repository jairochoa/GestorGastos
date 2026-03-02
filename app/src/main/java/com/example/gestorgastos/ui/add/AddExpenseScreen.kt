@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.gestorgastos.ui.add

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestorgastos.data.local.model.CurrencyCode
import com.example.gestorgastos.data.local.model.PaymentMethod
import androidx.compose.ui.platform.LocalContext
import com.example.gestorgastos.media.createReceiptImageUri
import com.example.gestorgastos.data.local.entity.CategoryEntity
import com.example.gestorgastos.media.copyReceiptToInternal
import com.example.gestorgastos.media.openReceiptImage
import com.example.gestorgastos.ui.receipt.ReceiptThumbnail
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding



@Composable
fun AddExpenseScreen(
    onBack: () -> Unit,
    categories: List<CategoryEntity>,
    onManageCategories: () -> Unit,
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
    ) -> Unit
) {
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }

    val selectedCategoryName = categories.firstOrNull { it.id == selectedCategoryId }?.name ?: "Sin categoría"
    var catExpanded by remember { mutableStateOf(false) }


    var amount by remember { mutableStateOf("") }
    var concept by remember { mutableStateOf("") }
    var merchant by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var currency by remember { mutableStateOf(CurrencyCode.USD) }
    var method by remember { mutableStateOf(PaymentMethod.CASH) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var receipt: Uri? by remember { mutableStateOf(null) }
    var pendingCameraUri: Uri? by remember { mutableStateOf(null) }

// Galería (Photo Picker)
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                // Copiar a interno para tener un Uri estable
                receipt = copyReceiptToInternal(context, uri)
            }
        }
    }

// Cámara (toma foto y guarda en el URI que le damos)
    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        if (ok) {
            receipt = pendingCameraUri
        } else {
            // Si canceló o falló, no usamos ese URI
            pendingCameraUri = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo gasto") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } }
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
                    ) { Text("Guardar") }
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
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Monto") })
            OutlinedTextField(value = concept, onValueChange = { concept = it }, label = { Text("Concepto") })

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DropdownEnum("Moneda", CurrencyCode.entries.toList(), currency) { currency = it }
                DropdownEnum("Método", PaymentMethod.entries.toList(), method) { method = it }
            }

            OutlinedTextField(value = merchant, onValueChange = { merchant = it }, label = { Text("Comercio") })
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Dirección") })
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") })

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

            receipt?.let { uri ->
                ReceiptThumbnail(
                    uri = uri,
                    onClick = { openReceiptImage(context, uri) }
                )
            }

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

                OutlinedButton(onClick = onManageCategories) { Text("Gestionar") }
            }

            // Espacio extra para que el contenido final no quede tapado por el bottomBar
            Spacer(Modifier.height(90.dp))
        }
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