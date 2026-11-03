package com.fintrack.domain.repository

import com.fintrack.domain.model.Budget
import com.fintrack.domain.model.Category
import com.fintrack.domain.model.MonthTotals
import com.fintrack.domain.model.Recurring
import com.fintrack.domain.model.Transaction
import com.fintrack.domain.model.Wallet
import com.fintrack.domain.model.WalletSummary
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    /** Range is [startMillis, endMillis). */
    fun observeTotals(startMillis: Long, endMillis: Long): Flow<MonthTotals>

    /** Newest first. Range is [startMillis, endMillis). */
    fun observeTransactions(startMillis: Long, endMillis: Long): Flow<List<Transaction>>

    /** Matches note text or amount across all months. Newest first. */
    fun search(term: String): Flow<List<Transaction>>

    /** Expense totals keyed by category id, for the given range. */
    fun observeExpenseByCategory(startMillis: Long, endMillis: Long): Flow<Map<String, Long>>

    /** Inserts when id is 0, otherwise replaces the row with that id. */
    suspend fun upsert(transaction: Transaction)

    suspend fun delete(transaction: Transaction)

    fun observeCategories(): Flow<List<Category>>
    suspend fun saveCategory(category: Category)

    /** Entries in the deleted category move to [FALLBACK_CATEGORY_ID]. */
    suspend fun deleteCategory(id: String)

    fun observeWallets(): Flow<List<WalletSummary>>
    suspend fun saveWallet(wallet: Wallet)

    /** Entries in the deleted wallet move to [DEFAULT_WALLET_ID]. The default wallet cannot be deleted. */
    suspend fun deleteWallet(id: Long)

    fun observeBudgets(): Flow<List<Budget>>
    suspend fun saveBudget(budget: Budget)
    suspend fun deleteBudget(categoryId: String)

    fun observeRecurring(): Flow<List<Recurring>>
    suspend fun saveRecurring(item: Recurring)
    suspend fun deleteRecurring(id: Long)

    /** Creates a transaction for every occurrence that is due at [nowMillis] and moves each schedule forward. */
    suspend fun processDueRecurring(nowMillis: Long)

    suspend fun exportCsv(): String

    /** Adds rows from CSV text. Returns the number of rows imported. */
    suspend fun importCsv(text: String): Int
}
