# Checkpoint 1 Status: Image Processing Extraction

**Status:** Complete (implementation + unit tests)  
**Module:** `modules/gooddisplay-epaper`  
**Package:** `expo.modules.gooddisplayepaper`

---

## Delivered Files

| Path | Role |
|------|------|
| `services/ImageProcessor.java` | Decode, scale, rotate, pack, `process()` |
| `services/ImageProcessingParity.java` | Buffer validation + legacy reference packer for tests |
| `models/ProcessedImage.java` | `blackLayer`, `colorLayer`, `width`, `height` |
| `models/ImageProcessingConfig.java` | Panel width/height, inch, color mode |
| `models/ColorMode.java` | Legacy values 2 / 3 / 4 |
| `protocol/HexCodec.java` | `hexStringToBytes`, `bytesToHexString` |
| `android/src/test/.../ImageProcessorTest.java` | Robolectric parity tests |
| `android/src/test/.../HexCodecTest.java` | Hex round-trip |

**Not modified:** `GooddisplayEpaperModule.kt` (no bridge integration per scope).

---

## Migrated Methods

| Legacy (`activity_imageview.java`) | New API |
|-----------------------------------|---------|
| `rotateBitmap` | `ImageProcessor.rotateBitmap` |
| `GetPictureData_SSD(int mode)` | `ImageProcessor.packSsdLayer(bitmap, mode)` |
| `GetPictureData_4G()` | `ImageProcessor.packQuadLayer(bitmap)` |
| `HexString2Btyes` | `HexCodec.hexStringToBytes` |
| `HexToString` / `NumToString` / `parse` | `HexCodec.bytesToHexString` (+ private helpers) |
| `writeTag` rotation + pack prelude | `ImageProcessor.process` (scale → 90° → pack) |
| `getBitmapFromImageView` | **Not ported** — replaced by URI / base64 / `Bitmap` input |

---

## Remaining Legacy Dependencies

| Item | Notes |
|------|--------|
| `data.setEpdInit` / DA/DB | Checkpoint 2 (`EpdConfigFactory`) |
| `data.epdArray` global | Replaced by `ImageProcessingConfig.fromEpdArray` / caller-supplied config |
| `image_buffer[100000]` | Replaced by exact-size `byte[]` per layer |
| Red plane `0xFF - byte` on NFC upload | Checkpoint 3 — not part of image extraction |
| Placeholder `R.drawable.*` templates | RN supplies real image; we scale to panel size |
| `epdIC` (IC family) | Not used in packing; no port yet |

---

## Parity Gaps

| Gap | Severity | Detail |
|-----|----------|--------|
| No golden dump from physical legacy app | Medium | Tests use inline legacy reference loop + synthetic bitmaps, not captured production buffers |
| `Bitmap.createScaledBitmap` | Low | Legacy used pre-sized drawables; we scale arbitrary decoded images to panel dimensions |
| Always rotate 90° | Info | Matches `writeTag` line 463; comment elsewhere says ≥4.2" may skip rotation, but code does not |
| Height not multiple of 8 / 4 | Low | Legacy assumes panel sizes from vendor; odd sizes may truncate loops (`height0 / 8 - 1`) |
| `processFromUri` requires `Context` | Info | Expo bridge will supply application context in CP4 |

---

## Unsupported / Partial Panel Types

| Case | Support |
|------|---------|
| All `MainActivity` inch codes via `fromEpdArray` | **Config only** — any W/H/inch/color can be passed; no preset table for every button yet |
| `pilot213Mono()` | **Tested** (128×250, mono) |
| 3.7" mono dual-upload placeholder (`0xFF` fill) | **NFC only** — not image processor |
| 4.2" mono dual-pass ordering | **NFC only** — packing still standard SSD |
| `epdInch` 267, 291, etc. | Same algorithms; not individually golden-tested |
| Image crop / screenshot coords (`data.x,y,w,h`) | **Not ported** — out of scope |

---

## Assumptions During Extraction

1. **Panel dimensions** — Caller provides `ImageProcessingConfig` matching legacy `epdWidth`/`epdHeight` after ≥4.2" swap (`fromEpdArray` applies swap when `epdInch >= 420`).
2. **Rotation** — Always 90° before packing, matching `writeTag`.
3. **ProcessedImage.width/height** — Dimensions of the **rotated** bitmap (`width0`/`height0`), not pre-rotation panel size.
4. **Tri-color** — `blackLayer` = SSD mode 0, `colorLayer` = SSD mode 1 (raw; NFC inversion deferred).
5. **Quad** — Single buffer in `blackLayer`; `colorLayer` is null.
6. **Mono** — `colorLayer` is null.
7. **RGB thresholds** — Unchanged from legacy (100 / 200 / 127 rules).

---

## Verification

Run from module Android project (when host app / Gradle wrapper is wired):

```bash
cd modules/gooddisplay-epaper/android
./gradlew test
```

Tests cover:

- Hex round-trip and IC DIY bytes
- SSD pack vs inline legacy reference on 16×8 checkerboard
- Pilot 2.13" mono buffer length 4000
- Tri and quad layer presence
- Base64 decode path

---

## Exit Criteria (Checkpoint 1)

| Criterion | Met |
|-----------|-----|
| `ProcessedImage` exposes black/color layers + dimensions | Yes |
| SSD + 4G packing ported | Yes |
| Bitmap / URI / base64 inputs | Yes |
| No Activity / NFC / bridge | Yes |
| Deterministic unit tests | Yes |

**Next:** Checkpoint 2 — APDU extraction (`ApduBuilder`, `EpdConfigFactory`).
