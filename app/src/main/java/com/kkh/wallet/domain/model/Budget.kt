package com.kkh.wallet.domain.model

/**
 * A monthly spending cap for a specific [categoryId].
 *
 * If [categoryId] is null, the budget is treated as an overall monthly limit.
 */
data class Budget(
    val id: Long = 0L,
    val categoryId: Long? = null,
    val monthlyAmount: Double,
    val warnThresholdPercent: Int = 80,
    val createdAt: Long = System.currentTimeMillis()
)
