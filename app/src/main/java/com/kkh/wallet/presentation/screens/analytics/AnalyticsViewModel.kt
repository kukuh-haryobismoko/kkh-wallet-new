package com.kkh.wallet.presentation.screens.analytics

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
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class MonthBucket(
    val label: String,
    val startMillis: Long,
    val endMillis: Long,
    val expense: Double = 0.0,
    val income: Double = 0.0
)

data class AnalyticsState(
    val monthBuckets: List<MonthBucket> = emptyList(),
    val categoriesById: Map<Long, Category> = emptyMap(),
    val walletsById: Map<Long, Wallet> = emptyMap(),
    val expensesByCategoryThisMonth: List<Pair<Category, Double>> = emptyList(),
    val totalExpenseThisMonth: Double = 0.0,
    val totalIncomeThisMonth: Double = 0.0,
    val creditUtilization: List<Pair<Wallet, Double>> = emptyList()
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    walletRepository: WalletRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {

    val state: StateFlow<AnalyticsState> = combine(
        transactionRepository.observeAll(),
        walletRepository.observeAll(),
        categoryRepository.observeActive()
    ) { txs, wallets, cats ->
        val catMap = cats.associateBy { it.id }
        val walletMap = wallets.associateBy { it.id }
        val now = System.currentTimeMillis()
        val buckets = buildLast6MonthBuckets(now).map { bucket ->
            val inBucket = txs.filter { it.dateMillis in bucket.startMillis..bucket.endMillis }
            bucket.copy(
                expense = inBucket.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount },
                income = inBucket.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            )
        }
        val monthStart = DateUtils.startOfMonth(now)
        val monthEnd = DateUtils.endOfMonth(now)
        val thisMonthTxs = txs.filter { it.dateMillis in monthStart..monthEnd }
        val byCat = thisMonthTxs
            .filter { it.type == TransactionType.EXPENSE && it.categoryId != null }
            .groupBy { it.categoryId!! }
            .mapNotNull { (catId, list) ->
                val c = catMap[catId] ?: return@mapNotNull null
                c to list.sumOf { it.amount }
            }
            .sortedByDescending { it.second }
        val totalExp = thisMonthTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val totalInc = thisMonthTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val utilization = wallets
            .filter { it.type.isCreditBased && it.creditLimit > 0 }
            .map { it to it.utilizationPercent }
            .sortedByDescending { it.second }

        AnalyticsState(
            monthBuckets = buckets,
            categoriesById = catMap,
            walletsById = walletMap,
            expensesByCategoryThisMonth = byCat,
            totalExpenseThisMonth = totalExp,
            totalIncomeThisMonth = totalInc,
            creditUtilization = utilization
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AnalyticsState())

    private fun buildLast6MonthBuckets(nowMillis: Long): List<MonthBucket> {
        val labels = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val result = mutableListOf<MonthBucket>()
        for (i in 5 downTo 0) {
            val base = DateUtils.addMonths(nowMillis, -i)
            val start = DateUtils.startOfMonth(base)
            val end = DateUtils.endOfMonth(base)
            val cal = Calendar.getInstance().apply { timeInMillis = start }
            result += MonthBucket(label = labels[cal.get(Calendar.MONTH)], startMillis = start, endMillis = end)
        }
        return result
    }
}
