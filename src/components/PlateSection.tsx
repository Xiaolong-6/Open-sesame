import { Pressable, Text, View } from "react-native";
import { sectionStyles as styles } from "./SectionStyles";
import type { PlateProfile } from "../types/profiles";

type Props = {
  activePlate?: PlateProfile;
  onOpenPicker: () => void;
  onAdd: () => void;
  onEditActive: () => void;
  onDeleteActive: () => void;
};

export function PlateSection({
  activePlate,
  onOpenPicker,
  onAdd,
  onEditActive,
  onDeleteActive,
}: Props) {
  return (
    <View style={styles.card}>
      <View style={styles.sectionHeader}>
        <Text style={styles.sectionTitle}>Step 2. Enter your license plate</Text>
      </View>

      <View style={styles.mainRow}>
        <Pressable
          style={[
            styles.compactMainButton,
            activePlate ? null : styles.emptyMainButton,
          ]}
          onPress={onOpenPicker}
        >
          <Text style={styles.compactMainTitle} numberOfLines={1}>
            {activePlate ? activePlate.plateNumber : "Choose plate"}
          </Text>
        </Pressable>
      </View>

      <View style={styles.buttonRow}>
        <Pressable style={styles.primaryButton} onPress={onAdd}>
          <Text style={styles.primaryButtonText}>Add</Text>
        </Pressable>

        <Pressable style={styles.smallButton} onPress={activePlate ? onEditActive : onAdd}>
          <Text style={styles.smallButtonText}>Edit</Text>
        </Pressable>

        <Pressable style={styles.dangerButton} onPress={onDeleteActive}>
          <Text style={styles.dangerButtonText}>Delete</Text>
        </Pressable>
      </View>
    </View>
  );
}
