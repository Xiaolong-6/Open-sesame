import { Pressable, Text, View } from "react-native";
import { sectionStyles as styles } from "./SectionStyles";
import type { GarageProfile } from "../types/profiles";

type Props = {
  activeGarage?: GarageProfile;
  onOpenPicker: () => void;
  onScan: () => void;
  onAdd: () => void;
  onEditActive: () => void;
  onDeleteActive: () => void;
};

export function DoorSection({
  activeGarage,
  onOpenPicker,
  onScan,
  onAdd,
  onEditActive,
  onDeleteActive,
}: Props) {
  return (
    <View style={styles.card}>
      <View style={styles.sectionHeader}>
        <Text style={styles.sectionTitle}>Step 1. Scan the door code</Text>
      </View>

      <View style={styles.mainRow}>
        <Pressable
          style={[
            styles.compactMainButton,
            activeGarage ? null : styles.emptyMainButton,
          ]}
          onPress={onOpenPicker}
        >
          <Text style={styles.compactMainTitle} numberOfLines={1}>
            {activeGarage ? activeGarage.name : "Choose door"}
          </Text>
        </Pressable>
      </View>

      <View style={styles.buttonRow}>
        <Pressable style={styles.primaryButton} onPress={onScan}>
          <Text style={styles.primaryButtonText}>Scan</Text>
        </Pressable>

        <Pressable style={styles.smallButton} onPress={activeGarage ? onEditActive : onAdd}>
          <Text style={styles.smallButtonText}>Edit</Text>
        </Pressable>

        <Pressable style={styles.dangerButton} onPress={onDeleteActive}>
          <Text style={styles.dangerButtonText}>Delete</Text>
        </Pressable>
      </View>
    </View>
  );
}
