# Open-Sesame AndroidNative Lite

Open-Sesame AndroidNative Lite is a small native Kotlin Android app for faster reuse of authorized **EuroPark (autoparkki)** door QR access pages in Finland.

The active Android project now lives at the repository root. Earlier Expo / React Native files are archived under `archive/expo-react-native/`, and the original MIT App Inventor version is archived under `archive/legacy-app-inventor/` for historical reference.

## Screenshot

The README screenshot is intentionally pending refresh for the next release so the repository does not show the older Step UI.

## Current version

- App version: `0.4.8`
- Android package: `com.xl6.opensesame`
- Minimum Android version: Android 8.0, API 26
- Target SDK: 35
- Default license plate: `ABC-123`

## Repository layout

```text
app/                         Active Android application module
gradle/                      Gradle wrapper files
build.gradle.kts             Root Gradle build file
settings.gradle.kts          Gradle project settings
gradle.properties            AndroidX / Gradle / Kotlin settings
archive/expo-react-native/   Archived Expo / React Native implementation
archive/legacy-app-inventor/ Archived MIT App Inventor implementation
```

## For users

### What it does

- Scan an authorized EuroPark (autoparkki) door QR code once.
- Save the door URL locally on this phone.
- Save one or more license plates locally on this phone.
- Confirm the selected door and vehicle on the main screen.
- Tap `OPEN DOOR` to submit the saved door URL and selected plate faster.
- Use the card menus to choose, add, edit, or delete saved profiles.
- Use the Developer mode checkbox in Help to show or hide advanced diagnostics.

### Important limitations

- Use this app only with EuroPark/autoparkki doors and license plates that you are authorized to use.
- The app does not bypass EuroPark access control.
- The app does not physically detect whether the door opened.
- A successful app response means the web request was sent or accepted by the parsed webpage flow; the user still needs to visually verify the door.
- The app is currently targeted at EuroPark/autoparkki access pages in Finland. It may not work outside that webpage flow.
- Door URLs and license plates are stored locally on the device, not in a cloud account.
- Local storage uses app-private Android preferences, not end-to-end encryption.

### Suomenkielinen lyhyt kuvaus

Open-Sesame on epävirallinen paikallinen apusovellus EuroParkin Suomessa käyttämille autoparkki-ovien QR-verkkosivuille. Sovellus tallentaa käyttäjän itse skannaaman valtuutetun oven URL-osoitteen ja rekisterinumeron puhelimeen, jotta sama avauspyyntö voidaan lähettää myöhemmin nopeammin.

Sovellus ei kierrä kulunvalvontaa, ei takaa oven avautumista eikä tarkista fyysisesti, avautuiko ovi. Käytä sovellusta vain oviin ja rekisterinumeroihin, joihin sinulla on käyttöoikeus.

## For developers

### Runtime flow

1. `MainActivity` loads local door and plate profiles from `ProfileStore`.
2. If there is no saved plate, `MainActivity.reload()` creates the default local plate `ABC-123`.
3. The Door card opens saved-door selection, and its menu can start `QrScannerActivity`, which uses CameraX / ML Kit to scan QR content.
4. `MainActivity.normalizeAutoparkkiAccessUrl()` accepts only HTTPS URLs whose host is `autoparkki.fi` or a subdomain and whose path starts with `/access/`.
5. When a door URL is saved, `AutoparkkiOpener.suggestDoorName()` may GET the page and derive a readable door name from the legacy page text.
6. The Vehicle card opens saved-plate selection, and its menu can add, edit, or delete plate profiles.
7. `OPEN DOOR` calls `MainActivity.openDoor()`, which delegates the request to `AutoparkkiOpener.openDoor(door, plate)` on a worker thread.
8. Advanced diagnostics are hidden from the default main screen and call `AutoparkkiOpener.debugAccessInfo()` only after developer mode is enabled.

### Door-opening request logic

`AutoparkkiOpener.openDoor()` mirrors the legacy autoparkki webpage workflow:

1. Validate that the stored access URL is an HTTPS EuroPark/autoparkki `/access/` URL.
2. Normalize the selected license plate to uppercase.
3. Send `GET` to the stored access URL.
4. Parse the first HTML `<form>` from the returned page.
5. Find the plate input field by likely names such as plate, license/licence, registration, register, regno, rekister, vehicle, or car; otherwise fall back to the first text input.
6. Copy existing non-submit form controls into a request parameter map.
7. Replace the detected plate-field value with the selected plate.
8. Find a submit control that looks like open/avaa/ovi/door/submit, or fall back to the first submit control.
9. Resolve the form action relative to the final GET URL.
10. Submit the form with either GET or POST, depending on the parsed form method.
11. Treat HTTP 2xx plus no obvious error text as a sent/accepted request. If the page text explicitly contains success-like wording, report a stronger success message.

The app **does not** use a hardware sensor, camera check, Bluetooth state, barrier status API, or any independent door-state verification. It cannot know whether the physical door opened. Any success/failure message is based only on HTTP status and parsed webpage text.

## Build

Open this repository root in Android Studio, then run:

```text
Build -> Build Bundle(s) / APK(s) -> Build APK(s)
```

Debug APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

From the command line on Windows:

```powershell
.\gradlew.bat assembleDebug
```

## Changelog

### 0.4.8

- Updated the app palette toward the original autoparkki page style with light gray background, white cards, and blue primary actions.
- Added a dark theme palette.
- Added a Preferences theme switcher with System, Light, and Dark options; System is the default.

### 0.4.7

- Polished Settings into quieter section cards with lighter rows.
- Replaced the text close button with a drawn close icon.
- Shortened Settings row labels for QR code, share link, releases, language, and reset.

### 0.4.6

- Made Settings a full-screen secondary menu.
- Reorganized Settings into Preferences, Share, About, Developer mode, and Developer tools sections.
- Moved Language out of Developer tools into regular Preferences.

### 0.4.5

- Replaced the top Help button with a hamburger-style settings entry.
- Moved sharing, release link, Developer mode, language, debug, update, and reset into a right-side full-height settings panel.
- Removed the Advanced controls from the main screen.

### 0.4.4

- Added a Developer mode language switcher for System, English, Finnish, and Chinese.
- Persisted Developer mode so language switching can recreate the app without hiding Advanced controls.
- Replaced non-ASCII helper glyphs in the Help dialog source with ASCII text.

### 0.4.3

- Reworked the Help dialog into a cleaner action-list layout.
- Replaced the native Developer mode checkbox with a custom toggle row.

### 0.4.2

- Added a share menu with local QR code display for the public release page.
- Added ZXing QR generation for the release page URL.

### 0.4.1

- Added Android system sharing for the public Open-Sesame release page from Help.
- Kept sharing limited to public release metadata, never saved door URLs or license plates.

### 0.4.0

- Added Android string resources for English, Finnish, and Chinese.
- Migrated the main screen, Help, menus, common dialogs, status messages, and scanner title to localized resources.

### 0.3.9

- Centralized app version, User-Agent, release URL, and share text in `ReleaseInfo`.

### 0.3.8

- Migrated the Gradle Kotlin JVM target configuration away from deprecated `kotlinOptions`.
- Removed the stale Step UI screenshot from the README until a current screenshot is added for release.
- Updated Developer mode documentation and repaired the Finnish safety summary encoding.

### 0.3.7

- Replaced static QR scan handoff state with Activity Result API result delivery.
- Added local profile JSON read recovery for corrupted saved door or vehicle data.

### 0.3.6

- Applied the same EuroPark/autoparkki access URL validation to scanned, manually added, edited, and opened door profiles.
- Removed license plate values from opener result messages.
- Scoped request cookies to one opener flow and host.

### 0.3.5

- Fixed Android lint blockers for CameraX image access and camera hardware feature declaration.

### 0.3.4

- Replaced deprecated system bar setup with AndroidX edge-to-edge APIs.
- Replaced `Uri.parse(...)` calls with AndroidX `toUri()`.
- Removed a redundant Regex character escape.

### 0.3.3

- Added a Developer mode checkbox to the Help dialog.
- Made Developer mode explicitly disableable from Help.

### 0.3.2

- Removed the redundant `-native-lite` suffix from the current app version name.
- Kept live request state in the top status bar only.
- Kept the text below the main button for the default hint or last successful open time.
- Replaced the default help alert with a cleaner custom dialog.
- Replaced Door and Vehicle picker alerts with matching bottom sheets.

### 0.3.1-native-lite

- Replaced text-based card action indicators with drawn chevron and vertical-menu icons.
- Kept Door and Vehicle values single-line with end ellipsis to protect the action area.
- Restored the main `OPEN DOOR` button after a short success confirmation.
- Shortened the help popup copy.

### 0.3.0-native-lite

User-facing changes:

- Reworked the main screen into a compact control panel.
- Replaced persistent Step 1 / Step 2 / Step 3 sections with Door and Vehicle cards.
- Changed the primary action from `OPEN` to `OPEN DOOR`.
- Moved choose, scan, edit, and delete actions into card taps and card menus.
- Hid debug/reset/update controls from the default main screen.
- Added one-line request status and clearer `OPEN DOOR` / `OPENING...` / `OPENED` / `TRY AGAIN` button states.
- Added haptic feedback for opening start, success, and failure.
- Added the upper-right `?` popup with the concise EuroPark/autoparkki quick-opener explanation and update link.
- Added default plate creation with `ABC-123` when no plate exists.
- Standardized visible operator wording to `EuroPark (autoparkki)`.
- Changed the QR scanner title to `Scan EuroPark (autoparkki) QR`.
- Moved the active Android Gradle project from `native-android/` to the repository root.
- Archived the earlier Expo / React Native implementation under `archive/expo-react-native/`.
- Archived the original MIT App Inventor implementation under `archive/legacy-app-inventor/`.
- Added a controlled-size v0.3.0 screenshot to the README.

Developer-facing changes:

- Moved always-visible debug metadata into the `DEBUG` popup.
- `DEBUG` now responds even when no door is selected.
- `DEBUG` shows `Mode: real opener`.
- Current-door diagnostics now include GET HTTP status, final URL, suggested door name, page title, form method/action/control count, detected plate field, submit control, and readable page text preview.
- Version values were updated for `0.3.0-native-lite`.
- Root-level build command is now `./gradlew assembleDebug` or `.\gradlew.bat assembleDebug`.

### 0.2.1-native-lite

- Native Kotlin Android implementation.
- Local door profile storage.
- Local license plate storage.
- QR scanning with CameraX and ML Kit.
- Real EuroPark/autoparkki opener prototype.
- Debug fetch and door-name suggestion.

## Safety note

Open-Sesame must only be used with garage access URLs and license plates that the user is authorized to use. It does not bypass access control; it automates the same authorized QR/web access workflow.
