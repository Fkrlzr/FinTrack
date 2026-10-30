package com.fintrack.domain.model

enum class TransactionType { INCOME, EXPENSE }

enum class Category(val label: String) {
    FOOD("Food"),
    TRANSPORT("Transport"),
    UTILITIES("Utilities"),
    SALARY("Salary"),
    SHOPPING("Shopping"),
    OTHERS("Others"),
}

/** Amounts are whole rupiah (IDR), stored as Long to avoid floating point drift. Always positive. */
data class Transaction(
    val id: Long = 0,
    val amountMinor: Long,
    val type: TransactionType,
    val category: Category,
    val note: String,
    val dateMillis: Long,
)

data class MonthTotals(
    val incomeMinor: Long,
    val expenseMinor: Long,
)
