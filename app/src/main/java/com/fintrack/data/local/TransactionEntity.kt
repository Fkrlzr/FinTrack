package com.fintrack.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fintrack.domain.model.Category
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
    val note: String,
    val dateMillis: Long,
)

fun TransactionEntity.toDomain() = Transaction(
    id = id,
    amountMinor = amountMinor,
    type = TransactionType.valueOf(type),
    category = Category.valueOf(category),
    note = note,
    dateMillis = dateMillis,
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    amountMinor = amountMinor,
    type = type.name,
    category = category.name,
    note = note,
    dateMillis = dateMillis,
)
