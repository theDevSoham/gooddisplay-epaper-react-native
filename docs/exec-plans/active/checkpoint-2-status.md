# Checkpoint 2 Status: APDU & Protocol Configuration

**Status:** Complete  
**Module:** `modules/gooddisplay-epaper`  
**Depends on:** Checkpoint 1 (`ImageProcessor`, `ProcessedImage`)

---

## Delivered Files

| Path | Role |
|------|------|
| `protocol/ApduConstants.java` | F0, D2/D4/DE/D1, sizes, legacy timeouts |
| `protocol/ApduPacket.java` | Immutable frame + kind + D2 index metadata |
| `protocol/ApduBuilder.java` | IC DIY, DB, DA, D2, D4, D1, DE, init + upload sequencing |
| `protocol/EpdConfigFactory.java` | `ScreenConfig` factory + `MainActivity` geometry |
| `protocol/LegacyEpdInit.java` | Port of `data.setEpdInit` (local vars, case blocks) |
| `models/ScreenConfig.java` | Panel + driver/DA bytes + flags |
| `protocol/HexCodec.java` | *(Checkpoint 1 — used by factory)* |
| `android/src/test/.../ApduBuilderTest.java` | APDU parity tests |
| `android/src/test/.../EpdConfigFactoryTest.java` | DA/DB + preset tests |

**Not modified:** `GooddisplayEpaperModule.kt`, `ImageProcessor` behavior, NFC.

---

## Migrated Protocol Flows

| Legacy step | API |
|-------------|-----|
| `F0DB020000` | `ApduBuilder.buildIcDiy()` |
| `HexString2Btyes(setEpdInit()[0])` | `ApduBuilder.buildDriverFlow(config)` |
| `HexString2Btyes(setEpdInit()[1])` | `ApduBuilder.buildScreenType(config)` |
| D2 upload loops | `ApduBuilder.buildD2Sequence` / `buildImageUploadSequence` |
| D4 `refreshcmd` | `ApduBuilder.buildRefresh(colorMode)` |
| *(none)* DE poll | `ApduBuilder.buildBusyPoll()` — assumed frame |
| *(none)* D1 read | `ApduBuilder.buildReadConfig()` — assumed frame |
| `data.setEpdInit` | `LegacyEpdInit` + `EpdConfigFactory.create` |

### Init sequence (Checkpoint 3)

```
buildInitSequence(config)
  → IC DIY
  → DB driver flow
  → DA screen type
```

### Image upload (Checkpoint 3)

```
buildImageUploadSequence(config, processedImage)
  → D2 packets per legacy pass rules
  → buildRefresh(colorMode)
  → buildBusyPoll() loop (target; not legacy)
```

---

## D2 Upload Pass Rules (parity)

| Profile | Passes |
|---------|--------|
| Mono (default) | BW ← `blackLayer` |
| Mono 3.7" (`inch=370`) | BW ← `0xFF` fill, then R ← `blackLayer` |
| Mono 4.2" (`inch=420`) | BW ← `blackLayer`, R ← `invert(colorLayer)` |
| Tri | BW ← `blackLayer`, R ← `invert(colorLayer)` |
| Quad | BW ← `blackLayer` (4G buffer) |

Red inversion matches legacy `(byte)(0xFF - image_buffer[j])` on wire.

### Chunking

- Payload cap **250** bytes, APDU length **255**
- Tail rule: on last full index `i`, if `datas % 250 != 0`, extra packet at `i + 1` with slice from offset `(datas/250)*250` (legacy index math)

---

## ScreenConfig

Replaces mutable `epdArray`:

| Field | Source |
|-------|--------|
| `width`, `height` | `MainActivity` geometry |
| `inchCode`, `colorMode` | Legacy `epdArray[6]`, `[4]` |
| `driverConfig` | `setEpdInit()[0]` bytes |
| `screenTypeCommand` | `setEpdInit()[1]` bytes (DA) |
| `scanDirection` | `VERTICAL_MIRROR_LEGACY` |
| `protocolFlags` | 370/420/tri upload behavior |
| `getProcessingWidth/Height()` | ≥4.2" swap (Checkpoint 1 alignment) |

`toImageProcessingConfig()` bridges to Checkpoint 1 without changing image algorithms.

---

## Remaining Legacy Dependencies

| Item | Checkpoint |
|------|------------|
| `IsoDep.transceive` | 3 |
| `delay_ms(10)` between init commands | 3 |
| SW `0x90` validation | 3 |
| Timer-based refresh wait | 3 → replace with DE poll |
| `react-native-nfc-manager` tag handoff | 4 |
| Expo events / `writeToTag` | 4–5 |

---

## Unsupported Panel Types

| Case | Status |
|------|--------|
| Inch codes not in `MainActivity` | `EpdConfigFactory.create` throws |
| Color/inch combo missing in `LegacyEpdInit` | `supports()` returns false / create throws |
| 4.2" + quad color in UI | Legacy blocks navigation — not in factory |
| D3 compressed upload | Constant only; no builder body |
| Custom / user-defined driver flow | Not supported |

---

## Parity Gaps

| Gap | Notes |
|-----|--------|
| **DE / D1 frames** | Not in legacy APK; `F0DE010000` / `F0D1010000` assumed — **verify on tag** in CP3 |
| **DB single transceive** | Legacy sends full driver blob in one APDU; not split (may hit tag max on some devices) |
| **Golden DB hex per panel** | Tests assert DA for pilot + DB prefix; full DB hash not snapshotted |
| **`LegacyEpdInit` generated** | Sync from `data/data.java` when legacy tables change |
| **D2 with `datas < 250`** | Legacy loop sends zero iterations; same here |

---

## Protocol Assumptions

1. Vendor prefix `0xF0` on all commands.
2. D2 packet index byte wraps as legacy (`byte` cast).
3. DA command is exact hex from `setEpdInit()[1]` — no regeneration from width/height.
4. DE busy complete when response byte indicates idle (`0x00`) — frame format TBD on hardware.
5. D1 read config returns tag configuration — optional diagnostic command.
6. `ScreenConfig.icModel` defaults to SSD (`2`) for all `MainActivity` presets.

---

## Verification

```bash
cd modules/gooddisplay-epaper/android
./gradlew test   # when Gradle wrapper is wired in host app
```

Tests cover:

- IC DIY bytes
- D2 255-byte layout and index sequencing
- 4000-byte mono layer → 16 packets (2.13" rotated size)
- Tail packet offset for 300-byte buffer
- D4 mono vs quad delay byte
- Init sequence ordering
- Tri-color 32 packets (2×16) and red inversion on wire
- Pilot DA `F0DA000003F00020`
- 420" processing dimension swap + flags

---

## Exit Criteria

| Criterion | Met |
|-----------|-----|
| DA / DB / D2 / D4 builders | Yes |
| DE / D1 builders (documented assumptions) | Yes |
| 250-byte chunking + tail rule | Yes |
| `ScreenConfig` replaces `epdArray` | Yes |
| `EpdConfigFactory` replaces `setEpdInit` | Yes |
| Mono / tri / quad upload planning | Yes |
| Protocol unit tests | Yes |
| No NFC / bridge | Yes |

**Next:** Checkpoint 3 — `NfcEpaperWriter` + `WriteSession` using `ApduBuilder` + `IsoDep`.
