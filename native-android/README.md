# Open-Sesame Native Lite v0.1

A Kotlin native Android proof-of-concept for Open-Sesame.

This version is intended to test whether the Expo/React Native app can be migrated to a smaller native Android implementation.

## Features

- Add/edit/delete Autoparkki door profiles.
- Add/edit/delete license plates.
- QR scan using CameraX + ML Kit Barcode Scanning.
- Local profile storage with SharedPreferences.
- Real opener prototype using native `HttpURLConnection`.
- Generic HTML form parser for Autoparkki access pages.
- Debug fetch / door-name suggestion.
- No Expo / React Native runtime.

## Expected APK size

This should be much smaller than the Expo APK, but exact size depends on CameraX and ML Kit dependencies. A no-camera variant would be smaller.

## Open in Android Studio

1. Open Android Studio.
2. File → Open.
3. Select this folder.
4. Let Gradle sync.
5. Build → Build Bundle(s) / APK(s) → Build APK(s).

## Notes

This is an alpha-quality native migration. It is not yet as polished as the Expo version.

Do not hardcode real garage URLs, license plates, cookies, or tokens into the repository.
