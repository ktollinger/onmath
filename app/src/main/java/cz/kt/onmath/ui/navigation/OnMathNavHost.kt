package cz.kt.onmath.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cz.kt.onmath.ui.AppViewModelProvider
import cz.kt.onmath.ui.home.HomeScreen
import cz.kt.onmath.ui.practice.ExpressionsScreen
import cz.kt.onmath.ui.practice.ExpressionsViewModel
import cz.kt.onmath.ui.practice.MultiplicationViewModel
import cz.kt.onmath.ui.practice.StandardPracticeScreen
import cz.kt.onmath.ui.practice.subtraction.SubtractionScreen
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
            val vm: MultiplicationViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val state by vm.uiState.collectAsStateWithLifecycle()
            StandardPracticeScreen(
                state = state,
                onDigit = vm::onDigit,
                onBackspace = vm::onBackspace,
                onSubmit = vm::onSubmit,
                onHint = vm::onHint,
                onRestart = vm::startNewSession,
                onBackClick = { navController.navigateUp() },
            )
        }

        composable(OnMathScreens.Expressions.route) {
            val vm: ExpressionsViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val state by vm.uiState.collectAsStateWithLifecycle()
            ExpressionsScreen(
                state = state,
                onSelectOperator = vm::onSelectOperator,
                onSelectSubstituted = vm::onSelectSubstituted,
                onFocusFinalAnswer = vm::onFocusFinalAnswer,
                onCancelIntermediate = vm::onCancelIntermediate,
                onUndoIntermediate = vm::onUndoIntermediate,
                onDigit = vm::onDigit,
                onBackspace = vm::onBackspace,
                onSubmit = vm::onSubmit,
                onNextProblem = vm::onNextProblem,
                onHint = vm::onHint,
                onRestart = vm::startNewSession,
                onBackClick = { navController.navigateUp() },
            )
        }

        composable(OnMathScreens.Subtraction.route) {
            SubtractionScreen(
                onBackClick = { navController.navigateUp() },
            )
        }
    }
}
