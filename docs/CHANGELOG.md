# Changelog

## 2.7.0

Real opener prototype release.

### Added
- `realOpenDoor()` in `src/services/opener.ts`.
- Authorized GET → parse form → submit license plate POST/GET flow.
- Generic HTML form parser for hidden fields, CSRF-like tokens, plate input detection, and submit controls.
- OPEN button now calls the real opener prototype instead of the mock opener.

### Safety / limitations
- The app only uses the saved access URL and selected license plate.
- It does not bypass authentication or generate access tokens.
- If the page has no parseable form, the request fails instead of guessing a dangerous POST.
- Success detection is conservative; HTTP 200 after submit means "request sent", not guaranteed physical opening.

## 2.6.0

One-screen compact UI release.

### Changed
- Removed helper text under Step 1 and Step 2.
- Removed secondary text under the current door and plate buttons.
- Reduced card padding, margins, OPEN button height, and status card height.
- Scan-time door-name detection now updates the Add Door modal default name directly after QR scan.

### Goal
- Keep the OPEN button and status visible on a typical phone screen without scrolling.

## 2.5.0

Mobile UI and metadata-fix release.

### Added
- Better door-name extraction from Autoparkki access pages.
- Location text such as `P-Luoteisrinne Finnoonsilta` is preferred over generic titles such as `EuroPark Finland - ADC`.

### Changed
- Step 1 and Step 2 now use a two-row mobile layout:
  - Row 1: current door/plate selector.
  - Row 2: action buttons.
- License plates no longer require a separate nickname/label; the plate number is the displayed profile name.
- Picker modal and debug panel show only the plate number for plate profiles.

## 2.4.0

Debug fetch release.

### Added
- `src/services/debugFetch.ts` for safe GET-only inspection of the selected door access URL.
- `src/components/DebugFetchModal.tsx` to display debug fetch output.
- Debug panel button: `Debug fetch active door`.

### Debug fetch output
- Requested URL
- Final URL after redirects
- HTTP status and status text
- Content-Type
- Detected title
- Text snippet from the returned page
- Error message, if fetch fails

### Safety note
- Debug fetch only performs a GET request.
- It does not submit the license plate.
- It does not send an open-door request.

## 2.3.0

Automatic door-name suggestion release.

### Added
- `src/services/doorMetadata.ts` to infer a useful default door name from a scanned or pasted Autoparkki access URL.
- Add-door modal now attempts to fetch the access page and use `og:title`, `<title>`, or `<h1>` as the default door name.
- If page-title detection fails, the app falls back to a stable `Autoparkki <token>` name.
- The automatic name is only applied while the user has not manually edited the door name.

### Notes
- Web builds may fail to fetch the title because of browser CORS rules; native Android/iOS builds should usually work.
- This feature does not send an open-door request. It only reads the access page for naming convenience.

## 2.2.0

Compact-UI release.

### Added
- Step-based home screen labels:
  - Step 1. Scan the door code
  - Step 2. Enter your license plate
  - Step 3. Open the door
- Compact door selector button with edit/delete controls.
- Compact plate selector button with edit/delete controls.
- `ProfilePickerModal` for choosing saved doors or plates.
- Manual release-check button that opens the GitHub releases page.
- Bottom explanatory text: version plus app purpose.

### Changed
- Saved door and plate lists are no longer permanently expanded on the home screen.
- Door and plate selection now happens through picker modals.
- Debug panel now includes release-check action.

## 2.1.0

Architecture-cleanup release.

### Added
- `src/` architecture with components, hooks, services, types, constants, and theme files.
- Profile editing for saved doors and saved license plates.
- Debug panel showing app version, mode, saved door count, saved plate count, and active profile names.
- QR invalid-content alert displays raw scanned content to aid debugging.
- `services/opener.ts` mock opener interface prepared for future real Autoparkki request implementation.

### Still mock-only
- The `OPEN` action does not send a real Autoparkki request yet.
