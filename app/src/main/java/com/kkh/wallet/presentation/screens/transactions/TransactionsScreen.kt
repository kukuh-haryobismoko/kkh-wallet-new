package com.kkh.wallet.presentation.screens.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kkh.wallet.domain.model.TransactionType
import com.kkh.wallet.presentation.components.EmptyState
import com.kkh.wallet.presentation.components.TransactionItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onAddTransaction: () -> Unit,
    onEditTransaction: (Long) -> Unit,
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Transactions") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTransaction,
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text("New") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                OutlinedTextField(
                    value = state.filter.query,
                    onValueChange = viewModel::setQuery,
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    placeholder = { Text("Search description, merchant, tag…") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = state.filter.type == null,
                            onClick = { viewModel.setTypeFilter(null) },
                            label = { Text("All") }
                        )
                    }
                    items(TransactionType.values()) { t ->
                        FilterChip(
                            selected = state.filter.type == t,
                            onClick = { viewModel.setTypeFilter(if (state.filter.type == t) null else t) },
                            label = { Text(t.displayName) }
                        )
                    }
                }
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = state.filter.walletId == null,
                            onClick = { viewModel.setWalletFilter(null) },
                            label = { Text("All wallets") }
                        )
                    }
                    items(state.wallets.values.toList()) { w ->
                        FilterChip(
                            selected = state.filter.walletId == w.id,
                            onClick = { viewModel.setWalletFilter(if (state.filter.walletId == w.id) null else w.id) },
                            label = { Text(w.name) }
                        )
                    }
                }
            }

            if (state.transactions.isEmpty()) {
                item {
                    EmptyState(
                        title = "No transactions",
                        message = "Adjust your filters or add a new transaction with the button below.",
                        icon = Icons.Outlined.Receipt
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
