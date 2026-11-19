package cz.kt.onmath.ui.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.kt.onmath.data.local.PracticeRepository
import cz.kt.onmath.data.settings.SettingsRepository
import cz.kt.onmath.domain.expression.ExpressionDifficulty
import cz.kt.onmath.domain.expression.ExpressionGenerator
import cz.kt.onmath.domain.expression.ExpressionProblem
import cz.kt.onmath.domain.model.MathTopic
import cz.kt.onmath.ui.components.FeedbackState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExpressionsViewModel(
    private val settingsRepository: SettingsRepository,
    private val practiceRepository: PracticeRepository,
    private val generator: ExpressionGenerator = ExpressionGenerator(),
) : ViewModel() {

    private var problems: List<ExpressionProblem> = emptyList()
    private var hadMistakeOnCurrent = false

    private val _uiState = MutableStateFlow(StandardPracticeUiState(title = "Výrazy a závorky"))
    val uiState: StateFlow<StandardPracticeUiState> = _uiState.asStateFlow()

    init {
        startNewSession()
    }

    fun startNewSession() {
        viewModelScope.launch {
            val settings = settingsRepository.settingsFlow.first()
            val difficulty = if (settings.expressionWithParentheses) {
                ExpressionDifficulty.WITH_PARENTHESES
            } else {
                ExpressionDifficulty.WITHOUT_PARENTHESES
            }
            problems = generator.generateSet(difficulty, count = 10)
            hadMistakeOnCurrent = false
            _uiState.value = StandardPracticeUiState(
                title = "Výrazy a závorky",
                problemDisplay = problems.first().displayText,
                totalCount = problems.size,
            )
        }
    }

    fun onDigit(digit: Int) {
        if (_uiState.value.isAdvancing || _uiState.value.isFinished) return
        _uiState.update { state ->
            if (state.inputText.length >= 4) state
            else state.copy(
                inputText = (state.inputText + digit.toString()).trimStart('0').ifEmpty { "0" },
                feedback = FeedbackState.None,
            )
        }
    }

    fun onBackspace() {
        if (_uiState.value.isAdvancing || _uiState.value.isFinished) return
        _uiState.update { state ->
            state.copy(
                inputText = state.inputText.dropLast(1),
                feedback = FeedbackState.None,
            )
        }
    }

    fun onHint() {
        val current = problems.getOrNull(_uiState.value.currentIndex) ?: return
        _uiState.update { it.copy(feedback = FeedbackState.Hint(current.hintText)) }
    }

    fun onSubmit() {
        val state = _uiState.value
        if (state.isAdvancing || state.isFinished) return
        val current = problems.getOrNull(state.currentIndex) ?: return
        val given = state.inputText.toIntOrNull() ?: return

        if (given == current.answer) {
            val newCorrectFirstTry = if (!hadMistakeOnCurrent) state.correctFirstTry + 1 else state.correctFirstTry
            _uiState.update {
                it.copy(
                    correctFirstTry = newCorrectFirstTry,
                    feedback = FeedbackState.Correct(),
                    isAdvancing = true,
                )
            }
            viewModelScope.launch {
                delay(650)
                advanceToNext(newCorrectFirstTry)
            }
        } else {
            if (!hadMistakeOnCurrent) {
                hadMistakeOnCurrent = true
                viewModelScope.launch {
                    practiceRepository.recordMistake(
                        topic = MathTopic.EXPRESSIONS,
                        problemKey = current.key,
                        displayText = current.displayText,
                        expectedAnswer = current.answer,
                        givenAnswer = given,
                    )
                }
            }
            _uiState.update {
                it.copy(
                    inputText = "",
                    feedback = FeedbackState.Wrong("Zkus zkontrolovat pořadí operací."),
                )
            }
        }
    }

    private suspend fun advanceToNext(correctFirstTry: Int) {
        val nextIndex = _uiState.value.currentIndex + 1
        if (nextIndex >= problems.size) {
            practiceRepository.recordSession(
                topic = MathTopic.EXPRESSIONS,
                totalProblems = problems.size,
                correctFirstTry = correctFirstTry,
            )
            _uiState.update {
                it.copy(isAdvancing = false, isFinished = true)
            }
        } else {
            hadMistakeOnCurrent = false
            _uiState.update {
                it.copy(
                    currentIndex = nextIndex,
                    problemDisplay = problems[nextIndex].displayText,
                    inputText = "",
                    feedback = FeedbackState.None,
                    isAdvancing = false,
                )
            }
        }
    }
}
