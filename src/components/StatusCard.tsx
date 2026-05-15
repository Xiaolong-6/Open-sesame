import { StyleSheet, Text, View } from "react-native";
import { colors } from "../styles/theme";
import type { OpenStatus } from "../types/profiles";

type Props = { status: OpenStatus; message: string; loaded: boolean; };
function getStatusText(status: OpenStatus, loaded: boolean) { if (!loaded) return "LOADING"; if (status === "opening") return "OPENING"; if (status === "success") return "SUCCESS"; if (status === "failed") return "FAILED"; return "READY"; }
export function StatusCard({ status, message, loaded }: Props) { return <View style={[styles.statusCard, status === "success" ? styles.statusCardSuccess : null, status === "failed" ? styles.statusCardFailed : null, status === "opening" ? styles.statusCardOpening : null]}><Text style={styles.statusLabel}>Status</Text><Text style={[styles.statusText, status === "failed" ? styles.statusTextFailed : null]}>{getStatusText(status, loaded)}</Text><Text style={styles.message}>{loaded ? message : "Loading saved settings..."}</Text></View>; }
const styles = StyleSheet.create({ statusCard: { backgroundColor: colors.greenSoft, borderRadius: 20, padding: 18 }, statusCardSuccess: { backgroundColor: colors.successSoft }, statusCardFailed: { backgroundColor: colors.dangerSoft }, statusCardOpening: { backgroundColor: colors.openingSoft }, statusLabel: { fontSize: 13, color: "#667085", fontWeight: "700", textTransform: "uppercase" }, statusText: { marginTop: 6, fontSize: 24, color: colors.greenDark, fontWeight: "900" }, statusTextFailed: { color: colors.danger }, message: { marginTop: 6, fontSize: 15, color: "#344054" } });
