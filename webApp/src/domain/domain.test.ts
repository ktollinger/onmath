import { describe, expect, it } from 'vitest';
import { createSeededRandom } from './models';
import { generateMultiplicationSet } from './multiplication';
import { buildSubtractionProblem, generateSubtractionProblem } from './subtraction';
import {
  allIntermediatesNonNegative,
  bin,
  evaluateNode,
  formatNode,
  generateExpressionProblem,
  num,
  reduceWithIntermediates,
  removeIntermediateAndAncestors,
  tokenizeExpression,
  type ExpressionNode,
} from './expression';

function allMultiplicationsWithinTen(node: ExpressionNode): boolean {
  if (node.kind === 'number') return true;
  const selfOk =
    node.operator === 'MULTIPLY'
      ? evaluateNode(node.left) <= 10 && evaluateNode(node.right) <= 10
      : node.operator === 'DIVIDE'
        ? evaluateNode(node.right) <= 10 && evaluateNode(node) <= 10
        : true;
  return selfOk && allMultiplicationsWithinTen(node.left) && allMultiplicationsWithinTen(node.right);
}

describe('multiplication domain', () => {
  it('generates 10 problems within selected tables', () => {
    const rng = createSeededRandom(42);
    const set = generateMultiplicationSet({ selectedTables: [6, 7], includeDivision: false }, 10, rng);
    expect(set).toHaveLength(10);
    for (const p of set) {
      expect(p.operation).toBe('MULTIPLY');
      expect(p.left * p.right).toBe(p.answer);
      expect([6, 7].includes(p.left) || [6, 7].includes(p.right)).toBe(true);
    }
  });
});

describe('subtraction domain', () => {
  it('computes columns and borrows for 402 - 158', () => {
    const p = buildSubtractionProblem(402, 158);
    expect(p.answer).toBe(244);
    expect(p.digitCount).toBe(3);
    expect(p.borrowCount).toBe(2);
    expect(p.columnsRightToLeft[0].expectedResultDigit).toBe(4);
    expect(p.columnsRightToLeft[1].incomingBorrow).toBe(true);
    expect(p.columnsRightToLeft[1].expectedResultDigit).toBe(4);
    expect(p.columnsRightToLeft[2].expectedResultDigit).toBe(2);
  });

  it('always generates problems with at least one borrow', () => {
    const rng = createSeededRandom(99);
    for (let i = 0; i < 40; i++) {
      const p = generateSubtractionProblem({ minDigits: 2, maxDigits: 5 }, rng);
      expect(p.minuend).toBeGreaterThan(p.subtrahend);
      expect(p.borrowCount).toBeGreaterThanOrEqual(1);
    }
  });
});

describe('expression domain', () => {
  it('respects precedence and parentheses', () => {
    const expr = bin(num(2), 'ADD', bin(num(3), 'MULTIPLY', num(4)));
    expect(formatNode(expr)).toBe('2 + 3 × 4');
    expect(evaluateNode(expr)).toBe(14);
  });

  it('keeps multiplications within 10x10 in standard difficulty', () => {
    const rng = createSeededRandom(77);
    for (let i = 0; i < 60; i++) {
      const p = generateExpressionProblem('WITH_PARENTHESES', false, rng);
      expect(allIntermediatesNonNegative(p.root)).toBe(true);
      expect(allMultiplicationsWithinTen(p.root)).toBe(true);
    }
  });

  it('tokenizes and reduces multi-step expressions', () => {
    const expr = bin(
      bin(bin(num(2), 'ADD', num(3), true), 'MULTIPLY', num(2)),
      'ADD',
      bin(num(4), 'MULTIPLY', num(5)),
    );
    const step1 = { 'root.L.L': 5 };
    expect(formatNode(reduceWithIntermediates(expr, step1))).toBe('5 × 2 + 4 × 5');

    const tokens = tokenizeExpression(expr, step1);
    const ops = tokens.filter((t) => t.kind === 'operator');
    expect(ops.map((o) => o.kind === 'operator' && o.isReady)).toEqual([true, false, true]);

    const cleared = removeIntermediateAndAncestors('root.L.L', { 'root.L.L': 5, 'root.L': 10, 'root.R': 20 }, [
      'root.L.L',
      'root.L',
      'root.R',
    ]);
    expect(cleared.intermediates).toEqual({ 'root.R': 20 });
    expect(cleared.order).toEqual(['root.R']);
  });
});
