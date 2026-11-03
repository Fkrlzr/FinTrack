package com.fintrack.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fintrack.domain.model.Budget

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val categoryId: String,
    val limitMinor: Long,
)

fun BudgetEntity.toDomain() = Budget(categoryId, limitMinor)

fun Budget.toEntity() = BudgetEntity(categoryId, limitMinor)
