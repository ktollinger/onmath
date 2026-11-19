# ADR 0003: Strategie perzistence (Preferences DataStore a Room DB)

- **Status:** Accepted
- **Date:** 2026-11-19
- **Author:** Kamil / Antigravity

## Context and Problem Statement

Aplikace potřebuje ukládat dva různé druhy dat:
1. Uživatelská nastavení a preference (např. metoda značení přenosů u odčítání, počet cifer 2–6, předvolby násobilky).
2. Historii dokončených sad příkladů a evidenci chybovosti jednotlivých příkladů pro sledování pokroku dítěte.

## Decision Drivers

- Rychlé a bezpečné čtení jednoduchých preferencí přes `Flow`.
- Strukturované dotazování nad historií lekcí a problematickými příklady.
- Konzistence s `onbudget` (Room + KSP).

## Proposed Decision

- **Preferences DataStore** (`androidx.datastore:datastore-preferences`) pro uložení nastavení aplikace a preferencí jednotlivých cvičení.
- **Room Database** pro uložení historie dokončených lekcí (`PracticeSessionEntity`) a záznamů o chybných/problematických příkladech (`MistakeRecordEntity`).

## Alternatives Considered

### Alternative 1: Vše v SharedPreferences / JSON souborech

- **Pros:** Jednodušší počáteční nastavení.
- **Cons:** Špatně se filtruje a agreguje úspěšnost po oblastech nebo problematických příkladech v čase.

### Alternative 2: Vše v Room DB včetně nastavení

- **Pros:** Jediná knihovna pro perzistenci.
- **Cons:** Zbytečná tabulka pro klíč–hodnota nastavení, která se čtou při každém startu obrazovky.
