package com.kkh.wallet.domain.model

/**
 * A user-owned wallet/account.
 *
 * For balance-based wallets ([WalletType.CASH], [WalletType.BANK], [WalletType.E_WALLET]):
 *   - [currentBalance] holds the spendable money.
 *   - [creditLimit] and [usedLimit] are zero.
 *
 * For credit/paylater wallets:
 *   - [creditLimit] is the total approved credit line.
 *   - [usedLimit] is the amount currently drawn (outstanding debt).
 *   - [availableLimit] is the spendable headroom (`creditLimit - usedLimit`).
 *   - [currentBalance] is conceptually zero/unused.
 */
data class Wallet(
    val id: Long = 0L,
    val name: String,
    val type: WalletType,
    val providerName: String = "",
    val iconKey: String = "default",
    val colorHex: String = "#6750A4",
    val currentBalance: Double = 0.0,
    val creditLimit: Double = 0.0,
    val usedLimit: Double = 0.0,
    val billingDate: Int? = null, // day-of-month for credit/paylater statement
    val dueDate: Int? = null,     // day-of-month payment is due
    val notes: String = "",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val availableLimit: Double
        get() = (creditLimit - usedLimit).coerceAtLeast(0.0)

    val utilizationPercent: Float
        get() = if (creditLimit > 0.0) {
            ((usedLimit / creditLimit) * 100.0).toFloat().coerceIn(0f, 100f)
        } else 0f

    /** Amount the user can actually spend from this wallet right now. */
    val spendablePower: Double
        get() = if (type.isCreditBased) availableLimit else currentBalance
}
