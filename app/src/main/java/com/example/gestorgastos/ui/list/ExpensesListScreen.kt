@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.gestorgastos.ui.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestorgastos.data.local.model.CurrencyCode
import com.example.gestorgastos.ui.formatMinor
import androidx.compose.ui.text.style.TextOverflow
import com.example.gestorgastos.ui.formatEpochDay
import androidx.compose.foundation.clickable

@Composable
fun ExpensesListScreen(
    state: ExpensesListUiState,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onInsertDemo: () -> Unit,
    onAdd: () -> Unit,
    onExportCsv: () -> Unit,
    onSetupPin: () -> Unit,
    onCategories: () -> Unit,
    onOpenExpense: (Long) -> Unit,
    onReports: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    text = "Gastos — ${state.month}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                ) },
                actions = {
                    TextButton(onClick = onPrevMonth) { Text("◀") }
                    TextButton(onClick = onNextMonth) { Text("▶") }
                    TextButton(onClick = onExportCsv) { Text("CSV") }
                    TextButton(onClick = onSetupPin) { Text("PIN") }
                    TextButton(onClick = onCategories) { Text("Categorías") }
                    TextButton(onClick = onReports) { Text("REP") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) { Text("+") }
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(12.dp)) {

            // Botón útil para probar el flujo en vivo
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(onClick = onInsertDemo) { Text("Insertar demo") }
            }

            Spacer(Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.rows, key = { it.expense.id }) { row ->
                    val e = row.expense
                    val currency = runCatching { CurrencyCode.valueOf(e.currency) }.getOrNull()
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenExpense(row.expense.id) }) {
                        Column(Modifier.padding(12.dp)) {
                            Text(e.concept, style = MaterialTheme.typography.titleMedium)
                            Text("Categoría: ${row.categoryName ?: "—"}")
                            Text("Método: ${e.paymentMethod}  •  Fecha: ${formatEpochDay(e.dateEpochDay)}")
                            Text(
                                text = currency?.let { formatMinor(e.amountMinor, it) }
                                    ?: "${e.currency} ${e.amountMinor}"
                            )
                            if (!e.merchant.isNullOrBlank()) Text("Comercio: ${e.merchant}")
                            if (!e.address.isNullOrBlank()) Text("Dirección: ${e.address}")
                            if (!e.description.isNullOrBlank()) Text("Desc: ${e.description}")
                            if (!e.receiptUri.isNullOrBlank()) Text("Recibo: ✔")
                        }
                    }
                }
            }
        }
    }
}