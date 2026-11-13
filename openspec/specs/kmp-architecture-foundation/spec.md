# kmp-architecture-foundation Specification

## Purpose

Defines the multiplatform application foundation, adaptive navigation shell, theme consistency, and typed error-handling contracts shared by Android and iOS.

## Requirements

### Requirement: Multiplatform application shell and top-level navigation
The application SHALL provide a shared user interface on Android and iOS with three top-level destinations (`History`, `Scan`, and `Generate`), defaulting to `Scan` on launch and preserving state when switching top-level tabs.

#### Scenario: Launch into Scan destination
- **WHEN** the user launches the application on Android or iOS
- **THEN** the system displays the `Scan` screen as the active top-level destination

#### Scenario: Switch between top-level destinations
- **WHEN** the user selects `History` or `Generate` from the navigation bar or rail
- **THEN** the system navigates to the selected destination without duplicating back-stack entries

### Requirement: Adaptive navigation layout across compact and wide screens
The application SHALL render a bottom navigation bar on compact window widths (< 600dp) and a vertical navigation rail on wide window widths (>= 600dp) on both Android and iOS (including iPad and landscape tablets).

#### Scenario: Compact window displays bottom navigation bar
- **WHEN** the application window width is less than 600dp
- **THEN** the system displays the pill-shaped bottom navigation bar with a prominent center Scan action

#### Scenario: Wide window displays navigation rail
- **WHEN** the application window width is 600dp or greater
- **THEN** the system displays a vertical navigation rail on the leading edge and expands the content pane

### Requirement: Back navigation on non-root screens
Every non-root screen (`Create` and `Preview`) SHALL provide a visible top-bar back button in addition to supporting platform back gestures on Android and iOS.

#### Scenario: Navigate back from non-root screen
- **WHEN** the user taps the top-bar back button on `Create` or `Preview`
- **THEN** the system pops the current screen and returns to the previous destination

### Requirement: Light and dark theme support with custom typography and icons
The application SHALL apply the QRNest color palette, semantic QR-type colors, custom SUSE typography, and vector icons automatically based on the system light or dark appearance on Android and iOS.

#### Scenario: System dark mode active
- **WHEN** the device system appearance is set to dark mode
- **THEN** the application renders dark surface, text, and QR-type semantic colors across all screens
