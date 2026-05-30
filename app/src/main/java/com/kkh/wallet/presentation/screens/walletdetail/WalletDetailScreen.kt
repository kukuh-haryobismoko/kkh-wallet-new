package com.kkh.wallet.presentation.screens.walletdetail

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kkh.wallet.presentation.components.EmptyState
import com.kkh.wallet.presentation.components.SectionHeader
import com.kkh.wallet.presentation.components.TransactionItem
import com.kkh.wallet.presentation.components.WalletCard
import com.kkh.wallet.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletDetailScreen(
    walletId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onAddTransaction: () -> Unit,
    onTransfer: () -> Unit,
    onEditTransaction: (Long) -> Unit,
    viewModel: WalletDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(walletId) { viewModel.setWalletId(walletId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val wallet = state.wallet

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(wallet?.name ?: "Wallet") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = null) }
                },
                actions = {
                    IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, contentDescription = "Edit") }
                }
            )
        }
    ) { padding ->
        if (wallet == null) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(padding), contentAlignment = Alignment.Center) {
                Text("Loading…")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Box(modifier = Modifier.padding(16.dp)) {
                    WalletCard(wallet = wallet)
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalButton(
                        onClick = onAddTransaction,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Transaction")
                    }
                    FilledTonalButton(
                        onClick = onTransfer,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Outlined.SwapHoriz, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (wallet.type.isCreditBased) "Pay" else "Transfer")
                    }
                }
            }

            item { WalletInfoCard(wallet = wallet, monthlySpend = state.monthlySpend) }

            item { SectionHeader("Transactions") }

            if (state.transactions.isEmpty()) {
                item {
                    EmptyState(
                        title = "No transactions",
                        message = "Transactions for this wallet will show up here."
                    )
                }
            } else {
                items(state.transactions, key = { it.id }) { tx ->
                    TransactionItem(
                        transaction = tx,
                        category = tx.categoryId?.let { state.categories[it] },
                        wallet = state.wallets[tx.walletId],
                        destinationWallet = tx.destinationWalletId?.let { state.wallets[it] },
                        onClick = { onEditTransaction(tx.id) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                }
            }
        }
    }
}

@Composable
private fun WalletInfoCard(
    wallet: com.kkh.wallet.domain.model.Wallet,
    monthlySpend: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Wallet info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow("Type", wallet.type.displayName)
            if (wallet.providerName.isNotBlank()) InfoRow("Provider", wallet.providerName)
            if (wallet.type.isCreditBased) {
                InfoRow("Credit limit", CurrencyFormatter.formatRp(wallet.creditLimit))
                InfoRow("Used limit", CurrencyFormatter.formatRp(wallet.usedLimit))
                InfoRow("Available limit", CurrencyFormatter.formatRp(wallet.availableLimit))
                wallet.billingDate?.let { InfoRow("Billing day", "Day $it") }
                wallet.dueDate?.let { InfoRow("Due day", "Day $it") }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Utilization", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { wallet.utilizationPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("${"%.0f".format(wallet.utilizationPercent)}% used", style = MaterialTheme.typography.labelMedium)
            } else {
                InfoRow("Current balance", CurrencyFormatter.formatRp(wallet.currentBalance))
            }
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow("This month's spend", CurrencyFormatter.formatRp(monthlySpend))
            if (wallet.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Notes", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(wallet.notes, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
