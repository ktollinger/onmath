package cz.kt.onmath

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.kt.onmath.data.settings.AppSettings
import cz.kt.onmath.ui.navigation.OnMathNavHost
import cz.kt.onmath.ui.theme.OnMathTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settingsRepository = (application as OnMathApplication).container.settingsRepository

        setContent {
            val settings by settingsRepository.settingsFlow.collectAsStateWithLifecycle(
                initialValue = AppSettings(),
            )
            val baseContext = LocalContext.current
            val baseConfig = LocalConfiguration.current
            val (localizedContext, localizedConfig) = remember(settings.language, baseContext, baseConfig) {
                val tag = settings.language.languageTag
                if (tag == null) {
                    baseContext to baseConfig
                } else {
                    val locale = Locale.forLanguageTag(tag)
                    val config = Configuration(baseConfig).apply { setLocale(locale) }
                    baseContext.createConfigurationContext(config) to config
                }
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedConfig,
            ) {
                OnMathTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        OnMathNavHost()
                    }
                }
            }
        }
    }
}
