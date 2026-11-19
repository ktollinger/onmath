package cz.kt.onmath.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cz.kt.onmath.domain.model.SubtractionMethod
import cz.kt.onmath.ui.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Nastavení", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zpět",
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SectionTitle("Odčítání pod sebou")

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Způsob značení přenosů",
                    style = MaterialTheme.typography.titleSmall,
                )
                MethodOptionRow(
                    selected = settings.subtractionMethod == SubtractionMethod.BORROW_TOP,
                    title = "Montessori / anglosaský způsob",
                    subtitle = "Půjčení z vyššího řádu (škrtání a snížení horní cifry)",
                    onClick = { viewModel.setSubtractionMethod(SubtractionMethod.BORROW_TOP) },
                )
                MethodOptionRow(
                    selected = settings.subtractionMethod == SubtractionMethod.CARRY_BOTTOM,
                    title = "Český školní způsob",
                    subtitle = "Přičtení 10 nahoře a přenesení jedničky dolů k menšiteli",
                    onClick = { viewModel.setSubtractionMethod(SubtractionMethod.CARRY_BOTTOM) },
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Počet cifer: ${settings.subtractionMinDigits} až ${settings.subtractionMaxDigits}",
                    style = MaterialTheme.typography.titleSmall,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Minimum cifer")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (2..5).forEach { count ->
                            FilterChip(
                                selected = settings.subtractionMinDigits == count,
                                onClick = {
                                    val newMax = settings.subtractionMaxDigits.coerceAtLeast(count)
                                    viewModel.setSubtractionDigitRange(count, newMax)
                                },
                                label = { Text(count.toString()) },
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Maximum cifer")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (2..6).forEach { count ->
                            FilterChip(
                                selected = settings.subtractionMaxDigits == count,
                                enabled = count >= settings.subtractionMinDigits,
                                onClick = {
                                    viewModel.setSubtractionDigitRange(settings.subtractionMinDigits, count)
                                },
                                label = { Text(count.toString()) },
                            )
                        }
                    }
                }
            }

            HorizontalDivider()
            SectionTitle("Násobilka")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Vybrané řady", style = MaterialTheme.typography.titleSmall)
                TextButton(onClick = viewModel::selectAllMultiplicationTables) {
                    Text("Vybrat vše")
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                (1..10).forEach { table ->
                    FilterChip(
                        selected = table in settings.multiplicationTables,
                        onClick = { viewModel.toggleMultiplicationTable(table) },
                        label = { Text(table.toString()) },
                    )
                }
            }

            SwitchSettingRow(
                title = "Zahrnout dělení v rámci násobilky",
                checked = settings.multiplicationIncludeDivision,
                onCheckedChange = viewModel::setMultiplicationIncludeDivision,
            )

            HorizontalDivider()
            SectionTitle("Výrazy a priority operací")

            SwitchSettingRow(
                title = "Používat závorky",
                checked = settings.expressionWithParentheses,
                onCheckedChange = viewModel::setExpressionWithParentheses,
            )

            HorizontalDivider()
            SectionTitle("Historie")

            OutlinedButton(
                onClick = viewModel::clearHistory,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Vymazat historii a statistiky")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun MethodOptionRow(
    selected: Boolean,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SwitchSettingRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
