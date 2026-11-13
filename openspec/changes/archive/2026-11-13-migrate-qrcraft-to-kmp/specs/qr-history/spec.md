# Spec Delta

## Purpose

Manages persistent storage and browsing of scanned and generated QR codes, including favorite ordering, sharing, and deletion on Android and iOS.

## ADDED Requirements

### Requirement: Separate Scanned and Generated history tabs ordered by favorite and recency
The system SHALL display `Scanned` (`isGenerated = false`) and `Generated` (`isGenerated = true`) history items in two swipeable tabs, ordering items within each tab by `isFavorite DESC, createdAt DESC`.

#### Scenario: View history tabs with favorites prioritized
- **WHEN** the user opens the `History` screen
- **THEN** the system lists favorited items first followed by the most recently created items in both the `Scanned` and `Generated` tabs

#### Scenario: Empty history state
- **WHEN** a history tab contains no saved records
- **THEN** the system displays the centered "History is empty" message

### Requirement: Favorite status toggling from history list
The system SHALL allow the user to toggle an item's favorite state directly from a history card and immediately re-sort the list.

#### Scenario: Toggle favorite on history item
- **WHEN** the user taps the star button on a history item
- **THEN** the system flips `isFavorite` in persistent storage and scrolls the updated list to the top

### Requirement: History item long-press actions for Share and Delete
The system SHALL display a modal bottom sheet when the user long-presses a history item, allowing the user to share the item's content via the platform share sheet or delete the item from storage.

#### Scenario: Long-press and delete history item
- **WHEN** the user long-presses a history item and taps "Delete" in the bottom sheet
- **THEN** the system deletes the item from the database and dismisses the bottom sheet

#### Scenario: Long-press and share history item
- **WHEN** the user long-presses a history item and taps "Share" in the bottom sheet
- **THEN** the system opens the native platform share sheet with the item's title and content
