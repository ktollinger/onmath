<script setup lang="ts">
export type FeedbackState =
  | { kind: 'none' }
  | { kind: 'correct'; message: string }
  | { kind: 'wrong'; message: string }
  | { kind: 'hint'; message: string };

defineProps<{
  state: FeedbackState;
}>();
</script>

<template>
  <div v-if="state.kind !== 'none'" class="banner" :class="`banner-${state.kind}`">
    <span v-if="state.kind === 'correct'" class="banner-icon">✓</span>
    <span v-else-if="state.kind === 'hint'" class="banner-icon">💡</span>
    <span>{{ state.message }}</span>
  </div>
</template>

<style scoped>
.banner {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.35;
}

.banner-icon {
  flex-shrink: 0;
  font-size: 16px;
}

.banner-correct {
  background: var(--correct-container);
  color: var(--correct);
}

.banner-wrong {
  background: var(--error-container);
  color: var(--error);
}

.banner-hint {
  background: var(--hint-container);
  color: var(--hint);
}
</style>
