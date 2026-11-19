package cz.kt.onmath.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.viewModelFactory
import cz.kt.onmath.OnMathApplication

/**
 * Central ViewModelProvider.Factory for OnMath, following the manual DI pattern.
 * Individual screen ViewModel initializers will be registered here in the UI layer.
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Screen ViewModels will be added as each screen is implemented.
    }
}

fun CreationExtras.onMathApplication(): OnMathApplication {
    return (this[AndroidViewModelFactory.APPLICATION_KEY] as OnMathApplication)
}
