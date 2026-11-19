# ADR 0004: Custom Numeric Keypad and Dual Subtraction Borrow Notation

- **Status:** Accepted
- **Date:** 2026-11-19
- **Author:** Kamil / Antigravity

## Context and Problem Statement

1. During phone practice, popping up the system soft keyboard is slow, covers part of the problem, and does not support comfortable right-to-left digit-by-digit entry for column subtraction.
2. The child attends a Montessori school that uses the Anglo-Saxon borrowing notation (borrowing from a higher place and decrementing the top digit), whereas standard Czech schools teach adding ten at the top and carrying a one at the bottom of the subtrahend.

## Decision Drivers

- Maximum comfort and speed for a child on a portrait phone screen.
- Ability to switch borrow/carry notation in settings without touching the math engine.

## Proposed Decision

- **Custom in-app numeric keypad** (`MathKeypad`, inspired by `CalculatorPad` in `onbudget`) anchored at the bottom of practice screens (digits `0–9`, `Backspace`, `Confirm`).
- **Parameterized borrow visualization (`SubtractionMethod`):**
  - `BORROW_TOP` (Montessori / Anglo-Saxon): tapping above a top digit marks a borrow / decremented value.
  - `CARRY_BOTTOM` (traditional Czech): tapping next to a bottom digit marks a carried one to the next subtrahend place.
- Borrow markers in the UI are optional visual aids (and highlighted in hint mode); correctness is evaluated solely on the final digits entered right-to-left.

## Alternatives Considered

### Alternative 1: Android system numeric keyboard

- **Pros:** No custom component needed.
- **Cons:** Inconsistent behavior across devices, unpredictable screen height, worse ergonomics for rapid digit-by-digit entry.

### Alternative 2: Hardcoding only one subtraction notation

- **Pros:** Simpler UI.
- **Cons:** Confuses the child if the visual notation in the app differs from what they practice at school.
