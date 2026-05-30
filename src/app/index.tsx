import { useEffect, useMemo, useState } from "react";

import {
  ActivityIndicator,
  Button,
  Image,
  ScrollView,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from "react-native";

import * as ImagePicker from "expo-image-picker";

import * as ImageManipulator from "expo-image-manipulator";

import { SafeAreaView } from "react-native-safe-area-context";
import GooddisplayEpaper, {
  WriteProgressEvent,
} from "../../modules/gooddisplay-epaper";

type TraceEntry = {
  timestampMs: number;
  phase: string;
  kind: string;
  commandHex: string;
  responseHex: string;
  durationMs: number;
  attempt: number;
  sw1: number;
  sw2: number;
};

// type ProgressEvent = {
//   phase: string;
//   uploadPercent: number;
//   packetsSent: number;
//   packetsTotal: number;
//   dePollCount: number;
//   message?: string;
//   sw1: number;
//   sw2: number;
// };

const PANEL_OPTIONS = [
  {
    label: '2.13" Mono',
    inchCode: 213,
    colorMode: "mono",
  },
  {
    label: '4.2" Mono',
    inchCode: 420,
    colorMode: "mono",
  },
  {
    label: '2.9" Tri',
    inchCode: 290,
    colorMode: "tri",
  },
  {
    label: '2.13" 4G',
    inchCode: 213,
    colorMode: "quad",
  },
];

export default function EpaperDebugScreen() {
  const [imageUri, setImageUri] = useState<string | null>(null);

  const [selectedPanel, setSelectedPanel] = useState(PANEL_OPTIONS[0]);

  const [logs, setLogs] = useState<string[]>([]);

  const [traces, setTraces] = useState<TraceEntry[]>([]);

  const [progress, setProgress] = useState<WriteProgressEvent | null>(null);

  const [isWriting, setIsWriting] = useState(false);

  const [rotation, setRotation] = useState(0);

  const appendLog = (message: string) => {
    console.log(message);

    setLogs((prev) => [
      `[${new Date().toLocaleTimeString()}] ${message}`,
      ...prev,
    ]);
  };

  useEffect(() => {
    const progressSub = GooddisplayEpaper.addListener(
      "onProgress",
      (event: WriteProgressEvent) => {
        setProgress(event);
      },
    );

    const statusSub = GooddisplayEpaper.addListener("onStatus", (event) => {
      appendLog(`STATUS ${event.phase} ${event.message ?? ""}`);
    });

    const traceSub = GooddisplayEpaper.addListener(
      "onTrace",
      (event: TraceEntry) => {
        setTraces((prev) => [event, ...prev]);

        console.log("TRACE", event.kind, event.commandHex, event.responseHex);
      },
    );

    const errorSub = GooddisplayEpaper.addListener("onError", (event) => {
      appendLog(`ERROR ${event.code}: ${event.message}`);

      setIsWriting(false);
    });

    const completeSub = GooddisplayEpaper.addListener(
      "onComplete",
      (result) => {
        appendLog(`COMPLETE APDUs=${result.totalApduCount}`);

        appendLog(`Retries=${result.transceiveRetryCount}`);

        appendLog(`DE Polls=${result.dePollCount}`);

        appendLog(`Duration=${result.totalDurationMs}ms`);

        setIsWriting(false);
      },
    );

    return () => {
      progressSub.remove();
      statusSub.remove();
      traceSub.remove();
      errorSub.remove();
      completeSub.remove();
    };
  }, []);

  const pickImage = async () => {
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ["images"],
      quality: 1,
    });

    if (result.canceled) {
      return;
    }

    const rawUri = result.assets[0].uri;

    const imageRef = await ImageManipulator.ImageManipulator.manipulate(rawUri)
      .rotate(rotation)
      .renderAsync();

    const manipulated = await imageRef.saveAsync({
      compress: 1,
      format: ImageManipulator.SaveFormat.PNG,
    });

    const uri = manipulated.uri;

    setImageUri(uri);

    appendLog(`Selected image: ${uri}`);
  };

  const startWrite = async () => {
    if (!imageUri) {
      appendLog("Select image first");
      return;
    }

    try {
      setIsWriting(true);

      setLogs([]);
      setTraces([]);
      setProgress(null);

      appendLog("Waiting for NFC tag...");

      appendLog("Bring E-Paper tag near device");

      const result = await GooddisplayEpaper.writeToTag({
        imageUri,
        inchCode: selectedPanel.inchCode,
        colorMode: selectedPanel.colorMode,
        emitApduTrace: true,
        transceiveMaxRetries: 2,
      });

      appendLog(`WRITE SUCCESS (${result.totalDurationMs}ms)`);
    } catch (e: any) {
      appendLog(`WRITE FAILED: ${e?.message ?? String(e)}`);
    } finally {
      setIsWriting(false);
    }
  };

  const cancelWrite = () => {
    appendLog("Cancelling write");

    GooddisplayEpaper.cancelWrite();

    setIsWriting(false);
  };

  const latestTrace = useMemo(() => {
    return traces[0];
  }, [traces]);

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView
        style={styles.container}
        contentContainerStyle={styles.content}
      >
        <Text style={styles.title}>GoodDisplay E-Paper Debug</Text>

        <Button title="Pick Image" onPress={pickImage} />

        {imageUri && (
          <Image source={{ uri: imageUri }} style={styles.preview} />
        )}

        <Text style={styles.section}>Panel</Text>

        <View style={styles.panelContainer}>
          {PANEL_OPTIONS.map((panel) => (
            <TouchableOpacity
              key={panel.label}
              style={[
                styles.panelButton,
                selectedPanel.label === panel.label &&
                  styles.panelButtonSelected,
              ]}
              onPress={() => setSelectedPanel(panel)}
            >
              <Text>{panel.label}</Text>
            </TouchableOpacity>
          ))}
        </View>

        <View style={styles.row}>
          {[0, 90, 180, 270].map((deg) => (
            <TouchableOpacity
              key={deg}
              style={[
                styles.rotationButton,
                rotation === deg && styles.rotationButtonActive,
              ]}
              onPress={() => setRotation(deg)}
            >
              <Text
                style={{
                  color: rotation === deg ? "#fff" : "#000",
                  fontWeight: "600",
                }}
              >
                {deg}°
              </Text>
            </TouchableOpacity>
          ))}
        </View>

        <View style={styles.actions}>
          <Button
            title={isWriting ? "Writing..." : "Write To Tag"}
            onPress={startWrite}
            disabled={isWriting}
          />

          <Button title="Cancel" color="red" onPress={cancelWrite} />
        </View>

        {isWriting && <ActivityIndicator size="large" />}

        {progress && (
          <View style={styles.card}>
            <Text>Phase: {progress.phase}</Text>

            <Text>Upload: {progress.uploadPercent}%</Text>

            <Text>
              Packets: {progress.packetsSent}/{progress.packetsTotal}
            </Text>

            <Text>DE Polls: {progress.dePollCount}</Text>

            <Text>
              SW: {progress.sw1.toString(16)} {progress.sw2.toString(16)}
            </Text>
          </View>
        )}

        {latestTrace && (
          <View style={styles.card}>
            <Text style={styles.section}>Latest Trace</Text>

            <Text>Kind: {latestTrace.kind}</Text>

            <Text>Duration: {latestTrace.durationMs}ms</Text>

            <Text numberOfLines={4}>CMD: {latestTrace.commandHex}</Text>

            <Text numberOfLines={2}>RSP: {latestTrace.responseHex}</Text>
          </View>
        )}

        <View style={styles.card}>
          <Text style={styles.section}>Logs</Text>

          {logs.map((log, idx) => (
            <Text key={`${log}-${idx}`} style={styles.log}>
              {log}
            </Text>
          ))}
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#fff",
  },

  content: {
    padding: 16,
    gap: 16,
  },

  title: {
    fontSize: 24,
    fontWeight: "700",
  },

  section: {
    fontSize: 18,
    fontWeight: "600",
  },

  preview: {
    width: 220,
    height: 220,
    borderWidth: 1,
    resizeMode: "contain",
  },

  panelContainer: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: 8,
  },

  panelButton: {
    padding: 12,
    borderWidth: 1,
    borderRadius: 8,
  },

  panelButtonSelected: {
    backgroundColor: "#ddd",
  },

  actions: {
    gap: 12,
  },

  card: {
    borderWidth: 1,
    borderRadius: 12,
    padding: 12,
    gap: 6,
  },

  log: {
    fontSize: 12,
  },

  rotationButton: {
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderWidth: 1,
    borderColor: "#ccc",
    borderRadius: 8,
    marginRight: 8,
  },

  rotationButtonActive: {
    backgroundColor: "#007AFF",
  },

  row: {
    flexDirection: "row",
    flexWrap: "wrap",
    marginVertical: 12,
  },
});
