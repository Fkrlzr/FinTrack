package com.fintrack.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fintrack.domain.model.Transaction
import com.fintrack.domain.model.TransactionType

@Entity(
    tableName = "transactions",
    indices = [Index("dateMillis")],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMinor: Long,
    val type: String,
    val category: String,
    @ColumnInfo(defaultValue = "1") val walletId: Long,
    val note: String,
    val dateMillis: Long,
)

data class CategorySumRow(val categoryId: String, val expenseMinor: Long)

data class WalletNetRow(val walletId: Long, val netMinor: Long)

fun TransactionEntity.toDomain() = Transaction(
    id = id,
    amountMinor = amountMinor,
    type = TransactionType.valueOf(type),
    categoryId = category,
    walletId = walletId,
    note = note,
    dateMillis = dateMillis,
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    amountMinor = amountMinor,
    type = type.name,
    category = categoryId,
    walletId = walletId,
    note = note,
    dateMillis = dateMillis,
)
