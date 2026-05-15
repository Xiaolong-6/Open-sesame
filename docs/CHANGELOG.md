# Changelog

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
