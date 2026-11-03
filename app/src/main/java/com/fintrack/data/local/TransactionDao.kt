package com.fintrack.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query(
        "SELECT * FROM transactions " +
            "WHERE dateMillis >= :start AND dateMillis < :end " +
            "ORDER BY dateMillis DESC, id DESC"
    )
    fun observeInRange(start: Long, end: Long): Flow<List<TransactionEntity>>

    @Query(
        "SELECT * FROM transactions " +
            "WHERE note LIKE '%' || :term || '%' OR CAST(amountMinor AS TEXT) LIKE '%' || :term || '%' " +
            "ORDER BY dateMillis DESC, id DESC"
    )
    fun search(term: String): Flow<List<TransactionEntity>>

    @Query(
        "SELECT COALESCE(SUM(amountMinor), 0) FROM transactions " +
            "WHERE type = :type AND dateMillis >= :start AND dateMillis < :end"
    )
    fun observeSum(type: String, start: Long, end: Long): Flow<Long>

    @Query(
        "SELECT category AS categoryId, SUM(amountMinor) AS expenseMinor FROM transactions " +
            "WHERE type = 'EXPENSE' AND dateMillis >= :start AND dateMillis < :end GROUP BY category"
    )
    fun observeExpenseByCategory(start: Long, end: Long): Flow<List<CategorySumRow>>

    @Query(
        "SELECT walletId, SUM(CASE WHEN type = 'INCOME' THEN amountMinor ELSE -amountMinor END) AS netMinor " +
            "FROM transactions GROUP BY walletId"
    )
    fun observeWalletNet(): Flow<List<WalletNetRow>>

    @Query("SELECT * FROM transactions ORDER BY dateMillis ASC, id ASC")
    suspend fun getAll(): List<TransactionEntity>

    @Upsert
    suspend fun upsert(entity: TransactionEntity): Long

    @Delete
    suspend fun delete(entity: TransactionEntity)

    @Query("UPDATE transactions SET category = :to WHERE category = :from")
    suspend fun reassignCategory(from: String, to: String)

    @Query("UPDATE transactions SET walletId = :to WHERE walletId = :from")
    suspend fun reassignWallet(from: Long, to: Long)
}
