package com.kkh.wallet.data.repository

import com.kkh.wallet.data.local.dao.WalletDao
import com.kkh.wallet.data.mapper.toDomain
import com.kkh.wallet.data.mapper.toEntity
import com.kkh.wallet.domain.model.Wallet
import com.kkh.wallet.domain.repository.WalletRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val dao: WalletDao
) : WalletRepository {

    override fun observeActive(): Flow<List<Wallet>> =
        dao.observeActive().map { list -> list.map { it.toDomain() } }

    override fun observeAll(): Flow<List<Wallet>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeById(id: Long): Flow<Wallet?> =
        dao.observeById(id).map { it?.toDomain() }

    override suspend fun findById(id: Long): Wallet? = dao.findById(id)?.toDomain()

    override suspend fun count(): Int = dao.count()

    override suspend fun upsert(wallet: Wallet): Long = dao.upsert(wallet.toEntity())

    override suspend fun delete(id: Long) = dao.deleteById(id)
}
