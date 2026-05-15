# 🚗 Open-Sesame 2.5

Open-Sesame is an Expo / React Native app for authorized Autoparkki carport access.

## Current status

Version 2.5 fixes the compact mobile layout, removes separate plate labels, and improves door-name detection from Autoparkki access pages.

Current features:

- Step-based UI:
  - Step 1. Scan the door code
  - Step 2. Enter your license plate
  - Step 3. Open the door
- Two-row compact mobile layout: current profile button on one row, actions on a second row.
- License plates no longer require a separate nickname/label.
- Improved automatic door-name suggestion: prefers location text such as parking-area/street name over generic page title.
- Debug fetch mode for the active door: GET the access page, show HTTP status, final URL, detected title, and page text snippet.
- Automatic default door-name suggestion from the access page title, with URL-token fallback.
- Compact current-door selector with edit/delete controls.
- Compact license-plate selector with edit/delete controls.
- Saved-profile picker modal instead of always-visible long lists.
- Manual releases button for checking updates.
- Scan an Autoparkki QR code once and save the access URL locally.
- Save, edit, delete, and select multiple garage / door profiles.
- Save, edit, delete, and select multiple license plate profiles.
- Store profiles locally with AsyncStorage.
- Debug panel showing version, mode, and profile counts.
- Mock `OPEN` flow for UI/profile testing.

Not yet enabled:

- Real Autoparkki open-door HTTP request.
- CSRF/cookie/session submission flow.
- Store / Play Store release polish.

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
