<div align="center">
  <img src="iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/app-icon-1024.png" width="120" alt="QRNest App Icon" />
  <h1>QRNest</h1>
  <p>
    <strong>Scan, Create &amp; Organize QR Codes — Built with Kotlin Multiplatform &amp; Compose Multiplatform</strong>
  </p>
  <p>
    <img src="https://img.shields.io/badge/Kotlin-2.4.21-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin" />
    <img src="https://img.shields.io/badge/Compose%20Multiplatform-1.12.2-4285F4?logo=jetpackcompose&logoColor=white" alt="Compose Multiplatform" />
    <img src="https://img.shields.io/badge/Platform-Android%20%7C%20iOS-3DDC84" alt="Platforms" />
    <img src="https://img.shields.io/badge/Architecture-Clean%20%2B%20MVI-B45309" alt="Architecture" />
  </p>
</div>

---

## About

**QRNest** is a cross-platform QR code scanner, generator, and history manager migrated and redesigned from the Android project **QRCraft** into a full **Kotlin Multiplatform (KMP)** application targeting **Android** and **iOS**.

It shares business logic, local persistence, design tokens, and UI across platforms using **Compose Multiplatform**, while integrating native camera, image picking, clipboard, and photo saving capabilities on each platform—with adaptive layouts supporting phones, landscape mode, and foldable form factors.

---

## Features

- **QR Scanner** — Real-time camera scanning with adaptive viewfinder, flashlight toggle, and gallery image QR decoding.
- **QR Generator** — Create QR codes for **Text**, **Link**, **Contact**, **Phone Number**, **Geolocation**, and **Wi-Fi**.
- **Specimen Preview & Actions** — Edit titles, toggle favorites, copy content, share, and save generated QR codes as images.
- **History Management** — Tabbed history (**Scanned** / **Generated**) with favorite pinning and adaptive multi-column grids on wide screens.
- **Adaptive Navigation** — Floating pill bottom bar on compact screens and navigation rail on expanded/landscape screens.

---

## Screenshots

### Android (Portrait)

| Scan | Create | Preview | History |
| :---: | :---: | :---: | :---: |
| <img src="screenshots/android/portrait_scan.png" width="200" alt="Android Portrait Scan" /> | <img src="screenshots/android/portrait_create.png" width="200" alt="Android Portrait Create" /> | <img src="screenshots/android/portrait_preview.png" width="200" alt="Android Portrait Preview" /> | <img src="screenshots/android/portrait_history.png" width="200" alt="Android Portrait History" /> |

### Android (Landscape)

| Scan | Create |
| :---: | :---: |
| <img src="screenshots/android/landscape_scan.png" width="380" alt="Android Landscape Scan" /> | <img src="screenshots/android/landscape_create.png" width="380" alt="Android Landscape Create" /> |

| Preview | History |
| :---: | :---: |
| <img src="screenshots/android/landscape_preview.png" width="380" alt="Android Landscape Preview" /> | <img src="screenshots/android/landscape_history.png" width="380" alt="Android Landscape History" /> |

### iOS

| Scan | Create | Preview | History |
| :---: | :---: | :---: | :---: |
| <img src="screenshots/ios/scan-portrait.png" width="200" alt="iOS Scan" /> | <img src="screenshots/ios/create-portrait.png" width="200" alt="iOS Create" /> | <img src="screenshots/ios/preview-portrait.png" width="200" alt="iOS Preview" /> | <img src="screenshots/ios/history-portrait.png" width="200" alt="iOS History" /> |

### iOS Duo (Folded & Unfolded)

#### Folded
| Scan | Create | Preview | History |
| :---: | :---: | :---: | :---: |
| <img src="screenshots/ios/duo/close_scan.png" width="200" alt="iOS Duo Closed Scan" /> | <img src="screenshots/ios/duo/close_create.png" width="200" alt="iOS Duo Closed Create" /> | <img src="screenshots/ios/duo/close_preview.png" width="200" alt="iOS Duo Closed Preview" /> | <img src="screenshots/ios/duo/close_history.png" width="200" alt="iOS Duo Closed History" /> |

#### Unfolded
| Scan | Create |
| :---: | :---: |
| <img src="screenshots/ios/duo/open_scan.png" width="360" alt="iOS Duo Open Scan" /> | <img src="screenshots/ios/duo/open_create.png" width="360" alt="iOS Duo Open Create" /> |

| Preview | History |
| :---: | :---: |
| <img src="screenshots/ios/duo/open_preview.png" width="360" alt="iOS Duo Open Preview" /> | <img src="screenshots/ios/duo/open_history.png" width="360" alt="iOS Duo Open History" /> |

---

## Tech Stack

| Category | Technology |
| :--- | :--- |
| **Language & UI** | Kotlin `2.4.21`, Compose Multiplatform `1.12.2`, Material 3 |
| **Architecture** | Clean Architecture + MVI (`State`, `Action`, `Event`), Multi-Module (`:core:*`, `:feature:*`) |
| **Build System** | Gradle Convention Plugins (`:build-logic`), Version Catalog (`libs.versions.toml`) |
| **DI** | Koin `4.2.2` (`koin-core`, `koin-compose-viewmodel`, `koin-android`) |
| **Navigation** | Type-Safe Navigation Compose `2.9.2` |
| **Persistence** | Room KMP `2.8.5` + Bundled SQLite `2.6.2` |
| **QR Generation** | QRose `1.1.2` |
| **QR Scanning (Android)** | CameraX `1.4.2` + Google ML Kit Barcode Scanning `17.3.0` |
| **QR Scanning (iOS)** | Native `AVFoundation` & `Vision` (`VisionQrImageScanner`) |
| **Async & Utilities** | `kotlinx-coroutines`, `kotlinx-serialization`, `kotlinx-datetime` |
| **Testing** | `kotlin.test`, AssertK `0.28.1`, Turbine `1.2.1`, `kotlinx-coroutines-test` |

---

## Module Structure

```text
QRNest
├── androidApp                 # Android application entry point, launcher/splash resources
├── iosApp                     # iOS Xcode project & SwiftUI entry point
├── composeApp                 # Shared KMP app entry, root navigation & Koin setup
├── build-logic                # Custom Gradle convention plugins
├── core
│   ├── domain                 # Shared domain models, Result wrapper & QR payload parser
│   ├── database               # Room KMP database, DAO & platform DatabaseFactory
│   ├── data                   # Repository implementations & mappers
│   ├── designsystem           # Theme, warm amber/espresso color tokens, typography & components
│   └── presentation           # Shared MVI helpers, UiText & adaptive navigation chrome
└── feature
    ├── scan                   # domain / data / presentation
    ├── generate               # domain / data / presentation
    ├── history                # domain / data / presentation
    └── preview                # domain / data / presentation
```

---

## Getting Started

### Android
```bash
./gradlew :androidApp:assembleDebug
```
Or run the `androidApp` configuration directly from Android Studio.

### iOS
Open [iosApp/iosApp.xcodeproj](iosApp/iosApp.xcodeproj) in Xcode and run on an iOS simulator or device.
