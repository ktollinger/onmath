export type MathTopic = 'MULTIPLICATION' | 'EXPRESSIONS' | 'SUBTRACTION';

export type SubtractionMethod = 'BORROW_TOP' | 'CARRY_BOTTOM';

export type AppLanguage = 'SYSTEM' | 'CZECH' | 'ENGLISH';

export type AppTheme = 'SYSTEM' | 'LIGHT' | 'DARK';

export interface RandomSource {
  /** Returns integer in [min, maxExclusive) */
  nextInt(min: number, maxExclusive: number): number;
  nextBoolean(): boolean;
}

export const defaultRandom: RandomSource = {
  nextInt(min: number, maxExclusive: number): number {
    return Math.floor(Math.random() * (maxExclusive - min)) + min;
  },
  nextBoolean(): boolean {
    return Math.random() < 0.5;
  },
};

export function createSeededRandom(seed: number): RandomSource {
  let state = seed >>> 0;
  const nextFloat = (): number => {
    state = (state * 1664525 + 1013904223) >>> 0;
    return state / 0x100000000;
  };
  return {
    nextInt(min: number, maxExclusive: number): number {
      return Math.floor(nextFloat() * (maxExclusive - min)) + min;
    },
    nextBoolean(): boolean {
      return nextFloat() < 0.5;
    },
  };
}
