# 🚗 Open-Sesame

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Expo-green.svg)](https://expo.dev/)

Open-Sesame is a lightweight Expo / React Native app for authorized Autoparkki carport access. It is designed to avoid repeatedly scanning the same Autoparkki door QR code by saving the access URL and license plate locally on the user's device.

Current active version: **2.7 real-opener prototype**.

---

## For users

### What the app does

Open-Sesame helps authorized users open an Autoparkki door faster:

1. Scan the Autoparkki door QR code once.
2. Save the door profile locally.
3. Enter and save your license plate.
4. Tap **OPEN** when you need to open the door.

The app is intended for users who already have legitimate access to the Autoparkki door URL shown on-site.

### Main features

- One-screen step-based UI:
  - Step 1. Scan the door code
  - Step 2. Enter your license plate
  - Step 3. Open the door
- Save multiple door profiles.
- Save multiple license plates.
- Select, edit, or delete saved doors and plates.
- Automatic door-name suggestion from the Autoparkki access page when possible.
- Manual release-check button.
- Local-only profile storage using AsyncStorage.

### Privacy

Saved door URLs and license plates are stored locally on your device. The app does not send saved profile data to the developer.

### Safety and authorization

Open-Sesame must only be used with garage access URLs and license plates that you are authorized to use. It does not generate access tokens, bypass payment, bypass authentication, or grant access to doors you cannot already open through the official Autoparkki QR/web flow.

### Current limitation

The real opener flow is currently a prototype. It has passed basic request-flow checks, but it should still be validated carefully with authorized access at the physical door. Autoparkki page-structure changes may require parser updates.

---

## For developers

### Tech stack

- Expo / React Native
- Expo Router
- TypeScript
- AsyncStorage
- EAS Build

### Install locally

```bash
npm install
npx expo start
```

For web preview:

```bash
npx expo start --web -c
```

For Android APK build through EAS:

```bash
npx eas-cli build -p android --profile production
```

### Project structure

```text
app/
  _layout.tsx
  index.tsx
src/
  components/
  constants/
  hooks/
  services/
  styles/
  types/
docs/
  CHANGELOG.md
  HANDOFF.md
legacy-app-inventor/
```

### Important developer files

- `app/index.tsx` — screen coordinator and modal wiring.
- `src/hooks/useOpenSesameProfiles.ts` — profile state, selection, add/edit/delete, and storage-triggering effects.
- `src/services/storage.ts` — AsyncStorage access.
- `src/services/qr.ts` — QR URL extraction and Autoparkki URL validation.
- `src/services/doorMetadata.ts` — access-page metadata fetch for default door naming.
- `src/services/debugFetch.ts` — GET-only debug fetch for inspecting selected access pages.
- `src/services/opener.ts` — real opener prototype.

### Real opener prototype

The current opener attempts the authorized Autoparkki web flow:

1. GET the saved access URL.
2. Parse the returned HTML form.
3. Preserve hidden fields and CSRF-like tokens when present.
4. Fill the selected license plate.
5. Submit the form.
6. Report HTTP status and conservative success/failure state.

The implementation intentionally fails instead of guessing if it cannot find a parseable form or plate field.

### Documentation

Developer-oriented notes are kept under:

- `docs/CHANGELOG.md`
- `docs/HANDOFF.md`

The original MIT App Inventor prototype is preserved under:

- `legacy-app-inventor/`

### License

This project is released under the MIT License. See [`LICENSE`](LICENSE).
