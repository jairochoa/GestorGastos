package com.example.gestorgastos.data.local.model

enum class CurrencyCode(val decimals: Int, val label: String) {
    USD(2, "USD"),
    COP(0, "COP"),
    VES(2, "VES") // tú lo llamas VEB; internamente guardamos VES
}