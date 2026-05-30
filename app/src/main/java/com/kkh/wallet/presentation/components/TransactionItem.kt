package com.kkh.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kkh.wallet.domain.model.Category
import com.kkh.wallet.domain.model.Transaction
import com.kkh.wallet.domain.model.TransactionType
import com.kkh.wallet.domain.model.Wallet
import com.kkh.wallet.presentation.theme.DangerRed
import com.kkh.wallet.presentation.theme.SuccessGreen
import com.kkh.wallet.util.CurrencyFormatter
import com.kkh.wallet.util.DateUtils

@Composable
fun TransactionItem(
    transaction: Transaction,
    category: Category?,
    wallet: Wallet?,
    destinationWallet: Wallet?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPositiveForUser = when (transaction.type) {
        TransactionType.INCOME -> true
        TransactionType.EXPENSE -> false
        TransactionType.TRANSFER -> false      // shown on source row
        TransactionType.CREDIT_PAYMENT -> false // money leaving the balance wallet
    }
    val amountColor = if (isPositiveForUser) SuccessGreen else DangerRed
    val title = when (transaction.type) {
        TransactionType.TRANSFER -> "Transfer to ${destinationWallet?.name ?: "another wallet"}"
        TransactionType.CREDIT_PAYMENT -> "Payment to ${destinationWallet?.name ?: "credit"}"
        else -> category?.name ?: transaction.description.ifBlank { transaction.type.displayName }
    }
    val subtitle = buildString {
        if (wallet != null) append(wallet.name)
        if (transaction.merchant.isNotBlank()) {
            if (isNotEmpty()) append(" • ")
            append(transaction.merchant)
        }
        if (transaction.description.isNotBlank() && transaction.type !in setOf(TransactionType.TRANSFER, TransactionType.CREDIT_PAYMENT)) {
            if (isNotEmpty()) append(" • ")
            append(transaction.description)
        }
    }
    val accent = runCatching {
        category?.colorHex?.let { Color(android.graphics.Color.parseColor(it)) }
    }.getOrNull() ?: MaterialTheme.colorScheme.primary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = IconRegistry.category(category?.iconKey ?: "category"),
                contentDescription = null,
                tint = accent
            )
        }
        Column(modifier = Modifier
            .weight(1f)
            .padding(start = 12.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 1)
            if (subtitle.isNotBlank()) {
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = CurrencyFormatter.formatSignedRp(transaction.amount, isPositiveForUser),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = amountColor
            )
            Text(
                text = DateUtils.formatDateShort(transaction.dateMillis),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TransactionListHeader(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
    }
}
