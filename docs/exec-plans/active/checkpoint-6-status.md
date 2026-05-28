# Checkpoint 6 Plan: Runtime Integration Validation

**Status:** Planned (post-Checkpoint 5)  
**Objective:** Validate end-to-end runtime correctness across existing vertical slices without introducing major new architecture.

---

## Scope

Checkpoint 6 validates the integration of already-built pieces:

- Expo bridge API + events
- Native NFC ownership session flow
- Image processing (Checkpoint 1)
- APDU construction (Checkpoint 2)
- Write runtime + DE polling (Checkpoint 3)
- Cancellation and session lifecycle (Checkpoints 3–5)

Non-goals:

- No broad refactor
- No new ownership model
- No new protocol abstraction unless a severe runtime defect is proven

---

## End-to-end runtime verification checklist

- [ ] `writeToTag` emits `waiting_for_tag` before tap
- [ ] Tag tap acquires IsoDep and emits `tag_acquired`
- [ ] Write phases progress: `starting → connecting → init → uploading → refreshing → polling_busy → complete`
- [ ] `writeToTag` resolved payload equals `onComplete` payload contract
- [ ] Event ordering remains deterministic under normal flow
- [ ] No stale foreground dispatch after success/failure/cancel

---

## Protocol validation checklist

- [ ] DA frame matches selected panel/color mode from `EpdConfigFactory`
- [ ] DB frame emitted for driver flow init
- [ ] D2 packetization enforces 250-byte payload rule
- [ ] D2 packet count equals expected bytes for panel mode
- [ ] D4 refresh command emitted after all uploads
- [ ] DE polling loop stops only on idle response
- [ ] SW1/SW2 surfaced in traces and error payloads

---

## Hardware validation strategy

Test matrix should include at minimum:

- 1 mono panel (baseline, e.g., 2.13)
- 1 tri-color panel (dual-buffer upload path)
- 1 device model with stock Android NFC stack
- 1 additional OEM model if available

For each matrix row:

1. Flash known test image
2. Verify full-screen visual correctness
3. Capture APDU trace for archival
4. Re-run same image twice to ensure determinism

---

## APDU trace verification

Per run, verify:

- [ ] Trace starts with connect/init APDUs
- [ ] Upload trace contains expected D2 count
- [ ] Final trace includes D4 then DE polling sequence
- [ ] `transceiveRetryCount` is zero on healthy baseline path
- [ ] If retries occur, trace `attempt` increments are coherent

Persist sample traces by panel profile for regression comparisons.

---

## Lifecycle edge-case matrix

| Scenario | Expected behavior |
|---|---|
| Cancel while waiting for tap | Promise rejects with cancelled code; dispatch disabled |
| Cancel during upload | In-flight write aborts cooperatively; IsoDep closes |
| Tag removed mid-upload | Write fails with NFC/protocol error and phase context |
| App backgrounded while waiting | Wait cancels; no leaked session |
| App backgrounded during write | Session fails safely; resources cleaned |
| Immediate second `writeToTag` while active session exists | Rejected/serialized deterministically |
| Reconnect after failure | New write succeeds with fresh session state |

---

## Cancellation tests

- [ ] Call `cancelWrite` before tag tap
- [ ] Call `cancelWrite` after `tag_acquired` but before first D2
- [ ] Call `cancelWrite` during D2 burst
- [ ] Call `cancelWrite` during DE polling
- [ ] Verify error code parity (`CANCELLED`) and no stuck state for next run

---

## Reconnect tests

- [ ] Failure run followed by immediate successful retry
- [ ] Success run followed by second success without app restart
- [ ] Ensure no stale tag object/session crosses attempts

---

## Tag removal tests

- [ ] Remove tag during upload (expect transceive failure surfaced)
- [ ] Re-present same tag and verify fresh write works
- [ ] Re-present different compatible tag and verify no ID coupling assumptions

---

## Backgrounding tests

- [ ] Home press while waiting
- [ ] Home press during active write
- [ ] Return to app and run write again
- [ ] Confirm no duplicate progress/trace listeners after resume

---

## Deliverables

1. Completed checklist with pass/fail per item
2. At least one successful trace artifact per supported panel mode
3. Failure artifact examples for cancel, removal, and background scenarios
4. Short defect list with severity and owner if issues found

---

## Success criteria

Checkpoint 6 is complete when:

- Core happy-path writes are stable on target hardware
- Edge-case behavior is deterministic and resource-safe
- Event contracts and APDU traces match CP1–CP5 design
- No major new abstractions were needed to achieve runtime correctness
