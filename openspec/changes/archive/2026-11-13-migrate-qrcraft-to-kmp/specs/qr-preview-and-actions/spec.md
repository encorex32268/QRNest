# Spec Delta

## Purpose

Renders the QR code visual preview, supports inline title editing, exports padded QR PNG images to device storage/photos, copies payloads to the clipboard, and triggers type-specific native platform actions.

## ADDED Requirements

### Requirement: QR code visual rendering and inline title editing
The system SHALL render a high-contrast QR code image for the loaded history item and allow the user to edit its custom title, persisting any title modification when navigating back.

#### Scenario: Edit title and navigate back
- **WHEN** the user modifies the title text on the `Preview` screen and taps the back button
- **THEN** the system updates the history record's `title` in persistent storage before navigating up

#### Scenario: Missing history record error handling
- **WHEN** the `Preview` screen is opened with an ID that does not exist or when an action is triggered on a missing item
- **THEN** the system notifies the user that the QR code was not found and navigates back

### Requirement: Copy QR content or Wi-Fi password to system clipboard
The system SHALL copy the QR code's raw content to the native system clipboard on Android and iOS when the user taps Copy, except for `Wi-Fi` items where the extracted password value SHALL be copied.

#### Scenario: Copy standard QR content
- **WHEN** the user taps the Copy button on a non-Wi-Fi QR code preview
- **THEN** the system copies the full QR content string to the device clipboard

#### Scenario: Copy Wi-Fi QR password
- **WHEN** the user taps the Copy button on a `Wi-Fi` QR code preview
- **THEN** the system extracts the password field from the Wi-Fi payload and copies it to the device clipboard

### Requirement: Export padded QR code PNG image to device storage or photo library
The system SHALL render the QR code as a 512x512 image surrounded by a 60px white border (632x632 PNG) and save it to the Downloads collection on Android or Photo Library/Documents on iOS, displaying a confirmation snackbar on success.

#### Scenario: Save QR code image succeeds
- **WHEN** the user taps the "Save" button on the `Preview` screen
- **THEN** the system saves the padded PNG image to the platform storage destination and displays a success snackbar

### Requirement: Type-specific native platform actions from preview content
The system SHALL provide interactive content actions on both Android and iOS: opening URLs in the browser (`Link`), dialing phone numbers (`Phone Number`), opening coordinates in maps (`Geolocation`), copying password and opening settings (`Wi-Fi`), and creating a contact (`Contact`).

#### Scenario: Tap Link content in preview
- **WHEN** the user taps the content button of a `Link` QR code
- **THEN** the system launches the platform browser with the target URL

#### Scenario: Tap Geolocation content in preview
- **WHEN** the user taps the content button of a `Geolocation` QR code
- **THEN** the system launches the native map application (or browser fallback) at the parsed latitude and longitude
