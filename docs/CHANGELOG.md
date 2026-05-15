# Changelog

## 2.1.0

Architecture-cleanup release.

### Added
- `src/` architecture with components, hooks, services, types, constants, and theme files.
- Profile editing for saved doors and saved license plates.
- Debug panel showing app version, mode, saved door count, saved plate count, and active profile names.
- QR invalid-content alert now displays raw scanned content to aid debugging.
- `services/opener.ts` mock opener interface prepared for future real Autoparkki request implementation.

### Changed
- `app/index.tsx` now acts as a screen coordinator instead of holding all business logic.
- Storage logic moved to `src/services/storage.ts`.
- QR parsing moved to `src/services/qr.ts`.
- Profile state management moved to `src/hooks/useOpenSesameProfiles.ts`.

### Still mock-only
- The `OPEN` action does not send a real Autoparkki request yet.
