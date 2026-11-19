package cz.kt.onmath.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExposureNeg1
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cz.kt.onmath.data.local.TopicStats
import cz.kt.onmath.domain.model.MathTopic
import cz.kt.onmath.domain.model.SubtractionMethod
import cz.kt.onmath.ui.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenTopic: (MathTopic) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = state.settings

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("OnMath", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Nastavení",
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Vyber si, co chceš procvičovat",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            val tablesDesc = if (settings.multiplicationTables.size == 10) {
                "Řady 1–10"
            } else {
                "Řady: ${settings.multiplicationTables.sorted().joinToString(", ")}"
            } + if (settings.multiplicationIncludeDivision) " + dělení" else ""

            TopicCard(
                title = "Násobilka",
                subtitle = tablesDesc,
                icon = Icons.Default.Close,
                stats = state.statsByTopic[MathTopic.MULTIPLICATION],
                onClick = { onOpenTopic(MathTopic.MULTIPLICATION) },
            )

            TopicCard(
                title = "Výrazy a závorky",
                subtitle = if (settings.expressionWithParentheses) {
                    "Priorita operací se závorkami"
                } else {
                    "Priorita operací bez závorek"
                },
                icon = Icons.Default.Functions,
                stats = state.statsByTopic[MathTopic.EXPRESSIONS],
                onClick = { onOpenTopic(MathTopic.EXPRESSIONS) },
            )

            val methodLabel = when (settings.subtractionMethod) {
                SubtractionMethod.BORROW_TOP -> "Montessori (půjčení nahoře)"
                SubtractionMethod.CARRY_BOTTOM -> "Školní (jednička dolů)"
            }
            TopicCard(
                title = "Odčítání pod sebou",
                subtitle = "${settings.subtractionMinDigits}–${settings.subtractionMaxDigits} cifer • $methodLabel",
                icon = Icons.Default.Calculate,
                stats = state.statsByTopic[MathTopic.SUBTRACTION],
                onClick = { onOpenTopic(MathTopic.SUBTRACTION) },
            )

            if (state.recentMistakes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Poslední chybičky k připomenutí",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        state.recentMistakes.forEach { mistake ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = mistake.displayText,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    text = "správně ${mistake.expectedAnswer}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TopicCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    stats: TopicStats?,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(52.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(12.dp),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (stats != null && stats.sessionCount > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Lekcí: ${stats.sessionCount} • Úspěšnost: ${stats.accuracyPercent} %",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
