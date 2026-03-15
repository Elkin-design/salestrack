# SalesTrack KMP - Implementation Plan

This plan details the implementation of **SalesTrack Inventory & Sales Manager**, a Kotlin Multiplatform application with total feature parity between Android and Desktop, designed with Clean Architecture and MVVM patterns.

## Architecture & Principles

- **Clean Architecture**: The logic is highly decoupled across three layers within the shared module:
  - `Domain`: Enterprise business rules, entities, boundary interfaces. Pure Kotlin, completely independent of frameworks.
  - `Data`: Repository implementations, Data Sources (Local SQLDelight, Remote Firebase).
  - `Presentation`: ViewModels holding state via `StateFlow`. UI Events and intents following MVVM.
- **Kotlin Multiplatform (KMP)**: 100% of business logic, ViewModels, Database schemas, and Network calls reside in the `:shared` module.
- **UI Framework**:
  - Android: Jetpack Compose with Material Design 3.
  - Desktop: Compose Multiplatform for Desktop (JVM).
- **Testing Approach**: 
  - The business logic (Use Cases) and ViewModels will be unit-tested. 
  - We will use **Mockito** (or **MockK**, the idiomatic mocking framework for Kotlin) for mocking dependencies in tests.

## Proposed Changes

### 1. Project Setup
- **KMP Base**: Target `android` and `desktop (jvm)`.
- **Dependencies**: 
  - UI: `org.jetbrains.compose` suite.
  - DI: `io.insert-koin:koin-core` and Koin for Android/Compose.
  - Local DB: `app.cash.sqldelight:android-driver`, `sqlite-driver` (JVM), and `coroutines-extensions`.
  - Remote DB & Auth: `dev.gitlive:firebase-auth` and `firebase-firestore` (or native SDKs abstracted via expect/actual if needed).
  - Testing: `kotlin.test`, `mockk` (or `mockito-kotlin`), `kotlinx-coroutines-test`.

### 2. Core/Shared Module Features (`:shared`)
- **Domain Entities**: `User`, `Business`, `Product`, `Sale`, `StockMovement`.
- **Use Cases**: `RegisterSaleUseCase`, `SyncOfflineDataUseCase`, `CalculateDailyReportUseCase`.
- **Repositories**: `AuthRepository`, `SalesRepository`, `ProductRepository`.
- **ViewModels**: `SalesViewModel`, `AuthViewModel`, `ReportViewModel` encapsulating UI state (`StateFlow`) and intents.

### 3. Android Platform App (`:androidApp`)
- Compose Material 3 based components with mobile UX (Bottom Navigation, FABs, Bottom Sheets).
- Integration with Android specific capabilities (ML Kit for camera barcode, Intents for sharing, Android Print, Notifications with FCM).

### 4. Desktop Platform App (`:desktopApp`)
- Compose Desktop based components with large-screen UX (Sidebar, Data Tables with sorting, Modals).
- Integration with Desktop specific capabilities (File selection dialogs for Export/Import CSV, Windows Notifications, HID scanner natively handled as keyboard input).

## Advanced Behaviors (Per Requirements)
- **Offline First**: All actions write to SQLDelight first. A `SyncManager` watches for local unsynced changes and pushes to Firestore when connectivity is restored.
- **Reporting Parity**: PDF, Excel, and CSV generation must be unified. We will use Java/Kotlin libraries capable of running on JVM and Android (e.g., Apache POI or similar lightweight alternatives).

## Verification Plan

### Automated Tests
- Unit testing domain Logic & Use Cases to ensure calculation bounds, discounting logic, etc.
- Unit testing ViewModels for exact state emissions upon intents.
- Run tests via `./gradlew test` (or equivalent Gradle commands).

### Manual Verification
- Deploying `.apk` locally to Android device/emulator.
- Running Desktop app via `./gradlew :desktopApp:run` to ensure fast startup and feature parity.
- Validating offline synchronization manually (turning off networking, entering a sale, turning on networking, and verifying Firestore sync).
