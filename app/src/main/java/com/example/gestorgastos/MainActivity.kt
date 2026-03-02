package com.example.gestorgastos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.gestorgastos.ui.nav.AppNav
import com.example.gestorgastos.ui.theme.GestorGastosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GestorGastosTheme {
                AppNav()
            }
        }
    }
}