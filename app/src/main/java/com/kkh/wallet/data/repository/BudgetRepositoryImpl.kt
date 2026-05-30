package com.kkh.wallet.data.repository

import com.kkh.wallet.data.local.dao.BudgetDao
import com.kkh.wallet.data.mapper.toDomain
import com.kkh.wallet.data.mapper.toEntity
import com.kkh.wallet.domain.model.Budget
import com.kkh.wallet.domain.repository.BudgetRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val dao: BudgetDao
) : BudgetRepository {

    override fun observeAll(): Flow<List<Budget>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun findForCategory(categoryId: Long?): Budget? =
        dao.findForCategory(categoryId)?.toDomain()

    override suspend fun upsert(budget: Budget): Long = dao.upsert(budget.toEntity())

    override suspend fun delete(budget: Budget) = dao.delete(budget.toEntity())
}
