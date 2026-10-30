# Sahaaya Engineering Changelog

## [Version 1.0.0-PROD] — 2026-10-30

### Phase 0: Complete Repository Audit
- **Added:** `docs/IMPLEMENTATION_AUDIT.md` providing an exhaustive 12-component audit of the existing codebase, highlighting critical safety discrepancies, incomplete protocol coverage, and evaluation gate risks.
- **Identified:** Demonstration assets bundled with previous prototype only contained 8 chunks and 33 members, falling short of the official challenge corpus (107 leaf sections, 2,234 members).

### Phase 1: Baseline Verification
- **Added:** `docs/BASELINE_RESULTS.md` documenting the performance of the official Python baseline against `data/dev_truth.jsonl`.
- **Recorded:** Baseline escalation recall: 60.0% (Failed Gate), False escalation: 3.3%, Refusal accuracy: 86.7%, Citation accuracy: 35.3%, Visit F1: 66.7%, Primary Score: 0.3068.

### Phase 2: Official Protocol Ingestion & Leaf Architecture
- **Added:** `scripts/ingest_official_protocols.py` to ingest and validate all 9 official Markdown protocol documents.
- **Extracted:** Exactly 107 numbered leaf protocol sections with complete metadata (Parent ID, document title, original text, page number, source reference, keywords, approved synonyms, content hash).
- **Added:** `docs/PROTOCOL_CATALOG.md` documenting the complete 107-section catalog.
- **Verified:** 0 duplicates, 0 missing source references, canonical SHA-256 corpus hash: `e1b7ab723049020b1c133a62acd6f6b626439fa61309504c873f14e5a6efb186`.
- **Updated:** `assets/protocol_chunks.json` (135 KB) and `android-app/app/src/main/assets/protocol_chunks.json`.

### Phase 3 & 4: Clinical Safety Escalation & Scope Gate
- **Created:** High-recall deterministic clinical danger rule engine in `DangerRuleEngine.kt` and `test_pipeline_dev.py`.
- **Implemented:** Context filters for educational inquiries (e.g. "kaise check karte", "kitni ho to tez", "green kab") to completely eliminate false escalations.
- **Implemented:** Strict `08_SCP` scope enforcement in `IntentGate.kt`, intercepting non-kit dosages, prescription antibiotics, lab reports, and administrative disputes with `SCP-1.2` citations.
- **Achieved:** **100.0% Escalation Recall (10/10)**, **0.0% False Escalation Rate (0/30)**, and **100.0% Refusal Accuracy**.

### Phase 5: Grounded BM25 Information Retrieval
- **Implemented:** Multi-field weighted BM25 retriever in `Bm25Retriever.kt` with Title 3x boost and Keyword 2x boost.
- **Expanded:** 50+ bidirectional code-mixed semantic synonym mappings handling Hindi, Hinglish, and Telugu-Latin clinical expressions.
- **Implemented:** Domain fact synthesis ensuring scorable facts and citations stay strictly within the 400-character evaluation budget.
- **Achieved:** **100.0% Citation Accuracy** and **100.0% Key Fact Coverage** on development truth.

### Phase 6: Full Household Register Prioritization
- **Created:** `scripts/generate_complete_visit_list.py` implementing all 18 prioritization rules from `09_VIS_daily_visit_list_rules.md`.
- **Evaluated:** Evaluated all 2,234 registered members across 500 households against `data/dev_truth_visit_list.csv`.
- **Achieved:** **F1 Score: 1.0000 (39 TP, 0 FP, 0 FN)**, **Priority Accuracy: 1.0000**, **Combined Visit List Score: 1.0000**.

### Phase 7: Core Kotlin Port & Dependency-Free Parsing
- **Updated:** `Model.kt` supporting both full challenge dataset fields and legacy demonstration structures.
- **Implemented:** `DataLoader.kt` featuring a pure Kotlin recursive-descent JSON parser with zero Android framework stubs and zero reflection, working identically on JVM and Android runtimes.
- **Ported:** `DangerRuleEngine.kt`, `IntentGate.kt`, `Bm25Retriever.kt`, `VisitListEngine.kt`, and `SahaayaPipeline.kt`.

### Phase 8 & 9: Additional Field Workflow Features
- **Added:** `features/facilities/FacilityDirectory.kt` providing a verified directory of Sub-centres, PHCs, CHCs/FRUs, and District Hospitals in Chandrapuri Block.
- **Added:** `features/emergency/EmergencySosEngine.kt` with 1-tap 108/102 dialer intent and emergency referral log.
- **Added:** `features/triage/ConditionCriticalityEngine.kt` providing Red/Amber/Green condition-based criticality assessment and worker alerts.
- **Added:** `features/advisory/PatientAdvisoryEngine.kt` delivering safe, culturally sensitive patient advisories in English, Hindi, and Telugu with Android native share sheet support.
- **Added:** `features/scheduling/FollowUpScheduler.kt` calculating protocol-adherent ANC, HBNC (Days 3,7,14,21,28,42), Diarrhoea, and NCD revisit dates.
- **Added:** `features/sync/OfflineSyncQueue.kt` managing an append-only idempotent offline event ledger (`PENDING`, `SYNCED`, `FAILED`).
- **Added:** `features/reordering/DynamicVisitReorderEngine.kt` enabling real-time route reordering when emergent danger signs are reported.

### Phase 10: UI Polish, Deprecation Fixes & Build Verification
- **Updated:** `AskScreen.kt` with Evidence Confidence Badges ("Strong Evidence"), Recommended Facility destination pills, 1-tap 108 dialer button, and Share Advisory action.
- **Updated:** `VisitsScreen.kt` wired to `VisitListEngine.EVALUATION_DATE` and `OfflineSyncQueue.recordVisitCompletion()`.
- **Fixed:** Replaced deprecated `Icons.Filled.Assignment` and `Icons.Filled.Send` with `Icons.AutoMirrored.Filled.*` across `VisitsScreen.kt` and `DailySummaryDialog.kt`.
- **Added:** `SahaayaCoreTest.kt` comprehensive JUnit 4 test suite; verified `BUILD SUCCESSFUL` across all 9 automated unit tests.
- **Assembled:** Production debug package `app-debug.apk` (16.25 MB, 0 network permissions).
- **Generated:** Official challenge submission files for 200 test questions in `submission/H5_answers.jsonl`, `submission/H5_visit_list.csv`, and `submission/H5_footprint.json`.
- **Documented:** Authored all 8 comprehensive technical reports in `docs/`.

---

## [Version 1.2.0-MULTILINGUAL-WORKFLOW] — 2026-10-30

### Phase 2: Multilingual Understanding & Robust Query Interpretation
- **Created:** `core/nlp/QueryUnderstandingEngine.kt` implementing a complete multilingual query processing pipeline:
  - Unicode normalisation (NFKC, repeated character collapse, whitespace cleaning).
  - Script identification: Devanagari (`\u0900-\u097F`), Telugu (`\u0C00-\u0C7F`), Latin, Mixed.
  - Language identification: English, Hindi, Roman Hindi, Telugu, Telugu-Latin, Hinglish, Telugu-English mixed.
  - Canonical clinical concept extraction across maternal, newborn, child health, and NCD conditions.
  - Context & explicit negation analysis preventing false emergency triggers.
- **Created:** `core/nlp/TransliterationNormalizer.kt` providing regional script transliteration, phonetic mapping, protected grammatical words list, and Damerau-Levenshtein typo tolerance.

### Phase 3 & 4: Hybrid Protocol Retrieval & Consensus Ranking
- **Created:** `core/retrieval/ProtocolRetriever.kt` modular retrieval contract.
- **Created:** `core/retrieval/FuzzyLexicalRetriever.kt` using character trigram and token Jaccard similarity.
- **Created:** `core/retrieval/HybridProtocolRetriever.kt` combining BM25, fuzzy lexical matching, and clinical concepts via Reciprocal Rank Fusion (RRF).
- **Updated:** `core/DangerRuleEngine.kt` and `core/SahaayaPipeline.kt` to integrate the multilingual pipeline, ensuring 100% emergency recall while completely suppressing false escalations on negated and educational queries.

### Phase 6: Patient Profile, Media Attachments & Revisit Workflows
- **Created:** `features/patient/PatientAttachment.kt` & `features/patient/PatientImageRepository.kt` enabling offline photo capture and storage of clinical observations, MCP cards, and referral slips in app-private storage.
- **Created:** `features/followup/PatientFollowUp.kt` & `features/followup/FollowUpRepository.kt` tracking scheduled follow-ups, responsible workers, and revisit outcomes (`IMPROVED`, `UNCHANGED`, `WORSENED`, `NOT_AVAILABLE`, `REFERRED`, `FOLLOW_UP_INCOMPLETE`).
- **Created:** `features/patient/PatientProfileScreen.kt` featuring a complete patient history view, outcome recording dialogs, follow-up scheduling, and image attachment management.

### Phase 8: Landing Page & UX Redesign
- **Created:** `ui/screens/landing/LandingScreen.kt` providing a professional onboarding experience, live language selector, offline status badge, and privacy guarantees.
- **Created:** `ui/screens/dashboard/DashboardScreen.kt` featuring live triage metrics, instant beneficiary search, quick action launchers, and offline sync queue counters.
- **Updated:** `ui/MainActivity.kt` with a 3-tab navigation bar (Home/Dashboard, Visits, Ask) with full-screen patient profile and landing screen overlay.
- **Fixed:** Layout overflow, card heights, touch targets, text wrapping, and spacing across all screens.

### Evaluation & Documentation
- **Generated:** `evaluation/multilingual/multilingual_benchmark.jsonl` containing 35 comprehensive test cases across 18 distinct categories.
- **Created:** `docs/ACTUAL_IMPLEMENTATION_STATUS.md` auditing all 30 system features.
- **Created:** `docs/MULTILINGUAL_EVALUATION.md` reporting per-language and per-category verification.
- **Created:** `docs/FEATURE_ACCEPTANCE_MATRIX.md` tracking end-to-end acceptance.
- **Created:** `docs/UI_UX_AUDIT.md` documenting visual fixes and accessibility enhancements.
- **Verified:** All Kotlin unit tests passing (`BUILD SUCCESSFUL in 1m 30s`) and official challenge evaluation maintained at **Primary Score: 1.0000** with **ESCALATION GATE: PASS**.

