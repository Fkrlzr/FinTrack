# FinTrack

A local-first Android app for personal income and expense tracking. No account, no login, no network access. All data stays on the device.

## Features

- **Dashboard:** monthly balance, income and expense totals, and a daily flow chart with a category filter.
- **Transactions:** quick-add sheet with a focused amount field, Income/Expense toggle, category, wallet, note, and date and time pickers.
- **Feed:** transactions grouped by day, swipe to delete with undo, tap to edit, and search by note text or amount across all months.
- **Spending by category:** share of monthly expenses per category, with optional monthly budgets that turn red when exceeded.
- **Wallets:** multiple wallets with opening balances and running balances.
- **Categories:** add, rename, or delete your own. Deleted categories move their entries to _Others_.
- **Recurring entries:** weekly or monthly items such as salary, rent, or subscriptions, created automatically when the app opens.
- **Backup:** export to CSV and import from CSV through the system file picker.
- **App lock:** fingerprint or screen lock, required when the app opens or returns from the background.
- **Currency:** Indonesian Rupiah (IDR), with whole-rupiah amounts.

## Tech stack

- Kotlin 2.0, Jetpack Compose with Material 3
- Room 2.6 for local storage (database version 2, with migration from version 1)
- Kotlin Coroutines and Flow, with MVVM and a repository layer
- AndroidX Biometric for the app lock
- Minimum Android 8.0 (API 26), target Android 15 (API 35)

## Project structure

```
app/src/main/java/com/fintrack/
├── data/
│   ├── backup/        CSV encoder and decoder
│   ├── local/         Room entities, DAOs, database, migrations
│   └── repository/    Repository implementation
├── domain/
│   ├── model/         Plain Kotlin models
│   └── repository/    Repository interface
├── security/          App lock store and biometric prompt
└── ui/
    ├── common/        Shared controls
    ├── dashboard/     Dashboard screen, ViewModel and state
    ├── editor/        Transaction editor sheet
    ├── format/        Money, date and amount helpers
    ├── lock/          Lock screen
    ├── manage/        Wallet, category, recurring, budget and settings sheets
    └── theme/         Colors, typography and shapes
```

Design notes are in [`PRD.md`](PRD.md) and [`DESIGN_GUIDELINES.md`](DESIGN_GUIDELINES.md).

## Building

Requirements:

- JDK 17. A newer JDK (JDK 25 was tested) fails with this project's Gradle version.
- Android SDK with platform 35 and build-tools 35.0.0.

Create `local.properties` in the project root with your SDK path (Android Studio does this automatically):

```properties
sdk.dir=C\:\\Users\\you\\AppData\\Local\\Android\\Sdk
```

Build a debug APK:

```bash
./gradlew assembleDebug        # Windows: gradlew.bat assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

Build a release APK:

```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`. The release build is currently signed with the debug key, which is fine for testing but not for publishing.

## Installing

Copy the APK to your phone and open it. Android will ask you to allow installs from that source. You can also install over USB with `adb install app-debug.apk`.

## Database schema

The Room schema is exported to `app/schemas/`. Each schema version has a JSON file, and every version bump needs a migration in `FinTrackDatabase.kt`.
