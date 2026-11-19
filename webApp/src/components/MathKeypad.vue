<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue';

const props = withDefaults(
  defineProps<{
    submitEnabled?: boolean;
  }>(),
  {
    submitEnabled: true,
  },
);

const emit = defineEmits<{
  digit: [value: number];
  backspace: [];
  submit: [];
}>();

const rows = [
  [1, 2, 3],
  [4, 5, 6],
  [7, 8, 9],
];

function handleKeyDown(event: KeyboardEvent) {
  if (event.ctrlKey || event.metaKey || event.altKey) return;
  if (event.key >= '0' && event.key <= '9') {
    event.preventDefault();
    emit('digit', Number(event.key));
  } else if (event.key === 'Backspace') {
    event.preventDefault();
    emit('backspace');
  } else if (event.key === 'Enter') {
    if (props.submitEnabled) {
      event.preventDefault();
      emit('submit');
    }
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeyDown);
});

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown);
});
</script>

<template>
  <div class="keypad">
    <div v-for="(row, idx) in rows" :key="idx" class="keypad-row">
      <button v-for="d in row" :key="d" type="button" class="key-btn" @click="emit('digit', d)">
        {{ d }}
      </button>
    </div>
    <div class="keypad-row">
      <button type="button" class="key-btn" aria-label="Backspace" @click="emit('backspace')">
        ⌫
      </button>
      <button type="button" class="key-btn" @click="emit('digit', 0)">0</button>
      <button
        type="button"
        class="key-btn key-submit"
        :disabled="!submitEnabled"
        aria-label="Submit"
        @click="emit('submit')"
      >
        ✓
      </button>
    </div>
  </div>
</template>

<style scoped>
.keypad {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
  margin-top: auto;
}

.keypad-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.key-btn {
  height: 54px;
  border: none;
  border-radius: 14px;
  background: var(--keypad-btn);
  color: var(--keypad-text);
  font-size: 24px;
  font-weight: 600;
  transition: transform 0.05s ease, background-color 0.12s ease;
}

.key-btn:active {
  transform: scale(0.97);
}

.key-btn:hover {
  background: var(--keypad-btn-hover);
}

.key-submit {
  background: var(--correct);
  color: #ffffff;
}

.key-submit:hover:not(:disabled) {
  filter: brightness(1.06);
}

.key-submit:disabled {
  background: var(--border);
  color: var(--text-muted);
  cursor: default;
}

@media (max-height: 700px) {
  .key-btn {
    height: 46px;
    font-size: 22px;
  }
}
</style>
