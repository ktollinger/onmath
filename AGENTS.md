# AGENTS.md

Pokyny pro vývoj a práci AI agentů v repozitáři `onmath`.

## Přehled a architektura

- **Balíček:** `cz.kt.onmath` (`minSdk = 31`, `targetSdk = 35`, Kotlin + Jetpack Compose + Material 3, manuální DI v `di/AppContainer.kt`).
- **Oddělení vrstev (viz `docs/adr/`):**
  - `domain/` — čistý Kotlin bez Android/Compose importů (generátory příkladů, AST výrazů, evaluace, unit testy). Udržovat nezávislé kvůli budoucímu KMP/iOS sdílení.
  - `data/` — Room (`OnMathDatabase`) a Preferences DataStore (`SettingsRepository`).
  - `ui/` — Compose obrazovky, `ViewModel`y se `StateFlow` a sdílené komponenty v `ui/components/`.

## Ověřování změn

Po každé změně kódu spusť unit testy a debug build:

```bash
./gradlew testDebugUnitTest assembleDebug
```

Novou doménovou logiku v `domain/` vždy pokryj unit testy v `app/src/test/java/cz/kt/onmath/`.

## Zásady UX a produktu

- **Žádné reklamy, žádný tracking, žádné oprávnění `INTERNET`, žádné notifikace.**
- **Stabilní vertikální layout:** Obrazovky procvičování nesmí při zobrazení nápovědy, chyby nebo mezivýsledku vertikálně poskakovat (používej ukotvení shora a pevné/minimální výšky slotů).
- **Bez nuceného přepínání:** Po vyřešení složitějších příkladů (odčítání, výrazy) výsledek zůstává na obrazovce a uživatel pokračuje tlačítkem *Další příklad*.
- **Vlastní klávesnice (`MathKeypad`):** Na obrazovkách procvičování nepoužívej systémovou klávesnici.
- **Dokumentace:** Významné architektonické nebo UX změny zaznamenej jako nové ADR do `docs/adr/` ve stejném formátu jako existující soubory.
