import { defaultRandom, type RandomSource } from './models';

export type BinaryOperatorType = 'ADD' | 'SUBTRACT' | 'MULTIPLY' | 'DIVIDE';

export interface OperatorMeta {
  type: BinaryOperatorType;
  symbol: '+' | '−' | '×' | '÷';
  precedence: 1 | 2;
}

export const OPERATORS: Record<BinaryOperatorType, OperatorMeta> = {
  ADD: { type: 'ADD', symbol: '+', precedence: 1 },
  SUBTRACT: { type: 'SUBTRACT', symbol: '−', precedence: 1 },
  MULTIPLY: { type: 'MULTIPLY', symbol: '×', precedence: 2 },
  DIVIDE: { type: 'DIVIDE', symbol: '÷', precedence: 2 },
};

export interface NumberNode {
  kind: 'number';
  value: number;
}

export interface BinaryNode {
  kind: 'binary';
  left: ExpressionNode;
  operator: BinaryOperatorType;
  right: ExpressionNode;
  explicitParentheses?: boolean;
}

export type ExpressionNode = NumberNode | BinaryNode;

export type ExpressionDifficulty = 'WITHOUT_PARENTHESES' | 'WITH_PARENTHESES';

export interface ExpressionProblem {
  root: ExpressionNode;
  displayText: string;
  key: string;
  answer: number;
}

export type ExpressionStepHint =
  | { kind: 'SOLVE_DEFAULT' }
  | { kind: 'PARENTHESES'; equation: string }
  | { kind: 'PRECEDENCE'; equation: string }
  | { kind: 'LEFT_TO_RIGHT'; equation: string };

export type ExpressionToken =
  | { kind: 'text'; text: string }
  | { kind: 'substituted'; nodeId: string; value: number; originalExpression: string }
  | {
      kind: 'operator';
      nodeId: string;
      symbol: string;
      isReady: boolean;
      isSelected: boolean;
      subExpression: string;
      expectedValue: number;
    };

export function num(value: number): NumberNode {
  return { kind: 'number', value };
}

export function bin(
  left: ExpressionNode,
  operator: BinaryOperatorType,
  right: ExpressionNode,
  explicitParentheses = false,
): BinaryNode {
  return { kind: 'binary', left, operator, right, explicitParentheses };
}

export function evaluateNode(node: ExpressionNode): number {
  if (node.kind === 'number') return node.value;
  const l = evaluateNode(node.left);
  const r = evaluateNode(node.right);
  switch (node.operator) {
    case 'ADD':
      return l + r;
    case 'SUBTRACT':
      return l - r;
    case 'MULTIPLY':
      return l * r;
    case 'DIVIDE':
      if (r === 0 || l % r !== 0) {
        throw new Error(`Invalid division: ${l} ÷ ${r}`);
      }
      return l / r;
  }
}

export function allIntermediatesNonNegative(node: ExpressionNode): boolean {
  if (node.kind === 'number') return node.value >= 0;
  if (!allIntermediatesNonNegative(node.left) || !allIntermediatesNonNegative(node.right)) {
    return false;
  }
  try {
    return evaluateNode(node) >= 0;
  } catch {
    return false;
  }
}

export function needsImplicitParens(parent: BinaryNode, child: ExpressionNode, isRight: boolean): boolean {
  if (child.kind !== 'binary' || child.explicitParentheses) return false;
  const childPrec = OPERATORS[child.operator].precedence;
  const parentPrec = OPERATORS[parent.operator].precedence;
  if (childPrec < parentPrec) return true;
  if (isRight && childPrec === parentPrec && (parent.operator === 'SUBTRACT' || parent.operator === 'DIVIDE')) {
    return true;
  }
  return false;
}

export function formatNode(node: ExpressionNode): string {
  if (node.kind === 'number') return String(node.value);
  const leftStr = formatChild(node, node.left, false);
  const rightStr = formatChild(node, node.right, true);
  const body = `${leftStr} ${OPERATORS[node.operator].symbol} ${rightStr}`;
  return node.explicitParentheses ? `(${body})` : body;
}

function formatChild(parent: BinaryNode, child: ExpressionNode, isRight: boolean): string {
  const formatted = formatNode(child);
  return needsImplicitParens(parent, child, isRight) ? `(${formatted})` : formatted;
}

function findFirstOperation(
  node: ExpressionNode,
  insideParens: boolean,
): { node: BinaryNode; inParens: boolean } | null {
  if (node.kind !== 'binary') return null;
  if (node.left.kind === 'number' && node.right.kind === 'number') {
    return { node, inParens: insideParens || Boolean(node.explicitParentheses) };
  }

  const parentPrec = OPERATORS[node.operator].precedence;
  const leftHasParens =
    node.left.kind === 'binary' &&
    (Boolean(node.left.explicitParentheses) || OPERATORS[node.left.operator].precedence < parentPrec);
  if (leftHasParens) {
    return findFirstOperation(node.left, true);
  }

  const rightHasParens =
    node.right.kind === 'binary' &&
    (Boolean(node.right.explicitParentheses) || OPERATORS[node.right.operator].precedence < parentPrec);
  if (rightHasParens) {
    return findFirstOperation(node.right, true);
  }

  const leftCandidate = findFirstOperation(node.left, false);
  const rightCandidate = findFirstOperation(node.right, false);

  if (leftCandidate && rightCandidate) {
    const leftPrec = OPERATORS[leftCandidate.node.operator].precedence;
    const rightPrec = OPERATORS[rightCandidate.node.operator].precedence;
    return rightPrec > leftPrec ? rightCandidate : leftCandidate;
  }
  return leftCandidate ?? rightCandidate;
}

export function firstStepHintInfo(node: ExpressionNode): ExpressionStepHint {
  const firstOp = findFirstOperation(node, false);
  if (!firstOp) return { kind: 'SOLVE_DEFAULT' };

  const leftVal = evaluateNode(firstOp.node.left);
  const rightVal = evaluateNode(firstOp.node.right);
  const result = evaluateNode(firstOp.node);
  const symbol = OPERATORS[firstOp.node.operator].symbol;
  const equation = `${leftVal} ${symbol} ${rightVal} = ${result}`;

  if (firstOp.inParens) return { kind: 'PARENTHESES', equation };
  if (OPERATORS[firstOp.node.operator].precedence === 2) return { kind: 'PRECEDENCE', equation };
  return { kind: 'LEFT_TO_RIGHT', equation };
}

export function reduceWithIntermediates(
  node: ExpressionNode,
  intermediates: Record<string, number>,
  nodeId = 'root',
): ExpressionNode {
  if (node.kind === 'number') return node;
  if (intermediates[nodeId] !== undefined) {
    return num(intermediates[nodeId]);
  }
  return {
    ...node,
    left: reduceWithIntermediates(node.left, intermediates, `${nodeId}.L`),
    right: reduceWithIntermediates(node.right, intermediates, `${nodeId}.R`),
  };
}

export function findBinaryNode(
  node: ExpressionNode,
  targetId: string,
  currentId = 'root',
): BinaryNode | null {
  if (node.kind !== 'binary') return null;
  if (currentId === targetId) return node;
  return (
    findBinaryNode(node.left, targetId, `${currentId}.L`) ??
    findBinaryNode(node.right, targetId, `${currentId}.R`)
  );
}

export function removeIntermediateAndAncestors(
  nodeId: string,
  intermediates: Record<string, number>,
  order: string[],
): { intermediates: Record<string, number>; order: string[] } {
  const toRemove = new Set(
    Object.keys(intermediates).filter((existing) => nodeId === existing || nodeId.startsWith(`${existing}.`)),
  );
  const nextIntermediates: Record<string, number> = {};
  for (const [k, v] of Object.entries(intermediates)) {
    if (!toRemove.has(k)) nextIntermediates[k] = v;
  }
  return {
    intermediates: nextIntermediates,
    order: order.filter((id) => !toRemove.has(id)),
  };
}

export function tokenizeExpression(
  node: ExpressionNode,
  intermediates: Record<string, number> = {},
  selectedNodeId: string | null = null,
): ExpressionToken[] {
  const out: ExpressionToken[] = [];
  appendTokens(node, 'root', intermediates, selectedNodeId, false, out);
  return out;
}

function appendTokens(
  node: ExpressionNode,
  nodeId: string,
  intermediates: Record<string, number>,
  selectedNodeId: string | null,
  wrapWithParens: boolean,
  out: ExpressionToken[],
): void {
  if (node.kind === 'number') {
    out.push({ kind: 'text', text: String(node.value) });
    return;
  }

  const substituted = intermediates[nodeId];
  if (substituted !== undefined) {
    out.push({
      kind: 'substituted',
      nodeId,
      value: substituted,
      originalExpression: formatNode(node),
    });
    return;
  }

  const showParens = Boolean(node.explicitParentheses) || wrapWithParens;
  if (showParens) out.push({ kind: 'text', text: '(' });

  const leftId = `${nodeId}.L`;
  const rightId = `${nodeId}.R`;
  const leftNeedsParens = needsImplicitParens(node, node.left, false) && intermediates[leftId] === undefined;
  const rightNeedsParens = needsImplicitParens(node, node.right, true) && intermediates[rightId] === undefined;

  appendTokens(node.left, leftId, intermediates, selectedNodeId, leftNeedsParens, out);

  const isReady = isResolved(node.left, leftId, intermediates) && isResolved(node.right, rightId, intermediates);
  const leftDisplay = resolvedDisplay(node.left, leftId, intermediates);
  const rightDisplay = resolvedDisplay(node.right, rightId, intermediates);
  const symbol = OPERATORS[node.operator].symbol;

  out.push({
    kind: 'operator',
    nodeId,
    symbol,
    isReady,
    isSelected: nodeId === selectedNodeId,
    subExpression: `${leftDisplay} ${symbol} ${rightDisplay}`,
    expectedValue: evaluateNode(node),
  });

  appendTokens(node.right, rightId, intermediates, selectedNodeId, rightNeedsParens, out);

  if (showParens) out.push({ kind: 'text', text: ')' });
}

function isResolved(node: ExpressionNode, nodeId: string, intermediates: Record<string, number>): boolean {
  return node.kind === 'number' || intermediates[nodeId] !== undefined;
}

function resolvedDisplay(node: ExpressionNode, nodeId: string, intermediates: Record<string, number>): string {
  if (node.kind === 'number') return String(node.value);
  return intermediates[nodeId] !== undefined ? String(intermediates[nodeId]) : formatNode(node);
}

// --- Generator ---

function allHighPrecedenceWithinTable(node: ExpressionNode): boolean {
  if (node.kind === 'number') return true;
  const childrenOk = allHighPrecedenceWithinTable(node.left) && allHighPrecedenceWithinTable(node.right);
  if (!childrenOk) return false;
  if (node.operator === 'MULTIPLY') {
    return evaluateNode(node.left) <= 10 && evaluateNode(node.right) <= 10;
  }
  if (node.operator === 'DIVIDE') {
    return evaluateNode(node.right) <= 10 && evaluateNode(node) <= 10;
  }
  return true;
}

function isValidExpression(node: ExpressionNode, allowLargeIntermediate: boolean): boolean {
  try {
    const result = evaluateNode(node);
    if (!allIntermediatesNonNegative(node) || result < 0 || result > 250) return false;
    return allowLargeIntermediate || allHighPrecedenceWithinTable(node);
  } catch {
    return false;
  }
}

function randomHighPrecedencePair(random: RandomSource): BinaryNode {
  if (random.nextBoolean()) {
    return bin(num(random.nextInt(2, 11)), 'MULTIPLY', num(random.nextInt(2, 11)));
  }
  const divisor = random.nextInt(2, 11);
  const quotient = random.nextInt(2, 11);
  return bin(num(divisor * quotient), 'DIVIDE', num(divisor));
}

function maybeAppendLowOp(core: ExpressionNode, random: RandomSource): ExpressionNode {
  if (random.nextBoolean()) return core;
  const op: BinaryOperatorType = random.nextBoolean() ? 'ADD' : 'SUBTRACT';
  const extra = num(random.nextInt(1, 21));
  return random.nextBoolean() ? bin(core, op, extra) : bin(extra, op, core);
}

function buildWithoutParentheses(random: RandomSource): ExpressionNode {
  const highNode = randomHighPrecedencePair(random);
  const lowOp: BinaryOperatorType = random.nextBoolean() ? 'ADD' : 'SUBTRACT';
  const other = num(random.nextInt(2, 31));
  const twoOp = random.nextBoolean() ? bin(other, lowOp, highNode) : bin(highNode, lowOp, other);

  if (random.nextBoolean()) return twoOp;

  const extraOp: BinaryOperatorType = random.nextBoolean() ? 'ADD' : 'SUBTRACT';
  const extraVal = num(random.nextInt(2, 21));
  return extraOp === 'SUBTRACT' || random.nextBoolean()
    ? bin(twoOp, extraOp, extraVal)
    : bin(extraVal, extraOp, twoOp);
}

function buildWithParentheses(allowLargeIntermediate: boolean, random: RandomSource): ExpressionNode | null {
  const innerOp: BinaryOperatorType = random.nextBoolean() ? 'ADD' : 'SUBTRACT';
  const inner = bin(num(random.nextInt(2, 21)), innerOp, num(random.nextInt(2, 21)), true);
  let innerResult: number;
  try {
    innerResult = evaluateNode(inner);
  } catch {
    return null;
  }
  if (innerResult <= 0) return null;

  if (random.nextBoolean()) {
    if (!allowLargeIntermediate && (innerResult < 2 || innerResult > 10)) return null;
    const factor = num(random.nextInt(2, 11));
    const core = random.nextBoolean() ? bin(inner, 'MULTIPLY', factor) : bin(factor, 'MULTIPLY', inner);
    return maybeAppendLowOp(core, random);
  }

  const divisors: number[] = [];
  for (let d = 2; d <= 10; d++) {
    if (innerResult % d === 0 && (allowLargeIntermediate || (innerResult / d >= 2 && innerResult / d <= 10))) {
      divisors.push(d);
    }
  }
  if (divisors.length === 0) return null;
  const divisor = num(divisors[random.nextInt(0, divisors.length)]);
  const core = bin(inner, 'DIVIDE', divisor);
  return maybeAppendLowOp(core, random);
}

export function generateExpressionProblem(
  difficulty: ExpressionDifficulty = 'WITH_PARENTHESES',
  allowLargeIntermediate = false,
  random: RandomSource = defaultRandom,
): ExpressionProblem {
  for (let attempt = 0; attempt < 200; attempt++) {
    const candidate =
      difficulty === 'WITHOUT_PARENTHESES'
        ? buildWithoutParentheses(random)
        : buildWithParentheses(allowLargeIntermediate, random);

    if (candidate && isValidExpression(candidate, allowLargeIntermediate)) {
      const key = formatNode(candidate);
      return {
        root: candidate,
        key,
        displayText: `${key} = ?`,
        answer: evaluateNode(candidate),
      };
    }
  }

  const fallback = bin(num(2), 'ADD', bin(num(3), 'MULTIPLY', num(4)));
  const key = formatNode(fallback);
  return {
    root: fallback,
    key,
    displayText: `${key} = ?`,
    answer: evaluateNode(fallback),
  };
}

export function generateExpressionSet(
  difficulty: ExpressionDifficulty = 'WITH_PARENTHESES',
  count = 10,
  allowLargeIntermediate = false,
  random: RandomSource = defaultRandom,
): ExpressionProblem[] {
  const result: ExpressionProblem[] = [];
  let attempts = 0;
  while (result.length < count && attempts < count * 10) {
    attempts++;
    const candidate = generateExpressionProblem(difficulty, allowLargeIntermediate, random);
    if (!result.some((p) => p.key === candidate.key)) {
      result.push(candidate);
    }
  }
  while (result.length < count) {
    result.push(generateExpressionProblem(difficulty, allowLargeIntermediate, random));
  }
  return result;
}
