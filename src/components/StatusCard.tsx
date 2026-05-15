import { StyleSheet, Text, View } from "react-native";
import { colors } from "../styles/theme";
import type { OpenStatus } from "../types/profiles";

type Props = {
  status: OpenStatus;
  message: string;
  loaded: boolean;
};

function getStatusText(status: OpenStatus, loaded: boolean) {
  if (!loaded) return "LOADING";
  if (status === "opening") return "OPENING";
  if (status === "success") return "SUCCESS";
  if (status === "failed") return "FAILED";
  return "READY";
}

export function StatusCard({ status, message, loaded }: Props) {
  return (
    <View
      style={[
        styles.statusCard,
        status === "success" ? styles.statusCardSuccess : null,
        status === "failed" ? styles.statusCardFailed : null,
        status === "opening" ? styles.statusCardOpening : null,
      ]}
    >
      <Text style={styles.statusLabel}>Status</Text>
      <Text
        style={[
          styles.statusText,
          status === "failed" ? styles.statusTextFailed : null,
        ]}
      >
        {getStatusText(status, loaded)}
      </Text>
      <Text style={styles.message} numberOfLines={2}>
        {loaded ? message : "Loading saved settings..."}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  statusCard: {
    backgroundColor: colors.greenSoft,
    borderRadius: 18,
    padding: 14,
    marginTop: 0,
  },
  statusCardSuccess: {
    backgroundColor: colors.successSoft,
  },
  statusCardFailed: {
    backgroundColor: colors.dangerSoft,
  },
  statusCardOpening: {
    backgroundColor: colors.openingSoft,
  },
  statusLabel: {
    fontSize: 12,
    color: "#667085",
    fontWeight: "700",
    textTransform: "uppercase",
  },
  statusText: {
    marginTop: 4,
    fontSize: 21,
    color: colors.greenDark,
    fontWeight: "900",
  },
  statusTextFailed: {
    color: colors.danger,
  },
  message: {
    marginTop: 4,
    fontSize: 13,
    color: "#344054",
  },
});
