package com.kkh.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val type: String,
    val providerName: String,
    val iconKey: String,
    val colorHex: String,
    val currentBalance: Double,
    val creditLimit: Double,
    val usedLimit: Double,
    val billingDate: Int?,
    val dueDate: Int?,
    val notes: String,
    val isArchived: Boolean,
    val createdAt: Long
)
