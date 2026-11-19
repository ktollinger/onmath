# ADR 0004: Vlastní numerická klávesnice a duální model přenosů u odčítání

- **Status:** Accepted
- **Date:** 2026-11-19
- **Author:** Kamil / Antigravity

## Context and Problem Statement

1. Při procvičování na telefonu je vyskakování systémové klávesnice pomalé, zakrývá příklad a u písemného odčítání neumožňuje pohodlné zadávání po jednotlivých cifrách zprava doleva.
2. Dítě navštěvuje Montessori školu, kde se u písemného odčítání používá anglosaský způsob přenosu (půjčení z vyššího řádu a snížení horní cifry), zatímco běžné české školy učí přičtení desítky nahoře a připsání jedničky dolů k menšiteli.

## Decision Drivers

- Maximální pohodlí a rychlost zadávání pro dítě na dotykovém displeji (primárně telefon na výšku).
- Možnost přepnout způsob zápisu přenosů v nastavení bez zásahu do výpočetního jádra.

## Proposed Decision

- **Vlastní vestavěná numerická klávesnice** (komponenta `MathKeypad` inspirovaná `CalculatorPad` z `onbudget`) pevně zakotvená ve spodní části obrazovky cvičení (číslice `0–9`, `Backspace`, `Potvrdit`).
- **Parametrizovaná vizualizace přenosů (`SubtractionMethod`):**
  - `BORROW_TOP` (Montessori / anglosaský způsob): kliknutím nad horní cifru si dítě může označit půjčení / snížení vyššího řádu.
  - `CARRY_BOTTOM` (český školní způsob): kliknutím u dolní cifry si dítě může označit přenesenou jedničku k dalšímu řádu menšitele.
- Značení přenosů v UI je volitelná vizuální pomůcka (případně zvýrazněná v režimu Nápověda); správnost příkladu se vyhodnocuje podle výsledných cifer zadávaných zprava doleva.

## Alternatives Considered

### Alternative 1: Systémová numerická klávesnice Androidu

- **Pros:** Není třeba psát vlastní komponentu.
- **Cons:** Různé chování napříč zařízeními, zabírá prostor nepředvídatelně, horší ergonomie pro rychlé zadávání po cifrách.

### Alternative 2: Pevně zadrátovaný pouze jeden způsob odčítání

- **Pros:** Jednodušší UI.
- **Cons:** Pokud se vizuální zápis v aplikaci liší od toho, co dítě dělá ve škole, aplikaci to zbytečně mate.
