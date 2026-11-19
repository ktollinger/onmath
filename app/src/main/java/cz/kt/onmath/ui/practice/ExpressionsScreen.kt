package cz.kt.onmath.ui.practice

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.kt.onmath.R
import cz.kt.onmath.domain.expression.BinaryNode
import cz.kt.onmath.domain.expression.BinaryOperator
import cz.kt.onmath.domain.expression.ExpressionNode
import cz.kt.onmath.domain.expression.NumberNode
import cz.kt.onmath.ui.components.FeedbackBanner
import cz.kt.onmath.ui.components.MathKeypad
import cz.kt.onmath.ui.components.PracticeTopBar
import cz.kt.onmath.ui.components.SessionSummaryView
import cz.kt.onmath.ui.components.asString

@Composable
fun ExpressionsScreen(
    state: ExpressionsUiState,
    onSelectOperator: (String) -> Unit,
    onSelectSubstituted: (String) -> Unit,
    onFocusFinalAnswer: () -> Unit,
    onCancelIntermediate: () -> Unit,
    onUndoIntermediate: () -> Unit,
    onDigit: (Int) -> Unit,
    onBackspace: () -> Unit,
    onSubmit: () -> Unit,
    onNextProblem: () -> Unit,
    onHint: () -> Unit,
    onRestart: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val resolvedTitle = state.title.asString()

    Scaffold(
        modifier = modifier,
        topBar = {
            if (!state.isFinished) {
                PracticeTopBar(
                    title = resolvedTitle,
                    currentIndex = state.currentIndex,
                    totalCount = state.totalCount,
                    onBackClick = onBackClick,
                )
            }
        },
    ) { padding ->
        if (state.isFinished) {
            SessionSummaryView(
                topicTitle = resolvedTitle,
                correctFirstTry = state.correctFirstTry,
                totalProblems = state.totalCount,
                onRestart = onRestart,
                onBackHome = onBackClick,
                modifier = Modifier.padding(padding),
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier.height(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.intermediates.isEmpty() && !state.isEditingIntermediate && !state.isSolved) {
                        Text(
                            text = stringResource(R.string.expr_tip_tap_operator),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val rootNode = state.root
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(132.dp)
                        .horizontalScroll(rememberScrollState()),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    if (rootNode != null) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 4.dp),
                        ) {
                            ExpressionTreeNodeView(
                                node = rootNode,
                                nodeId = "root",
                                wrapWithParens = false,
                                intermediates = state.intermediates,
                                selectedNodeId = state.selectedNodeId,
                                enabled = !state.isSolved,
                                onSelectOperator = onSelectOperator,
                                onSelectSubstituted = onSelectSubstituted,
                            )

                            Box(
                                modifier = Modifier
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(enabled = state.isEditingIntermediate && !state.isSolved) {
                                        onFocusFinalAnswer()
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = if (state.isSolved) "= ${state.inputText}" else "= ?",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.isSolved) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier.height(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (state.isEditingIntermediate) {
                            stringResource(R.string.expr_label_intermediate, state.selectedSubExpression)
                        } else {
                            stringResource(R.string.expr_label_final)
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (state.isEditingIntermediate) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        width = 2.dp,
                        color = if (state.isEditingIntermediate) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                    ),
                    color = if (state.isEditingIntermediate) {
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .height(64.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val display = state.activeInputText
                        Text(
                            text = display.ifEmpty { "?" },
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (display.isEmpty()) {
                                MaterialTheme.colorScheme.outlineVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                    }
                }

                Box(
                    modifier = Modifier.height(40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.isEditingIntermediate && !state.isSolved) {
                        TextButton(onClick = onCancelIntermediate) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 4.dp),
                            )
                            Text(stringResource(R.string.expr_back_to_final))
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (state.isSolved) {
                        Button(onClick = onNextProblem) {
                            Text(stringResource(R.string.action_next_problem))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.padding(start = 6.dp),
                            )
                        }
                    } else {
                        OutlinedButton(onClick = onHint) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 6.dp),
                            )
                            Text(stringResource(R.string.action_hint))
                        }

                        if (state.canUndoIntermediate) {
                            OutlinedButton(onClick = onUndoIntermediate) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Undo,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 6.dp),
                                )
                                Text(stringResource(R.string.action_undo_step))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 72.dp),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    FeedbackBanner(state = state.feedback)
                }
            }

            MathKeypad(
                onDigit = onDigit,
                onBackspace = onBackspace,
                onSubmit = onSubmit,
                submitEnabled = state.activeInputText.isNotEmpty() && !state.isSolved,
            )
        }
    }
}

@Composable
private fun ExpressionTreeNodeView(
    node: ExpressionNode,
    nodeId: String,
    wrapWithParens: Boolean,
    intermediates: Map<String, Int>,
    selectedNodeId: String?,
    enabled: Boolean,
    onSelectOperator: (String) -> Unit,
    onSelectSubstituted: (String) -> Unit,
) {
    when (node) {
        is NumberNode -> {
            Box(
                modifier = Modifier.height(42.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = node.value.toString(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        is BinaryNode -> {
            val leftId = "$nodeId.L"
            val rightId = "$nodeId.R"
            val intermediateValue = intermediates[nodeId]
            val isSelected = selectedNodeId == nodeId
            val isReady = isNodeResolved(node.left, leftId, intermediates) &&
                isNodeResolved(node.right, rightId, intermediates)
            val showParens = node.explicitParentheses || wrapWithParens

            Column(
                modifier = Modifier.width(IntrinsicSize.Max),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = if (isSelected) {
                        Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                            .padding(horizontal = 4.dp)
                    } else {
                        Modifier
                    },
                ) {
                    if (showParens) {
                        Box(
                            modifier = Modifier.height(42.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "(",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    ExpressionTreeNodeView(
                        node = node.left,
                        nodeId = leftId,
                        wrapWithParens = needsImplicitParens(node, node.left, isRight = false),
                        intermediates = intermediates,
                        selectedNodeId = selectedNodeId,
                        enabled = enabled,
                        onSelectOperator = onSelectOperator,
                        onSelectSubstituted = onSelectSubstituted,
                    )

                    OperatorTokenView(
                        symbol = node.operator.symbol,
                        isReady = isReady,
                        isSelected = isSelected,
                        isSolved = intermediateValue != null,
                        enabled = enabled,
                        onClick = {
                            if (intermediateValue != null) {
                                onSelectSubstituted(nodeId)
                            } else {
                                onSelectOperator(nodeId)
                            }
                        },
                    )

                    ExpressionTreeNodeView(
                        node = node.right,
                        nodeId = rightId,
                        wrapWithParens = needsImplicitParens(node, node.right, isRight = true),
                        intermediates = intermediates,
                        selectedNodeId = selectedNodeId,
                        enabled = enabled,
                        onSelectOperator = onSelectOperator,
                        onSelectSubstituted = onSelectSubstituted,
                    )

                    if (showParens) {
                        Box(
                            modifier = Modifier.height(42.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = ")",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                if (intermediateValue != null) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp)
                            .height(2.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(1.dp),
                            ),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(enabled = enabled) {
                                onSelectSubstituted(nodeId)
                            },
                    ) {
                        Text(
                            text = intermediateValue.toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OperatorTokenView(
    symbol: String,
    isReady: Boolean,
    isSelected: Boolean,
    isSolved: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    when {
        isSelected -> {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .defaultMinSize(minWidth = 38.dp, minHeight = 42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(enabled = enabled, onClick = onClick),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Text(
                        text = symbol,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }

        isReady && !isSolved -> {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .defaultMinSize(minWidth = 38.dp, minHeight = 42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(enabled = enabled, onClick = onClick),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Text(
                        text = symbol,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        else -> {
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 26.dp, minHeight = 42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = enabled, onClick = onClick),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = symbol,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSolved) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.padding(horizontal = 2.dp),
                )
            }
        }
    }
}

private fun isNodeResolved(
    node: ExpressionNode,
    nodeId: String,
    intermediates: Map<String, Int>,
): Boolean = node is NumberNode || nodeId in intermediates

private fun needsImplicitParens(
    parent: BinaryNode,
    child: ExpressionNode,
    isRight: Boolean,
): Boolean {
    if (child !is BinaryNode || child.explicitParentheses) return false
    return when {
        child.operator.precedence < parent.operator.precedence -> true
        isRight && child.operator.precedence == parent.operator.precedence &&
            (parent.operator == BinaryOperator.SUBTRACT || parent.operator == BinaryOperator.DIVIDE) -> true
        else -> false
    }
}
