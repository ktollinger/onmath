package cz.kt.onmath.ui.practice.subtraction

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cz.kt.onmath.domain.model.SubtractionMethod
import cz.kt.onmath.domain.subtraction.SubtractionColumn
import cz.kt.onmath.domain.subtraction.SubtractionProblem
import cz.kt.onmath.ui.AppViewModelProvider
import cz.kt.onmath.ui.components.FeedbackBanner
import cz.kt.onmath.ui.components.MathKeypad
import cz.kt.onmath.ui.components.PracticeTopBar
import cz.kt.onmath.ui.components.SessionSummaryView
import cz.kt.onmath.ui.theme.ErrorContainerRed
import cz.kt.onmath.ui.theme.ErrorRed
import cz.kt.onmath.ui.theme.HintAmber
import cz.kt.onmath.ui.theme.HintContainerAmber

@Composable
fun SubtractionScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SubtractionViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            if (!state.isFinished) {
                PracticeTopBar(
                    title = "Odčítání pod sebou",
                    currentIndex = state.currentIndex,
                    totalCount = state.totalCount,
                    onBackClick = onBackClick,
                )
            }
        },
    ) { padding ->
        if (state.isFinished) {
            SessionSummaryView(
                topicTitle = "Odčítání pod sebou",
                correctFirstTry = state.correctFirstTry,
                totalProblems = state.totalCount,
                onRestart = viewModel::startNewSession,
                onBackHome = onBackClick,
                modifier = Modifier.padding(padding),
            )
            return@Scaffold
        }

        val problem = state.problem ?: return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                ColumnSubtractionBoard(
                    problem = problem,
                    method = state.method,
                    resultDigits = state.resultDigits,
                    activePlaceIndex = state.activePlaceIndex,
                    markedBorrows = state.markedBorrows,
                    wrongPlaces = state.wrongPlaces,
                    onSelectPlace = viewModel::onSelectPlace,
                    onToggleBorrow = viewModel::onToggleBorrowMarker,
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (state.method == SubtractionMethod.BORROW_TOP) {
                        "Tip: Ťukni na horní cifru nebo nad ni pro označení půjčení"
                    } else {
                        "Tip: Ťukni na dolní cifru pro označení přenesené jedničky"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = viewModel::onHint,
                    enabled = !state.isAdvancing,
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 6.dp),
                    )
                    Text("Nápověda k řádu")
                }

                Spacer(modifier = Modifier.height(10.dp))

                FeedbackBanner(state = state.feedback)
            }

            MathKeypad(
                onDigit = viewModel::onDigit,
                onBackspace = viewModel::onBackspace,
                onSubmit = viewModel::onSubmit,
                submitEnabled = state.resultDigits.isNotEmpty() && !state.isAdvancing,
            )
        }
    }
}

@Composable
private fun ColumnSubtractionBoard(
    problem: SubtractionProblem,
    method: SubtractionMethod,
    resultDigits: Map<Int, Int>,
    activePlaceIndex: Int,
    markedBorrows: Set<Int>,
    wrongPlaces: Set<Int>,
    onSelectPlace: (Int) -> Unit,
    onToggleBorrow: (Int) -> Unit,
) {
    // Display columns left-to-right (highest placeIndex down to 0)
    val columnsLeftToRight = problem.columnsRightToLeft.reversed()
    val cellWidth: Dp = if (problem.digitCount >= 6) 44.dp else 50.dp
    val subtrahendDigitsCount = problem.subtrahend.toString().length

    Column(horizontalAlignment = Alignment.End) {
        // 1. Montessori BORROW_TOP helper row above minuend
        if (method == SubtractionMethod.BORROW_TOP) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Spacer(modifier = Modifier.width(28.dp))
                columnsLeftToRight.forEach { col ->
                    BorrowTopCell(
                        column = col,
                        isMarked = col.placeIndex in markedBorrows,
                        cellWidth = cellWidth,
                        onClick = { onToggleBorrow(col.placeIndex) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
        }

        // 2. Minuend row (horní číslo - v režimu BORROW_TOP klikatelné pro označení půjčení)
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width(28.dp))
            columnsLeftToRight.forEach { col ->
                val canLend = method == SubtractionMethod.BORROW_TOP && col.placeIndex > 0
                val crossedOut = canLend && col.placeIndex in markedBorrows
                Box(
                    modifier = Modifier
                        .width(cellWidth)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = canLend) { onToggleBorrow(col.placeIndex) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = col.minuendDigit.toString(),
                        fontSize = 32.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (crossedOut) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (crossedOut) {
                            MaterialTheme.colorScheme.outline
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                }
            }
        }

        // 3. Subtrahend row (dolní číslo + znaménko − + volitelná jednička v české metodě)
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.width(28.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "−",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            columnsLeftToRight.forEach { col ->
                val showDigit = col.placeIndex < subtrahendDigitsCount
                val showCarrySlot = method == SubtractionMethod.CARRY_BOTTOM && col.placeIndex > 0
                val isMarked = col.placeIndex in markedBorrows

                Box(
                    modifier = Modifier
                        .width(cellWidth)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = showCarrySlot) { onToggleBorrow(col.placeIndex) },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = if (showDigit) col.subtrahendDigit.toString() else " ",
                            fontSize = 32.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                        )
                        if (showCarrySlot) {
                            Text(
                                text = if (isMarked) "1" else "·",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMarked) HintAmber else MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.padding(start = 2.dp, bottom = 4.dp),
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        HorizontalDivider(
            modifier = Modifier.width(cellWidth * problem.digitCount + 4.dp * (problem.digitCount - 1) + 28.dp),
            thickness = 2.5.dp,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Result digit boxes (tappable, filled right-to-left)
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width(28.dp))
            columnsLeftToRight.forEach { col ->
                val isActive = col.placeIndex == activePlaceIndex
                val isWrong = col.placeIndex in wrongPlaces
                val entered = resultDigits[col.placeIndex]

                val borderColor = when {
                    isWrong -> ErrorRed
                    isActive -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outlineVariant
                }
                val bgColor = when {
                    isWrong -> ErrorContainerRed
                    isActive -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    else -> MaterialTheme.colorScheme.surfaceContainerLow
                }

                Surface(
                    onClick = { onSelectPlace(col.placeIndex) },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(if (isActive || isWrong) 2.dp else 1.dp, borderColor),
                    color = bgColor,
                    modifier = Modifier.size(width = cellWidth, height = 54.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = entered?.toString() ?: "",
                            fontSize = 28.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isWrong) ErrorRed else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BorrowTopCell(
    column: SubtractionColumn,
    isMarked: Boolean,
    cellWidth: Dp,
    onClick: () -> Unit,
) {
    val canLend = column.placeIndex > 0
    val decremented = if (column.minuendDigit == 0) 9 else column.minuendDigit - 1

    Surface(
        onClick = onClick,
        enabled = canLend,
        shape = RoundedCornerShape(8.dp),
        border = if (canLend) {
            BorderStroke(
                width = 1.dp,
                color = if (isMarked) HintAmber else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
            )
        } else {
            null
        },
        color = when {
            !canLend -> MaterialTheme.colorScheme.surface
            isMarked -> HintContainerAmber
            else -> MaterialTheme.colorScheme.surfaceContainerLow
        },
        modifier = Modifier.size(width = cellWidth, height = 30.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (canLend) {
                Text(
                    text = if (isMarked) decremented.toString() else "−1",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isMarked) HintAmber else MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
    }
}
