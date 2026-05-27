# Execution Plan: Native Module Migration

**Status:** Active  
**Goal:** Flash an image from Expo TypeScript onto a GoodDisplay NFC E-Paper tag.  
**Constraint:** Headless Expo Native Module — no Activities, no UI coupling.

---

## Legacy Source Inventory

| File | In repo | Role |
|------|---------|------|
| `data/data.java` | Yes | Protocol tables, `setEpdInit`, mutable globals |
| `data/activity_imageview.java` | Yes | Bitmap pack, NFC write, D2/D4 |
| `data/MainActivity.java` | Yes | Screen picker UI → `epdArray` |
| `data/MyApplication.java` | **No** | Referenced in SOURCES.md only |

`MyApplication.java` is absent from the tree. Treat as non-blocking: no reusable driver logic documented for it. Do not introduce an `Application` subclass in the module.

---

## Analysis Matrix

### Reusable logic (extract verbatim first)

| Logic | Source | Target |
|-------|--------|--------|
| `setEpdInit(epdColor, epdInch)` | `data.java` | `EpdConfigFactory` + `ScreenConfig` |
| DA/DB hex strings | `data.java` | `protocol/` constants or config records |
| `HexString2Btyes` / hex helpers | `activity_imageview.java` | `protocol/HexCodec.java` |
| `GetPictureData_SSD` | `activity_imageview.java` | `ImageProcessor.packSsdLayer` |
| `GetPictureData_4G` | `activity_imageview.java` | `ImageProcessor.packQuadLayer` |
| `rotateBitmap` | `activity_imageview.java` | `ImageProcessor.rotate` |
| D2 packet builder + upload loops | `activity_imageview.java` | `ApduBuilder` + `NfcEpaperWriter` |
| D4 refresh bytes | `activity_imageview.java` | `ApduBuilder.buildRefresh` |
| `writeTag` sequencing | `activity_imageview.java` | `NfcEpaperWriter.write` |
| Inch → width/height tables | `MainActivity.java` | `ScreenConfig` presets (data, not UI) |

### UI-coupled logic (do not port)

| Logic | Source | Replacement |
|-------|--------|-------------|
| `onCreate`, layouts, Toolbar | `activity_imageview` | None |
| `ImageView` + `R.drawable.*` placeholders | `activity_imageview` | RN provides image URI |
| `getBitmapFromImageView` | `activity_imageview` | `BitmapFactory` + URI/base64 |
| `onResume` / `onPause` NFC foreground dispatch | `activity_imageview` | `react-native-nfc-manager` |
| `onNewIntent` tag detection | `activity_imageview` | RN passes tag handle to module |
| `runOnUiThread` + `resultTextView` | `activity_imageview` | Expo `sendEvent` |
| `startRefreshMonitoring` timer UI | `activity_imageview` | `DE` poll + `onProgress` events |
| Screen size buttons + Intents | `MainActivity` | `writeToTag({ screenSize, colorMode })` |
| `onKeyDown` back navigation | `activity_imageview` | RN navigation |

### NFC / APDU flows

```
IC DIY (F0DB020000)
  → DB (epd_init[0])
  → DA (epd_init[1])  [expect 0x90]
  → D2 × packets × layers
  → D4 refresh        [expect 0x90]
  → wait idle         [legacy: timer | target: DE]
```

Extract as:

- `ApduTransceiver` — thin `IsoDep.transceive` wrapper (timeout, errors)
- `ApduBuilder` — D2/D4/hex frame builders
- `NfcEpaperWriter` — orchestration only

### Bitmap processing flows

```
Input (URI/base64)
  → decode Bitmap
  → optional rotate 90° (panel rules)
  → pack layer(s):
        mono: 1× GetPictureData_SSD(0)
        tri:  GetPictureData_SSD(0) + GetPictureData_SSD(1)
        quad: GetPictureData_4G()
  → byte[] in ProcessedImage
```

### Mutable global state (eliminate)

| Legacy | Location | Migration |
|--------|----------|-----------|
| `globalBitmap`, `x,y,w,h`, `imageView_*`, `scaling`, `epdmodel` | `data.java` | Remove; pass via `WriteSession` / args |
| `epdArray` static | `data.java` | `ScreenConfig` instance |
| `data_DB`, `start`, … mutable strings during `setEpdInit` | `data.java` | Local vars inside factory method |
| `bitmap0`, `bitmap90`, `image_buffer[100000]` | `activity_imageview` | `ProcessedImage` + sized buffers |
| `isodep`, `cmd`, `dataNum` instance fields | `activity_imageview` | `WriteSession` (per call) |
| `data.setEpdArray` from MainActivity | cross-activity | RN explicit parameters |

### Protocol command builders

| Builder | Inputs | Output |
|---------|--------|--------|
| `icDiy()` | — | `F0DB020000` |
| `driverFlow(ScreenConfig)` | inch, color | `byte[]` from `epd_init[0]` |
| `screenType(ScreenConfig)` | inch, color | `byte[]` from `epd_init[1]` |
| `uploadChunk(layer, index, payload)` | index, 250 B | 255 B APDU |
| `refresh(ColorMode)` | 2/3 vs 4 | 5 B D4 |

Keep hex strings in `EpdConfigFactory` until byte-exact parity is proven.

---

## Extraction Boundaries

```
┌──────────────────────────────────────────────────────────┐
│ TypeScript (app)                                         │
│  - NFC tag discovery (react-native-nfc-manager)          │
│  - image picker URI                                      │
│  - ExpoEpaper.writeToTag({ ... })                        │
└────────────────────────┬─────────────────────────────────┘
                         │ AsyncFunction + Events
┌────────────────────────▼─────────────────────────────────┐
│ ExpoEpaperModule.kt                                      │
│  - validate args, map to ScreenConfig                      │
│  - invoke WriteSession on worker thread                    │
│  - emit onProgress / onStatus / onComplete / onError     │
└────────────────────────┬─────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        ▼                ▼                ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────────┐
│ImageProcessor│ │NfcEpaperWriter│ │EpdConfigFactory  │
│(no NFC)      │ │(orchestration)│ │(no IsoDep)       │
└──────────────┘ └───────┬──────┘ └──────────────────┘
                         │
                         ▼
                 ┌──────────────┐
                 │ ApduBuilder  │
                 │ HexCodec     │
                 └───────┬──────┘
                         ▼
                 ┌──────────────┐
                 │ IsoDep       │  ← passed in, not discovered here
                 └──────────────┘
```

**Rules:**

1. `ExpoEpaperModule` must not call `IsoDep.get(Tag)` from Activity context.
2. `ImageProcessor` must not import NFC classes.
3. `EpdConfigFactory` must be pure (no static mutable fields).
4. One `WriteSession` per `writeToTag` call; discard after completion.

---

## Target Package Structure

```
android/src/main/java/expo/modules/epaper/
├── ExpoEpaperModule.kt          # Expo ModuleDefinition only
├── services/
│   ├── ImageProcessor.java      # decode, rotate, pack
│   ├── NfcEpaperWriter.java     # write sequence
│   ├── EpdConfigFactory.java    # setEpdInit port
│   ├── WriteSession.java        # per-flash immutable state
│   └── ApduTransceiver.java     # IsoDep wrapper (optional split)
├── models/
│   ├── ScreenConfig.java        # width, height, inch, color, IC
│   ├── ProcessedImage.java      # layer buffers + metadata
│   └── ColorMode.java           # MONO, TRI, QUAD enum
└── protocol/
    ├── ApduBuilder.java         # D2, D4 frames
    ├── ApduConstants.java       # F0, D2, FA, screen indices
    └── HexCodec.java            # hex ↔ bytes
```

Scaffold only in early checkpoints — no full `writeTag` until checkpoints 1–3 pass.

---

## Execution Checkpoints

### Checkpoint 1 — Image processing extraction

**Deliverables:**

- [ ] `ScreenConfig` presets for one pilot panel (recommend `2.13"`, `epdColor=2`)
- [ ] `ImageProcessor`: `rotate`, `packSsdLayer(mode)`, buffer sizing
- [ ] `ProcessedImage` with `byte[] bw` (+ `red` when needed)
- [ ] Unit-level test: golden bitmap → hex dump matches legacy `image_buffer` prefix

**Exit criteria:** Packed buffer length equals `width*height/8` for pilot config.

**Legacy refs:** `GetPictureData_SSD`, `rotateBitmap`, lines 461–464 (rotation rule).

---

### Checkpoint 2 — APDU extraction

**Deliverables:**

- [ ] `HexCodec` ported from `HexString2Btyes`
- [ ] `ApduBuilder.buildD2Chunk`, `buildRefresh`
- [ ] `EpdConfigFactory` returns `{ driverFlowHex, screenTypeHex }` for pilot panel
- [ ] `ApduTransceiver` mock tests: packet sizes = 255, tail packet rule

**Exit criteria:** First D2 packet bytes match legacy manual construction (`F0 D2 00 00 FA ...`).

**Legacy refs:** `writeTag` lines 432–442, 643–667, 823–843.

**Improvement (after parity):** Implement `DE` poll; document frame in `GOODDISPLAY.md`.

---

### Checkpoint 3 — NFC writer extraction

**Deliverables:**

- [ ] `WriteSession` holds `ScreenConfig`, `ProcessedImage`, progress callback
- [ ] `NfcEpaperWriter.write(IsoDep, WriteSession)` — full sequence for pilot mono panel
- [ ] No `Activity`, no `runOnUiThread`
- [ ] Instrumented test on real tag OR recorded transceive fixture

**Exit criteria:** Pilot image displays on tag; `0x90` after DA and D4.

**Legacy refs:** entire `writeTag` method.

---

### Checkpoint 4 — Expo bridge implementation

**Deliverables:**

- [ ] Expo module package (`expo-modules` layout)
- [ ] `ExpoEpaperModule.kt` with `AsyncFunction("writeToTag")`
- [ ] Accept: `imageUri`, `screenSize`, `colorMode` (map → `ScreenConfig`)
- [ ] IsoDep from RN (document handoff contract)
- [ ] Threading: blocking NFC off main thread

**Exit criteria:** `await ExpoEpaper.writeToTag(...)` completes from example app.

---

### Checkpoint 5 — Progress event system

**Deliverables:**

- [ ] Events: `onProgress`, `onStatus`, `onComplete`, `onError`
- [ ] Progress during D2 loops (legacy `dataNum` formula)
- [ ] Status: `connecting`, `uploading`, `refreshing`, `polling`
- [ ] Replace timer-based refresh with `DE` poll when validated

**Exit criteria:** RN UI shows upload % and completion without polling native state.

---

## Build Order (strict)

1. Checkpoint 1 — Image processing  
2. Checkpoint 2 — APDU  
3. Checkpoint 3 — NFC writer  
4. Checkpoint 4 — Expo bridge  
5. Checkpoint 5 — Progress events  

Do not start Expo module scaffolding before `ApduBuilder` golden tests pass.

---

## Pilot Scope (first flash)

| Parameter | Value |
|-----------|--------|
| Panel | 2.13" mono (`epdInch=213`, `epdColor=2`) |
| Resolution | 128×250 |
| Layers | 1× BW |
| Rotation | 90° before pack |

Expand to tri-color (`epdColor=3`) only after mono path is stable.

---

## Risk Register

| Risk | Mitigation |
|------|------------|
| DB blob too large for one transceive | Log length; compare with tag max APDU; chunk only if proven necessary |
| Legacy tail packet off-by-one | Copy loop structure exactly; fixture test |
| No `DE` in legacy | Add after mono works; keep timer fallback behind flag |
| `MyApplication` missing | Skip |
| RN ↔ IsoDep handoff | Document in ARCHITECTURE.md; spike with `nfc-manager` early in CP4 |

---

## Definition of Done (milestone)

- [ ] Expo TS calls `writeToTag` with file URI
- [ ] Mono pilot panel displays image
- [ ] No Activities / static globals in module code path
- [ ] Protocol doc and this plan updated with any byte-level discoveries

---

## Agent Notes

- Prefer copying legacy code into `services/` over rewriting algorithms.
- When legacy and ARCHITECTURE.md disagree (DE polling), **match legacy first**, then add DE behind parity tests.
- File line anchors: `data/activity_imageview.java` `writeTag` ≈ 412–898; `data.java` `setEpdInit` ≈ 183–835.
