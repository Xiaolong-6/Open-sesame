import { useEffect, useState } from "react";
import { Modal, Pressable, StyleSheet, Text, TextInput, View } from "react-native";
import { colors } from "../styles/theme";
import type { GarageProfile, ModalMode, PlateProfile } from "../types/profiles";

type Props = {
  modalMode: ModalMode;
  editingGarage?: GarageProfile;
  editingPlate?: PlateProfile;
  scannedGarageUrl?: string;
  defaultGarageName: string;
  defaultPlateLabel: string;
  onClose: () => void;
  onSaveGarage: (name: string, url: string) => void;
  onUpdateGarage: (id: string, name: string, url: string) => void;
  onSavePlate: (label: string, plate: string) => void;
  onUpdatePlate: (id: string, label: string, plate: string) => void;
};

export function ProfileModal(props: Props) {
  const [garageName, setGarageName] = useState("");
  const [garageUrl, setGarageUrl] = useState("");
  const [plateLabel, setPlateLabel] = useState("");
  const [plateNumber, setPlateNumber] = useState("");

  useEffect(() => {
    if (props.modalMode === "addGarage") { setGarageName(props.defaultGarageName); setGarageUrl(props.scannedGarageUrl || ""); }
    if (props.modalMode === "editGarage" && props.editingGarage) { setGarageName(props.editingGarage.name); setGarageUrl(props.editingGarage.accessUrl); }
    if (props.modalMode === "addPlate") { setPlateLabel(props.defaultPlateLabel); setPlateNumber(""); }
    if (props.modalMode === "editPlate" && props.editingPlate) { setPlateLabel(props.editingPlate.label); setPlateNumber(props.editingPlate.plateNumber); }
  }, [props.modalMode, props.editingGarage, props.editingPlate, props.scannedGarageUrl, props.defaultGarageName, props.defaultPlateLabel]);

  const isGarage = props.modalMode === "addGarage" || props.modalMode === "editGarage";
  const isPlate = props.modalMode === "addPlate" || props.modalMode === "editPlate";
  const visible = isGarage || isPlate;

  function handleSave() {
    if (props.modalMode === "addGarage") props.onSaveGarage(garageName, garageUrl);
    if (props.modalMode === "editGarage" && props.editingGarage) props.onUpdateGarage(props.editingGarage.id, garageName, garageUrl);
    if (props.modalMode === "addPlate") props.onSavePlate(plateLabel, plateNumber);
    if (props.modalMode === "editPlate" && props.editingPlate) props.onUpdatePlate(props.editingPlate.id, plateLabel, plateNumber);
  }

  return <Modal visible={visible} transparent animationType="fade"><View style={styles.modalBackdrop}><View style={styles.modalCard}>{isGarage ? <><Text style={styles.modalTitle}>{props.modalMode === "editGarage" ? "Edit door" : "Add door"}</Text><Text style={styles.label}>Door name</Text><TextInput value={garageName} onChangeText={setGarageName} placeholder="Home garage" style={styles.input} /><Text style={styles.label}>Autoparkki access URL</Text><TextInput value={garageUrl} onChangeText={setGarageUrl} placeholder="https://dc.autoparkki.fi/access/..." autoCapitalize="none" autoCorrect={false} keyboardType="url" style={styles.input} /></> : null}{isPlate ? <><Text style={styles.modalTitle}>{props.modalMode === "editPlate" ? "Edit plate" : "Add plate"}</Text><Text style={styles.label}>Plate label</Text><TextInput value={plateLabel} onChangeText={setPlateLabel} placeholder="My car" style={styles.input} /><Text style={styles.label}>License plate</Text><TextInput value={plateNumber} onChangeText={(text) => setPlateNumber(text.toUpperCase())} placeholder="ABC-123" autoCapitalize="characters" autoCorrect={false} style={styles.input} /></> : null}<View style={styles.modalActions}><Pressable style={styles.cancelButton} onPress={props.onClose}><Text style={styles.cancelButtonText}>Cancel</Text></Pressable><Pressable style={styles.saveButton} onPress={handleSave}><Text style={styles.saveButtonText}>Save</Text></Pressable></View></View></View></Modal>;
}

const styles = StyleSheet.create({ modalBackdrop: { flex: 1, backgroundColor: "rgba(16, 24, 40, 0.45)", justifyContent: "center", padding: 24 }, modalCard: { backgroundColor: colors.card, borderRadius: 24, padding: 20 }, modalTitle: { fontSize: 24, fontWeight: "900", color: colors.text, marginBottom: 12 }, label: { fontSize: 14, fontWeight: "700", color: "#344054", marginBottom: 8, marginTop: 10 }, input: { height: 52, borderRadius: 14, borderWidth: 1, borderColor: colors.border, paddingHorizontal: 14, fontSize: 16, backgroundColor: colors.surface, color: "#101828" }, modalActions: { flexDirection: "row", justifyContent: "flex-end", gap: 10, marginTop: 20 }, cancelButton: { borderRadius: 14, backgroundColor: colors.surfaceSoft, paddingHorizontal: 16, paddingVertical: 12 }, cancelButtonText: { color: "#475467", fontWeight: "800" }, saveButton: { borderRadius: 14, backgroundColor: colors.green, paddingHorizontal: 16, paddingVertical: 12 }, saveButtonText: { color: "#FFFFFF", fontWeight: "900" } });
