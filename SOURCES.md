# Legacy Android Sources

These files are the original Android implementation from the legacy NFC E-Paper app.

They are the primary reverse-engineering sources for the Expo Native Module migration.

The architecture in these files is Activity-oriented and must NOT be preserved directly.

Goal:
extract reusable services from these files.

---

# Source Files

data/
├── MyActivity.java
├── data.java
└── activity_imageview.java

---

# File Responsibilities

## MyApplication.java

Purpose:
- Android application bootstrap
- app-wide initialization
- global Android setup

Migration Notes:
- avoid Application-oriented architecture where possible
- only preserve reusable initialization logic
- do not recreate Android app lifecycle structure

---

## data.java

Purpose:
- protocol constants
- screen configuration data
- shared command parameters
- display metadata
- APDU-related constants

Migration Notes:
- this file remains reusable
- prefer extracting immutable configuration structures
- avoid mutable static state

Likely Destination:
- services/EpdConfigFactory.java
- models/ScreenConfig.java
- protocol/ constants

---

## activity_imageview.java

Purpose:
- bitmap processing
- image conversion
- NFC transceive
- APDU transmission
- image upload flow
- redraw flow
- busy polling
- screen flashing pipeline

Contains:
- IsoDep logic
- D2 upload flow
- D4 redraw flow
- DE polling flow
- bitmap packing
- black/white conversion
- tricolor image generation

Migration Notes:
- THIS IS THE PRIMARY SOURCE FILE
- extract logic into headless services
- remove ALL UI dependencies
- remove ALL Activity dependencies
- remove ALL Intent dependencies

Do NOT preserve:
- ImageView logic
- Android UI rendering
- Activity lifecycle logic
- navigation logic
- click listeners

Likely Destination:
- services/ImageProcessor.java
- services/NfcEpaperWriter.java
- services/ApduBuilder.java
- models/ProcessedImage.java

---

# Important Protocol Notes

The legacy implementation uses GoodDisplay / Fudan ESL APDUs.

Important commands:
- DA → Set Screen Type
- DB → Set Driver Flow
- D2 → Upload Image
- D4 → Redraw
- DE → Busy Poll

Packet size:
- 250 bytes max

Three-color displays require:
- two image layers
- two uploads

---

# Migration Philosophy

The legacy app is effectively:

"an embedded hardware driver hidden inside Activities"

The migration objective is:

"convert the driver into a reusable Expo Native Module SDK"

Prefer:
- service extraction
- immutable state
- protocol correctness
- headless architecture

Avoid:
- Android UI patterns
- lifecycle coupling
- Activity-driven design