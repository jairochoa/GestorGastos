@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.gestorgastos.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestorgastos.data.local.dao.CategoryTotal
import com.example.gestorgastos.data.local.dao.DailyTotal
import com.example.gestorgastos.data.local.model.CurrencyCode
import com.example.gestorgastos.ui.amountMinorToDecimalString
import java.time.LocalDate
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign


@Composable
fun ReportsScreen(
    state: ReportsUiState,
    onBack: () -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onCurrency: (CurrencyCode) -> Unit
) {
    val totalMinor = state.daily.sumOf { it.totalMinor }
    val totalByCatMinor = state.byCategory.sumOf { it.totalMinor }

    val totalStr = amountMinorToDecimalString(totalMinor, state.currency.decimals)
    val totalByCatStr = amountMinorToDecimalString(totalByCatMinor, state.currency.decimals)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes — ${state.month}") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } },
                actions = {
                    TextButton(onClick = onPrevMonth) { Text("◀") }
                    TextButton(onClick = onNextMonth) { Text("▶") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CurrencyPicker(state.currency, onCurrency)

                Column(horizontalAlignment = Alignment.End) {
                    Text("Total del período", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = "${state.currency.label} $totalStr",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            if (state.loading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            Text("Gastos por día (${state.currency.label})", style = MaterialTheme.typography.titleMedium)
            DailyBarChart(
                daily = state.daily,
                currency = state.currency
            )

            Divider()

            Text("Totales por categoría (${state.currency.label})", style = MaterialTheme.typography.titleMedium)
            CategoryTotalsList(state.byCategory, state.currency)
            Text(
                text = "Total (suma categorías): ${state.currency.label} $totalByCatStr",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun CurrencyPicker(selected: CurrencyCode, onCurrency: (CurrencyCode) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Moneda:")
        OutlinedButton(onClick = { expanded = true }) { Text(selected.label) }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            CurrencyCode.values().forEach { c ->
                DropdownMenuItem(
                    text = { Text(c.label) },
                    onClick = {
                        expanded = false
                        onCurrency(c)
                    }
                )
            }
        }
    }
}

@Composable
private fun DailyBarChart(daily: List<DailyTotal>, currency: CurrencyCode) {
    if (daily.isEmpty()) {
        Text("No hay datos para este mes/moneda.")
        return
    }

    val max = daily.maxOf { it.totalMinor }.coerceAtLeast(1)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        daily.forEach { d ->
            val day = LocalDate.ofEpochDay(d.dateEpochDay.toLong()).dayOfMonth
            val frac = (d.totalMinor.toFloat() / max.toFloat()).coerceIn(0f, 1f)
            val amt = amountMinorToDecimalString(d.totalMinor, currency.decimals)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(day.toString().padStart(2, '0'), modifier = Modifier.width(34.dp))

                // Contenedor de barra ocupa el espacio disponible
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(frac)
                    ) {}
                }

                Spacer(Modifier.width(10.dp))

                Text(
                    text = "${currency.label} $amt",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(130.dp)
                )
            }
        }
    }
}

@Composable
private fun CategoryTotalsList(byCategory: List<CategoryTotal>, currency: CurrencyCode) {
    if (byCategory.isEmpty()) {
        Text("No hay datos por categoría.")
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        byCategory.forEach { c ->
            val name = c.categoryName ?: "Sin categoría"
            val amt = amountMinorToDecimalString(c.totalMinor, currency.decimals)

            Row(Modifier.fillMaxWidth()) {
                Text(
                    text = name,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${currency.label} $amt",
                    modifier = Modifier.width(140.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}