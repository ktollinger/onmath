package cz.kt.onmath.ui.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.kt.onmath.R
import cz.kt.onmath.data.local.PracticeRepository
import cz.kt.onmath.data.settings.SettingsRepository
import cz.kt.onmath.domain.expression.ExpressionDifficulty
import cz.kt.onmath.domain.expression.ExpressionGenerator
import cz.kt.onmath.domain.expression.ExpressionNode
import cz.kt.onmath.domain.expression.ExpressionProblem
import cz.kt.onmath.domain.expression.ExpressionStepHint
import cz.kt.onmath.domain.expression.ExpressionToken
import cz.kt.onmath.domain.expression.findBinaryNode
import cz.kt.onmath.domain.expression.firstStepHintInfo
import cz.kt.onmath.domain.expression.reduceWith
import cz.kt.onmath.domain.expression.removeIntermediateAndAncestors
import cz.kt.onmath.domain.expression.tokenize
import cz.kt.onmath.domain.model.MathTopic
import cz.kt.onmath.ui.components.FeedbackState
import cz.kt.onmath.ui.components.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExpressionsUiState(
    val title: UiText = UiText.Resource(R.string.topic_expressions),
    val root: ExpressionNode? = null,
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
    val isSolved: Boolean = false,
    val isFinished: Boolean = false,
) {
    val isEditingIntermediate: Boolean
        get() = selectedNodeId != null

    val activeInputText: String
        get() = if (isEditingIntermediate) intermediateInputText else inputText

    val canUndoIntermediate: Boolean
        get() = intermediateOrder.isNotEmpty() && !isSolved
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
                root = first.root,
                originalExpression = first.key,
                tokens = first.root.tokenize(),
                totalCount = problems.size,
            )
        }
    }

    fun onSelectOperator(nodeId: String) {
        val state = _uiState.value
        if (state.isSolved || state.isFinished) return
        val current = problems.getOrNull(state.currentIndex) ?: return

        if (state.selectedNodeId == nodeId) {
            if (state.intermediateInputText.isEmpty()) {
                onCancelIntermediate()
            }
            return
        }

        val committed = tryAutoCommitPendingIntermediate(state, current) ?: return
        val (workingIntermediates, workingOrder) = committed

        val refreshedTokens = current.root.tokenize(workingIntermediates, selectedNodeId = null)
        val opToken = refreshedTokens.filterIsInstance<ExpressionToken.Operator>()
            .firstOrNull { it.nodeId == nodeId } ?: return

        if (!opToken.isReady) {
            _uiState.update {
                it.copy(
                    intermediates = workingIntermediates,
                    intermediateOrder = workingOrder,
                    selectedNodeId = null,
                    selectedSubExpression = "",
                    intermediateInputText = "",
                    tokens = refreshedTokens,
                    feedback = FeedbackState.Hint(UiText.Resource(R.string.expr_need_inner_first)),
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                intermediates = workingIntermediates,
                intermediateOrder = workingOrder,
                selectedNodeId = nodeId,
                selectedSubExpression = opToken.subExpression,
                intermediateInputText = "",
                tokens = current.root.tokenize(workingIntermediates, selectedNodeId = nodeId),
                feedback = FeedbackState.None,
            )
        }
    }

    fun onFocusFinalAnswer() {
        val state = _uiState.value
        if (state.isSolved || state.isFinished || state.selectedNodeId == null) return
        val current = problems.getOrNull(state.currentIndex) ?: return
        val committed = tryAutoCommitPendingIntermediate(state, current) ?: return
        val (workingIntermediates, workingOrder) = committed
        _uiState.update {
            it.copy(
                intermediates = workingIntermediates,
                intermediateOrder = workingOrder,
                selectedNodeId = null,
                selectedSubExpression = "",
                intermediateInputText = "",
                tokens = current.root.tokenize(workingIntermediates, selectedNodeId = null),
                feedback = FeedbackState.None,
            )
        }
    }

    private fun tryAutoCommitPendingIntermediate(
        state: ExpressionsUiState,
        current: ExpressionProblem,
    ): Pair<Map<String, Int>, List<String>>? {
        val pendingId = state.selectedNodeId ?: return state.intermediates to state.intermediateOrder
        if (state.intermediateInputText.isEmpty()) return state.intermediates to state.intermediateOrder

        val prevNode = current.root.findBinaryNode(pendingId) ?: return state.intermediates to state.intermediateOrder
        val given = state.intermediateInputText.toIntOrNull()
        if (given != prevNode.evaluate()) {
            _uiState.update {
                it.copy(
                    intermediateInputText = "",
                    feedback = FeedbackState.Wrong(
                        UiText.Resource(R.string.expr_wrong_intermediate, state.selectedSubExpression),
                    ),
                )
            }
            return null
        }

        val updatedIntermediates = state.intermediates + (pendingId to given)
        val updatedOrder = state.intermediateOrder.filterNot { it == pendingId } + pendingId
        return updatedIntermediates to updatedOrder
    }

    fun onSelectSubstituted(nodeId: String) {
        val state = _uiState.value
        if (state.isSolved || state.isFinished) return
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
        if (state.isSolved || state.isFinished || state.selectedNodeId == null) return
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
        if (state.isSolved || state.isFinished || state.intermediateOrder.isEmpty()) return
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
        if (state.isSolved || state.isFinished) return
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
        if (state.isSolved || state.isFinished) return
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
        val hintText = when (val hint = current.root.reduceWith(state.intermediates).firstStepHintInfo()) {
            ExpressionStepHint.SolveDefault -> UiText.Resource(R.string.expr_hint_solve)
            is ExpressionStepHint.Parentheses -> UiText.Resource(R.string.expr_hint_parens, hint.equation)
            is ExpressionStepHint.Precedence -> UiText.Resource(R.string.expr_hint_precedence, hint.equation)
            is ExpressionStepHint.LeftToRight -> UiText.Resource(R.string.expr_hint_left_to_right, hint.equation)
        }
        _uiState.update { it.copy(feedback = FeedbackState.Hint(hintText)) }
    }

    fun onSubmit() {
        val state = _uiState.value
        if (state.isSolved || state.isFinished) return
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
                    feedback = FeedbackState.Correct(UiText.Resource(R.string.feedback_correct_inspect)),
                    isSolved = true,
                )
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
                    feedback = FeedbackState.Wrong(UiText.Resource(R.string.expr_wrong_order)),
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
                    feedback = FeedbackState.Wrong(
                        UiText.Resource(R.string.expr_wrong_intermediate, state.selectedSubExpression),
                    ),
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
                topic = MathTopic.EXPRESSIONS,
                totalProblems = problems.size,
                correctFirstTry = correctFirstTry,
            )
            _uiState.update {
                it.copy(isSolved = false, isFinished = true)
            }
        } else {
            hadMistakeOnCurrent = false
            val next = problems[nextIndex]
            _uiState.update {
                it.copy(
                    currentIndex = nextIndex,
                    root = next.root,
                    originalExpression = next.key,
                    tokens = next.root.tokenize(),
                    intermediates = emptyMap(),
                    intermediateOrder = emptyList(),
                    selectedNodeId = null,
                    selectedSubExpression = "",
                    intermediateInputText = "",
                    inputText = "",
                    feedback = FeedbackState.None,
                    isSolved = false,
                )
            }
        }
    }
}
