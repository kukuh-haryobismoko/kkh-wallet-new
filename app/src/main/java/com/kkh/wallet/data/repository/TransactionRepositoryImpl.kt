package com.kkh.wallet.data.repository

import androidx.room.withTransaction
import com.kkh.wallet.data.local.KKHDatabase
import com.kkh.wallet.data.local.dao.TransactionDao
import com.kkh.wallet.data.local.dao.WalletDao
import com.kkh.wallet.data.mapper.toDomain
import com.kkh.wallet.data.mapper.toEntity
import com.kkh.wallet.domain.model.Transaction
import com.kkh.wallet.domain.model.TransactionType
import com.kkh.wallet.domain.model.WalletType
import com.kkh.wallet.domain.repository.TransactionRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Single source of truth for transactions and any wallet balance/limit changes
 * caused by transactions. Every mutation runs inside a Room transaction so that
 * a write to the `transactions` table and the corresponding adjustments to
 * `wallets` either both succeed or both fail.
 *
 * Effect rules (per [TransactionType]):
 *  - EXPENSE on a balance wallet: balance -= amount
 *  - EXPENSE on a credit wallet:  usedLimit += amount
 *  - INCOME on a balance wallet:  balance += amount
 *  - INCOME on a credit wallet:   usedLimit -= amount (treated as a refund)
 *  - TRANSFER: source -= amount, destination += amount
 *      (both wallets must be balance-based; transfers to a credit wallet are
 *       represented as CREDIT_PAYMENT instead.)
 *  - CREDIT_PAYMENT: source (balance) -= amount, destination (credit) usedLimit -= amount
 */
@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val database: KKHDatabase,
    private val transactionDao: TransactionDao,
    private val walletDao: WalletDao
) : TransactionRepository {

    override fun observeAll(): Flow<List<Transaction>> =
        transactionDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeRecent(limit: Int): Flow<List<Transaction>> =
        transactionDao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    override fun observeForWallet(walletId: Long): Flow<List<Transaction>> =
        transactionDao.observeForWallet(walletId).map { list -> list.map { it.toDomain() } }

    override fun observeBetween(fromMillis: Long, toMillis: Long): Flow<List<Transaction>> =
        transactionDao.observeBetween(fromMillis, toMillis).map { list -> list.map { it.toDomain() } }

    override suspend fun findById(id: Long): Transaction? =
        transactionDao.findById(id)?.toDomain()

    override suspend fun addTransaction(transaction: Transaction): Long =
        database.withTransaction {
            require(transaction.amount > 0.0) { "Amount must be positive" }
            val sourceWallet = walletDao.findById(transaction.walletId)
                ?: error("Source wallet not found: ${transaction.walletId}")

            when (transaction.type) {
                TransactionType.EXPENSE -> applyExpense(sourceWallet.type, transaction.walletId, transaction.amount)
                TransactionType.INCOME -> applyIncome(sourceWallet.type, transaction.walletId, transaction.amount)
                TransactionType.TRANSFER -> {
                    val destId = requireNotNull(transaction.destinationWalletId) {
                        "TRANSFER requires destinationWalletId"
                    }
                    val destWallet = walletDao.findById(destId)
                        ?: error("Destination wallet not found: $destId")
                    require(!WalletType.fromName(sourceWallet.type).isCreditBased) {
                        "Cannot transfer FROM a credit-based wallet. Use a different transaction type."
                    }
                    require(!WalletType.fromName(destWallet.type).isCreditBased) {
                        "Cannot transfer TO a credit-based wallet. Use CREDIT_PAYMENT instead."
                    }
                    walletDao.adjustBalance(transaction.walletId, -transaction.amount)
                    walletDao.adjustBalance(destId, +transaction.amount)
                }
                TransactionType.CREDIT_PAYMENT -> {
                    val destId = requireNotNull(transaction.destinationWalletId) {
                        "CREDIT_PAYMENT requires destinationWalletId"
                    }
                    val destWallet = walletDao.findById(destId)
                        ?: error("Destination wallet not found: $destId")
                    require(!WalletType.fromName(sourceWallet.type).isCreditBased) {
                        "Credit payments must come from a balance-based wallet."
                    }
                    require(WalletType.fromName(destWallet.type).isCreditBased) {
                        "Credit payments must target a credit/paylater wallet."
                    }
                    walletDao.adjustBalance(transaction.walletId, -transaction.amount)
                    walletDao.adjustUsedLimit(destId, -transaction.amount)
                }
            }

            transactionDao.upsert(transaction.toEntity())
        }

    override suspend fun updateTransaction(transaction: Transaction) {
        database.withTransaction {
            val existing = transactionDao.findById(transaction.id)
                ?: error("Transaction not found: ${transaction.id}")
            // Reverse old effect.
            applyReverse(existing.toDomain())
            // Apply new effect via the same code path as addTransaction (minus the insert).
            applyForward(transaction)
            transactionDao.update(transaction.toEntity())
        }
    }

    override suspend fun deleteTransaction(id: Long) {
        database.withTransaction {
            val existing = transactionDao.findById(id) ?: return@withTransaction
            applyReverse(existing.toDomain())
            transactionDao.deleteById(id)
        }
    }

    // ---- internal helpers -----------------------------------------------------

    private suspend fun applyForward(t: Transaction) {
        val source = walletDao.findById(t.walletId) ?: error("Source wallet missing")
        when (t.type) {
            TransactionType.EXPENSE -> applyExpense(source.type, t.walletId, t.amount)
            TransactionType.INCOME -> applyIncome(source.type, t.walletId, t.amount)
            TransactionType.TRANSFER -> {
                val destId = requireNotNull(t.destinationWalletId)
                walletDao.adjustBalance(t.walletId, -t.amount)
                walletDao.adjustBalance(destId, +t.amount)
            }
            TransactionType.CREDIT_PAYMENT -> {
                val destId = requireNotNull(t.destinationWalletId)
                walletDao.adjustBalance(t.walletId, -t.amount)
                walletDao.adjustUsedLimit(destId, -t.amount)
            }
        }
    }

    private suspend fun applyReverse(t: Transaction) {
        val source = walletDao.findById(t.walletId) ?: return
        when (t.type) {
            TransactionType.EXPENSE -> reverseExpense(source.type, t.walletId, t.amount)
            TransactionType.INCOME -> reverseIncome(source.type, t.walletId, t.amount)
            TransactionType.TRANSFER -> {
                val destId = t.destinationWalletId ?: return
                walletDao.adjustBalance(t.walletId, +t.amount)
                walletDao.adjustBalance(destId, -t.amount)
            }
            TransactionType.CREDIT_PAYMENT -> {
                val destId = t.destinationWalletId ?: return
                walletDao.adjustBalance(t.walletId, +t.amount)
                walletDao.adjustUsedLimit(destId, +t.amount)
            }
        }
    }

    private suspend fun applyExpense(walletTypeName: String, walletId: Long, amount: Double) {
        if (WalletType.fromName(walletTypeName).isCreditBased) {
            walletDao.adjustUsedLimit(walletId, +amount)
        } else {
            walletDao.adjustBalance(walletId, -amount)
        }
    }

    private suspend fun applyIncome(walletTypeName: String, walletId: Long, amount: Double) {
        if (WalletType.fromName(walletTypeName).isCreditBased) {
            walletDao.adjustUsedLimit(walletId, -amount)
        } else {
            walletDao.adjustBalance(walletId, +amount)
        }
    }

    private suspend fun reverseExpense(walletTypeName: String, walletId: Long, amount: Double) {
        if (WalletType.fromName(walletTypeName).isCreditBased) {
            walletDao.adjustUsedLimit(walletId, -amount)
        } else {
            walletDao.adjustBalance(walletId, +amount)
        }
    }

    private suspend fun reverseIncome(walletTypeName: String, walletId: Long, amount: Double) {
        if (WalletType.fromName(walletTypeName).isCreditBased) {
            walletDao.adjustUsedLimit(walletId, +amount)
        } else {
            walletDao.adjustBalance(walletId, -amount)
        }
    }
}
