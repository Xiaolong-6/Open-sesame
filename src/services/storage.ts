import AsyncStorage from "@react-native-async-storage/async-storage";
import { STORAGE_KEYS } from "../constants/storageKeys";
import type { GarageProfile, PlateProfile } from "../types/profiles";

export async function loadProfileStorage() {
  const [savedGarages, savedPlates, savedGarageId, savedPlateId] = await Promise.all([
    AsyncStorage.getItem(STORAGE_KEYS.garages),
    AsyncStorage.getItem(STORAGE_KEYS.plates),
    AsyncStorage.getItem(STORAGE_KEYS.activeGarageId),
    AsyncStorage.getItem(STORAGE_KEYS.activePlateId),
  ]);
  const garageProfiles: GarageProfile[] = savedGarages ? JSON.parse(savedGarages) : [];
  const plateProfiles: PlateProfile[] = savedPlates ? JSON.parse(savedPlates) : [];
  return { garageProfiles, plateProfiles, activeGarageId: savedGarageId || undefined, activePlateId: savedPlateId || undefined };
}

export async function saveGarageProfiles(profiles: GarageProfile[]) {
  await AsyncStorage.setItem(STORAGE_KEYS.garages, JSON.stringify(profiles));
}
export async function savePlateProfiles(profiles: PlateProfile[]) {
  await AsyncStorage.setItem(STORAGE_KEYS.plates, JSON.stringify(profiles));
}
export async function saveActiveGarageId(id?: string) {
  if (id) await AsyncStorage.setItem(STORAGE_KEYS.activeGarageId, id);
  else await AsyncStorage.removeItem(STORAGE_KEYS.activeGarageId);
}
export async function saveActivePlateId(id?: string) {
  if (id) await AsyncStorage.setItem(STORAGE_KEYS.activePlateId, id);
  else await AsyncStorage.removeItem(STORAGE_KEYS.activePlateId);
}
export async function clearAllProfileStorage() {
  await AsyncStorage.multiRemove([STORAGE_KEYS.garages, STORAGE_KEYS.plates, STORAGE_KEYS.activeGarageId, STORAGE_KEYS.activePlateId]);
}
