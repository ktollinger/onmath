# OnMath

Offline Android aplikace v Kotlinu a Jetpack Compose pro procvičování matematiky na úrovni 4. třídy ZŠ — bez reklam, bez sběru dat, bez účtů a bez notifikací.

## Hlavní funkce

- **Písemné odčítání pod sebou** (`2–6` cifer)
  - Podpora dvou metod přenosu: půjčování nahoře (Montessori / anglosaský zápis) i přenos dole (český školní zápis).
  - Zadávání výsledku zprava doleva pomocí vestavěné numerické klávesnice, nápověda ke konkrétnímu řádu a ruční potvrzení přechodu na další příklad.
- **Násobilka a dělení**
  - Výběr konkrétních řad (`1–10`) a volitelné zapnutí dělení v rámci násobilky.
- **Výrazy a priority operací**
  - Příklady se závorkami i bez závorek, s přepínačem obtížnosti (do `10 × 10` vs. těžší násobení typu `18 × 3`).
  - **Interaktivní mezivýsledky jako na papíře:** původní zadání zůstává viditelné, dítě si může klepnutím na operátor zapsat mezivýsledek pod příslušný podvýraz a plynule pokračovat dalším operátorem. Mezivýsledky jsou volitelné.
- **Lokální statistiky a chyby**
  - Úspěšnost lekcí a přehled chyb ukládané čistě lokálně (Room + DataStore).

## Zásady projektu

- **100% offline:** Aplikace nevyžaduje oprávnění `INTERNET` a nekomunikuje se žádným serverem.
- **Respekt k dítěti:** Žádné reklamy, žádné notifikace, žádné nátlakové denní streaky.

## Architektura a dokumentace

- `app/src/main/java/cz/kt/onmath/domain/` — čistě kotlinové matematické jádro a generátory příkladů (bez závislostí na Androidu).
- `app/src/main/java/cz/kt/onmath/data/` — lokální perzistence (Room databáze + Preferences DataStore).
- `app/src/main/java/cz/kt/onmath/ui/` — Jetpack Compose obrazovky, ViewModely a sdílené komponenty (`MathKeypad`).
- [Architektonická rozhodnutí (ADR)](docs/adr/)
- [Plán rozvoje (Roadmap)](docs/ROADMAP.md)

## Sestavení a testy

Požadavky: Android SDK (compileSdk 35, minSdk 31), JDK 17.

```bash
./gradlew testDebugUnitTest assembleDebug
```

## Licence

Tento projekt je licencován pod licencí [MIT](LICENSE).
