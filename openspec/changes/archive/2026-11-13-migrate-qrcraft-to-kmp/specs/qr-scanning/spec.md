# Spec Delta

## Purpose

Provides live camera QR/barcode scanning, flashlight control, camera permission management, and gallery image QR code detection on Android and iOS.

## ADDED Requirements

### Requirement: Camera permission verification and request flow
The system SHALL verify camera authorization on entering the `Scan` screen and display a permission dialog when camera access is not granted, allowing the user to grant access or open system settings if permanently denied.

#### Scenario: Camera permission granted on first request
- **WHEN** the user taps "Grant Access" in the camera permission dialog and approves the system prompt
- **THEN** the system dismisses the dialog, displays a confirmation snackbar, and starts the live camera preview

#### Scenario: Camera permission denied previously
- **WHEN** camera permission was previously denied without rationale (Android) or is in `Denied` status (iOS) and the user taps "Grant Access"
- **THEN** the system opens the application's system settings page on the host platform

### Requirement: Live camera QR code detection and persistence
The system SHALL analyze live camera frames within the central scan frame on Android and iOS, parse the detected barcode payload into a supported `QRCodeType`, persist the scanned record with `isGenerated = false`, and navigate to `Preview`.

#### Scenario: Valid QR code detected in camera frame
- **WHEN** a QR code enters the target scan region during live camera preview
- **THEN** the system shows a loading indicator, parses and saves the scanned QR code to history, and navigates to the `Preview` screen for the saved entry

### Requirement: Camera flashlight toggle
The system SHALL allow the user to toggle the device camera torch on and off from the `Scan` screen while live scanning is active.

#### Scenario: Toggle flashlight on and off
- **WHEN** the user taps the flashlight button during live camera scanning
- **THEN** the system toggles the hardware torch state and updates the flashlight button visual state

### Requirement: Scan QR code from gallery image
The system SHALL allow the user to pick an image from the device photo library on Android and iOS, decode any QR code in the image, save the result to history, and navigate to `Preview`, or show an error dialog if no QR code is found.

#### Scenario: Selected gallery image contains a valid QR code
- **WHEN** the user selects an image containing a readable QR code from the system photo picker
- **THEN** the system decodes the QR code, persists the scanned entry to history, and navigates to `Preview`

#### Scenario: Selected gallery image contains no QR code
- **WHEN** the user selects an image with no detectable QR code from the system photo picker
- **THEN** the system displays the "No QR-codes found" dialog until dismissed
