package com.example.gestorgastos.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestorgastos.data.local.dao.CategoryTotal
import com.example.gestorgastos.data.local.dao.DailyTotal
import com.example.gestorgastos.data.local.model.CurrencyCode
import com.example.gestorgastos.data.repository.ExpenseRepository
import com.example.gestorgastos.ui.monthRangeEpochDays
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.YearMonth

data class ReportsUiState(
    val month: YearMonth = YearMonth.now(),
    val currency: CurrencyCode = CurrencyCode.USD,
    val loading: Boolean = false,
    val daily: List<DailyTotal> = emptyList(),
    val byCategory: List<CategoryTotal> = emptyList()
)

class ReportsViewModel(
    private val repo: ExpenseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReportsUiState())
    val state: StateFlow<ReportsUiState> = _state

    fun load() {
        val current = _state.value
        val (fromDay, toDay) = monthRangeEpochDays(current.month)

        _state.value = current.copy(loading = true)

        viewModelScope.launch {
            val currencyName = current.currency.name
            val daily = repo.dailyTotalsForCurrency(fromDay, toDay, currencyName)
            val byCat = repo.totalsByCategoryForCurrency(fromDay, toDay, currencyName)
            _state.value = _state.value.copy(loading = false, daily = daily, byCategory = byCat)
        }
    }

    fun prevMonth() {
        _state.value = _state.value.copy(month = _state.value.month.minusMonths(1))
        load()
    }

    fun nextMonth() {
        _state.value = _state.value.copy(month = _state.value.month.plusMonths(1))
        load()
    }

    fun setCurrency(c: CurrencyCode) {
        _state.value = _state.value.copy(currency = c)
        load()
    }
}