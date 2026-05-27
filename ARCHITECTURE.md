# Architecture: Expo NFC E-Paper Native Module

This repository migrates a legacy Android NFC driver app into a **headless Expo Native Module**. The legacy code is an embedded hardware driver hidden inside Activities; the target is a reusable SDK for React Native.

**Related docs:**

| Doc | Purpose |
|-----|---------|
| [SOURCES.md](SOURCES.md) | Legacy file map |
| [AGENTS.md](AGENTS.md) | Agent constraints |
| [docs/protocol/GOODDISPLAY.md](docs/protocol/GOODDISPLAY.md) | APDU byte reference |
| [docs/exec-plans/active/native-module-migration.md](docs/exec-plans/active/native-module-migration.md) | Checkpoints & extraction plan |

---

## System Context

```
React Native / Expo TS
    ↓  writeToTag({ imageUri, screenSize, colorMode })
    ↓  react-native-nfc-manager (tag discovery)
Expo Module Bridge (ExpoEpaperModule.kt)
    ↓
Native Services
    ├── EpdConfigFactory   ← data.java
    ├── ImageProcessor     ← activity_imageview (bitmap)
    └── NfcEpaperWriter    ← activity_imageview (NFC)
            ↓
        protocol/ (ApduBuilder, HexCodec)
            ↓
        IsoDep.transceive
            ↓
        GoodDisplay NFC E-Paper Tag
```

This is **not** a normal app migration. Treat the project as an embedded NFC e-paper hardware SDK exposed to React Native.

---

## Critical Rules

### Never

- Create Activities, Fragments, XML layouts, or Compose UI
- Use Intent navigation or `onNewIntent` for business logic
- Run NFC discovery inside the native module
- Keep mutable static globals (`data.epdArray`, `globalBitmap`, etc.)
- Couple image processing to Android UI widgets

### Always

- Expo SDK **56** + modern `ModuleDefinition` / `AsyncFunction` / Events
- Headless services with session-scoped state
- RN owns tag discovery; native owns IsoDep transceive
- Match legacy protocol bytes before refactoring

Docs: https://docs.expo.dev/versions/v56.0.0/

---

## Legacy Source Analysis

### `data/MyApplication.java`

Listed in [SOURCES.md](SOURCES.md) but **not present** in the repository. No migration action unless a copy surfaces. Do not add an `Application` class to the module.

### `data/data.java` — configuration & protocol tables

**Reusable:**

- `setEpdInit(epdColor, epdInch)` → `[driverFlowHex, screenTypeHex]` (indices 0 and 1)
- Per-panel register sequences (DA/DB payloads as concatenated hex)
- Inch/color matrix for 2-, 3-, and 4-color panels

**Mutable globals (forbidden in module):**

| Field | Purpose in legacy |
|-------|-------------------|
| `epdArray[10]` | Width/height/color/IC/inch shared across Activities |
| `globalBitmap` | Cross-screen image stash |
| `x, y, w, h`, `imageView_*`, `scaling`, `epdmodel` | UI crop/scaling (unused by NFC path) |
| `data_DB`, `start`, `RST`, … | Overwritten during each `setEpdInit` call |

**Extraction:** `EpdConfigFactory` returns immutable `ScreenConfig` + hex strings. Refactor `setEpdInit` to use local variables instead of static `data_DB1`, `start1`, etc.

### `data/MainActivity.java` — screen selection UI

**Reusable (data only):**

- `epdArray` encoding: W/H bytes, color mode, IC id, inch code
- Per-button resolution tables (e.g. 2.13" → 128×250, inch `213`)

**UI-coupled (discard):**

- Buttons, RadioGroup, Toast, `startActivity(activity_imageview)`

**Extraction:** `ScreenConfig.fromPreset(screenSize, colorMode)` replaces the button grid. RN passes enums/strings, not Intents.

### `data/activity_imageview.java` — primary driver

**Reusable:**

| Method / block | Target |
|----------------|--------|
| `GetPictureData_SSD`, `GetPictureData_4G` | `ImageProcessor` |
| `rotateBitmap` | `ImageProcessor` |
| `HexString2Btyes`, `HexToString` | `protocol/HexCodec` |
| D2/D4 construction in `writeTag` | `ApduBuilder`, `NfcEpaperWriter` |
| `writeTag` sequence | `NfcEpaperWriter.write` |
| `delay_ms(10)` | `NfcEpaperWriter` (configurable) |

**UI / lifecycle (discard):**

- `ImageView`, drawable placeholders, Toolbar
- `onResume` / `onPause` foreground dispatch, `onNewIntent`
- `runOnUiThread` + `resultTextView`
- `startRefreshMonitoring` / Handler timers (replace with `DE` poll + events)

**Instance state → `WriteSession`:**

- `bitmap0`, `bitmap90`, `image_buffer`
- `epdWidth`, `epdHeight`, `epdColor`, `epdInch`
- `isodep`, `cmd`, `dataNum`, `ScreenIndex_*`

---

## Layer Responsibilities

### TypeScript (application)

- Discover NFC tags (`react-native-nfc-manager`)
- Pick image → `file://` or `content://` URI
- Call `ExpoEpaper.writeToTag({ imageUri, screenSize, colorMode })`
- Subscribe to module events for progress UI

### `ExpoEpaperModule.kt` (bridge only)

- Parse and validate JS arguments
- Build `ScreenConfig` + open `WriteSession`
- Run `NfcEpaperWriter` off the main thread
- Map service callbacks → Expo events
- **No** APDU or bitmap logic here

### `services/ImageProcessor`

- `BitmapFactory` decode from URI / base64
- Rotation rules: 90° for most panels; width/height swap when `inch >= 420`
- Pack SSD (BW/R) and 4G buffers into `ProcessedImage`
- **No** `android.nfc` imports

### `services/EpdConfigFactory`

- Port of `data.setEpdInit`
- Returns driver flow + DA command for `ScreenConfig`
- Pure functions, no static mutable fields

### `services/NfcEpaperWriter`

Orchestrates:

1. `F0DB020000` (IC DIY)
2. DB driver flow (`epd_init[0]`)
3. DA screen type (`epd_init[1]`) — verify `0x90`
4. D2 upload(s) per color mode
5. D4 refresh — verify `0x90`
6. Busy wait — **target:** DE poll; **legacy:** fixed timers

Depends on `ApduTransceiver(IsoDep)` injected per call.

### `protocol/`

| Class | Role |
|-------|------|
| `ApduConstants` | `F0`, `D2`, `D4`, `FA`, screen indices |
| `ApduBuilder` | Assemble D2 255-byte frames, D4 refresh |
| `HexCodec` | Hex string ↔ `byte[]` |

### `models/`

| Class | Role |
|-------|------|
| `ScreenConfig` | width, height, inchCode, colorMode, icFamily, DA/DB hex |
| `ProcessedImage` | `byte[]` layers (bw, optional red), dimensions |
| `ColorMode` | `MONO=2`, `TRI=3`, `QUAD=4` (legacy values) |
| `WriteSession` | Immutable per-flash context + progress listener |

---

## Native Module Layout

Proposed tree (scaffold during migration; see execution plan):

```
android/src/main/java/expo/modules/epaper/
├── ExpoEpaperModule.kt
├── services/
│   ├── ImageProcessor.java
│   ├── NfcEpaperWriter.java
│   ├── EpdConfigFactory.java
│   ├── WriteSession.java
│   └── ApduTransceiver.java
├── models/
│   ├── ScreenConfig.java
│   ├── ProcessedImage.java
│   └── ColorMode.java
└── protocol/
    ├── ApduBuilder.java
    ├── ApduConstants.java
    └── HexCodec.java
```

Kotlin for the Expo bridge; Java acceptable for ported legacy code.

---

## NFC Architecture

| Concern | Owner |
|---------|--------|
| Tag discovery, user prompt | React Native (`react-native-nfc-manager`) |
| `IsoDep.connect`, timeout | Native module (or RN handoff — document in CP4) |
| APDU transceive | `ApduTransceiver` |
| Retry / disconnect policy | `NfcEpaperWriter` (minimal at first) |

Legacy used foreground dispatch + `onNewIntent`. **Do not port.** The module receives an already-discovered tag or technology handle.

IsoDep timeout (legacy): **50000 ms**.

---

## APDU Flow Decomposition

See [GOODDISPLAY.md](docs/protocol/GOODDISPLAY.md) for byte-level detail.

```
writeToTag
├── ImageProcessor.process(uri, config) → ProcessedImage
└── NfcEpaperWriter.write(isoDep, session)
      ├── transceive(IC_DIY)
      ├── transceive(DB from EpdConfigFactory)
      ├── transceive(DA) → check SW 90
      ├── for each layer in ProcessedImage
      │     └── for each D2 chunk → transceive
      ├── transceive(D4) → check SW 90
      └── pollBusy() → DE until idle (target)
```

**D2 frame:** `F0 D2 [screenIndex] [packetIndex] FA [250 payload bytes]`

**Layers:**

- Mono: 1× BW (`screenIndex=0`)
- Tri: BW then R (`screenIndex=1`, red bytes inverted with `0xFF - b`)
- Quad: single 4G buffer
- Special: 3.7" mono, 4.2" mono — legacy dual-pass rules preserved

---

## Image Processing Requirements

**Inputs:** file URI, content URI, base64 (via bridge decode).

**Color modes:**

| Mode | Legacy `epdColor` | Packing |
|------|-------------------|---------|
| Black/white | 2 | `GetPictureData_SSD(0)` |
| Black/white/red | 3 | SSD(0) + SSD(1), R inverted on upload |
| Black/white/red/yellow | 4 | `GetPictureData_4G()` |

**Operations:** decode, resize (if needed later), rotate, threshold, vertical scan pack, dithering (legacy uses fixed RGB thresholds only).

Three-color displays require **two** D2 upload passes.

---

## State Management

**Forbidden:** static `data.epdArray`, `data.globalBitmap`, activity fields shared across lifecycle.

**Required pattern:**

```java
// One session per writeToTag invocation
class WriteSession {
  final ScreenConfig config;
  final ProcessedImage image;
  final ProgressCallback progress;
  // no static fields
}
```

`EpdConfigFactory` must not write static strings during lookup. Convert `setEpdInit` switch bodies to return new objects.

---

## Expo Module API (target)

```ts
await ExpoEpaper.writeToTag({
  imageUri: string,
  screenSize: string,  // e.g. "2.13"
  colorMode: "mono" | "tri" | "quad",
});
```

**Events:**

| Event | When |
|-------|------|
| `onProgress` | D2 upload fraction |
| `onStatus` | phase: init, upload, refresh, poll |
| `onComplete` | successful flash |
| `onError` | failure with code/message |

Use `ModuleDefinition`, `AsyncFunction`, `Events` only (no legacy `ReactPackage`).

---

## Redraw / Busy Handling

**Legacy:** After D4, `Handler` loops with fixed durations (2s / 16s / 20s by color).

**Target (module):** Immediate Return Mode — D4 returns `9000`, then poll **DE** until idle (`00`). Implement in Checkpoint 2/5 after mono parity.

---

## Build Order

Aligned with [native-module-migration.md](docs/exec-plans/active/native-module-migration.md):

1. Image processing extraction  
2. APDU extraction  
3. NFC writer extraction  
4. Expo bridge  
5. Progress events  

Do not implement the Expo bridge before APDU golden tests pass.

---

## First Milestone

**"Flash image from Expo TS onto e-paper tag"**

Pilot: **2.13" monochrome** (`128×250`, `epdInch=213`, `epdColor=2`).

Everything else (tri-color, quad, 3.7"/4.2" specials) follows after mono path is byte-exact and stable.

---

## Engineering Philosophy

Prefer:

- Deterministic, protocol-correct code
- Progressive extraction from legacy Java
- Agent-legible docs and checkpoints
- Isolated, testable services

Avoid:

- Android UI patterns in the module
- Premature abstraction (interfaces for every layer)
- Rewriting algorithms before parity tests
- Framework-heavy NFC lifecycle code

This project is an embedded systems bridge for React Native, not a mobile UI product.
