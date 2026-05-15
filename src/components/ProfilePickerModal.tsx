import { Modal, Pressable, StyleSheet, Text, View } from "react-native";
import { colors } from "../styles/theme";
import type { GarageProfile, PickerMode, PlateProfile } from "../types/profiles";

type Props = {
  pickerMode: PickerMode;
  garageProfiles: GarageProfile[];
  plateProfiles: PlateProfile[];
  activeGarageId?: string;
  activePlateId?: string;
  onClose: () => void;
  onSelectGarage: (id: string) => void;
  onSelectPlate: (id: string) => void;
};

export function ProfilePickerModal({
  pickerMode,
  garageProfiles,
  plateProfiles,
  activeGarageId,
  activePlateId,
  onClose,
  onSelectGarage,
  onSelectPlate,
}: Props) {
  const isGarage = pickerMode === "garage";
  const isPlate = pickerMode === "plate";

  return (
    <Modal visible={pickerMode !== "none"} transparent animationType="fade">
      <View style={styles.backdrop}>
        <View style={styles.card}>
          <Text style={styles.title}>{isGarage ? "Choose door" : "Choose plate"}</Text>

          {isGarage && garageProfiles.length === 0 ? (
            <Text style={styles.emptyText}>No saved doors yet. Scan or add one first.</Text>
          ) : null}

          {isPlate && plateProfiles.length === 0 ? (
            <Text style={styles.emptyText}>No saved plates yet. Add one first.</Text>
          ) : null}

          {isGarage
            ? garageProfiles.map((garage) => (
                <Pressable
                  key={garage.id}
                  style={[
                    styles.option,
                    garage.id === activeGarageId ? styles.optionActive : null,
                  ]}
                  onPress={() => {
                    onSelectGarage(garage.id);
                    onClose();
                  }}
                >
                  <Text style={styles.optionTitle}>{garage.name}</Text>
                  <Text style={styles.optionSubtitle} numberOfLines={1}>
                    {garage.accessUrl}
                  </Text>
                </Pressable>
              ))
            : null}

          {isPlate
            ? plateProfiles.map((plate) => (
                <Pressable
                  key={plate.id}
                  style={[
                    styles.option,
                    plate.id === activePlateId ? styles.optionActive : null,
                  ]}
                  onPress={() => {
                    onSelectPlate(plate.id);
                    onClose();
                  }}
                >
                  <Text style={styles.optionTitle}>{plate.plateNumber}</Text>
                </Pressable>
              ))
            : null}

          <View style={styles.actions}>
            <Pressable style={styles.cancelButton} onPress={onClose}>
              <Text style={styles.cancelButtonText}>Close</Text>
            </Pressable>
          </View>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  backdrop: {
    flex: 1,
    backgroundColor: "rgba(16, 24, 40, 0.45)",
    justifyContent: "center",
    padding: 24,
  },
  card: {
    backgroundColor: colors.card,
    borderRadius: 24,
    padding: 20,
    maxHeight: "80%",
  },
  title: {
    fontSize: 24,
    fontWeight: "900",
    color: colors.text,
    marginBottom: 14,
  },
  emptyText: {
    fontSize: 14,
    color: colors.muted,
    lineHeight: 20,
  },
  option: {
    borderRadius: 16,
    borderWidth: 1,
    borderColor: colors.borderSoft,
    backgroundColor: colors.surface,
    padding: 14,
    marginBottom: 10,
  },
  optionActive: {
    borderColor: colors.green,
    backgroundColor: colors.greenSoft,
  },
  optionTitle: {
    fontSize: 15,
    fontWeight: "900",
    color: colors.text,
  },
  optionSubtitle: {
    marginTop: 4,
    fontSize: 12,
    color: colors.muted,
  },
  actions: {
    alignItems: "flex-end",
    marginTop: 8,
  },
  cancelButton: {
    borderRadius: 14,
    backgroundColor: colors.surfaceSoft,
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  cancelButtonText: {
    color: "#475467",
    fontWeight: "800",
  },
});
