import { defaultRandom, type RandomSource } from './models';

export interface SubtractionConfig {
  minDigits: number;
  maxDigits: number;
}

export interface SubtractionColumn {
  placeIndex: number;
  minuendDigit: number;
  subtrahendDigit: number;
  incomingBorrow: boolean;
  requiresBorrowFromNext: boolean;
  expectedResultDigit: number;
  effectiveUpperAfterLend: number;
  effectiveLowerWithCarry: number;
}

export interface SubtractionProblem {
  minuend: number;
  subtrahend: number;
  answer: number;
  digitCount: number;
  borrowCount: number;
  key: string;
  columnsRightToLeft: SubtractionColumn[];
}

export function buildSubtractionProblem(minuend: number, subtrahend: number): SubtractionProblem {
  if (minuend <= subtrahend) {
    throw new Error(`Minuend (${minuend}) must be greater than subtrahend (${subtrahend}).`);
  }

  const digitCount = String(minuend).length;
  const columns: SubtractionColumn[] = [];
  let borrowIn = 0;
  let m = minuend;
  let s = subtrahend;

  for (let place = 0; place < digitCount; place++) {
    const mDigit = m % 10;
    const sDigit = s % 10;
    m = Math.floor(m / 10);
    s = Math.floor(s / 10);

    const upperAfterLend = mDigit - borrowIn;
    const requiresBorrow = upperAfterLend < sDigit;
    const effectiveTop = requiresBorrow ? upperAfterLend + 10 : upperAfterLend;
    const expectedDigit = effectiveTop - sDigit;

    columns.push({
      placeIndex: place,
      minuendDigit: mDigit,
      subtrahendDigit: sDigit,
      incomingBorrow: borrowIn === 1,
      requiresBorrowFromNext: requiresBorrow,
      expectedResultDigit: expectedDigit,
      effectiveUpperAfterLend: upperAfterLend,
      effectiveLowerWithCarry: sDigit + borrowIn,
    });

    borrowIn = requiresBorrow ? 1 : 0;
  }

  return {
    minuend,
    subtrahend,
    answer: minuend - subtrahend,
    digitCount,
    borrowCount: columns.filter((c) => c.requiresBorrowFromNext).length,
    key: `${minuend}-${subtrahend}`,
    columnsRightToLeft: columns,
  };
}

export function generateSubtractionProblem(
  config: SubtractionConfig,
  random: RandomSource = defaultRandom,
): SubtractionProblem {
  const minDigits = Math.max(2, Math.min(6, config.minDigits));
  const maxDigits = Math.max(minDigits, Math.min(6, config.maxDigits));

  for (let attempt = 0; attempt < 300; attempt++) {
    const digits = random.nextInt(minDigits, maxDigits + 1);
    const minVal = 10 ** (digits - 1);
    const maxVal = 10 ** digits - 1;
    const minuend = random.nextInt(minVal, maxVal + 1);
    const subtrahendMin = digits > 2 ? Math.floor(minVal / 10) : 10;
    if (minuend <= subtrahendMin) continue;

    const subtrahend = random.nextInt(subtrahendMin, minuend);
    const candidate = buildSubtractionProblem(minuend, subtrahend);
    if (candidate.borrowCount >= 1) {
      return candidate;
    }
  }

  return buildSubtractionProblem(402, 158);
}

export function generateSubtractionSet(
  config: SubtractionConfig,
  count = 10,
  random: RandomSource = defaultRandom,
): SubtractionProblem[] {
  const result: SubtractionProblem[] = [];
  let attempts = 0;
  while (result.length < count && attempts < count * 10) {
    attempts++;
    const candidate = generateSubtractionProblem(config, random);
    if (!result.some((p) => p.key === candidate.key)) {
      result.push(candidate);
    }
  }
  while (result.length < count) {
    result.push(generateSubtractionProblem(config, random));
  }
  return result;
}
