package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallet")
data class WalletEntity(
    @PrimaryKey val id: Int = 1,
    val balance: Long = 250000L, // Default initial balance Rp 250.000 for realistic testing
    val kilatCoins: Long = 1250L,
    val userName: String = "Sobat Kilat"
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val type: String, // GAME, PLN, PULSA, DATA, TOPUP_SALDO
    val title: String,
    val subtitle: String,
    val targetNumberOrId: String,
    val amountPrice: Long,
    val adminFee: Long,
    val totalPayment: Long,
    val paymentMethodName: String,
    val status: String, // BERHASIL, PENDING, GAGAL
    val serialNumberOrToken: String,
    val timestamp: Long,
    val notes: String = ""
)

@Entity(tableName = "saved_favorites")
data class SavedFavoriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // PLN, PULSA, GAME
    val label: String,
    val targetNumber: String,
    val extraData: String = ""
)
