package cz.kt.onmath.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cz.kt.onmath.domain.model.SubtractionMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "onmath_settings")

interface SettingsRepository {
    val settingsFlow: Flow<AppSettings>

    suspend fun updateSubtractionMethod(method: SubtractionMethod)
    suspend fun updateSubtractionDigitRange(minDigits: Int, maxDigits: Int)
    suspend fun updateMultiplicationTables(tables: Set<Int>)
    suspend fun updateMultiplicationIncludeDivision(include: Boolean)
    suspend fun updateExpressionWithParentheses(withParentheses: Boolean)
}

class DataStoreSettingsRepository(
    private val context: Context,
) : SettingsRepository {

    private object Keys {
        val SUBTRACTION_METHOD = stringPreferencesKey("subtraction_method")
        val SUBTRACTION_MIN_DIGITS = intPreferencesKey("subtraction_min_digits")
        val SUBTRACTION_MAX_DIGITS = intPreferencesKey("subtraction_max_digits")
        val MULTIPLICATION_TABLES = stringPreferencesKey("multiplication_tables")
        val MULTIPLICATION_INCLUDE_DIVISION = booleanPreferencesKey("multiplication_include_division")
        val EXPRESSION_WITH_PARENTHESES = booleanPreferencesKey("expression_with_parentheses")
    }

    override val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        val method = prefs[Keys.SUBTRACTION_METHOD]
            ?.let { runCatching { SubtractionMethod.valueOf(it) }.getOrNull() }
            ?: SubtractionMethod.BORROW_TOP

        val minDigits = (prefs[Keys.SUBTRACTION_MIN_DIGITS] ?: 2).coerceIn(2, 6)
        val maxDigits = (prefs[Keys.SUBTRACTION_MAX_DIGITS] ?: 6).coerceIn(minDigits, 6)

        val tables = prefs[Keys.MULTIPLICATION_TABLES]
            ?.split(",")
            ?.mapNotNull { it.trim().toIntOrNull() }
            ?.filter { it in 1..10 }
            ?.toSet()
            ?.takeIf { it.isNotEmpty() }
            ?: (1..10).toSet()

        AppSettings(
            subtractionMethod = method,
            subtractionMinDigits = minDigits,
            subtractionMaxDigits = maxDigits,
            multiplicationTables = tables,
            multiplicationIncludeDivision = prefs[Keys.MULTIPLICATION_INCLUDE_DIVISION] ?: false,
            expressionWithParentheses = prefs[Keys.EXPRESSION_WITH_PARENTHESES] ?: true,
        )
    }

    override suspend fun updateSubtractionMethod(method: SubtractionMethod) {
        context.dataStore.edit { it[Keys.SUBTRACTION_METHOD] = method.name }
    }

    override suspend fun updateSubtractionDigitRange(minDigits: Int, maxDigits: Int) {
        val safeMin = minDigits.coerceIn(2, 6)
        val safeMax = maxDigits.coerceIn(safeMin, 6)
        context.dataStore.edit {
            it[Keys.SUBTRACTION_MIN_DIGITS] = safeMin
            it[Keys.SUBTRACTION_MAX_DIGITS] = safeMax
        }
    }

    override suspend fun updateMultiplicationTables(tables: Set<Int>) {
        val valid = tables.filter { it in 1..10 }.sorted()
        if (valid.isEmpty()) return
        context.dataStore.edit {
            it[Keys.MULTIPLICATION_TABLES] = valid.joinToString(",")
        }
    }

    override suspend fun updateMultiplicationIncludeDivision(include: Boolean) {
        context.dataStore.edit { it[Keys.MULTIPLICATION_INCLUDE_DIVISION] = include }
    }

    override suspend fun updateExpressionWithParentheses(withParentheses: Boolean) {
        context.dataStore.edit { it[Keys.EXPRESSION_WITH_PARENTHESES] = withParentheses }
    }
}
