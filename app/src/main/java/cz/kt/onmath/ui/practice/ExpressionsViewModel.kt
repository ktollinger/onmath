package cz.kt.onmath.ui.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.kt.onmath.data.local.PracticeRepository
import cz.kt.onmath.data.settings.SettingsRepository
import cz.kt.onmath.domain.expression.ExpressionDifficulty
import cz.kt.onmath.domain.expression.ExpressionGenerator
import cz.kt.onmath.domain.expression.ExpressionProblem
import cz.kt.onmath.domain.expression.ExpressionToken
import cz.kt.onmath.domain.expression.findBinaryNode
import cz.kt.onmath.domain.expression.firstStepHint
import cz.kt.onmath.domain.expression.reduceWith
import cz.kt.onmath.domain.expression.removeIntermediateAndAncestors
import cz.kt.onmath.domain.expression.tokenize
import cz.kt.onmath.domain.model.MathTopic
import cz.kt.onmath.ui.components.FeedbackState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExpressionsUiState(
    val title: String = "Výrazy a závorky",
    val originalExpression: String = "",
    val tokens: List<ExpressionToken> = emptyList(),
    val intermediates: Map<String, Int> = emptyMap(),
    val intermediateOrder: List<String> = emptyList(),
    val selectedNodeId: String? = null,
    val selectedSubExpression: String = "",
    val intermediateInputText: String = "",
    val inputText: String = "",
    val currentIndex: Int = 0,
    val totalCount: Int = 10,
    val correctFirstTry: Int = 0,
    val feedback: FeedbackState = FeedbackState.None,
    val isAdvancing: Boolean = false,
    val isFinished: Boolean = false,
) {
    val isEditingIntermediate: Boolean
        get() = selectedNodeId != null

    val activeInputText: String
        get() = if (isEditingIntermediate) intermediateInputText else inputText

    val canUndoIntermediate: Boolean
        get() = intermediateOrder.isNotEmpty() && !isAdvancing
}

class ExpressionsViewModel(
    private val settingsRepository: SettingsRepository,
    private val practiceRepository: PracticeRepository,
    private val generator: ExpressionGenerator = ExpressionGenerator(),
) : ViewModel() {

    private var problems: List<ExpressionProblem> = emptyList()
    private var hadMistakeOnCurrent = false

    private val _uiState = MutableStateFlow(ExpressionsUiState())
    val uiState: StateFlow<ExpressionsUiState> = _uiState.asStateFlow()

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
            problems = generator.generateSet(
                difficulty = difficulty,
                count = 10,
                allowLargeIntermediate = settings.expressionLargeIntermediate,
            )
            hadMistakeOnCurrent = false
            val first = problems.first()
            _uiState.value = ExpressionsUiState(
                originalExpression = first.key,
                tokens = first.root.tokenize(),
                totalCount = problems.size,
            )
        }
    }

    fun onSelectOperator(nodeId: String) {
        val state = _uiState.value
        if (state.isAdvancing || state.isFinished) return
        val opToken = state.tokens.filterIsInstance<ExpressionToken.Operator>()
            .firstOrNull { it.nodeId == nodeId } ?: return

        if (!opToken.isReady) {
            _uiState.update {
                it.copy(feedback = FeedbackState.Hint("Nejdřív spočítej podvýraz v závorce nebo násobení/dělení."))
            }
            return
        }

        val current = problems.getOrNull(state.currentIndex) ?: return
        if (state.selectedNodeId == nodeId) {
            _uiState.update {
                it.copy(
                    selectedNodeId = null,
                    selectedSubExpression = "",
                    intermediateInputText = "",
                    tokens = current.root.tokenize(it.intermediates, selectedNodeId = null),
                    feedback = FeedbackState.None,
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    selectedNodeId = nodeId,
                    selectedSubExpression = opToken.subExpression,
                    intermediateInputText = "",
                    tokens = current.root.tokenize(it.intermediates, selectedNodeId = nodeId),
                    feedback = FeedbackState.None,
                )
            }
        }
    }

    fun onSelectSubstituted(nodeId: String) {
        val state = _uiState.value
        if (state.isAdvancing || state.isFinished) return
        val current = problems.getOrNull(state.currentIndex) ?: return

        val (updatedIntermediates, updatedOrder) = removeIntermediateAndAncestors(
            nodeId = nodeId,
            intermediates = state.intermediates,
            order = state.intermediateOrder,
        )
        val updatedTokens = current.root.tokenize(updatedIntermediates, selectedNodeId = nodeId)
        val subExpr = updatedTokens.filterIsInstance<ExpressionToken.Operator>()
            .firstOrNull { it.nodeId == nodeId }
            ?.subExpression
            .orEmpty()

        _uiState.update {
            it.copy(
                intermediates = updatedIntermediates,
                intermediateOrder = updatedOrder,
                selectedNodeId = nodeId,
                selectedSubExpression = subExpr,
                intermediateInputText = "",
                tokens = updatedTokens,
                feedback = FeedbackState.None,
            )
        }
    }

    fun onCancelIntermediate() {
        val state = _uiState.value
        if (state.isAdvancing || state.isFinished || state.selectedNodeId == null) return
        val current = problems.getOrNull(state.currentIndex) ?: return
        _uiState.update {
            it.copy(
                selectedNodeId = null,
                selectedSubExpression = "",
                intermediateInputText = "",
                tokens = current.root.tokenize(it.intermediates, selectedNodeId = null),
                feedback = FeedbackState.None,
            )
        }
    }

    fun onUndoIntermediate() {
        val state = _uiState.value
        if (state.isAdvancing || state.isFinished || state.intermediateOrder.isEmpty()) return
        val current = problems.getOrNull(state.currentIndex) ?: return
        val lastNodeId = state.intermediateOrder.last()
        val (updatedIntermediates, updatedOrder) = removeIntermediateAndAncestors(
            nodeId = lastNodeId,
            intermediates = state.intermediates,
            order = state.intermediateOrder,
        )
        _uiState.update {
            it.copy(
                intermediates = updatedIntermediates,
                intermediateOrder = updatedOrder,
                selectedNodeId = null,
                selectedSubExpression = "",
                intermediateInputText = "",
                tokens = current.root.tokenize(updatedIntermediates, selectedNodeId = null),
                feedback = FeedbackState.None,
            )
        }
    }

    fun onDigit(digit: Int) {
        val state = _uiState.value
        if (state.isAdvancing || state.isFinished) return
        _uiState.update { current ->
            if (current.isEditingIntermediate) {
                if (current.intermediateInputText.length >= 4) current
                else current.copy(
                    intermediateInputText = (current.intermediateInputText + digit.toString())
                        .trimStart('0')
                        .ifEmpty { "0" },
                    feedback = FeedbackState.None,
                )
            } else {
                if (current.inputText.length >= 4) current
                else current.copy(
                    inputText = (current.inputText + digit.toString())
                        .trimStart('0')
                        .ifEmpty { "0" },
                    feedback = FeedbackState.None,
                )
            }
        }
    }

    fun onBackspace() {
        val state = _uiState.value
        if (state.isAdvancing || state.isFinished) return
        _uiState.update { current ->
            if (current.isEditingIntermediate) {
                current.copy(
                    intermediateInputText = current.intermediateInputText.dropLast(1),
                    feedback = FeedbackState.None,
                )
            } else {
                current.copy(
                    inputText = current.inputText.dropLast(1),
                    feedback = FeedbackState.None,
                )
            }
        }
    }

    fun onHint() {
        val state = _uiState.value
        val current = problems.getOrNull(state.currentIndex) ?: return
        val hint = current.root.reduceWith(state.intermediates).firstStepHint()
        _uiState.update { it.copy(feedback = FeedbackState.Hint(hint)) }
    }

    fun onSubmit() {
        val state = _uiState.value
        if (state.isAdvancing || state.isFinished) return
        val current = problems.getOrNull(state.currentIndex) ?: return

        val selectedId = state.selectedNodeId
        if (selectedId != null) {
            submitIntermediate(state, current, selectedId)
            return
        }

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

    private fun submitIntermediate(
        state: ExpressionsUiState,
        current: ExpressionProblem,
        selectedId: String,
    ) {
        val given = state.intermediateInputText.toIntOrNull() ?: return
        val targetNode = current.root.findBinaryNode(selectedId) ?: return
        val expected = targetNode.evaluate()

        if (given == expected) {
            val updatedIntermediates = state.intermediates + (selectedId to given)
            val updatedOrder = state.intermediateOrder.filterNot { it == selectedId } + selectedId
            val updatedInputText = if (selectedId == "root") given.toString() else state.inputText
            _uiState.update {
                it.copy(
                    intermediates = updatedIntermediates,
                    intermediateOrder = updatedOrder,
                    selectedNodeId = null,
                    selectedSubExpression = "",
                    intermediateInputText = "",
                    inputText = updatedInputText,
                    tokens = current.root.tokenize(updatedIntermediates, selectedNodeId = null),
                    feedback = FeedbackState.None,
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    intermediateInputText = "",
                    feedback = FeedbackState.Wrong("Mezivýsledek pro ${state.selectedSubExpression} nesedí, zkus to znovu."),
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
            val next = problems[nextIndex]
            _uiState.update {
                it.copy(
                    currentIndex = nextIndex,
                    originalExpression = next.key,
                    tokens = next.root.tokenize(),
                    intermediates = emptyMap(),
                    intermediateOrder = emptyList(),
                    selectedNodeId = null,
                    selectedSubExpression = "",
                    intermediateInputText = "",
                    inputText = "",
                    feedback = FeedbackState.None,
                    isAdvancing = false,
                )
            }
        }
    }
}
