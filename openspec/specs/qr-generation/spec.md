# qr-generation Specification

## Purpose

Enables users to select a QR code category, enter structured content with real-time validation, format the payload, and persist the generated QR code.

## Requirements

### Requirement: QR code type selection grid
The system SHALL display all supported QR code categories (`Text`, `Link`, `Contact`, `Phone Number`, `Geolocation`, `Wi-Fi`) in an adaptive grid (2 columns on compact screens, 3 columns on wide screens) and navigate to the creation form when a category is selected.

#### Scenario: Select a QR code type
- **WHEN** the user taps a category card on the `Generate` screen
- **THEN** the system navigates to the `Create` screen configured for the selected `QRCodeType`

### Requirement: Type-specific form validation
The system SHALL enable the "Generate QR-Code" button only when all required fields for the selected `QRCodeType` are non-empty (1 field for `Text`/`Link`/`Phone Number`, 2 fields for `Geolocation`, 3 fields for `Contact`/`Wi-Fi`).

#### Scenario: Single-field type validation
- **WHEN** the user enters non-empty text on the `Create` screen for `Text`, `Link`, or `Phone Number`
- **THEN** the system enables the "Generate QR-Code" button

#### Scenario: Multi-field type validation
- **WHEN** any required field for `Geolocation` (latitude, longitude), `Contact` (name, email, phone), or `Wi-Fi` (SSID, password, encryption type) is empty
- **THEN** the system keeps the "Generate QR-Code" button disabled until all required fields are populated

### Requirement: QR payload generation and persistence
The system SHALL format the form inputs into the canonical payload string for the chosen `QRCodeType`, persist a new history record with `isGenerated = true`, and navigate to the `Preview` screen.

#### Scenario: Generate QR code and navigate to preview
- **WHEN** the user taps the enabled "Generate QR-Code" button
- **THEN** the system formats the QR content, inserts the generated record into history, and navigates to `Preview` with the new record ID

### Requirement: Form state restoration across process death
The system SHALL preserve entered text field values and the selected `QRCodeType` in `SavedStateHandle` so in-progress form input is restored after Android process death or navigation back-stack recreation.

#### Scenario: Restore form inputs after recreation
- **WHEN** the `Create` screen ViewModel is recreated with existing `SavedStateHandle` state
- **THEN** the previously entered field values and button enabled state are restored
