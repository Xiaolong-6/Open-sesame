# Open-Sesame Developer Handoff

## Current version

2.8.0

## Current app mode

Real-opener prototype mode. The `OPEN` button calls `realOpenDoor()` in `src/services/opener.ts`.

## Important files

- `app/index.tsx`: page coordinator and modal wiring.
- `src/components/DoorSection.tsx`: compact current-door selector.
- `src/components/PlateSection.tsx`: compact license-plate selector.
- `src/components/ProfilePickerModal.tsx`: saved profile selector modal.
- `src/hooks/useOpenSesameProfiles.ts`: profile state, selection, add/edit/delete, storage-triggering effects.
- `src/services/storage.ts`: AsyncStorage access.
- `src/services/qr.ts`: QR URL extraction and Autoparkki URL validation.
- `src/services/opener.ts`: real opener prototype. It performs GET access page, parses form fields, fills the selected plate, and submits the form.
- `src/services/doorMetadata.ts`: access-page metadata fetch for default door naming. It prefers page-location text before falling back to page title or URL token.
- `src/services/debugFetch.ts`: GET-only debug fetch for inspecting selected access pages.
- `src/components/DebugFetchModal.tsx`: displays debug fetch result.
- `src/constants/appInfo.ts`: version, mode, subtitle, release URL, and purpose text.

## Next recommended task

Validate the real opener prototype on Android with authorized access. Collect the OPEN result status, final URL, and response snippet if it fails:

1. GET `garage.accessUrl`.
2. Capture cookies/session.
3. Extract CSRF token and required form fields.
4. POST license plate/open request.
5. Parse success/failure response.
6. Keep mock mode as fallback.

Do not hardcode real garage URLs, license plates, cookies, or tokens into the repository.

## App icon assets

- `assets/icon.png`: main Expo app icon.
- `assets/adaptive-icon.png`: Android adaptive icon foreground.
- `assets/splash-icon.png`: splash image.

