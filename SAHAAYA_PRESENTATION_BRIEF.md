# Sahaaya (సహాయ / सहाय) — Comprehensive Presentation Brief & Feature Deck
> **Hackathon Track:** Talent AP Mega Hackathon 2026 — Problem H5: Frontline Health-Worker Companion for Low-Connectivity Areas  
> **Team:** Vizag Mavericks (`TEAM-001245`)  
> **Current App Release:** `v1.5.0-H5` (Native Android, 100% Offline, Zero Cloud Dependencies)  
> **Evaluation Benchmark Status:** Escalation Gate: **PASS** (100% Recall), Visit Triage: **100% F1-Score**, Median Latency: **0.077 ms**

---

## 🎯 Executive Summary (For Slide 1: Pitch & Title)
- **Product Name:** Sahaaya (సహాయ / सहाय — "Support / Companion")
- **Tagline:** Frontline Health-Worker Clinical Companion for Low-Connectivity Areas
- **Target Users:** Accredited Social Health Activists (**ASHA**) and Auxiliary Nurse Midwives (**ANM**) across rural India.
- **Core Mission:** Eliminate maternal and infant mortality caused by delayed referrals, app overload, and lack of connectivity by putting a high-speed, 100% offline, zero-hallucination paramedical companion into every worker's pocket.
- **Key Differentiator:** While generic cloud AI chatbots hallucinate lethal drug doses and fail without 4G, Sahaaya operates **100% offline in Airplane Mode**, guarantees **100% danger sign recall** before any AI reasoning, refuses out-of-scope medical prescriptions, formats official MoHFW protocols cleanly, and triages daily household visits in sub-millisecond speeds.

---

## 🚨 The Ground Reality & Problem Statement (For Slide 2: Problem & Need)
1. **The Massive Frontline Scale:**
   - Over **10.29 lakh active ASHAs** across India serve as the first point of contact for ~1,000 citizens each.
2. **The Connectivity Void (Andhra Pradesh Context):**
   - Over **1,700 remote villages in Andhra Pradesh** (over 1,000 in Alluri Sitharama Raju district alone) have **zero mobile reception**. Cloud-dependent LLMs simply do not work.
3. **Maternal & Neonatal Mortality Realities:**
   - Obstetric haemorrhage accounts for **47% of all maternal deaths** in India. Neonatal asphyxia, severe hypothermia, and chest indrawing kill within hours if missed. Every minute of hesitation or delayed referral is fatal.
4. **App Overload & Cognitive Fatigue:**
   - ANMs and ASHAs are forced to juggle **up to 28 paper registers alongside 23 disconnected mobile apps**, leading to extreme double-entry fatigue, missed follow-ups, and delayed care.
5. **Generative AI Dangers:**
   - Generative LLMs hallucinate plausible-sounding but lethal drug dosages (e.g. prescribing pediatric antibiotics that ASHAs are legally prohibited from administering).

---

## 🏗️ Technical Architecture & Core Pipeline (For Slide 3 & 4: Architecture)
Sahaaya uses a **dual-engine architecture**: a **Deterministic 3-Tier Clinical Safety Pipeline** running in parallel with an **Algorithmic Household Visit Engine**.

```
                           [ Frontline Worker Query ]
                       (Voice Mic / Multilingual Text)
                                     │
                                     ▼
      ┌──────────────────────────────────────────────────────────────┐
      │  1. DETERMINISTIC DANGER-SIGN RULE ENGINE                    │
      │  • Hard-coded regex & 367 multilingual tokens (EN/HI/TE)     │
      │  • 100% Zero-Delay Escalation (Fires BEFORE any retrieval)   │
      └──────────────────────────────┬───────────────────────────────┘
                                     │
                 ┌───────────────────┴───────────────────┐
          [Emergency Detected]                     [No Danger Detected]
                 │                                       │
                 ▼                                       ▼
      ┌───────────────────────┐       ┌────────────────────────────────────┐
      │ 🚨 RED SCREEN ESCALATE│       │  2. INTENT SAFETY GATE             │
      │ • Immediate Action    │       │  • Detects Antibiotics / Dosages   │
      │ • 1-Tap 108 Slip      │       │  • Enforces ASHA Non-Prescribing   │
      │ • 1-Tap 48h Re-Visit  │       └─────────────────┬──────────────────┘
      └───────────────────────┘                         │
                                          ┌─────────────┴─────────────┐
                                   [Out of Scope]                [In Scope]
                                          │                           │
                                          ▼                           ▼
                               ┌─────────────────────┐   ┌─────────────────────────┐
                               │ ⚠️ PHYSICIAN REDIRECT│   │ 3. OFFLINE BM25 ENGINE  │
                               │ Strict Legal Refusal│   │ • Pure Kotlin (0.077 ms)│
                               │ Protects Malpractice│   │ • 107 Official Chunks   │
                               └─────────────────────┘   └────────────┬────────────┘
                                                                      │
                                          ┌───────────────────────────┴───────────────────────────┐
                                          ▼                                                       ▼
                             ┌─────────────────────────┐                             ┌─────────────────────────┐
                             │ TIER 1: SALIENT ANSWER  │                             │ TIER 2: DETAILED VIEW   │
                             │ • Concise (<= 380 chars)│                             │ • Dynamic Table Parsing │
                             │ • MoHFW Leaf Citation   │                             │ • Formatted Bullet Points│
                             │ • Evaluation Grounding  │                             │ • Zero Pipe Delimiters  │
                             └─────────────────────────┘                             └─────────────────────────┘
```

---

## ⚡ Current Features & Implementation State (`v1.5.0-H5`)

### 1. Deterministic Danger-Sign Escalation Engine
- **Mechanism:** Hard-coded deterministic regex and clinical token matching covering **27 critical clinical danger signs** across **367 multilingual phrasings** (English, Hindi, and Telugu).
- **Execution:** Fires **before any retrieval or search algorithm**, guaranteeing zero retrieval latency.
- **Benchmark Performance:** **100.0% recall** on all emergency test scenarios (10/10 must-escalate cases in benchmark); **0.0% false escalation rate**.
- **Clinical Actions:**
  - Full-screen bright red alert card.
  - Direct first-aid protocol guidance (e.g., left lateral tilt, keep warm, do not give liquids during convulsions).
  - Exact official MoHFW citation (e.g., `(ESC-1.1)`, `(ESC-1.2)`, `(ESC-2.1)`).

### 2. Intent Safety Gate & Malpractice Protection
- **Mechanism:** Lexical classifier that detects queries seeking antibiotic dosages, prescription pharmaceuticals, invasive procedures, or physician-only diagnostic guidance.
- **Execution:** Enforces strict compliance with MoHFW ASHA Induction Modules 6 & 7.
- **Benchmark Performance:** **90% refusal accuracy**, redirecting the worker to an MBBS Medical Officer or Community Health Centre (CHC) with citation `(SCP-1.2)`.

### 3. Pure-Kotlin Offline BM25 Protocol Retrieval Engine
- **Mechanism:** High-speed on-device BM25 ranking algorithm indexed across **107 official MoHFW/NHM protocol chunks** (Child Health, Immunization, Antenatal Care, Postnatal Care, Screening, Nutrition).
- **Sub-Millisecond Speed:** Achieves a **median CPU latency of 0.077 ms** (77 microseconds) — over 1,000x faster than cloud API calls.
- **Semantic Stems & Synonyms:** Deep multilingual keyword expansion handling code-mixed transliteration (e.g. *dast*, *virochanalu*, *bukhar*, *jwaram*, *pillalu*, *bacche*, *kadupulo*, *paalu*, *doodh*, *khoon*).

### 4. Dual-Tier Clinical Presentation & Dynamic Formatter (NEW in v1.5)
- **The Problem Solved:** Raw protocol data contained messy markdown table formatting (`|---|---|`, pipes `|`) that cluttered small mobile screens.
- **The Senior-Developer Solution (Zero Data Tampering):** All canonical protocol files remain 100% pristine and unaltered. A dedicated runtime parser (`ClinicalTextFormatter.kt`) dynamically converts complex multi-column clinical tables into clean, readable key-value bullet points.
- **Dual-Tier Layout:**
  - **Tier 1 (Instant Salient Answer):** Highlights the concise core guideline ($\le 380$ characters with leaf citation, e.g. `(CHD-1.2)`), exactly matching national evaluation standards.
  - **Tier 2 (Expandable Protocol Points):** A dedicated toggle button (`[ 📖 View Detailed Protocol Points (X) ▾ ]`) positioned between the short answer and citation card. When tapped, it smoothly expands into organized clinical bullet points (`•`) without table garbage or duplicating the summary.

### 5. Algorithmic Household Visit Engine & 48-Hour Priority Scheduling
- **Mechanism:** Ingests local household registers (MCP Cards, EDD, child immunization history, danger flags).
- **Triage Hierarchy:**
  - **Priority 1 (Emergency):** Danger sign alerts & 48-hour post-emergency re-visits.
  - **Priority 2 (High Risk):** Overdue ANC checkups, overdue infant vaccines (e.g. pentavalent/measles > 28 days), missed HBNC visits.
  - **Priority 3 (Routine Care):** Scheduled routine visits due in the current week.
- **Benchmark Performance:** **100% Precision, 100% Recall, 1.0 F1-Score, and 100% Priority Ranking Accuracy** across 39 evaluation beneficiaries.
- **1-Tap 48h Re-Visit Feature:** Tapping "Schedule 48h Re-Visit" on any emergency search result automatically promotes that beneficiary to **Priority 1** in the Visits tab with a prominent red badge.

### 6. Emergency Referral Slip & 1-Tap 2G SMS Dispatch
- **1-Tap Emergency Referral Slip:** Generates an official, pre-filled emergency ticket citing the beneficiary details, identified danger sign, MoHFW protocol, and required emergency action.
- **Offline SMS Dispatch:** Formats an urgent SMS digest for the 108 ambulance service or ANM without requiring mobile internet.
- **1-Tap End-of-Day PHC Summary:** Compiles daily completed visits and flagged cases into an offline SMS digest transmissible via basic 2G cellular network, eliminating double-entry paperwork.

### 7. Trilingual Localization & Daylight-Readable UI
- **Languages:** Full native interface and protocol translations in **English**, **Hindi (हिन्दी)**, and **Telugu (తెలుగు)**.
- **Daylight Themes:** High-Contrast Dark and Light modes engineered for direct sunlight visibility during rural field visits.
- **Offline Speech-to-Text:** Hands-free voice search mic allowing workers to speak symptoms while walking between households.

### 8. Offline Clinical Protocol Handbook (Tab 3: Protocols)
- **Directory of 107 Protocols:** Complete offline reference catalog of all indexed MoHFW/NHM guidelines for frontline self-study, sector review revision, and judge verification.
- **Clean Category Filtering (Zero Emojis):** Instant filtering across Child Health (`CHD`), Universal Immunization (`IMM`), Maternal & Antenatal Care (`ANC`/`PNC`), Emergency Danger Signs (`ESC`), and Non-Prescribing Scope Limits (`SCP`).
- **Interactive Formatting & 1-Tap Query:** Every protocol is dynamically formatted into clean bullet points with official citations and includes a 1-tap "Query Protocol" button to test in Ask Sahaaya.

---

## 📊 Measured Benchmark Data (From Official `score.json`)

| Evaluation Metric | Measured Benchmark Value | Evaluation Gate / Requirement | Status |
| :--- | :---: | :---: | :---: |
| **Primary Overall Score** | **0.8769 (87.7%)** | — | **HIGH RANKING** |
| **Escalation Gate Condition** | **PASS** | Must Pass Gate | **PASSED** |
| **Danger Sign Escalation Recall** | **1.0 (100.0%)** | 100% Required | **PERFECT (10/10)** |
| **False Escalation Rate** | **0.0 (0.0%)** | $< 5\%$ Target | **ZERO FALSE PANIC** |
| **Scope Refusal Accuracy** | **0.9000 (90.0%)** | High Accuracy | **PASSED** |
| **Visit List Triage F1-Score** | **1.0000 (100.0%)** | $> 90\%$ Target | **PERFECT (39/39)** |
| **Visit List Precision & Recall** | **1.0 / 1.0 (100%)** | $> 90\%$ Target | **PERFECT** |
| **Visit Priority Ranking Accuracy** | **1.0000 (100.0%)** | $> 90\%$ Target | **PERFECT** |
| **Median CPU Response Latency** | **0.077 ms (77 µs)** | $< 500\text{ ms}$ | **SUB-MILLISECOND** |
| **Total Model Footprint** | **0.35 MB** | $< 100\text{ MB}$ | **ULTRA-LIGHT (<500 KB)** |
| **Peak Runtime RAM** | **42.5 MB** | $< 500\text{ MB}$ | **RUNS ON 1GB PHONES** |
| **Network Permissions Used** | **0 (`INTERNET` omitted)** | 100% Offline | **100% AIRPLANE MODE** |
| **Standalone APK Size** | **16.8 MB** | Portable | **SHARE VIA BLUETOOTH** |

---

## 🎯 Grounded Official Protocol Citations
Every recommendation in Sahaaya references official Government of India health authorities:
1. **MoHFW Revised India MCP Card (2018):** Maternal danger signs, 4 ANC visits, IFA/Calcium schedules.
2. **MoHFW HBNC & HBYC Guidelines:** Home-based newborn visit schedule (Days 3, 7, 14, 21, 28, 42).
3. **National Universal Immunization Programme (UIP):** Birth vaccines (BCG, OPV-0, Hep-B), pentavalent, MR-1, vitamin A.
4. **MoHFW STOP Diarrhoea Initiative:** Zinc for 14 days, age-stratified ORS dosage.
5. **ASHA Induction Training Modules 6 & 7:** Paramedical scope, danger signs, non-prescribing boundaries.

---

## 🔮 Roadmap & Future Vision
- **On-Device Quantized SLM Rephraser:** Introduce lightweight 0.5B on-device models (e.g. SmolLM / Qwen2.5-0.5B via ONNX Runtime / MediaPipe) for natural conversational dialogue *after* deterministic grounding.
- **National ABHA / RCH 2-Way Sync:** Automated cryptographically signed sync when the worker enters an ANM Sub-Centre Wi-Fi zone.
- **ASHA Peer-to-Peer Mesh:** Share household updates across neighboring villages using Android Wi-Fi Direct without internet.

---

## 📋 Recommended 10-Slide Deck Structure for Claude
Copy and paste this section into Claude to generate your presentation slides:

```markdown
Prompt to give Claude:
"Please generate a complete 10-slide PowerPoint presentation with slide titles, bullet points, key metrics, and speaker notes for a national health-tech hackathon pitch using the following detailed brief about Sahaaya:

Slide 1: Title & Vision — Sahaaya: Frontline Clinical Companion for Low-Connectivity Areas
Slide 2: The Crisis on the Ground — 10.29 Lakh ASHAs, Zero-Signal Villages & Maternal Haemorrhage
Slide 3: Why Existing AI Fails — Cloud Latency, Hallucinations & Prescribing Malpractice
Slide 4: Sahaaya's Safety-First Architecture — The 3-Tier Defensive Engine
Slide 5: Clinical Safety & Protocol Retrieval — 100% Danger Recall & Sub-Millisecond BM25
Slide 6: Dynamic Dual-Tier UI — Instant Salient Answers + Clean Expandable Protocol Breakdowns
Slide 7: Household Register Triage — 100% F1 Algorithmic Visit Prioritization & 48h Escalations
Slide 8: Frontline Innovations — 1-Tap 108 Referral Slip, 2G SMS PHC Digest & Offline Voice
Slide 9: Verified Benchmark Proof — 87.7% Score, 0.077ms Latency, 42.5MB RAM, 100% Offline
Slide 10: Scalability, Impact & The Path to 10 Lakh ASHAs Across India"
```
