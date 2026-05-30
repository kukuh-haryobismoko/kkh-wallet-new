package com.kkh.wallet.domain.model

/**
 * A user-facing category for expenses or income, e.g. "Food", "Salary".
 *
 * [isIncome] separates income-style categories (Salary, Investment) from
 * expense-style ones. Categories are pre-seeded on first launch but the user
 * may add, rename, or recolor their own.
 */
data class Category(
    val id: Long = 0L,
    val name: String,
    val iconKey: String,
    val colorHex: String,
    val isIncome: Boolean = false,
    val isCustom: Boolean = false,
    val isArchived: Boolean = false
)
