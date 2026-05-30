# Checkpoint 7 Status: Protocol Parity & Image Fidelity

**Status:** Implemented  
**Module:** `modules/gooddisplay-epaper`  
**Depends on:** Checkpoints 1–6 (runtime integration validated)

---

## Summary

Three runtime parity defects were investigated against legacy `activity_imageview.java`. Two code fixes were applied:

| Defect | Root cause | Fix |
|--------|------------|-----|
| **1 — False timeout after successful refresh** | Legacy never sends DE; CP3 assumed `F0DE010000` | Replace DE poll with legacy timer wait after D4 |
| **2 — Image rotation mismatch (quad)** | Quad panels use 2× width assets before 90° rotate | Double processing width for quad color mode |
| **3 — Image sizing mismatch (quad)** | Same as #2 — Expo scaled to 128×250 instead of 256×250 | `ScreenConfig.getProcessingWidth()` quad rule |

No architecture or NFC ownership changes.

---

## Defect 1 — Busy Poll / Completion Detection

### Android behavior

Legacy `writeTag` after successful D4 (`response[0] == 0x90`):

1. Calls `startRefreshMonitoring()`
2. Runs a Handler loop every **100 ms**
3. Completes when tick count reaches color-specific threshold:
   - Mono (`epdColor == 2`): **20 ticks → 2 s**
   - Tri (`epdColor == 3`): **160 ticks → 16 s**
   - Quad (`epdColor == 4`): **200 ticks → 20 s**
4. **Never sends DE APDU**

Reference: `data/activity_imageview.java` lines 847–954.

### Expo behavior (before fix)

`NfcEpaperWriter.pollUntilIdle()` sent `F0DE010000` repeatedly after D4 until idle or timeout.

### Observed runtime trace (quad panel)

```
D4: F0D4858000 → 9000   (refresh accepted, display updates)
DE: F0DE010000 → 6A86   (repeated — instruction not supported)
→ timeout / FAILED despite successful physical refresh
```

`6A86` = wrong parameters / unsupported instruction for this tag family on DE.

### Root cause

DE polling was an **architecture assumption** from CP2/CP3 (documented in `GOODDISPLAY.md` as “target, not legacy”). It is **not** part of the reference Android completion strategy and is **not supported** on tested quad hardware.

### Fix

`NfcEpaperWriter.waitForLegacyRefresh()`:

- After D4 success, wait fixed duration matching legacy timer (`WriteOptions.legacyRefreshWaitMs`)
- Poll every 100 ms for cancellation + NFC connection only
- **No DE transceive**

### Before / after APDU trace

**Before (failed path):**

```
… D2 × N
D4 F0D4858000 → 9000
DE F0DE010000 → 6A86
DE F0DE010000 → 6A86
… (until timeout)
→ FAILED
```

**After (expected path):**

```
… D2 × N
D4 F0D4858000 → 9000
(wait 20s for quad — no APDU)
→ COMPLETE
```

---

## Defect 2 — Image Rotation Parity

### Android behavior

```
bitmap0 (panel-sized drawable, e.g. f213 at 256×250 for quad)
  → rotateBitmap(bitmap0, 90)
  → bitmap90
  → GetPictureData_4G() / GetPictureData_SSD()
  → width0 = bitmap90.getWidth(), height0 = bitmap90.getHeight()
```

Rotation is always **+90°** via `Matrix.postRotate(90)` in `writeTag` (line 463), regardless of color mode.

### Expo behavior (before fix)

```
decode image
  → scale to ScreenConfig processing size
  → rotateBitmap(+90°)
  → pack
```

Rotation implementation already matched legacy (`ImageProcessor.rotateBitmap` is a direct port).

### Parity mismatch

For **2.13" quad**, legacy quad drawable / driver path uses **256×250** source bitmap (see Defect 3). Expo scaled to **128×250**, then rotated to **250×128** instead of **250×256**.

Wrong pre-rotation dimensions produce different packed scan order → visible orientation/scaling errors on display.

### Fix

Correct processing dimensions for quad (Defect 3). Rotation code unchanged.

---

## Defect 3 — Image Resize Parity

### Android behavior

`MainActivity` sets `epdArray` width/height (213 → **128×250**).

For **quad color**, `data.setEpdInit` driver comments document doubled horizontal resolution:

```
2.13-inch 4-color 128x250 → 256x250 resolution scaled up by double
start1 = "A0060020010000FA"
```

Legacy `f213` drawable is authored at driver resolution (**256×250**), not raw epdArray width.

Processing pipeline:

```
epdArray: 128 × 250
quad asset: 256 × 250
rotate 90°: 250 × 256
buffer: width0 × height0 / 4 = 16000 bytes
D2 packets: 64
```

### Expo behavior (before fix)

```
ScreenConfig processing: 128 × 250  (epdArray only)
rotate 90°: 250 × 128
buffer: 8000 bytes
D2 packets: 32
```

Upload “succeeded” but carried **half** the horizontal pixel data → wrong visual scale/orientation.

### Fix

`ScreenConfig.getProcessingWidth()`:

- After ≥4.2" swap logic, if `colorMode == QUAD`: multiply width by **2** (legacy “scaled up by double”)
- Panel-specific overrides for 291 (366) and 420 (400 native width)

213 quad result: **256×250** → rotate → **250×256** → **16000-byte** buffer.

### Verification

| Panel | Mode | Processing size | Rotated | Buffer (quad) | D2 packets |
|-------|------|-----------------|---------|---------------|------------|
| 213 | mono | 128×250 | 250×128 | 4000 (SSD) | 16 |
| 213 | quad | **256×250** | **250×256** | **16000** | **64** |

---

## Files changed

| File | Change |
|------|--------|
| `services/NfcEpaperWriter.java` | DE poll → legacy refresh timer wait |
| `services/WriteOptions.java` | `legacyRefreshWaitMs()` (2s / 16s / 20s) |
| `models/ScreenConfig.java` | Quad processing width doubling |
| `services/ImageProcessor.java` | `scaleToPanel()` helper, bitmap recycle |
| `protocol/ApduConstants.java` | Document DE unused for legacy parity |
| `android/src/test/.../NfcEpaperWriterTest.java` | No DE in trace assertions |
| `android/src/test/.../ImageProcessorTest.java` | Quad 213 buffer = 16000 |
| `android/src/test/.../EpdConfigFactoryQuadTest.java` | Processing dims 256×250 |

---

## Remaining risks

1. **Timer vs hardware variance**: Legacy uses fixed timers; slow panels may still be updating when COMPLETE fires (same as Android app).
2. **HR quad panels (291, 267)**: Width override for 291 uses comment value 366; needs hardware golden check.
3. **4.2" quad (420)**: Special width/height rules may need refinement from drawable assets.
4. **Dynamic images vs preset drawables**: Expo scales arbitrary URIs to panel size; Android reference app uses pre-authored drawables — aspect ratio handling may still differ for non-standard inputs.
5. **DE command**: Reserved in `ApduBuilder` for future tags that support it; not used in current parity path.

---

## Testing strategy

### Unit tests (Robolectric)

- [x] Quad 213 processing dimensions 256×250
- [x] Quad 213 packed buffer 16000 bytes
- [x] Write completes without DE commands in trace
- [x] Legacy refresh wait uses configurable `busyTimeoutMs` override in tests

### Hardware validation (recommended)

1. Flash same image on 2.13" quad via legacy Android app and Expo module
2. Confirm `writeToTag` ends **COMPLETE** (not FAILED after D4)
3. Confirm APDU trace has **no DE** after D4
4. Confirm D2 packet count = **64** for quad 213
5. Compare visual output orientation and fill against Android

---

## Exit criteria

| Criterion | Status |
|-----------|--------|
| Refresh ends COMPLETE (no false DE timeout) | ✅ Code |
| No DE APDU after D4 in writer | ✅ |
| Quad 213 uses 256×250 processing size | ✅ |
| Rotation remains +90° legacy port | ✅ (unchanged) |
| No architecture changes | ✅ |

Hardware re-validation required to mark exit criteria fully closed on device.
