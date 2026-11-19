# AGENTS.md

Guidelines for development and AI coding agents working in the `onmath` repository.

## Overview & Architecture

- **Package:** `cz.kt.onmath` (`minSdk = 31`, `targetSdk = 35`, Kotlin + Jetpack Compose + Material 3, manual DI in `di/AppContainer.kt`).
- **Layer boundaries (see `docs/adr/`):**
  - `domain/` — pure Kotlin with zero `android.*` or `androidx.compose.*` imports (problem generators, expression AST, evaluation, unit tests). Keep it platform-independent for future KMP/iOS sharing.
  - `data/` — Room (`OnMathDatabase`) and Preferences DataStore (`SettingsRepository`).
  - `ui/` — Jetpack Compose screens, `StateFlow`-backed `ViewModel`s, and shared components under `ui/components/`.

## Verification

Run unit tests and assemble the debug APK after every code change:

```bash
./gradlew testDebugUnitTest assembleDebug
```

Always cover new domain logic in `domain/` with unit tests under `app/src/test/java/cz/kt/onmath/`.

## Product & UX Rules

- **No ads, no analytics/tracking, no `INTERNET` permission, no notifications.**
- **Stable vertical layout:** Practice screens must not jump vertically when hints, errors, or intermediate results appear (anchor content to the top and use fixed/minimum-height slots).
- **Manual pacing:** On multi-step exercises (column subtraction, expressions), keep the solved result visible until the user taps *Další příklad* / *Next problem*.
- **Custom keypad (`MathKeypad`):** Never trigger the system soft keyboard on practice screens.
- **Documentation:** Record significant architectural or UX decisions as new ADRs in `docs/adr/` following the existing numbering and format.
