package com.kkh.wallet.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIntoContainer
import androidx.compose.animation.slideOutOfContainer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.kkh.wallet.presentation.screens.addeditwallet.AddEditWalletScreen
import com.kkh.wallet.presentation.screens.addtransaction.AddTransactionScreen
import com.kkh.wallet.presentation.screens.analytics.AnalyticsScreen
import com.kkh.wallet.presentation.screens.dashboard.DashboardScreen
import com.kkh.wallet.presentation.screens.settings.SettingsScreen
import com.kkh.wallet.presentation.screens.transactions.TransactionsScreen
import com.kkh.wallet.presentation.screens.transfer.TransferScreen
import com.kkh.wallet.presentation.screens.walletdetail.WalletDetailScreen
import com.kkh.wallet.presentation.screens.wallets.WalletsScreen

private data class BottomTab(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

private val BOTTOM_TABS = listOf(
    BottomTab(Screen.Dashboard,    "Home",      Icons.Outlined.Home),
    BottomTab(Screen.Wallets,      "Wallets",   Icons.Outlined.AccountBalanceWallet),
    BottomTab(Screen.Transactions, "History",   Icons.Outlined.SwapHoriz),
    BottomTab(Screen.Analytics,    "Analytics", Icons.Outlined.Analytics),
    BottomTab(Screen.Settings,     "Settings",  Icons.Outlined.Settings)
)

@Composable
fun KKHNavHost(navController: NavHostController) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = BOTTOM_TABS.any { tab -> tab.screen.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    BOTTOM_TABS.forEach { tab ->
                        val selected = currentRoute == tab.screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(tab.screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(padding),
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(220)) + fadeIn(tween(220)) },
            exitTransition  = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(220)) + fadeOut(tween(220)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End,   tween(220)) + fadeIn(tween(220)) },
            popExitTransition  = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End,  tween(220)) + fadeOut(tween(220)) }
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onWalletClick = { walletId -> navController.navigate(Screen.WalletDetail.build(walletId)) },
                    onAddTransaction = { navController.navigate(Screen.AddTransaction.build()) },
                    onAddWallet = { navController.navigate(Screen.AddWallet.route) },
                    onTransfer = { navController.navigate(Screen.Transfer.build()) },
                    onSeeAllTransactions = { navController.navigate(Screen.Transactions.route) }
                )
            }

            composable(Screen.Wallets.route) {
                WalletsScreen(
                    onAddWallet = { navController.navigate(Screen.AddWallet.route) },
                    onWalletClick = { walletId -> navController.navigate(Screen.WalletDetail.build(walletId)) }
                )
            }

            composable(Screen.Transactions.route) {
                TransactionsScreen(
                    onAddTransaction = { navController.navigate(Screen.AddTransaction.build()) },
                    onEditTransaction = { id -> navController.navigate(Screen.EditTransaction.build(id)) }
                )
            }

            composable(Screen.Analytics.route) {
                AnalyticsScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }

            composable(Screen.AddWallet.route) {
                AddEditWalletScreen(
                    walletId = null,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.EditWallet.route,
                arguments = listOf(navArgument(Screen.EditWallet.ARG_ID) { type = NavType.LongType })
            ) { entry ->
                val walletId = entry.arguments?.getLong(Screen.EditWallet.ARG_ID) ?: -1L
                AddEditWalletScreen(
                    walletId = walletId.takeIf { it > 0 },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.WalletDetail.route,
                arguments = listOf(navArgument(Screen.WalletDetail.ARG_ID) { type = NavType.LongType })
            ) { entry ->
                val walletId = entry.arguments?.getLong(Screen.WalletDetail.ARG_ID) ?: -1L
                WalletDetailScreen(
                    walletId = walletId,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Screen.EditWallet.build(walletId)) },
                    onAddTransaction = { navController.navigate(Screen.AddTransaction.build(walletId)) },
                    onTransfer = { navController.navigate(Screen.Transfer.build(walletId)) },
                    onEditTransaction = { id -> navController.navigate(Screen.EditTransaction.build(id)) }
                )
            }

            composable(
                route = Screen.AddTransaction.route,
                arguments = listOf(
                    navArgument(Screen.AddTransaction.ARG_WALLET_ID) {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { entry ->
                val walletId = entry.arguments?.getLong(Screen.AddTransaction.ARG_WALLET_ID) ?: -1L
                AddTransactionScreen(
                    initialWalletId = walletId.takeIf { it > 0 },
                    transactionId = null,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.EditTransaction.route,
                arguments = listOf(navArgument(Screen.EditTransaction.ARG_ID) { type = NavType.LongType })
            ) { entry ->
                val txId = entry.arguments?.getLong(Screen.EditTransaction.ARG_ID) ?: -1L
                AddTransactionScreen(
                    initialWalletId = null,
                    transactionId = txId.takeIf { it > 0 },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Transfer.route,
                arguments = listOf(
                    navArgument(Screen.Transfer.ARG_SOURCE) {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { entry ->
                val sourceId = entry.arguments?.getLong(Screen.Transfer.ARG_SOURCE) ?: -1L
                TransferScreen(
                    initialSourceWalletId = sourceId.takeIf { it > 0 },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
