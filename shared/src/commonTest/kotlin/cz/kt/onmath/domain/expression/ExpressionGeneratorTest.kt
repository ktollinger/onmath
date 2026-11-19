package cz.kt.onmath.domain.expression

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
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
            assertFalse(problem.displayText.contains("("), "Unexpected parentheses in ${problem.displayText}")
        }
    }

    @Test
    fun generate_withParentheses_includesParenthesesAndValidResults() {
        val generator = ExpressionGenerator(Random(77))
        repeat(100) {
            val problem = generator.generate(ExpressionDifficulty.WITH_PARENTHESES)
            assertTrue(problem.root.allIntermediatesNonNegative())
            assertTrue(problem.answer >= 0)
            assertTrue(problem.displayText.contains("("), "Expected parentheses in ${problem.displayText}")
            assertTrue(problem.hintText.isNotBlank())
        }
    }

    @Test
    fun generateSet_returnsTenProblems() {
        val generator = ExpressionGenerator(Random(11))
        val set = generator.generateSet(ExpressionDifficulty.WITH_PARENTHESES, count = 10)
        assertEquals(10, set.size)
    }

    @Test
    fun generate_standardDifficulty_keepsMultiplicationsWithinTenByTen() {
        val generator = ExpressionGenerator(Random(42))
        repeat(100) {
            val problem = generator.generate(
                difficulty = ExpressionDifficulty.WITH_PARENTHESES,
                allowLargeIntermediate = false,
            )
            assertTrue(
                allMultiplicationsWithinTen(problem.root),
                "Expected <= 10x10 in ${problem.displayText}",
            )
        }
    }

    @Test
    fun tokenize_substitutesIntermediateAndUpdatesReadyOperators() {
        // (9 + 9) × 3
        val expr = BinaryNode(
            left = BinaryNode(NumberNode(9), BinaryOperator.ADD, NumberNode(9), explicitParentheses = true),
            operator = BinaryOperator.MULTIPLY,
            right = NumberNode(3),
        )

        val initialTokens = expr.tokenize()
        val initialOps = initialTokens.filterIsInstance<ExpressionToken.Operator>()
        assertEquals(2, initialOps.size)
        assertTrue(initialOps[0].isReady) // + inside (9 + 9)
        assertFalse(initialOps[1].isReady) // × waits for (9 + 9)

        val afterFirstStep = expr.tokenize(intermediates = mapOf("root.L" to 18))
        val opsAfter = afterFirstStep.filterIsInstance<ExpressionToken.Operator>()
        assertEquals(1, opsAfter.size)
        assertTrue(opsAfter[0].isReady) // × is now ready for 18 × 3
        assertEquals("18 × 3", opsAfter[0].subExpression)
        assertEquals(54, opsAfter[0].expectedValue)
        assertTrue(afterFirstStep.first() is ExpressionToken.SubstitutedNumber)
    }

    @Test
    fun tokenize_handlesComplexMultiStepExpression() {
        // (2 + 3) × 2 + 4 × 5
        val expr = BinaryNode(
            left = BinaryNode(
                left = BinaryNode(NumberNode(2), BinaryOperator.ADD, NumberNode(3), explicitParentheses = true),
                operator = BinaryOperator.MULTIPLY,
                right = NumberNode(2),
            ),
            operator = BinaryOperator.ADD,
            right = BinaryNode(NumberNode(4), BinaryOperator.MULTIPLY, NumberNode(5)),
        )

        val step1 = mapOf("root.L.L" to 5)
        assertEquals("5 × 2 + 4 × 5", expr.reduceWith(step1).format())

        val step2 = step1 + ("root.L" to 10) + ("root.R" to 20)
        assertEquals("10 + 20", expr.reduceWith(step2).format())

        val (cleared, order) = removeIntermediateAndAncestors(
            nodeId = "root.L.L",
            intermediates = step2,
            order = listOf("root.L.L", "root.L", "root.R"),
        )
        assertEquals(mapOf("root.R" to 20), cleared)
        assertEquals(listOf("root.R"), order)
    }

    private fun allMultiplicationsWithinTen(node: ExpressionNode): Boolean = when (node) {
        is NumberNode -> true
        is BinaryNode -> {
            val selfOk = when (node.operator) {
                BinaryOperator.MULTIPLY -> node.left.evaluate() <= 10 && node.right.evaluate() <= 10
                BinaryOperator.DIVIDE -> node.right.evaluate() <= 10 && node.evaluate() <= 10
                else -> true
            }
            selfOk && allMultiplicationsWithinTen(node.left) && allMultiplicationsWithinTen(node.right)
        }
    }
}
