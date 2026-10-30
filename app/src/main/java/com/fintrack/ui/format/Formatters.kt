package com.fintrack.ui.format

import com.fintrack.domain.model.TransactionType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private const val MAX_DIGITS = 12

private val idSymbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
    groupingSeparator = '.'
    decimalSeparator = ','
}

/** "Rp 1.234.500", with a leading "-" for negative values. Rupiah has no minor unit in daily use. */
fun formatMoney(amount: Long): String {
    val text = "Rp " + DecimalFormat("#,##0", idSymbols).format(abs(amount))
    return if (amount < 0) "-$text" else text
}

fun formatSigned(amount: Long, type: TransactionType): String =
    (if (type == TransactionType.INCOME) "+ " else "- ") + formatMoney(amount)

fun formatNet(amount: Long): String =
    (if (amount < 0) "- " else "+ ") + formatMoney(abs(amount))

/** Editable form of an amount without grouping. */
fun formatPlainAmount(amount: Long): String = amount.toString()

/** Keeps digits only. */
fun sanitizeAmountInput(raw: String): String = raw.filter { it.isDigit() }.trimStart('0').take(MAX_DIGITS)

/** Returns null for empty or zero input. */
fun parseAmount(text: String): Long? = text.toLongOrNull()?.takeIf { it > 0 }
private val zone: ZoneId get() = ZoneId.systemDefault()
private val dayFormatter = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault())
private val fullDayFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault())
private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
private val shortMonthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.getDefault())

fun millisToLocalDate(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

fun monthRangeMillis(month: YearMonth): Pair<Long, Long> {
    val start = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
    val end = month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
    return start to end
}

/** Moves [millis] to [date], keeping its local time of day. */
fun withLocalDate(millis: Long, date: LocalDate): Long {
    val time: LocalTime = Instant.ofEpochMilli(millis).atZone(zone).toLocalTime()
    return date.atTime(time).atZone(zone).toInstant().toEpochMilli()
}

/** Material DatePicker works in UTC midnight millis. */
fun LocalDate.toPickerMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

fun pickerMillisToLocalDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

fun formatDayHeader(date: LocalDate, today: LocalDate = LocalDate.now()): String = when (date) {
    today -> "Today"
    today.minusDays(1) -> "Yesterday"
    else -> date.format(dayFormatter)
}

fun formatFullDate(date: LocalDate): String = date.format(fullDayFormatter)

fun formatMonth(month: YearMonth): String = month.format(monthFormatter)

fun formatShortMonth(month: YearMonth): String = month.format(shortMonthFormatter)
