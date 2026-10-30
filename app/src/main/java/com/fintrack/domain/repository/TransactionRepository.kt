package com.fintrack.domain.repository

import com.fintrack.domain.model.MonthTotals
import com.fintrack.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    /** Range is [startMillis, endMillis). */
    fun observeTotals(startMillis: Long, endMillis: Long): Flow<MonthTotals>

    /** Newest first. Range is [startMillis, endMillis). */
    fun observeTransactions(startMillis: Long, endMillis: Long): Flow<List<Transaction>>

    /** Inserts when id is 0, otherwise replaces the row with that id. */
    suspend fun upsert(transaction: Transaction)

    suspend fun delete(transaction: Transaction)
}
