# Quad 213 Buffer Parity Audit

**Panel:** inchCode = 213, colorMode = 4 (quad)  
**Method:** Code-path trace only — no comment inference, no doc inference  
**Asset note:** `R.drawable.f213` is **not present** in this repository. Bitmap dimensions before rotation cannot be numerically proven without the drawable file or a runtime log from the legacy APK.

---

## Section 1 — Android Runtime Dimensions

### Execution path (213 + color 4)

```
MainActivity.button_0213.onClick
  → epdArray[0..6] written
  → data.setEpdArray(epdArray)
  → startActivity(activity_imageview)

activity_imageview.onCreate
  → epdArray = data.getEpdArray()
  → epdInch = epdArray[6]        // 213
  → epdColor = epdArray[4]       // 4 (when user selected 4-color)
  → epdWidth  = epdArray[0]*256 + epdArray[1]
  → epdHeight = epdArray[2]*256 + epdArray[3]
  → (213 < 420 → no width/height swap)
  → imageView.setImageResource(R.drawable.f213)
  → bitmap0 = getBitmapFromImageView(imageView)

writeTag (NFC thread)
  → bitmap90 = rotateBitmap(bitmap0, 90)
  → (epdColor == 4 branch)
  → GetPictureData_4G()
  → datas = width0 * height0 / 4
  → D2 upload loop
```

### Values provable from code (213 + color 4)

| Quantity | Value | Derivation |
|----------|-------|------------|
| **epdArray width** | **128** | `MainActivity.java:142-143` → `128/256=0`, `128%256=128` |
| **epdArray height** | **250** | `MainActivity.java:144-145` → `250/256=0`, `250%256=250` |
| **epdInch** | **213** | `MainActivity.java:148` |
| **epdColor** | **4** | `MainActivity.java:146` sets `epdArray[4]=ColorValue`; user must select 4-color radio |
| **epdWidth (onCreate)** | **128** | `activity_imageview.java:84-85`, `213 < 420` → no swap |
| **epdHeight (onCreate)** | **250** | same |
| **epdWidth used in image path?** | **No** | `epdWidth` / `epdHeight` are assigned in `onCreate` but **never read** in `writeTag`, `GetPictureData_4G`, or bitmap loading (grep confirms only assignments at lines 84-90) |
| **bitmap0 width (W₀)** | **W₀ = f213 intrinsic width** | `getBitmapFromImageView` → `((BitmapDrawable) drawable).getBitmap()` — no scaling, no epdArray (lines 246-255, 217) |
| **bitmap0 height (H₀)** | **H₀ = f213 intrinsic height** | same |
| **bitmap90 width after rotate** | **H₀** | `rotateBitmap`: `createBitmap(..., width, height, matrix, false)` with `postRotate(90)` swaps dimensions |
| **bitmap90 height after rotate** | **W₀** | same |
| **width0** | **H₀** | Set inside `GetPictureData_4G()` from `bitmap90.getWidth()` (line 307) |
| **height0** | **W₀** | Set from `bitmap90.getHeight()` (line 308) |
| **GetPictureData_4G bytes written** | **H₀ × ⌊W₀/4⌋** | Loop: `i` from `width0-1..0`, `j` from `0..height0/4-1` (lines 309-338); one byte per `(i,j)` |
| **datas (upload length)** | **H₀ × W₀ / 4** | `writeTag` line 772 (integer division) |
| **Consistency** | **H₀×W₀/4 = H₀×⌊W₀/4⌋ iff W₀ % 4 == 0** | If W₀ not divisible by 4, packing loop and `datas` disagree |

### Derived formulas (symbolic)

Let **W₀, H₀** = `bitmap0.getWidth/Height()` after loading `f213`.

| Quantity | Formula |
|----------|---------|
| width after rotate | H₀ |
| height after rotate | W₀ |
| image_buffer bytes used | H₀ × (W₀ / 4) — Java int division in loop bound |
| datas | (H₀ × W₀) / 4 |
| D2 full packets | `datas / 250` |
| D2 tail packet | +1 if `datas % 250 != 0` |

### Numeric scenarios (depends on f213 asset — not in repo)

**Scenario A — if f213 bitmap is 128×250** (matches epdArray; same W/H as `bw213`/`r213` slot in switch):

| Quantity | Value |
|----------|-------|
| W₀ × H₀ | 128 × 250 |
| After rotate | 250 × 128 |
| width0 × height0 | 250 × 128 |
| datas | 250×128/4 = **8000** |
| D2 packets | 8000/250 = **32** (remainder 0 → no tail) |

**Scenario B — if f213 bitmap is 256×250**:

| Quantity | Value |
|----------|-------|
| After rotate | 250 × 256 |
| datas | 250×256/4 = **16000** |
| D2 packets | **64** |

**Which scenario is Android?** Cannot be determined from source code in this repo. Requires measuring `((BitmapDrawable)f213).getBitmap()` from the legacy APK.

---

## Section 2 — Android Packing Algorithm

### Branch selection for epdColor = 4

In `writeTag`, after `bitmap90 = rotateBitmap(bitmap0, 90)`:

| Block | Condition | Runs for color 4? |
|-------|-----------|-------------------|
| Mono 3.7" dual | `epdColor==2 && epdInch==370` | No |
| Mono 4.2" dual | `epdColor==2 && epdInch==420` | No |
| Mono default | `epdColor==2 && inch not 370/420` | No |
| Tri upload | `if (epdColor == 3)` | No |
| **Quad upload** | **`if (epdColor == 4)`** | **Yes** (line 769) |

For inch 213 + color 4, **only** the block at line 769 executes for image upload.

### Is GetPictureData_4G used?

**Yes.** Line 771: `GetPictureData_4G();` is the only pack call in the color-4 block.

### Is GetPictureData_SSD used?

**Not for color 4 upload.** `GetPictureData_SSD` appears only in mono (`epdColor==2`) and tri (`epdColor==3`) blocks. No call path from `epdColor==4` to `GetPictureData_SSD`.

### GetPictureData_4G algorithm (actual code)

Source: `activity_imageview.java:302-341`

1. Input bitmap: `bitmap90` (already rotated 90°)
2. Set `width0`, `height0` from bitmap dimensions
3. Scan order:
   - Outer `i`: X from `width0-1` down to `0` (horizontal mirror)
   - Middle `j`: Y groups of 4 rows: `0 .. height0/4 - 1`
   - Inner `k`: `0..3` pixels within group
4. Per pixel: 2 bits base-4 (`temp = temp * 4 + nibble`)
5. Thresholds:
   - Black: R,G,B ≤ 100 → +0
   - White: R,G,B ≥ 200 → +1
   - Else avg ≤ 127 → +3, else → +2
6. One output byte per `(i, j)` group
7. Bytes per pixel group: **4 pixels → 1 byte** (2 bits × 4 = 8 bits)

### D2 upload (color 4)

Source: `activity_imageview.java:771-813`

- `ScreenIndex_BW = 0` (line 778)
- Single pass (no red plane for quad)
- Payload: `image_buffer[j + 250 * i]` for packet index `i`

---

## Section 3 — Expo Runtime Dimensions

### Execution path (213 + quad)

```
EpdConfigFactory.create(ColorMode.QUAD, 213)
  → PanelGeometry.forInch(213) → width=128, height=250
  → ScreenConfig (colorMode=4)

ScreenConfig.toImageProcessingConfig()
  → getProcessingWidth(): 128 * 2 = 256  (213 < 420, no swap; quad branch)
  → getProcessingHeight(): 250

ImageProcessor.process(source, config)
  → scaleToPanel(source, 256, 250)
  → rotateBitmap(scaled, 90f)
  → packQuadLayer(rotated)
  → ProcessedImage(blackLayer, null, width0, height0, 4)

ApduBuilder.buildImageUploadSequence(config, processedImage)
  → planUploadPasses → single BW pass with blackLayer
  → buildD2Sequence(SCREEN_INDEX_BW, blackLayer)
```

### Values provable from current Expo code (213 + quad)

| Quantity | Value | Derivation |
|----------|-------|------------|
| **ScreenConfig.width** | **128** | `EpdConfigFactory.PanelGeometry` case 213 |
| **ScreenConfig.height** | **250** | same |
| **Processing width** | **256** | `ScreenConfig.getProcessingWidth()` → `128 * 2` (quad default branch) |
| **Processing height** | **250** | `getProcessingHeight()` → no swap (213 < 420) |
| **Scaled bitmap (before rotate)** | **256 × 250** | `ImageProcessor.scaleToPanel` |
| **Rotated width** | **250** | `rotateBitmap(90°)` swaps |
| **Rotated height** | **256** | same |
| **ProcessedImage.width** | **250** | `rotated.getWidth()` |
| **ProcessedImage.height** | **256** | `rotated.getHeight()` |
| **blackLayer length** | **16000** | `(250 × 256) / 4` |
| **D2 packets** | **64** | `16000 / 250`, remainder 0 |

### Expo vs pre-Checkpoint-7 (for reference)

Before CP7 (`ScreenConfig` without quad doubling):

| Quantity | Pre-CP7 Expo |
|----------|--------------|
| Processing size | 128 × 250 |
| After rotate | 250 × 128 |
| Buffer | 8000 |
| D2 packets | 32 |

---

## Section 4 — Buffer Comparison

| Property | Android | Expo (current, post-CP7) |
|----------|---------|----------------------------|
| **Width before rotate** | **W₀** (f213 intrinsic; **not epdArray**) | **256** (forced by `scaleToPanel`) |
| **Height before rotate** | **H₀** (f213 intrinsic) | **250** |
| **Width after rotate** | **H₀** | **250** |
| **Height after rotate** | **W₀** | **256** |
| **Buffer size (datas)** | **(H₀ × W₀) / 4** | **16000** |
| **D2 packets** | **(H₀×W₀/4)/250 + tail** | **64** |
| **Packing algorithm** | `GetPictureData_4G` | `ImageProcessor.packQuadLayer` (ported same loops/thresholds) |
| **Scaling step** | **None** | **`Bitmap.createScaledBitmap` to 256×250** |
| **Uses epdArray W×H for bitmap?** | **No** | **No (uses ScreenConfig processing dims)** |

### If f213 asset is 128×250 (Scenario A — matches epdArray)

| Property | Android | Expo (post-CP7) | Match? |
|----------|---------|-----------------|--------|
| Before rotate | 128 × 250 | 256 × 250 | **No** |
| After rotate | 250 × 128 | 250 × 256 | **No** |
| Buffer | 8000 | 16000 | **No** |
| D2 packets | 32 | 64 | **No** |

### If f213 asset is 256×250 (Scenario B)

| Property | Android | Expo (post-CP7) | Match? |
|----------|---------|-----------------|--------|
| Before rotate | 256 × 250 | 256 × 250 | Yes |
| After rotate | 250 × 256 | 250 × 256 | Yes |
| Buffer | 16000 | 16000 | Yes |
| D2 packets | 64 | 64 | Yes |

### If f213 asset is 128×250 — Expo pre-CP7

| Property | Android | Expo (pre-CP7) | Match? |
|----------|---------|------------------|--------|
| Before rotate | 128 × 250 | 128 × 250 (scaled) | Yes* |
| After rotate | 250 × 128 | 250 × 128 | Yes |
| Buffer | 8000 | 8000 | Yes |
| D2 packets | 32 | 32 | Yes |

\*Expo still applies `createScaledBitmap`; Android does not. Dimensions match only if source already 128×250 or stretch produces same pixel grid.

---

## Section 5 — Root Cause

### Answer: **E — Multiple issues exist**

#### Issue E1 — Wrong sizing assumption in Checkpoint 7 (proven)

**CP7 doubled quad width to 256 based on `data.java` comments**, not on Android execution.

Android code **never**:

- multiplies width by 2 for quad
- reads `epdWidth` / `epdHeight` when building the upload buffer
- scales `bitmap0` to epdArray dimensions

Android upload dimensions come **only** from `R.drawable.f213` intrinsic bitmap size → rotate → pack.

If `f213` is 128×250 (consistent with epdArray and mono/tri drawable slots), **post-CP7 Expo uploads 16000 bytes / 64 D2 packets while Android uploads 8000 bytes / 32 D2 packets** — exactly 2× data horizontally. That produces a **“half baked” / partially rendered** appearance (wrong column count in packed buffer).

#### Issue E2 — Expo always scales; Android never scales (proven)

Android path:

```java
bitmap0 = getBitmapFromImageView(imageView);  // intrinsic drawable pixels
bitmap90 = rotateBitmap(bitmap0, 90);
```

Expo path:

```java
Bitmap scaled = scaleToPanel(source, panelWidth, panelHeight);
Bitmap rotated = rotateBitmap(scaled, 90f);
```

For user-supplied images with aspect ratio ≠ panel ratio, Expo **stretches** pixels. Android reference app uses a **pre-authored** drawable with no scaling step. This can cause orientation/fill differences even when buffer dimensions match.

#### Issue C — Rotation (not proven as mismatch)

`rotateBitmap` in Expo is a line-for-line port of Android (`postRotate(angle)` + `createBitmap`). Both use **+90°** for all inch codes including 213. **No code-path difference found.**

#### Issue D — Packing algorithm (not proven as mismatch)

`packQuadLayer` matches `GetPictureData_4G` loop structure, thresholds (100/200/127), scan order, and 4-pixels-per-byte encoding. **No difference found in executed logic.**

#### What is NOT proven wrong from code alone

- **A/B individually** — cannot label “width wrong” or “height wrong” without knowing W₀,H₀ of f213
- **D** — packing algorithm matches

---

## Section 6 — Minimal Fix (proposal only — not implemented)

### Step 0 — Required measurement (before any code change)

Extract or log from legacy APK:

```java
Bitmap b = ((BitmapDrawable) getResources().getDrawable(R.drawable.f213)).getBitmap();
// log b.getWidth(), b.getHeight()
```

This single measurement selects between Scenario A and B.

### Fix option 1 — If f213 is 128×250 (most likely from epdArray parity)

| Item | Detail |
|------|--------|
| **File** | `models/ScreenConfig.java` |
| **Method** | Remove `quadProcessingWidth()` doubling in `getProcessingWidth()` for colorMode QUAD |
| **Reason** | Android execution never applies 2×; CP7 introduced 16000-byte buffer vs Android 8000-byte path |

Processing dimensions should be **128 × 250** (same as `EpdConfigFactory.PanelGeometry` / epdArray), not 256 × 250.

### Fix option 2 — If f213 is 256×250 (Scenario B)

| Item | Detail |
|------|--------|
| **File** | None required for dimensions |
| **Reason** | Post-CP7 Expo already matches 256×250 → 250×256 → 16000 bytes |

Remaining mismatch would be Issue E2 (scaling vs pre-authored drawable) or input image content — not buffer length.

### Fix option 3 — Align Expo with Android execution model (smallest behavioral parity)

| Item | Detail |
|------|--------|
| **File** | `services/ImageProcessor.java` |
| **Method** | `process()` — for quad, skip `scaleToPanel` when source dimensions already equal target; otherwise scale to **epdArray dimensions (128×250 for 213)**, not doubled width |
| **Reason** | Android never scales bitmap0; Expo should target the same pixel grid Android uploads, not a derived comment-based size |

### What NOT to change

- `packQuadLayer` / `GetPictureData_4G` logic — algorithms match
- `rotateBitmap` — already identical
- `ApduBuilder.buildD2Sequence` — packetization matches legacy loop
- NFC / protocol / bridge layers

---

## Success Criteria Checklist

| Criterion | Status |
|-----------|--------|
| Android runtime dimensions traced from code | ✅ (symbolic W₀/H₀; epdArray 128×250 proven) |
| Expo runtime dimensions traced from code | ✅ (256×250 → 250×256 → 16000 → 64 D2) |
| Buffer lengths derived | ✅ (formulas + scenarios) |
| D2 packet counts derived | ✅ |
| Parity mismatch identified | ✅ (CP7 2× width not in Android path; scaling difference) |
| Minimal fix proposed | ✅ (revert doubling pending f213 measurement) |
| f213 intrinsic dimensions proven | ❌ **Blocked — asset not in repo** |

---

## Recommended next action (measurement only)

Add a one-line log in legacy `activity_imageview.onCreate` after line 217, or inspect `f213.png` from the original APK:

```
bitmap0.getWidth() × bitmap0.getHeight()
```

Until that value is known, **do not treat CP7’s 256×250 target as proven Android behavior**.
