package com.example.gestorgastos.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestorgastos.data.local.model.CurrencyCode
import com.example.gestorgastos.data.local.model.PaymentMethod
import com.example.gestorgastos.data.local.entity.ExpenseEntity
import com.example.gestorgastos.data.repository.ExpenseRepository
import com.example.gestorgastos.ui.monthRangeEpochDays
import com.example.gestorgastos.ui.todayEpochDay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth

data class ExpensesListUiState(
    val month: YearMonth = YearMonth.now(),
    val rows: List<com.example.gestorgastos.data.local.dao.ExpenseListRow> = emptyList()
)

class ExpensesListViewModel(
    private val expenseRepo: ExpenseRepository
) : ViewModel() {

    private val monthFlow = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<ExpensesListUiState> =
        monthFlow.flatMapLatest { ym ->
            val (fromDay, toDay) = monthRangeEpochDays(ym)
            expenseRepo.observeListRows(fromDay, toDay)
                .map { rows -> ExpensesListUiState(month = ym, rows = rows) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExpensesListUiState())

    fun prevMonth() { monthFlow.update { it.minusMonths(1) } }
    fun nextMonth() { monthFlow.update { it.plusMonths(1) } }

    // Para probar rápido que el Flow actualiza la pantalla
    fun insertDemo() {
        viewModelScope.launch {
            val e = ExpenseEntity(
                amountMinor = 1200,
                currency = CurrencyCode.USD.name,
                concept = "Demo",
                description = "Gasto de prueba",
                merchant = "Demo Store",
                address = null,
                paymentMethod = PaymentMethod.CASH.name,
                categoryId = null,
                dateEpochDay = todayEpochDay(),
                receiptUri = null
            )
            expenseRepo.insert(e)
        }
    }
}