# Agent Guide

This file is for coding agents working on Open-Sesame.

## Project Shape

- The active Android app is the native Kotlin project at the repository root.
- Archived implementations live under `archive/` and should not be edited unless the task explicitly targets historical material.
- The Android package is `com.xl6.opensesame`.
- Current release metadata is centralized in:
  - `app/build.gradle.kts`
  - `app/src/main/java/com/xl6/opensesame/ReleaseInfo.kt`
  - `README.md`

## Non-Negotiables

- Never commit or upload keystores, signing passwords, QR access URLs, real door names, or real license plates.
- Before making a commit for app work, bump `versionCode` and `versionName` unless the user explicitly says not to.
- Docs-only commits may skip the version bump when the change cannot affect the APK and the user explicitly confirms the exception.
- Keep release signing material outside the repository.
- Do not put real user data in screenshots, docs, tests, logs, or release notes.
- Do not publish unsigned APKs as release assets.

## Development Principles

- Keep the app focused on one core job: confirm the saved door and vehicle, then open the door with one primary action.
- Main-screen UI should stay simple. Configuration, diagnostics, sharing, language, and theme controls belong in secondary screens.
- Prefer native Android/Kotlin code and existing local helpers over new frameworks.
- Use Android string resources for user-visible text.
- Keep destructive operations explicit and recoverable where practical.
- Treat successful HTTP responses as request status only. The app cannot verify the physical door state.

## Verification Before Release

Run these from the repository root:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat lintDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleRelease
```

Then sign the release APK using the private release keystore outside this repository. See `docs/RELEASE.md`.

## Documentation Expectations

- Keep `README.md` short and user-facing.
- Put detailed release steps in `docs/RELEASE.md`.
- Put product and UI direction in `docs/PRODUCT_PRINCIPLES.md`.
- Put code structure and runtime flow in `docs/ARCHITECTURE.md`.
- Put detailed version history in GitHub Releases or `docs/release-notes/`, not in the README.
