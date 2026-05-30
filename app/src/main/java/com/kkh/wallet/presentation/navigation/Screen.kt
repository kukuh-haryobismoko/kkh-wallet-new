package com.kkh.wallet.presentation.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Wallets : Screen("wallets")
    data object Transactions : Screen("transactions")
    data object Analytics : Screen("analytics")
    data object Settings : Screen("settings")

    data object AddTransaction : Screen("add_transaction?walletId={walletId}") {
        fun build(walletId: Long? = null): String =
            "add_transaction?walletId=${walletId ?: -1L}"
        const val ARG_WALLET_ID = "walletId"
    }

    data object EditTransaction : Screen("edit_transaction/{transactionId}") {
        fun build(id: Long): String = "edit_transaction/$id"
        const val ARG_ID = "transactionId"
    }

    data object Transfer : Screen("transfer?sourceWalletId={sourceWalletId}") {
        fun build(sourceWalletId: Long? = null): String =
            "transfer?sourceWalletId=${sourceWalletId ?: -1L}"
        const val ARG_SOURCE = "sourceWalletId"
    }

    data object AddWallet : Screen("add_wallet")
    data object EditWallet : Screen("edit_wallet/{walletId}") {
        fun build(id: Long): String = "edit_wallet/$id"
        const val ARG_ID = "walletId"
    }

    data object WalletDetail : Screen("wallet_detail/{walletId}") {
        fun build(id: Long): String = "wallet_detail/$id"
        const val ARG_ID = "walletId"
    }

    data object Onboarding : Screen("onboarding")
}
