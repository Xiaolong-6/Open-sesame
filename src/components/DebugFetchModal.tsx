import { Modal, Pressable, ScrollView, StyleSheet, Text, View } from "react-native";

import { colors } from "../styles/theme";
import type { DebugFetchResult } from "../services/debugFetch";

type Props = {
  visible: boolean;
  loading: boolean;
  result?: DebugFetchResult;
  onClose: () => void;
  onRunAgain: () => void;
};

export function DebugFetchModal({
  visible,
  loading,
  result,
  onClose,
  onRunAgain,
}: Props) {
  return (
    <Modal visible={visible} transparent animationType="fade">
      <View style={styles.backdrop}>
        <View style={styles.card}>
          <Text style={styles.title}>Debug fetch active door</Text>
          <Text style={styles.help}>
            This only performs a GET request to inspect the access page. It does not
            submit the plate number and does not open the door.
          </Text>

          <ScrollView style={styles.resultBox}>
            {loading ? <Text style={styles.line}>Fetching access page...</Text> : null}

            {!loading && !result ? (
              <Text style={styles.line}>No debug result yet.</Text>
            ) : null}

            {result ? (
              <>
                <Text style={styles.label}>Requested URL</Text>
                <Text style={styles.value}>{result.requestedUrl}</Text>

                <Text style={styles.label}>Result</Text>
                <Text style={[styles.value, result.ok ? styles.ok : styles.failed]}>
                  {result.ok ? "OK" : "FAILED"}
                </Text>

                <Text style={styles.label}>HTTP status</Text>
                <Text style={styles.value}>
                  {result.status ? `${result.status} ${result.statusText || ""}` : "n/a"}
                </Text>

                <Text style={styles.label}>Final URL</Text>
                <Text style={styles.value}>{result.finalUrl || "n/a"}</Text>

                <Text style={styles.label}>Content-Type</Text>
                <Text style={styles.value}>{result.contentType || "n/a"}</Text>

                <Text style={styles.label}>Detected title</Text>
                <Text style={styles.value}>{result.title || "n/a"}</Text>

                {result.error ? (
                  <>
                    <Text style={styles.label}>Error</Text>
                    <Text style={[styles.value, styles.failed]}>{result.error}</Text>
                  </>
                ) : null}

                <Text style={styles.label}>Page text snippet</Text>
                <Text style={styles.snippet}>{result.snippet || "n/a"}</Text>
              </>
            ) : null}
          </ScrollView>

          <View style={styles.actions}>
            <Pressable style={styles.cancelButton} onPress={onClose}>
              <Text style={styles.cancelButtonText}>Close</Text>
            </Pressable>

            <Pressable style={styles.runButton} onPress={onRunAgain} disabled={loading}>
              <Text style={styles.runButtonText}>{loading ? "Fetching..." : "Run again"}</Text>
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
    padding: 20,
  },
  card: {
    backgroundColor: colors.card,
    borderRadius: 24,
    padding: 18,
    maxHeight: "88%",
  },
  title: {
    fontSize: 22,
    fontWeight: "900",
    color: colors.text,
    marginBottom: 8,
  },
  help: {
    fontSize: 13,
    color: colors.muted,
    lineHeight: 19,
    marginBottom: 12,
  },
  resultBox: {
    borderRadius: 16,
    backgroundColor: colors.surface,
    borderWidth: 1,
    borderColor: colors.borderSoft,
    padding: 12,
    maxHeight: 420,
  },
  label: {
    marginTop: 10,
    fontSize: 12,
    color: colors.muted,
    fontWeight: "900",
    textTransform: "uppercase",
  },
  line: {
    fontSize: 14,
    color: colors.text,
  },
  value: {
    marginTop: 3,
    fontSize: 13,
    color: colors.text,
    lineHeight: 18,
  },
  ok: {
    color: colors.greenDark,
    fontWeight: "900",
  },
  failed: {
    color: colors.danger,
    fontWeight: "900",
  },
  snippet: {
    marginTop: 3,
    fontSize: 12,
    color: colors.text,
    lineHeight: 18,
  },
  actions: {
    flexDirection: "row",
    justifyContent: "flex-end",
    gap: 10,
    marginTop: 16,
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
  runButton: {
    borderRadius: 14,
    backgroundColor: colors.green,
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  runButtonText: {
    color: "#FFFFFF",
    fontWeight: "900",
  },
});
