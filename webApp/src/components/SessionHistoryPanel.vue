<script setup lang="ts">
import { computed, ref } from 'vue';
import { store } from '../state/store';

export interface SolvedItem {
  expression: string;
  answer: number;
  hadMistake: boolean;
}

const props = defineProps<{
  items: SolvedItem[];
  totalCount: number;
}>();

const mobileExpanded = ref(false);
const lastItem = computed(() => props.items[props.items.length - 1]);
</script>

<template>
  <aside class="history-aside">
    <!-- Desktop card -->
    <div class="history-desktop">
      <div class="history-header">
        <span>{{ store.t('historyPanelTitle', items.length, totalCount) }}</span>
      </div>
      <div v-if="items.length === 0" class="history-empty">
        {{ store.t('historyPanelEmpty') }}
      </div>
      <ol v-else class="history-list">
        <li
          v-for="(item, idx) in items"
          :key="idx"
          class="history-item"
          :class="{ latest: idx === items.length - 1 }"
        >
          <span class="history-expr">{{ item.expression }} = <strong>{{ item.answer }}</strong></span>
          <span
            class="history-badge"
            :class="item.hadMistake ? 'badge-warn' : 'badge-ok'"
            :title="item.hadMistake ? store.t('historyWithCorrection') : store.t('historyFirstTry')"
          >
            {{ item.hadMistake ? '↻' : '✓' }}
          </span>
        </li>
      </ol>
    </div>

    <!-- Mobile compact collapsible bar -->
    <div v-if="items.length > 0" class="history-mobile">
      <button
        type="button"
        class="history-mobile-toggle"
        @click="mobileExpanded = !mobileExpanded"
      >
        <span class="history-mobile-last">
          {{ store.t('historyLastSolved', lastItem.expression, lastItem.answer) }}
        </span>
        <span class="history-badge" :class="lastItem.hadMistake ? 'badge-warn' : 'badge-ok'">
          {{ lastItem.hadMistake ? '↻' : '✓' }}
        </span>
        <span class="history-mobile-count">{{ items.length }}/{{ totalCount }}</span>
      </button>

      <ol v-if="mobileExpanded" class="history-list history-mobile-list">
        <li v-for="(item, idx) in items" :key="idx" class="history-item">
          <span class="history-expr">{{ item.expression }} = <strong>{{ item.answer }}</strong></span>
          <span class="history-badge" :class="item.hadMistake ? 'badge-warn' : 'badge-ok'">
            {{ item.hadMistake ? '↻' : '✓' }}
          </span>
        </li>
      </ol>
    </div>
  </aside>
</template>

<style scoped>
.history-aside {
  width: 100%;
}

.history-desktop {
  display: none;
}

.history-mobile {
  padding: 0 16px 8px;
}

.history-mobile-toggle {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 12px;
  border: 1px solid var(--border);
  background: var(--surface);
  color: var(--text);
  font-size: 13px;
}

.history-mobile-last {
  flex: 1;
  text-align: left;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-mobile-count {
  font-size: 12px;
  color: var(--text-muted);
  font-weight: 600;
}

.history-mobile-list {
  margin-top: 8px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 8px 12px;
}

.history-header {
  font-size: 14px;
  font-weight: 700;
  color: var(--primary);
  margin-bottom: 10px;
}

.history-empty {
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.45;
}

.history-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.history-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 7px 10px;
  border-radius: 10px;
  background: var(--surface-alt);
  font-size: 14px;
}

.history-item.latest {
  border: 1.5px solid var(--primary);
  background: color-mix(in srgb, var(--primary-container) 45%, var(--surface));
}

.history-expr {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-badge {
  width: 22px;
  height: 22px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 800;
  flex-shrink: 0;
}

.badge-ok {
  background: var(--correct-container);
  color: var(--correct);
}

.badge-warn {
  background: var(--hint-container);
  color: var(--hint);
}

@media (min-width: 860px) {
  .history-aside {
    width: 310px;
    flex-shrink: 0;
    padding: 16px 0 14px;
    display: flex;
    flex-direction: column;
  }

  .history-mobile {
    display: none;
  }

  .history-desktop {
    display: flex;
    flex-direction: column;
    flex: 1;
    background: var(--surface);
    border: 1px solid var(--border);
    border-radius: 18px;
    padding: 16px;
  }
}
</style>
