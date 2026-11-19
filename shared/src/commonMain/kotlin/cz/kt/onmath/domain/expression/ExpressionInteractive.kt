package cz.kt.onmath.domain.expression

sealed interface ExpressionToken {
    data class Text(val text: String) : ExpressionToken

    data class SubstitutedNumber(
        val nodeId: String,
        val value: Int,
        val originalExpression: String,
    ) : ExpressionToken

    data class Operator(
        val nodeId: String,
        val symbol: String,
        val isReady: Boolean,
        val isSelected: Boolean,
        val subExpression: String,
        val expectedValue: Int,
    ) : ExpressionToken
}

fun ExpressionNode.tokenize(
    intermediates: Map<String, Int> = emptyMap(),
    selectedNodeId: String? = null,
): List<ExpressionToken> {
    val tokens = mutableListOf<ExpressionToken>()
    appendTokens(
        nodeId = "root",
        intermediates = intermediates,
        selectedNodeId = selectedNodeId,
        wrapWithParens = false,
        out = tokens,
    )
    return tokens
}

fun ExpressionNode.reduceWith(
    intermediates: Map<String, Int>,
    nodeId: String = "root",
): ExpressionNode = when (this) {
    is NumberNode -> this
    is BinaryNode -> {
        val substituted = intermediates[nodeId]
        if (substituted != null) {
            NumberNode(substituted)
        } else {
            copy(
                left = left.reduceWith(intermediates, "$nodeId.L"),
                right = right.reduceWith(intermediates, "$nodeId.R"),
            )
        }
    }
}

fun ExpressionNode.findBinaryNode(
    targetId: String,
    currentId: String = "root",
): BinaryNode? {
    if (this !is BinaryNode) return null
    if (currentId == targetId) return this
    return left.findBinaryNode(targetId, "$currentId.L")
        ?: right.findBinaryNode(targetId, "$currentId.R")
}

fun removeIntermediateAndAncestors(
    nodeId: String,
    intermediates: Map<String, Int>,
    order: List<String>,
): Pair<Map<String, Int>, List<String>> {
    val toRemove = intermediates.keys.filter { existing ->
        nodeId == existing || nodeId.startsWith("$existing.")
    }.toSet()
    return (intermediates - toRemove) to order.filterNot { it in toRemove }
}

private fun ExpressionNode.appendTokens(
    nodeId: String,
    intermediates: Map<String, Int>,
    selectedNodeId: String?,
    wrapWithParens: Boolean,
    out: MutableList<ExpressionToken>,
) {
    when (this) {
        is NumberNode -> out.add(ExpressionToken.Text(value.toString()))
        is BinaryNode -> {
            val substituted = intermediates[nodeId]
            if (substituted != null) {
                out.add(
                    ExpressionToken.SubstitutedNumber(
                        nodeId = nodeId,
                        value = substituted,
                        originalExpression = format(),
                    ),
                )
                return
            }

            val showParens = explicitParentheses || wrapWithParens
            if (showParens) out.add(ExpressionToken.Text("("))

            val leftId = "$nodeId.L"
            val rightId = "$nodeId.R"
            val leftNeedsParens = needsImplicitParens(left, isRight = false) && leftId !in intermediates
            val rightNeedsParens = needsImplicitParens(right, isRight = true) && rightId !in intermediates

            left.appendTokens(
                nodeId = leftId,
                intermediates = intermediates,
                selectedNodeId = selectedNodeId,
                wrapWithParens = leftNeedsParens,
                out = out,
            )

            val isReady = left.isResolved(leftId, intermediates) && right.isResolved(rightId, intermediates)
            val leftDisplay = left.resolvedDisplay(leftId, intermediates)
            val rightDisplay = right.resolvedDisplay(rightId, intermediates)
            out.add(
                ExpressionToken.Operator(
                    nodeId = nodeId,
                    symbol = operator.symbol,
                    isReady = isReady,
                    isSelected = nodeId == selectedNodeId,
                    subExpression = "$leftDisplay ${operator.symbol} $rightDisplay",
                    expectedValue = evaluate(),
                ),
            )

            right.appendTokens(
                nodeId = rightId,
                intermediates = intermediates,
                selectedNodeId = selectedNodeId,
                wrapWithParens = rightNeedsParens,
                out = out,
            )

            if (showParens) out.add(ExpressionToken.Text(")"))
        }
    }
}

private fun BinaryNode.needsImplicitParens(child: ExpressionNode, isRight: Boolean): Boolean {
    if (child !is BinaryNode || child.explicitParentheses) return false
    return when {
        child.operator.precedence < operator.precedence -> true
        isRight && child.operator.precedence == operator.precedence &&
            (operator == BinaryOperator.SUBTRACT || operator == BinaryOperator.DIVIDE) -> true
        else -> false
    }
}

private fun ExpressionNode.isResolved(nodeId: String, intermediates: Map<String, Int>): Boolean =
    this is NumberNode || nodeId in intermediates

private fun ExpressionNode.resolvedDisplay(nodeId: String, intermediates: Map<String, Int>): String =
    when (this) {
        is NumberNode -> value.toString()
        is BinaryNode -> intermediates[nodeId]?.toString() ?: format()
    }
