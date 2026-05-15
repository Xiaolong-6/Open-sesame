# 🚗 Open-Sesame 2.1

Open-Sesame is an Expo / React Native app for authorized Autoparkki carport access.

## Current status

Version 2.1 is an architecture-cleanup release. It keeps the v2.0 user-facing behavior but splits code into services, hooks, components, types, constants, and styles.

Current features:

- Scan an Autoparkki QR code once and save the access URL locally.
- Save multiple garage / door profiles.
- Save multiple license plate profiles.
- Edit saved doors and saved plates.
- Delete saved doors and saved plates.
- Remember the active door and active license plate.
- Store profiles locally with AsyncStorage.
- Debug panel showing version, mode, and profile counts.
- Mock `OPEN` flow for UI/profile testing.

Not yet enabled:

- Real Autoparkki open-door HTTP request.
- CSRF/cookie/session submission flow.

## Install locally

```bash
npm install
npx expo start
```

For Android APK build through EAS:

```bash
npx eas-cli build -p android --profile production
```

## Project structure

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

## Privacy

Garage URLs and license plate profiles are stored locally on the user's device. The app does not send profile data to the developer.

## Safety note

Open-Sesame must only be used with garage access URLs and license plates that the user is authorized to use. It does not bypass access control; the intended purpose is to automate the same authorized QR/web access workflow.
