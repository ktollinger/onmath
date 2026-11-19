package cz.kt.onmath.di

import android.content.Context
import cz.kt.onmath.data.local.OfflinePracticeRepository
import cz.kt.onmath.data.local.OnMathDatabase
import cz.kt.onmath.data.local.PracticeRepository
import cz.kt.onmath.data.settings.DataStoreSettingsRepository
import cz.kt.onmath.data.settings.SettingsRepository

interface AppContainer {
    val settingsRepository: SettingsRepository
    val practiceRepository: PracticeRepository
}

class AppDataContainer(private val context: Context) : AppContainer {

    override val settingsRepository: SettingsRepository by lazy {
        DataStoreSettingsRepository(context)
    }

    override val practiceRepository: PracticeRepository by lazy {
        OfflinePracticeRepository(
            OnMathDatabase.getDatabase(context).practiceDao()
        )
    }
}
