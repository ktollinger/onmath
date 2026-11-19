<script setup lang="ts">
import { computed, ref } from 'vue';
import FeedbackBanner, { type FeedbackState } from '../components/FeedbackBanner.vue';
import MathKeypad from '../components/MathKeypad.vue';
import PracticeHeader from '../components/PracticeHeader.vue';
import SessionSummary from '../components/SessionSummary.vue';
import { generateSubtractionSet, type SubtractionProblem } from '../domain/subtraction';
import { store } from '../state/store';

const emit = defineEmits<{
  back: [];
}>();

const problems = ref<SubtractionProblem[]>([]);
const currentIndex = ref(0);
const resultDigits = ref<Record<number, number>>({});
const activePlaceIndex = ref(0);
const markedBorrows = ref<number[]>([]);
const wrongPlaces = ref<number[]>([]);
const hadMistake = ref(false);
const correctFirstTry = ref(0);
const feedback = ref<FeedbackState>({ kind: 'none' });
const isSolved = ref(false);
const isFinished = ref(false);

function startNewSession() {
  problems.value = generateSubtractionSet(
    {
      minDigits: store.settings.subtractionMinDigits,
      maxDigits: store.settings.subtractionMaxDigits,
    },
    10,
  );
  currentIndex.value = 0;
  resultDigits.value = {};
  activePlaceIndex.value = 0;
  markedBorrows.value = [];
  wrongPlaces.value = [];
  hadMistake.value = false;
  correctFirstTry.value = 0;
  feedback.value = { kind: 'none' };
  isSolved.value = false;
  isFinished.value = false;
}

startNewSession();

const currentProblem = computed(() => problems.value[currentIndex.value]);
const colsLeftToRight = computed(() =>
  currentProblem.value ? [...currentProblem.value.columnsRightToLeft].reverse() : [],
);
const subtrahendDigitsCount = computed(() =>
  currentProblem.value ? String(currentProblem.value.subtrahend).length : 0,
);
const hasDigits = computed(() => Object.keys(resultDigits.value).length > 0);

function toggleBorrow(place: number) {
  if (isSolved.value || isFinished.value || place <= 0) return;
  if (markedBorrows.value.includes(place)) {
    markedBorrows.value = markedBorrows.value.filter((p) => p !== place);
  } else {
    markedBorrows.value = [...markedBorrows.value, place];
  }
}

function selectPlace(place: number) {
  if (isSolved.value || isFinished.value) return;
  activePlaceIndex.value = place;
  feedback.value = { kind: 'none' };
}

function onDigit(digit: number) {
  const p = currentProblem.value;
  if (!p || isSolved.value || isFinished.value) return;
  resultDigits.value = { ...resultDigits.value, [activePlaceIndex.value]: digit };
  wrongPlaces.value = wrongPlaces.value.filter((w) => w !== activePlaceIndex.value);
  activePlaceIndex.value = Math.min(activePlaceIndex.value + 1, p.digitCount - 1);
  feedback.value = { kind: 'none' };
}

function onBackspace() {
  if (isSolved.value || isFinished.value) return;
  const targetPlace =
    resultDigits.value[activePlaceIndex.value] !== undefined
      ? activePlaceIndex.value
      : Math.max(0, activePlaceIndex.value - 1);
  const copy = { ...resultDigits.value };
  delete copy[targetPlace];
  resultDigits.value = copy;
  wrongPlaces.value = wrongPlaces.value.filter((w) => w !== targetPlace);
  activePlaceIndex.value = targetPlace;
  feedback.value = { kind: 'none' };
}

function showHint() {
  const p = currentProblem.value;
  if (!p) return;
  const col = p.columnsRightToLeft[activePlaceIndex.value];
  if (!col) return;

  const placeKeys = [
    'placeOnes',
    'placeTens',
    'placeHundreds',
    'placeThousands',
    'placeTenThousands',
    'placeHundredThousands',
  ] as const;
  const placeName =
    col.placeIndex < placeKeys.length
      ? store.t(placeKeys[col.placeIndex])
      : store.t('placeOther', col.placeIndex + 1);

  let message: string;
  if (store.settings.subtractionMethod === 'BORROW_TOP') {
    const topAfterLend = col.effectiveUpperAfterLend;
    message = col.requiresBorrowFromNext
      ? store.t('subHintBorrowTopWithBorrow', placeName, topAfterLend + 10, col.subtrahendDigit, col.expectedResultDigit)
      : store.t('subHintBorrowTopPlain', placeName, topAfterLend, col.subtrahendDigit, col.expectedResultDigit);
  } else {
    const bottomWithCarry = col.effectiveLowerWithCarry;
    message = col.requiresBorrowFromNext
      ? store.t('subHintCarryBottomWithCarry', placeName, col.minuendDigit + 10, bottomWithCarry, col.expectedResultDigit)
      : store.t('subHintCarryBottomPlain', placeName, col.minuendDigit, bottomWithCarry, col.expectedResultDigit);
  }
  feedback.value = { kind: 'hint', message };
}

function onSubmit() {
  if (isFinished.value) return;
  if (isSolved.value) {
    onNextProblem();
    return;
  }
  const p = currentProblem.value;
  if (!p || !hasDigits.value) return;

  let givenAnswer = 0;
  let power = 1;
  for (let place = 0; place < p.digitCount; place++) {
    givenAnswer += (resultDigits.value[place] ?? 0) * power;
    power *= 10;
  }

  if (givenAnswer === p.answer) {
    if (!hadMistake.value) correctFirstTry.value += 1;
    wrongPlaces.value = [];
    feedback.value = { kind: 'correct', message: store.t('feedbackCorrectInspect') };
    isSolved.value = true;
  } else {
    const wrong = p.columnsRightToLeft
      .filter((col) => (resultDigits.value[col.placeIndex] ?? 0) !== col.expectedResultDigit)
      .map((col) => col.placeIndex);
    if (!hadMistake.value) {
      hadMistake.value = true;
      store.recordMistake('SUBTRACTION', `${p.minuend} − ${p.subtrahend}`, p.answer);
    }
    wrongPlaces.value = wrong;
    activePlaceIndex.value = wrong.length > 0 ? Math.min(...wrong) : 0;
    feedback.value = { kind: 'wrong', message: store.t('subWrongColumns') };
  }
}

function onNextProblem() {
  if (!isSolved.value || isFinished.value) return;
  if (currentIndex.value + 1 >= problems.value.length) {
    store.recordSession('SUBTRACTION', problems.value.length, correctFirstTry.value);
    isSolved.value = false;
    isFinished.value = true;
  } else {
    currentIndex.value += 1;
    resultDigits.value = {};
    activePlaceIndex.value = 0;
    markedBorrows.value = [];
    wrongPlaces.value = [];
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
      :title="store.t('topicSubtraction')"
      :current-index="currentIndex"
      :total-count="problems.length"
      @back="emit('back')"
    />

    <SessionSummary
      v-if="isFinished"
      :topic-title="store.t('topicSubtraction')"
      :correct-first-try="correctFirstTry"
      :total-problems="problems.length"
      @restart="startNewSession"
      @back-home="emit('back')"
    />

    <div v-else-if="currentProblem" class="practice-screen">
      <div class="practice-body sub-body">
        <div class="board" :class="{ compact: currentProblem.digitCount >= 6 }">
          <!-- BORROW_TOP helper row -->
          <div v-if="store.settings.subtractionMethod === 'BORROW_TOP'" class="board-row">
            <span class="op-spacer" />
            <button
              v-for="col in colsLeftToRight"
              :key="`bt-${col.placeIndex}`"
              type="button"
              class="borrow-cell"
              :class="{
                clickable: col.placeIndex > 0,
                marked: markedBorrows.includes(col.placeIndex),
              }"
              :disabled="col.placeIndex === 0"
              @click="toggleBorrow(col.placeIndex)"
            >
              <span v-if="col.placeIndex > 0 && markedBorrows.includes(col.placeIndex)">
                {{ Math.max(0, col.minuendDigit - 1) }}
              </span>
            </button>
          </div>

          <!-- Minuend row -->
          <div class="board-row">
            <span class="op-spacer" />
            <button
              v-for="col in colsLeftToRight"
              :key="`m-${col.placeIndex}`"
              type="button"
              class="digit-cell"
              :class="{
                crossed: store.settings.subtractionMethod === 'BORROW_TOP' && col.placeIndex > 0 && markedBorrows.includes(col.placeIndex),
              }"
              :disabled="!(store.settings.subtractionMethod === 'BORROW_TOP' && col.placeIndex > 0)"
              @click="toggleBorrow(col.placeIndex)"
            >
              {{ col.minuendDigit }}
            </button>
          </div>

          <!-- Subtrahend row -->
          <div class="board-row">
            <span class="op-spacer minus">−</span>
            <button
              v-for="col in colsLeftToRight"
              :key="`s-${col.placeIndex}`"
              type="button"
              class="digit-cell"
              :disabled="!(store.settings.subtractionMethod === 'CARRY_BOTTOM' && col.placeIndex > 0)"
              @click="toggleBorrow(col.placeIndex)"
            >
              <span>{{ col.placeIndex < subtrahendDigitsCount ? col.subtrahendDigit : '' }}</span>
              <span
                v-if="store.settings.subtractionMethod === 'CARRY_BOTTOM' && col.placeIndex > 0 && markedBorrows.includes(col.placeIndex)"
                class="carry-badge"
              >
                1
              </span>
            </button>
          </div>

          <div class="board-line" />

          <!-- Result row -->
          <div class="board-row">
            <span class="op-spacer" />
            <button
              v-for="col in colsLeftToRight"
              :key="`r-${col.placeIndex}`"
              type="button"
              class="result-cell"
              :class="{
                active: col.placeIndex === activePlaceIndex && !isSolved,
                wrong: wrongPlaces.includes(col.placeIndex),
              }"
              @click="selectPlace(col.placeIndex)"
            >
              {{ resultDigits[col.placeIndex] ?? '' }}
            </button>
          </div>
        </div>

        <p class="tip-text">
          {{
            store.settings.subtractionMethod === 'BORROW_TOP'
              ? store.t('subTipBorrowTop')
              : store.t('subTipCarryBottom')
          }}
        </p>

        <div class="action-slot">
          <button v-if="isSolved" type="button" class="btn-primary" @click="onNextProblem">
            {{ store.t('actionNextProblem') }} →
          </button>
          <button v-else type="button" class="btn-outline" @click="showHint">
            💡 {{ store.t('actionHintColumn') }}
          </button>
        </div>

        <div class="feedback-slot">
          <FeedbackBanner :state="feedback" />
        </div>
      </div>

      <MathKeypad
        :submit-enabled="hasDigits"
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

.sub-body {
  justify-content: flex-start;
  padding-top: 14px;
  gap: 10px;
}

.board {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  --cell-w: 48px;
}

.board.compact {
  --cell-w: 42px;
}

.board-row {
  display: flex;
  align-items: center;
  gap: 4px;
}

.op-spacer {
  width: 26px;
  text-align: center;
  font-size: 28px;
  font-weight: 700;
}

.op-spacer.minus {
  color: var(--primary);
}

.borrow-cell {
  width: var(--cell-w);
  height: 24px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: var(--primary);
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 16px;
  font-weight: 700;
}

.borrow-cell.clickable {
  background: color-mix(in srgb, var(--border) 45%, transparent);
}

.borrow-cell.marked {
  background: var(--primary-container);
}

.digit-cell {
  position: relative;
  width: var(--cell-w);
  height: 38px;
  border: none;
  background: transparent;
  color: var(--text);
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 30px;
  font-weight: 700;
}

.digit-cell.crossed {
  text-decoration: line-through;
  color: var(--text-muted);
}

.carry-badge {
  position: absolute;
  right: 2px;
  bottom: 0;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--primary);
  color: #ffffff;
  font-size: 11px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.board-line {
  width: 100%;
  height: 2.5px;
  background: var(--text);
  margin: 2px 0;
}

.result-cell {
  width: var(--cell-w);
  height: 50px;
  border-radius: 10px;
  border: 1.5px solid var(--border);
  background: var(--surface);
  color: var(--text);
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 28px;
  font-weight: 700;
}

.result-cell.active {
  border-color: var(--primary);
  border-width: 2px;
  background: color-mix(in srgb, var(--primary) 16%, var(--surface));
}

.result-cell.wrong {
  border-color: var(--error);
  border-width: 2px;
  background: var(--error-container);
  color: var(--error);
}

.tip-text {
  margin: 0;
  font-size: 12px;
  color: var(--text-muted);
  text-align: center;
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
