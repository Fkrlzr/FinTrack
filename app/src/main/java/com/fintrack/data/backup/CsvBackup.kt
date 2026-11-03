package com.fintrack.data.backup

import com.fintrack.domain.model.TransactionType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** Plain CSV in the user's own format: one row per transaction, names instead of internal ids. */
object CsvBackup {
    const val HEADER = "date,type,category,wallet,amount_idr,note"

    private val stamp = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    data class Row(
        val dateTime: LocalDateTime,
        val type: TransactionType,
        val category: String,
        val wallet: String,
        val amountMinor: Long,
        val note: String,
    )

    fun encode(rows: List<Row>): String = buildString {
        appendLine(HEADER)
        rows.forEach { r ->
            val fields = listOf(
                r.dateTime.format(stamp),
                r.type.name,
                r.category,
                r.wallet,
                r.amountMinor.toString(),
                r.note,
            )
            appendLine(fields.joinToString(",") { escape(it) })
        }
    }

    /** Throws [IllegalArgumentException] with a readable message when the file is not a valid backup. */
    fun decode(text: String): List<Row> {
        val records = parseRecords(text).filter { record -> record.any { it.isNotBlank() } }
        require(records.isNotEmpty() && records.first().joinToString(",") == HEADER) {
            "This file is not a FinTrack backup."
        }
        return records.drop(1).mapIndexed { index, f ->
            val line = index + 2
            require(f.size >= 6) { "Line $line has ${f.size} fields, expected 6." }
            val amount = f[4].trim().toLongOrNull()
            require(amount != null && amount > 0) { "Line $line has an invalid amount." }
            Row(
                dateTime = LocalDateTime.parse(f[0].trim(), stamp),
                type = TransactionType.valueOf(f[1].trim().uppercase()),
                category = f[2].trim(),
                wallet = f[3].trim(),
                amountMinor = amount,
                note = f[5],
            )
        }
    }

    private fun escape(value: String): String =
        if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }

    private fun parseRecords(text: String): List<List<String>> {
        val records = mutableListOf<List<String>>()
        var fields = mutableListOf<String>()
        val field = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < text.length) {
            val c = text[i]
            when {
                inQuotes -> when {
                    c == '"' && i + 1 < text.length && text[i + 1] == '"' -> {
                        field.append('"')
                        i++
                    }
                    c == '"' -> inQuotes = false
                    else -> field.append(c)
                }
                c == '"' -> inQuotes = true
                c == ',' -> {
                    fields.add(field.toString())
                    field.clear()
                }
                c == '\n' || c == '\r' -> {
                    if (c == '\r' && i + 1 < text.length && text[i + 1] == '\n') i++
                    fields.add(field.toString())
                    field.clear()
                    records.add(fields)
                    fields = mutableListOf()
                }
                else -> field.append(c)
            }
            i++
        }
        if (field.isNotEmpty() || fields.isNotEmpty()) {
            fields.add(field.toString())
            records.add(fields)
        }
        return records
    }
}
