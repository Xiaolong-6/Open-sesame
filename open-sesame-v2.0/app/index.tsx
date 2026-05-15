import AsyncStorage from "@react-native-async-storage/async-storage";
import {
  BarcodeScanningResult,
  CameraView,
  useCameraPermissions,
} from "expo-camera";
import { useEffect, useMemo, useState } from "react";
import {
  Alert,
  KeyboardAvoidingView,
  Modal,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

type OpenStatus = "ready" | "opening" | "success" | "failed";

type GarageProfile = {
  id: string;
  name: string;
  accessUrl: string;
  createdAt: string;
  lastUsedAt?: string;
};

type PlateProfile = {
  id: string;
  label: string;
  plateNumber: string;
  createdAt: string;
  lastUsedAt?: string;
};

type ModalMode = "none" | "addGarage" | "addPlate" | "scanGarage";

const STORAGE_KEYS = {
  garages: "openSesame.garageProfiles",
  plates: "openSesame.plateProfiles",
  activeGarageId: "openSesame.activeGarageProfileId",
  activePlateId: "openSesame.activePlateProfileId",
};

function createId(prefix: string) {
  return `${prefix}_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;
}

function normalizePlate(input: string) {
  return input.trim().toUpperCase();
}

function normalizeUrl(input: string) {
  return input.trim();
}

function extractAutoparkkiUrlFromQr(rawData: string) {
  const data = rawData.trim();

  // QR generators sometimes wrap the URL in text, add whitespace/newlines,
  // or preserve upper/lower-case inconsistently. Extract the first URL robustly.
  const urlMatch = data.match(/https?:\/\/[^\s"'<>]+/i);
  const detectedUrl = (urlMatch ? urlMatch[0] : data).trim();

  let parsedUrl: URL;

  try {
    parsedUrl = new URL(detectedUrl);
  } catch {
    return undefined;
  }

  const hostname = parsedUrl.hostname.toLowerCase();

  if (parsedUrl.protocol !== "https:") {
    return undefined;
  }

  // Accept autoparkki.fi and all subdomains, e.g. dc.autoparkki.fi.
  if (hostname !== "autoparkki.fi" && !hostname.endsWith(".autoparkki.fi")) {
    return undefined;
  }

  // Your known URLs use /access/<uuid>. Keep this permissive but still useful.
  if (!parsedUrl.pathname.toLowerCase().startsWith("/access/")) {
    return undefined;
  }

  return parsedUrl.toString();
}

export default function HomeScreen() {
  const [garageProfiles, setGarageProfiles] = useState<GarageProfile[]>([]);
  const [plateProfiles, setPlateProfiles] = useState<PlateProfile[]>([]);

  const [activeGarageId, setActiveGarageId] = useState<string | undefined>();
  const [activePlateId, setActivePlateId] = useState<string | undefined>();

  const [status, setStatus] = useState<OpenStatus>("ready");
  const [message, setMessage] = useState("Ready");
  const [loaded, setLoaded] = useState(false);

  const [modalMode, setModalMode] = useState<ModalMode>("none");
  const [cameraPermission, requestCameraPermission] = useCameraPermissions();
  const [scanLocked, setScanLocked] = useState(false);

  const [garageNameDraft, setGarageNameDraft] = useState("");
  const [garageUrlDraft, setGarageUrlDraft] = useState("");

  const [plateLabelDraft, setPlateLabelDraft] = useState("");
  const [plateNumberDraft, setPlateNumberDraft] = useState("");

  const activeGarage = useMemo(
    () => garageProfiles.find((item) => item.id === activeGarageId),
    [garageProfiles, activeGarageId]
  );

  const activePlate = useMemo(
    () => plateProfiles.find((item) => item.id === activePlateId),
    [plateProfiles, activePlateId]
  );

  useEffect(() => {
    async function loadSavedSettings() {
      try {
        const [savedGarages, savedPlates, savedGarageId, savedPlateId] =
          await Promise.all([
            AsyncStorage.getItem(STORAGE_KEYS.garages),
            AsyncStorage.getItem(STORAGE_KEYS.plates),
            AsyncStorage.getItem(STORAGE_KEYS.activeGarageId),
            AsyncStorage.getItem(STORAGE_KEYS.activePlateId),
          ]);

        const parsedGarages: GarageProfile[] = savedGarages
          ? JSON.parse(savedGarages)
          : [];
        const parsedPlates: PlateProfile[] = savedPlates
          ? JSON.parse(savedPlates)
          : [];

        setGarageProfiles(parsedGarages);
        setPlateProfiles(parsedPlates);

        if (savedGarageId && parsedGarages.some((g) => g.id === savedGarageId)) {
          setActiveGarageId(savedGarageId);
        } else if (parsedGarages.length > 0) {
          setActiveGarageId(parsedGarages[0].id);
        }

        if (savedPlateId && parsedPlates.some((p) => p.id === savedPlateId)) {
          setActivePlateId(savedPlateId);
        } else if (parsedPlates.length > 0) {
          setActivePlateId(parsedPlates[0].id);
        }
      } catch {
        setStatus("failed");
        setMessage("Could not load saved settings.");
      } finally {
        setLoaded(true);
      }
    }

    loadSavedSettings();
  }, []);

  useEffect(() => {
    if (!loaded) return;

    AsyncStorage.setItem(
      STORAGE_KEYS.garages,
      JSON.stringify(garageProfiles)
    ).catch(() => {
      setStatus("failed");
      setMessage("Could not save garage profiles.");
    });
  }, [garageProfiles, loaded]);

  useEffect(() => {
    if (!loaded) return;

    AsyncStorage.setItem(STORAGE_KEYS.plates, JSON.stringify(plateProfiles)).catch(
      () => {
        setStatus("failed");
        setMessage("Could not save plate profiles.");
      }
    );
  }, [plateProfiles, loaded]);

  useEffect(() => {
    if (!loaded) return;

    if (activeGarageId) {
      AsyncStorage.setItem(STORAGE_KEYS.activeGarageId, activeGarageId).catch(
        () => {
          setStatus("failed");
          setMessage("Could not save active garage.");
        }
      );
    } else {
      AsyncStorage.removeItem(STORAGE_KEYS.activeGarageId).catch(() => {});
    }
  }, [activeGarageId, loaded]);

  useEffect(() => {
    if (!loaded) return;

    if (activePlateId) {
      AsyncStorage.setItem(STORAGE_KEYS.activePlateId, activePlateId).catch(
        () => {
          setStatus("failed");
          setMessage("Could not save active plate.");
        }
      );
    } else {
      AsyncStorage.removeItem(STORAGE_KEYS.activePlateId).catch(() => {});
    }
  }, [activePlateId, loaded]);

  function confirmAction(title: string, messageText: string, onConfirm: () => void) {
    if (Platform.OS === "web") {
      const confirmed = window.confirm(`${title}\n\n${messageText}`);
      if (confirmed) {
        onConfirm();
      }
      return;
    }

    Alert.alert(title, messageText, [
      {
        text: "Cancel",
        style: "cancel",
      },
      {
        text: "Delete",
        style: "destructive",
        onPress: onConfirm,
      },
    ]);
  }

  function openAddGarageModal() {
    setGarageNameDraft(`Garage ${garageProfiles.length + 1}`);
    setGarageUrlDraft("");
    setModalMode("addGarage");
  }

  async function openScanGarageModal() {
    setScanLocked(false);

    if (!cameraPermission?.granted) {
      const result = await requestCameraPermission();

      if (!result.granted) {
        Alert.alert(
          "Camera permission needed",
          "Camera access is required to scan the garage QR code."
        );
        return;
      }
    }

    setModalMode("scanGarage");
  }

  function handleBarcodeScanned(result: BarcodeScanningResult) {
    if (scanLocked) return;

    setScanLocked(true);

    const extractedUrl = extractAutoparkkiUrlFromQr(result.data);

    if (!extractedUrl) {
      setScanLocked(false);
      Alert.alert(
        "Invalid QR code",
        "This QR code does not look like an Autoparkki access URL."
      );
      return;
    }

    setGarageUrlDraft(extractedUrl);
    setGarageNameDraft(`Garage ${garageProfiles.length + 1}`);
    setStatus("ready");
    setMessage("QR scanned. Name this door and save it.");
    setModalMode("addGarage");
  }

  function openAddPlateModal() {
    setPlateLabelDraft(`Plate ${plateProfiles.length + 1}`);
    setPlateNumberDraft("");
    setModalMode("addPlate");
  }

  function closeModal() {
    setModalMode("none");
  }

  function saveGarageProfile() {
    const name = garageNameDraft.trim() || `Garage ${garageProfiles.length + 1}`;
    const accessUrl = normalizeUrl(garageUrlDraft);

    if (!accessUrl) {
      Alert.alert("Missing URL", "Paste the Autoparkki QR/access URL.");
      return;
    }

    if (!accessUrl.startsWith("https://")) {
      Alert.alert("Invalid URL", "The access URL should start with https://.");
      return;
    }

    const now = new Date().toISOString();

    const newGarage: GarageProfile = {
      id: createId("garage"),
      name,
      accessUrl,
      createdAt: now,
    };

    setGarageProfiles((previous) => [...previous, newGarage]);
    setActiveGarageId(newGarage.id);
    setStatus("ready");
    setMessage(`Saved door: ${name}`);
    closeModal();
  }

  function savePlateProfile() {
    const label = plateLabelDraft.trim() || `Plate ${plateProfiles.length + 1}`;
    const plateNumber = normalizePlate(plateNumberDraft);

    if (!plateNumber) {
      Alert.alert("Missing plate number", "Enter the license plate number.");
      return;
    }

    const now = new Date().toISOString();

    const newPlate: PlateProfile = {
      id: createId("plate"),
      label,
      plateNumber,
      createdAt: now,
    };

    setPlateProfiles((previous) => [...previous, newPlate]);
    setActivePlateId(newPlate.id);
    setStatus("ready");
    setMessage(`Saved plate: ${label} · ${plateNumber}`);
    closeModal();
  }

  function deleteGarage(id: string) {
    const target = garageProfiles.find((item) => item.id === id);
    if (!target) return;

    confirmAction("Delete door?", `Delete "${target.name}" from this device?`, () => {
      setGarageProfiles((previous) => {
        const next = previous.filter((item) => item.id !== id);

        if (activeGarageId === id) {
          setActiveGarageId(next[0]?.id);
        }

        return next;
      });

      setStatus("ready");
      setMessage(`Deleted door: ${target.name}`);
    });
  }

  function deletePlate(id: string) {
    const target = plateProfiles.find((item) => item.id === id);
    if (!target) return;

    confirmAction(
      "Delete plate?",
      `Delete "${target.label} · ${target.plateNumber}" from this device?`,
      () => {
        setPlateProfiles((previous) => {
          const next = previous.filter((item) => item.id !== id);

          if (activePlateId === id) {
            setActivePlateId(next[0]?.id);
          }

          return next;
        });

        setStatus("ready");
        setMessage(`Deleted plate: ${target.label}`);
      }
    );
  }

  async function handleOpenDoor() {
    if (!activeGarage) {
      Alert.alert("No door selected", "Add or select a garage door first.");
      return;
    }

    if (!activePlate) {
      Alert.alert("No plate selected", "Add or select a license plate first.");
      return;
    }

    setStatus("opening");
    setMessage(`Opening ${activeGarage.name} for ${activePlate.plateNumber}...`);

    try {
      // v0.3 mock mode.
      // Real Autoparkki request will be added after QR/profile flow is stable.
      await new Promise((resolve) => setTimeout(resolve, 900));

      // Do not write profile metadata during mock OPEN.
      // On some native devices, back-to-back AsyncStorage writes triggered by
      // profile timestamp updates can race and falsely show "Could not save ...".
      // We will re-enable last-used timestamps when the real opener request is wired.
      setStatus("success");
      setMessage(`Mock success: ${activeGarage.name} · ${activePlate.plateNumber}`);
    } catch (error) {
      setStatus("failed");
      setMessage(error instanceof Error ? error.message : "Unknown error");
    }
  }

  function getStatusText() {
    if (!loaded) return "LOADING";

    switch (status) {
      case "ready":
        return "READY";
      case "opening":
        return "OPENING";
      case "success":
        return "SUCCESS";
      case "failed":
        return "FAILED";
      default:
        return "READY";
    }
  }

  function getStatusHint() {
    if (!loaded) {
      return "Loading saved settings...";
    }

    return message;
  }

  return (
    <SafeAreaView style={styles.safeArea}>
      <KeyboardAvoidingView
        style={styles.keyboard}
        behavior={Platform.OS === "ios" ? "padding" : undefined}
      >
        <ScrollView contentContainerStyle={styles.container}>
          <View style={styles.header}>
            <Text style={styles.title}>Open-Sesame</Text>
            <Text style={styles.subtitle}>You only have to scan once</Text>
          </View>

          <View style={styles.card}>
            <View style={styles.sectionHeader}>
              <Text style={styles.sectionTitle}>Current door</Text>

              <View style={styles.sectionActions}>
                <Pressable style={styles.scanButton} onPress={openScanGarageModal}>
                  <Text style={styles.scanButtonText}>Scan</Text>
                </Pressable>

                <Pressable style={styles.smallButton} onPress={openAddGarageModal}>
                  <Text style={styles.smallButtonText}>Add door</Text>
                </Pressable>
              </View>
            </View>

            {activeGarage ? (
              <View style={styles.activeBox}>
                <Text style={styles.activeTitle}>{activeGarage.name}</Text>
                <Text style={styles.activeSubtitle} numberOfLines={1}>
                  {activeGarage.accessUrl}
                </Text>
              </View>
            ) : (
              <View style={styles.emptyBox}>
                <Text style={styles.emptyText}>No door saved yet.</Text>
              </View>
            )}

            <View style={styles.profileList}>
              {garageProfiles.map((garage) => (
                <View key={garage.id} style={styles.profileRow}>
                  <Pressable
                    style={[
                      styles.profileMain,
                      garage.id === activeGarageId ? styles.profileMainActive : null,
                    ]}
                    onPress={() => {
                      setActiveGarageId(garage.id);
                      setStatus("ready");
                      setMessage(`Selected door: ${garage.name}`);
                    }}
                  >
                    <Text style={styles.profileTitle}>{garage.name}</Text>
                    <Text style={styles.profileSubtitle} numberOfLines={1}>
                      {garage.accessUrl}
                    </Text>
                  </Pressable>

                  <Pressable
                    style={styles.deleteButton}
                    onPress={() => deleteGarage(garage.id)}
                  >
                    <Text style={styles.deleteButtonText}>Delete</Text>
                  </Pressable>
                </View>
              ))}
            </View>
          </View>

          <View style={styles.card}>
            <View style={styles.sectionHeader}>
              <Text style={styles.sectionTitle}>License plate</Text>
              <Pressable style={styles.smallButton} onPress={openAddPlateModal}>
                <Text style={styles.smallButtonText}>Add plate</Text>
              </Pressable>
            </View>

            {activePlate ? (
              <View style={styles.activeBox}>
                <Text style={styles.activeTitle}>
                  {activePlate.label} · {activePlate.plateNumber}
                </Text>
              </View>
            ) : (
              <View style={styles.emptyBox}>
                <Text style={styles.emptyText}>No plate saved yet.</Text>
              </View>
            )}

            <View style={styles.profileList}>
              {plateProfiles.map((plate) => (
                <View key={plate.id} style={styles.profileRow}>
                  <Pressable
                    style={[
                      styles.profileMain,
                      plate.id === activePlateId ? styles.profileMainActive : null,
                    ]}
                    onPress={() => {
                      setActivePlateId(plate.id);
                      setStatus("ready");
                      setMessage(
                        `Selected plate: ${plate.label} · ${plate.plateNumber}`
                      );
                    }}
                  >
                    <Text style={styles.profileTitle}>
                      {plate.label} · {plate.plateNumber}
                    </Text>
                  </Pressable>

                  <Pressable
                    style={styles.deleteButton}
                    onPress={() => deletePlate(plate.id)}
                  >
                    <Text style={styles.deleteButtonText}>Delete</Text>
                  </Pressable>
                </View>
              ))}
            </View>

            <Pressable
              style={({ pressed }) => [
                styles.openButton,
                pressed || status === "opening" ? styles.openButtonPressed : null,
              ]}
              onPress={handleOpenDoor}
              disabled={status === "opening"}
            >
              <Text style={styles.openButtonText}>
                {status === "opening" ? "OPENING..." : "OPEN"}
              </Text>
            </Pressable>
          </View>

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
              {getStatusText()}
            </Text>
            <Text style={styles.message}>{getStatusHint()}</Text>
          </View>

          <Text style={styles.note}>
            v0.3.1 QR/profile mode. Real garage request is not enabled yet.
          </Text>
        </ScrollView>

        <Modal visible={modalMode !== "none"} transparent animationType="fade">
          <View style={styles.modalBackdrop}>
            <View style={styles.modalCard}>
              {modalMode === "addGarage" ? (
                <>
                  <Text style={styles.modalTitle}>Add door</Text>

                  <Text style={styles.label}>Door name</Text>
                  <TextInput
                    value={garageNameDraft}
                    onChangeText={setGarageNameDraft}
                    placeholder="Home garage"
                    style={styles.input}
                  />

                  <Text style={styles.label}>Autoparkki access URL</Text>
                  <TextInput
                    value={garageUrlDraft}
                    onChangeText={setGarageUrlDraft}
                    placeholder="https://dc.autoparkki.fi/access/..."
                    autoCapitalize="none"
                    autoCorrect={false}
                    keyboardType="url"
                    style={styles.input}
                  />

                  <View style={styles.modalActions}>
                    <Pressable style={styles.cancelButton} onPress={closeModal}>
                      <Text style={styles.cancelButtonText}>Cancel</Text>
                    </Pressable>
                    <Pressable style={styles.saveButton} onPress={saveGarageProfile}>
                      <Text style={styles.saveButtonText}>Save door</Text>
                    </Pressable>
                  </View>
                </>
              ) : null}

              {modalMode === "addPlate" ? (
                <>
                  <Text style={styles.modalTitle}>Add plate</Text>

                  <Text style={styles.label}>Plate label</Text>
                  <TextInput
                    value={plateLabelDraft}
                    onChangeText={setPlateLabelDraft}
                    placeholder="My car"
                    style={styles.input}
                  />

                  <Text style={styles.label}>License plate</Text>
                  <TextInput
                    value={plateNumberDraft}
                    onChangeText={(text) => setPlateNumberDraft(text.toUpperCase())}
                    placeholder="ABC-123"
                    autoCapitalize="characters"
                    autoCorrect={false}
                    style={styles.input}
                  />

                  <View style={styles.modalActions}>
                    <Pressable style={styles.cancelButton} onPress={closeModal}>
                      <Text style={styles.cancelButtonText}>Cancel</Text>
                    </Pressable>
                    <Pressable style={styles.saveButton} onPress={savePlateProfile}>
                      <Text style={styles.saveButtonText}>Save plate</Text>
                    </Pressable>
                  </View>
                </>
              ) : null}

              {modalMode === "scanGarage" ? (
                <>
                  <Text style={styles.modalTitle}>Scan door QR</Text>

                  <View style={styles.scannerBox}>
                    <CameraView
                      style={styles.scanner}
                      facing="back"
                      barcodeScannerSettings={{
                        barcodeTypes: ["qr"],
                      }}
                      onBarcodeScanned={
                        scanLocked ? undefined : handleBarcodeScanned
                      }
                    />
                  </View>

                  <Text style={styles.scanHint}>
                    Point the camera at the Autoparkki QR code on the garage door.
                  </Text>

                  <View style={styles.modalActions}>
                    <Pressable style={styles.cancelButton} onPress={closeModal}>
                      <Text style={styles.cancelButtonText}>Cancel</Text>
                    </Pressable>

                    <Pressable
                      style={styles.saveButton}
                      onPress={() => {
                        setScanLocked(false);
                      }}
                    >
                      <Text style={styles.saveButtonText}>Scan again</Text>
                    </Pressable>
                  </View>
                </>
              ) : null}
            </View>
          </View>
        </Modal>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: "#F4F1EA",
  },
  keyboard: {
    flex: 1,
  },
  container: {
    flexGrow: 1,
    padding: 24,
    paddingBottom: 36,
  },
  header: {
    marginTop: 16,
    marginBottom: 24,
  },
  title: {
    fontSize: 38,
    fontWeight: "800",
    color: "#1F2933",
    letterSpacing: -0.6,
  },
  subtitle: {
    marginTop: 8,
    fontSize: 17,
    color: "#697386",
  },
  card: {
    backgroundColor: "#FFFFFF",
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
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    gap: 12,
    marginBottom: 12,
  },
  sectionActions: {
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: "900",
    color: "#1F2933",
  },
  smallButton: {
    borderRadius: 12,
    backgroundColor: "#E8F3EE",
    paddingHorizontal: 12,
    paddingVertical: 8,
  },
  smallButtonText: {
    color: "#14543D",
    fontSize: 13,
    fontWeight: "800",
  },
  scanButton: {
    borderRadius: 12,
    backgroundColor: "#1F7A5A",
    paddingHorizontal: 12,
    paddingVertical: 8,
  },
  scanButtonText: {
    color: "#FFFFFF",
    fontSize: 13,
    fontWeight: "900",
  },
  activeBox: {
    borderRadius: 18,
    backgroundColor: "#F2F4F7",
    padding: 16,
    marginBottom: 12,
  },
  activeTitle: {
    fontSize: 17,
    fontWeight: "900",
    color: "#1F2933",
  },
  activeSubtitle: {
    marginTop: 6,
    fontSize: 13,
    color: "#697386",
  },
  emptyBox: {
    borderRadius: 18,
    backgroundColor: "#FFF7E8",
    padding: 16,
    marginBottom: 12,
  },
  emptyText: {
    fontSize: 14,
    color: "#7A4D00",
    fontWeight: "700",
  },
  profileList: {
    gap: 10,
  },
  profileRow: {
    flexDirection: "row",
    alignItems: "stretch",
    gap: 8,
  },
  profileMain: {
    flex: 1,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: "#EAECF0",
    backgroundColor: "#FAFAFA",
    padding: 12,
  },
  profileMainActive: {
    borderColor: "#1F7A5A",
    backgroundColor: "#E8F3EE",
  },
  profileTitle: {
    fontSize: 15,
    fontWeight: "800",
    color: "#1F2933",
  },
  profileSubtitle: {
    marginTop: 4,
    fontSize: 12,
    color: "#697386",
  },
  deleteButton: {
    width: 78,
    borderRadius: 16,
    backgroundColor: "#FDE8E8",
    justifyContent: "center",
    alignItems: "center",
  },
  deleteButtonText: {
    color: "#B42318",
    fontSize: 12,
    fontWeight: "900",
  },
  openButton: {
    marginTop: 22,
    height: 76,
    borderRadius: 22,
    backgroundColor: "#1F7A5A",
    justifyContent: "center",
    alignItems: "center",
  },
  openButtonPressed: {
    opacity: 0.75,
  },
  openButtonText: {
    color: "#FFFFFF",
    fontSize: 28,
    fontWeight: "900",
    letterSpacing: 1.5,
  },
  statusCard: {
    backgroundColor: "#E8F3EE",
    borderRadius: 20,
    padding: 18,
  },
  statusCardSuccess: {
    backgroundColor: "#DFF3E8",
  },
  statusCardFailed: {
    backgroundColor: "#FDE8E8",
  },
  statusCardOpening: {
    backgroundColor: "#EEF4FF",
  },
  statusLabel: {
    fontSize: 13,
    color: "#667085",
    fontWeight: "700",
    textTransform: "uppercase",
  },
  statusText: {
    marginTop: 6,
    fontSize: 24,
    color: "#14543D",
    fontWeight: "900",
  },
  statusTextFailed: {
    color: "#B42318",
  },
  message: {
    marginTop: 6,
    fontSize: 15,
    color: "#344054",
  },
  note: {
    marginTop: 18,
    fontSize: 13,
    color: "#697386",
    textAlign: "center",
  },
  modalBackdrop: {
    flex: 1,
    backgroundColor: "rgba(16, 24, 40, 0.45)",
    justifyContent: "center",
    padding: 24,
  },
  modalCard: {
    backgroundColor: "#FFFFFF",
    borderRadius: 24,
    padding: 20,
  },
  modalTitle: {
    fontSize: 24,
    fontWeight: "900",
    color: "#1F2933",
    marginBottom: 12,
  },
  label: {
    fontSize: 14,
    fontWeight: "700",
    color: "#344054",
    marginBottom: 8,
    marginTop: 10,
  },
  input: {
    height: 52,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: "#D0D5DD",
    paddingHorizontal: 14,
    fontSize: 16,
    backgroundColor: "#FAFAFA",
    color: "#101828",
  },
  modalActions: {
    flexDirection: "row",
    justifyContent: "flex-end",
    gap: 10,
    marginTop: 20,
  },
  cancelButton: {
    borderRadius: 14,
    backgroundColor: "#F2F4F7",
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  cancelButtonText: {
    color: "#475467",
    fontWeight: "800",
  },
  saveButton: {
    borderRadius: 14,
    backgroundColor: "#1F7A5A",
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  saveButtonText: {
    color: "#FFFFFF",
    fontWeight: "900",
  },
  scannerBox: {
    height: 320,
    borderRadius: 20,
    overflow: "hidden",
    backgroundColor: "#101828",
    marginTop: 12,
  },
  scanner: {
    flex: 1,
  },
  scanHint: {
    marginTop: 12,
    fontSize: 14,
    color: "#697386",
    lineHeight: 20,
  },
});
