# ADR 0001: Technologický stack a manuální Dependency Injection

- **Status:** Accepted
- **Date:** 2026-11-19
- **Author:** Kamil / Antigravity

## Context and Problem Statement

Vytváříme novou Android aplikaci `OnMath` pro procvičování matematiky na 1. stupni ZŠ. Vývojář má dlouholeté zkušenosti s Javou a starším Androidem (View/XML), ale nikoliv s moderním Kotlinem a Jetpack Compose. Cílem je zvolit takovou architekturu, která bude moderní, udržitelná a zároveň snadno čitelná a pochopitelná při přechodu z Javy — po vzoru referenčního projektu `onbudget` (větev `refactor/6-ui-architecture`).

## Decision Drivers

- Konzistence s projektem `onbudget` (stejné knihovny, verze Gradle/Kotlin/Compose).
- Přehlednost bez skryté „magie“ generovaného kódu (snadné trasování závislostí z pohledu Java vývojáře).
- Rychlý vývoj deklarativního UI pro interaktivní matematické obrazovky.

## Proposed Decision

- **Jazyk a UI:** Kotlin + Jetpack Compose (Material 3), Navigation Compose (`NavHost` + oddělený scaffold).
- **Platforma:** Android (`minSdk = 31`, `compileSdk = 35`, `targetSdk = 35`, Java 17 bytecode).
- **Dependency Injection:** Manuální DI pomocí rozhraní `AppContainer` / `AppDataContainer` a centrální `AppViewModelProvider.Factory` (stejný vzor jako v `onbudget`).
- **Stav obrazovek:** `ViewModel` + `StateFlow` s jednosměrným tokem dat (UDF).

## Alternatives Considered

### Alternative 1: Hilt / Dagger DI

- **Pros:** Standard v enterprise Androidu, méně ručního psaní factory tříd.
- **Cons:** Další vrstva anotací, KSP generování a obtížnější ladění pro menší projekt; zbytečná komplexita oproti několika ViewModelům.

### Alternative 2: Klasické Android Views + XML v Javě

- **Pros:** Známé prostředí ze staršího Androidu.
- **Cons:** Výrazně pracnější tvorba interaktivního UI (např. dynamické sloupce odčítání pod sebou s přenosy a vlastní klávesnice).
