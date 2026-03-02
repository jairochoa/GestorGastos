package com.example.gestorgastos.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestorgastos.data.local.entity.ExpenseEntity
import com.example.gestorgastos.data.local.model.CurrencyCode
import com.example.gestorgastos.data.local.model.PaymentMethod
import com.example.gestorgastos.data.repository.ExpenseRepository
import com.example.gestorgastos.ui.parseAmountToMinor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class EditExpenseUiState(
    val loaded: Boolean = false,
    val expense: ExpenseEntity? = null
)

class EditExpenseViewModel(
    private val repo: ExpenseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditExpenseUiState())
    val state: StateFlow<EditExpenseUiState> = _state

    fun load(id: Long) {
        viewModelScope.launch {
            val e = repo.getById(id)
            _state.value = EditExpenseUiState(loaded = true, expense = e)
        }
    }

    fun save(
        original: ExpenseEntity,
        amountText: String,
        currency: CurrencyCode,
        concept: String,
        merchant: String?,
        address: String?,
        description: String?,
        paymentMethod: PaymentMethod,
        receiptUri: String?,
        categoryId: Long?
    ): Boolean {
        val amountMinor = parseAmountToMinor(amountText, currency) ?: return false
        if (concept.isBlank()) return false

        viewModelScope.launch {
            repo.update(
                original.copy(
                    amountMinor = amountMinor,
                    currency = currency.name,
                    concept = concept.trim(),
                    merchant = merchant?.trim().takeUnless { it.isNullOrBlank() },
                    address = address?.trim().takeUnless { it.isNullOrBlank() },
                    description = description?.trim().takeUnless { it.isNullOrBlank() },
                    paymentMethod = paymentMethod.name,
                    receiptUri = receiptUri,
                    categoryId = categoryId,
                    updatedAtEpochMillis = System.currentTimeMillis()
                )
            )
        }
        return true
    }

    fun delete(expense: ExpenseEntity) {
        viewModelScope.launch { repo.delete(expense) }
    }
}