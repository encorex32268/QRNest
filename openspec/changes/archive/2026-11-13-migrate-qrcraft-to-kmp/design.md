# Design

## Context

`QRCraft` (`/Users/lihan/SideProject/QRCraft`) is a single-module Android application using Jetpack Compose, CameraX, ML Kit Barcode Scanning, Accompanist Permissions, Room, QRose, and Koin. `QRNest` (`/Users/lihan/Downloads/QRNest`) is initialized with Gradle 9.5.1, AGP 9.1.1, Kotlin Multiplatform, and Compose Multiplatform, currently containing a starter `:shared` and `:androidApp` setup. See `proposal.md` for motivation.

Key technical constraints:
- **Gradle 9 KMP Rules (`kmp-module-structure`)**: KMP modules must apply `com.android.kotlin.multiplatform.library` configured via `KotlinMultiplatformAndroidLibraryExtension`. Android application plugins (`com.android.application`) cannot coexist with KMP plugins in the same module, requiring `:composeApp` (shared KMP library) + `:androidApp` (thin Android app shell).
- **Strict Clean Architecture & Feature Isolation**: `presentation` → `domain` ← `data`. Features (`scan`, `generate`, `history`, `preview`) must never depend on each other; shared models/contracts live in `:core:domain`, shared Room DB in `:core:database`, shared data implementations in `:core:data`, shared UI primitives/theme in `:core:designsystem`, and shared presentation helpers in `:core:presentation`.
- **Missing iOS Platform Implementations**: `QRCraft` relies on Android-only APIs for permissions, live camera scanning, gallery image QR decoding, saving PNG images with padding, clipboard, and external intents. Native iOS implementations using Apple frameworks (`AVFoundation`, `Vision`, `Photos`/`PhotosUI`, `UIKit`, `ContactsUI`) must be designed behind shared KMP abstractions.

## Goals / Non-Goals

**Goals:**
- Establish a 3-tier Gradle 9 convention plugin hierarchy in `:build-logic` (`kmp.library` → `cmp.library` → `cmp.feature`, plus `cmp.application`, `android.application`, `android.application.compose`, `room`).
- Keep 100% of ViewModels, UI state, navigation graphs, Compose screens, and Room database entities/DAOs in `commonMain`.
- Implement full native parity on iOS (`iosMain`) and Android (`androidMain`) for camera permissions, live barcode scanning + torch, gallery image picking + barcode detection, padded PNG generation + saving, clipboard, and external system actions.
- Ensure all ViewModels and domain logic are unit-testable in `commonTest` on both Android JVM (`testAndroidHostTest`) and iOS Simulator (`iosSimulatorArm64Test`) without Robolectric or MockK.

**Non-Goals:**
- Desktop (JVM) or Web target deployment (Android and iOS are the active targets).
- Cloud synchronization or remote backend integration (all QR history remains offline-first in local Room SQLite storage).
- Custom QR visual styling (colors/logos) beyond QRCraft's existing high-contrast QR rendering and 60px padded PNG export.

## Decisions

### 1. Module Topology & Dependency Graph (`kmp-module-structure`)
- **Decision**: Organize the project into:
  - `:build-logic`: Included build providing convention plugins under `com.lihan.convention.*`.
  - `:composeApp`: Shared umbrella KMP library (`cmp.application`) hosting `App()`, root `NavHost`, and `initKoin()`. Exports static framework `ComposeApp` for Xcode.
  - `:androidApp`: Thin Android application shell (`android.application.compose`) hosting `QRNestApp`, `MainActivity`, splash screen, and `AndroidManifest.xml`.
  - `:core:domain`: `Result<D, E>`, `Error`, `DataError`, `EmptyResult`, `QRCodeType` (pure Kotlin integer constants matching barcode types without ML Kit imports), `QRCodeHistory`, `ScannedBarcode`, `BarcodePayloadParser`, `HistoryRepository` interface.
  - `:core:database`: `QRNestDatabase`, `QRCodeHistoryEntity`, `QRCodeHistoryDao` (fixing `getHistoryById` parameter from `Int` to `Long`), `QRNestDatabaseConstructor`, and platform `RoomDatabase.Builder` (`BundledSQLiteDriver`).
  - `:core:data`: `RoomHistoryRepository` and `coreDataModule`.
  - `:core:designsystem`: `QRNestTheme`, `AppColors`, `Typography` (SUSE fonts in `composeResources/font`), `QRNestIcons` (vector drawables in `composeResources/drawable`), `QRNestButton`, `QRNestIconButton`, `QRNestSnackbar`, `CircleIcon`, `TextLinkButton`.
  - `:core:presentation`: `UiText`, `DataError.toUiText()`, `ObserveAsEvents`, `TimeFormat` (`kotlinx-datetime`), `QRCodeHistoryUi` + mappers, `TopLevelDestination`, `BottomNavigation`, `AdaptiveNavigationRail`, `MainAdaptiveLayout` (using `BoxWithConstraints` with `maxWidth >= 600.dp` instead of Android-only `LocalConfiguration`), and `ExternalAppLauncher`.
  - `:feature:scan:{domain,data,presentation}`: `QrImageScanner` interface (`suspend fun scanImage(bytes: ByteArray): Result<ScannedBarcode, DataError.Local>`) in `domain`; ML Kit (`androidMain`) and Apple Vision `VNDetectBarcodesRequest` (`iosMain`) implementations in `data`; `ScanViewModel`, `ScanScreen`, `CameraScannerView`, `CameraPermissionController`, and `ImagePickerLauncher` in `presentation`.
  - `:feature:generate:{domain,data,presentation}`: `QrPayloadFormatter` in `domain`; `GenerateScreen`, `CreateScreen`, `CreateViewModel` in `presentation`.
  - `:feature:history:{domain,data,presentation}`: `ScanHistoryViewModel`, `ScanHistoryScreen`, `QRCodeHistoryList`, `QRCodeHistoryItem` in `presentation`.
  - `:feature:preview:{domain,data,presentation}`: `ClipboardManager`, `QrImageSaver` interfaces in `domain`; platform clipboard + padded PNG renderer & storage saver in `data`; `PreviewViewModel`, `PreviewScreen`, `QRCodePreviewCard` in `presentation`.
- **Alternatives Considered**: Keeping `:shared` as a single monolithic KMP module. Rejected because the user explicitly requested `kmp-module-structure` with feature-layered Clean Architecture modularization.

### 2. Decoupling Barcode Parsing & ViewModel State from Android/Compose Types (`kmp-presentation-mvi`, `kmp-compose-architecture`)
- **Decision**:
  1. Replace `com.google.mlkit.vision.barcode.common.Barcode` in `ScanAction` and `QRCodeType` with pure Kotlin `ScannedBarcode(val type: Int, val content: String)` and a shared `BarcodePayloadParser` in `:core:domain` that parses standard QR raw strings (`WIFI:...`, `tel:...`, `geo:...`, `BEGIN:VCARD` / `MECARD:`, `http(s)://...`, and multi-line text) into `(type, formattedContent)`.
  2. Replace `android.net.Uri` in `ScanAction.ScanQRCodeImage` with `ByteArray` read by the platform image picker before dispatching to `ScanViewModel`.
  3. Replace Compose `TextFieldState` inside `CreateState` and `PreviewState` with plain `String` properties updated via MVI `Action`s (`OnFirstFieldChange`, `OnSecondFieldChange`, `OnThirdFieldChange`, `OnTitleChange`) and persisted in `SavedStateHandle`.
- **Rationale**: In `QRCraft`, `ScanViewModelTest`, `CreateViewModelTest`, and `PreviewViewModelTest` required `@RunWith(RobolectricTestRunner::class)` and MockK because `Barcode`, `Uri`, and `TextFieldState` leaked into ViewModels. Using pure Kotlin types allows all ViewModel unit tests to run in `commonTest` on both Android JVM and iOS Native (`iosSimulatorArm64Test`) in milliseconds.

### 3. Native iOS Camera, Permissions, Image Picker, and Storage Implementations
- **Decision**:
  - **Camera Permission (`iosMain`)**: Use `AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)` and `AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo)`. If status is `AVAuthorizationStatusDenied` or `AVAuthorizationStatusRestricted`, route "Grant Access" to `UIApplication.sharedApplication.openURL(NSURL(string = UIApplicationOpenSettingsURLString))`.
  - **Live Camera & Torch (`iosMain`)**: Implement `CameraScannerView` using `UIKitView` wrapping an `AVCaptureSession` + `AVCaptureVideoPreviewLayer` + `AVCaptureMetadataOutput` configured for QR and standard 1D/2D barcode types. Torch is controlled via `AVCaptureDevice.lockForConfiguration()` and `setTorchMode(AVCaptureTorchModeOn / Off)`.
  - **Gallery Image Picker & Static Barcode Detection (`iosMain`)**: Present `PHPickerViewController` (filtered to images) from the active `UIViewController`, read image bytes as `ByteArray`, and analyze in `VisionQrImageScanner` using `VNImageRequestHandler` + `VNDetectBarcodesRequest` (converting `ByteArray` → `NSData` → `UIImage` → `CGImage`).
  - **Padded PNG Rendering & Saving (`iosMain` / `androidMain`)**: Generate base 512x512 QR bytes via `qrose` (`QrCodePainter.toByteArray(512, 512)`), add a 60px white border (632x632 PNG) using `android.graphics.Bitmap`/`Canvas` on Android and `UIGraphicsImageRenderer` / `UIGraphicsBeginImageContextWithOptions` on iOS. Save to `MediaStore.Downloads` on Android and `PHPhotoLibrary` (`PHAssetCreationRequest.creationRequestForAssetFromImage`) on iOS (with fallback to `NSDocumentDirectory` if photo add permission is denied).
  - **External Actions (`iosMain`)**: Use `UIApplication.sharedApplication.openURL` for `https://`, `tel:`, `http://maps.apple.com/`, and `UIApplicationOpenSettingsURLString`; `UIActivityViewController` (with iPad `popoverPresentationController` anchor) for sharing; and `CNContactViewController` in a `UINavigationController` for adding contacts.
- **Alternatives Considered**: Third-party KMP wrappers (e.g., Moko Permissions, Calf, Peekaboo). Rejected in favor of direct `expect`/`actual` and Koin-bound platform implementations in `androidMain`/`iosMain` to avoid version conflicts with Kotlin 2.x / Compose 1.10+ / Gradle 9 and maintain full control over UX and lifecycle.

## Risks / Trade-offs

- **[Risk] iOS Simulator lacks a hardware camera for live `AVCaptureSession`** → **Mitigation**: Guard `AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)` nullability gracefully on iOS Simulator (rendering the scan frame overlay without crashing) while allowing gallery image picking (`PHPickerViewController` + `VNDetectBarcodesRequest`) to work fully on the simulator.
- **[Risk] Gradle 9 `com.android.kotlin.multiplatform.library` resource and preview changes** → **Mitigation**: Enable `experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true` (or `androidResources { enable = true }`) in `configureKotlinMultiplatform()` and add `androidx-compose-ui-tooling` via `androidMainImplementation` in `cmp.library` and `cmp.application`.
- **[Risk] iPad crash when presenting `UIActivityViewController` without popover anchor** → **Mitigation**: Configure `popoverPresentationController?.sourceView` and centered `sourceRect` on the root `UIViewController.view` whenever presenting `UIActivityViewController` on iOS.

## Migration Plan

1. Set up `gradle/libs.versions.toml` and `:build-logic` convention plugins; replace `:shared` with `:composeApp` and update Xcode build phase to `:composeApp:embedAndSignAppleFrameworkForXcode`.
2. Migrate `:core:domain`, `:core:database`, and `:core:data` with `Result`/`DataError` and Room KMP; verify with unit tests in `commonTest`.
3. Migrate `:core:designsystem` (SUSE fonts, vector icons, theme) and `:core:presentation` (adaptive scaffold, navigation bar/rail, `UiText`, `ExternalAppLauncher`).
4. Migrate `:feature:generate`, `:feature:history`, `:feature:preview`, and `:feature:scan` with Android and iOS platform implementations and unit test suites in `commonTest`.
5. Wire navigation graphs and Koin modules in `:composeApp`, configure `:androidApp` and `iosApp` (`Info.plist`), and verify both Android and iOS builds and test suites.
