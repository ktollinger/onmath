package cz.kt.onmath

import android.app.Application
import cz.kt.onmath.di.AppContainer
import cz.kt.onmath.di.AppDataContainer

class OnMathApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}
