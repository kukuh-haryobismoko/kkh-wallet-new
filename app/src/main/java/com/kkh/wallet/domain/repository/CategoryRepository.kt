package com.kkh.wallet.domain.repository

import com.kkh.wallet.domain.model.Budget
import com.kkh.wallet.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeActive(): Flow<List<Category>>
    suspend fun findById(id: Long): Category?
    suspend fun upsert(category: Category): Long
    suspend fun delete(category: Category)
    suspend fun seedDefaultsIfEmpty()
}

interface BudgetRepository {
    fun observeAll(): Flow<List<Budget>>
    suspend fun findForCategory(categoryId: Long?): Budget?
    suspend fun upsert(budget: Budget): Long
    suspend fun delete(budget: Budget)
}
