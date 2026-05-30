package com.kkh.wallet.presentation.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kkh.wallet.presentation.components.BarItem
import com.kkh.wallet.presentation.components.DonutChart
import com.kkh.wallet.presentation.components.EmptyState
import com.kkh.wallet.presentation.components.PieLegend
import com.kkh.wallet.presentation.components.PieSlice
import com.kkh.wallet.presentation.components.SectionHeader
import com.kkh.wallet.presentation.components.SimpleBarChart
import com.kkh.wallet.presentation.theme.DangerRed
import com.kkh.wallet.presentation.theme.SuccessGreen
import com.kkh.wallet.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("Analytics") }) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("This month", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            StatCol("Income", state.totalIncomeThisMonth, SuccessGreen)
                            StatCol("Expense", state.totalExpenseThisMonth, DangerRed)
                            StatCol("Net", state.totalIncomeThisMonth - state.totalExpenseThisMonth,
                                if (state.totalIncomeThisMonth >= state.totalExpenseThisMonth) SuccessGreen else DangerRed)
                        }
                    }
                }
            }

            item { SectionHeader("Last 6 months — expense") }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SimpleBarChart(
                            bars = state.monthBuckets.map { BarItem(it.label, it.expense) },
                            barColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item { SectionHeader("Last 6 months — income") }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SimpleBarChart(
                            bars = state.monthBuckets.map { BarItem(it.label, it.income) },
                            barColor = SuccessGreen
                        )
                    }
                }
            }

            item { SectionHeader("Top categories this month") }
            if (state.expensesByCategoryThisMonth.isEmpty()) {
                item {
                    EmptyState(
                        title = "No expense data yet",
                        message = "Add some transactions to see your spending breakdown.",
                        icon = Icons.Outlined.Analytics
                    )
                }
            } else {
                item {
                    val slices = state.expensesByCategoryThisMonth.take(6).map { (cat, value) ->
                        val color = runCatching { Color(android.graphics.Color.parseColor(cat.colorHex)) }
                            .getOrDefault(MaterialTheme.colorScheme.primary)
                        PieSlice(cat.name, value, color)
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            DonutChart(slices = slices, centerLabel = "Total", centerValue = CurrencyFormatter.formatRp(slices.sumOf { it.value }))
                            Spacer(modifier = Modifier.width(8.dp))
                            PieLegend(slices = slices, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            if (state.creditUtilization.isNotEmpty()) {
                item { SectionHeader("Credit & paylater utilization") }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            state.creditUtilization.forEach { (wallet, pct) ->
                                Column {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(wallet.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                        Text("${"%.0f".format(pct)}%", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { (pct / 100f).coerceIn(0f, 1f) },
                                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                        color = if (pct > 80) DangerRed else MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "${CurrencyFormatter.formatRp(wallet.usedLimit)} of ${CurrencyFormatter.formatRp(wallet.creditLimit)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCol(label: String, value: Double, tint: Color) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f))
        Text(CurrencyFormatter.formatRp(value), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = tint)
    }
}
