package com.kkh.wallet.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.kkh.wallet.data.local.KKHDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Exports and imports the full database as a single JSON document. Designed for
 * personal backups — not encrypted on its own (the user should pick a safe Uri,
 * e.g. Google Drive or a private folder).
 */
@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: KKHDatabase
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Serializable
    data class WalletDto(
        val id: Long, val name: String, val type: String, val providerName: String,
        val iconKey: String, val colorHex: String, val currentBalance: Double,
        val creditLimit: Double, val usedLimit: Double,
        val billingDate: Int? = null, val dueDate: Int? = null,
        val notes: String = "", val isArchived: Boolean = false, val createdAt: Long
    )

    @Serializable
    data class TransactionDto(
        val id: Long, val walletId: Long, val destinationWalletId: Long? = null,
        val categoryId: Long? = null, val amount: Double, val type: String,
        val description: String = "", val merchant: String = "", val dateMillis: Long,
        val attachmentUri: String? = null, val tags: List<String> = emptyList(),
        val createdAt: Long
    )

    @Serializable
    data class CategoryDto(
        val id: Long, val name: String, val iconKey: String, val colorHex: String,
        val isIncome: Boolean, val isCustom: Boolean = false, val isArchived: Boolean = false
    )

    @Serializable
    data class BudgetDto(
        val id: Long, val categoryId: Long? = null, val monthlyAmount: Double,
        val warnThresholdPercent: Int = 80, val createdAt: Long
    )

    @Serializable
    data class BackupPayload(
        val version: Int = 1,
        val exportedAt: Long,
        val wallets: List<WalletDto>,
        val transactions: List<TransactionDto>,
        val categories: List<CategoryDto>,
        val budgets: List<BudgetDto>
    )

    suspend fun export(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val wallets = database.walletDao().listAllOnce().map {
                WalletDto(
                    id = it.id, name = it.name, type = it.type, providerName = it.providerName,
                    iconKey = it.iconKey, colorHex = it.colorHex, currentBalance = it.currentBalance,
                    creditLimit = it.creditLimit, usedLimit = it.usedLimit,
                    billingDate = it.billingDate, dueDate = it.dueDate, notes = it.notes,
                    isArchived = it.isArchived, createdAt = it.createdAt
                )
            }
            val txs = database.transactionDao().listAllOnce().map {
                TransactionDto(
                    id = it.id, walletId = it.walletId, destinationWalletId = it.destinationWalletId,
                    categoryId = it.categoryId, amount = it.amount, type = it.type,
                    description = it.description, merchant = it.merchant, dateMillis = it.dateMillis,
                    attachmentUri = it.attachmentUri, tags = it.tags, createdAt = it.createdAt
                )
            }
            val cats = database.categoryDao().listAllOnce().map {
                CategoryDto(it.id, it.name, it.iconKey, it.colorHex, it.isIncome, it.isCustom, it.isArchived)
            }
            val budgets = database.budgetDao().listAllOnce().map {
                BudgetDto(it.id, it.categoryId, it.monthlyAmount, it.warnThresholdPercent, it.createdAt)
            }
            val payload = BackupPayload(
                exportedAt = System.currentTimeMillis(),
                wallets = wallets, transactions = txs, categories = cats, budgets = budgets
            )
            val text = json.encodeToString(BackupPayload.serializer(), payload)
            context.contentResolver.openOutputStream(uri, "wt")?.use { os ->
                os.write(text.toByteArray(Charsets.UTF_8))
            } ?: error("Cannot open output stream")
            wallets.size + txs.size + cats.size + budgets.size
        }
    }

    suspend fun import(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val text = context.contentResolver.openInputStream(uri)?.use {
                it.readBytes().toString(Charsets.UTF_8)
            } ?: error("Cannot open input stream")
            val payload = json.decodeFromString(BackupPayload.serializer(), text)
            database.withTransaction {
                database.budgetDao().deleteAll()
                database.transactionDao().deleteAll()
                database.walletDao().deleteAll()
                database.categoryDao().deleteAll()

                database.categoryDao().insertAll(payload.categories.map {
                    com.kkh.wallet.data.local.entity.CategoryEntity(
                        id = it.id, name = it.name, iconKey = it.iconKey, colorHex = it.colorHex,
                        isIncome = it.isIncome, isCustom = it.isCustom, isArchived = it.isArchived
                    )
                })
                database.walletDao().insertAll(payload.wallets.map {
                    com.kkh.wallet.data.local.entity.WalletEntity(
                        id = it.id, name = it.name, type = it.type, providerName = it.providerName,
                        iconKey = it.iconKey, colorHex = it.colorHex,
                        currentBalance = it.currentBalance, creditLimit = it.creditLimit,
                        usedLimit = it.usedLimit, billingDate = it.billingDate, dueDate = it.dueDate,
                        notes = it.notes, isArchived = it.isArchived, createdAt = it.createdAt
                    )
                })
                database.transactionDao().insertAll(payload.transactions.map {
                    com.kkh.wallet.data.local.entity.TransactionEntity(
                        id = it.id, walletId = it.walletId, destinationWalletId = it.destinationWalletId,
                        categoryId = it.categoryId, amount = it.amount, type = it.type,
                        description = it.description, merchant = it.merchant, dateMillis = it.dateMillis,
                        attachmentUri = it.attachmentUri, tags = it.tags, createdAt = it.createdAt
                    )
                })
                database.budgetDao().insertAll(payload.budgets.map {
                    com.kkh.wallet.data.local.entity.BudgetEntity(
                        id = it.id, categoryId = it.categoryId, monthlyAmount = it.monthlyAmount,
                        warnThresholdPercent = it.warnThresholdPercent, createdAt = it.createdAt
                    )
                })
            }
            payload.wallets.size + payload.transactions.size + payload.categories.size + payload.budgets.size
        }
    }
}
