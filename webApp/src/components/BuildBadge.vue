<script setup lang="ts">
const version = __APP_VERSION__;
const sha = __GIT_SHA__;
const buildTime = __BUILD_TIME__;

function formatBuildTime(iso: string): string {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  return d.toLocaleString(undefined, {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

const formattedTime = formatBuildTime(__BUILD_TIME__);
</script>

<template>
  <footer class="build-badge" :title="`v${version} • ${sha} • ${buildTime}`">
    <span>v{{ version }}</span>
    <span>•</span>
    <span>{{ sha }}</span>
    <span>•</span>
    <span>{{ formattedTime }}</span>
  </footer>
</template>

<style scoped>
.build-badge {
  display: flex;
  justify-content: flex-end;
  gap: 5px;
  padding: 4px 16px 8px;
  font-size: 11px;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  color: var(--text-muted);
  opacity: 0.72;
  user-select: none;
}

@media (min-width: 860px) {
  .build-badge {
    position: fixed;
    right: 12px;
    bottom: 8px;
    padding: 4px 8px;
    border-radius: 8px;
    background: color-mix(in srgb, var(--surface) 85%, transparent);
    border: 1px solid var(--border);
  }
}
</style>
