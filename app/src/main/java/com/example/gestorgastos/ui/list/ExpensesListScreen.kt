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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.input.KeyboardType
import com.example.gestorgastos.data.local.dao.ExpenseListRow
import com.example.gestorgastos.ui.amountMinorToDecimalString
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment


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
    var query by rememberSaveable { mutableStateOf("") }
    var filterCurrency by rememberSaveable { mutableStateOf<String?>(null) }   // "USD" / "COP" / "VES"
    var filterCategory by rememberSaveable { mutableStateOf<String?>(null) }   // nombre categoría o null
    var filterMethod by rememberSaveable { mutableStateOf<String?>(null) }     // "CASH" / etc

    val currencyOptions = remember(state.rows) {
        state.rows.map { it.expense.currency }.distinct().sorted()
    }
    val categoryOptions = remember(state.rows) {
        state.rows.map { it.categoryName ?: "Sin categoría" }.distinct().sorted()
    }
    val methodOptions = remember(state.rows) {
        state.rows.map { it.expense.paymentMethod }.distinct().sorted()
    }

    val q = query.trim()
    val visibleRows = state.rows
        .asSequence()
        .filter { row ->
            // filtro moneda
            filterCurrency == null || row.expense.currency == filterCurrency
        }
        .filter { row ->
            // filtro categoría (por nombre)
            if (filterCategory == null) true
            else {
                val name = row.categoryName ?: "Sin categoría"
                name == filterCategory
            }
        }
        .filter { row ->
            // filtro metodo
            filterMethod == null || row.expense.paymentMethod == filterMethod
        }
        .filter { row ->
            // búsqueda
            if (q.isBlank()) true else matchesExpenseQuery(row, q)
        }
        .toList()
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

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Buscar (concepto, comercio o monto)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                trailingIcon = {
                    if (query.isNotBlank()) {
                        TextButton(onClick = { query = "" }) { Text("Limpiar") }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Mostrando ${visibleRows.size} de ${state.rows.size}",
                style = MaterialTheme.typography.labelMedium
            )

            // Botón útil para probar el flujo en vivo
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(onClick = onInsertDemo) { Text("Insertar demo") }
            }

            Spacer(Modifier.height(8.dp))

            FiltersRow(
                currencyOptions = currencyOptions,
                selectedCurrency = filterCurrency,
                onCurrency = { filterCurrency = it },

                categoryOptions = categoryOptions,
                selectedCategory = filterCategory,
                onCategory = { filterCategory = it },

                methodOptions = methodOptions,
                selectedMethod = filterMethod,
                onMethod = { filterMethod = it },

                onReset = {
                    filterCurrency = null
                    filterCategory = null
                    filterMethod = null
                }
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(visibleRows, key = { it.expense.id }) { row ->
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

private fun matchesExpenseQuery(row: ExpenseListRow, rawQuery: String): Boolean {
    val q = rawQuery.trim().lowercase()
    if (q.isBlank()) return true

    val e = row.expense

    val concept = e.concept.lowercase()
    val merchant = (e.merchant ?: "").lowercase()
    val category = (row.categoryName ?: "").lowercase()
    val description = (e.description ?: "").lowercase()


    // Coincidencia por texto
    if (concept.contains(q) || merchant.contains(q) || category.contains(q) || description.contains(q)) return true

    // Coincidencia por monto (busca en amountMinor y en formato decimal)
    val qNum = q.replace(",", ".")
        .filter { it.isDigit() || it == '.' }

    if (qNum.isNotBlank()) {
        // 1) Buscar en amountMinor directo (ej: 1200)
        if (e.amountMinor.toString().contains(qNum.filter { it.isDigit() })) return true

        // 2) Buscar en decimal (ej: 12.00)
        val decimals = runCatching { CurrencyCode.valueOf(e.currency).decimals }.getOrElse { 2 }
        val formatted = amountMinorToDecimalString(e.amountMinor, decimals) // "12.00"
        if (formatted.contains(qNum)) return true
    }

    return false
}

@Composable
private fun FiltersRow(
    currencyOptions: List<String>,
    selectedCurrency: String?,
    onCurrency: (String?) -> Unit,

    categoryOptions: List<String>,
    selectedCategory: String?,
    onCategory: (String?) -> Unit,

    methodOptions: List<String>,
    selectedMethod: String?,
    onMethod: (String?) -> Unit,

    onReset: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SimpleDropdown(
                label = "Moneda",
                options = currencyOptions,
                selected = selectedCurrency,
                onSelected = onCurrency,
                modifier = Modifier.weight(1f)
            )
            SimpleDropdown(
                label = "Categoría",
                options = categoryOptions,
                selected = selectedCategory,
                onSelected = onCategory,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SimpleDropdown(
                label = "Método",
                options = methodOptions,
                selected = selectedMethod,
                onSelected = onMethod,
                modifier = Modifier.weight(1f)
            )

            OutlinedButton(onClick = onReset) { Text("Reset") }
        }
    }
}

@Composable
private fun SimpleDropdown(
    label: String,
    options: List<String>,
    selected: String?,
    onSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selected ?: "Todos", maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Todos") },
                onClick = { expanded = false; onSelected(null) }
            )
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = { expanded = false; onSelected(opt) }
                )
            }
        }
    }
}