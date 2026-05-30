package com.kkh.wallet.data.repository

import com.kkh.wallet.data.local.dao.CategoryDao
import com.kkh.wallet.data.local.entity.CategoryEntity
import com.kkh.wallet.data.mapper.toDomain
import com.kkh.wallet.data.mapper.toEntity
import com.kkh.wallet.domain.model.Category
import com.kkh.wallet.domain.repository.CategoryRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val dao: CategoryDao
) : CategoryRepository {

    override fun observeActive(): Flow<List<Category>> =
        dao.observeActive().map { list -> list.map { it.toDomain() } }

    override suspend fun findById(id: Long): Category? = dao.findById(id)?.toDomain()

    override suspend fun upsert(category: Category): Long = dao.upsert(category.toEntity())

    override suspend fun delete(category: Category) = dao.delete(category.toEntity())

    override suspend fun seedDefaultsIfEmpty() {
        if (dao.count() > 0) return
        dao.insertAll(DEFAULTS)
    }

    private companion object {
        // (name, iconKey, colorHex, isIncome)
        val DEFAULTS: List<CategoryEntity> = listOf(
            CategoryEntity(0, "Food",                 "restaurant",   "#EF4444", false, false, false),
            CategoryEntity(0, "Transportation",       "directions_car","#3B82F6", false, false, false),
            CategoryEntity(0, "Shopping",             "shopping_bag", "#EC4899", false, false, false),
            CategoryEntity(0, "Bills",                "receipt_long", "#F59E0B", false, false, false),
            CategoryEntity(0, "Entertainment",        "movie",        "#8B5CF6", false, false, false),
            CategoryEntity(0, "Health",               "favorite",     "#10B981", false, false, false),
            CategoryEntity(0, "Education",            "school",       "#0EA5E9", false, false, false),
            CategoryEntity(0, "Transfer",             "swap_horiz",   "#6B7280", false, false, false),
            CategoryEntity(0, "Credit Card Payment",  "credit_card",  "#475569", false, false, false),
            CategoryEntity(0, "Other",                "category",     "#64748B", false, false, false),

            CategoryEntity(0, "Salary",               "payments",     "#22C55E", true,  false, false),
            CategoryEntity(0, "Investment",           "trending_up",  "#0D9488", true,  false, false)
        )
    }
}
