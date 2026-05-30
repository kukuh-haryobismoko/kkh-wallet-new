package com.kkh.wallet.domain.repository

import com.kkh.wallet.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeAll(): Flow<List<Transaction>>
    fun observeRecent(limit: Int): Flow<List<Transaction>>
    fun observeForWallet(walletId: Long): Flow<List<Transaction>>
    fun observeBetween(fromMillis: Long, toMillis: Long): Flow<List<Transaction>>
    suspend fun findById(id: Long): Transaction?

    /**
     * Persists a new transaction AND atomically adjusts the affected wallet balances/limits.
     * Returns the new transaction id.
     */
    suspend fun addTransaction(transaction: Transaction): Long

    /**
     * Updates an existing transaction. Reverses the prior balance/limit effect and then
     * applies the new one. Both effects happen in a single database transaction.
     */
    suspend fun updateTransaction(transaction: Transaction)

    /**
     * Deletes a transaction and reverses its balance/limit effect.
     */
    suspend fun deleteTransaction(id: Long)
}
