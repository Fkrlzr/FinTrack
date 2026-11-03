package com.fintrack.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sortOrder, label")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sortOrder, label")
    suspend fun getAll(): List<CategoryEntity>

    @Upsert
    suspend fun upsert(entity: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets ORDER BY id")
    fun observeAll(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets ORDER BY id")
    suspend fun getAll(): List<WalletEntity>

    @Upsert
    suspend fun upsert(entity: WalletEntity): Long

    @Query("DELETE FROM wallets WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets")
    fun observeAll(): Flow<List<BudgetEntity>>

    @Upsert
    suspend fun upsert(entity: BudgetEntity)

    @Query("DELETE FROM budgets WHERE categoryId = :categoryId")
    suspend fun delete(categoryId: String)
}

@Dao
interface RecurringDao {
    @Query("SELECT * FROM recurring ORDER BY nextDateMillis")
    fun observeAll(): Flow<List<RecurringEntity>>

    @Query("SELECT * FROM recurring WHERE nextDateMillis <= :now")
    suspend fun getDue(now: Long): List<RecurringEntity>

    @Upsert
    suspend fun upsert(entity: RecurringEntity): Long

    @Query("DELETE FROM recurring WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE recurring SET category = :to WHERE category = :from")
    suspend fun reassignCategory(from: String, to: String)

    @Query("UPDATE recurring SET walletId = :to WHERE walletId = :from")
    suspend fun reassignWallet(from: Long, to: Long)
}
