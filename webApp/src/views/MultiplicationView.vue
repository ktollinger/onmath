<script setup lang="ts">
import { ref } from 'vue';
import FeedbackBanner, { type FeedbackState } from '../components/FeedbackBanner.vue';
import MathKeypad from '../components/MathKeypad.vue';
import PracticeHeader from '../components/PracticeHeader.vue';
import SessionSummary from '../components/SessionSummary.vue';
import { generateMultiplicationSet, type MultiplicationProblem } from '../domain/multiplication';
import { store } from '../state/store';

const emit = defineEmits<{
  back: [];
}>();

const problems = ref<MultiplicationProblem[]>([]);
const currentIndex = ref(0);
const inputText = ref('');
const correctFirstTry = ref(0);
const hadMistake = ref(false);
const feedback = ref<FeedbackState>({ kind: 'none' });
const isSolved = ref(false);
const isFinished = ref(false);

function startNewSession() {
  problems.value = generateMultiplicationSet(
    {
      selectedTables: store.settings.multiplicationTables,
      includeDivision: store.settings.multiplicationIncludeDivision,
    },
    10,
  );
  currentIndex.value = 0;
  inputText.value = '';
  correctFirstTry.value = 0;
  hadMistake.value = false;
  feedback.value = { kind: 'none' };
  isSolved.value = false;
  isFinished.value = false;
}

startNewSession();

function onDigit(d: number) {
  if (isSolved.value || isFinished.value || inputText.value.length >= 4) return;
  const next = `${inputText.value}${d}`.replace(/^0+/, '');
  inputText.value = next || '0';
  feedback.value = { kind: 'none' };
}

function onBackspace() {
  if (isSolved.value || isFinished.value) return;
  inputText.value = inputText.value.slice(0, -1);
  feedback.value = { kind: 'none' };
}

function showHint() {
  const current = problems.value[currentIndex.value];
  if (!current) return;
  const message =
    current.operation === 'MULTIPLY'
      ? store.t('multHintMultiply', current.left, current.right)
      : store.t('multHintDivide', current.right, current.left);
  feedback.value = { kind: 'hint', message };
}

function onSubmit() {
  if (isFinished.value) return;
  if (isSolved.value) {
    onNextProblem();
    return;
  }
  const current = problems.value[currentIndex.value];
  if (!current || inputText.value === '') return;

  const given = Number(inputText.value);
  if (given === current.answer) {
    if (!hadMistake.value) correctFirstTry.value += 1;
    feedback.value = { kind: 'correct', message: store.t('feedbackCorrectInspect') };
    isSolved.value = true;
  } else {
    if (!hadMistake.value) {
      hadMistake.value = true;
      store.recordMistake('MULTIPLICATION', current.displayText, current.answer);
    }
    inputText.value = '';
    feedback.value = { kind: 'wrong', message: store.t('multWrong') };
  }
}

function onNextProblem() {
  if (!isSolved.value || isFinished.value) return;
  if (currentIndex.value + 1 >= problems.value.length) {
    store.recordSession('MULTIPLICATION', problems.value.length, correctFirstTry.value);
    isSolved.value = false;
    isFinished.value = true;
  } else {
    currentIndex.value += 1;
    hadMistake.value = false;
    inputText.value = '';
    feedback.value = { kind: 'none' };
    isSolved.value = false;
  }
}
</script>

<template>
  <div class="practice-root">
    <PracticeHeader
      v-if="!isFinished"
      :title="store.t('topicMultiplication')"
      :current-index="currentIndex"
      :total-count="problems.length"
      @back="emit('back')"
    />

    <SessionSummary
      v-if="isFinished"
      :topic-title="store.t('topicMultiplication')"
      :correct-first-try="correctFirstTry"
      :total-problems="problems.length"
      @restart="startNewSession"
      @back-home="emit('back')"
    />

    <div v-else class="practice-screen">
      <div class="practice-body mult-body">
        <div class="problem-display">{{ problems[currentIndex]?.displayText }}</div>

        <div class="answer-box" :class="{ empty: !inputText }">
          {{ inputText || '?' }}
        </div>

        <div class="action-slot">
          <button v-if="isSolved" type="button" class="btn-primary" @click="onNextProblem">
            {{ store.t('actionNextProblem') }} →
          </button>
          <button v-else type="button" class="btn-outline" @click="showHint">
            💡 {{ store.t('actionHint') }}
          </button>
        </div>

        <div class="feedback-slot">
          <FeedbackBanner :state="feedback" />
        </div>
      </div>

      <MathKeypad
        :submit-enabled="inputText.length > 0"
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

.mult-body {
  justify-content: center;
  gap: 18px;
}

.problem-display {
  font-size: 36px;
  font-weight: 800;
  text-align: center;
}

.answer-box {
  width: 65%;
  max-width: 240px;
  height: 64px;
  border-radius: 16px;
  border: 2px solid var(--primary);
  background: var(--surface);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 800;
}

.answer-box.empty {
  color: var(--border);
}

.action-slot {
  height: 44px;
  display: flex;
  align-items: center;
}

.feedback-slot {
  width: 100%;
  min-height: 58px;
  display: flex;
  align-items: flex-start;
}
</style>
