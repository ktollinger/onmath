package cz.kt.onmath.ui.practice

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.kt.onmath.R
import cz.kt.onmath.ui.components.FeedbackBanner
import cz.kt.onmath.ui.components.FeedbackState
import cz.kt.onmath.ui.components.MathKeypad
import cz.kt.onmath.ui.components.PracticeTopBar
import cz.kt.onmath.ui.components.SessionSummaryView
import cz.kt.onmath.ui.components.UiText
import cz.kt.onmath.ui.components.asString

data class StandardPracticeUiState(
    val title: UiText = UiText.Plain(""),
    val problemDisplay: String = "",
    val inputText: String = "",
    val currentIndex: Int = 0,
    val totalCount: Int = 10,
    val correctFirstTry: Int = 0,
    val feedback: FeedbackState = FeedbackState.None,
    val isAdvancing: Boolean = false,
    val isFinished: Boolean = false,
)

@Composable
fun StandardPracticeScreen(
    state: StandardPracticeUiState,
    onDigit: (Int) -> Unit,
    onBackspace: () -> Unit,
    onSubmit: () -> Unit,
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
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = state.problemDisplay,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 44.sp,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .height(68.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = state.inputText.ifEmpty { "?" },
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.inputText.isEmpty()) {
                                MaterialTheme.colorScheme.outlineVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
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
                        Text(stringResource(R.string.action_hint))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                FeedbackBanner(state = state.feedback)
            }

            MathKeypad(
                onDigit = onDigit,
                onBackspace = onBackspace,
                onSubmit = onSubmit,
                submitEnabled = state.inputText.isNotEmpty() && !state.isAdvancing,
            )
        }
    }
}
