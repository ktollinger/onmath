package cz.kt.onmath.domain.expression

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ExpressionGeneratorTest {

    @Test
    fun expressionNode_respectsOperatorPrecedenceAndParentheses() {
        // 2 + 3 × 4 = 14
        val noParens = BinaryNode(
            left = NumberNode(2),
            operator = BinaryOperator.ADD,
            right = BinaryNode(NumberNode(3), BinaryOperator.MULTIPLY, NumberNode(4)),
        )
        assertEquals("2 + 3 × 4", noParens.format())
        assertEquals(14, noParens.evaluate())

        // (2 + 3) × 4 = 20
        val withParens = BinaryNode(
            left = BinaryNode(NumberNode(2), BinaryOperator.ADD, NumberNode(3), explicitParentheses = true),
            operator = BinaryOperator.MULTIPLY,
            right = NumberNode(4),
        )
        assertEquals("(2 + 3) × 4", withParens.format())
        assertEquals(20, withParens.evaluate())
    }

    @Test
    fun generate_withoutParentheses_hasValidNonNegativeResultsAndNoParens() {
        val generator = ExpressionGenerator(Random(99))
        repeat(100) {
            val problem = generator.generate(ExpressionDifficulty.WITHOUT_PARENTHESES)
            assertTrue(problem.root.allIntermediatesNonNegative())
            assertTrue(problem.answer >= 0)
            assertFalse("Unexpected parentheses in ${problem.displayText}", problem.displayText.contains("("))
        }
    }

    @Test
    fun generate_withParentheses_includesParenthesesAndValidResults() {
        val generator = ExpressionGenerator(Random(77))
        repeat(100) {
            val problem = generator.generate(ExpressionDifficulty.WITH_PARENTHESES)
            assertTrue(problem.root.allIntermediatesNonNegative())
            assertTrue(problem.answer >= 0)
            assertTrue("Expected parentheses in ${problem.displayText}", problem.displayText.contains("("))
            assertTrue(problem.hintText.isNotBlank())
        }
    }

    @Test
    fun generateSet_returnsTenProblems() {
        val generator = ExpressionGenerator(Random(11))
        val set = generator.generateSet(ExpressionDifficulty.WITH_PARENTHESES, count = 10)
        assertEquals(10, set.size)
    }
}
