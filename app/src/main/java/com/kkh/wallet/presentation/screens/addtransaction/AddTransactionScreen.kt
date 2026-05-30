package com.kkh.wallet.presentation.screens.addtransaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kkh.wallet.domain.model.TransactionType
import com.kkh.wallet.domain.model.WalletType
import com.kkh.wallet.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    initialWalletId: Long?,
    transactionId: Long?,
    onBack: () -> Unit,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { viewModel.init(initialWalletId, transactionId) }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val wallets by viewModel.wallets.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.finished) { if (state.finished) onBack() }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbar.showSnackbar(it) }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.dateMillis)
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.setDate(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (transactionId == null) "New Transaction" else "Edit Transaction") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = null) }
                },
                actions = {
                    if (transactionId != null) {
                        IconButton(onClick = viewModel::delete) { Icon(Icons.Outlined.Delete, contentDescription = "Delete") }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Type", style = MaterialTheme.typography.titleMedium)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TransactionType.values().forEach { t ->
                        FilterChip(
                            selected = state.type == t,
                            onClick = { viewModel.setType(t) },
                            label = { Text(t.displayName) }
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = state.amountInput,
                    onValueChange = viewModel::setAmount,
                    label = { Text("Amount (IDR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                val sourceLabel = when (state.type) {
                    TransactionType.TRANSFER -> "From wallet"
                    TransactionType.CREDIT_PAYMENT -> "Pay from"
                    TransactionType.EXPENSE -> "Wallet"
                    TransactionType.INCOME -> "Receive into"
                }
                Text(sourceLabel, style = MaterialTheme.typography.titleMedium)
                val candidates = when (state.type) {
                    TransactionType.TRANSFER, TransactionType.CREDIT_PAYMENT ->
                        wallets.filter { !it.type.isCreditBased }
                    else -> wallets
                }
                FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    candidates.forEach { w ->
                        FilterChip(
                            selected = state.walletId == w.id,
                            onClick = { viewModel.setWallet(w.id) },
                            label = { Text(w.name) }
                        )
                    }
                }
            }

            if (state.type == TransactionType.TRANSFER || state.type == TransactionType.CREDIT_PAYMENT) {
                item {
                    val destLabel = if (state.type == TransactionType.TRANSFER) "To wallet" else "Pay to (credit/paylater)"
                    Text(destLabel, style = MaterialTheme.typography.titleMedium)
                    val candidates = when (state.type) {
                        TransactionType.TRANSFER -> wallets.filter { !it.type.isCreditBased && it.id != state.walletId }
                        TransactionType.CREDIT_PAYMENT -> wallets.filter { it.type.isCreditBased }
                        else -> emptyList()
                    }
                    FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        candidates.forEach { w ->
                            FilterChip(
                                selected = state.destinationWalletId == w.id,
                                onClick = { viewModel.setDestination(w.id) },
                                label = { Text(w.name) }
                            )
                        }
                    }
                }
            }

            if (state.type == TransactionType.EXPENSE || state.type == TransactionType.INCOME) {
                item {
                    Text("Category", style = MaterialTheme.typography.titleMedium)
                    val candidates = categories.filter {
                        if (state.type == TransactionType.INCOME) it.isIncome else !it.isIncome
                    }
                    FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        candidates.forEach { c ->
                            FilterChip(
                                selected = state.categoryId == c.id,
                                onClick = { viewModel.setCategory(c.id) },
                                label = { Text(c.name) }
                            )
                        }
                    }
                }
            }

            item {
                Text("Date", style = MaterialTheme.typography.titleMedium)
                AssistChip(
                    onClick = { showDatePicker = true },
                    label = { Text(DateUtils.formatDay(state.dateMillis)) },
                    colors = AssistChipDefaults.assistChipColors()
                )
            }

            item {
                OutlinedTextField(
                    value = state.description,
                    onValueChange = viewModel::setDescription,
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = state.merchant,
                    onValueChange = viewModel::setMerchant,
                    label = { Text("Merchant / Store (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = state.tags,
                    onValueChange = viewModel::setTags,
                    label = { Text("Tags, comma-separated (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Button(
                    onClick = viewModel::save,
                    enabled = !state.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors()
                ) {
                    Text(if (state.isSaving) "Saving…" else "Save Transaction")
                }
            }
        }
    }
}
