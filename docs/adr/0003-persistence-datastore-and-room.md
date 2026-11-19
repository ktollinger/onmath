# ADR 0003: Persistence Strategy (Preferences DataStore and Room DB)

- **Status:** Accepted
- **Date:** 2026-11-19
- **Author:** Kamil / Antigravity

## Context and Problem Statement

The app needs to persist two different kinds of data locally:
1. User settings and preferences (e.g., subtraction borrow notation, digit range 2–6, multiplication table selection).
2. Completed practice session history and individual mistake records to track the child's progress.

## Decision Drivers

- Fast, type-safe reactive reading of simple preferences via `Flow`.
- Structured querying over session history and recurring mistakes.
- Consistency with `onbudget` (Room + KSP).

## Proposed Decision

- **Preferences DataStore** (`androidx.datastore:datastore-preferences`) for app settings and exercise preferences.
- **Room Database** for completed session history (`PracticeSessionEntity`) and mistake records (`MistakeRecordEntity`).

## Alternatives Considered

### Alternative 1: Everything in SharedPreferences / JSON files

- **Pros:** Simpler initial setup.
- **Cons:** Hard to filter and aggregate accuracy by topic or problematic exercises over time.

### Alternative 2: Everything in Room DB including settings

- **Pros:** Single persistence library.
- **Cons:** Unnecessary table boilerplate for simple key-value settings read on every screen start.
