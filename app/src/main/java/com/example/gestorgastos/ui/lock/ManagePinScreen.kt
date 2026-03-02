@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.gestorgastos.ui.lock

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.gestorgastos.security.SecurityManager

@Composable
fun ManagePinScreen(
    security: SecurityManager,
    onBack: () -> Unit,
    onVerifiedToChange: () -> Unit,
    onPinDisabled: () -> Unit
) {
    var current by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PIN") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Introduce tu PIN actual para cambiarlo o desactivarlo.")

            OutlinedTextField(
                value = current,
                onValueChange = { current = it; error = null },
                label = { Text("PIN actual") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        if (security.verifyPin(current)) onVerifiedToChange()
                        else error = "PIN incorrecto"
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Cambiar PIN") }

                OutlinedButton(
                    onClick = {
                        if (security.verifyPin(current)) {
                            security.clearPin()
                            onPinDisabled()
                        } else error = "PIN incorrecto"
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Desactivar") }
            }

            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}