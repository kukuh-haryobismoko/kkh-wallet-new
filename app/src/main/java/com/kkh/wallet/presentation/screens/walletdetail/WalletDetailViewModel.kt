package com.kkh.wallet.presentation.screens.walletdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkh.wallet.domain.model.Category
import com.kkh.wallet.domain.model.Transaction
import com.kkh.wallet.domain.model.TransactionType
import com.kkh.wallet.domain.model.Wallet
import com.kkh.wallet.domain.repository.CategoryRepository
import com.kkh.wallet.domain.repository.TransactionRepository
import com.kkh.wallet.domain.repository.WalletRepository
import com.kkh.wallet.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WalletDetailState(
    val wallet: Wallet? = null,
    val transactions: List<Transaction> = emptyList(),
    val categories: Map<Long, Category> = emptyMap(),
    val wallets: Map<Long, Wallet> = emptyMap()
) {
    val monthlySpend: Double
        get() = transactions
            .filter { it.type == TransactionType.EXPENSE && it.dateMillis >= DateUtils.startOfMonth() }
            .sumOf { it.amount }
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WalletDetailViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {

    private val walletId = MutableStateFlow<Long?>(null)

    val state: StateFlow<WalletDetailState> = combine(
        walletId.flatMapLatest { id -> if (id == null) kotlinx.coroutines.flow.flowOf(null) else walletRepository.observeById(id) },
        walletId.flatMapLatest { id -> if (id == null) kotlinx.coroutines.flow.flowOf(emptyList()) else transactionRepository.observeForWallet(id) },
        categoryRepository.observeActive(),
        walletRepository.observeAll()
    ) { wallet, txs, cats, allWallets ->
        WalletDetailState(
            wallet = wallet,
            transactions = txs,
            categories = cats.associateBy { it.id },
            wallets = allWallets.associateBy { it.id }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WalletDetailState())

    fun setWalletId(id: Long) { walletId.value = id }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch { transactionRepository.deleteTransaction(id) }
    }
}
