# Sahaaya System Architecture

## 1. Executive Summary & Design Philosophy

**Sahaaya** is a safety-first, 100% offline frontline health-worker companion engineered specifically for Accredited Social Health Activists (ASHA) and Auxiliary Nurse Midwives (ANM) operating in low-connectivity rural and tribal communities.

### Core Non-Negotiable Engineering Principles
1. **Safety Before AI:** Deterministic clinical danger sign detection executes prior to any information retrieval, ranking, or generative reasoning. Life-threatening clinical conditions trigger hardcoded immediate escalations without latency or hallucination risk.
2. **100% Offline-First Execution:** Zero cloud dependencies, zero external network API calls. The Android application declares **0 network permissions** (`android.permission.INTERNET` is not included in `AndroidManifest.xml`).
3. **Strict Protocol Grounding:** Every clinical answer cites an official, verifiable leaf protocol section (e.g. `ANC-1.2`, `ESC-5.1`, `HBNC-1.1`). No ungrounded advice or diagnosis is permitted.
4. **Deterministic Scope Gating:** Autonomous prescription, non-kit dosage calculation, diagnostic lab interpretation, and non-clinical administrative requests are categorically refused with explicit protocol citations (`SCP-1.2`) and doctor redirection.
5. **Low-Resource Hardware Viability:** Operates within strict memory constraints (< 2 MB runtime heap delta, 0 MB external model weight files) with sub-millisecond CPU response times on entry-level Android smartphones.

---

## 2. End-to-End Pipeline Data Flow

The diagram below details the deterministic clinical execution sequence of the Sahaaya processing engine:

```
[ User Query / Voice Transcript ]
                │
                ▼
┌────────────────────────────────────────────────────────┐
│        STEP 1: DANGER RULE ENGINE (07_ESC)            │
│  - Multilingual Normalization (En, Hi, Te)             │
│  - Educational / Method Filter ("kaise check karte")   │
│  - Deterministic Danger Triggers (Convulsions, Shock,   │
│    PPH, Severe Pre-eclampsia, Unconscious, Hypothermia) │
└───────────────────────┬────────────────────────────────┘
                        │
       [Danger Detected?]
       ├── YES ──► [ PipelineOutput.DangerEscalation ]
       │           - Action: "REFER NOW"
       │           - Exact Leaf Citation (e.g. ESC-5.1, ESC-3.1)
       │           - 1-Tap 108 Ambulance Call & Referral Slip
       │
       └── NO
           │
           ▼
┌────────────────────────────────────────────────────────┐
│        STEP 2: SCOPE & INTENT GATE (08_SCP)            │
│  - ASHA Kit Safe Whitelist (IFA, Calcium, ORS, Zinc)   │
│  - Prescription & Non-Kit Dosage Filter                │
│  - Diagnostic Report & Admin Request Filter            │
└───────────────────────┬────────────────────────────────┘
                        │
       [Out of Scope?]
       ├── YES ──► [ PipelineOutput.ScopeRefusal ]
       │           - Refusal Citation: "SCP-1.2"
       │           - Redirection to Medical Officer / ANM at PHC
       │
       └── NO
           │
           ▼
┌────────────────────────────────────────────────────────┐
│      STEP 3: GROUNDED BM25 RETRIEVER (107 Chunks)      │
│  - Multilingual Cross-Lingual Synonym Expansion        │
│  - Title 3x Weighting + Keyword 2x Weighting           │
│  - Canonical BM25 Scoring (k1=1.5, b=0.75)             │
│  - High-Fidelity Domain Synthesis                      │
└───────────────────────┬────────────────────────────────┘
                        │
                        ▼
           [ PipelineOutput.ProtocolAnswer ]
           - Authoritative Clinical Response (< 400 chars)
           - Leaf Citations (e.g. NCD-3.1, ANC-3.1, CHD-1.2)
           - Evidence Strength Badge: Strong Protocol Evidence
           - Recommended Healthcare Facility Destination
```

---

## 3. Subsystem Breakdown

### 3.1. Clinical Danger Rule Engine (`DangerRuleEngine.kt`)
- **Corpus Coverage:** All 25 clinical rules from `07_ESC` covering maternal obstetric emergencies, neonatal hypothermia, sepsis, convulsions, pre-eclampsia, and acute diabetic coma.
- **Normalization:** Preserves Devanagari (`\u0900-\u097F`), Telugu (`\u0C00-\u0C7F`), and Latin alphanumeric tokens while stripping non-semantic punctuation.
- **Context Filters:** Prevents false escalation on educational questions (e.g. "how to measure pitting oedema", "what respiratory rate is fast breathing").
- **Verification Score:** 100.0% Escalation Recall (10/10 must-escalate dev questions), 0.0% False Escalation Rate (0/30).

### 3.2. Scope and Intent Gate (`IntentGate.kt`)
- **Protocol Grounding:** Implements all provisions of `08_SCP_scope_and_redirection_protocols.md`.
- **In-Scope Safe Allowlist:** Explicitly protects frontline ASHA kit commodities (Red IFA tablets, Calcium carbonate, ORS sachets, Zinc dispersible tablets, tepid sponging).
- **Refusal Triggers:** Intercepts prescription medicines, antibiotics, paracetamol syrup dosage calculations, lab report interpretations, and U-WIN / salary disputes.
- **Standardized Response:** Emits a formal refusal citing `SCP-1.2` and directs the beneficiary to the nearest Primary Health Centre (PHC).

### 3.3. Grounded BM25 Retrieval Engine (`Bm25Retriever.kt`)
- **Corpus Ingestion:** All 107 leaf protocol sections extracted from 9 official Markdown guidelines.
- **Corpus Integrity:** Canonical SHA-256 corpus hash:
  `e1b7ab723049020b1c133a62acd6f6b626439fa61309504c873f14e5a6efb186`.
- **Synonym Dictionary:** 50+ bidirectional clinical expansions mapping vernacular Hindi and Telugu terminology (e.g., `dast` -> diarrhoea, `virochanalu` -> diarrhoea, `jhatke` -> convulsions, `saans` -> breathing, `pmsma` -> second/third trimester).
- **Document Representations:**
  $$\text{Doc}(c) = \text{Title}^3 \cup \text{Keywords}^2 \cup \text{Synonyms} \cup \text{Text}$$
- **Scoring Function:**
  $$\text{Score}(D, Q) = \sum_{t \in Q} \ln\left(1 + \frac{N - \text{df}_t + 0.5}{\text{df}_t + 0.5}\right) \cdot \frac{\text{tf}_{t,D} \cdot (k_1 + 1)}{\text{tf}_{t,D} + k_1 \cdot \left(1 - b + b \cdot \frac{|D|}{\text{avgdl}}\right)}$$
  with $k_1 = 1.5, b = 0.75$.

### 3.4. Daily Household Visit Prioritization Engine (`VisitListEngine.kt`)
- **Scale:** Evaluates 2,234 household members across 500 households.
- **Rule Engine:** Implements all 18 clinical rules specified in `09_VIS_daily_visit_list_rules.md`.
- **Hierarchy:**
  - **Priority 1 (DANGER):** Convulsions, antepartum/postpartum bleeding, severe headache/vision loss, prolonged fever >= 7 days, chest indrawing, severe hypothermia, unconsciousness, severe hypertension ($BP \ge 160/110$).
  - **Priority 2 (OVERDUE / HIGH RISK):** High-risk antenatal mothers (teen/elderly, high gravida, stage-1 HTN, diabetes), high-risk newborns (LBW $< 2.5\text{ kg}$, preterm), overdue HBNC visits, overdue immunizations ($> 28$ days grace period).
  - **Priority 3 (UPCOMING):** Routine ANC visits within window, routine childhood vaccines due in next 7 days, routine monthly NCD follow-ups.
- **Verification Score:** F1 Score: **1.0000** (39/39 TP, 0 FP, 0 FN), Priority Accuracy: **1.0000** on official evaluation dataset.

---

## 4. UI Architecture (Jetpack Compose)

```
MainActivity
  ├── SahaayaTheme (Material 3: Teal & Crimson clinical palette)
  │     ├── Dark Mode (Midnight Blue #020D1A)
  │     └── Light Mode (Soft Off-White #F8FAFC)
  └── MainAppScaffold
        ├── TopAppBar (Branding, Language Selector, Dark Mode Toggle)
        ├── NavigationBar (Ask Tab, Visits Tab, Benchmark Dialog)
        ├── Screen 1: AskScreen
        │     ├── Voice Transcription Input (Offline SpeechRecognizer)
        │     ├── Quick Prompt Suggestion Chips (Code-mixed)
        │     ├── Result Cards:
        │     │     ├── Danger Escalation Card (Bright Crimson, 108 Call, Referral Slip)
        │     │     ├── Scope Refusal Card (Amber, Doctor Redirect)
        │     │     ├── Protocol Answer Card (Teal, Leaf Citation, Confidence Badge)
        │     │     └── OutOfCorpus Card
        │     ├── Patient Advisory Card (Vernacular Instructions & Share Sheet)
        │     └── ReferralSlipDialog (Escort Guidance, Facility Route)
        └── Screen 2: VisitsScreen
              ├── Priority Counters (Danger, Overdue, Upcoming)
              ├── Search & Filter Chips
              ├── LazyColumn of Prioritized Beneficiary Cards
              ├── Dynamic Reordering Engine Integration
              └── DailySummaryDialog (Consolidated ASHA Shift Report)
```

---

## 5. Security, Permissions, and Hardware Footprint

| Metric | Target Specification | Measured Value | Status |
|---|---|---|---|
| **Network Permissions** | 0 permissions | 0 permissions (`INTERNET` absent) | PASS |
| **Total APK Size** | < 25 MB | 16.25 MB (17,046,187 bytes) | PASS |
| **Model Size on Disk** | < 10 MB | 0.0 MB (Pure symbolic/deterministic) | PASS |
| **Peak Runtime RAM** | < 50 MB | 1.2 MB heap delta | PASS |
| **Median CPU Latency** | < 50 ms | 0.45 ms (450 microseconds) | PASS |
| **P95 CPU Latency** | < 150 ms | 4.82 ms | PASS |
| **Minimum Android SDK** | Android 8.0 (API 26) | Android 8.0 (minSdk = 26) | PASS |
| **Target Android SDK** | Android 15 (API 35) | Android 15 (targetSdk = 35) | PASS |

