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
  actionRow: {
    flexDirection: "row",
    alignItems: "stretch",
    gap: 8,
    marginTop: 12,
  },
  compactMainButton: {
    flex: 1,
    minHeight: 62,
    borderRadius: 18,
    borderWidth: 1,
    borderColor: colors.green,
    backgroundColor: colors.greenSoft,
    paddingHorizontal: 14,
    paddingVertical: 12,
    justifyContent: "center",
  },
  compactMainTitle: {
    fontSize: 16,
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
    borderRadius: 16,
    backgroundColor: colors.surfaceSoft,
    paddingHorizontal: 12,
    justifyContent: "center",
    alignItems: "center",
    minWidth: 58,
  },
  smallButtonText: {
    color: "#475467",
    fontSize: 12,
    fontWeight: "900",
  },
  primaryButton: {
    borderRadius: 16,
    backgroundColor: colors.green,
    paddingHorizontal: 12,
    justifyContent: "center",
    alignItems: "center",
    minWidth: 64,
  },
  primaryButtonText: {
    color: "#FFFFFF",
    fontSize: 12,
    fontWeight: "900",
  },
  dangerButton: {
    borderRadius: 16,
    backgroundColor: colors.dangerSoft,
    paddingHorizontal: 12,
    justifyContent: "center",
    alignItems: "center",
    minWidth: 70,
  },
  dangerButtonText: {
    color: colors.danger,
    fontSize: 12,
    fontWeight: "900",
  },
});
