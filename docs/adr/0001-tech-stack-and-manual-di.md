# ADR 0001: Tech Stack and Manual Dependency Injection

- **Status:** Accepted
- **Date:** 2026-11-19
- **Author:** Kamil / Antigravity

## Context and Problem Statement

We are building a new Android app `OnMath` for elementary school math practice. The developer has extensive experience with Java and classic Android (Views/XML), but is newer to modern Kotlin and Jetpack Compose. The goal is to choose an architecture that is modern, maintainable, and easy to read and trace when transitioning from Java — following the reference project `onbudget` (`refactor/6-ui-architecture` branch).

## Decision Drivers

- Consistency with `onbudget` (same libraries, Gradle/Kotlin/Compose versions).
- Clarity without hidden code-generation "magic" (easy dependency tracing for a Java developer).
- Fast declarative UI iteration for interactive math screens.

## Proposed Decision

- **Language & UI:** Kotlin + Jetpack Compose (Material 3), Navigation Compose (`NavHost` + separate scaffolds).
- **Platform:** Android (`minSdk = 31`, `compileSdk = 35`, `targetSdk = 35`, Java 17 bytecode).
- **Dependency Injection:** Manual DI via `AppContainer` / `AppDataContainer` interfaces and a central `AppViewModelProvider.Factory` (same pattern as `onbudget`).
- **Screen State:** `ViewModel` + `StateFlow` with unidirectional data flow (UDF).

## Alternatives Considered

### Alternative 1: Hilt / Dagger DI

- **Pros:** Enterprise Android standard, less manual factory boilerplate.
- **Cons:** Extra annotation/KSP layer and harder debugging for a small project; unnecessary complexity for a handful of ViewModels.

### Alternative 2: Classic Android Views + XML in Java

- **Pros:** Familiar from older Android development.
- **Cons:** Much more boilerplate for interactive UI (e.g., dynamic column subtraction boards with borrow markers and custom keypads).
