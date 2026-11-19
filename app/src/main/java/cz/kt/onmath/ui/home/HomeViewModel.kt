package cz.kt.onmath.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.kt.onmath.data.local.MistakeRecordEntity
import cz.kt.onmath.data.local.PracticeRepository
import cz.kt.onmath.data.local.TopicStats
import cz.kt.onmath.data.settings.AppSettings
import cz.kt.onmath.data.settings.SettingsRepository
import cz.kt.onmath.domain.model.MathTopic
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val settings: AppSettings = AppSettings(),
    val statsByTopic: Map<MathTopic, TopicStats> = emptyMap(),
    val recentMistakes: List<MistakeRecordEntity> = emptyList(),
)

class HomeViewModel(
    settingsRepository: SettingsRepository,
    practiceRepository: PracticeRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        settingsRepository.settingsFlow,
        practiceRepository.getTopicStats(),
        practiceRepository.getRecentMistakes(limit = 5),
    ) { settings, statsList, mistakes ->
        val statsMap = statsList.mapNotNull { stat ->
            runCatching { MathTopic.valueOf(stat.topic) to stat }.getOrNull()
        }.toMap()

        HomeUiState(
            settings = settings,
            statsByTopic = statsMap,
            recentMistakes = mistakes,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )
}
