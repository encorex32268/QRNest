# Tasks

## 1. Gradle 9 Build-Logic & Multi-Module Scaffolding

- [x] 1.1 Update `gradle/libs.versions.toml` with project version constants (`projectApplicationId`, `projectMinSdkVersion = 26`, `projectCompileSdkVersion = 36`, `projectTargetSdkVersion = 36`), Gradle plugin artifacts, Koin, Compose Navigation, Lifecycle/SavedStateHandle, Room KMP + Bundled SQLite, `kotlinx-datetime`, `kotlinx-serialization-json`, QRose, CameraX, ML Kit Barcode Scanning, AssertK, and Turbine, and verify `./gradlew help` succeeds.
- [x] 1.2 Create `:build-logic` (`settings.gradle.kts`, `gradle.properties`, `convention/build.gradle.kts`) and implement convention plugins (`KmpLibraryConventionPlugin`, `CmpLibraryConventionPlugin`, `CmpFeatureConventionPlugin`, `CmpApplicationConventionPlugin`, `AndroidApplicationConventionPlugin`, `AndroidApplicationComposeConventionPlugin`, `RoomConventionPlugin`) under `com.lihan.convention`, and verify `./gradlew :build-logic:convention:check` passes.
- [x] 1.3 Scaffold module directories and `build.gradle.kts` files for `:composeApp`, `:core:{domain,database,data,designsystem,presentation}`, and `:feature:{scan,generate,history,preview}:{domain,data,presentation}`, remove legacy `:shared`, update `settings.gradle.kts` and `iosApp/iosApp.xcodeproj/project.pbxproj` (`:composeApp:embedAndSignAppleFrameworkForXcode`), and verify `./gradlew projects` lists all modules cleanly.

## 2. Core Domain, Database, and Data Layers

- [x] 2.1 Implement `Error`, `Result<D, E>`, `EmptyResult<E>`, `map`/`onSuccess`/`onFailure`/`asEmptyResult` extensions, `DataError`, `QRCodeType`, `QRCodeHistory`, `ScannedBarcode`, `BarcodePayloadParser`, and `HistoryRepository` in `:core:domain`, and verify `BarcodePayloadParser` unit tests in `:core:domain:commonTest` pass on Android host JVM and iOS simulator.
- [x] 2.2 Implement `QRCodeHistoryEntity`, `QRCodeHistoryDao` (using `Long` IDs throughout), `QRNestDatabase`, `QRNestDatabaseConstructor`, and platform `RoomDatabase.Builder` (`androidMain` and `iosMain` with `BundledSQLiteDriver`) in `:core:database`, and verify `:core:database:assembleAndroidMain` and `:core:database:compileKotlinIosSimulatorArm64` succeed.
- [x] 2.3 Implement `RoomHistoryRepository`, entity-domain mappers, and `coreDataModule` / `coreDatabaseModule` in `:core:data`, and verify unit tests with `FakeHistoryRepository` in `:core:data:commonTest` pass.

## 3. Core Design System & Shared Presentation

- [x] 3.1 Migrate SUSE font files (`suse_regular.ttf`, `suse_medium.ttf`, `suse_semibold.ttf`) and all vector drawable icons from QRCraft into `:core:designsystem/src/commonMain/composeResources/`, implement `QRNestTheme`, `AppColors`, `Typography`, `QRNestIcons`, `QRNestButton`, `QRNestIconButton`, `QRNestSnackbar`, `CircleIcon`, and `TextLinkButton` with `@Preview` functions in `commonMain`, and verify `:core:designsystem:assembleAndroidMain` and `:core:designsystem:compileKotlinIosSimulatorArm64` pass.
- [x] 3.2 Implement `UiText`, `DataError.toUiText()`, `ObserveAsEvents`, `TimeFormat` (using `kotlinx-datetime`), `QRCodeHistoryUi` + mapper, `TopLevelDestination`, `BottomNavigation`, `AdaptiveNavigationRail`, `MainAdaptiveLayout` (using multiplatform `BoxWithConstraints` for >= 600.dp breakpoint), and `ExternalAppLauncher` (`androidMain` Intents and `iosMain` `UIApplication` / `UIActivityViewController` / `CNContactViewController`) in `:core:presentation`, and verify `:core:presentation` compiles for Android and iOS and `TimeFormat` unit tests pass.

## 4. Feature: Generate & Create QR Code

- [x] 4.1 Implement `QrPayloadFormatter` in `:feature:generate:domain` for formatting `Text`, `Link`, `Contact`, `PhoneNumber`, `Geolocation`, and `WiFi` inputs, and verify unit tests in `:feature:generate:domain` cover boundary, unicode/emoji, and malformed coordinate inputs.
- [x] 4.2 Implement `GenerateRoute`, `CreateRoute`, `generateGraph`, `GenerateScreenRoot`/`GenerateScreen`, `CreateState`, `CreateAction`, `CreateEvent`, `CreateViewModel` (with `SavedStateHandle` persistence), `CreateScreenRoot`/`CreateScreen`, `GenerateTypeCard`, `CreateQRTextField`, and `generatePresentationModule` in `:feature:generate:presentation`, and verify `CreateViewModelTest` passes in `commonTest` on both Android and iOS targets.

## 5. Feature: Scan History

- [x] 5.1 Implement `HistoryRoute`, `historyGraph`, `ScanHistoryState` (`@Stable`), `ScanHistoryAction`, `ScanHistoryEvent`, `ScanHistoryViewModel`, `ScanHistoryScreenRoot`/`ScanHistoryScreen`, `QRCodeHistoryList`, `QRCodeHistoryItem`, and `historyPresentationModule` in `:feature:history:presentation`, and verify `ScanHistoryViewModelTest` passes in `commonTest` on both Android and iOS targets.

## 6. Feature: Preview & Export Actions

- [x] 6.1 Implement `ClipboardManager` and `QrImageSaver` interfaces in `:feature:preview:domain` and their platform implementations in `:feature:preview:data` (`AndroidClipboardManager` / `IosClipboardManager` via `UIPasteboard`, and 512x512 QRose rendering + 60px white border padding saved via `MediaStore.Downloads` on Android and `PHPhotoLibrary`/Documents on iOS), and verify `:feature:preview:data` compiles for Android and iOS.
- [x] 6.2 Implement `PreviewRoute`, `previewGraph`, `PreviewState`, `PreviewAction`, `PreviewEvent`, `PreviewViewModel` (with `SavedStateHandle` route arguments and title editing), `PreviewScreenRoot`/`PreviewScreen`, `QRCodePreviewCard`, and `previewPresentationModule` in `:feature:preview:presentation`, and verify `PreviewViewModelTest` passes in `commonTest` on both Android and iOS targets.

## 7. Feature: Camera & Image QR Scanning

- [x] 7.1 Implement `QrImageScanner` interface in `:feature:scan:domain` and platform implementations in `:feature:scan:data` (`MlKitQrImageScanner` in `androidMain` and `VisionQrImageScanner` using `VNDetectBarcodesRequest` in `iosMain`), and verify `:feature:scan:data` compiles on both platforms.
- [x] 7.2 Implement multiplatform `CameraPermissionController` (`Manifest.permission.CAMERA` on Android; `AVCaptureDevice` authorization on iOS), `ImagePickerLauncher` (`PickVisualMedia` on Android; `PHPickerViewController` on iOS), and `CameraScannerView` (CameraX + ML Kit analyzer + torch on Android; `AVCaptureSession` + `AVCaptureMetadataOutput` + torch on iOS) in `:feature:scan:presentation`, and verify both targets compile cleanly.
- [x] 7.3 Implement `ScanRoute`, `scanGraph`, `ScanState`, `ScanAction`, `ScanEvent`, `ScanViewModel`, `ScanScreenRoot`/`ScanScreen`, `ScanFrame`, `CameraPermissionDialog`, `NoQRCodeFoundDialog`, and `scanPresentationModule` in `:feature:scan:presentation`, and verify `ScanViewModelTest` passes in `commonTest` on both Android and iOS targets.

## 8. App Wiring, Platform Configuration, and End-to-End Verification

- [x] 8.1 Wire `App()`, `QRNestAppState`, root `NavHost` (connecting `scanGraph`, `generateGraph`, `historyGraph`, `previewGraph`), and `initKoin()` in `:composeApp`, update `:androidApp` (`QRNestApp`, `MainActivity` with `installSplashScreen()`, `AndroidManifest.xml`, splash resources) and `iosApp` (`iOSApp.swift` calling `InitKoinKt.doInitKoin(config: nil)`, `ContentView.swift` importing `ComposeApp`, `Info.plist` usage descriptions), and verify `:androidApp:assembleDebug` and `:composeApp:linkDebugFrameworkIosSimulatorArm64` succeed.
- [x] 8.2 Run the full multiplatform unit test suite across all modules (`./gradlew testAndroidHostTest iosSimulatorArm64Test`) and verify all tests pass with zero failures.
