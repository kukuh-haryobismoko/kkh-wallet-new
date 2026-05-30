package com.kkh.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kkh.wallet.domain.model.Wallet
import com.kkh.wallet.domain.model.WalletType
import com.kkh.wallet.presentation.theme.GradientBlue
import com.kkh.wallet.presentation.theme.GradientGreen
import com.kkh.wallet.presentation.theme.GradientOrange
import com.kkh.wallet.presentation.theme.GradientPink
import com.kkh.wallet.presentation.theme.GradientPurple
import com.kkh.wallet.presentation.theme.GradientSlate
import com.kkh.wallet.util.CurrencyFormatter

/**
 * A gradient debit/credit card-style tile that summarizes a wallet's spendable
 * power and (for credit wallets) limit utilization.
 */
@Composable
fun WalletCard(
    wallet: Wallet,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val gradient = gradientFor(wallet)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(gradient))
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = wallet.name,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = wallet.providerName.ifBlank { wallet.type.displayName },
                        color = Color.White.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = IconRegistry.wallet(wallet.type),
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column {
                Text(
                    text = if (wallet.type.isCreditBased) "Available Limit" else "Balance",
                    color = Color.White.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = CurrencyFormatter.formatRp(wallet.spendablePower),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                if (wallet.type.isCreditBased) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { wallet.utilizationPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.25f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${"%.0f".format(wallet.utilizationPercent)}% used",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "Limit ${CurrencyFormatter.formatRp(wallet.creditLimit)}",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

private fun gradientFor(wallet: Wallet): List<Color> = when (wallet.type) {
    WalletType.CASH -> GradientGreen
    WalletType.BANK -> GradientBlue
    WalletType.E_WALLET -> GradientPurple
    WalletType.CREDIT_CARD -> GradientSlate
    WalletType.PAYLATER -> if (wallet.colorHex.contains("EC", ignoreCase = true)) GradientPink else GradientOrange
}
