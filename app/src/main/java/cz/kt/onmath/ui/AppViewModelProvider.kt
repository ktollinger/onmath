package cz.kt.onmath.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cz.kt.onmath.OnMathApplication
import cz.kt.onmath.ui.home.HomeViewModel
import cz.kt.onmath.ui.settings.SettingsViewModel

/**
 * Central ViewModelProvider.Factory for OnMath, following the manual DI pattern.
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val container = onMathApplication().container
            HomeViewModel(
                settingsRepository = container.settingsRepository,
                practiceRepository = container.practiceRepository,
            )
        }

        initializer {
            val container = onMathApplication().container
            SettingsViewModel(
                settingsRepository = container.settingsRepository,
                practiceRepository = container.practiceRepository,
            )
        }
    }
}

fun CreationExtras.onMathApplication(): OnMathApplication {
    return (this[AndroidViewModelFactory.APPLICATION_KEY] as OnMathApplication)
}
