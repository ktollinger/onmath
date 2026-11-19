<script setup lang="ts">
import { computed } from 'vue';
import { store } from '../state/store';

const props = defineProps<{
  topicTitle: string;
  correctFirstTry: number;
  totalProblems: number;
}>();

const emit = defineEmits<{
  restart: [];
  backHome: [];
}>();

const accuracy = computed(() =>
  props.totalProblems === 0 ? 0 : Math.round((props.correctFirstTry * 100) / props.totalProblems),
);

const stars = computed(() => {
  if (accuracy.value >= 90) return 3;
  if (accuracy.value >= 70) return 2;
  if (accuracy.value >= 40) return 1;
  return 0;
});
</script>

<template>
  <div class="summary">
    <div class="stars">
      <span v-for="i in 3" :key="i" class="star" :class="{ active: i <= stars }">★</span>
    </div>
    <h2 class="summary-title">{{ store.t('summaryTitle') }}</h2>
    <p class="summary-topic">{{ topicTitle }}</p>

    <div class="summary-card">
      <div class="summary-score">{{ correctFirstTry }} / {{ totalProblems }}</div>
      <div class="summary-sub">{{ store.t('summaryFirstTry', accuracy) }}</div>
    </div>

    <div class="summary-actions">
      <button type="button" class="btn-primary summary-btn" @click="emit('restart')">
        {{ store.t('summaryRestart') }}
      </button>
      <button type="button" class="btn-outline summary-btn" @click="emit('backHome')">
        {{ store.t('summaryBackHome') }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.summary {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
  text-align: center;
}

.stars {
  display: flex;
  gap: 8px;
  font-size: 42px;
  margin-bottom: 12px;
}

.star {
  color: var(--border);
}

.star.active {
  color: var(--hint);
}

.summary-title {
  margin: 0 0 6px;
  font-size: 26px;
}

.summary-topic {
  margin: 0 0 22px;
  color: var(--text-muted);
  font-weight: 600;
}

.summary-card {
  width: 100%;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 24px;
}

.summary-score {
  font-size: 36px;
  font-weight: 800;
  color: var(--primary);
}

.summary-sub {
  margin-top: 4px;
  color: var(--text-muted);
}

.summary-actions {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.summary-btn {
  width: 100%;
  justify-content: center;
  height: 48px;
}
</style>
