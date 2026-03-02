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

@Composable
fun AddExpenseScreen(
    onBack: () -> Unit,
    onSave: (
        amount: String,
        currency: CurrencyCode,
        concept: String,
        merchant: String?,
        address: String?,
        description: String?,
        method: PaymentMethod,
        receiptUri: String?
    ) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var concept by remember { mutableStateOf("") }
    var merchant by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var currency by remember { mutableStateOf(CurrencyCode.USD) }
    var method by remember { mutableStateOf(PaymentMethod.CASH) }

    val context = LocalContext.current

    var receipt: Uri? by remember { mutableStateOf(null) }
    var pendingCameraUri: Uri? by remember { mutableStateOf(null) }

// Galería (Photo Picker)
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        receipt = uri
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
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Volver") }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Monto") })
            OutlinedTextField(value = concept, onValueChange = { concept = it }, label = { Text("Concepto") })

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DropdownEnum("Moneda", CurrencyCode.entries.toList(), currency) { currency = it }
                DropdownEnum("Método", PaymentMethod.entries.toList(), method) { method = it }
            }

            OutlinedTextField(value = merchant, onValueChange = { merchant = it }, label = { Text("Comercio") })
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Dirección") })
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") })

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                OutlinedButton(onClick = {
                    // 1) Crear un URI para que la cámara guarde ahí
                    val uri = createReceiptImageUri(context)
                    pendingCameraUri = uri
                    // 2) Lanzar cámara
                    takePicture.launch(uri)
                }) {
                    Text("Tomar foto")
                }

                OutlinedButton(onClick = {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Text("Elegir foto")
                }

                if (receipt != null) Text("✔ Recibo")
            }

            Button(
                onClick = {
                    onSave(
                        amount, currency, concept,
                        merchant.ifBlank { null },
                        address.ifBlank { null },
                        description.ifBlank { null },
                        method,
                        receipt?.toString()
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar") }
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