# Release Process

This document describes how to prepare a signed Open-Sesame AndroidNative Lite release.

## Versioning

Before committing app changes, update:

- `app/build.gradle.kts`
  - `versionCode`
  - `versionName`
- `app/src/main/java/com/xl6/opensesame/ReleaseInfo.kt`
  - `VERSION_NAME`
- `README.md`
  - Current version

Docs-only commits may skip the version bump when both are true:

- The change cannot affect the APK or release artifact.
- The user explicitly confirms the exception.

Record the exception in the commit message when useful, for example `Docs-only; no version bump`.

## Pre-Release Checks

Run from the repository root:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat lintDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleRelease
```

Expected unsigned release output:

```text
app/build/outputs/apk/release/app-release-unsigned.apk
```

## Signing

Release APKs must be signed with the long-term release keystore. Keep it outside the repository.

Do not commit:

- `.jks`
- `.keystore`
- `.p12`
- signing passwords
- `keystore.properties`
- signed APKs

Use the Android SDK build tools:

```powershell
$buildTools = "$env:LOCALAPPDATA\Android\Sdk\build-tools\37.0.0"
$unsigned = "app\build\outputs\apk\release\app-release-unsigned.apk"
$aligned = "app\build\outputs\apk\release\open-sesame-VERSION-aligned.apk"
$signed = "app\build\outputs\apk\release\open-sesame-VERSION-release.apk"

& "$buildTools\zipalign.exe" -p -f 4 $unsigned $aligned
& "$buildTools\apksigner.bat" sign `
  --ks "PATH_TO_PRIVATE_KEYSTORE.jks" `
  --ks-key-alias "open-sesame" `
  --ks-pass "pass:STORE_PASSWORD" `
  --key-pass "pass:KEY_PASSWORD" `
  --out $signed `
  $aligned

& "$buildTools\apksigner.bat" verify --verbose --print-certs $signed
```

Upload only the signed `open-sesame-VERSION-release.apk`.

## Install Testing

Install on a test phone:

```powershell
adb install -r app\build\outputs\apk\release\open-sesame-VERSION-release.apk
```

If Android reports `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, the installed app was signed with a different key. Uninstall the old package before installing the release build:

```powershell
adb uninstall com.xl6.opensesame
adb install app\build\outputs\apk\release\open-sesame-VERSION-release.apk
```

## Screenshot Rules

- Use PNG for README screenshots.
- Remove the phone status bar before committing screenshots.
- Do not show real door names, real license plates, QR URLs, or debug output.
- Keep screenshots in `docs/images/`.

## GitHub Release

1. Push `main`.
2. Create and push tag `vVERSION`.
3. Create a GitHub Release from that tag.
4. Upload the signed APK.
5. Mark it as the latest release unless intentionally publishing an older hotfix.

Keep detailed release notes on GitHub Releases. Keep README changelog short.
