package cz.kt.onmath.ui.navigation

import cz.kt.onmath.domain.model.MathTopic

sealed class OnMathScreens(val route: String) {
    data object Home : OnMathScreens("home")
    data object Settings : OnMathScreens("settings")
    data object Multiplication : OnMathScreens("practice/multiplication")
    data object Expressions : OnMathScreens("practice/expressions")
    data object Subtraction : OnMathScreens("practice/subtraction")

    companion object {
        fun routeForTopic(topic: MathTopic): String = when (topic) {
            MathTopic.MULTIPLICATION -> Multiplication.route
            MathTopic.EXPRESSIONS -> Expressions.route
            MathTopic.SUBTRACTION -> Subtraction.route
        }
    }
}
