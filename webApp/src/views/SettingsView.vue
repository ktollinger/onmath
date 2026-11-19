<script setup lang="ts">
import type { AppLanguage, AppTheme } from '../domain/models';
import { store } from '../state/store';

const emit = defineEmits<{
  back: [];
}>();

const languages: Array<{ value: AppLanguage; labelKey: 'settingsLanguageSystem' | 'settingsLanguageCzech' | 'settingsLanguageEnglish' }> = [
  { value: 'SYSTEM', labelKey: 'settingsLanguageSystem' },
  { value: 'CZECH', labelKey: 'settingsLanguageCzech' },
  { value: 'ENGLISH', labelKey: 'settingsLanguageEnglish' },
];

const themes: Array<{ value: AppTheme; labelKey: 'settingsThemeSystem' | 'settingsThemeLight' | 'settingsThemeDark' }> = [
  { value: 'SYSTEM', labelKey: 'settingsThemeSystem' },
  { value: 'LIGHT', labelKey: 'settingsThemeLight' },
  { value: 'DARK', labelKey: 'settingsThemeDark' },
];

function toggleTable(table: number) {
  const current = store.settings.multiplicationTables;
  if (current.includes(table)) {
    if (current.length > 1) {
      store.settings.multiplicationTables = current.filter((t) => t !== table);
    }
  } else {
    store.settings.multiplicationTables = [...current, table].sort((a, b) => a - b);
  }
}

function setMinDigits(value: number) {
  store.settings.subtractionMinDigits = value;
  if (store.settings.subtractionMaxDigits < value) {
    store.settings.subtractionMaxDigits = value;
  }
}
</script>

<template>
  <div class="settings">
    <div class="top-bar">
      <div class="top-bar-left">
        <button type="button" class="icon-btn" :aria-label="store.t('back')" @click="emit('back')">
          ←
        </button>
        <h1 class="title">{{ store.t('settingsTitle') }}</h1>
      </div>
    </div>

    <div class="settings-list">
      <section class="section">
        <h2 class="section-title">{{ store.t('settingsLanguageSection') }}</h2>
        <div class="chip-row">
          <button
            v-for="lang in languages"
            :key="lang.value"
            type="button"
            class="chip"
            :class="{ selected: store.settings.language === lang.value }"
            @click="store.settings.language = lang.value"
          >
            {{ store.t(lang.labelKey) }}
          </button>
        </div>
      </section>

      <section class="section">
        <h2 class="section-title">{{ store.t('settingsThemeSection') }}</h2>
        <div class="chip-row">
          <button
            v-for="theme in themes"
            :key="theme.value"
            type="button"
            class="chip"
            :class="{ selected: store.settings.theme === theme.value }"
            @click="store.settings.theme = theme.value"
          >
            {{ store.t(theme.labelKey) }}
          </button>
        </div>
      </section>

      <section class="section">
        <h2 class="section-title">{{ store.t('settingsSubtractionSection') }}</h2>
        <div class="sub-label">{{ store.t('settingsSubtractionMethod') }}</div>

        <label class="radio-card" @click="store.settings.subtractionMethod = 'BORROW_TOP'">
          <input
            type="radio"
            name="subMethod"
            :checked="store.settings.subtractionMethod === 'BORROW_TOP'"
          />
          <div>
            <div class="radio-title">{{ store.t('settingsMethodBorrowTopTitle') }}</div>
            <div class="radio-sub">{{ store.t('settingsMethodBorrowTopSubtitle') }}</div>
          </div>
        </label>

        <label class="radio-card" @click="store.settings.subtractionMethod = 'CARRY_BOTTOM'">
          <input
            type="radio"
            name="subMethod"
            :checked="store.settings.subtractionMethod === 'CARRY_BOTTOM'"
          />
          <div>
            <div class="radio-title">{{ store.t('settingsMethodCarryBottomTitle') }}</div>
            <div class="radio-sub">{{ store.t('settingsMethodCarryBottomSubtitle') }}</div>
          </div>
        </label>

        <div class="sub-label">
          {{ store.t('settingsDigitCount', store.settings.subtractionMinDigits, store.settings.subtractionMaxDigits) }}
        </div>
        <div class="digit-row">
          <span>{{ store.t('settingsMinDigits') }}</span>
          <div class="chip-row">
            <button
              v-for="n in [2, 3, 4, 5]"
              :key="n"
              type="button"
              class="chip chip-sm"
              :class="{ selected: store.settings.subtractionMinDigits === n }"
              @click="setMinDigits(n)"
            >
              {{ n }}
            </button>
          </div>
        </div>
        <div class="digit-row">
          <span>{{ store.t('settingsMaxDigits') }}</span>
          <div class="chip-row">
            <button
              v-for="n in [2, 3, 4, 5, 6]"
              :key="n"
              type="button"
              class="chip chip-sm"
              :disabled="n < store.settings.subtractionMinDigits"
              :class="{ selected: store.settings.subtractionMaxDigits === n }"
              @click="store.settings.subtractionMaxDigits = n"
            >
              {{ n }}
            </button>
          </div>
        </div>
      </section>

      <section class="section">
        <div class="section-header">
          <h2 class="section-title">{{ store.t('settingsMultiplicationSection') }}</h2>
          <button
            type="button"
            class="btn-text"
            @click="store.settings.multiplicationTables = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]"
          >
            {{ store.t('settingsSelectAll') }}
          </button>
        </div>
        <div class="tables-grid">
          <button
            v-for="t in 10"
            :key="t"
            type="button"
            class="chip"
            :class="{ selected: store.settings.multiplicationTables.includes(t) }"
            @click="toggleTable(t)"
          >
            {{ t }}
          </button>
        </div>
        <label class="switch-row">
          <span>{{ store.t('settingsIncludeDivision') }}</span>
          <input v-model="store.settings.multiplicationIncludeDivision" type="checkbox" />
        </label>
      </section>

      <section class="section">
        <h2 class="section-title">{{ store.t('settingsExpressionsSection') }}</h2>
        <label class="switch-row">
          <span>{{ store.t('settingsUseParentheses') }}</span>
          <input v-model="store.settings.expressionWithParentheses" type="checkbox" />
        </label>
        <label class="switch-row">
          <span>{{ store.t('settingsLargeIntermediate') }}</span>
          <input v-model="store.settings.expressionLargeIntermediate" type="checkbox" />
        </label>
      </section>

      <section class="section">
        <h2 class="section-title">{{ store.t('settingsHistorySection') }}</h2>
        <button type="button" class="btn-danger-outline" @click="store.clearHistory()">
          {{ store.t('settingsClearHistory') }}
        </button>
      </section>
    </div>
  </div>
</template>

<style scoped>
.settings {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.settings-list {
  padding: 4px 16px 32px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.section {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--primary);
}

.sub-label {
  font-size: 14px;
  font-weight: 600;
}

.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tables-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
}

.chip {
  padding: 8px 14px;
  border-radius: 10px;
  border: 1px solid var(--border);
  background: var(--surface-alt);
  color: var(--text);
  font-size: 14px;
  font-weight: 600;
}

.chip-sm {
  padding: 6px 11px;
}

.chip.selected {
  background: var(--primary);
  border-color: var(--primary);
  color: #ffffff;
}

.chip:disabled {
  opacity: 0.4;
  cursor: default;
}

.radio-card {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px;
  border-radius: 12px;
  background: var(--surface-alt);
  cursor: pointer;
}

.radio-title {
  font-size: 14px;
  font-weight: 600;
}

.radio-sub {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 2px;
}

.digit-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-size: 14px;
}

.switch-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-size: 14px;
  cursor: pointer;
}

.switch-row input {
  width: 20px;
  height: 20px;
  accent-color: var(--primary);
}

.btn-danger-outline {
  width: 100%;
  padding: 10px 16px;
  border-radius: 12px;
  border: 1.5px solid var(--error);
  background: transparent;
  color: var(--error);
  font-size: 14px;
  font-weight: 600;
}
</style>
