# Sahaaya Known Limitations & Future Roadmap

## 1. Scope Boundaries & Clinical Constraints

### What Sahaaya IS:
- A verified, offline decision-support companion grounded strictly in approved national primary healthcare protocols.
- An instantaneous clinical danger sign escalation engine designed to prevent maternal and infant mortality.
- An out-of-scope gatekeeper preventing unauthorized frontline prescription or laboratory interpretation.
- An automated prioritization engine for daily household visits across maternal, neonatal, and chronic disease cohorts.

### What Sahaaya is NOT:
- **Not an Autonomous Medical Doctor:** Sahaaya does not diagnose complex diseases or prescribe Schedule H / Schedule X medications.
- **Not a Black-Box Generative Chatbot:** Sahaaya does not utilize unbounded large language models that can hallucinate unverified pharmacological advice.
- **Not a Telemedicine Substitute:** Sahaaya guides immediate stabilization, 108 ambulance dispatch, and hospital transfer; it does not replace physician care.

---

## 2. Technical Limitations & Field Operational Factors

### 2.1. Offline Voice Recognition Dependence
- **Mechanism:** The application invokes the standard Android `RecognizerIntent.ACTION_RECOGNIZE_SPEECH` API.
- **Limitation:** On physical devices operating in 100% Airplane Mode, voice transcription requires that the user has previously downloaded the offline language model for Hindi (`hi-IN`) or Telugu (`te-IN`) via *Google Voice Typing Settings > Offline Speech Recognition*.
- **Mitigation:** The application provides a seamless fallback with complete on-screen keyboard input and code-mixed quick suggestion chips.

### 2.2. Government IT System Integration
- **Boundary:** In accordance with the problem statement guidelines, there is no direct live API integration with national portals such as U-WIN (Universal Immunization Registry), RCH Portal, ANMOL, or the Ayushman Bharat Digital Mission (ABDM).
- **Impact:** Frontline workers cannot use Sahaaya to resolve portal password resets, OTP failures, or server login timeouts. The `IntentGate` categorically intercepts and explains these boundaries under `SCP-1.2`.

### 2.3. Temporal Anchor in Evaluation vs. Field Operations
- **Challenge Evaluation Constraint:** Official challenge benchmark scripts require an evaluation anchor date of `2026-10-26` to evaluate retrospective vaccine and ANC overdue status.
- **Field Operational Adaptation:** In live production deployments, the `VisitListEngine` accepts an injectable `today: LocalDate = LocalDate.now()` which calculates real-time overdue intervals daily.

### 2.4. Geographic Adaptation
- **Current Dataset:** The facility directory and beat maps are modeled around the Chandrapuri Block catchment area in Andhra Pradesh.
- **Portability:** Facility coordinates, ambulance contact numbers, and ANM sector mappings are completely decoupled into `FacilityDirectory.kt` and `assets/households.csv`, allowing deployment to any district in India simply by updating local CSV/JSON configuration files.

---

## 3. Recommended Future Roadmap

1. **Native On-Device ASR (Whisper-Tiny / Vosk):** Integrate an embedded ONNX / TFLite quantized acoustic speech model for Hindi and Telugu directly into the APK to eliminate third-party voice package dependencies.
2. **Bluetooth P2P Mesh Synchronization:** Enable local Bluetooth / Wi-Fi Direct peer-to-peer syncing between ASHA phones and the supervisor ANM tablet during weekly VHND sessions without requiring internet connectivity.
3. **ABDM Sandbox Integration:** Implement offline FHIR / ABHA card scanning via QR code camera parser to auto-populate beneficiary records into the offline register.

