# ADR 0006: Standalone Client-Side PWA in TypeScript for the Web Version

- **Status:** Accepted
- **Date:** 2026-11-19
- **Author:** Kamil / Antigravity

## Context and Problem Statement

We want to add a web version of `OnMath` (`webApp/`) self-hosted on a home NAS (`emilka.home`), usable both on desktop browsers and as an installable home-screen PWA on iOS and Android without requiring Xcode or APK sideloading.

Two architectural questions arose:
1. Should the web app reuse `:shared` via `Kotlin/JS` or a server-side REST API?
2. Does the web app need a backend server (e.g., Node/Fastify) to generate problems and persist state?

## Decision Drivers

- Minimal build and runtime complexity for a small home-hosted app.
- Fast startup and native browser ergonomics on mobile (iPhone SE 2022) and desktop.
- Offline capability as a Progressive Web App (PWA).
- Alignment with the developer's existing web stack (`Vue 3 + TypeScript + Vite + pnpm`, as in `ocelot`).

## Proposed Decision

- **Pure client-side PWA (`webApp/`):**
  - Built with **Vue 3 + TypeScript + Vite** (`pnpm`), styled with plain CSS and design tokens, and packaged with `vite-plugin-pwa` (manifest, Service Worker, offline cache, notebook app icon).
  - Settings (`language`, `theme`, topic options) and practice history/mistakes are stored locally in `localStorage` — **no backend or REST API** is required.
- **Native TypeScript `domain/` instead of `Kotlin/JS`:**
  - While KMP `:shared` is ideal for Android ↔ iOS (native Kotlin/JVM and Kotlin/Native), the math domain is only ~400–500 lines of pure logic.
  - Exporting `:shared` via `Kotlin/JS` (`@JsExport`, collection wrappers, Gradle/JDK dependency in Docker web builds) adds more friction than writing and unit-testing the same pure `domain/` logic directly in TypeScript (with Vitest).
- **Hybrid input & theming:**
  - Always display the in-app `MathKeypad` for touch ergonomics, while also supporting physical keyboard input (`0–9`, `Backspace`, `Enter`) on desktop.
  - Provide both **Language** (*System / Čeština / English*) and **Theme** (*System / Light / Dark*) selectors in Settings.
- **Deployment:**
  - Multi-stage `Dockerfile` (`node:alpine` + `pnpm build` → `nginx:alpine`) with `docker-compose.yml` for `emilka.home`.

## Alternatives Considered

### Alternative 1: Share `:shared` via Kotlin/JS (`js(IR)`)

- **Pros:** Single source of truth for math generators across Android, iOS, and Web.
- **Cons:** Requires `@JsExport` annotations, Kotlin-to-JS collection bridging, and a JDK + Gradle toolchain inside web CI/Docker builds for a very small domain layer.

### Alternative 2: Server-side REST API for problem generation

- **Pros:** Centralizes generator logic on the server.
- **Cons:** Adds network latency, breaks offline PWA usage, and introduces unnecessary backend deployment/maintenance complexity.
