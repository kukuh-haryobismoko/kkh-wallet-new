package com.kkh.wallet.presentation.screens.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkh.wallet.domain.model.Transaction
import com.kkh.wallet.domain.model.TransactionType
import com.kkh.wallet.domain.model.Wallet
import com.kkh.wallet.domain.repository.TransactionRepository
import com.kkh.wallet.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TransferState(
    val sourceWalletId: Long? = null,
    val destinationWalletId: Long? = null,
    val amountInput: String = "",
    val description: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val isSaving: Boolean = false,
    val finished: Boolean = false,
    val errorMessage: String? = null
) {
    val isCreditPayment: Boolean
        get() = false // computed in VM where wallets are available
}

@HiltViewModel
class TransferViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    walletRepository: WalletRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TransferState())
    val state: StateFlow<TransferState> = _state.asStateFlow()

    val wallets: StateFlow<List<Wallet>> = walletRepository.observeActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun init(initialSourceWalletId: Long?) {
        if (initialSourceWalletId != null) {
            _state.update { it.copy(sourceWalletId = initialSourceWalletId) }
        }
    }

    fun setSource(id: Long) = _state.update { it.copy(sourceWalletId = id, errorMessage = null) }
    fun setDestination(id: Long) = _state.update { it.copy(destinationWalletId = id, errorMessage = null) }
    fun setAmount(v: String) = _state.update { it.copy(amountInput = v.filter { c -> c.isDigit() }.take(15)) }
    fun setDescription(v: String) = _state.update { it.copy(description = v) }
    fun setDate(millis: Long) = _state.update { it.copy(dateMillis = millis) }

    fun save() {
        val s = _state.value
        val amount = s.amountInput.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            _state.update { it.copy(errorMessage = "Enter an amount greater than zero") }; return
        }
        val src = s.sourceWalletId
        val dst = s.destinationWalletId
        if (src == null || dst == null) {
            _state.update { it.copy(errorMessage = "Select source and destination wallets") }; return
        }
        if (src == dst) {
            _state.update { it.copy(errorMessage = "Source and destination must differ") }; return
        }
        val allWallets = wallets.value
        val srcWallet = allWallets.firstOrNull { it.id == src }
        val dstWallet = allWallets.firstOrNull { it.id == dst }
        if (srcWallet == null || dstWallet == null) {
            _state.update { it.copy(errorMessage = "Wallet not found") }; return
        }
        if (srcWallet.type.isCreditBased) {
            _state.update { it.copy(errorMessage = "Source must be a balance-based wallet") }; return
        }

        val type = if (dstWallet.type.isCreditBased) TransactionType.CREDIT_PAYMENT else TransactionType.TRANSFER

        val tx = Transaction(
            id = 0L,
            walletId = src,
            destinationWalletId = dst,
            categoryId = null,
            amount = amount,
            type = type,
            description = s.description.trim(),
            merchant = "",
            dateMillis = s.dateMillis,
            attachmentUri = null,
            tags = emptyList()
        )

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                transactionRepository.addTransaction(tx)
                _state.update { it.copy(isSaving = false, finished = true) }
            } catch (t: Throwable) {
                _state.update { it.copy(isSaving = false, errorMessage = t.message ?: "Failed to save") }
            }
        }
    }
}
