import type { GarageProfile, PlateProfile } from "../types/profiles";

export async function mockOpenDoor(garage: GarageProfile, plate: PlateProfile): Promise<string> {
  await new Promise((resolve) => setTimeout(resolve, 900));
  return `Mock success: ${garage.name} · ${plate.plateNumber}`;
}

// Future real opener implementation target:
// 1. GET garage.accessUrl.
// 2. Preserve cookies/session.
// 3. Extract CSRF token and required form fields.
// 4. POST plate/open request.
// 5. Parse success/failure response.
