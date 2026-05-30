# KKH Wallet

A personal finance Android app for multi-wallet, multi-credit-card households in Indonesia. Built with Kotlin, Jetpack Compose, MVVM + Clean Architecture, Room, and Hilt.

## Features

- **Multi-wallet tracking** — Cash, Bank, E-Wallet, Credit Card, and Paylater. Each wallet type has its own balance/limit model and gradient styling.
- **Transactions** — Expense, Income, Transfer (between balance wallets), and Credit Payment (balance → credit). Every transaction atomically updates the affected wallet's balance or used-limit in a single Room transaction, so balances never drift out of sync.
- **Dashboard** — Total balance, monthly income vs. expense, recent transactions, donut chart of expense-by-category, and horizontal wallet cards.
- **Analytics** — 6-month expense and income bar charts, top categories this month (donut), and credit/paylater utilization breakdown.
- **Search & filter** — Filter transactions by type, wallet, or free-text search across description, merchant, tags, category, and wallet name.
- **Backup & Restore** — One-tap JSON export/import to any URI (Drive, Files, etc.). Restore wipes existing data first inside a single transaction.
- **Material 3** — Dynamic light/dark themes with edge-to-edge layouts, custom typography, and a fintech-style gradient palette.
- **IDR formatting** — All money is formatted as `Rp1.250.000` using `id_ID` locale grouping.

## Tech stack

| Layer        | Library                                          |
|--------------|--------------------------------------------------|
| UI           | Jetpack Compose (Material 3), Navigation Compose |
| State        | ViewModel + StateFlow + `collectAsStateWithLifecycle` |
| DI           | Hilt 2.51.1                                      |
| Persistence  | Room 2.6.1 (KSP), DataStore Preferences          |
| Background   | Coroutines, WorkManager (scaffolded)             |
| Serialization| kotlinx.serialization (JSON)                     |
| Build        | AGP 8.5.2, Kotlin 2.0.20, Compose Compiler plugin 2.0.20 |

Min SDK 29, Target SDK 34, JDK 17.

## Project structure

```
com.kkh.wallet
├── data
│   ├── backup/        BackupManager (JSON export/import)
│   ├── local/         Room: KKHDatabase, entities, DAOs, type converters
│   ├── mapper/        Entity ↔ Domain mappers
│   ├── preferences/   DataStore: ThemeMode, biometric flag, currency
│   └── repository/    Repository impls (financial logic lives here)
├── domain
│   ├── model/         Wallet, Transaction, Category, Budget, enums
│   └── repository/    Repository interfaces
├── di/                Hilt modules (DatabaseModule, RepositoryModule)
├── presentation
│   ├── components/    WalletCard, TransactionItem, Charts, IconRegistry
│   ├── navigation/    Screen routes + KKHNavHost
│   ├── screens/       dashboard, wallets, addeditwallet, walletdetail,
│   │                  addtransaction, transactions, transfer, analytics,
│   │                  settings — each as <Screen> + <ViewModel>
│   └── theme/         Color, Type, Theme
├── util/              CurrencyFormatter, DateUtils
├── KKHWalletApplication.kt   (Hilt entry, notification channels, seed)
└── MainActivity.kt
```

## Financial logic at a glance

`TransactionRepositoryImpl` is the single source of truth for any wallet balance change. Every `add`, `update`, and `delete` runs inside `database.withTransaction { … }`. The effect rules:

| Transaction      | Source effect              | Destination effect           |
|------------------|----------------------------|------------------------------|
| EXPENSE (balance) | `balance -= amount`        | —                            |
| EXPENSE (credit)  | `usedLimit += amount`      | —                            |
| INCOME (balance)  | `balance += amount`        | —                            |
| INCOME (credit)   | `usedLimit -= amount`      | —                            |
| TRANSFER          | `balance -= amount`        | `balance += amount`          |
| CREDIT_PAYMENT    | `balance -= amount`        | `usedLimit -= amount`        |

Updating a transaction reverses the old effect and applies the new one inside one transaction; deleting reverses the effect.

## Building

### Option A — Android Studio (recommended)

1. Install Android Studio Hedgehog or newer.
2. **File → Open** and select the unzipped `KKHWallet/` folder.
3. Wait for Gradle sync to finish. Android Studio will auto-generate the Gradle wrapper.
4. **Run → Run 'app'** on an emulator or connected device (API 29+).

### Option B — Command line

You need a system Gradle ≥ 8.7 (only for the first wrapper generation):

```bash
cd KKHWallet
gradle wrapper --gradle-version 8.9       # one-time, creates gradlew + jar
./gradlew assembleDebug                    # → app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease                  # → app/build/outputs/apk/release/app-release-unsigned.apk
./gradlew bundleRelease                    # → app/build/outputs/bundle/release/app-release.aab
```

JDK 17 is required. If you don't have a system Gradle, just open the project in Android Studio once; the wrapper jar will be generated automatically.

## What's fully working vs. scaffolded

**Fully working**
- Wallet CRUD with type-aware fields (balance vs. credit limit/used limit/billing/due day)
- Transaction CRUD with atomic balance updates and edit-time effect reversal
- Multi-wallet dashboard with monthly summaries, donut chart, and wallet carousel
- Wallet detail view with utilization, this-month spend, and per-wallet transaction list
- Transactions screen with type/wallet filtering and free-text search
- Transfer screen that auto-routes to `CREDIT_PAYMENT` when destination is a credit wallet
- Analytics: 6-month bar charts, top categories donut, credit utilization
- Settings: theme mode switch, JSON backup & restore via SAF
- Light/dark Material 3 theming, edge-to-edge, custom gradient wallet cards
- IDR formatting, Indonesian date formatting

**Scaffolded (deps wired but not implemented end-to-end)**
- Biometric unlock toggle (UI switch only; no actual `BiometricPrompt` invocation)
- Push notifications for bill reminders (channels created, but no `WorkManager` jobs scheduled)
- Receipt image attachments (model field exists; no image picker UI)
- Budgets (model + DAO + repo present; no dedicated screen yet)
- PIN code (UI flow not implemented; the `pinHash` preference key is reserved)

These are intentionally left as extension points — the architecture is in place, just plug in the implementation.

## Default categories

On first launch, 12 categories are seeded: Food, Transportation, Shopping, Bills, Entertainment, Health, Education, Transfer, Credit Card Payment, Other (expense) and Salary, Investment (income).

## License

Personal project — no license bundled. Customize as you wish.
