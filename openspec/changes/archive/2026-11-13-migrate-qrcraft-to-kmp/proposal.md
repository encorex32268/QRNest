# Proposal

## Why

QRCraft (`/Users/lihan/SideProject/QRCraft`) is currently an Android-only QR code utility application tightly coupled to Android framework APIs (Accompanist Permissions, CameraX, ML Kit, Android MediaStore, `android.net.Uri`, `java.time`). Migrating QRCraft to QRNest as a Kotlin Multiplatform (KMP) and Compose Multiplatform (CMP) project enables sharing UI, presentation (MVI), domain, and local persistence (Room KMP) across Android and iOS while providing native platform implementations for camera scanning, permissions, file/photo storage, image picking, clipboard, and external system actions.

## What Changes

- **BREAKING**: Replace the template `:shared` module with a Gradle 9 multi-module architecture governed by `:build-logic` convention plugins (`kmp.library`, `cmp.library`, `cmp.feature`, `cmp.application`, `android.application.compose`, `room`) and the mandatory `:composeApp` (shared KMP library) + `:androidApp` (thin Android application shell) split.
- Establish shared core modules (`:core:domain`, `:core:database`, `:core:data`, `:core:designsystem`, `:core:presentation`) with typed error handling (`Result<D, E>`, `DataError`, `EmptyResult`), Room KMP database (`QRNestDatabase`), Koin DI, `kotlinx-datetime` time formatting, and the QRCraft Material 3 design system (SUSE typography, vector icons, light/dark color tokens, adaptive navigation rail and bottom bar).
- Implement four decoupled feature module groups (`:feature:scan`, `:feature:generate`, `:feature:history`, `:feature:preview`) following Clean Architecture (`presentation` → `domain` ← `data`) and MVI (`State`, `Action`, `Event`, `ViewModel`, `Root`/`Screen` composables).
- Implement cross-platform camera permission handling, live camera preview with torch control, and real-time barcode/QR detection (CameraX + ML Kit on Android; `AVCaptureSession` + `AVCaptureMetadataOutput` on iOS).
- Implement cross-platform gallery image picking and static image QR detection (`PickVisualMedia` + ML Kit on Android; `PHPickerViewController` + Vision `VNDetectBarcodesRequest` on iOS).
- Implement cross-platform QR image export with white border padding (`MediaStore` Downloads on Android; `PHPhotoLibrary` / Documents on iOS), clipboard copy (`ClipboardManager` on Android; `UIPasteboard` on iOS), and external system actions (browser, phone dialer, maps, Wi-Fi settings, contact creation, share sheet).
- Migrate and expand unit tests in `commonTest` using `kotlin.test`, AssertK, Turbine, `UnconfinedTestDispatcher`, and in-memory fakes so tests run on both Android host JVM and iOS simulator without Robolectric or MockK.

## Capabilities

### New Capabilities
- `kmp-architecture-foundation`: Multi-module Gradle 9 convention plugin setup, Koin dependency injection, type-safe Compose Navigation, adaptive scaffold layout, and shared Material 3 design system across Android and iOS.
- `qr-scanning`: Camera permission lifecycle, live camera QR/barcode scanning with flashlight toggle, and gallery image QR code decoding across Android and iOS.
- `qr-generation`: Multi-type QR code creation (Text, Link, Contact, Phone Number, Geolocation, Wi-Fi) with reactive form validation, payload formatting, and process-death state restoration.
- `qr-history`: Reactive Scanned and Generated QR code history persistence using Room KMP, tabbed browsing, favorite toggling, and item management (share and delete).
- `qr-preview-and-actions`: QR code rendering, inline title editing, padded PNG image export to device storage/photos, clipboard copying, system share sheet, and type-specific deep actions across Android and iOS.

### Modified Capabilities

## Impact

- **Build & Project Structure**: Replaces `:shared` with `:build-logic`, `:composeApp`, `:core:{domain,database,data,designsystem,presentation}`, and `:feature:{scan,generate,history,preview}:{domain,data,presentation}`. Updates `settings.gradle.kts`, `gradle/libs.versions.toml`, and `iosApp/iosApp.xcodeproj` framework linkage (`ComposeApp`).
- **Platform Configurations**: Updates `androidApp/src/main/AndroidManifest.xml` (camera & legacy storage permissions, splash screen theme) and `iosApp/iosApp/Info.plist` (`NSCameraUsageDescription`, `NSPhotoLibraryUsageDescription`, `NSPhotoLibraryAddUsageDescription`).
- **Dependencies**: Adds Koin (BOM + Compose ViewModel), JetBrains Compose Navigation & Lifecycle (`SavedStateHandle`), Room KMP + Bundled SQLite, `kotlinx-datetime`, `kotlinx-serialization-json`, QRose (multiplatform QR rendering), CameraX + ML Kit Barcode Scanning (`androidMain`), and AssertK + Turbine (`commonTest`).
