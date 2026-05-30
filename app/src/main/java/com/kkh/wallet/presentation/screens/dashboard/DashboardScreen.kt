package com.kkh.wallet.presentation.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kkh.wallet.presentation.components.DonutChart
import com.kkh.wallet.presentation.components.EmptyState
import com.kkh.wallet.presentation.components.PieLegend
import com.kkh.wallet.presentation.components.PieSlice
import com.kkh.wallet.presentation.components.SectionHeader
import com.kkh.wallet.presentation.components.TransactionItem
import com.kkh.wallet.presentation.components.WalletCard
import com.kkh.wallet.presentation.theme.DangerRed
import com.kkh.wallet.presentation.theme.SuccessGreen
import com.kkh.wallet.util.CurrencyFormatter

@Composable
fun DashboardScreen(
    onWalletClick: (Long) -> Unit,
    onAddTransaction: () -> Unit,
    onAddWallet: () -> Unit,
    onTransfer: () -> Unit,
    onSeeAllTransactions: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTransaction,
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text("Transaction") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item { HeaderSection(state) }

            item {
                QuickActionsRow(
                    onAddTransaction = onAddTransaction,
                    onAddWallet = onAddWallet,
                    onTransfer = onTransfer
                )
            }

            item { SectionHeader("Wallets") }
            if (state.wallets.isEmpty()) {
                item {
                    EmptyState(
                        title = "No wallets yet",
                        message = "Add your first wallet to start tracking your balance, expenses, and credit cards."
                    )
                }
            } else {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.wallets, key = { it.id }) { w ->
                            Box(modifier = Modifier.width(300.dp)) {
                                WalletCard(wallet = w, onClick = { onWalletClick(w.id) })
                            }
                        }
                    }
                }
            }

            item {
                SectionHeader(
                    title = "This month",
                    padding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
                )
                MonthlySummaryCard(state)
            }

            if (state.monthlyTransactions.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Expense by category",
                        padding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
                    )
                    CategoryBreakdownCard(state)
                }
            }

            item {
                SectionHeader(
                    title = "Recent transactions",
                    padding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
                    action = {
                        TextButton(onClick = onSeeAllTransactions) { Text("See all") }
                    }
                )
            }

            if (state.recentTransactions.isEmpty()) {
                item {
                    EmptyState(
                        title = "No transactions yet",
                        message = "Tap the button below to record your first one.",
                        icon = Icons.Outlined.Receipt
                    )
                }
            } else {
                val walletMap = state.wallets.associateBy { it.id }
                val catMap = state.categories.associateBy { it.id }
                items(state.recentTransactions, key = { it.id }) { t ->
                    TransactionItem(
                        transaction = t,
                        category = t.categoryId?.let { catMap[it] },
                        wallet = walletMap[t.walletId],
                        destinationWallet = t.destinationWalletId?.let { walletMap[it] },
                        onClick = onSeeAllTransactions
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(state: DashboardState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(text = "Welcome back", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = "KKH Wallet", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Total Balance", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(
                    text = CurrencyFormatter.formatRp(state.totalBalance),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatPill(label = "Used Limit", value = CurrencyFormatter.formatRp(state.totalDebt), tint = DangerRed)
                    StatPill(label = "Available Credit", value = CurrencyFormatter.formatRp(state.totalAvailableCredit), tint = SuccessGreen)
                }
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: String, tint: androidx.compose.ui.graphics.Color) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
        Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = tint)
    }
}

@Composable
private fun QuickActionsRow(onAddTransaction: () -> Unit, onAddWallet: () -> Unit, onTransfer: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickAction(icon = Icons.Outlined.Add, label = "Transaction", onClick = onAddTransaction, modifier = Modifier.weight(1f))
        QuickAction(icon = Icons.Outlined.SwapHoriz, label = "Transfer", onClick = onTransfer, modifier = Modifier.weight(1f))
        QuickAction(icon = Icons.Outlined.Receipt, label = "New Wallet", onClick = onAddWallet, modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickAction(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun MonthlySummaryCard(state: DashboardState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            SummaryColumn(
                title = "Income",
                value = state.monthlyIncome,
                icon = Icons.Outlined.ArrowDownward,
                tint = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            SummaryColumn(
                title = "Expense",
                value = state.monthlyExpense,
                icon = Icons.Outlined.ArrowUpward,
                tint = DangerRed,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryColumn(title: String, value: Double, icon: ImageVector, tint: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) { Icon(imageVector = icon, contentDescription = null, tint = tint) }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = CurrencyFormatter.formatRp(value), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CategoryBreakdownCard(state: DashboardState) {
    val catMap = state.categories.associateBy { it.id }
    val slices = state.monthlyTransactions
        .filter { it.type == com.kkh.wallet.domain.model.TransactionType.EXPENSE }
        .groupBy { it.categoryId }
        .map { (catId, list) ->
            val cat = catId?.let { catMap[it] }
            val color = runCatching {
                cat?.colorHex?.let { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(it)) }
            }.getOrNull() ?: MaterialTheme.colorScheme.primary
            PieSlice(
                label = cat?.name ?: "Uncategorized",
                value = list.sumOf { it.amount },
                color = color
            )
        }
        .sortedByDescending { it.value }
        .take(6)

    if (slices.isEmpty()) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            DonutChart(
                slices = slices,
                centerLabel = "Spent",
                centerValue = CurrencyFormatter.formatRp(slices.sumOf { it.value })
            )
            Spacer(modifier = Modifier.width(8.dp))
            PieLegend(slices = slices, modifier = Modifier.weight(1f))
        }
    }
}
