package cz.kt.onmath.domain.expression

enum class BinaryOperator(val symbol: String, val precedence: Int) {
    ADD("+", 1),
    SUBTRACT("−", 1),
    MULTIPLY("×", 2),
    DIVIDE("÷", 2),
    ;

    fun apply(left: Int, right: Int): Int = when (this) {
        ADD -> left + right
        SUBTRACT -> left - right
        MULTIPLY -> left * right
        DIVIDE -> {
            require(right != 0) { "Division by zero." }
            require(left % right == 0) { "Division must be exact: $left ÷ $right." }
            left / right
        }
    }
}

sealed interface ExpressionNode {
    fun evaluate(): Int
    fun allIntermediatesNonNegative(): Boolean
    fun format(): String
}

data class NumberNode(val value: Int) : ExpressionNode {
    override fun evaluate(): Int = value
    override fun allIntermediatesNonNegative(): Boolean = value >= 0
    override fun format(): String = value.toString()
}

data class BinaryNode(
    val left: ExpressionNode,
    val operator: BinaryOperator,
    val right: ExpressionNode,
    val explicitParentheses: Boolean = false,
) : ExpressionNode {

    override fun evaluate(): Int = operator.apply(left.evaluate(), right.evaluate())

    override fun allIntermediatesNonNegative(): Boolean {
        if (!left.allIntermediatesNonNegative() || !right.allIntermediatesNonNegative()) {
            return false
        }
        return runCatching { evaluate() >= 0 }.getOrDefault(false)
    }

    override fun format(): String {
        val leftStr = formatChild(left, isRight = false)
        val rightStr = formatChild(right, isRight = true)
        val body = "$leftStr ${operator.symbol} $rightStr"
        return if (explicitParentheses) "($body)" else body
    }

    private fun formatChild(child: ExpressionNode, isRight: Boolean): String {
        if (child !is BinaryNode) return child.format()
        if (child.explicitParentheses) return child.format()

        val needsParens = when {
            child.operator.precedence < operator.precedence -> true
            isRight && child.operator.precedence == operator.precedence &&
                (operator == BinaryOperator.SUBTRACT || operator == BinaryOperator.DIVIDE) -> true
            else -> false
        }
        val formatted = child.format()
        return if (needsParens) "($formatted)" else formatted
    }
}

/**
 * Finds the next highest-priority operation to guide the child when asking for a hint.
 */
fun ExpressionNode.firstStepHint(): String {
    val firstOp = findFirstOperation(this, insideParens = false)
        ?: return "Spočítej výsledek."

    val (node, inParens) = firstOp
    val leftVal = node.left.evaluate()
    val rightVal = node.right.evaluate()
    val stepResult = node.evaluate()
    val expr = "$leftVal ${node.operator.symbol} $rightVal = $stepResult"

    return when {
        inParens -> "Nejdřív spočítej závorku: $expr"
        node.operator.precedence == 2 -> "Násobení a dělení mají přednost: $expr"
        else -> "Postupuj zleva doprava: $expr"
    }
}

private fun findFirstOperation(
    node: ExpressionNode,
    insideParens: Boolean,
): Pair<BinaryNode, Boolean>? {
    if (node !is BinaryNode) return null

    val currentInParens = insideParens || node.explicitParentheses || hasParenthesizedChild(node)
    if (node.left is NumberNode && node.right is NumberNode) {
        return node to (insideParens || node.explicitParentheses)
    }

    val leftCandidate = findFirstOperation(node.left, currentInParens && node.left is BinaryNode && (node.left.explicitParentheses || node.left.operator.precedence < node.operator.precedence))
    val rightCandidate = findFirstOperation(node.right, currentInParens && node.right is BinaryNode && (node.right.explicitParentheses || node.right.operator.precedence < node.operator.precedence))

    // Prefer explicit parentheses first, then higher precedence, then left-to-right.
    if (node.left is BinaryNode && (node.left.explicitParentheses || node.left.operator.precedence < node.operator.precedence)) {
        return findFirstOperation(node.left, insideParens = true)
    }
    if (node.right is BinaryNode && (node.right.explicitParentheses || node.right.operator.precedence < node.operator.precedence)) {
        return findFirstOperation(node.right, insideParens = true)
    }

    if (leftCandidate != null && rightCandidate != null) {
        val leftPrec = leftCandidate.first.operator.precedence
        val rightPrec = rightCandidate.first.operator.precedence
        return if (rightPrec > leftPrec) rightCandidate else leftCandidate
    }
    return leftCandidate ?: rightCandidate
}

private fun hasParenthesizedChild(node: BinaryNode): Boolean =
    (node.left is BinaryNode && node.left.explicitParentheses) ||
        (node.right is BinaryNode && node.right.explicitParentheses)
