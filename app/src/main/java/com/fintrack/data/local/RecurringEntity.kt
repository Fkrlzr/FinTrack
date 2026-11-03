package com.fintrack.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fintrack.domain.model.Frequency
import com.fintrack.domain.model.Recurring
import com.fintrack.domain.model.TransactionType

@Entity(tableName = "recurring")
data class RecurringEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMinor: Long,
    val type: String,
    val category: String,
    val walletId: Long,
    val note: String,
    val frequency: String,
    val nextDateMillis: Long,
)

fun RecurringEntity.toDomain() = Recurring(
    id = id,
    amountMinor = amountMinor,
    type = TransactionType.valueOf(type),
    categoryId = category,
    walletId = walletId,
    note = note,
    frequency = Frequency.valueOf(frequency),
    nextDateMillis = nextDateMillis,
)

fun Recurring.toEntity() = RecurringEntity(
    id = id,
    amountMinor = amountMinor,
    type = type.name,
    category = categoryId,
    walletId = walletId,
    note = note,
    frequency = frequency.name,
    nextDateMillis = nextDateMillis,
)
