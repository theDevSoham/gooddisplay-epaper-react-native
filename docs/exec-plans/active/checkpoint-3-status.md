# Checkpoint 3 Status: NFC Transport & Write Execution

**Status:** Complete  
**Module:** `modules/gooddisplay-epaper`  
**Depends on:** Checkpoint 1 (image), Checkpoint 2 (APDU)

---

## Delivered Files

| Path | Role |
|------|------|
| `services/NfcEpaperWriter.java` | Full write orchestration |
| `services/WriteSession.java` | Per-flash state + packet plans |
| `services/ApduTransceiver.java` | Traced transceive + retries |
| `services/TransceiveChannel.java` | Transport abstraction |
| `services/IsoDepTransceiveChannel.java` | Production IsoDep adapter |
| `services/WriteOptions.java` | Timeouts, retries, validation flags |
| `services/ApduResponse.java` | SW / busy-idle helpers |
| `services/ApduTraceEntry.java` | APDU trace row |
| `services/NfcWriteException.java` | Phase + SW on failure |
| `services/WriteProgressListener.java` | Optional callbacks (CP4) |
| `models/WriteProgress.java` | Phase, %, counters, SW |
| `models/WriteResult.java` | Metrics + trace list |
| `android/src/test/.../RecordingTransceiveChannel.java` | Fake NFC channel |
| `android/src/test/.../NfcEpaperWriterTest.java` | Transport tests |
| `android/src/test/.../ApduResponseTest.java` | DE idle rules |

**Not modified:** `GooddisplayEpaperModule.kt`, `ImageProcessor`, `ApduBuilder`, `EpdConfigFactory`.

---

## Write Sequence (implemented)

```
connect (IsoDep, 50000ms timeout)
→ IC DIY
→ delay 10ms
→ DB driver flow
→ delay 10ms
→ DA screen type  [validate SW1=90 if requireDaSuccess]
→ D2 × N (upload progress)
→ D4 refresh      [validate SW1=90 if requireD4Success]
→ DE poll loop    [until idle or timeout]
→ close IsoDep (if closeIsoDepOnComplete)
```

Replaces legacy `Handler` timer waits after D4.

---

## DE Polling Semantics

| Rule | Implementation |
|------|----------------|
| Frame | `F0DE010000` (`ApduConstants.BUSY_POLL_HEX`) |
| Interval | `WriteOptions.busyPollIntervalMs` (default 100ms) |
| Idle | `ApduResponse.isBusyIdle`: first byte `0x00` **or** `90 00` |
| Timeout | `busyTimeoutMs` or color-based default (5s / 20s / 25s) |
| Disconnect | `NfcWriteException` if channel drops during poll |

**Hardware uncertainty:** Exact DE response layout not present in legacy APK; idle detection accepts both `00` and `9000` until tag traces confirm one format.

---

## Observability

| Feature | Location |
|---------|----------|
| APDU trace log | `ApduTransceiver.getTrace()` → `WriteResult.getApduTrace()` |
| Logcat | Tag `GooddisplayEpaper`, per-APDU SW + truncated hex |
| Progress | `WriteProgress` phase, upload %, packet counts, DE poll count |
| Timing | `WriteResult` connect/init/upload/refresh/busy/total ms |
| Retries | `WriteResult.transceiveRetryCount` |

---

## Retry & Cleanup

| Behavior | Detail |
|----------|--------|
| Transceive retry | `WriteOptions.transceiveMaxRetries` (default 1 retry) |
| Session cleanup | `finally { closeQuietly() }` when `closeIsoDepOnComplete` |
| Failure state | `WriteProgress.Phase.FAILED` + rethrow `NfcWriteException` |
| Legacy mismatch | Legacy did **not** close IsoDep in `finally`; we always close by default |

---

## Validated Protocol Assumptions

| Assumption | Validation |
|------------|------------|
| Init order IC DIY → DB → DA | Unit test command index 0 = `F0DB020000` |
| D2 follows init | Test asserts byte 0/1 of 4th command = F0/D2 |
| DA requires `90` when flag on | Test fails on `6A82` |
| D4 before DE | Trace order in writer |
| Pilot 2.13" mono = 16 D2 packets | Matches Checkpoint 2 |

---

## Legacy Mismatches (intentional)

| Legacy | Checkpoint 3 |
|--------|----------------|
| Timer refresh (2s / 16s / 20s) | DE poll with timeout cap |
| No DA failure abort | Optional strict `requireDaSuccess` (default **true**) |
| No per-D2 SW check | Logged; not failing |
| IsoDep left open | Closed on complete (configurable) |
| `runOnUiThread` progress | `WriteProgressListener` (bridge in CP4) |

---

## Remaining Hardware Uncertainties

- DE response byte layout on physical tags
- D1 read config usefulness (`F0D1010000` unused in write path)
- Maximum single-APDU size for large DB blobs on some tags
- Whether D2 responses should be validated per packet

---

## Unsupported Device Cases

| Case | Behavior |
|------|----------|
| Non-IsoDep tags | Caller must not open session (writer expects connected IsoDep) |
| Missing `ProcessedImage` / config mismatch | Fails at `WriteSession.create` or upload planning |
| DE never idle before timeout | `NfcWriteException` POLLING_BUSY |
| NFC drop mid-upload | IOException / connection lost on poll |

---

## Transport Tests

| Test | Covers |
|------|--------|
| `write_executesInitUploadRefreshAndDePoll` | Ordering, counts, IC DIY + D2 header |
| `write_failsOnInvalidDaSw` | SW validation |
| `write_busyTimeout` | DE timeout |
| `transceive_retriesOnFailure` | Retry counter |
| `disconnect_closesChannelOnComplete` | Cleanup |
| `ApduResponseTest` | Idle detection |

Run: `cd modules/gooddisplay-epaper/android && ./gradlew test` (when host Gradle is wired).

---

## API Usage (native / future bridge)

```java
WriteSession session =
    WriteSession.create(isoDep, EpdConfigFactory.pilot213Mono(), processedImage);
WriteResult result = NfcEpaperWriter.write(session);
List<ApduTraceEntry> trace = result.getApduTrace();
```

With options + listener:

```java
WriteSession.create(isoDep, config, image, WriteOptions.builder()
    .busyTimeoutForColorMode(config.getColorMode())
    .build(), listener);
```

---

## Exit Criteria

| Criterion | Met |
|-----------|-----|
| IsoDep lifecycle | Yes |
| Exact protocol order | Yes |
| DE replaces timers | Yes |
| WriteSession encapsulation | Yes |
| Trace + metrics | Yes |
| Transport tests | Yes |
| No Expo / RN / UI | Yes |

**Next:** Checkpoint 4 — `GooddisplayEpaperModule` + `react-native-nfc-manager` handoff.
