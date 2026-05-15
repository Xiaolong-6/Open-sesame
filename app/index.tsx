import { BarcodeScanningResult, useCameraPermissions } from "expo-camera";
import { useState } from "react";
import {
  Alert,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";

import { DebugFetchModal } from "../src/components/DebugFetchModal";
import { DebugPanel } from "../src/components/DebugPanel";
import { DoorSection } from "../src/components/DoorSection";
import { PlateSection } from "../src/components/PlateSection";
import { ProfileModal } from "../src/components/ProfileModal";
import { ProfilePickerModal } from "../src/components/ProfilePickerModal";
import { ScanGarageModal } from "../src/components/ScanGarageModal";
import { StatusCard } from "../src/components/StatusCard";
import { APP_INFO } from "../src/constants/appInfo";
import { useOpenSesameProfiles } from "../src/hooks/useOpenSesameProfiles";
import {
  debugFetchAccessPage,
  type DebugFetchResult,
} from "../src/services/debugFetch";
import { suggestDoorNameFromAccessUrl } from "../src/services/doorMetadata";
import { mockOpenDoor } from "../src/services/opener";
import { extractAutoparkkiUrlFromQr } from "../src/services/qr";
import { colors } from "../src/styles/theme";
import type {
  GarageProfile,
  ModalMode,
  PickerMode,
  PlateProfile,
} from "../src/types/profiles";

function confirmAction(title: string, messageText: string, onConfirm: () => void) {
  if (Platform.OS === "web") {
    const confirmed = window.confirm(`${title}\n\n${messageText}`);
    if (confirmed) onConfirm();
    return;
  }

  Alert.alert(title, messageText, [
    { text: "Cancel", style: "cancel" },
    { text: "OK", style: "destructive", onPress: onConfirm },
  ]);
}

export default function HomeScreen() {
  const profiles = useOpenSesameProfiles();
  const [modalMode, setModalMode] = useState<ModalMode>("none");
  const [pickerMode, setPickerMode] = useState<PickerMode>("none");
  const [editingGarage, setEditingGarage] = useState<GarageProfile | undefined>();
  const [editingPlate, setEditingPlate] = useState<PlateProfile | undefined>();
  const [scannedGarageUrl, setScannedGarageUrl] = useState<string | undefined>();
  const [scannedGarageName, setScannedGarageName] = useState<string | undefined>();
  const [scanLocked, setScanLocked] = useState(false);
  const [debugFetchVisible, setDebugFetchVisible] = useState(false);
  const [debugFetchLoading, setDebugFetchLoading] = useState(false);
  const [debugFetchResult, setDebugFetchResult] = useState<DebugFetchResult | undefined>();
  const [cameraPermission, requestCameraPermission] = useCameraPermissions();

  function closeModal() {
    setModalMode("none");
    setEditingGarage(undefined);
    setEditingPlate(undefined);
    setScannedGarageUrl(undefined);
    setScannedGarageName(undefined);
  }

  function handleError(error: unknown) {
    Alert.alert("Cannot save", error instanceof Error ? error.message : "Unknown error");
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
        `This does not look like an Autoparkki access URL.\n\nRaw content:\n${result.data}`
      );
      return;
    }

    setScannedGarageUrl(extractedUrl);
    setScannedGarageName("Detecting door name...");
    profiles.setStatus("ready");
    profiles.setMessage("QR scanned. Detecting door name...");
    setModalMode("addGarage");

    suggestDoorNameFromAccessUrl(extractedUrl)
      .then((suggestedName) => {
        if (suggestedName) {
          setScannedGarageName(suggestedName);
          profiles.setMessage(`Detected door: ${suggestedName}`);
        }
      })
      .catch(() => {
        profiles.setMessage("QR scanned. Name this door and save it.");
      });
  }

  async function runDebugFetch() {
    if (!profiles.activeGarage) {
      Alert.alert("No door selected", "Select a saved door before running debug fetch.");
      return;
    }

    setDebugFetchVisible(true);
    setDebugFetchLoading(true);

    try {
      const result = await debugFetchAccessPage(profiles.activeGarage.accessUrl);
      setDebugFetchResult(result);
    } finally {
      setDebugFetchLoading(false);
    }
  }

  async function handleOpenDoor() {
    if (!profiles.activeGarage) {
      Alert.alert("No door selected", "Add or select a garage door first.");
      return;
    }

    if (!profiles.activePlate) {
      Alert.alert("No plate selected", "Add or select a license plate first.");
      return;
    }

    profiles.setStatus("opening");
    profiles.setMessage(
      `Opening ${profiles.activeGarage.name} for ${profiles.activePlate.plateNumber}...`
    );

    try {
      const message = await mockOpenDoor(profiles.activeGarage, profiles.activePlate);
      profiles.setStatus("success");
      profiles.setMessage(message);
    } catch (error) {
      profiles.setStatus("failed");
      profiles.setMessage(error instanceof Error ? error.message : "Unknown error");
    }
  }

  function editActiveGarage() {
    if (!profiles.activeGarage) {
      setModalMode("addGarage");
      return;
    }
    setEditingGarage(profiles.activeGarage);
    setModalMode("editGarage");
  }

  function deleteActiveGarage() {
    if (!profiles.activeGarage) {
      Alert.alert("No door selected", "There is no current door to delete.");
      return;
    }
    confirmAction("Delete door?", `Delete "${profiles.activeGarage.name}" from this device?`, () =>
      profiles.deleteGarageProfile(profiles.activeGarage!.id)
    );
  }

  function editActivePlate() {
    if (!profiles.activePlate) {
      setModalMode("addPlate");
      return;
    }
    setEditingPlate(profiles.activePlate);
    setModalMode("editPlate");
  }

  function deleteActivePlate() {
    if (!profiles.activePlate) {
      Alert.alert("No plate selected", "There is no current plate to delete.");
      return;
    }
    confirmAction(
      "Delete plate?",
      `Delete "${profiles.activePlate.plateNumber}" from this device?`,
      () => profiles.deletePlateProfile(profiles.activePlate!.id)
    );
  }

  return (
    <SafeAreaView style={styles.safeArea}>
      <KeyboardAvoidingView
        style={styles.keyboard}
        behavior={Platform.OS === "ios" ? "padding" : undefined}
      >
        <ScrollView contentContainerStyle={styles.container}>
          <View style={styles.header}>
            <Text style={styles.title}>{APP_INFO.name}</Text>
            <Text style={styles.subtitle}>{APP_INFO.subtitle}</Text>
          </View>

          <DoorSection
            activeGarage={profiles.activeGarage}
            onOpenPicker={() => setPickerMode("garage")}
            onScan={openScanGarageModal}
            onAdd={() => setModalMode("addGarage")}
            onEditActive={editActiveGarage}
            onDeleteActive={deleteActiveGarage}
          />

          <PlateSection
            activePlate={profiles.activePlate}
            onOpenPicker={() => setPickerMode("plate")}
            onAdd={() => setModalMode("addPlate")}
            onEditActive={editActivePlate}
            onDeleteActive={deleteActivePlate}
          />

          <View style={styles.openCard}>
            <Text style={styles.openTitle}>Step 3. Open the door</Text>
            <Pressable
              style={({ pressed }) => [
                styles.openButton,
                pressed || profiles.status === "opening" ? styles.openButtonPressed : null,
              ]}
              onPress={handleOpenDoor}
              disabled={profiles.status === "opening"}
            >
              <Text style={styles.openButtonText}>
                {profiles.status === "opening" ? "OPENING..." : "OPEN"}
              </Text>
            </Pressable>
          </View>

          <StatusCard
            status={profiles.status}
            message={profiles.message}
            loaded={profiles.loaded}
          />

          <DebugPanel
            garageProfiles={profiles.garageProfiles}
            plateProfiles={profiles.plateProfiles}
            activeGarage={profiles.activeGarage}
            activePlate={profiles.activePlate}
            onDebugFetch={runDebugFetch}
            onClearAll={() =>
              confirmAction(
                "Clear all local profiles?",
                "This removes all saved doors and plates from this device.",
                () => {
                  profiles.clearAllProfiles().catch(() => {
                    profiles.setStatus("failed");
                    profiles.setMessage("Could not clear local profiles.");
                  });
                }
              )
            }
          />

          <Text style={styles.note}>
            v{APP_INFO.version}. {APP_INFO.purpose}
          </Text>
        </ScrollView>

        <ProfileModal
          modalMode={modalMode}
          editingGarage={editingGarage}
          editingPlate={editingPlate}
          scannedGarageUrl={scannedGarageUrl}
          defaultGarageName={scannedGarageName || `Garage ${profiles.garageProfiles.length + 1}`}
          defaultPlateLabel={`Plate ${profiles.plateProfiles.length + 1}`}
          onClose={closeModal}
          onSaveGarage={(name, url) => {
            try {
              profiles.addGarageProfile(name, url);
              closeModal();
            } catch (error) {
              handleError(error);
            }
          }}
          onUpdateGarage={(id, name, url) => {
            try {
              profiles.updateGarageProfile(id, name, url);
              closeModal();
            } catch (error) {
              handleError(error);
            }
          }}
          onSavePlate={(label, plate) => {
            try {
              profiles.addPlateProfile(label, plate);
              closeModal();
            } catch (error) {
              handleError(error);
            }
          }}
          onUpdatePlate={(id, label, plate) => {
            try {
              profiles.updatePlateProfile(id, label, plate);
              closeModal();
            } catch (error) {
              handleError(error);
            }
          }}
        />

        <ProfilePickerModal
          pickerMode={pickerMode}
          garageProfiles={profiles.garageProfiles}
          plateProfiles={profiles.plateProfiles}
          activeGarageId={profiles.activeGarageId}
          activePlateId={profiles.activePlateId}
          onClose={() => setPickerMode("none")}
          onSelectGarage={(id) => {
            profiles.setActiveGarageId(id);
            profiles.setStatus("ready");
            profiles.setMessage("Selected door.");
          }}
          onSelectPlate={(id) => {
            profiles.setActivePlateId(id);
            profiles.setStatus("ready");
            profiles.setMessage("Selected plate.");
          }}
        />

        <ScanGarageModal
          visible={modalMode === "scanGarage"}
          scanLocked={scanLocked}
          onBarcodeScanned={handleBarcodeScanned}
          onClose={closeModal}
          onScanAgain={() => setScanLocked(false)}
        />

        <DebugFetchModal
          visible={debugFetchVisible}
          loading={debugFetchLoading}
          result={debugFetchResult}
          onClose={() => setDebugFetchVisible(false)}
          onRunAgain={runDebugFetch}
        />
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: colors.background,
  },
  keyboard: {
    flex: 1,
  },
  container: {
    flexGrow: 1,
    padding: 20,
    paddingBottom: 24,
  },
  header: {
    marginTop: 10,
    marginBottom: 16,
  },
  title: {
    fontSize: 34,
    fontWeight: "800",
    color: colors.text,
    letterSpacing: -0.6,
  },
  subtitle: {
    marginTop: 4,
    fontSize: 15,
    color: colors.muted,
  },
  openCard: {
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
  openTitle: {
    fontSize: 17,
    fontWeight: "900",
    color: colors.text,
    marginBottom: 10,
  },
  openButton: {
    height: 64,
    borderRadius: 20,
    backgroundColor: colors.green,
    justifyContent: "center",
    alignItems: "center",
  },
  openButtonPressed: {
    opacity: 0.75,
  },
  openButtonText: {
    color: "#FFFFFF",
    fontSize: 26,
    fontWeight: "900",
    letterSpacing: 1.5,
  },
  note: {
    marginTop: 12,
    fontSize: 12,
    lineHeight: 17,
    color: colors.muted,
    textAlign: "center",
  },
});
