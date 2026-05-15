import { StyleSheet } from "react-native";
import { colors } from "../styles/theme";

export const sectionStyles = StyleSheet.create({
  card: {
    backgroundColor: colors.card,
    borderRadius: 22,
    padding: 16,
    marginBottom: 12,
    shadowColor: "#000",
    shadowOpacity: 0.06,
    shadowRadius: 14,
    shadowOffset: { width: 0, height: 6 },
    elevation: 3,
  },
  sectionHeader: {
    marginBottom: 10,
  },
  sectionTitle: {
    fontSize: 17,
    fontWeight: "900",
    color: colors.text,
  },
  mainRow: {
    marginTop: 4,
  },
  buttonRow: {
    flexDirection: "row",
    alignItems: "stretch",
    gap: 8,
    marginTop: 10,
  },
  compactMainButton: {
    minHeight: 50,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: colors.green,
    backgroundColor: colors.greenSoft,
    paddingHorizontal: 14,
    justifyContent: "center",
  },
  compactMainTitle: {
    fontSize: 16,
    fontWeight: "900",
    color: colors.text,
  },
  emptyMainButton: {
    borderColor: colors.borderSoft,
    backgroundColor: colors.warningSoft,
  },
  smallButton: {
    flex: 1,
    minHeight: 46,
    borderRadius: 14,
    backgroundColor: colors.surfaceSoft,
    justifyContent: "center",
    alignItems: "center",
  },
  smallButtonText: {
    color: "#475467",
    fontSize: 13,
    fontWeight: "900",
  },
  primaryButton: {
    flex: 1,
    minHeight: 46,
    borderRadius: 14,
    backgroundColor: colors.green,
    justifyContent: "center",
    alignItems: "center",
  },
  primaryButtonText: {
    color: "#FFFFFF",
    fontSize: 13,
    fontWeight: "900",
  },
  dangerButton: {
    flex: 1,
    minHeight: 46,
    borderRadius: 14,
    backgroundColor: colors.dangerSoft,
    justifyContent: "center",
    alignItems: "center",
  },
  dangerButtonText: {
    color: colors.danger,
    fontSize: 13,
    fontWeight: "900",
  },
});
