import { StyleSheet } from "react-native";
import { colors } from "../styles/theme";

export const sectionStyles = StyleSheet.create({
  card: {
    backgroundColor: colors.card,
    borderRadius: 24,
    padding: 20,
    marginBottom: 18,
    shadowColor: "#000",
    shadowOpacity: 0.08,
    shadowRadius: 18,
    shadowOffset: { width: 0, height: 8 },
    elevation: 4,
  },
  sectionHeader: {
    marginBottom: 12,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: "900",
    color: colors.text,
  },
  sectionSubtitle: {
    marginTop: 4,
    fontSize: 13,
    color: colors.muted,
    lineHeight: 18,
  },
  mainRow: {
    marginTop: 12,
  },
  buttonRow: {
    flexDirection: "row",
    alignItems: "stretch",
    gap: 10,
    marginTop: 12,
  },
  compactMainButton: {
    minHeight: 66,
    borderRadius: 18,
    borderWidth: 1,
    borderColor: colors.green,
    backgroundColor: colors.greenSoft,
    paddingHorizontal: 16,
    paddingVertical: 12,
    justifyContent: "center",
  },
  compactMainTitle: {
    fontSize: 17,
    fontWeight: "900",
    color: colors.text,
  },
  compactMainSubtitle: {
    marginTop: 4,
    fontSize: 12,
    color: colors.muted,
  },
  emptyMainButton: {
    borderColor: colors.borderSoft,
    backgroundColor: colors.warningSoft,
  },
  smallButton: {
    flex: 1,
    minHeight: 54,
    borderRadius: 16,
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
    minHeight: 54,
    borderRadius: 16,
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
    minHeight: 54,
    borderRadius: 16,
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
