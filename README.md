# 🚗 Open-Sesame 2.0

Open-Sesame is a lightweight Expo / React Native app for authorized Autoparkki carport access.

## Version 2.0 status

This version is an Expo-based rewrite of the original MIT App Inventor prototype.

Current features:

- Scan an Autoparkki QR code once and save the access URL locally.
- Save multiple garage / door profiles.
- Save multiple license plate profiles.
- Remember the active door and active license plate.
- Delete saved doors and plates.
- Local-only storage using AsyncStorage.
- Mock `OPEN` flow for UI/profile testing.

Not yet enabled:

- Real Autoparkki open-door HTTP request.
- CSRF/cookie/session submission flow.
- App Store / Play Store release configuration.

## Install locally

```bash
npm install
npx expo start
```

For Android APK build through EAS:

```bash
npx eas-cli build -p android --profile production
```

## Notes

Open-Sesame must only be used with garage access URLs and license plates that the user is authorized to use. It does not bypass access control; the intended purpose is to automate the same authorized QR/web access workflow.

## Project structure

```text
app/_layout.tsx
app/index.tsx
app.json
eas.json
package.json
tsconfig.json
.eas/workflows/create-production-builds.yml
```

## Privacy

Garage URLs and license plate profiles are stored locally on the user's device. The app does not send profile data to the developer.
