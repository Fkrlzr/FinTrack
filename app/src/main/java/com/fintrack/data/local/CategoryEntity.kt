package com.fintrack.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fintrack.domain.model.Category

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val label: String,
    val sortOrder: Int,
)

fun CategoryEntity.toDomain() = Category(id, label, sortOrder)

fun Category.toEntity() = CategoryEntity(id, label, sortOrder)
