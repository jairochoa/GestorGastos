@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.gestorgastos.ui.lock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.gestorgastos.security.SecurityManager

@Composable
fun SetupPinScreen(
    security: SecurityManager,
    onPinSet: () -> Unit
) {
    var p1 by remember { mutableStateOf("") }
    var p2 by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Configurar PIN") }) }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            OutlinedTextField(
                value = p1,
                onValueChange = { p1 = it; error = null },
                label = { Text("Nuevo PIN") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = p2,
                onValueChange = { p2 = it; error = null },
                label = { Text("Repetir PIN") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    error = null
                    if (p1.length < 4) { error = "PIN mínimo 4 dígitos"; return@Button }
                    if (p1 != p2) { error = "Los PIN no coinciden"; return@Button }
                    security.setPin(p1)
                    onPinSet()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar PIN") }

            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}