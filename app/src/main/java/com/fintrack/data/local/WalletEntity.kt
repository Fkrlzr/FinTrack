package com.fintrack.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fintrack.domain.model.Wallet

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val openingMinor: Long,
)

fun WalletEntity.toDomain() = Wallet(id, name, openingMinor)

fun Wallet.toEntity() = WalletEntity(id, name, openingMinor)
