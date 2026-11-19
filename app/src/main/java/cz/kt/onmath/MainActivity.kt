package cz.kt.onmath

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import cz.kt.onmath.ui.navigation.OnMathNavHost
import cz.kt.onmath.ui.theme.OnMathTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OnMathTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    OnMathNavHost()
                }
            }
        }
    }
}
