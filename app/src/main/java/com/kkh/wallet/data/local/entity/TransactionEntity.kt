package com.kkh.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index("walletId"),
        Index("destinationWalletId"),
        Index("categoryId"),
        Index("dateMillis")
    ],
    foreignKeys = [
        ForeignKey(
            entity = WalletEntity::class,
            parentColumns = ["id"],
            childColumns = ["walletId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WalletEntity::class,
            parentColumns = ["id"],
            childColumns = ["destinationWalletId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val walletId: Long,
    val destinationWalletId: Long?,
    val categoryId: Long?,
    val amount: Double,
    val type: String,
    val description: String,
    val merchant: String,
    val dateMillis: Long,
    val attachmentUri: String?,
    val tags: List<String>,
    val createdAt: Long
)
