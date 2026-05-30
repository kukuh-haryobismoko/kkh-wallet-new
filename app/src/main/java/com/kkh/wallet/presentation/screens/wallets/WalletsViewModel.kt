package com.kkh.wallet.presentation.screens.wallets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkh.wallet.domain.model.Wallet
import com.kkh.wallet.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class WalletsViewModel @Inject constructor(
    walletRepository: WalletRepository
) : ViewModel() {

    val wallets: StateFlow<List<Wallet>> = walletRepository
        .observeActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
