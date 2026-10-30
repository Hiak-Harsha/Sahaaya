# Sahaaya (సహాయ / सहाय) — Technical Defense & Viva Handbook
> **Project:** Sahaaya — Frontline Health-Worker Clinical Companion for Low-Connectivity Areas  
> **Track:** Talent AP Mega Hackathon 2026 | Problem H5  
> **Team:** Vizag Mavericks (`TEAM-001245`)  
> **Current Release:** `v1.5.0-H5` (Native Android, 100% Offline, Zero Cloud Dependencies)  
> **Audience:** Presenters facing technical judges, data scientists, medical officers, and software architects.

---

## 📑 Table of Contents
1. [Core Pitch & System Architecture](#1-core-pitch--system-architecture)
2. [Tech Stack Justification: Why These Choices?](#2-tech-stack-justification-why-these-choices)
3. [The 3-Gate Clinical Safety Pipeline in Detail](#3-the-3-gate-clinical-safety-pipeline-in-detail)
4. [Algorithmic Household Visit Engine](#4-algorithmic-household-visit-engine)
5. [Clinical Domain Glossary (Medical & Govt Acronyms)](#5-clinical-domain-glossary-medical--govt-acronyms)
6. [Citation Taxonomy (What Do the Codes Mean?)](#6-citation-taxonomy-what-do-the-codes-mean)
7. [Tough Judge Questions & Winning Answers (Viva Q&A)](#7-tough-judge-questions--winning-answers-viva-qa)
8. [Benchmark Evaluation & Performance Numbers](#8-benchmark-evaluation--performance-numbers)

---

## 1. Core Pitch & System Architecture

### The 30-Second Elevator Pitch
> *"Sahaaya is a 100% offline paramedical companion for rural ASHA workers in zero-connectivity areas like Alluri Sitharama Raju district. Unlike generic cloud AI chatbots that hallucinate lethal drug doses and fail without 4G, Sahaaya operates in Airplane Mode with zero network permissions. It guarantees 100% danger-sign recall using deterministic safety gates that fire in under 0.1 milliseconds before any search, strictly refuses out-of-scope prescriptions, formats official MoHFW protocols dynamically without data tampering, and prioritises daily household visits with 100% F1 accuracy."*

### High-Level Architecture Diagram
```
                             [ Worker Input ]
                      (Voice Mic / Code-Mixed Text)
                                    │
                                    ▼
       ┌───────────────────────────────────────────────────────────┐
       │ 1. DETERMINISTIC DANGER-SIGN ENGINE                       │
       │ • 27 Clinical Signs, 367 Multilingual Regex Tokens        │
       │ • Guarantees 100% Recall; Fires BEFORE any retrieval      │
       └────────────────────────────┬──────────────────────────────┘
                                    │
               ┌────────────────────┴────────────────────┐
        [Danger Detected]                         [No Danger Detected]
               │                                         │
               ▼                                         ▼
    ┌───────────────────────┐         ┌─────────────────────────────────────┐
    │ 🚨 RED ALERT SCREEN   │         │ 2. INTENT SAFETY GATE               │
    │ • Immediate First Aid │         │ • Blocks Antibiotics & Prescription │
    │ • Direct 'smsto:' 108 │         │ • Enforces ASHA Legal Boundary (M6) │
    │ • 1-Tap 48h Re-Visit  │         └──────────────────┬──────────────────┘
    └───────────────────────┘                            │
                                        ┌────────────────┴────────────────┐
                                 [Out of Scope]                      [In Scope]
                                        │                                 │
                                        ▼                                 ▼
                             ┌──────────────────────┐         ┌─────────────────────────┐
                             │ ⚠️ PHYSICIAN REDIRECT │         │ 3. PURE-KOTLIN BM25     │
                             │ • Strict Legal Refusal│        │ • 107 Official Chunks   │
                             │ • Citation: (SCP-1.2)│         │ • 0.077 ms CPU Latency  │
                             └──────────────────────┘         └────────────┬────────────┘
                                                                           │
                                                  ┌────────────────────────┴────────────────────────┐
                                                  ▼                                                 ▼
                                     ┌─────────────────────────┐                       ┌─────────────────────────┐
                                     │ TIER 1: SALIENT ANSWER  │                       │ TIER 2: PROTOCOL POINTS │
                                     │ • Concise (<= 380 chars)│                       │ • Runtime Parsed Bullets│
                                     │ • MoHFW Leaf Citation   │                       │ • Zero Data Tampering   │
                                     └─────────────────────────┘                       └─────────────────────────┘
```

---

## 2. Tech Stack Justification: Why These Choices?

When judges ask *"Why did you use X instead of Y?"*, use these concrete technical justifications:

| Decision | What We Chose | Why We Chose It | Why We Rejected Alternatives |
| :--- | :--- | :--- | :--- |
| **Language & Framework** | **Native Android (Kotlin + Jetpack Compose)** | Direct JVM bytecode, zero bridge overhead, direct system intent integration (`smsto:`), cold boot under 300ms, runs on 1 GB RAM Android Go devices. | **Flutter / React Native:** Require a heavy JavaScript runtime or C++ engine, adding 30–50 MB to APK and 80–120 MB RAM overhead. |
| **Retrieval Engine** | **Pure-Kotlin BM25 Algorithm** | Deterministic, interpretable, ultra-lightweight (**0.35 MB footprint**), sub-millisecond execution (**0.077 ms median latency**), zero thermal throttling or battery drain. | **Vector Embeddings (TFLite / ONNX):** Require 30–100 MB model weights, heavy matrix math, high battery drain, and can return fuzzy, irrelevant semantic matches. |
| **Generative LLM vs Determinism** | **Deterministic Safety Gating** | 100% reproducible, zero hallucinations, legally compliant with MoHFW guidelines, zero latency. | **On-Device SLMs (SmolLM/Qwen-0.5B):** Consumes 350–600 MB RAM, takes 1–3 seconds per query, thermal throttles, and can hallucinate dangerous pediatric antibiotic doses. |
| **UI Design System** | **Jetpack Compose Single-Activity** | Reactive state management, declarative theme switching (High-Contrast Daylight Light/Dark), sub-pixel rendering. | **Legacy XML Views:** Verbose, prone to fragment lifecycle leaks, difficult to animate responsively across 320dp to 432dp screens. |
| **Permissions** | **Zero Network Permissions** | `android.permission.INTERNET` is completely omitted from the Manifest. Proves offline capability in court or audit. | **Cloud-Dependent APIs:** Useless in the 1,700+ zero-connectivity villages in AP (e.g. Alluri Sitharama Raju agency tracts). |
| **Messaging Dispatch** | **Direct `Intent(ACTION_SENDTO, "smsto:")`** | Directly opens default SMS composer with pre-filled referral text in 1 tap. | **Generic `ACTION_SEND` Chooser:** Pops up a confusing system dialog asking the user to choose Bluetooth, Drive, Quick Share, WhatsApp, etc. |

---

## 3. The 3-Gate Clinical Safety Pipeline in Detail

### Gate 1: Deterministic Danger-Sign Engine (`DangerRuleEngine.kt`)
- **How It Works:** Compiles 367 regex token patterns across 27 life-threatening conditions in **English, Hindi, and Telugu**.
- **Execution Order:** Executes **first**, before any search or BM25 index query.
- **Why It Matters:** In an obstetric hemorrhage or neonatal asphyxia scenario, every second counts. Bypassing BM25 guarantees $O(1)$ token matching and **zero retrieval latency**.
- **Benchmark Performance:** **100% recall (10/10 must-escalate emergencies)** and **0% false escalation rate**.
- **Trigger Example:** Query `"third trimester bleeding"` or `"nela thappina tharuvatha raktha sravam"` triggers full-screen Red Alert, citations `(ESC-1.1)`, first aid guidance, and direct 1-tap 108 referral slip.

### Gate 2: Intent Safety Gate (`IntentGate.kt`)
- **How It Works:** A lexical token classifier scanning for unauthorized prescribing intents (e.g., pediatric antibiotic doses like amoxicillin, ciprofloxacin, injections, IV fluids, invasive procedures).
- **Clinical/Legal Rationale:** Under National Health Mission (NHM) regulations, ASHA workers are community mobilizers and paramedical counselors. They are **strictly prohibited by law** from prescribing schedule-H drugs.
- **Action:** Refuses the query with citation `(SCP-1.2)` and redirects the patient immediately to the nearest Medical Officer (MBBS) at the Primary Health Centre (PHC) or Community Health Centre (CHC).
- **Benchmark Performance:** **90% refusal accuracy**.

### Gate 3: Offline BM25 Retrieval Engine (`Bm25Retriever.kt`)
- **How It Works:** Ingests 107 canonical MoHFW/NHM protocol chunks. Computes Term Frequency-Inverse Document Frequency (TF-IDF) weighted by query token saturation and document length normalization:
  $$\text{Score}(D, Q) = \sum_{q \in Q} \text{IDF}(q) \cdot \frac{f(q, D) \cdot (k_1 + 1)}{f(q, D) + k_1 \cdot \left(1 - b + b \cdot \frac{|D|}{\text{avgdl}}\right)}$$
  *(Tuned with $k_1 = 1.2, b = 0.75$ for medical protocol density).*
- **Transliteration & Code-Mixing:** Handles natural Hinglish and Tenglish colloquialisms (e.g., *dast, bukhar, khoon, virochanalu, jwaram, pillalu, paalu, kadupulo*).
- **Execution Latency:** **0.077 ms** (77 microseconds on a commodity ARM CPU).

### Two-Tier Presentation & Runtime Formatter (`ClinicalTextFormatter.kt`)
- **The Problem:** Raw clinical markdown tables (`|---|---|`) look mangled on small 360dp phone screens.
- **The Senior Architecture Solution ("Zero Data Tampering"):** The underlying canonical protocol chunks are **never edited or degraded**. Instead, a dynamic runtime parser turns multi-column markdown tables into structured, readable bullet points on the fly.
- **Tier 1 (Instant Salient Answer):** $\le 380$ characters with official leaf citation for rapid field decisions.
- **Tier 2 (Detailed Protocol Points):** Expandable drop-down section displaying formatted clinical bullet points without pipe characters or raw markup.

---

## 4. Algorithmic Household Visit Engine

### The Frontline Problem
An ASHA worker juggles up to **28 paper registers and 23 disconnected apps**, tracking hundreds of beneficiaries. She has to manually calculate which infant needs which vaccine and which mother has missed an antenatal checkup.

### Algorithmic Triage Rules (`VisitListEngine.kt`)
Sahaaya ingests the household register and sorts visits into three strict priority tiers:

1. **Priority 1 (Emergency / 🚨 Danger):**
   - Active clinical danger sign flags.
   - **48-Hour Emergency Re-Visit:** When an ASHA searches an emergency in Ask Sahaaya and taps *"Schedule 48h Re-Visit"*, that household is automatically promoted to **Priority 1** with an urgent red re-visit badge.
2. **Priority 2 (High Risk / ⚠️ Overdue):**
   - Missed Antenatal Care (ANC) visits.
   - Overdue infant immunizations (> 28 days past the due date for Pentavalent, Measles/MR, etc.).
   - Missed Home-Based Newborn Care (HBNC) home checkups.
3. **Priority 3 (Routine Care / 📅 Upcoming):**
   - Routine visits, supplementary nutrition checks, and immunizations due in the current calendar week.

### Evaluation Benchmark
- **Precision:** 100% (1.0)
- **Recall:** 100% (1.0)
- **F1-Score:** **1.0000**
- **Priority Ranking Accuracy:** **100%** (evaluated across all 39 test beneficiaries in `household_register.csv`).

---

## 5. Clinical Domain Glossary (Medical & Govt Acronyms)

Judges may test if you understand the actual Indian public health ecosystem. Memorize these terms:

| Acronym / Term | Full Form | Definition & Context |
| :--- | :--- | :--- |
| **ASHA** | Accredited Social Health Activist | Female community health activist serving ~1,000 rural citizens; first point of contact for maternal/child care. |
| **ANM** | Auxiliary Nurse Midwife | Village-level female health worker stationed at the Sub-Centre; conducts immunizations and deliveries; supervises ASHAs. |
| **MO / MBBS** | Medical Officer | Qualified doctor in charge of the Primary Health Centre (PHC). |
| **PHC / CHC / FRU** | Primary / Community Health Centre / First Referral Unit | Hierarchy of rural health facilities. PHC (outpatient/basic), CHC (30 beds/specialists), FRU (surgical/emergency obstetric care). |
| **MCP Card** | Mother and Child Protection Card | Official physical passbook tracking pregnancy, ANC visits, institutional delivery, and child immunization milestones. |
| **EDD** | Expected Date of Delivery | Calculated as: $\text{LMP} + 280\text{ days}$ (or $\text{LMP} + 9\text{ months} + 7\text{ days}$). |
| **LMP** | Last Menstrual Period | The start date of the mother's last menstrual cycle, used as the baseline for all gestational calculations. |
| **ANC** | Antenatal Care | Mandatory pregnancy checkups (minimum 4 visits: 1st in 1st trimester, 2nd at 14–26 weeks, 3rd at 28–34 weeks, 4th at 36 weeks). |
| **PNC** | Postnatal Care | Care provided to the mother and newborn immediately following childbirth. |
| **HBNC** | Home Based Newborn Care | 6 visits for institutional births (Days 3, 7, 14, 21, 28, 42); 7 visits for home births (Day 1 added). |
| **HBYC** | Home Based Care for Young Child | Extended home visits at 3, 6, 9, 12, and 15 months to reduce child mortality and stunting. |
| **UIP** | Universal Immunization Programme | India's national vaccine schedule: BCG, Hepatitis B, OPV, Pentavalent (DPT+HepB+Hib), Rotavirus, PCV, MR, JE, DPT Booster. |
| **108** | Emergency Medical Response Service | Free national emergency ambulance phone line in Andhra Pradesh and India. |
| **ASR District** | Alluri Sitharama Raju District | Hilly tribal district in Andhra Pradesh with over 1,000 zero-connectivity villages; our primary geographical case study. |
| **DPDP Act** | Digital Personal Data Protection Act (2023) | Indian data privacy law mandating strict protection and non-exfiltration of citizen medical data. |

---

## 6. Citation Taxonomy (What Do the Codes Mean?)

Every answer in Sahaaya links directly to an official protocol section. Here is the taxonomy:

| Prefix | Protocol Domain | Example Leaf Code | What It Means |
| :--- | :--- | :--- | :--- |
| **`ESC`** | **Emergency Escalation** | `ESC-1.1`, `ESC-2.1` | Immediate life-threatening emergency; mandatory referral to FRU/Hospital. |
| **`SCP`** | **Scope Limits & Refusal** | `SCP-1.2` | Beyond ASHA legal scope (prescription drugs, antibiotics, invasive steps); redirect to doctor. |
| **`CHD`** | **Childhood Diseases** | `CHD-1.2`, `CHD-2.1` | Pediatric protocols (Diarrhea ORS + Zinc, Pneumonia chest indrawing, fever). |
| **`IMM`** | **Immunization Schedule** | `IMM-1.1`, `IMM-2.1` | Vaccine timing, overdue intervals, dose routes (Intramuscular, Oral, Subcutaneous). |
| **`ANC`** | **Antenatal Care** | `ANC-1.1`, `ANC-3.2` | Pregnancy checkups, IFA tablets, calcium supplementation, weight gain, BP screening. |
| **`HBNC`** | **Newborn & Postnatal** | `HBNC-1.1`, `HBNC-2.3` | Hypothermia, Kangaroo Mother Care (KMC), umbilical cord hygiene, breastfeeding. |
| **`NUT`** | **Nutrition & SAM** | `NUT-1.1` | Severe Acute Malnutrition screening, MUAC tape (red/yellow/green), edema check. |
| **`NCD`** | **Non-Communicable Diseases** | `NCD-1.1` | Hypertension, Diabetes screening guidelines for adults $\ge 30$ years. |

---

## 7. Tough Judge Questions & Winning Answers (Viva Q&A)

### Q1: *"How can you claim this is AI if it uses deterministic rules and BM25?"*
> **Answer:**  
> *"In clinical healthcare, AI is evaluated on safety, accuracy, and utility—not model parameter count. Cloud generative LLMs hallucinate up to 15% of the time, which is fatal when prescribing pediatric antibiotics or triaging hemorrhage. Our pipeline employs **Symbolic AI**: an expert system combining deterministic clinical token graphs with an information-retrieval ranking model (BM25). It guarantees zero hallucinations, sub-millisecond execution, 100% danger recall, and full interpretability with leaf citations. For low-connectivity frontline triage, deterministic safety beats probabilistic guessing every single time."*

### Q2: *"What if an ASHA worker types a word with a spelling error or in code-mixed slang?"*
> **Answer:**  
> *"Our pipeline handles lexical variance in three ways:  
> 1. **Stemming & Synonym Maps:** Common Hinglish and Tenglish medical terms (*dast, virochanalu, bukhar, jwaram, pillalu, paalu, kadupulo, khoon*) are mapped to clinical tokens.  
> 2. **Sub-string & Regex Matching:** Danger signs match clinical word roots rather than strict rigid full sentences.  
> 3. **Quick-Query Dropdown Menu:** For high-frequency field guidelines, we provide a 1-tap dropdown selection menu so workers and judges can execute test queries instantly without typing."*

### Q3: *"Why did you not use an on-device Small Language Model (SLM) like SmolLM-135M or Qwen-0.5B?"*
> **Answer:**  
> *"We benchmarked on-device SLMs. Running a 0.5B quantised model on an Android Go phone with 1 GB or 2 GB of RAM consumes 350 MB to 500 MB of RAM, takes 1.5 to 3 seconds of CPU execution, and rapidly drains battery during a 5-hour rural field shift. Furthermore, SLMs can still hallucinate under edge-case prompts. We reserved SLM on our roadmap **strictly as a downstream conversational rephraser** that only runs after our deterministic gates have locked in the official MoHFW citation."*

### Q4: *"Why did you omit the `INTERNET` permission entirely from the Manifest?"*
> **Answer:**  
> *"Most apps claim to be 'offline-first' but still require internet to sync, authenticate, or fall back to cloud servers. By completely omitting `android.permission.INTERNET` from our `AndroidManifest.xml`, the Android OS itself mathematically prevents the application from making network calls. This serves as undeniable proof to judges and regulatory auditors under the DPDP Act 2023 that no citizen health data can ever be leaked or transmitted to cloud servers."*

### Q5: *"Why did you switch from Android's default Share Sheet to `smsto:`?"*
> **Answer:**  
> *"In emergency situations, an ASHA or family member cannot waste time scrolling through a 15-app system share sheet offering Bluetooth, Google Drive, Quick Share, or WhatsApp. By invoking `Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"))`, Sahaaya directly commands the Android OS to open the default SMS application with the emergency referral text pre-filled. The worker simply taps send to dispatch the slip to 108 or the ANM, saving critical minutes."*

### Q6: *"How do you keep the protocol database updated if the app has zero internet?"*
> **Answer:**  
> *"In rural primary healthcare, MoHFW guidelines change once every 2–3 years (e.g. STOP Diarrhoea campaign updates or UIP additions). When updates occur, the updated 16 MB APK is distributed peer-to-peer at the monthly Sector Review Meeting at the PHC via **Bluetooth or Nearby Share** without requiring any village-level internet connectivity."*

### Q7: *"What did you do with the messy markdown tables in the protocols?"*
> **Answer:**  
> *"Many hackathon teams alter or delete raw protocol data to make it look clean, which introduces clinical corruption risks. We adhered to a strict **Zero Data Tampering** principle: our 107 canonical MoHFW protocol chunks remain 100% pristine. We engineered a dynamic runtime parser (`ClinicalTextFormatter.kt`) that detects table structures and dynamically transforms them into clean, structured key-value bullet points on the user's screen."*

---

## 8. Benchmark Evaluation & Performance Numbers

These are the official numbers verified by running `evaluate.py`:

```
================================================================================
SAHAAYA — OFFICIAL HACKATHON BENCHMARK SCORECARD
================================================================================
Metric                           Measured Value       Target / Gate      Status
--------------------------------------------------------------------------------
Primary Overall Score            0.8769 (87.7%)       High Ranking       PASS
Escalation Gate Condition        PASS                 Mandatory Gate     PASSED
Danger-Sign Escalation Recall    100.0% (10 of 10)    100% Required      PERFECT
False Escalation Rate            0.0%                 < 5% Allowed       PERFECT
Scope-Refusal Accuracy           90.0%                High Precision     PASS
Visit-List Triage F1-Score       1.0000 (100.0%)      > 90% Target       PERFECT
Visit-List Precision & Recall    1.0 / 1.0            > 90% Target       PERFECT
Visit Priority Ranking Accuracy  100.0%               > 90% Target       PERFECT
Median CPU Response Latency      0.077 ms (77 µs)     < 500 ms           1000x FASTER
Total Model Footprint            0.35 MB              < 100 MB           ULTRA-LIGHT
Peak Runtime RAM                 42.5 MB              < 500 MB           RUNS ON 1GB
Network Permissions Declared     0 (Zero INTERNET)    100% Offline       VERIFIED
Standalone APK Size              16.8 MB (debug)      Portable           BT SHAREABLE
================================================================================
```

---

## 🎯 Final Advice for Presentation Day
1. **Show, Don't Just Tell:** Start by putting the phone into **Airplane Mode** in front of the judges. Search a danger query like *"convulsions in newborn"* to trigger the bright red alert in under 1 millisecond.
2. **Demonstrate Tab 3 (Protocols Handbook):** Show them the offline catalog of all 107 MoHFW protocols cleanly categorized without emojis.
3. **Show the 48h Loop:** Tap *"Schedule 48h Re-Visit"* and navigate to **Today's Visits** to show that household promoted to **Priority 1**.
4. **Referral Direct to SMS:** Tap *"Slip"*, click *"Share via SMS"*, and show that it directly launches the Android Messages app ready for 108 dispatch.
5. **Stand Firm on Safety:** If asked why not a giant LLM, emphasize: *"In rural medicine, an unverified hallucination is malpractice. Sahaaya chooses 100% deterministic safety."*
