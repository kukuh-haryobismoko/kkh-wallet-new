package com.kkh.wallet.presentation.screens.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkh.wallet.data.backup.BackupManager
import com.kkh.wallet.data.preferences.ThemeMode
import com.kkh.wallet.data.preferences.UserPreferences
import com.kkh.wallet.data.preferences.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isWorking: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val backupManager: BackupManager
) : ViewModel() {

    val settings: StateFlow<UserSettings> = userPreferences.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserSettings())

    private val _ui = MutableStateFlow(SettingsUiState())
    val ui: StateFlow<SettingsUiState> = _ui.asStateFlow()

    fun consumeMessage() = _ui.update { it.copy(message = null) }

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { userPreferences.setTheme(mode) }
    }

    fun setBiometric(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setBiometric(enabled) }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            _ui.update { it.copy(isWorking = true) }
            val result = backupManager.export(uri)
            _ui.update {
                it.copy(
                    isWorking = false,
                    message = result.fold(
                        onSuccess = { count -> "Backup saved ($count items)" },
                        onFailure = { t -> "Backup failed: ${t.message}" }
                    )
                )
            }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            _ui.update { it.copy(isWorking = true) }
            val result = backupManager.import(uri)
            _ui.update {
                it.copy(
                    isWorking = false,
                    message = result.fold(
                        onSuccess = { count -> "Restored $count items" },
                        onFailure = { t -> "Restore failed: ${t.message}" }
                    )
                )
            }
        }
    }
}
