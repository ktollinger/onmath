# ADR 0002: Čistý Kotlin matematický engine oddělený od Androidu

- **Status:** Accepted
- **Date:** 2026-11-19
- **Author:** Kamil / Antigravity

## Context and Problem Statement

Aplikace pokrývá tři odlišné matematické oblasti: malou násobilku, výrazy s prioritou operací a závorkami, a písemné odčítání dlouhých čísel (2–6 cifer) s přenosem. Pokud by se generování příkladů, vyhodnocování výrazů a výpočet přenosů míchaly přímo do `ViewModel` nebo Compose komponent, kód by se špatně testoval a snadno by vznikal nekonzistentní stav.

## Decision Drivers

- 100% deterministické a rychle spustitelné unit testy (JUnit na JVM bez emulátoru).
- Přehledný doménový kód v čistém Kotlinu (blízký standardní objektové Javě).
- Záruka pedagogické správnosti generovaných příkladů (např. dělení vždy beze zbytku, nezáporné mezivýsledky, skutečný výskyt přenosu u odčítání).

## Proposed Decision

Veškerá matematická logika je izolována v balíčku `cz.kt.onmath.domain` bez jakýchkoliv závislostí na `android.*` nebo `androidx.compose.*`:

- **Generátory úloh** pro každou oblast (`MultiplicationGenerator`, `ExpressionGenerator`, `SubtractionGenerator`).
- **Vyhodnocovač výrazů a kroků** (AST / parser respektující priority `*`, `/` nad `+`, `-` a závorky) umožňující nejen spočítat výsledek, ale i určit další krok pro nápovědu.
- **Model písemného odčítání** reprezentující jednotlivé řády zprava doleva, očekávané cifry výsledku i místa, kde dochází k přenosu.

## Alternatives Considered

### Alternative 1: Logika přímo ve ViewModelech

- **Pros:** Méně souborů na začátku.
- **Cons:** Nutnost mockovat Android/Coroutines/DB při testování matematických pravidel, horší znovupoužitelnost.
