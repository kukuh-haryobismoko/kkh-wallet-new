package com.kkh.wallet.domain.model

/**
 * The kind of operation a transaction represents.
 *
 * - [EXPENSE] reduces balance / consumes limit on the source wallet.
 * - [INCOME] increases balance on the destination wallet.
 * - [TRANSFER] moves money between two non-credit wallets.
 * - [CREDIT_PAYMENT] pays down used limit on a credit/paylater wallet from
 *   a balance-based wallet.
 */
enum class TransactionType(val displayName: String) {
    EXPENSE("Expense"),
    INCOME("Income"),
    TRANSFER("Transfer"),
    CREDIT_PAYMENT("Credit Payment");

    companion object {
        fun fromName(name: String): TransactionType =
            values().firstOrNull { it.name == name } ?: EXPENSE
    }
}
