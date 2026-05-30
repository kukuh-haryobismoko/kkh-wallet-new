package com.kkh.wallet.domain.model

/**
 * A single recorded movement of money.
 *
 * Most transactions touch a single wallet ([walletId]); transfers and credit
 * payments also populate [destinationWalletId].
 */
data class Transaction(
    val id: Long = 0L,
    val walletId: Long,
    val destinationWalletId: Long? = null,
    val categoryId: Long? = null,
    val amount: Double,
    val type: TransactionType,
    val description: String = "",
    val merchant: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val attachmentUri: String? = null,
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
