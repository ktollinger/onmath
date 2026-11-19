package cz.kt.onmath.domain.subtraction

import cz.kt.onmath.domain.model.SubtractionMethod
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.random.Random

class SubtractionGeneratorTest {

    @Test
    fun buildProblem_402Minus158_computesColumnsAndBorrowsCorrectly() {
        val problem = SubtractionGenerator.buildProblem(402, 158)
        assertEquals(244, problem.answer)
        assertEquals(3, problem.digitCount)
        assertEquals(2, problem.borrowCount)

        // Units: 2 - 8 -> borrow 1 -> 12 - 8 = 4
        val units = problem.columnsRightToLeft[0]
        assertFalse(units.incomingBorrow)
        assertTrue(units.requiresBorrowFromNext)
        assertEquals(4, units.expectedResultDigit)

        // Tens: 0 - 1 (incoming) - 5 -> borrow 1 -> 9 - 5 = 4
        val tens = problem.columnsRightToLeft[1]
        assertTrue(tens.incomingBorrow)
        assertTrue(tens.requiresBorrowFromNext)
        assertEquals(4, tens.expectedResultDigit)

        // Hundreds: 4 - 1 (incoming) - 1 = 2
        val hundreds = problem.columnsRightToLeft[2]
        assertTrue(hundreds.incomingBorrow)
        assertFalse(hundreds.requiresBorrowFromNext)
        assertEquals(2, hundreds.expectedResultDigit)
    }

    @Test
    fun hintForColumn_differsBySubtractionMethod() {
        val problem = SubtractionGenerator.buildProblem(402, 158)
        val montessoriHint = problem.hintForColumn(0, SubtractionMethod.BORROW_TOP)
        val czechHint = problem.hintForColumn(0, SubtractionMethod.CARRY_BOTTOM)

        assertTrue(montessoriHint.contains("Půjč si"))
        assertTrue(czechHint.contains("Přičti 10"))
    }

    @Test
    fun generate_respectsDigitRangeAndAlwaysRequiresBorrow() {
        val generator = SubtractionGenerator(Random(55))
        val config = SubtractionConfig(minDigits = 2, maxDigits = 6)

        repeat(100) {
            val problem = generator.generate(config)
            assertTrue(problem.minuend > problem.subtrahend)
            assertEquals(problem.minuend - problem.subtrahend, problem.answer)
            assertTrue(problem.digitCount in 2..6)
            assertTrue(problem.borrowCount >= 1, "Expected at least one borrow in ${problem.key}")
        }
    }

    @Test
    fun generateSet_returnsTenProblems() {
        val generator = SubtractionGenerator(Random(5))
        val set = generator.generateSet(count = 10)
        assertEquals(10, set.size)
    }
}
