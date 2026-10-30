# Sahaaya Implementation Audit & Technical Review
**Date:** 2026-10-30  
**Project:** Sahaaya (సహాయ / सहाय) — Frontline Health-Worker Clinical Companion for Low-Connectivity Areas  
**Target Roles:** ASHA workers, ANM workers, Medical Officers  
**Challenge Context:** Talent AP Mega Hackathon 2026 — Problem H5  

---

## 1. Executive Summary

This audit provides a comprehensive technical review of the existing Sahaaya codebase (`android-app`, `assets`, `data`, and `scripts`), contrasting the current demonstration prototype with the authoritative **H5 Challenge Package v1.0** requirements and the official evaluation suite (`evaluate.py`).

The existing application provides a clean Compose Multiplatform UI foundation and establishes the core architectural sequence:
$$\text{Deterministic Danger Engine} \longrightarrow \text{Intent/Scope Gate} \longrightarrow \text{Protocol Retrieval} \longrightarrow \text{Grounded Answer}$$
However, the underlying data corpus, retrieval normalizer, danger-sign coverage, and visit list engine were heavily simplified for an initial 30-question, 33-member demonstration.

---

## 2. Component-by-Component Audit Matrix

| Component | Current Implementation | Identified Problem | Proposed Modification | Classification | Regression Risk |
|:---|:---|:---|:---|:---:|:---:|
| **Protocol Corpus & Storage** | 8 chunks in `assets/protocol_chunks.json` (9.1 KB). Regex-based JSON parser in `DataLoader.kt`. | Missing 99 protocol leaf sections out of the 107 official sections. Official evaluation requires exact leaf IDs (`ANC-1.2`, `ESC-2.3`, `NUT-1.2`, etc.). Brittle regex JSON parser breaks on nested strings. | Ingest all 9 official Markdown protocols (`01_ANC` through `09_VIS`) into 107 canonical leaf sections with title, text, sourceDoc, pageNum, versioning metadata, and content hash. Replace regex parser with robust JSON parser. | **Critical** | Low |
| **Danger-Sign Rule Engine** | 27 rules in `assets/danger_rules.json`. Normalization strips all non-ASCII characters (`[^a-z0-9\s]`). Substring/token matching without negation context. | Strips Devanagari/Telugu script characters. Causes 4 of 10 must-escalate questions to FAIL in dev evaluation (60% recall), failing the two-sided escalation gate and halving the primary score. False escalation on negated queries ("no bleeding"). | Expand to comprehensive structured danger rules covering all 31 ESC sections. Retain Indic unicode characters. Implement contextual negation detection ("nahi", "not", "no"). Guarantee 100% escalation recall while maintaining false-escalation rate $\le 20\%$. | **Critical** | Medium |
| **Intent & Scope Gate** | Hardcoded keyword lists in `IntentGate.kt` for antibiotics and dosages with a few ASHA kit exemptions (IFA, ORS, zinc). | Missing official out-of-scope categories from `08_SCP` (lab tests, government admin/app login, diseases outside scope like dengue, rabies, covid). Can fail when emergency queries mention medicines. | Implement formal classification adhering to `08_SCP_scope_and_redirect.md`. Enforce that life-threatening emergencies (`ESC`) ALWAYS take precedence over scope refusals. Provide clinically accurate redirect messages. | **High** | Low |
| **Retrieval & Normalization** | Pure Kotlin BM25 with 21 stopwords and ~20 synonym mappings. Extractive top-1 chunk. | Fails on code-mixed queries (Hindi, Hinglish, Marathi, Telugu) where colloquial phrases differ from protocol vocabulary. Citation accuracy only 35.3% on dev data. Key facts often truncated or displaced beyond the 400-char evaluator boundary. | Multilingual clinical normalizer mapping vernacular terms (`pasli chalna`, `jhatke`, `virochanalu`, `dast`, `taap`) to protocol concepts. Reciprocal rank fusion (RRF) top-k candidates. Answer construction guaranteeing citations and key facts within the first 400 characters. | **High** | Low |
| **Household Register & Visit Engine** | `VisitListEngine.kt` uses a 33-row synthetic CSV (`register.csv`) with pre-computed due dates and danger flags. | Does not implement `09_VIS_daily_visit_list_rules.md`! Cannot process the 500-household, 2,234-member `household_register.csv`. Lacks ANC windows, Td schedules, HRP follow-up, 20 childhood vaccines, HBNC visit days, MUAC checks, NCD follow-up, and Priority 1-3 tiers. | Implement complete challenge-compliant Visit List engine covering all 18 rules of `09_VIS`. Injectable date provider (`2026-10-26` for competition evaluation; system date for field operation). Process full 2,234-member register. | **Critical** | Medium |
| **Evaluation Harness & Scorer** | Prototype had custom 15-test JVM script in `CoreLogicTest.kt`. | Disconnected from official `evaluate.py`. Did not output `H5_answers.jsonl`, `H5_visit_list.csv`, or `H5_footprint.json`. | Integrate complete end-to-end evaluation harness matching `evaluate.py`. Verify all 40 dev questions and generate valid submission files. | **Critical** | Low |
| **Nearby Facilities Directory** | Conceptual UI dialogs only. | Frontline workers cannot look up verified referral destinations offline based on protocol referral requirements (Sub-centre, PHC, CHC, DH). | Local offline healthcare facility directory with verified Chandrapuri block / AP facilities, facility type mapping, manual village selector, and offline referral guidance. | **High** | Low |
| **Emergency SOS Flow** | Emergency card in `AskScreen.kt` with basic SMS referral slip. | No persistent offline audit log; no confirmation of transmission; lacks direct 108/102 emergency call integration. | Dedicated Emergency SOS action flow: 1-tap call intent to emergency services (108/102), local SQLite/Room event logging, offline persistent referral slip creation, and clear connection status. | **High** | Low |
| **Condition-Based Criticality & Alerts** | Basic priority badge in visit list. | No decoupled criticality assessment engine that feeds local ASHA notifications and dynamic visit list reordering. | Protocol-driven criticality engine outputting immediate referral, same-day assessment, scheduled follow-up, or routine monitoring. Feeds local priority alerts. | **High** | Low |
| **Patient Advisory Sharing** | Read-only protocol text on screen. | Workers cannot share simple, patient-friendly advice in regional languages without disclosing sensitive clinical notes. | Grounded Patient Advisory module generating simplified, protocol-grounded advice with one-tap Android native share intent (`ACTION_SEND`). | **Medium** | Low |
| **Follow-up Scheduling** | Pre-calculated static CSV dates. | Workers cannot record field observations, schedule protocol-defined revisit dates, or track visit completion. | Follow-up scheduling engine using protocol intervals (HBNC days, 30-day MUAC, 30-day BP/sugar, ANC windows) with offline persistence. | **High** | Low |
| **Offline Persistence & Sync Queue** | In-memory parsing on every app launch. | No local database or transactional sync queue for field updates when disconnected for hours. | Encrypted local storage layer (AES-256 / SQLCipher or encrypted SharedPreferences) and idempotent offline sync queue with status tracking (`PENDING`, `SYNCED`, `FAILED`). | **High** | Low |

---

## 3. Finding Classifications

- **Critical Findings (Safety & Evaluation Gate):**
  1. Protocol chunk corpus contains only 8 chunks instead of all 107 official leaf sections.
  2. Danger rule engine misses 40% of development escalation cases and strips non-Latin scripts.
  3. Visit list engine lacks all `09_VIS` rules and fails on the 500-household register.
  4. Absence of official evaluation output format (`H5_answers.jsonl`, `H5_visit_list.csv`).

- **High-Priority Findings (Core Functionality & Field Workflow):**
  1. Multilingual code-mixed query understanding for Indic languages.
  2. Strict alignment of Intent Gate with `08_SCP` scope protocol.
  3. Verified offline healthcare facility directory and destination recommender.
  4. Emergency SOS action flow with offline audit logging.
  5. Priority notifications and dynamic daily visit list reordering.
  6. Secure offline persistence and synchronization event queue.

- **Medium-Priority Findings (Usability & Polish):**
  1. Patient advisory generation and native sharing.
  2. Revisit and follow-up scheduling engine.
  3. Deprecated icon warnings in Jetpack Compose UI.

---

## 4. Architectural Verification

The target architecture enforces safety and zero-cloud dependence:

```
[ Frontline Worker Input: Text / Speech ]
                  │
                  ▼
┌────────────────────────────────────────────────────────┐
│ 1. MULTILINGUAL NORMALIZATION & SCRIPT PRESERVATION    │
│    Preserves Devanagari, Telugu, Latin scripts.        │
│    Normalizes code-mixed terms to clinical concepts.   │
└─────────────────────────┬──────────────────────────────┘
                          │
                          ▼
┌────────────────────────────────────────────────────────┐
│ 2. DETERMINISTIC DANGER-SIGN ENGINE                    │
│    * ALWAYS FIRES FIRST *                              │
│    Context-aware regex, negation filter, 31 ESC rules. │
└─────────────────────────┬──────────────────────────────┘
                          │
             ┌────────────┴────────────┐
       [Danger Detected]         [No Danger]
             │                         │
             ▼                         ▼
┌─────────────────────────┐  ┌───────────────────────────┐
│ 🚨 IMMEDIATE REFERRAL   │  │ 3. INTENT & SCOPE GATE    │
│ • Emergency SOS trigger │  │    Refuses prescription,  │
│ • Local Referral Slip   │  │    diagnosis, non-kit dose│
│ • 108 / 102 Call Intent │  └─────────────┬─────────────┘
└─────────────────────────┘                │
                             ┌─────────────┴─────────────┐
                       [Out of Scope]               [In Scope]
                             │                           │
                             ▼                           ▼
                  ┌──────────────────────┐  ┌──────────────────────────┐
                  │ ⚠️ DOCTOR REDIRECT   │  │ 4. HYBRID RETRIEVAL      │
                  │ Clear clinical scope │  │    BM25 + Semantic Map   │
                  │ refusal (SCP-1.2)    │  │    Top-k candidates (RRF)│
                  └──────────────────────┘  └─────────────┬────────────┘
                                                          │
                                            ┌─────────────┴────────────┐
                                   [Confident Match]           [Low Evidence]
                                            │                          │
                                            ▼                          ▼
                                 ┌──────────────────────┐   ┌──────────────────┐
                                 │ ✅ GROUNDED ANSWER   │   │ ℹ️ SAFE FALLBACK │
                                 │ • Strict Leaf ID cite│   │ "Not in protocol,│
                                 │ • Key facts in <400c │   │  ask ANM/Doctor" │
                                 └──────────────────────┘   └──────────────────┘
```

---

## 5. Next Steps

With this audit established, we proceed immediately to Phase 1: recording the empirical baseline results across both the Python challenge package and the Android application.

