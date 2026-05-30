package com.kkh.wallet.presentation.screens.dashboard

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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class DashboardState(
    val wallets: List<Wallet> = emptyList(),
    val categories: List<Category> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val monthlyTransactions: List<Transaction> = emptyList()
) {
    val totalBalance: Double
        get() = wallets.filter { !it.type.isCreditBased }.sumOf { it.currentBalance }
    val totalDebt: Double
        get() = wallets.filter { it.type.isCreditBased }.sumOf { it.usedLimit }
    val totalAvailableCredit: Double
        get() = wallets.filter { it.type.isCreditBased }.sumOf { it.availableLimit }

    val monthlyExpense: Double
        get() = monthlyTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
    val monthlyIncome: Double
        get() = monthlyTransactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    walletRepository: WalletRepository,
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {

    val state: StateFlow<DashboardState> = combine(
        walletRepository.observeActive(),
        transactionRepository.observeRecent(10),
        categoryRepository.observeActive(),
        flowOf(Unit).flatMapLatest {
            val now = System.currentTimeMillis()
            transactionRepository.observeBetween(DateUtils.startOfMonth(now), DateUtils.endOfMonth(now))
        }
    ) { wallets, recent, categories, monthly ->
        DashboardState(
            wallets = wallets,
            categories = categories,
            recentTransactions = recent,
            monthlyTransactions = monthly
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardState())
}
