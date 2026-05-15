import { Linking, Pressable, StyleSheet, Text, View } from "react-native";
import { APP_INFO } from "../constants/appInfo";
import { colors } from "../styles/theme";
import type { GarageProfile, PlateProfile } from "../types/profiles";

type Props = {
  garageProfiles: GarageProfile[];
  plateProfiles: PlateProfile[];
  activeGarage?: GarageProfile;
  activePlate?: PlateProfile;
  onClearAll: () => void;
};

export function DebugPanel({
  garageProfiles,
  plateProfiles,
  activeGarage,
  activePlate,
  onClearAll,
}: Props) {
  return (
    <View style={styles.card}>
      <Text style={styles.title}>Debug</Text>
      <Text style={styles.line}>Version: {APP_INFO.version}</Text>
      <Text style={styles.line}>Mode: {APP_INFO.mode}</Text>
      <Text style={styles.line}>Saved doors: {garageProfiles.length}</Text>
      <Text style={styles.line}>Saved plates: {plateProfiles.length}</Text>
      <Text style={styles.line}>Active door: {activeGarage?.name || "none"}</Text>
      <Text style={styles.line}>
        Active plate: {activePlate ? `${activePlate.label} · ${activePlate.plateNumber}` : "none"}
      </Text>

      <Pressable style={styles.releaseButton} onPress={() => Linking.openURL(APP_INFO.releaseUrl)}>
        <Text style={styles.releaseButtonText}>Check releases manually</Text>
      </Pressable>

      <Pressable style={styles.clearButton} onPress={onClearAll}>
        <Text style={styles.clearButtonText}>Clear all local profiles</Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    marginTop: 18,
    backgroundColor: colors.surfaceSoft,
    borderRadius: 20,
    padding: 18,
  },
  title: {
    fontSize: 18,
    fontWeight: "900",
    color: colors.text,
    marginBottom: 8,
  },
  line: {
    fontSize: 13,
    color: colors.muted,
    marginTop: 4,
  },
  releaseButton: {
    marginTop: 14,
    borderRadius: 14,
    backgroundColor: colors.greenSoft,
    paddingVertical: 12,
    alignItems: "center",
  },
  releaseButtonText: {
    color: colors.greenDark,
    fontWeight: "900",
  },
  clearButton: {
    marginTop: 10,
    borderRadius: 14,
    backgroundColor: colors.dangerSoft,
    paddingVertical: 12,
    alignItems: "center",
  },
  clearButtonText: {
    color: colors.danger,
    fontWeight: "900",
  },
});
