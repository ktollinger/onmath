package cz.kt.onmath.ui.practice.subtraction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.kt.onmath.R
import cz.kt.onmath.data.local.PracticeRepository
import cz.kt.onmath.data.settings.SettingsRepository
import cz.kt.onmath.domain.model.MathTopic
import cz.kt.onmath.domain.model.SubtractionMethod
import cz.kt.onmath.domain.subtraction.SubtractionConfig
import cz.kt.onmath.domain.subtraction.SubtractionGenerator
import cz.kt.onmath.domain.subtraction.SubtractionProblem
import cz.kt.onmath.ui.components.FeedbackState
import cz.kt.onmath.ui.components.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.pow

data class SubtractionUiState(
    val problem: SubtractionProblem? = null,
    val method: SubtractionMethod = SubtractionMethod.BORROW_TOP,
    val currentIndex: Int = 0,
    val totalCount: Int = 10,
    val correctFirstTry: Int = 0,
    val resultDigits: Map<Int, Int> = emptyMap(),
    val activePlaceIndex: Int = 0,
    val markedBorrows: Set<Int> = emptySet(),
    val wrongPlaces: Set<Int> = emptySet(),
    val feedback: FeedbackState = FeedbackState.None,
    val isSolved: Boolean = false,
    val isFinished: Boolean = false,
)

class SubtractionViewModel(
    private val settingsRepository: SettingsRepository,
    private val practiceRepository: PracticeRepository,
    private val generator: SubtractionGenerator = SubtractionGenerator(),
) : ViewModel() {

    private var problems: List<SubtractionProblem> = emptyList()
    private var hadMistakeOnCurrent = false

    private val _uiState = MutableStateFlow(SubtractionUiState())
    val uiState: StateFlow<SubtractionUiState> = _uiState.asStateFlow()

    init {
        startNewSession()
    }

    fun startNewSession() {
        viewModelScope.launch {
            val settings = settingsRepository.settingsFlow.first()
            val config = SubtractionConfig(
                minDigits = settings.subtractionMinDigits,
                maxDigits = settings.subtractionMaxDigits,
            )
            problems = generator.generateSet(config, count = 10)
            hadMistakeOnCurrent = false
            _uiState.value = SubtractionUiState(
                problem = problems.first(),
                method = settings.subtractionMethod,
                totalCount = problems.size,
                activePlaceIndex = 0,
            )
        }
    }

    fun onSelectPlace(placeIndex: Int) {
        if (_uiState.value.isSolved || _uiState.value.isFinished) return
        _uiState.update { it.copy(activePlaceIndex = placeIndex, feedback = FeedbackState.None) }
    }

    fun onToggleBorrowMarker(placeIndex: Int) {
        if (_uiState.value.isSolved || _uiState.value.isFinished) return
        _uiState.update { state ->
            val updated = if (placeIndex in state.markedBorrows) {
                state.markedBorrows - placeIndex
            } else {
                state.markedBorrows + placeIndex
            }
            state.copy(markedBorrows = updated)
        }
    }

    fun onDigit(digit: Int) {
        val state = _uiState.value
        val problem = state.problem ?: return
        if (state.isSolved || state.isFinished) return

        val updatedDigits = state.resultDigits.toMutableMap().apply {
            this[state.activePlaceIndex] = digit
        }
        val nextPlace = (state.activePlaceIndex + 1).coerceAtMost(problem.digitCount - 1)
        _uiState.update {
            it.copy(
                resultDigits = updatedDigits,
                activePlaceIndex = nextPlace,
                wrongPlaces = it.wrongPlaces - state.activePlaceIndex,
                feedback = FeedbackState.None,
            )
        }
    }

    fun onBackspace() {
        val state = _uiState.value
        if (state.isSolved || state.isFinished) return

        val updatedDigits = state.resultDigits.toMutableMap()
        val targetPlace = if (state.activePlaceIndex in updatedDigits) {
            state.activePlaceIndex
        } else {
            (state.activePlaceIndex - 1).coerceAtLeast(0)
        }
        updatedDigits.remove(targetPlace)
        _uiState.update {
            it.copy(
                resultDigits = updatedDigits,
                activePlaceIndex = targetPlace,
                wrongPlaces = it.wrongPlaces - targetPlace,
                feedback = FeedbackState.None,
            )
        }
    }

    fun onHint() {
        val state = _uiState.value
        val problem = state.problem ?: return
        val col = problem.columnsRightToLeft.getOrNull(state.activePlaceIndex) ?: return
        val placeLabel: UiText = when (col.placeIndex) {
            0 -> UiText.Resource(R.string.place_ones)
            1 -> UiText.Resource(R.string.place_tens)
            2 -> UiText.Resource(R.string.place_hundreds)
            3 -> UiText.Resource(R.string.place_thousands)
            4 -> UiText.Resource(R.string.place_ten_thousands)
            5 -> UiText.Resource(R.string.place_hundred_thousands)
            else -> UiText.Resource(R.string.place_other, col.placeIndex + 1)
        }
        val hint = when (state.method) {
            SubtractionMethod.BORROW_TOP -> {
                val topAfterLend = col.effectiveUpperAfterLend
                if (col.requiresBorrowFromNext) {
                    UiText.Resource(
                        R.string.sub_hint_borrow_top_with_borrow,
                        placeLabel,
                        topAfterLend + 10,
                        col.subtrahendDigit,
                        col.expectedResultDigit,
                    )
                } else {
                    UiText.Resource(
                        R.string.sub_hint_borrow_top_plain,
                        placeLabel,
                        topAfterLend,
                        col.subtrahendDigit,
                        col.expectedResultDigit,
                    )
                }
            }
            SubtractionMethod.CARRY_BOTTOM -> {
                val bottomWithCarry = col.effectiveLowerWithCarry
                if (col.requiresBorrowFromNext) {
                    UiText.Resource(
                        R.string.sub_hint_carry_bottom_with_carry,
                        placeLabel,
                        col.minuendDigit + 10,
                        bottomWithCarry,
                        col.expectedResultDigit,
                    )
                } else {
                    UiText.Resource(
                        R.string.sub_hint_carry_bottom_plain,
                        placeLabel,
                        col.minuendDigit,
                        bottomWithCarry,
                        col.expectedResultDigit,
                    )
                }
            }
        }
        _uiState.update { it.copy(feedback = FeedbackState.Hint(hint)) }
    }

    fun onSubmit() {
        val state = _uiState.value
        val problem = state.problem ?: return
        if (state.isFinished) return
        if (state.isSolved) {
            onNextProblem()
            return
        }
        if (state.resultDigits.isEmpty()) return

        var givenAnswer = 0
        for (place in 0 until problem.digitCount) {
            val digit = state.resultDigits[place] ?: 0
            givenAnswer += digit * 10.0.pow(place).toInt()
        }

        if (givenAnswer == problem.answer) {
            val newCorrectFirstTry = if (!hadMistakeOnCurrent) state.correctFirstTry + 1 else state.correctFirstTry
            _uiState.update {
                it.copy(
                    correctFirstTry = newCorrectFirstTry,
                    wrongPlaces = emptySet(),
                    feedback = FeedbackState.Correct(UiText.Resource(R.string.feedback_correct_inspect)),
                    isSolved = true,
                )
            }
        } else {
            val wrong = problem.columnsRightToLeft
                .filter { col -> (state.resultDigits[col.placeIndex] ?: 0) != col.expectedResultDigit }
                .map { it.placeIndex }
                .toSet()

            if (!hadMistakeOnCurrent) {
                hadMistakeOnCurrent = true
                viewModelScope.launch {
                    practiceRepository.recordMistake(
                        topic = MathTopic.SUBTRACTION,
                        problemKey = problem.key,
                        displayText = "${problem.minuend} − ${problem.subtrahend}",
                        expectedAnswer = problem.answer,
                        givenAnswer = givenAnswer,
                    )
                }
            }
            _uiState.update {
                it.copy(
                    wrongPlaces = wrong,
                    activePlaceIndex = wrong.minOrNull() ?: 0,
                    feedback = FeedbackState.Wrong(UiText.Resource(R.string.sub_wrong_columns)),
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
                topic = MathTopic.SUBTRACTION,
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
                    problem = problems[nextIndex],
                    currentIndex = nextIndex,
                    resultDigits = emptyMap(),
                    activePlaceIndex = 0,
                    markedBorrows = emptySet(),
                    wrongPlaces = emptySet(),
                    feedback = FeedbackState.None,
                    isSolved = false,
                )
            }
        }
    }
}
