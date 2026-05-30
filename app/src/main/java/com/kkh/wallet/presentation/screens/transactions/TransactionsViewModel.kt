package com.kkh.wallet.presentation.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkh.wallet.domain.model.Category
import com.kkh.wallet.domain.model.Transaction
import com.kkh.wallet.domain.model.TransactionType
import com.kkh.wallet.domain.model.Wallet
import com.kkh.wallet.domain.repository.CategoryRepository
import com.kkh.wallet.domain.repository.TransactionRepository
import com.kkh.wallet.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class TransactionsFilter(
    val query: String = "",
    val walletId: Long? = null,
    val categoryId: Long? = null,
    val type: TransactionType? = null
)

data class TransactionsState(
    val transactions: List<Transaction> = emptyList(),
    val wallets: Map<Long, Wallet> = emptyMap(),
    val categories: Map<Long, Category> = emptyMap(),
    val filter: TransactionsFilter = TransactionsFilter()
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    walletRepository: WalletRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {

    private val filter = MutableStateFlow(TransactionsFilter())

    val state: StateFlow<TransactionsState> = combine(
        transactionRepository.observeAll(),
        walletRepository.observeAll(),
        categoryRepository.observeActive(),
        filter
    ) { txs, wallets, cats, f ->
        val catMap = cats.associateBy { it.id }
        val walletMap = wallets.associateBy { it.id }
        val filtered = txs.filter { tx ->
            (f.type == null || tx.type == f.type) &&
            (f.walletId == null || tx.walletId == f.walletId || tx.destinationWalletId == f.walletId) &&
            (f.categoryId == null || tx.categoryId == f.categoryId) &&
            (f.query.isBlank() ||
                tx.description.contains(f.query, ignoreCase = true) ||
                tx.merchant.contains(f.query, ignoreCase = true) ||
                tx.tags.any { it.contains(f.query, ignoreCase = true) } ||
                catMap[tx.categoryId]?.name?.contains(f.query, ignoreCase = true) == true ||
                walletMap[tx.walletId]?.name?.contains(f.query, ignoreCase = true) == true
            )
        }
        TransactionsState(
            transactions = filtered,
            wallets = walletMap,
            categories = catMap,
            filter = f
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransactionsState())

    fun setQuery(q: String) = filter.update { it.copy(query = q) }
    fun setWalletFilter(id: Long?) = filter.update { it.copy(walletId = id) }
    fun setCategoryFilter(id: Long?) = filter.update { it.copy(categoryId = id) }
    fun setTypeFilter(t: TransactionType?) = filter.update { it.copy(type = t) }
    fun clearFilters() { filter.value = TransactionsFilter() }
}
