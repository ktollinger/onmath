package cz.kt.onmath.ui.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.kt.onmath.R
import cz.kt.onmath.data.local.PracticeRepository
import cz.kt.onmath.data.settings.SettingsRepository
import cz.kt.onmath.domain.model.MathTopic
import cz.kt.onmath.domain.multiplication.MultiplicationConfig
import cz.kt.onmath.domain.multiplication.MultiplicationGenerator
import cz.kt.onmath.domain.multiplication.MultiplicationOperation
import cz.kt.onmath.domain.multiplication.MultiplicationProblem
import cz.kt.onmath.ui.components.FeedbackState
import cz.kt.onmath.ui.components.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MultiplicationViewModel(
    private val settingsRepository: SettingsRepository,
    private val practiceRepository: PracticeRepository,
    private val generator: MultiplicationGenerator = MultiplicationGenerator(),
) : ViewModel() {

    private var problems: List<MultiplicationProblem> = emptyList()
    private var hadMistakeOnCurrent = false

    private val _uiState = MutableStateFlow(
        StandardPracticeUiState(title = UiText.Resource(R.string.topic_multiplication)),
    )
    val uiState: StateFlow<StandardPracticeUiState> = _uiState.asStateFlow()

    init {
        startNewSession()
    }

    fun startNewSession() {
        viewModelScope.launch {
            val settings = settingsRepository.settingsFlow.first()
            val config = MultiplicationConfig(
                selectedTables = settings.multiplicationTables,
                includeDivision = settings.multiplicationIncludeDivision,
            )
            problems = generator.generateSet(config, count = 10)
            hadMistakeOnCurrent = false
            _uiState.value = StandardPracticeUiState(
                title = UiText.Resource(R.string.topic_multiplication),
                problemDisplay = problems.first().displayText,
                totalCount = problems.size,
            )
        }
    }

    fun onDigit(digit: Int) {
        if (_uiState.value.isSolved || _uiState.value.isFinished) return
        _uiState.update { state ->
            if (state.inputText.length >= 4) state
            else state.copy(
                inputText = (state.inputText + digit.toString()).trimStart('0').ifEmpty { "0" },
                feedback = FeedbackState.None,
            )
        }
    }

    fun onBackspace() {
        if (_uiState.value.isSolved || _uiState.value.isFinished) return
        _uiState.update { state ->
            state.copy(
                inputText = state.inputText.dropLast(1),
                feedback = FeedbackState.None,
            )
        }
    }

    fun onHint() {
        val current = problems.getOrNull(_uiState.value.currentIndex) ?: return
        val hint = when (current.operation) {
            MultiplicationOperation.MULTIPLY ->
                UiText.Resource(R.string.mult_hint_multiply, current.left, current.right)
            MultiplicationOperation.DIVIDE ->
                UiText.Resource(R.string.mult_hint_divide, current.right, current.left)
        }
        _uiState.update { it.copy(feedback = FeedbackState.Hint(hint)) }
    }

    fun onSubmit() {
        val state = _uiState.value
        if (state.isFinished) return
        if (state.isSolved) {
            onNextProblem()
            return
        }
        val current = problems.getOrNull(state.currentIndex) ?: return
        val given = state.inputText.toIntOrNull() ?: return

        if (given == current.answer) {
            val newCorrectFirstTry = if (!hadMistakeOnCurrent) state.correctFirstTry + 1 else state.correctFirstTry
            _uiState.update {
                it.copy(
                    correctFirstTry = newCorrectFirstTry,
                    feedback = FeedbackState.Correct(UiText.Resource(R.string.feedback_correct_inspect)),
                    isSolved = true,
                )
            }
        } else {
            if (!hadMistakeOnCurrent) {
                hadMistakeOnCurrent = true
                viewModelScope.launch {
                    practiceRepository.recordMistake(
                        topic = MathTopic.MULTIPLICATION,
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
                    feedback = FeedbackState.Wrong(UiText.Resource(R.string.mult_wrong)),
                )
            }
        }
    }

    fun onNextProblem() {
        val state = _uiState.value
        if (!state.isSolved || state.isFinished) return
        viewModelScope.launch {
            advanceToNext(state.correctFirstTry)
        }
    }

    private suspend fun advanceToNext(correctFirstTry: Int) {
        val nextIndex = _uiState.value.currentIndex + 1
        if (nextIndex >= problems.size) {
            practiceRepository.recordSession(
                topic = MathTopic.MULTIPLICATION,
                totalProblems = problems.size,
                correctFirstTry = correctFirstTry,
            )
            _uiState.update {
                it.copy(isSolved = false, isFinished = true)
            }
        } else {
            hadMistakeOnCurrent = false
            _uiState.update {
                it.copy(
                    currentIndex = nextIndex,
                    problemDisplay = problems[nextIndex].displayText,
                    inputText = "",
                    feedback = FeedbackState.None,
                    isSolved = false,
                )
            }
        }
    }
}
