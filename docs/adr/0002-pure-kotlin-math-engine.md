# ADR 0002: Pure Kotlin Math Engine Decoupled from Android

- **Status:** Accepted
- **Date:** 2026-11-19
- **Author:** Kamil / Antigravity

## Context and Problem Statement

The app covers three distinct math topics: multiplication tables, expressions with operator precedence and parentheses, and column subtraction of multi-digit numbers (2–6 digits) with borrowing. Mixing problem generation, expression evaluation, and borrow calculation directly into `ViewModel`s or Compose components would make the code hard to test and prone to inconsistent state.

## Decision Drivers

- 100% deterministic and fast unit tests (JUnit on JVM without an emulator).
- Clean domain code in pure Kotlin (close to standard object-oriented Java).
- Guaranteed pedagogical validity of generated problems (e.g., exact integer division, non-negative intermediate values, actual borrows in subtraction).

## Proposed Decision

All math logic is isolated in the `cz.kt.onmath.domain` package with zero dependencies on `android.*` or `androidx.compose.*`:

- **Problem generators** for each topic (`MultiplicationGenerator`, `ExpressionGenerator`, `SubtractionGenerator`).
- **Expression evaluator and step analyzer** (AST / parser respecting `*`, `/` precedence over `+`, `-` and parentheses) capable of computing both the final answer and the next hint step.
- **Column subtraction model** representing place values right-to-left, expected result digits, and borrow locations.

## Alternatives Considered

### Alternative 1: Logic directly inside ViewModels

- **Pros:** Fewer files initially.
- **Cons:** Requires mocking Android/Coroutines/DB to test math rules; poor reusability (e.g., for a future KMP/iOS port).
