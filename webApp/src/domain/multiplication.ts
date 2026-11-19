import { defaultRandom, type RandomSource } from './models';

export type MultiplicationOperation = 'MULTIPLY' | 'DIVIDE';

export interface MultiplicationConfig {
  selectedTables: number[];
  includeDivision: boolean;
}

export interface MultiplicationProblem {
  left: number;
  right: number;
  operation: MultiplicationOperation;
  symbol: '×' | '÷';
  answer: number;
  displayText: string;
  key: string;
}

export function generateMultiplicationProblem(
  config: MultiplicationConfig,
  random: RandomSource = defaultRandom,
): MultiplicationProblem {
  const validTables = config.selectedTables.filter((t) => t >= 1 && t <= 10);
  if (validTables.length === 0) {
    throw new Error('At least one multiplication table must be selected.');
  }

  const table = validTables[random.nextInt(0, validTables.length)];
  const factor = random.nextInt(1, 11);
  const useDivision = config.includeDivision && random.nextBoolean();

  if (useDivision) {
    const dividend = table * factor;
    return {
      left: dividend,
      right: table,
      operation: 'DIVIDE',
      symbol: '÷',
      answer: factor,
      displayText: `${dividend} ÷ ${table} = ?`,
      key: `${dividend}÷${table}`,
    };
  }

  const swapOrder = random.nextBoolean();
  const left = swapOrder ? factor : table;
  const right = swapOrder ? table : factor;
  return {
    left,
    right,
    operation: 'MULTIPLY',
    symbol: '×',
    answer: left * right,
    displayText: `${left} × ${right} = ?`,
    key: `${left}×${right}`,
  };
}

export function generateMultiplicationSet(
  config: MultiplicationConfig,
  count = 10,
  random: RandomSource = defaultRandom,
): MultiplicationProblem[] {
  const result: MultiplicationProblem[] = [];
  let attempts = 0;
  while (result.length < count && attempts < count * 10) {
    attempts++;
    const candidate = generateMultiplicationProblem(config, random);
    if (result[result.length - 1]?.key !== candidate.key) {
      result.push(candidate);
    }
  }
  while (result.length < count) {
    result.push(generateMultiplicationProblem(config, random));
  }
  return result;
}
