@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.gestorgastos.ui.lock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.gestorgastos.security.SecurityManager

@Composable
fun LockScreen(
    security: SecurityManager,
    onUnlocked: () -> Unit,
    onSetupPin: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    // Cuando entra a esta pantalla, enfoca el input
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboard?.show()
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Bloqueado") }) }) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            if (!security.isPinSet()) {
                Text("No hay PIN configurado.")
                Button(onClick = onSetupPin) { Text("Configurar PIN") }
                return@Column
            }

            OutlinedTextField(
                value = pin,
                onValueChange = {
                    // acepta solo dígitos (y limpia error al escribir)
                    pin = it.filter { ch -> ch.isDigit() }.take(12)
                    error = null
                },
                label = { Text("PIN") },
                isError = error != null,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                supportingText = {
                    if (error != null) Text(error!!)
                }
            )

            Button(
                onClick = {
                    val ok = security.verifyPin(pin)
                    if (ok) {
                        keyboard?.hide()
                        onUnlocked()
                    } else {
                        error = "PIN incorrecto"
                        pin = "" // limpia el campo
                        focusRequester.requestFocus()
                        keyboard?.show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = pin.isNotBlank()
            ) {
                Text("Entrar")
            }
        }
    }
}