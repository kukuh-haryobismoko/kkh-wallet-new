package com.kkh.wallet.domain.model

/**
 * The type of wallet/account a user can hold.
 *
 * Wallets are classified by how they hold and account for money:
 *  - [CASH], [BANK], [E_WALLET]: balance-based, spending reduces the balance.
 *  - [CREDIT_CARD], [PAYLATER]: limit-based, spending consumes available credit
 *    until a payment is made.
 */
enum class WalletType(val displayName: String, val isCreditBased: Boolean) {
    CASH("Cash", false),
    BANK("Bank Account", false),
    E_WALLET("E-Wallet", false),
    CREDIT_CARD("Credit Card", true),
    PAYLATER("Paylater", true);

    companion object {
        fun fromName(name: String): WalletType =
            values().firstOrNull { it.name == name } ?: CASH
    }
}
