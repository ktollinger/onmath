package cz.kt.onmath.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.kt.onmath.data.local.PracticeRepository
import cz.kt.onmath.data.settings.AppSettings
import cz.kt.onmath.data.settings.SettingsRepository
import cz.kt.onmath.domain.model.AppLanguage
import cz.kt.onmath.domain.model.SubtractionMethod
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val practiceRepository: PracticeRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings(),
    )

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            settingsRepository.updateLanguage(language)
        }
    }

    fun setSubtractionMethod(method: SubtractionMethod) {
        viewModelScope.launch {
            settingsRepository.updateSubtractionMethod(method)
        }
    }

    fun setSubtractionDigitRange(minDigits: Int, maxDigits: Int) {
        viewModelScope.launch {
            settingsRepository.updateSubtractionDigitRange(minDigits, maxDigits)
        }
    }

    fun toggleMultiplicationTable(table: Int) {
        val current = settings.value.multiplicationTables
        val updated = if (table in current) {
            if (current.size == 1) current else current - table
        } else {
            current + table
        }
        viewModelScope.launch {
            settingsRepository.updateMultiplicationTables(updated)
        }
    }

    fun selectAllMultiplicationTables() {
        viewModelScope.launch {
            settingsRepository.updateMultiplicationTables((1..10).toSet())
        }
    }

    fun setMultiplicationIncludeDivision(include: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateMultiplicationIncludeDivision(include)
        }
    }

    fun setExpressionWithParentheses(withParentheses: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateExpressionWithParentheses(withParentheses)
        }
    }

    fun setExpressionLargeIntermediate(allowLarge: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateExpressionLargeIntermediate(allowLarge)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            practiceRepository.clearAllHistory()
        }
    }
}
