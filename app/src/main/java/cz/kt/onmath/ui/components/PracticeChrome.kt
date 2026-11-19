package cz.kt.onmath.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.kt.onmath.ui.theme.CorrectContainerGreen
import cz.kt.onmath.ui.theme.CorrectGreen
import cz.kt.onmath.ui.theme.ErrorContainerRed
import cz.kt.onmath.ui.theme.ErrorRed
import cz.kt.onmath.ui.theme.HintAmber
import cz.kt.onmath.ui.theme.HintContainerAmber

sealed interface FeedbackState {
    data object None : FeedbackState
    data class Correct(val message: String = "Správně!") : FeedbackState
    data class Wrong(val message: String = "Zkus to znovu nebo použij nápovědu.") : FeedbackState
    data class Hint(val message: String) : FeedbackState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeTopBar(
    title: String,
    currentIndex: Int,
    totalCount: Int,
    onBackClick: () -> Unit,
) {
    Column {
        TopAppBar(
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = title, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "${(currentIndex + 1).coerceAtMost(totalCount)} / $totalCount",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 16.dp),
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Zpět",
                    )
                }
            },
        )
        LinearProgressIndicator(
            progress = { (currentIndex.toFloat() / totalCount.coerceAtLeast(1)).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun FeedbackBanner(
    state: FeedbackState,
    modifier: Modifier = Modifier,
) {
    if (state is FeedbackState.None) return

    val (containerColor, contentColor, text) = when (state) {
        is FeedbackState.Correct -> Triple(CorrectContainerGreen, CorrectGreen, state.message)
        is FeedbackState.Wrong -> Triple(ErrorContainerRed, ErrorRed, state.message)
        is FeedbackState.Hint -> Triple(HintContainerAmber, HintAmber, state.message)
        FeedbackState.None -> return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (state is FeedbackState.Hint) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp),
                )
            } else if (state is FeedbackState.Correct) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
fun SessionSummaryView(
    topicTitle: String,
    correctFirstTry: Int,
    totalProblems: Int,
    onRestart: () -> Unit,
    onBackHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accuracy = if (totalProblems == 0) 0 else (correctFirstTry * 100) / totalProblems
    val stars = when {
        accuracy >= 90 -> 3
        accuracy >= 70 -> 2
        accuracy >= 40 -> 1
        else -> 0
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { index ->
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (index < stars) HintAmber else MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(48.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Lekce dokončena!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = topicTitle,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "$correctFirstTry / $totalProblems",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "správně na první pokus ($accuracy %)",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRestart,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text("Procvičit další sadu", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onBackHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text("Zpět na výběr oblasti", fontSize = 16.sp)
        }
    }
}
