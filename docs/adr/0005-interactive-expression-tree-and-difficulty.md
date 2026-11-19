# ADR 0005: Interactive Expression Tree, Intermediate Results, and Difficulty Toggle

- **Status:** Accepted
- **Date:** 2026-11-19
- **Author:** Kamil / Antigravity

## Context and Problem Statement

1. For a 4th-grade student, holding multiple intermediate results in working memory (e.g. `(9 + 9) × 3` or `(2 + 3) × 2 + 4 × 5`) is challenging.
2. Replacing a sub-expression in-place (`(14 − 7)` → `7`) shortens the text, but causes the child to lose visual context of the original problem (on paper, students do not erase the original expression — they write intermediate results underneath it).
3. Requiring the user to press `✓` on the keypad after entering an intermediate result before tapping the next operator feels unintuitive.
4. Some generated expressions produce multiplications outside the `10 × 10` table (e.g. `18 × 3`), which are better separated behind an optional higher-difficulty toggle.

## Decision Drivers

- Keep the original expression readable and intact at all times.
- Preserve visual clarity even for multi-step expressions and prevent vertical layout jumping.
- Never force intermediate steps on trivial expressions.

## Proposed Decision

- **Paper-style expression tree (`ExpressionsScreen`):**
  - The original expression always remains visible in the top row.
  - Operators whose operands are already resolved (numbers or entered intermediates) are highlighted as tappable chips.
  - Entered intermediate results render with an underline directly **underneath** their corresponding sub-expression in a recursive tree layout.
- **Fluid interaction (`ExpressionsViewModel` + `ExpressionInteractive`):**
  - Intermediate steps are optional — focus starts on the final answer box by default.
  - If the child types a number for an intermediate step and immediately taps the next operator or the final answer box, the pending intermediate is automatically validated and committed.
  - Once the final answer is solved, the result remains visible until the user taps *Další příklad* (*Next problem*).
- **Difficulty toggle (`ExpressionGenerator` + `AppSettings`):**
  - By default (`expressionLargeIntermediate = false`), all multiplications and divisions stay within the `10 × 10` table.
  - A settings toggle enables harder expressions with intermediate values above `10` (such as `18 × 3`).

## Alternatives Considered

### Alternative 1: In-place substitution of the expression text

- **Pros:** Saves vertical space.
- **Cons:** Breaks visual correspondence with the original problem and requires a separate line to repeat the original prompt.

### Alternative 2: Mandatory step-by-step solving before entering the final answer

- **Pros:** Forces explicit order-of-operations steps on every problem.
- **Cons:** Tedious on trivial expressions such as `(1 + 1) × 1 − 1 ÷ 1`.
