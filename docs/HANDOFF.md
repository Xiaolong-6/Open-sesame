# Open-Sesame Developer Handoff

## Current version

2.3.0

## Current app mode

Mock mode. The `OPEN` button calls `mockOpenDoor()` in `src/services/opener.ts`.

## Important files

- `app/index.tsx`: page coordinator and modal wiring.
- `src/components/DoorSection.tsx`: compact current-door selector.
- `src/components/PlateSection.tsx`: compact license-plate selector.
- `src/components/ProfilePickerModal.tsx`: saved profile selector modal.
- `src/hooks/useOpenSesameProfiles.ts`: profile state, selection, add/edit/delete, storage-triggering effects.
- `src/services/storage.ts`: AsyncStorage access.
- `src/services/qr.ts`: QR URL extraction and Autoparkki URL validation.
- `src/services/opener.ts`: mock opener; target file for future real opener implementation.
- `src/services/doorMetadata.ts`: optional access-page title fetch for default door naming.
- `src/constants/appInfo.ts`: version, mode, subtitle, release URL, and purpose text.

## Next recommended task

Implement a debug-only real request prototype in `src/services/opener.ts`:

1. GET `garage.accessUrl`.
2. Capture cookies/session.
3. Extract CSRF token and required form fields.
4. POST license plate/open request.
5. Parse success/failure response.
6. Keep mock mode as fallback.

Do not hardcode real garage URLs, license plates, cookies, or tokens into the repository.
