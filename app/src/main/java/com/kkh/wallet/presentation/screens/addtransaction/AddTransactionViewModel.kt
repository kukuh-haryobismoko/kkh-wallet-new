package com.kkh.wallet.presentation.screens.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkh.wallet.domain.model.Category
import com.kkh.wallet.domain.model.Transaction
import com.kkh.wallet.domain.model.TransactionType
import com.kkh.wallet.domain.model.Wallet
import com.kkh.wallet.domain.repository.CategoryRepository
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

data class AddTransactionState(
    val editingId: Long = 0L,
    val type: TransactionType = TransactionType.EXPENSE,
    val walletId: Long? = null,
    val destinationWalletId: Long? = null,
    val categoryId: Long? = null,
    val amountInput: String = "",
    val description: String = "",
    val merchant: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val attachmentUri: String? = null,
    val tags: String = "",
    val isSaving: Boolean = false,
    val finished: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    walletRepository: WalletRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddTransactionState())
    val state: StateFlow<AddTransactionState> = _state.asStateFlow()

    val wallets: StateFlow<List<Wallet>> = walletRepository.observeActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categories: StateFlow<List<Category>> = categoryRepository.observeActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun init(initialWalletId: Long?, transactionId: Long?) {
        if (transactionId != null) {
            viewModelScope.launch {
                val tx = transactionRepository.findById(transactionId) ?: return@launch
                _state.value = AddTransactionState(
                    editingId = tx.id,
                    type = tx.type,
                    walletId = tx.walletId,
                    destinationWalletId = tx.destinationWalletId,
                    categoryId = tx.categoryId,
                    amountInput = tx.amount.toLong().toString(),
                    description = tx.description,
                    merchant = tx.merchant,
                    dateMillis = tx.dateMillis,
                    attachmentUri = tx.attachmentUri,
                    tags = tx.tags.joinToString(", ")
                )
            }
        } else if (initialWalletId != null) {
            _state.update { it.copy(walletId = initialWalletId) }
        }
    }

    fun setType(t: TransactionType) = _state.update {
        // Reset incompatible fields if type changes
        it.copy(
            type = t,
            destinationWalletId = if (t == TransactionType.TRANSFER || t == TransactionType.CREDIT_PAYMENT) it.destinationWalletId else null,
            categoryId = if (t == TransactionType.EXPENSE || t == TransactionType.INCOME) it.categoryId else null,
            errorMessage = null
        )
    }
    fun setWallet(id: Long) = _state.update { it.copy(walletId = id, errorMessage = null) }
    fun setDestination(id: Long) = _state.update { it.copy(destinationWalletId = id, errorMessage = null) }
    fun setCategory(id: Long) = _state.update { it.copy(categoryId = id, errorMessage = null) }
    fun setAmount(v: String) = _state.update { it.copy(amountInput = v.filter { c -> c.isDigit() }.take(15)) }
    fun setDescription(v: String) = _state.update { it.copy(description = v) }
    fun setMerchant(v: String) = _state.update { it.copy(merchant = v) }
    fun setDate(millis: Long) = _state.update { it.copy(dateMillis = millis) }
    fun setAttachment(uri: String?) = _state.update { it.copy(attachmentUri = uri) }
    fun setTags(v: String) = _state.update { it.copy(tags = v) }

    fun save() {
        val s = _state.value
        val amount = s.amountInput.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            _state.update { it.copy(errorMessage = "Enter an amount greater than zero") }; return
        }
        val walletId = s.walletId
        if (walletId == null) {
            _state.update { it.copy(errorMessage = "Select a source wallet") }; return
        }
        if ((s.type == TransactionType.TRANSFER || s.type == TransactionType.CREDIT_PAYMENT) && s.destinationWalletId == null) {
            _state.update { it.copy(errorMessage = "Select a destination wallet") }; return
        }
        if (s.type == TransactionType.TRANSFER && s.destinationWalletId == walletId) {
            _state.update { it.copy(errorMessage = "Source and destination must differ") }; return
        }
        if ((s.type == TransactionType.EXPENSE || s.type == TransactionType.INCOME) && s.categoryId == null) {
            _state.update { it.copy(errorMessage = "Select a category") }; return
        }

        val tx = Transaction(
            id = s.editingId,
            walletId = walletId,
            destinationWalletId = s.destinationWalletId,
            categoryId = s.categoryId,
            amount = amount,
            type = s.type,
            description = s.description.trim(),
            merchant = s.merchant.trim(),
            dateMillis = s.dateMillis,
            attachmentUri = s.attachmentUri,
            tags = s.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
        )

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                if (s.editingId == 0L) transactionRepository.addTransaction(tx)
                else transactionRepository.updateTransaction(tx)
                _state.update { it.copy(isSaving = false, finished = true) }
            } catch (t: Throwable) {
                _state.update { it.copy(isSaving = false, errorMessage = t.message ?: "Failed to save") }
            }
        }
    }

    fun delete() {
        val id = _state.value.editingId
        if (id == 0L) return
        viewModelScope.launch {
            transactionRepository.deleteTransaction(id)
            _state.update { it.copy(finished = true) }
        }
    }
}
