import { reactive, watch } from 'vue';
import type { AppLanguage, AppTheme, MathTopic, SubtractionMethod } from '../domain/models';
import { translate, type MessageKey } from '../i18n/messages';

export interface StoredSession {
  topic: MathTopic;
  totalProblems: number;
  correctFirstTry: number;
}

export interface StoredMistake {
  id: string;
  topic: MathTopic;
  displayText: string;
  expectedAnswer: number;
}

export interface AppSettingsState {
  language: AppLanguage;
  theme: AppTheme;
  subtractionMethod: SubtractionMethod;
  subtractionMinDigits: number;
  subtractionMaxDigits: number;
  multiplicationTables: number[];
  multiplicationIncludeDivision: boolean;
  expressionWithParentheses: boolean;
  expressionLargeIntermediate: boolean;
}

const STORAGE_KEYS = {
  settings: 'onmath.settings.v1',
  sessions: 'onmath.sessions.v1',
  mistakes: 'onmath.mistakes.v1',
};

const defaultSettings: AppSettingsState = {
  language: 'SYSTEM',
  theme: 'SYSTEM',
  subtractionMethod: 'BORROW_TOP',
  subtractionMinDigits: 2,
  subtractionMaxDigits: 6,
  multiplicationTables: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
  multiplicationIncludeDivision: false,
  expressionWithParentheses: true,
  expressionLargeIntermediate: false,
};

function loadJson<T>(key: string, fallback: T): T {
  try {
    const raw = localStorage.getItem(key);
    if (!raw) return fallback;
    return JSON.parse(raw) as T;
  } catch {
    return fallback;
  }
}

function loadSettings(): AppSettingsState {
  const parsed = loadJson<Partial<AppSettingsState>>(STORAGE_KEYS.settings, {});
  const minD = Math.max(2, Math.min(6, parsed.subtractionMinDigits ?? 2));
  const maxD = Math.max(minD, Math.min(6, parsed.subtractionMaxDigits ?? 6));
  const tables = Array.isArray(parsed.multiplicationTables)
    ? parsed.multiplicationTables.filter((t) => t >= 1 && t <= 10)
    : defaultSettings.multiplicationTables;

  return {
    ...defaultSettings,
    ...parsed,
    subtractionMinDigits: minD,
    subtractionMaxDigits: maxD,
    multiplicationTables: tables.length > 0 ? tables : defaultSettings.multiplicationTables,
  };
}

export const store = reactive({
  settings: loadSettings(),
  sessions: loadJson<StoredSession[]>(STORAGE_KEYS.sessions, []),
  mistakes: loadJson<StoredMistake[]>(STORAGE_KEYS.mistakes, []),

  t(key: MessageKey, ...args: Array<string | number>): string {
    return translate(this.settings.language, key, ...args);
  },

  statsFor(topic: MathTopic): { sessionCount: number; accuracyPercent: number } | null {
    const list = this.sessions.filter((s) => s.topic === topic);
    if (list.length === 0) return null;
    const total = list.reduce((acc, s) => acc + s.totalProblems, 0);
    const correct = list.reduce((acc, s) => acc + s.correctFirstTry, 0);
    return {
      sessionCount: list.length,
      accuracyPercent: total === 0 ? 0 : Math.round((correct * 100) / total),
    };
  },

  recordSession(topic: MathTopic, totalProblems: number, correctFirstTry: number): void {
    this.sessions.push({ topic, totalProblems, correctFirstTry });
  },

  recordMistake(topic: MathTopic, displayText: string, expectedAnswer: number): void {
    this.mistakes.unshift({
      id: `${Date.now()}-${Math.random().toString(36).slice(2, 7)}`,
      topic,
      displayText,
      expectedAnswer,
    });
    if (this.mistakes.length > 30) {
      this.mistakes.length = 30;
    }
  },

  clearHistory(): void {
    this.sessions = [];
    this.mistakes = [];
  },
});

export function applyTheme(theme: AppTheme): void {
  if (typeof document === 'undefined') return;
  const root = document.documentElement;
  if (theme === 'LIGHT') {
    root.setAttribute('data-theme', 'light');
  } else if (theme === 'DARK') {
    root.setAttribute('data-theme', 'dark');
  } else {
    root.removeAttribute('data-theme');
  }
}

applyTheme(store.settings.theme);

watch(
  () => store.settings,
  (val) => {
    localStorage.setItem(STORAGE_KEYS.settings, JSON.stringify(val));
    applyTheme(val.theme);
  },
  { deep: true },
);

watch(
  () => store.sessions,
  (val) => {
    localStorage.setItem(STORAGE_KEYS.sessions, JSON.stringify(val));
  },
  { deep: true },
);

watch(
  () => store.mistakes,
  (val) => {
    localStorage.setItem(STORAGE_KEYS.mistakes, JSON.stringify(val));
  },
  { deep: true },
);
