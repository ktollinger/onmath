package cz.kt.onmath.domain.expression

import kotlin.random.Random

enum class ExpressionDifficulty {
    WITHOUT_PARENTHESES,
    WITH_PARENTHESES,
}

data class ExpressionProblem(
    val root: ExpressionNode,
    val displayText: String,
    val answer: Int,
    val hintText: String,
) {
    val key: String
        get() = displayText.removeSuffix(" = ?")
}

class ExpressionGenerator(
    private val random: Random = Random.Default,
) {
    fun generate(
        difficulty: ExpressionDifficulty = ExpressionDifficulty.WITH_PARENTHESES,
        allowLargeIntermediate: Boolean = false,
    ): ExpressionProblem {
        repeat(200) {
            val candidate = when (difficulty) {
                ExpressionDifficulty.WITHOUT_PARENTHESES -> buildWithoutParentheses()
                ExpressionDifficulty.WITH_PARENTHESES -> buildWithParentheses(allowLargeIntermediate)
            }
            if (candidate != null && isValid(candidate, allowLargeIntermediate)) {
                val formatted = candidate.format()
                val answer = candidate.evaluate()
                return ExpressionProblem(
                    root = candidate,
                    displayText = "$formatted = ?",
                    answer = answer,
                    hintText = candidate.firstStepHint(),
                )
            }
        }
        // Deterministic safe fallback
        val fallback = BinaryNode(
            left = NumberNode(2),
            operator = BinaryOperator.ADD,
            right = BinaryNode(NumberNode(3), BinaryOperator.MULTIPLY, NumberNode(4)),
        )
        return ExpressionProblem(
            root = fallback,
            displayText = "${fallback.format()} = ?",
            answer = fallback.evaluate(),
            hintText = fallback.firstStepHint(),
        )
    }

    fun generateSet(
        difficulty: ExpressionDifficulty = ExpressionDifficulty.WITH_PARENTHESES,
        count: Int = 10,
        allowLargeIntermediate: Boolean = false,
    ): List<ExpressionProblem> {
        require(count > 0) { "Count must be positive." }
        val result = mutableListOf<ExpressionProblem>()
        var attempts = 0
        while (result.size < count && attempts < count * 10) {
            attempts++
            val candidate = generate(difficulty, allowLargeIntermediate)
            if (result.none { it.key == candidate.key }) {
                result.add(candidate)
            }
        }
        while (result.size < count) {
            result.add(generate(difficulty, allowLargeIntermediate))
        }
        return result
    }

    private fun buildWithoutParentheses(): ExpressionNode? {
        // Mix high-precedence (×, ÷) with low-precedence (+, -) so priority rules actually matter.
        val highNode = randomHighPrecedencePair()
        val lowOp = if (random.nextBoolean()) BinaryOperator.ADD else BinaryOperator.SUBTRACT
        val other = NumberNode(random.nextInt(2, 31))

        val twoOp = if (random.nextBoolean()) {
            BinaryNode(other, lowOp, highNode)
        } else {
            BinaryNode(highNode, lowOp, other)
        }

        // 50% chance to add a third operation
        if (random.nextBoolean()) return twoOp

        val extraOp = if (random.nextBoolean()) BinaryOperator.ADD else BinaryOperator.SUBTRACT
        val extraVal = NumberNode(random.nextInt(2, 21))
        return if (extraOp == BinaryOperator.SUBTRACT || random.nextBoolean()) {
            BinaryNode(twoOp, extraOp, extraVal)
        } else {
            BinaryNode(extraVal, extraOp, twoOp)
        }
    }

    private fun buildWithParentheses(allowLargeIntermediate: Boolean): ExpressionNode? {
        val innerOp = if (random.nextBoolean()) BinaryOperator.ADD else BinaryOperator.SUBTRACT
        val leftVal = random.nextInt(2, 21)
        val rightVal = random.nextInt(2, 21)
        val inner = BinaryNode(NumberNode(leftVal), innerOp, NumberNode(rightVal), explicitParentheses = true)
        val innerResult = runCatching { inner.evaluate() }.getOrNull() ?: return null
        if (innerResult <= 0) return null

        val outerHighOp = if (random.nextBoolean()) BinaryOperator.MULTIPLY else BinaryOperator.DIVIDE
        return when (outerHighOp) {
            BinaryOperator.MULTIPLY -> {
                if (!allowLargeIntermediate && innerResult !in 2..10) return null
                val factor = NumberNode(random.nextInt(2, 11))
                val core = if (random.nextBoolean()) {
                    BinaryNode(inner, BinaryOperator.MULTIPLY, factor)
                } else {
                    BinaryNode(factor, BinaryOperator.MULTIPLY, inner)
                }
                maybeAppendLowOp(core)
            }
            BinaryOperator.DIVIDE -> {
                val divisors = (2..10).filter { divisor ->
                    innerResult % divisor == 0 && (allowLargeIntermediate || innerResult / divisor in 2..10)
                }
                if (divisors.isEmpty()) return null
                val divisor = NumberNode(divisors.random(random))
                val core = BinaryNode(inner, BinaryOperator.DIVIDE, divisor)
                maybeAppendLowOp(core)
            }
            else -> null
        }
    }

    private fun maybeAppendLowOp(core: ExpressionNode): ExpressionNode {
        if (random.nextBoolean()) return core
        val op = if (random.nextBoolean()) BinaryOperator.ADD else BinaryOperator.SUBTRACT
        val extra = NumberNode(random.nextInt(1, 21))
        return if (random.nextBoolean()) {
            BinaryNode(core, op, extra)
        } else {
            BinaryNode(extra, op, core)
        }
    }

    private fun randomHighPrecedencePair(): BinaryNode {
        return if (random.nextBoolean()) {
            val a = random.nextInt(2, 11)
            val b = random.nextInt(2, 11)
            BinaryNode(NumberNode(a), BinaryOperator.MULTIPLY, NumberNode(b))
        } else {
            val divisor = random.nextInt(2, 11)
            val quotient = random.nextInt(2, 11)
            BinaryNode(NumberNode(divisor * quotient), BinaryOperator.DIVIDE, NumberNode(divisor))
        }
    }

    private fun isValid(node: ExpressionNode, allowLargeIntermediate: Boolean): Boolean {
        val result = runCatching { node.evaluate() }.getOrNull() ?: return false
        if (!node.allIntermediatesNonNegative() || result !in 0..250) return false
        return allowLargeIntermediate || node.allHighPrecedenceWithinTable()
    }

    private fun ExpressionNode.allHighPrecedenceWithinTable(): Boolean = when (this) {
        is NumberNode -> true
        is BinaryNode -> {
            val childrenOk = left.allHighPrecedenceWithinTable() && right.allHighPrecedenceWithinTable()
            val selfOk = when (operator) {
                BinaryOperator.MULTIPLY -> left.evaluate() <= 10 && right.evaluate() <= 10
                BinaryOperator.DIVIDE -> right.evaluate() <= 10 && evaluate() <= 10
                BinaryOperator.ADD, BinaryOperator.SUBTRACT -> true
            }
            childrenOk && selfOk
        }
    }
}
