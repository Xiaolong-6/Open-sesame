# Open-Sesame Native Lite v0.2.1

A Kotlin native Android proof-of-concept for Open-Sesame.

## v0.2.1 changes

- Fixed Kotlin compile errors from `TextView.text` shadowing the `text` color field.
- Replaced invalid `lineSpacing = ...` assignment with `setLineSpacing(...)`.

## v0.2 changes

- Fixed status-bar overlap.
- Replaced square Android default buttons with rounded text-button controls.
- Fixed debug text newlines.
- Added AndroidX Gradle properties.
- Set Java/Kotlin JVM target to 17.
- Improved mobile spacing and visual hierarchy.

## Build

Open this folder in Android Studio, then run:

```text
Build → Build Bundle(s) / APK(s) → Build APK(s)
```

APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```
