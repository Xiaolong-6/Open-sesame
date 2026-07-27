# Product Principles

Open-Sesame AndroidNative Lite is a small utility app, not a dashboard. The main interaction should remain fast, legible, and low-friction.

## Core Use Case

The user opens the app, confirms the selected door and license plate, and taps `OPEN DOOR`.

Everything else is secondary:

- scanning or adding profiles
- choosing between saved profiles
- editing and deleting profiles
- sharing the public release page
- language and theme preferences
- diagnostics and reset actions

## Main-Screen Rules

- Keep one dominant primary action: `OPEN DOOR`.
- Do not show setup steps once the app is configured.
- If no door is saved, the Door area should directly offer `Scan new door`.
- If no vehicle is saved, the Vehicle area should directly offer `Add license plate`.
- Door and vehicle cards should be tappable entry points to their picker dialogs.
- Avoid extra chevrons, overflow buttons, and repeated Edit/Delete buttons on the main screen.
- The default status should be calm, such as `Ready to open`.
- Opening, success, and failure states should be visually clear and short-lived where appropriate.

## Profile Management

Door and vehicle management should feel consistent.

- Tap a row to select it.
- Long-press a row to enter selection mode.
- Selection mode may show checkboxes.
- `Edit selected` is enabled only when exactly one item is selected.
- `Delete selected` can act on one or more selected items.
- Use one haptic event when entering selection mode, not repeated vibration while selecting.

## Settings

Settings is the home for lower-frequency functions:

- language
- theme
- QR code sharing
- Android share-sheet link sharing
- release/update page
- developer mode
- developer tools
- reset app data

Settings should be full-screen and visually consistent with the app, not a stack of native alert dialogs.

## Visual Direction

The visual style should stay close to the original autoparkki web page:

- light gray background
- white content cards
- blue primary actions
- restrained typography
- clear spacing

Dark theme should follow the same hierarchy, with system theme as the default.

## Privacy Boundary

Do not expose:

- real license plates
- real door names
- QR access URLs
- debug output containing final URLs, form data, or readable access-page text

Use placeholders such as `ABC-123`, `XYZ-890`, and generic door names in docs and screenshots.
