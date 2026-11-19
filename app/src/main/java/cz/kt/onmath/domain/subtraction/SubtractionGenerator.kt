package cz.kt.onmath.domain.subtraction

import cz.kt.onmath.domain.model.SubtractionMethod
import kotlin.math.pow
import kotlin.random.Random

data class SubtractionConfig(
    val minDigits: Int = 2,
    val maxDigits: Int = 6,
) {
    init {
        require(minDigits in 2..6) { "minDigits must be in 2..6." }
        require(maxDigits in 2..6) { "maxDigits must be in 2..6." }
        require(minDigits <= maxDigits) { "minDigits must be <= maxDigits." }
    }
}

/**
 * Represents a single place-value column in column subtraction (placeIndex 0 = units).
 */
data class SubtractionColumn(
    val placeIndex: Int,
    val minuendDigit: Int,
    val subtrahendDigit: Int,
    val incomingBorrow: Boolean,
    val requiresBorrowFromNext: Boolean,
    val expectedResultDigit: Int,
) {
    /**
     * Effective upper digit in Montessori BORROW_TOP after lending 1 to the right column (may be -1 if minuendDigit was 0).
     */
    val effectiveUpperAfterLend: Int
        get() = minuendDigit - (if (incomingBorrow) 1 else 0)

    /**
     * Effective lower digit in Czech CARRY_BOTTOM after receiving 1 carried from the right column.
     */
    val effectiveLowerWithCarry: Int
        get() = subtrahendDigit + (if (incomingBorrow) 1 else 0)
}

data class SubtractionProblem(
    val minuend: Int,
    val subtrahend: Int,
    val answer: Int,
    val columnsRightToLeft: List<SubtractionColumn>,
) {
    val digitCount: Int
        get() = columnsRightToLeft.size

    val borrowCount: Int
        get() = columnsRightToLeft.count { it.requiresBorrowFromNext }

    val key: String
        get() = "$minuend-$subtrahend"

    fun hintForColumn(placeIndex: Int, method: SubtractionMethod): String {
        val col = columnsRightToLeft.getOrNull(placeIndex) ?: return ""
        val placeName = placeLabel(placeIndex)

        return when (method) {
            SubtractionMethod.BORROW_TOP -> {
                val topAfterLend = col.effectiveUpperAfterLend
                if (col.requiresBorrowFromNext) {
                    val borrowedTop = topAfterLend + 10
                    "$placeName: Půjč si 1 z vyššího řádu → $borrowedTop − ${col.subtrahendDigit} = ${col.expectedResultDigit}."
                } else {
                    "$placeName: $topAfterLend − ${col.subtrahendDigit} = ${col.expectedResultDigit}."
                }
            }
            SubtractionMethod.CARRY_BOTTOM -> {
                val bottomWithCarry = col.effectiveLowerWithCarry
                if (col.requiresBorrowFromNext) {
                    val topPlusTen = col.minuendDigit + 10
                    "$placeName: Přičti 10 → $topPlusTen − $bottomWithCarry = ${col.expectedResultDigit} (a 1 jde dolů do dalšího řádu)."
                } else {
                    "$placeName: ${col.minuendDigit} − $bottomWithCarry = ${col.expectedResultDigit}."
                }
            }
        }
    }

    private fun placeLabel(placeIndex: Int): String = when (placeIndex) {
        0 -> "Jednotky"
        1 -> "Desítky"
        2 -> "Stovky"
        3 -> "Tisíce"
        4 -> "Desetitisíce"
        5 -> "Statisíce"
        else -> "Řád ${placeIndex + 1}"
    }
}

class SubtractionGenerator(
    private val random: Random = Random.Default,
) {
    fun generate(config: SubtractionConfig = SubtractionConfig()): SubtractionProblem {
        repeat(300) {
            val digits = random.nextInt(config.minDigits, config.maxDigits + 1)
            val minVal = 10.0.pow(digits - 1).toInt()
            val maxVal = 10.0.pow(digits).toInt() - 1

            val minuend = random.nextInt(minVal, maxVal + 1)
            val subtrahendMin = if (digits > 2) minVal / 10 else 10
            if (minuend <= subtrahendMin) return@repeat

            val subtrahend = random.nextInt(subtrahendMin, minuend)
            val problem = buildProblem(minuend, subtrahend)
            if (problem.borrowCount >= 1) {
                return problem
            }
        }
        // Safe deterministic fallback with borrow
        return buildProblem(402, 158)
    }

    fun generateSet(
        config: SubtractionConfig = SubtractionConfig(),
        count: Int = 10,
    ): List<SubtractionProblem> {
        require(count > 0) { "Count must be positive." }
        val result = mutableListOf<SubtractionProblem>()
        var attempts = 0
        while (result.size < count && attempts < count * 10) {
            attempts++
            val candidate = generate(config)
            if (result.none { it.key == candidate.key }) {
                result.add(candidate)
            }
        }
        while (result.size < count) {
            result.add(generate(config))
        }
        return result
    }

    companion object {
        fun buildProblem(minuend: Int, subtrahend: Int): SubtractionProblem {
            require(minuend > subtrahend && subtrahend >= 0) {
                "Minuend ($minuend) must be greater than non-negative subtrahend ($subtrahend)."
            }
            val topStr = minuend.toString()
            val totalDigits = topStr.length
            val columns = mutableListOf<SubtractionColumn>()

            var carry = 0
            var m = minuend
            var s = subtrahend

            for (placeIndex in 0 until totalDigits) {
                val mDigit = m % 10
                val sDigit = s % 10
                m /= 10
                s /= 10

                val incomingBorrow = carry == 1
                val diff = mDigit - carry - sDigit
                val requiresBorrow = diff < 0
                val resultDigit = if (requiresBorrow) diff + 10 else diff
                carry = if (requiresBorrow) 1 else 0

                columns.add(
                    SubtractionColumn(
                        placeIndex = placeIndex,
                        minuendDigit = mDigit,
                        subtrahendDigit = sDigit,
                        incomingBorrow = incomingBorrow,
                        requiresBorrowFromNext = requiresBorrow,
                        expectedResultDigit = resultDigit,
                    )
                )
            }

            return SubtractionProblem(
                minuend = minuend,
                subtrahend = subtrahend,
                answer = minuend - subtrahend,
                columnsRightToLeft = columns,
            )
        }
    }
}
