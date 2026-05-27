# Checkpoint 4 Status: Expo Native Module Bridge

**Status:** Complete (Android)  
**Module:** `modules/gooddisplay-epaper`  
**Depends on:** Checkpoints 1–3

---

## Delivered

| Layer | Files |
|-------|--------|
| Expo bridge | `GooddisplayEpaperModule.kt` |
| RN handoff | `bridge/NfcManagerBridge.kt`, `bridge/NfcHandoffStore.kt` |
| Write orchestration | `bridge/WriteBridge.kt` |
| Panel metadata | `bridge/PanelCatalog.kt` |
| TypeScript | `src/GooddisplayEpaper.types.ts`, `src/GooddisplayEpaperModule.ts` |
| Cancellation (minimal CP3 hook) | `WriteCancellation.java`, `WriteCancelledException.java` |

**Stubs:** iOS + web return unsupported errors (no NFC write).

---

## Bridge Architecture

```
React Native (react-native-nfc-manager)
  requestTechnology(IsoDep)
  registerNfcHandoff({ tagIdHex? })
       ↓
GooddisplayEpaperModule.writeToTag(options)
       ↓
WriteBridge
  → ImageProcessor.process
  → EpdConfigFactory.create
  → WriteSession + NfcEpaperWriter
       ↓
Events: onProgress | onStatus | onTrace | onComplete | onError
```

### NFC responsibility split

| Layer | Responsibility |
|-------|----------------|
| **RN** | Tag discovery, `requestTechnology`, user UX, `cancelTechnologyRequest` |
| **Native module** | IsoDep handoff, image pack, protocol write, events |
| **Not in module** | Foreground dispatch, Activities, `onNewIntent` |

### react-native-nfc-manager handoff

1. `await NfcManager.requestTechnology(NfcTech.IsoDep)`
2. `await GooddisplayEpaper.registerNfcHandoff({ tagIdHex })` — reflection reads Tag from nfc-manager
3. `await GooddisplayEpaper.writeToTag({ imageUri, inchCode, colorMode })`
4. `await NfcManager.cancelTechnologyRequest()`

`NfcManagerBridge` tries known class/field names; optional `tagIdHex` validates id bytes.

---

## Exposed API

### Functions

| API | Description |
|-----|-------------|
| `writeToTag(options)` | Full flash pipeline |
| `registerNfcHandoff(options?)` | Capture IsoDep from nfc-manager |
| `cancelWrite()` | Cooperative cancel + IsoDep close |
| `getSupportedPanels()` | Inch codes + supported color modes |
| `getProtocolVersion()` | `"1.0.0"` |

### Events

| Event | Payload |
|-------|---------|
| `onProgress` | `WriteProgressEvent` — upload %, packets, DE count, SW |
| `onStatus` | Phase changes (`init`, `uploading`, …) |
| `onTrace` | Per-APDU trace row (when `emitApduTrace`) |
| `onComplete` | Full `WriteResult` + embedded trace |
| `onError` | `{ code, message, phase?, sw1?, sw2? }` |

### TypeScript

Import from module:

```typescript
import GooddisplayEpaper from './modules/gooddisplay-epaper/src/GooddisplayEpaperModule';
import type { WriteToTagOptions, WriteResult } from './modules/gooddisplay-epaper/src/GooddisplayEpaper.types';

GooddisplayEpaper.addListener('onProgress', (e) => { ... });
await GooddisplayEpaper.writeToTag({ imageUri, inchCode: 213, colorMode: 'mono' });
```

---

## Observability

Preserved from Checkpoint 3 via bridge:

- Per-APDU `onTrace` during write
- Full trace array on `WriteResult` / `onComplete`
- Timing: connect, init, upload, refresh, busy, total ms
- `transceiveRetryCount`, `dePollCount`, SW bytes

Logcat tag: `GooddisplayEpaper` (from `ApduTransceiver`).

---

## Cancellation Semantics

| Action | Effect |
|--------|--------|
| `cancelWrite()` | Sets `WriteCancellation`, closes IsoDep, in-flight transceive fails |
| Writer | Checks cancellation at phase boundaries + each D2 + DE poll |
| Result | `WriteCancelledException` → `onError` code `CANCELLED` |
| Idempotent | Returns `false` if no active write |

Cancel is **cooperative** — not instantaneous mid-transceive.

---

## Expo Integration Assumptions

- Expo SDK **56** Modules API (`ModuleDefinition`, `AsyncFunction`, `Events`)
- `appContext.reactContext` available for `ContentResolver` image decode
- `react-native-nfc-manager` linked in host app (reflection at runtime)
- Android NFC permission in host app manifest
- Module autolinked via `expo-module.config.json`

---

## Remaining RN Integration Work (Checkpoint 5 / app)

- [ ] Add `react-native-nfc-manager` to host app
- [ ] NFC permission + feature declarations
- [ ] Example flow: pick image → hold tag → handoff → write
- [ ] Wire `onProgress` / `onComplete` to UI (no hooks in module — app responsibility)
- [ ] Hardware validation of DE poll frame on physical tag
- [ ] Optional: dedicated handoff if reflection breaks on nfc-manager version bump

---

## Unsupported Platform Cases

| Platform | Behavior |
|----------|----------|
| **Web** | Throws "not supported on web" |
| **iOS** | Throws unsupported (no CoreNFC e-paper path yet) |
| **Android without nfc-manager** | `registerNfcHandoff` fails with handoff error |
| **Non-IsoDep tags** | Handoff error at IsoDep.get |

---

## Event Semantics Notes

- `onComplete` fires with same payload as `writeToTag` return value
- `onError` fires before promise rejection on write failures
- `onStatus` fires on phase transitions; `onProgress` on upload/DE updates
- Phase strings are lowercase enum names (`polling_busy`, etc.)

---

## CP1–CP3 Changes (bridge-only)

| Change | Reason |
|--------|--------|
| `WriteCancellation` + checks in `NfcEpaperWriter` | `cancelWrite()` |
| `WriteSession.create(..., WriteCancellation)` | Pass token to writer |

Deterministic image/APDU behavior unchanged.

---

## Exit Criteria

| Criterion | Met |
|-----------|-----|
| Expo ModuleDefinition + AsyncFunction + Events | Yes (Android) |
| writeToTag / cancel / panels / version | Yes |
| Integrates CP1–3 without rewrite | Yes |
| nfc-manager handoff adapter | Yes |
| TypeScript types | Yes |
| No RN hooks / UI / Activities | Yes |

**Next:** Checkpoint 5 — app-level RN integration, progress UX, hardware soak tests.
