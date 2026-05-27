# Expo HAS CHANGED

Read the exact versioned docs at:
https://docs.expo.dev/versions/v56.0.0/

Use ONLY Expo SDK 56 APIs.

Never use deprecated Expo Modules APIs.

---

# Repository Purpose

This repository is a headless Expo Native Module for NFC E-Paper tags.

Goal:

React Native / Expo TS
    ↓
Expo Module
    ↓
NFC APDU Layer
    ↓
GoodDisplay E-Paper Tags

This is NOT an Android UI app.

---

# Critical Constraints

DO NOT:

- create Activities
- create Fragments
- create XML layouts
- use Compose
- use Intent navigation
- use onNewIntent for business logic
- couple NFC logic to UI
- keep mutable global state

The module must remain headless.

---

# Architecture

Use:

React Native / Expo TS
    ↓
Expo Module Bridge
    ↓
Native Services
    ↓
Image Processing
    ↓
IsoDep APDU Layer

---

# NFC Rules

React Native handles NFC discovery.

Use:
- react-native-nfc-manager

Native module responsibilities:
- IsoDep connection
- APDU transceive
- image upload
- redraw
- busy polling

---

# Existing Legacy Sources

Legacy Android code:
- MainActivity.java
- activity_imageview.java
- data.java

Rules:
- data.java remains reusable
- extract logic from Activities
- decompose Activities into services

Never preserve Activity-oriented architecture.

---

# Protocol Rules

Important APDUs:
- DA → Set Screen Type
- DB → Set Driver Flow
- D2 → Upload Image
- D4 → Refresh
- DE → Busy Poll

Packet size:
- 250 bytes max

Three-color displays require:
- two image buffers
- two uploads

---

# Image Processing

Must support:
- black/white
- black/white/red
- black/white/yellow

Input:
- file URI
- content URI
- base64

Avoid UI dependencies.

---

# Native Module Rules

Use:
- Expo Modules API
- ModuleDefinition
- AsyncFunction
- Events

Do NOT use:
- legacy ReactPackage APIs

---

# State Rules

Avoid static mutable globals.

Use session objects.

---

# Repository Navigation

Legacy Android source map:
- SOURCES.md

High-level architecture:
- ARCHITECTURE.md

Protocol docs:
- docs/protocol/

Execution plans:
- docs/exec-plans/

Active tasks:
- docs/exec-plans/active/

Completed tasks:
- docs/exec-plans/completed/

---

# Current Goal

First milestone:

"Flash image from Expo TS onto E-Paper tag successfully"

Optimize for correctness and reliability over abstraction.