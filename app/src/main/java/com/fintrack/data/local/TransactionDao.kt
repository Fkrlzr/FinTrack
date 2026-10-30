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
            "WHERE dateMillis >= :startMillis AND dateMillis < :endMillis " +
            "ORDER BY dateMillis DESC, id DESC"
    )
    fun observeInRange(startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>>

    @Query(
        "SELECT COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amountMinor ELSE -amountMinor END), 0) " +
            "FROM transactions"
    )
    fun observeBalance(): Flow<Long>

    @Query(
        "SELECT COALESCE(SUM(amountMinor), 0) FROM transactions " +
            "WHERE type = :type AND dateMillis >= :startMillis AND dateMillis < :endMillis"
    )
    fun observeSum(type: String, startMillis: Long, endMillis: Long): Flow<Long>

    @Upsert
    suspend fun upsert(entity: TransactionEntity): Long

    @Delete
    suspend fun delete(entity: TransactionEntity)
}
