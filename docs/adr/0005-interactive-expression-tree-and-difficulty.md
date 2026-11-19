# ADR 0005: Interaktivní strom výrazů, mezivýsledky a přepínač obtížnosti

- **Status:** Accepted
- **Date:** 2026-11-19
- **Author:** Kamil / Antigravity

## Context and Problem Statement

1. Pro žáka 4. třídy je obtížné držet v paměti více mezivýsledků najednou (např. `(9 + 9) × 3` nebo `(2 + 3) × 2 + 4 × 5`).
2. Nahrazení původního podvýrazu v textu (`(14 − 7)` → `7`) sice výraz zkrátí, ale dítě ztrácí přehled o tom, kde původní výraz byl (na papíře zadání negumuje, ale připisuje mezivýsledky pod něj).
3. Při zadávání mezivýsledku a následném kliknutí na další operátor je nepřirozené muset pokaždé potvrzovat tlačítkem `✓` na klávesnici.
4. Některé vygenerované příklady vedou na násobení mimo malou násobilku (např. `18 × 3`), což je vhodné mít oddělené jako volitelnou vyšší obtížnost.

## Decision Drivers

- Zachovat původní zadání výrazu vždy čitelné a na jednom místě.
- Udržet přehlednost i u příkladů s více mezivýsledky a zabránit vertikálnímu poskakování rozhraní.
- Nenutit dítě vyplňovat mezivýsledky u triviálních příkladů.

## Proposed Decision

- **Vizuální strom výrazu (`ExpressionsScreen`):**
  - Původní výraz zůstává vždy zobrazený v horní řadě.
  - Operátory, jejichž oba operandy jsou už vyhodnocené (čísla nebo zapsané mezivýsledky), jsou zvýrazněné jako klikací tlačítka.
  - Zadané mezivýsledky se vykreslují s podtržením přímo **pod** příslušným podvýrazem v rekurzivní stromové struktuře.
- **Plynulá interakce (`ExpressionsViewModel` + `ExpressionInteractive`):**
  - Mezivýsledky jsou volitelné — výchozí fokus je na celkovém výsledku.
  - Pokud dítě zadá číslici do mezivýsledku a rovnou klepne na další operátor nebo na pole celkového výsledku, rozpracovaný mezivýsledek se automaticky ověří a uloží.
  - Po vyřešení příkladu zůstává výsledek zobrazený až do ručního potvrzení tlačítkem *Další příklad*.
- **Obtížnost (`ExpressionGenerator` + `AppSettings`):**
  - Ve výchozím nastavení (`expressionLargeIntermediate = false`) zůstávají všechna násobení a dělení v rozsahu malé násobilky do `10 × 10`.
  - Přepínač v nastavení umožňuje zapnout těžší varianty s mezivýsledky nad `10` (např. `18 × 3`).

## Alternatives Considered

### Alternative 1: Přepisování původního výrazu v místě (in-place substituce)

- **Pros:** Šetří vertikální prostor.
- **Cons:** Dítě ztrácí vizuální vazbu na původní zadání a musí si ho číst v odděleném řádku nad příkladem.

### Alternative 2: Vynucené krokování po operacích bez možnosti zadat rovnou výsledek

- **Pros:** Zaručí procvičení pořadí operací u každého příkladu.
- **Cons:** Zbytečně zdržuje u triviálních příkladů typu `(1 + 1) × 1 − 1 ÷ 1`.
