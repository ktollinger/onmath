<script setup lang="ts">
import { ref } from 'vue';
import type { MathTopic } from './domain/models';
import ExpressionsView from './views/ExpressionsView.vue';
import HomeView from './views/HomeView.vue';
import MultiplicationView from './views/MultiplicationView.vue';
import SettingsView from './views/SettingsView.vue';
import SubtractionView from './views/SubtractionView.vue';

type Screen = 'HOME' | 'SETTINGS' | MathTopic;

const currentScreen = ref<Screen>('HOME');
</script>

<template>
  <main class="app-shell">
    <HomeView
      v-if="currentScreen === 'HOME'"
      @open-topic="currentScreen = $event"
      @open-settings="currentScreen = 'SETTINGS'"
    />
    <SettingsView v-else-if="currentScreen === 'SETTINGS'" @back="currentScreen = 'HOME'" />
    <MultiplicationView v-else-if="currentScreen === 'MULTIPLICATION'" @back="currentScreen = 'HOME'" />
    <ExpressionsView v-else-if="currentScreen === 'EXPRESSIONS'" @back="currentScreen = 'HOME'" />
    <SubtractionView v-else-if="currentScreen === 'SUBTRACTION'" @back="currentScreen = 'HOME'" />
  </main>
</template>
