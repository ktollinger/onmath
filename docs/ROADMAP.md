# Plán rozvoje (Roadmap)

Úkolovník budoucích vylepšení pro `onmath` při zachování základních principů: **offline, bez reklam, bez sběru dat, bez notifikací**.

## 1. Lokalizace (Čeština / Angličtina / Automaticky)
- [ ] Extrahovat všechny texty z Kotlin souborů (`ui/`) do Android `strings.xml` (`values/` a `values-cs/` / `values-en/`).
- [ ] Přidat do `AppSettings` a `SettingsRepository` volbu jazyka (`SYSTEM`, `CS`, `EN`).
- [ ] V `SettingsScreen` nabídnout výběr: *Automaticky (podle systému)*, *Čeština*, *English*.
- [ ] Napojit přepínání jazyka přes `AppCompatDelegate.setApplicationLocales(...)` tak, aby změna platila okamžitě bez nutnosti měnit jazyk telefonu.

## 2. Grafika a vizuální identita
- [ ] Navrhnout vlastní adaptive ikonu aplikace (místo výchozí šablony).
- [ ] Doplnit jednoduché vektorové ilustrace / odznaky na `HomeScreen` a v obrazovce dokončení lekce (`SessionSummaryView`).

## 3. Dobrovolné úkoly a odměny (bez notifikací a nátlaku)
- [ ] Definovat lokální milníky v Room databázi (např. *100 správných příkladů z násobilky*, *Lekce bez chyby*, *První vyřešené závorky*).
- [ ] Zobrazit sbírku odznaků / samolepek v aplikaci jako čistě dobrovolnou motivaci — bez notifikací, bez trestání za přestávku (žádné denní streaky) a bez online žebříčků.

## 4. Verze pro iOS (iPhone 16 Pro, iPhone SE 2022)
- [ ] Vydělit `domain/` do sdíleného Kotlin Multiplatform (KMP) modulu (`shared`).
- [ ] Postavit nativní SwiftUI rozhraní optimalizované i pro menší displej iPhone SE 2022 (kompaktní `MathKeypad` a strom výrazů).
- [ ] Lokální ukládání výsledků a nastavení na iOS (`UserDefaults` + SwiftData/SQLite).

## 5. Dobrovolná podpora („Kafe pro vývojáře“)
- [ ] Přidat do Nastavení / „O aplikaci“ (za jednoduchý rodičovský zámek, např. těžší příklad) volitelný *Tip Jar* přes nativní Google Play Billing / StoreKit 2, aby aplikace zůstala v souladu s pravidly obchodů i bez reklam a sledování.
