export type OpenStatus = "ready" | "opening" | "success" | "failed";

export type GarageProfile = {
  id: string;
  name: string;
  accessUrl: string;
  createdAt: string;
  lastUsedAt?: string;
};

export type PlateProfile = {
  id: string;
  label: string;
  plateNumber: string;
  createdAt: string;
  lastUsedAt?: string;
};

export type ModalMode = "none" | "addGarage" | "editGarage" | "addPlate" | "editPlate" | "scanGarage";
