package com.kkh.wallet.presentation.screens.wallets

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kkh.wallet.presentation.components.EmptyState
import com.kkh.wallet.presentation.components.WalletCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletsScreen(
    onAddWallet: () -> Unit,
    onWalletClick: (Long) -> Unit,
    viewModel: WalletsViewModel = hiltViewModel()
) {
    val wallets by viewModel.wallets.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Wallets") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddWallet,
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text("Add Wallet") }
            )
        }
    ) { padding ->
        if (wallets.isEmpty()) {
            EmptyState(
                title = "No wallets yet",
                message = "Add a wallet for each account you want to track: cash, banks, e-wallets, credit cards, and paylater.",
                icon = Icons.Outlined.AccountBalanceWallet,
                modifier = Modifier.padding(padding)
            )
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp)
        ) {
            items(wallets, key = { it.id }) { wallet ->
                WalletCard(
                    wallet = wallet,
                    onClick = { onWalletClick(wallet.id) },
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
    }
}
