# GoodDisplay / Fudan ESL NFC Protocol

Reverse-engineered from legacy sources: `data/data.java`, `data/activity_imageview.java`.

This document is the canonical protocol reference for the Expo Native Module migration. Implementation must match legacy byte sequences before optimizing.

---

## Transport

| Property | Value |
|----------|--------|
| NFC technology | `IsoDep` |
| Command prefix | `0xF0` (vendor wrapper) |
| Max payload per D2 packet | **250 bytes** |
| D2 APDU length | **255 bytes** (`5` header + `250` data) |
| Hex config blobs | Concatenated register sequences; converted via `HexString2Btyes` |

IsoDep timeout in legacy: **50000 ms**.

---

## Command Summary

| Cmd | Name | Legacy usage | Notes |
|-----|------|--------------|-------|
| `DA` | Set screen type | `data.setEpdInit()[1]` | `F0DA...` — resolution + color profile |
| `DB` | Set driver flow | `data.setEpdInit()[0]` | Long hex blob; may exceed 255 B as one transceive in legacy |
| `D2` | Upload image | `writeTag` loops | Chunked; see [D2 upload](#d2-upload-image) |
| `D3` | Upload compressed | Not used in legacy | Reserved for future |
| `D4` | Redraw / refresh | End of `writeTag` | Immediate-return mode with `0x80` flag |
| `DE` | Busy poll | **Not in legacy app** | Target architecture; legacy uses timers |
| `D1` | Read config | Not used in legacy | Reserved |

Pre-init command (not in DA/DB table above):

```
F0 DB 02 00 00   → "F0DB020000" (IC DIY)
```

Sent once before driver flow in `activity_imageview.writeTag`.

---

## DA — Set Screen Type

Returned as `epd_init[1]` from `data.setEpdInit(epdColor, epdInch)`.

Example (2.13" monochrome):

```
F0DA000003F00020
```

Comment pattern in legacy:

- Screen parameter + length `03`
- Custom screen marker `F0`
- Size/resolution nibble + color nibble (must match A0 command semantics per vendor docs)

`ScreenConfig.daCommandHex` should store this string verbatim per profile.

---

## DB — Set Driver Flow

Returned as `epd_init[0]` from `data.setEpdInit`.

Structure in legacy:

1. Header: `F0DB0000XX` where `XX` is half the byte length of following payload (vendor checksum rule in comments).
2. Body: concatenated register writes (`A006...`, `A4010C...`, `A102...`, etc.).

Driver flow can be **hundreds of bytes**. Legacy sends the entire `epd_init[0]` string as **one** `IsoDep.transceive` after hex decode.

Migration: preserve single-transceive behavior first; add chunking only if hardware limits require it.

---

## D2 — Upload Image

### Packet layout (255 bytes)

| Offset | Field | Value |
|--------|--------|--------|
| 0 | CLA/vendor | `0xF0` |
| 1 | INS | `0xD2` |
| 2 | Screen index | `0` = BW plane, `1` = red/chroma plane |
| 3 | Packet index | `0..N` sequential |
| 4 | Length marker | `0xFA` (250) |
| 5–254 | Payload | Image bytes |

### Screen indices (legacy constants)

```java
ScreenIndex_BW = 0;
ScreenIndex_R  = 1;
```

### Upload algorithm (preserve order)

1. Compute `datas = width * height / 8` (SSD BW/R) or `width * height / 4` (4-color).
2. For `i = 0 .. datas/250 - 1`, send full 250-byte payload slice.
3. **Tail rule**: on last full packet index, if `datas % 250 != 0`, send **one more** packet at index `i + 1` (legacy duplicates tail handling; migration must copy exactly).
4. Tricolor: upload BW buffer (`ScreenIndex_BW`), then R buffer (`ScreenIndex_R`) with `0xFF - byte` inversion on red plane.

### Special cases (from legacy)

| Condition | Behavior |
|-----------|----------|
| `epdColor == 3` | Two passes: BW then inverted R |
| `epdColor == 2`, `epdInch == 420` | Two passes (mono panel in tri-color mode) |
| `epdColor == 2`, `epdInch == 370` | Placeholder RW pass with `0xFF` fill, then BW data |
| `epdColor == 4` | Single pass, `GetPictureData_4G`, `datas = w*h/4` |
| Red plane | `(byte)(0xFF - image_buffer[j])` |

---

## D4 — Redraw

5-byte command:

| epdColor | Byte[2] (delay) | Byte[3] | Byte[4] |
|----------|-----------------|---------|---------|
| 4 (quad) | `0x85` | `0x80` | `0x00` |
| 2, 3 | `0x05` | `0x80` | `0x00` |

```
[0] F0
[1] D4
[2] power-on delay factor (×100 ms per legacy comment)
[3] 0x80 = direct return + image area
[4] 0x00
```

Success: first response byte `0x90`.

Legacy does **not** poll `DE` after D4; it estimates refresh duration:

| Color | Legacy timer |
|-------|----------------|
| 2 | ~2 s |
| 3 | ~16 s |
| 4 | ~20 s |

**Migration target** (per ARCHITECTURE.md): Immediate Return Mode — after D4 returns `9000`, poll `DE` until status `00`.

### DE — Busy Poll (target, not legacy)

Document for implementers; add when extracting `NfcEpaperWriter`:

```
F0 DE ...  → poll until idle (0x00)
```

Exact DE frame must be confirmed against vendor PDF or tag experimentation during checkpoint 2.

---

## End-to-End Write Sequence

```
┌─────────────┐
│ RN discovers│
│ tag (IsoDep)│
└──────┬──────┘
       ▼
┌─────────────┐
│ Connect     │
│ IsoDep      │
└──────┬──────┘
       ▼
┌─────────────┐     F0DB020000
│ IC DIY      │────────────────►
└──────┬──────┘
       ▼
┌─────────────┐     epd_init[0] (DB driver flow)
│ Driver init │────────────────►
└──────┬──────┘
       ▼
┌─────────────┐     epd_init[1] (DA screen type)
│ Screen type │────────────────► check 0x90
└──────┬──────┘
       ▼
┌─────────────┐     rotate 90° (except ≥4.2" rules)
│ Image       │     pack buffers (BW / R / 4G)
│ processing  │
└──────┬──────┘
       ▼
┌─────────────┐     D2 × N packets × layers
│ Image upload│────────────────►
└──────┬──────┘
       ▼
┌─────────────┐     D4 refresh
│ Redraw      │────────────────► check 0x90
└──────┬──────┘
       ▼
┌─────────────┐     DE poll (new) or timer (legacy)
│ Wait idle   │
└─────────────┘
```

Inter-command delay in legacy: **10 ms** (`delay_ms(10)`).

---

## Screen Configuration Encoding

`MainActivity` writes `epdArray` before navigation:

| Index | Meaning |
|-------|---------|
| 0, 1 | Width (high byte, low byte) |
| 2, 3 | Height (high byte, low byte) |
| 4 | Color count: `2` mono, `3` tri, `4` quad |
| 5 | IC family: `2` = SSD (legacy default) |
| 6 | Inch code: `97`, `153`, `154`, `213`, `266`, `267`, `270`, `290`, `291`, `370`, `420` |

`activity_imageview` derives:

```text
epdWidth  = epdArray[0]*256 + epdArray[1]
epdHeight = epdArray[2]*256 + epdArray[3]
if epdInch >= 420: swap width/height for processing
```

Migration maps inch + color → `ScreenConfig` + `EpdConfigFactory` (from `data.setEpdInit`).

---

## Image Packing (feeds D2)

### SSD monochrome / tricolor — `GetPictureData_SSD(mode)`

- Source bitmap: **rotated 90°** (`bitmap90`) for panels `< 4.2"`.
- Scan: vertical, X from `width-1` down to `0`.
- BW (`mode=0`): RGB ≤ 100 → black bit `0`, else white bit `1`.
- RW (`mode=1`): R ≥ 100, G/B ≤ 100 → red bit `0`, else `1`.
- Packing: 8 pixels per byte, MSB first (`temp = temp * 2 + bit`).

### Quad — `GetPictureData_4G()`

- 4 pixels per byte, base-4 accumulation.
- Black / white / red-ish / yellow-ish thresholds per legacy RGB rules.

---

## Response Codes

| First byte | Meaning in legacy |
|------------|-------------------|
| `0x90` | Success (SW1; legacy checks `[0]` only) |
| other | "Init NG" / "Update error" |

Full ISO7816 SW parsing is a future improvement.

---

## References

| Source | Role |
|--------|------|
| `data/data.java` | DA/DB hex tables per inch × color |
| `data/activity_imageview.java` | D2/D4 construction, upload loops |
| `data/MainActivity.java` | `epdArray` population (→ RN params) |
| `SOURCES.md` | File map |
| `ARCHITECTURE.md` | Module boundaries |
