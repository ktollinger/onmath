<script setup lang="ts">
import { store } from '../state/store';

defineProps<{
  title: string;
  currentIndex: number;
  totalCount: number;
}>();

const emit = defineEmits<{
  back: [];
}>();
</script>

<template>
  <header class="header">
    <div class="top-bar">
      <div class="top-bar-left">
        <button type="button" class="icon-btn" :aria-label="store.t('back')" @click="emit('back')">
          ←
        </button>
        <h1 class="header-title">{{ title }}</h1>
      </div>
      <span class="counter">{{ Math.min(currentIndex + 1, Math.max(1, totalCount)) }} / {{ totalCount }}</span>
    </div>
    <div class="progress-track">
      <div
        class="progress-fill"
        :style="{ width: `${totalCount === 0 ? 0 : (currentIndex / totalCount) * 100}%` }"
      />
    </div>
  </header>
</template>

<style scoped>
.header {
  width: 100%;
}

.header-title {
  margin: 0;
  font-size: 19px;
  font-weight: 700;
}

.counter {
  font-size: 15px;
  font-weight: 600;
  color: var(--primary);
}

.progress-track {
  width: 100%;
  height: 4px;
  background: var(--border);
}

.progress-fill {
  height: 100%;
  background: var(--primary);
  transition: width 0.2s ease;
}
</style>
