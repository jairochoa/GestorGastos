package com.example.gestorgastos.ui

import com.example.gestorgastos.data.local.model.CurrencyCode
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault())

fun formatEpochDay(epochDay: Int): String =
    java.time.LocalDate.ofEpochDay(epochDay.toLong()).format(DATE_FMT)

fun monthRangeEpochDays(ym: YearMonth): Pair<Int, Int> {
    val start = ym.atDay(1).toEpochDay().toInt()
    val end = ym.atEndOfMonth().toEpochDay().toInt()
    return start to end
}

fun parseAmountToMinor(input: String, currency: CurrencyCode): Long? {
    val norm = input.trim().replace(",", ".")
    val bd = norm.toBigDecimalOrNull() ?: return null
    val scale = currency.decimals
    return try {
        bd.setScale(scale, RoundingMode.HALF_UP)
            .movePointRight(scale)
            .longValueExact()
    } catch (_: Exception) {
        null
    }
}

fun formatMinor(amountMinor: Long, currency: CurrencyCode): String {
    val scale = currency.decimals
    val bd = amountMinor.toBigDecimal().movePointLeft(scale)
    return "${currency.label} ${bd.toPlainString()}"
}

fun todayEpochDay(): Int = LocalDate.now().toEpochDay().toInt()

fun amountMinorToDecimalString(amountMinor: Long, currencyDecimals: Int): String {
    // 1200 con 2 decimales => "12.00"
    val bd = amountMinor.toBigDecimal().movePointLeft(currencyDecimals)
    return bd.toPlainString()
}