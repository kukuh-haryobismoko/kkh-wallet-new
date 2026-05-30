package com.kkh.wallet.presentation.screens.addeditwallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkh.wallet.domain.model.Wallet
import com.kkh.wallet.domain.model.WalletType
import com.kkh.wallet.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddEditWalletState(
    val id: Long = 0L,
    val name: String = "",
    val type: WalletType = WalletType.CASH,
    val providerName: String = "",
    val colorHex: String = "#6750A4",
    val currentBalanceInput: String = "",
    val creditLimitInput: String = "",
    val usedLimitInput: String = "",
    val billingDateInput: String = "",
    val dueDateInput: String = "",
    val notes: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val finished: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AddEditWalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddEditWalletState())
    val state: StateFlow<AddEditWalletState> = _state.asStateFlow()

    fun load(walletId: Long?) {
        if (walletId == null) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val w = walletRepository.findById(walletId)
            if (w != null) {
                _state.value = AddEditWalletState(
                    id = w.id,
                    name = w.name,
                    type = w.type,
                    providerName = w.providerName,
                    colorHex = w.colorHex,
                    currentBalanceInput = if (w.currentBalance > 0) w.currentBalance.toLong().toString() else "",
                    creditLimitInput = if (w.creditLimit > 0) w.creditLimit.toLong().toString() else "",
                    usedLimitInput = if (w.usedLimit > 0) w.usedLimit.toLong().toString() else "",
                    billingDateInput = w.billingDate?.toString().orEmpty(),
                    dueDateInput = w.dueDate?.toString().orEmpty(),
                    notes = w.notes
                )
            } else {
                _state.update { it.copy(isLoading = false, errorMessage = "Wallet not found") }
            }
        }
    }

    fun setName(v: String) = _state.update { it.copy(name = v, errorMessage = null) }
    fun setType(v: WalletType) = _state.update { it.copy(type = v, errorMessage = null) }
    fun setProvider(v: String) = _state.update { it.copy(providerName = v) }
    fun setColor(v: String) = _state.update { it.copy(colorHex = v) }
    fun setCurrentBalance(v: String) = _state.update { it.copy(currentBalanceInput = sanitizeNumeric(v)) }
    fun setCreditLimit(v: String) = _state.update { it.copy(creditLimitInput = sanitizeNumeric(v)) }
    fun setUsedLimit(v: String) = _state.update { it.copy(usedLimitInput = sanitizeNumeric(v)) }
    fun setBillingDate(v: String) = _state.update { it.copy(billingDateInput = sanitizeDay(v)) }
    fun setDueDate(v: String) = _state.update { it.copy(dueDateInput = sanitizeDay(v)) }
    fun setNotes(v: String) = _state.update { it.copy(notes = v) }

    private fun sanitizeNumeric(input: String): String = input.filter { it.isDigit() }.take(15)
    private fun sanitizeDay(input: String): String {
        val digits = input.filter { it.isDigit() }.take(2)
        val n = digits.toIntOrNull() ?: return digits
        return if (n in 1..31) digits else digits.dropLast(1)
    }

    fun save() {
        val s = _state.value
        if (s.name.isBlank()) {
            _state.update { it.copy(errorMessage = "Wallet name is required") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val wallet = Wallet(
                    id = s.id,
                    name = s.name.trim(),
                    type = s.type,
                    providerName = s.providerName.trim(),
                    colorHex = s.colorHex,
                    currentBalance = if (!s.type.isCreditBased) s.currentBalanceInput.toDoubleOrNull() ?: 0.0 else 0.0,
                    creditLimit = if (s.type.isCreditBased) s.creditLimitInput.toDoubleOrNull() ?: 0.0 else 0.0,
                    usedLimit = if (s.type.isCreditBased) s.usedLimitInput.toDoubleOrNull() ?: 0.0 else 0.0,
                    billingDate = s.billingDateInput.toIntOrNull()?.takeIf { it in 1..31 },
                    dueDate = s.dueDateInput.toIntOrNull()?.takeIf { it in 1..31 },
                    notes = s.notes.trim(),
                    createdAt = if (s.id == 0L) System.currentTimeMillis() else System.currentTimeMillis()
                )
                walletRepository.upsert(wallet)
                _state.update { it.copy(isSaving = false, finished = true) }
            } catch (t: Throwable) {
                _state.update { it.copy(isSaving = false, errorMessage = t.message ?: "Failed to save wallet") }
            }
        }
    }
}
