import { useEffect, useMemo, useState } from "react";
import { clearAllProfileStorage, loadProfileStorage, saveActiveGarageId, saveActivePlateId, saveGarageProfiles, savePlateProfiles } from "../services/storage";
import type { GarageProfile, OpenStatus, PlateProfile } from "../types/profiles";

function createId(prefix: string) { return `${prefix}_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`; }
function normalizePlate(input: string) { return input.trim().toUpperCase(); }
function normalizeUrl(input: string) { return input.trim(); }

export function useOpenSesameProfiles() {
  const [garageProfiles, setGarageProfiles] = useState<GarageProfile[]>([]);
  const [plateProfiles, setPlateProfiles] = useState<PlateProfile[]>([]);
  const [activeGarageId, setActiveGarageId] = useState<string | undefined>();
  const [activePlateId, setActivePlateId] = useState<string | undefined>();
  const [status, setStatus] = useState<OpenStatus>("ready");
  const [message, setMessage] = useState("Ready");
  const [loaded, setLoaded] = useState(false);

  const activeGarage = useMemo(() => garageProfiles.find((item) => item.id === activeGarageId), [garageProfiles, activeGarageId]);
  const activePlate = useMemo(() => plateProfiles.find((item) => item.id === activePlateId), [plateProfiles, activePlateId]);

  useEffect(() => { (async () => {
    try {
      const saved = await loadProfileStorage();
      setGarageProfiles(saved.garageProfiles); setPlateProfiles(saved.plateProfiles);
      if (saved.activeGarageId && saved.garageProfiles.some((g) => g.id === saved.activeGarageId)) setActiveGarageId(saved.activeGarageId);
      else if (saved.garageProfiles.length > 0) setActiveGarageId(saved.garageProfiles[0].id);
      if (saved.activePlateId && saved.plateProfiles.some((p) => p.id === saved.activePlateId)) setActivePlateId(saved.activePlateId);
      else if (saved.plateProfiles.length > 0) setActivePlateId(saved.plateProfiles[0].id);
    } catch { setStatus("failed"); setMessage("Could not load saved settings."); }
    finally { setLoaded(true); }
  })(); }, []);

  useEffect(() => { if (loaded) saveGarageProfiles(garageProfiles).catch(() => { setStatus("failed"); setMessage("Could not save garage profiles."); }); }, [garageProfiles, loaded]);
  useEffect(() => { if (loaded) savePlateProfiles(plateProfiles).catch(() => { setStatus("failed"); setMessage("Could not save plate profiles."); }); }, [plateProfiles, loaded]);
  useEffect(() => { if (loaded) saveActiveGarageId(activeGarageId).catch(() => { setStatus("failed"); setMessage("Could not save active garage."); }); }, [activeGarageId, loaded]);
  useEffect(() => { if (loaded) saveActivePlateId(activePlateId).catch(() => { setStatus("failed"); setMessage("Could not save active plate."); }); }, [activePlateId, loaded]);

  function addGarageProfile(nameInput: string, urlInput: string) {
    const name = nameInput.trim() || `Garage ${garageProfiles.length + 1}`;
    const accessUrl = normalizeUrl(urlInput);
    if (!accessUrl) throw new Error("Paste the Autoparkki QR/access URL.");
    if (!accessUrl.startsWith("https://")) throw new Error("The access URL should start with https://.");
    const newGarage: GarageProfile = { id: createId("garage"), name, accessUrl, createdAt: new Date().toISOString() };
    setGarageProfiles((previous) => [...previous, newGarage]); setActiveGarageId(newGarage.id); setStatus("ready"); setMessage(`Saved door: ${name}`);
  }
  function updateGarageProfile(id: string, nameInput: string, urlInput: string) {
    const name = nameInput.trim() || "Garage"; const accessUrl = normalizeUrl(urlInput);
    if (!accessUrl) throw new Error("Paste the Autoparkki QR/access URL.");
    if (!accessUrl.startsWith("https://")) throw new Error("The access URL should start with https://.");
    setGarageProfiles((previous) => previous.map((item) => item.id === id ? { ...item, name, accessUrl } : item)); setStatus("ready"); setMessage(`Updated door: ${name}`);
  }
  function deleteGarageProfile(id: string) {
    const target = garageProfiles.find((item) => item.id === id); if (!target) return;
    setGarageProfiles((previous) => { const next = previous.filter((item) => item.id !== id); if (activeGarageId === id) setActiveGarageId(next[0]?.id); return next; });
    setStatus("ready"); setMessage(`Deleted door: ${target.name}`);
  }
  function addPlateProfile(labelInput: string, plateInput: string) {
    const label = labelInput.trim() || `Plate ${plateProfiles.length + 1}`; const plateNumber = normalizePlate(plateInput);
    if (!plateNumber) throw new Error("Enter the license plate number.");
    const newPlate: PlateProfile = { id: createId("plate"), label, plateNumber, createdAt: new Date().toISOString() };
    setPlateProfiles((previous) => [...previous, newPlate]); setActivePlateId(newPlate.id); setStatus("ready"); setMessage(`Saved plate: ${label} · ${plateNumber}`);
  }
  function updatePlateProfile(id: string, labelInput: string, plateInput: string) {
    const label = labelInput.trim() || "Plate"; const plateNumber = normalizePlate(plateInput);
    if (!plateNumber) throw new Error("Enter the license plate number.");
    setPlateProfiles((previous) => previous.map((item) => item.id === id ? { ...item, label, plateNumber } : item)); setStatus("ready"); setMessage(`Updated plate: ${label} · ${plateNumber}`);
  }
  function deletePlateProfile(id: string) {
    const target = plateProfiles.find((item) => item.id === id); if (!target) return;
    setPlateProfiles((previous) => { const next = previous.filter((item) => item.id !== id); if (activePlateId === id) setActivePlateId(next[0]?.id); return next; });
    setStatus("ready"); setMessage(`Deleted plate: ${target.label}`);
  }
  async function clearAllProfiles() {
    await clearAllProfileStorage(); setGarageProfiles([]); setPlateProfiles([]); setActiveGarageId(undefined); setActivePlateId(undefined); setStatus("ready"); setMessage("All local profiles cleared.");
  }

  return { garageProfiles, plateProfiles, activeGarageId, activePlateId, activeGarage, activePlate, status, message, loaded, setStatus, setMessage, setActiveGarageId, setActivePlateId, addGarageProfile, updateGarageProfile, deleteGarageProfile, addPlateProfile, updatePlateProfile, deletePlateProfile, clearAllProfiles };
}
