# OnMath

An offline Android app built with Kotlin and Jetpack Compose for practicing 4th-grade elementary math — with no ads, no data collection, no accounts, and no notifications.

## Features

- **Column Subtraction** (`2–6` digits)
  - Supports two borrowing/carrying notations: borrowing from the top (Montessori / Anglo-Saxon notation) and carrying at the bottom (traditional Czech school notation).
  - Right-to-left digit entry via a custom in-app numeric keypad, place-specific hints, and manual confirmation before advancing to the next problem.
- **Multiplication & Division**
  - Selectable multiplication tables (`1–10`) and an optional toggle to include division within the selected tables.
- **Expressions & Order of Operations**
  - Problems with and without parentheses, plus a difficulty toggle (staying within `10 × 10` vs. larger intermediate multiplications like `18 × 3`).
  - **Paper-style interactive intermediate results:** the original expression always stays visible; the child can tap any ready operator to jot down an intermediate result directly underneath the sub-expression and seamlessly tap the next operator. Intermediate steps are optional.
- **Local Progress & Mistake Tracking**
  - Session accuracy and mistake history stored strictly on-device (Room + DataStore).

## Principles

- **100% offline:** No `INTERNET` permission and no backend communication.
- **Child-friendly:** No ads, no notifications, and no manipulative daily streaks.

## Architecture & Documentation

- `app/src/main/java/cz/kt/onmath/domain/` — pure Kotlin math engine and problem generators (zero Android dependencies).
- `app/src/main/java/cz/kt/onmath/data/` — local persistence (Room database + Preferences DataStore).
- `app/src/main/java/cz/kt/onmath/ui/` — Jetpack Compose screens, ViewModels, and shared components (`MathKeypad`).
- [Architecture Decision Records (ADR)](docs/adr/)
- [Roadmap](docs/ROADMAP.md)

## Build & Test

Requirements: Android SDK (`compileSdk = 35`, `minSdk = 31`), JDK 17.

```bash
./gradlew testDebugUnitTest assembleDebug
```

## License

Licensed under the [MIT License](LICENSE).
