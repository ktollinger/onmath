package cz.kt.onmath.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cz.kt.onmath.ui.home.HomeScreen
import cz.kt.onmath.ui.settings.SettingsScreen

@Composable
fun OnMathNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = OnMathScreens.Home.route,
        modifier = modifier,
    ) {
        composable(OnMathScreens.Home.route) {
            HomeScreen(
                onOpenTopic = { topic ->
                    navController.navigate(OnMathScreens.routeForTopic(topic))
                },
                onOpenSettings = {
                    navController.navigate(OnMathScreens.Settings.route)
                },
            )
        }

        composable(OnMathScreens.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.navigateUp() },
            )
        }

        composable(OnMathScreens.Multiplication.route) {
            PracticePlaceholderScreen(
                title = "Násobilka",
                onBackClick = { navController.navigateUp() },
            )
        }

        composable(OnMathScreens.Expressions.route) {
            PracticePlaceholderScreen(
                title = "Výrazy a závorky",
                onBackClick = { navController.navigateUp() },
            )
        }

        composable(OnMathScreens.Subtraction.route) {
            PracticePlaceholderScreen(
                title = "Odčítání pod sebou",
                onBackClick = { navController.navigateUp() },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PracticePlaceholderScreen(
    title: String,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zpět",
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Text("$title – připravuje se")
        }
    }
}
