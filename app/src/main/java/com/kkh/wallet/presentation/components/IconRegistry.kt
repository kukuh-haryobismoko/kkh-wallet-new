package com.kkh.wallet.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector
import com.kkh.wallet.domain.model.WalletType

/**
 * Maps category iconKey strings (seeded from CategoryRepositoryImpl) to vector icons.
 */
object IconRegistry {
    fun category(iconKey: String): ImageVector = when (iconKey) {
        "restaurant"     -> Icons.Outlined.Restaurant
        "directions_car" -> Icons.Outlined.DirectionsCar
        "shopping_bag"   -> Icons.Outlined.ShoppingBag
        "receipt_long"   -> Icons.Outlined.ReceiptLong
        "movie"          -> Icons.Outlined.Movie
        "favorite"       -> Icons.Outlined.Favorite
        "school"         -> Icons.Outlined.School
        "swap_horiz"     -> Icons.Outlined.SwapHoriz
        "credit_card"    -> Icons.Filled.CreditCard
        "payments"       -> Icons.Filled.Payments
        "trending_up"    -> Icons.Outlined.TrendingUp
        else             -> Icons.Outlined.Category
    }

    fun wallet(type: WalletType): ImageVector = when (type) {
        WalletType.CASH -> Icons.Filled.Payments
        WalletType.BANK -> Icons.Filled.AccountBalance
        WalletType.E_WALLET -> Icons.Filled.Smartphone
        WalletType.CREDIT_CARD -> Icons.Filled.CreditCard
        WalletType.PAYLATER -> Icons.Filled.AccountBalanceWallet
    }
}
