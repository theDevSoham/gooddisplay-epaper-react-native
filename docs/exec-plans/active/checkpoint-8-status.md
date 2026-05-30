# Checkpoint 8 Status: Refresh Completion Parity

**Status:** Implemented  
**Module:** `modules/gooddisplay-epaper`  
**Depends on:** Checkpoints 1–7 (write path validated on hardware)

---

## Summary

Runtime reported **FAILED** after a **successful physical refresh** because Expo polled **DE** (`F0DE010000`) after D4; the tag returns **6A86** (instruction not supported). Legacy Android never sends DE and instead uses a **fixed timer** after D4 success.

**Fix:** Replace `pollUntilIdle()` with `waitForLegacyRefresh()` — same 100 ms tick loop as `startRefreshMonitoring`, no DE transceive.

No architecture, protocol, or NFC ownership changes.

---

## Legacy Completion Strategy

### Path in `activity_imageview.writeTag`

After image upload and D4 refresh:

1. Build `refreshcmd` (`F0 D4 … 80 00`); quad uses byte `[2]=0x85`, mono/tri use `0x05`.
2. `response = isodep.transceive(refreshcmd)`.
3. If `response[0] == 0x90`:
   - UI: “E-paper is updating, please wait！”
   - **`startRefreshMonitoring()`** — no further APDUs until complete.

### `startRefreshMonitoring` (lines 904–962)

| Mechanism | Behavior |
|-----------|----------|
| Tick interval | `handler.postDelayed(this, 100)` → **100 ms** |
| NFC check | Each tick: `if (!isodep.isConnected())` → error UI, stop |
| Completion | Fixed **tick count** by color mode (not DE, not D4 response body) |
| DE APDU | **Never sent** (grep `0xDE` / `F0DE` in `data/` → no matches) |

### Completion thresholds (proven from code)

| `epdColor` | Condition | Wall time |
|------------|-----------|-----------|
| 2 (mono) | `count == 20` | **2 s** |
| 3 (tri) | `count == 160` | **16 s** |
| 4 (quad) | `count == 200` | **20 s** |

### Other timing in `writeTag`

`delay_ms(10)` appears **before** upload (init pacing), not as post-D4 completion.

`stopRefreshMonitoring()` clears the Handler callback; called on error, back, and `onDestroy`.

---

## Current Completion Strategy (before fix)

### Expo path

```
NfcEpaperWriter.write()
  → executeRefresh()     // D4, validate SW1=90
  → pollUntilIdle()      // loop: F0DE010000 until idle or timeout
```

### `pollUntilIdle` behavior

- APDU: `ApduBuilder.buildBusyPoll()` → `F0DE010000` (`ApduConstants.BUSY_POLL_HEX`)
- Interval: `WriteOptions.busyPollIntervalMs` (default 100 ms)
- Cap: `busyTimeoutMs` / `defaultBusyTimeoutMs` (quad default **25 s**)
- Success: `ApduResponse.isBusyIdle(response)` (SW1=90)
- Failure: timeout after N polls → `NfcWriteException` → phase **FAILED**

### Migration origin

DE polling was introduced in **Checkpoint 2/3** as a documented “target” flow (`GOODDISPLAY.md`, `ApduConstants` comment: “not in legacy app”). It was **not** ported from `activity_imageview`.

---

## Root Cause

| Factor | Finding |
|--------|---------|
| **Invalid DE polling for this panel** | **Yes.** Quad tag accepts D4 (`9000`) then rejects every DE with `6A86`. Display finishes refresh while polls continue until 25 s timeout. |
| **Incorrect DE parameters** | Secondary. Instruction unsupported on tested hardware; tuning DE would not restore legacy parity. |
| **Timeout too short** | **No.** Physical refresh completes **before** 25 s; failure is from **wrong mechanism**, not insufficient wait. |
| **Panel-specific** | DE unsupported on tested 2.13" quad; legacy never relied on DE for any color mode. |
| **Combination** | **Invalid DE polling** (primary) + **wrong completion model** (timer vs poll). |

### Observed trace (user runtime)

```
REFRESH: F0D4858000 → 9000
BUSY_POLL: F0DE010000 → 6A86  (×222)
→ DE busy poll timeout after 222 polls (25000ms)
→ FAILED (display already updated)
```

### +10 s safety margin

**Not applied.** Investigation shows legacy **20 s** quad wait matches Android `count == 200`. The false failure occurs at ~25 s while polling DE, not because 20 s is too short. If RN/bridge overhead ever requires a longer hold, override via `WriteToTagOptions.busyTimeoutMs` → maps to `legacyRefreshWaitMs` (documented extension point).

---

## Minimal Fix (implemented)

### Files

| File | Change |
|------|--------|
| `services/NfcEpaperWriter.java` | `pollUntilIdle()` → `waitForLegacyRefresh()` — sleep ticks only, no DE |
| `services/WriteOptions.java` | `legacyRefreshWaitMs(colorMode)` → 2000 / 16000 / 20000; builder override |
| `bridge/WriteBridge.kt` | `legacyRefreshWaitForColorMode()` instead of DE timeout mapping |
| `protocol/ApduConstants.java` | Comment: DE not used in legacy path |
| `services/NfcEpaperWriterTest.java` | Assert no DE APDU; disconnect-during-wait test |

### `waitForLegacyRefresh` behavior

- After D4 success, wait `WriteOptions.getLegacyRefreshWaitMs(colorMode)`.
- Loop every 100 ms (configurable): cancellation check + `isConnected()` only.
- **No** `transceive` during wait.
- `dePollCount` on progress/result now means **refresh wait ticks** (metric name unchanged for API stability).

### Expected trace (quad)

```
… D2 × N
D4 F0D4858000 → 9000
(wait 20s — no APDU)
→ COMPLETE
```

---

## Verification

### Unit tests

- `write_executesInitUploadRefreshWithoutDePoll` — 20 APDUs, zero DE bytes on wire.
- `write_refreshWait_nfcDisconnectFails` — connection loss during wait still fails correctly.

### Hardware checklist

- [ ] Quad 213: D4 `9000`, no DE in trace, status **complete** ~20 s after D4.
- [ ] Mono/tri: wait 2 s / 16 s respectively.
- [ ] No `6A86` after D4 on success path.

---

## Success Criteria

| Criterion | Status |
|-----------|--------|
| Physical refresh succeeds | Already verified pre-CP8 |
| Runtime reports COMPLETE not FAILED | **Expected after fix** |
| No false timeout after successful refresh | **Addressed** (removed DE) |
| Completion matches legacy timer | **Yes** (2 / 16 / 20 s) |
| Timeout increase justified by evidence | **N/A** — mechanism fix, not longer timeout |
