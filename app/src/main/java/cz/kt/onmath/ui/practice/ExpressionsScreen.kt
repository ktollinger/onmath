package cz.kt.onmath.ui.practice

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.kt.onmath.domain.expression.ExpressionToken
import cz.kt.onmath.ui.components.FeedbackBanner
import cz.kt.onmath.ui.components.MathKeypad
import cz.kt.onmath.ui.components.PracticeTopBar
import cz.kt.onmath.ui.components.SessionSummaryView

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpressionsScreen(
    state: ExpressionsUiState,
    onSelectOperator: (String) -> Unit,
    onSelectSubstituted: (String) -> Unit,
    onCancelIntermediate: () -> Unit,
    onUndoIntermediate: () -> Unit,
    onDigit: (Int) -> Unit,
    onBackspace: () -> Unit,
    onSubmit: () -> Unit,
    onHint: () -> Unit,
    onRestart: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            if (!state.isFinished) {
                PracticeTopBar(
                    title = state.title,
                    currentIndex = state.currentIndex,
                    totalCount = state.totalCount,
                    onBackClick = onBackClick,
                )
            }
        },
    ) { padding ->
        if (state.isFinished) {
            SessionSummaryView(
                topicTitle = state.title,
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
                verticalArrangement = Arrangement.Center,
            ) {
                if (state.intermediates.isNotEmpty()) {
                    Text(
                        text = "Zadání: ${state.originalExpression}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    Text(
                        text = "Klepni na znaménko pro zápis mezivýsledku",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                ) {
                    state.tokens.forEach { token ->
                        when (token) {
                            is ExpressionToken.Text -> {
                                Box(
                                    modifier = Modifier.defaultMinSize(minHeight = 44.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = token.text,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }

                            is ExpressionToken.SubstitutedNumber -> {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable(enabled = !state.isAdvancing) {
                                            onSelectSubstituted(token.nodeId)
                                        },
                                ) {
                                    Text(
                                        text = token.value.toString(),
                                        fontSize = 30.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                                    )
                                }
                            }

                            is ExpressionToken.Operator -> {
                                OperatorTokenView(
                                    token = token,
                                    enabled = !state.isAdvancing,
                                    onClick = { onSelectOperator(token.nodeId) },
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.defaultMinSize(minHeight = 44.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "= ?",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (state.isEditingIntermediate) {
                    Text(
                        text = "Mezivýsledek: ${state.selectedSubExpression}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

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

                if (state.isEditingIntermediate) {
                    TextButton(onClick = onCancelIntermediate) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 4.dp),
                        )
                        Text("Zpět k celkovému výsledku")
                    }
                } else {
                    Spacer(modifier = Modifier.height(14.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                ) {
                    OutlinedButton(
                        onClick = onHint,
                        enabled = !state.isAdvancing,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 6.dp),
                        )
                        Text("Nápověda")
                    }

                    if (state.canUndoIntermediate) {
                        OutlinedButton(onClick = onUndoIntermediate) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 6.dp),
                            )
                            Text("Zpět krok")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                FeedbackBanner(state = state.feedback)
            }

            MathKeypad(
                onDigit = onDigit,
                onBackspace = onBackspace,
                onSubmit = onSubmit,
                submitEnabled = state.activeInputText.isNotEmpty() && !state.isAdvancing,
            )
        }
    }
}

@Composable
private fun OperatorTokenView(
    token: ExpressionToken.Operator,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    when {
        token.isSelected -> {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .defaultMinSize(minWidth = 40.dp, minHeight = 44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(enabled = enabled, onClick = onClick),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 10.dp)) {
                    Text(
                        text = token.symbol,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }

        token.isReady -> {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .defaultMinSize(minWidth = 40.dp, minHeight = 44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(enabled = enabled, onClick = onClick),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 10.dp)) {
                    Text(
                        text = token.symbol,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        else -> {
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 28.dp, minHeight = 44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = enabled, onClick = onClick),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = token.symbol,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
    }
}
