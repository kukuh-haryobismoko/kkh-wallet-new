package com.kkh.wallet.domain.repository

import com.kkh.wallet.domain.model.Wallet
import kotlinx.coroutines.flow.Flow

interface WalletRepository {
    fun observeActive(): Flow<List<Wallet>>
    fun observeAll(): Flow<List<Wallet>>
    fun observeById(id: Long): Flow<Wallet?>
    suspend fun findById(id: Long): Wallet?
    suspend fun count(): Int
    suspend fun upsert(wallet: Wallet): Long
    suspend fun delete(id: Long)
}
