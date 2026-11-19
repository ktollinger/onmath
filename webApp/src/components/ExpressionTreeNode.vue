<script setup lang="ts">
import { computed } from 'vue';
import {
  OPERATORS,
  needsImplicitParens,
  type BinaryNode,
  type ExpressionNode,
} from '../domain/expression';

defineOptions({
  name: 'ExpressionTreeNode',
});

const props = defineProps<{
  node: ExpressionNode;
  nodeId: string;
  wrapWithParens: boolean;
  intermediates: Record<string, number>;
  selectedNodeId: string | null;
  enabled: boolean;
}>();

const emit = defineEmits<{
  selectOperator: [nodeId: string];
  selectSubstituted: [nodeId: string];
}>();

const leftId = computed(() => `${props.nodeId}.L`);
const rightId = computed(() => `${props.nodeId}.R`);

function isResolved(child: ExpressionNode, id: string): boolean {
  return child.kind === 'number' || props.intermediates[id] !== undefined;
}

const intermediateValue = computed(() =>
  props.node.kind === 'binary' ? props.intermediates[props.nodeId] : undefined,
);

const isSelected = computed(() => props.selectedNodeId === props.nodeId);

const isReady = computed(() => {
  if (props.node.kind !== 'binary') return false;
  return isResolved(props.node.left, leftId.value) && isResolved(props.node.right, rightId.value);
});

const showParens = computed(() => {
  if (props.node.kind !== 'binary') return false;
  return Boolean(props.node.explicitParentheses) || props.wrapWithParens;
});

function handleOpClick(binNode: BinaryNode) {
  if (!props.enabled) return;
  if (intermediateValue.value !== undefined) {
    emit('selectSubstituted', props.nodeId);
  } else {
    void binNode;
    emit('selectOperator', props.nodeId);
  }
}
</script>

<template>
  <span v-if="node.kind === 'number'" class="expr-num">{{ node.value }}</span>

  <div v-else class="expr-bin">
    <div class="expr-row" :class="{ 'expr-row-selected': isSelected }">
      <span v-if="showParens" class="expr-paren">(</span>

      <ExpressionTreeNode
        :node="node.left"
        :node-id="leftId"
        :wrap-with-parens="needsImplicitParens(node, node.left, false)"
        :intermediates="intermediates"
        :selected-node-id="selectedNodeId"
        :enabled="enabled"
        @select-operator="emit('selectOperator', $event)"
        @select-substituted="emit('selectSubstituted', $event)"
      />

      <button
        type="button"
        class="op-btn"
        :class="{
          'op-selected': isSelected,
          'op-ready': isReady && intermediateValue === undefined && !isSelected,
          'op-solved': intermediateValue !== undefined && !isSelected,
        }"
        :disabled="!enabled"
        @click="handleOpClick(node)"
      >
        {{ OPERATORS[node.operator].symbol }}
      </button>

      <ExpressionTreeNode
        :node="node.right"
        :node-id="rightId"
        :wrap-with-parens="needsImplicitParens(node, node.right, true)"
        :intermediates="intermediates"
        :selected-node-id="selectedNodeId"
        :enabled="enabled"
        @select-operator="emit('selectOperator', $event)"
        @select-substituted="emit('selectSubstituted', $event)"
      />

      <span v-if="showParens" class="expr-paren">)</span>
    </div>

    <template v-if="intermediateValue !== undefined">
      <div class="expr-underline" />
      <button
        type="button"
        class="inter-pill"
        :disabled="!enabled"
        @click="emit('selectSubstituted', nodeId)"
      >
        {{ intermediateValue }}
      </button>
    </template>
  </div>
</template>

<style scoped>
.expr-num {
  display: inline-flex;
  align-items: center;
  height: 40px;
  font-size: 26px;
  font-weight: 700;
}

.expr-paren {
  display: inline-flex;
  align-items: center;
  height: 40px;
  font-size: 26px;
  font-weight: 700;
  color: var(--text-muted);
}

.expr-bin {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
}

.expr-row {
  display: inline-flex;
  align-items: flex-start;
  gap: 3px;
  border-radius: 8px;
}

.expr-row-selected {
  background: color-mix(in srgb, var(--primary-container) 55%, transparent);
  padding: 0 4px;
}

.op-btn {
  min-width: 24px;
  height: 40px;
  padding: 0 3px;
  border: none;
  background: transparent;
  color: var(--text);
  font-size: 26px;
  font-weight: 700;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.op-ready {
  min-width: 34px;
  padding: 0 6px;
  background: var(--surface-alt);
  border: 1.5px solid var(--border);
  color: var(--primary);
}

.op-selected {
  min-width: 34px;
  padding: 0 6px;
  background: var(--primary);
  color: #ffffff;
}

.op-solved {
  color: var(--primary);
}

.expr-underline {
  width: calc(100% - 8px);
  height: 2px;
  margin-top: 2px;
  background: color-mix(in srgb, var(--primary) 50%, transparent);
  border-radius: 1px;
}

.inter-pill {
  margin-top: 4px;
  padding: 2px 10px;
  border-radius: 8px;
  border: 1.5px solid var(--primary);
  background: var(--primary-container);
  color: var(--on-primary-container);
  font-size: 16px;
  font-weight: 700;
}
</style>
