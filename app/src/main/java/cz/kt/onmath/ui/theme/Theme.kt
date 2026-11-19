package cz.kt.onmath.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = OnPrimaryWhite,
    primaryContainer = PrimaryContainerBlue,
    onPrimaryContainer = OnPrimaryContainerBlue,
    secondary = SecondaryTeal,
    secondaryContainer = SecondaryContainerTeal,
    error = ErrorRed,
    errorContainer = ErrorContainerRed,
    background = SurfaceLight,
    surface = SurfaceLight,
    surfaceContainerLow = SurfaceCard,
)

@Composable
fun OnMathTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography(),
        content = content,
    )
}
