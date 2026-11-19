# Roadmap

Planned improvements for `onmath`, while preserving its core principles: **offline, no ads, no data collection, no notifications**.

## 1. Localization (Czech / English / System Default)
- [ ] Extract all hardcoded Czech strings from Kotlin UI files into Android `strings.xml` (`values/` and `values-cs/` / `values-en/`).
- [ ] Add a language preference to `AppSettings` and `SettingsRepository` (`SYSTEM`, `CS`, `EN`).
- [ ] Expose a three-way selector in `SettingsScreen`: *System default*, *Čeština*, *English*.
- [ ] Wire per-app language switching via `AppCompatDelegate.setApplicationLocales(...)` so language changes apply immediately without changing the device language.

## 2. Graphics & Visual Identity
- [x] Design a custom adaptive app icon (replacing the default template). *(Notebook theme)*
- [ ] Add lightweight vector illustrations / badges on `HomeScreen` and `SessionSummaryView`.
- [ ] *Suite branding idea:* Unified `O` monogram family across personal apps — e.g. `O+` for OnMath (see [docs/ideas/icon-o-plus.svg](ideas/icon-o-plus.svg)), `O$` / `O€` for OnBudget.

## 3. Voluntary Quests & Rewards (No Notifications, No Pressure)
- [ ] Define local milestones in the Room database (e.g. *100 correct multiplication problems*, *Error-free session*, *First parentheses solved*).
- [ ] Display a collectible badge / sticker shelf inside the app as purely voluntary motivation — no notifications, no punishing daily streaks, and no online leaderboards.

## 4. iOS Port (iPhone 16 Pro, iPhone SE 2022)
- [x] Extract `domain/` into a shared Kotlin Multiplatform (KMP) module (`shared`).
- [x] Build a native SwiftUI interface tuned for smaller screens like iPhone SE 2022 as well (compact `MathKeypad` and expression tree spacing).
- [x] Implement local persistence on iOS (`UserDefaults` + JSON history).

## 5. Optional Support ("Buy Me a Coffee")
- [ ] Add an optional *Tip Jar* in Settings / About (behind a simple parental gate, such as a harder math problem) using native Google Play Billing / StoreKit 2, keeping the app compliant with store policies and free of ads or tracking.
