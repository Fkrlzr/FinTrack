package com.fintrack.domain.model

enum class TransactionType { INCOME, EXPENSE }

enum class Frequency { WEEKLY, MONTHLY }

/**
 * Category id is a stable key. The defaults use the old enum names (FOOD, OTHERS, ...), so
 * transactions stored before version 2 keep their category.
 */
data class Category(val id: String, val label: String, val sortOrder: Int)

data class Wallet(val id: Long = 0, val name: String, val openingMinor: Long)

data class WalletSummary(val wallet: Wallet, val balanceMinor: Long)

/** Amounts are whole rupiah (IDR), stored as Long to avoid floating point drift. Always positive. */
data class Transaction(
    val id: Long = 0,
    val amountMinor: Long,
    val type: TransactionType,
    val categoryId: String,
    val walletId: Long,
    val note: String,
    val dateMillis: Long,
)

data class Budget(val categoryId: String, val limitMinor: Long)

data class Recurring(
    val id: Long = 0,
    val amountMinor: Long,
    val type: TransactionType,
    val categoryId: String,
    val walletId: Long,
    val note: String,
    val frequency: Frequency,
    val nextDateMillis: Long,
)

data class MonthTotals(
    val incomeMinor: Long,
    val expenseMinor: Long,
)

const val DEFAULT_WALLET_ID = 1L
const val FALLBACK_CATEGORY_ID = "OTHERS"
