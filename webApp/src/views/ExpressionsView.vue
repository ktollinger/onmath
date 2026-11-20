<script setup lang="ts">
import { computed, ref } from 'vue';
import ExpressionTreeNode from '../components/ExpressionTreeNode.vue';
import FeedbackBanner, { type FeedbackState } from '../components/FeedbackBanner.vue';
import MathKeypad from '../components/MathKeypad.vue';
import PracticeHeader from '../components/PracticeHeader.vue';
import SessionSummary from '../components/SessionSummary.vue';
import {
  evaluateNode,
  findBinaryNode,
  firstStepHintInfo,
  generateExpressionSet,
  reduceWithIntermediates,
  removeIntermediateAndAncestors,
  tokenizeExpression,
  type ExpressionProblem,
} from '../domain/expression';
import { store } from '../state/store';

const emit = defineEmits<{
  back: [];
}>();

const problems = ref<ExpressionProblem[]>([]);
const currentIndex = ref(0);
const intermediates = ref<Record<string, number>>({});
const intermediateOrder = ref<string[]>([]);
const selectedNodeId = ref<string | null>(null);
const selectedSubExpression = ref('');
const intermediateInputText = ref('');
const inputText = ref('');
const correctFirstTry = ref(0);
const hadMistake = ref(false);
const feedback = ref<FeedbackState>({ kind: 'none' });
const isSolved = ref(false);
const isFinished = ref(false);

const activeInputText = computed(() =>
  selectedNodeId.value !== null ? intermediateInputText.value : inputText.value,
);

function startNewSession() {
  const difficulty = store.settings.expressionWithParentheses ? 'WITH_PARENTHESES' : 'WITHOUT_PARENTHESES';
  problems.value = generateExpressionSet(difficulty, 10, store.settings.expressionLargeIntermediate);
  currentIndex.value = 0;
  intermediates.value = {};
  intermediateOrder.value = [];
  selectedNodeId.value = null;
  selectedSubExpression.value = '';
  intermediateInputText.value = '';
  inputText.value = '';
  correctFirstTry.value = 0;
  hadMistake.value = false;
  feedback.value = { kind: 'none' };
  isSolved.value = false;
  isFinished.value = false;
}

startNewSession();

function tryAutoCommitPendingIntermediate(current: ExpressionProblem): boolean {
  const pendingId = selectedNodeId.value;
  if (!pendingId || intermediateInputText.value === '') return true;
  const prevNode = findBinaryNode(current.root, pendingId);
  if (!prevNode) return true;

  const given = Number(intermediateInputText.value);
  if (given !== evaluateNode(prevNode)) {
    intermediateInputText.value = '';
    feedback.value = {
      kind: 'wrong',
      message: store.t('exprWrongIntermediate', selectedSubExpression.value),
    };
    return false;
  }

  intermediates.value = { ...intermediates.value, [pendingId]: given };
  intermediateOrder.value = [...intermediateOrder.value.filter((id) => id !== pendingId), pendingId];
  return true;
}

function onSelectOperator(nodeId: string) {
  if (isSolved.value || isFinished.value) return;
  const current = problems.value[currentIndex.value];
  if (!current) return;

  if (selectedNodeId.value === nodeId) {
    if (intermediateInputText.value === '') onCancelIntermediate();
    return;
  }

  if (!tryAutoCommitPendingIntermediate(current)) return;

  const tokens = tokenizeExpression(current.root, intermediates.value, null);
  const opToken = tokens.find((t) => t.kind === 'operator' && t.nodeId === nodeId);
  if (!opToken || opToken.kind !== 'operator') return;

  if (!opToken.isReady) {
    selectedNodeId.value = null;
    selectedSubExpression.value = '';
    intermediateInputText.value = '';
    feedback.value = { kind: 'hint', message: store.t('exprNeedInnerFirst') };
    return;
  }

  selectedNodeId.value = nodeId;
  selectedSubExpression.value = opToken.subExpression;
  intermediateInputText.value = '';
  feedback.value = { kind: 'none' };
}

function onSelectSubstituted(nodeId: string) {
  if (isSolved.value || isFinished.value) return;
  const current = problems.value[currentIndex.value];
  if (!current) return;

  const cleared = removeIntermediateAndAncestors(nodeId, intermediates.value, intermediateOrder.value);
  intermediates.value = cleared.intermediates;
  intermediateOrder.value = cleared.order;

  const tokens = tokenizeExpression(current.root, intermediates.value, nodeId);
  const opToken = tokens.find((t) => t.kind === 'operator' && t.nodeId === nodeId);

  selectedNodeId.value = nodeId;
  selectedSubExpression.value = opToken?.kind === 'operator' ? opToken.subExpression : '';
  intermediateInputText.value = '';
  feedback.value = { kind: 'none' };
}

function onFocusFinalAnswer() {
  if (isSolved.value || isFinished.value || selectedNodeId.value === null) return;
  const current = problems.value[currentIndex.value];
  if (!current) return;
  if (!tryAutoCommitPendingIntermediate(current)) return;
  selectedNodeId.value = null;
  selectedSubExpression.value = '';
  intermediateInputText.value = '';
  feedback.value = { kind: 'none' };
}

function onCancelIntermediate() {
  if (isSolved.value || isFinished.value) return;
  selectedNodeId.value = null;
  selectedSubExpression.value = '';
  intermediateInputText.value = '';
  feedback.value = { kind: 'none' };
}

function onUndoIntermediate() {
  if (isSolved.value || isFinished.value || intermediateOrder.value.length === 0) return;
  const lastId = intermediateOrder.value[intermediateOrder.value.length - 1];
  const cleared = removeIntermediateAndAncestors(lastId, intermediates.value, intermediateOrder.value);
  intermediates.value = cleared.intermediates;
  intermediateOrder.value = cleared.order;
  selectedNodeId.value = null;
  selectedSubExpression.value = '';
  intermediateInputText.value = '';
  feedback.value = { kind: 'none' };
}

function onDigit(d: number) {
  if (isSolved.value || isFinished.value) return;
  if (selectedNodeId.value !== null) {
    if (intermediateInputText.value.length >= 4) return;
    intermediateInputText.value = `${intermediateInputText.value}${d}`.replace(/^0+/, '') || '0';
  } else {
    if (inputText.value.length >= 4) return;
    inputText.value = `${inputText.value}${d}`.replace(/^0+/, '') || '0';
  }
  feedback.value = { kind: 'none' };
}

function onBackspace() {
  if (isSolved.value || isFinished.value) return;
  if (selectedNodeId.value !== null) {
    intermediateInputText.value = intermediateInputText.value.slice(0, -1);
  } else {
    inputText.value = inputText.value.slice(0, -1);
  }
  feedback.value = { kind: 'none' };
}

function showHint() {
  const current = problems.value[currentIndex.value];
  if (!current) return;
  const reduced = reduceWithIntermediates(current.root, intermediates.value);
  const info = firstStepHintInfo(reduced);
  let message: string;
  switch (info.kind) {
    case 'PARENTHESES':
      message = store.t('exprHintParens', info.equation);
      break;
    case 'PRECEDENCE':
      message = store.t('exprHintPrecedence', info.equation);
      break;
    case 'LEFT_TO_RIGHT':
      message = store.t('exprHintLeftToRight', info.equation);
      break;
    default:
      message = store.t('exprHintSolve');
  }
  feedback.value = { kind: 'hint', message };
}

function onSubmit() {
  if (isFinished.value) return;
  if (isSolved.value) {
    onNextProblem();
    return;
  }
  const current = problems.value[currentIndex.value];
  if (!current) return;

  if (selectedNodeId.value !== null) {
    const selectedId = selectedNodeId.value;
    const targetNode = findBinaryNode(current.root, selectedId);
    if (!targetNode || intermediateInputText.value === '') return;
    const given = Number(intermediateInputText.value);
    if (given === evaluateNode(targetNode)) {
      intermediates.value = { ...intermediates.value, [selectedId]: given };
      intermediateOrder.value = [...intermediateOrder.value.filter((id) => id !== selectedId), selectedId];
      if (selectedId === 'root') inputText.value = String(given);
      selectedNodeId.value = null;
      selectedSubExpression.value = '';
      intermediateInputText.value = '';
      feedback.value = { kind: 'none' };
    } else {
      intermediateInputText.value = '';
      feedback.value = {
        kind: 'wrong',
        message: store.t('exprWrongIntermediate', selectedSubExpression.value),
      };
    }
    return;
  }

  if (inputText.value === '') return;
  const given = Number(inputText.value);
  if (given === current.answer) {
    if (!hadMistake.value) correctFirstTry.value += 1;
    feedback.value = { kind: 'correct', message: store.t('feedbackCorrectInspect') };
    isSolved.value = true;
  } else {
    if (!hadMistake.value) {
      hadMistake.value = true;
      store.recordMistake('EXPRESSIONS', current.displayText, current.answer);
    }
    inputText.value = '';
    feedback.value = { kind: 'wrong', message: store.t('exprWrongOrder') };
  }
}

function onNextProblem() {
  if (!isSolved.value || isFinished.value) return;
  if (currentIndex.value + 1 >= problems.value.length) {
    store.recordSession('EXPRESSIONS', problems.value.length, correctFirstTry.value);
    isSolved.value = false;
    isFinished.value = true;
  } else {
    currentIndex.value += 1;
    intermediates.value = {};
    intermediateOrder.value = [];
    selectedNodeId.value = null;
    selectedSubExpression.value = '';
    intermediateInputText.value = '';
    inputText.value = '';
    hadMistake.value = false;
    feedback.value = { kind: 'none' };
    isSolved.value = false;
  }
}
</script>

<template>
  <div class="practice-root">
    <PracticeHeader
      v-if="!isFinished"
      :title="store.t('topicExpressions')"
      :current-index="currentIndex"
      :total-count="problems.length"
      @back="emit('back')"
    />

    <SessionSummary
      v-if="isFinished"
      :topic-title="store.t('topicExpressions')"
      :correct-first-try="correctFirstTry"
      :total-problems="problems.length"
      @restart="startNewSession"
      @back-home="emit('back')"
    />

    <div v-else class="practice-screen">
      <div class="practice-body expr-body">
        <div class="tip-slot">
          <span v-if="Object.keys(intermediates).length === 0 && !selectedNodeId && !isSolved" class="tip-text">
            {{ store.t('exprTipTapOperator') }}
          </span>
        </div>

        <div class="tree-scroll">
          <div v-if="problems[currentIndex]" class="tree-row">
            <ExpressionTreeNode
              :node="problems[currentIndex].root"
              node-id="root"
              :wrap-with-parens="false"
              :intermediates="intermediates"
              :selected-node-id="selectedNodeId"
              :enabled="!isSolved"
              @select-operator="onSelectOperator"
              @select-substituted="onSelectSubstituted"
            />
            <button
              type="button"
              class="equals-btn"
              :class="{ solved: isSolved }"
              :disabled="!selectedNodeId || isSolved"
              @click="onFocusFinalAnswer"
            >
              {{ isSolved ? `= ${inputText}` : '= ?' }}
            </button>
          </div>
        </div>

        <div class="input-label" :class="{ active: selectedNodeId !== null }">
          {{
            selectedNodeId !== null
              ? store.t('exprLabelIntermediate', selectedSubExpression)
              : store.t('exprLabelFinal')
          }}
        </div>

        <div
          class="answer-box"
          :class="{ intermediate: selectedNodeId !== null, empty: !activeInputText }"
        >
          {{ activeInputText || '?' }}
        </div>

        <div class="cancel-slot">
          <button
            v-if="selectedNodeId !== null && !isSolved"
            type="button"
            class="btn-text"
            @click="onCancelIntermediate"
          >
            ✕ {{ store.t('exprBackToFinal') }}
          </button>
        </div>

        <div class="action-slot">
          <button v-if="isSolved" type="button" class="btn-primary" @click="onNextProblem">
            {{ store.t('actionNextProblem') }} →
          </button>
          <template v-else>
            <button type="button" class="btn-outline" @click="showHint">
              💡 {{ store.t('actionHint') }}
            </button>
            <button
              v-if="intermediateOrder.length > 0"
              type="button"
              class="btn-outline"
              @click="onUndoIntermediate"
            >
              ↶ {{ store.t('actionUndoStep') }}
            </button>
          </template>
        </div>

        <div class="feedback-slot">
          <FeedbackBanner :state="feedback" />
        </div>
      </div>

      <MathKeypad
        :submit-enabled="activeInputText.length > 0"
        @digit="onDigit"
        @backspace="onBackspace"
        @submit="onSubmit"
      />
    </div>
  </div>
</template>

<style scoped>
.practice-root {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.expr-body {
  justify-content: flex-start;
  padding-top: 8px;
}

.tip-slot {
  height: 20px;
  display: flex;
  align-items: center;
}

.tip-text {
  font-size: 12px;
  color: var(--text-muted);
}

.tree-scroll {
  width: 100%;
  min-height: 152px;
  overflow-x: auto;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  margin-top: 8px;
  margin-bottom: 6px;
}

.tree-row {
  display: inline-flex;
  align-items: flex-start;
  gap: 4px;
  padding: 0 8px;
}

.equals-btn {
  height: 40px;
  border: none;
  background: transparent;
  color: var(--text-muted);
  font-size: 26px;
  font-weight: 700;
  padding: 0 4px;
}

.equals-btn.solved {
  color: var(--primary);
}

.input-label {
  height: 22px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-muted);
  display: flex;
  align-items: center;
}

.input-label.active {
  color: var(--primary);
}

.answer-box {
  width: 65%;
  max-width: 240px;
  height: 60px;
  margin-top: 6px;
  border-radius: 16px;
  border: 2px solid var(--primary);
  background: var(--surface);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  font-weight: 800;
}

.answer-box.intermediate {
  border-color: var(--hint);
  background: color-mix(in srgb, var(--hint-container) 45%, var(--surface));
}

.answer-box.empty {
  color: var(--border);
}

.cancel-slot {
  height: 36px;
  display: flex;
  align-items: center;
}

.action-slot {
  height: 44px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.feedback-slot {
  width: 100%;
  min-height: 68px;
  margin-top: 8px;
  display: flex;
  align-items: flex-start;
}

@media (max-height: 700px) {
  .tree-scroll {
    min-height: 136px;
  }
  .answer-box {
    height: 52px;
  }
}
</style>
