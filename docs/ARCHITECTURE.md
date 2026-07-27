# Architecture

Open-Sesame AndroidNative Lite is a single-module native Kotlin Android app.

## Main Modules

- `MainActivity.kt`: app entry point, state coordination, permission handling, and high-level actions.
- `MainScreenViews.kt`: main screen layout and view construction.
- `MainDialogs.kt`: centered dialogs and secondary interaction surfaces.
- `ProfileStore.kt`: local persistence for saved doors, vehicles, preferences, and developer mode.
- `ProfileModels.kt`: door and vehicle profile models.
- `AutoparkkiOpener.kt`: EuroPark/autoparkki access page parsing and request submission.
- `QrScannerActivity.kt`: CameraX and ML Kit QR scanner.
- `ReleaseInfo.kt`: app version, release URL, share text, and User-Agent metadata.
- `ShareController.kt`: QR code generation and Android share intent handling.
- `UpdateChecker.kt`: GitHub release update check.
- `LocaleController.kt`: language preference and locale application.
- `ThemeController.kt`: system/light/dark theme preference.
- `UiKit.kt`: shared UI constants and small view helpers.
- `Haptics.kt`: haptic feedback helpers.

## Runtime Flow

1. `MainActivity` loads saved door and vehicle profiles from `ProfileStore`.
2. The main screen renders the current profile state.
3. If no door is saved, the Door card starts QR scanning directly.
4. If doors exist, tapping Door opens the door picker.
5. If no vehicle is saved, the Vehicle card prompts for a new license plate.
6. If vehicles exist, tapping Vehicle opens the vehicle picker.
7. `OPEN DOOR` validates that both selected profiles exist, then calls `AutoparkkiOpener.openDoor(...)`.
8. The opener fetches the saved access page, parses the form, fills the selected plate, and submits the request.
9. The UI reports request status based on HTTP and parsed page signals.

## Access URL Rules

Saved door URLs must be HTTPS URLs for `autoparkki.fi` or a subdomain with an `/access/` path. This validation should apply consistently to scanned, manually entered, edited, and opened door profiles.

## Storage

The app stores data locally in app-private Android preferences. It does not sync doors or plates to a cloud account.

Stored values are not end-to-end encrypted. Treat logs, screenshots, and debug output as sensitive.

## Localization

User-visible strings should live in Android string resources:

- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-fi/strings.xml`
- `app/src/main/res/values-zh/strings.xml`

Future languages should add a new `values-xx/strings.xml` file and reuse existing string keys where possible.
