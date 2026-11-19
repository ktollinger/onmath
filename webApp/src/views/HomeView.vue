<script setup lang="ts">
import { computed } from 'vue';
import type { MathTopic } from '../domain/models';
import { store } from '../state/store';

const emit = defineEmits<{
  openTopic: [topic: MathTopic];
  openSettings: [];
}>();

const tablesDesc = computed(() => {
  const base =
    store.settings.multiplicationTables.length === 10
      ? store.t('homeTablesAll')
      : store.t('homeTablesSelected', [...store.settings.multiplicationTables].sort((a, b) => a - b).join(', '));
  return base + (store.settings.multiplicationIncludeDivision ? store.t('homeWithDivision') : '');
});

const expressionsDesc = computed(() =>
  store.t(store.settings.expressionWithParentheses ? 'homeExprWithParens' : 'homeExprWithoutParens'),
);

const subtractionDesc = computed(() => {
  const method = store.t(
    store.settings.subtractionMethod === 'BORROW_TOP' ? 'homeMethodBorrowTop' : 'homeMethodCarryBottom',
  );
  return store.t(
    'homeSubtractionSubtitle',
    store.settings.subtractionMinDigits,
    store.settings.subtractionMaxDigits,
    method,
  );
});

const multStats = computed(() => store.statsFor('MULTIPLICATION'));
const exprStats = computed(() => store.statsFor('EXPRESSIONS'));
const subStats = computed(() => store.statsFor('SUBTRACTION'));
const recentMistakes = computed(() => store.mistakes.slice(0, 5));
</script>

<template>
  <div class="home">
    <div class="top-bar">
      <h1 class="brand">{{ store.t('appName') }}</h1>
      <button
        type="button"
        class="icon-btn"
        :aria-label="store.t('settingsTitle')"
        @click="emit('openSettings')"
      >
        ⚙
      </button>
    </div>

    <div class="home-content">
      <p class="subtitle">{{ store.t('homeSubtitle') }}</p>

      <button type="button" class="topic-card" @click="emit('openTopic', 'MULTIPLICATION')">
        <div class="topic-icon">×</div>
        <div class="topic-info">
          <div class="topic-title">{{ store.t('topicMultiplication') }}</div>
          <div class="topic-sub">{{ tablesDesc }}</div>
          <div v-if="multStats" class="topic-stats">
            {{ store.t('homeTopicStats', multStats.sessionCount, multStats.accuracyPercent) }}
          </div>
        </div>
        <span class="chevron">›</span>
      </button>

      <button type="button" class="topic-card" @click="emit('openTopic', 'EXPRESSIONS')">
        <div class="topic-icon">( )</div>
        <div class="topic-info">
          <div class="topic-title">{{ store.t('topicExpressions') }}</div>
          <div class="topic-sub">{{ expressionsDesc }}</div>
          <div v-if="exprStats" class="topic-stats">
            {{ store.t('homeTopicStats', exprStats.sessionCount, exprStats.accuracyPercent) }}
          </div>
        </div>
        <span class="chevron">›</span>
      </button>

      <button type="button" class="topic-card" @click="emit('openTopic', 'SUBTRACTION')">
        <div class="topic-icon">−</div>
        <div class="topic-info">
          <div class="topic-title">{{ store.t('topicSubtraction') }}</div>
          <div class="topic-sub">{{ subtractionDesc }}</div>
          <div v-if="subStats" class="topic-stats">
            {{ store.t('homeTopicStats', subStats.sessionCount, subStats.accuracyPercent) }}
          </div>
        </div>
        <span class="chevron">›</span>
      </button>

      <div v-if="recentMistakes.length > 0" class="mistakes-section">
        <div class="mistakes-title">{{ store.t('homeRecentMistakes') }}</div>
        <div class="mistakes-card">
          <div v-for="m in recentMistakes" :key="m.id" class="mistake-row">
            <span>{{ m.displayText }}</span>
            <span class="mistake-answer">{{ store.t('homeCorrectAnswer', m.expectedAnswer) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.brand {
  margin: 0;
  font-size: 26px;
  font-weight: 800;
}

.home-content {
  padding: 0 16px 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.subtitle {
  margin: 0 0 4px;
  color: var(--text-muted);
  font-weight: 600;
}

.topic-card {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border-radius: 18px;
  border: 1px solid var(--border);
  background: var(--surface);
  color: var(--text);
  text-align: left;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.04);
}

.topic-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: var(--primary-container);
  color: var(--on-primary-container);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 800;
  flex-shrink: 0;
}

.topic-info {
  flex: 1;
  min-width: 0;
}

.topic-title {
  font-size: 18px;
  font-weight: 700;
}

.topic-sub {
  margin-top: 2px;
  font-size: 14px;
  color: var(--text-muted);
}

.topic-stats {
  margin-top: 6px;
  font-size: 12px;
  font-weight: 700;
  color: var(--primary);
}

.chevron {
  font-size: 22px;
  color: var(--text-muted);
}

.mistakes-section {
  margin-top: 8px;
}

.mistakes-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-muted);
  margin-bottom: 8px;
}

.mistakes-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mistake-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.mistake-answer {
  font-weight: 700;
  color: var(--primary);
}
</style>
