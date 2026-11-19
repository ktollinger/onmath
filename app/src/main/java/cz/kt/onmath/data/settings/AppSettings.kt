package cz.kt.onmath.data.settings

import cz.kt.onmath.domain.model.SubtractionMethod

data class AppSettings(
    val subtractionMethod: SubtractionMethod = SubtractionMethod.BORROW_TOP,
    val subtractionMinDigits: Int = 2,
    val subtractionMaxDigits: Int = 6,
    val multiplicationTables: Set<Int> = (1..10).toSet(),
    val multiplicationIncludeDivision: Boolean = false,
    val expressionWithParentheses: Boolean = true,
)
