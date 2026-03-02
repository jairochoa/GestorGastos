@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.gestorgastos.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestorgastos.data.local.entity.ExpenseEntity
import com.example.gestorgastos.data.local.model.CurrencyCode
import com.example.gestorgastos.data.local.model.PaymentMethod
import com.example.gestorgastos.data.repository.ExpenseRepository
import com.example.gestorgastos.ui.parseAmountToMinor
import com.example.gestorgastos.ui.todayEpochDay
import kotlinx.coroutines.launch

class AddExpenseViewModel(
    private val expenseRepo: ExpenseRepository
) : ViewModel() {

    fun save(
        amountText: String,
        currency: CurrencyCode,
        concept: String,
        merchant: String?,
        address: String?,
        description: String?,
        paymentMethod: PaymentMethod,
        receiptUri: String?
    ): Boolean {
        val amountMinor = parseAmountToMinor(amountText, currency) ?: return false
        if (concept.isBlank()) return false

        viewModelScope.launch {
            val e = ExpenseEntity(
                amountMinor = amountMinor,
                currency = currency.name,
                concept = concept.trim(),
                description = description?.trim().takeUnless { it.isNullOrBlank() },
                merchant = merchant?.trim().takeUnless { it.isNullOrBlank() },
                address = address?.trim().takeUnless { it.isNullOrBlank() },
                paymentMethod = paymentMethod.name,
                categoryId = null,
                dateEpochDay = todayEpochDay(),
                receiptUri = receiptUri
            )
            expenseRepo.insert(e)
        }
        return true
    }
}