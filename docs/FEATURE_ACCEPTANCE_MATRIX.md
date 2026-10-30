# FEATURE ACCEPTANCE MATRIX: SAHAAYA COMPANION

**Evaluation Date**: October 30, 2026  
**Platform**: Android Native (Jetpack Compose + Kotlin)  
**Standard**: TalentAP Mega Hackathon 2026 — Problem H5  

---

| # | Requirement | UI | Logic | Persistence | Tests | Offline | Accepted | Evidence / Notes |
|:---:|:---|:---:|:---:|:---:|:---:|:---:|:---:|:---|
| 1 | **Landing / Onboarding Experience** | YES | YES | YES | YES | YES | **ACCEPTED** | `LandingScreen.kt` with language selection, offline guarantee badge, and direct dashboard launch. |
| 2 | **Structured Main Dashboard** | YES | YES | YES | YES | YES | **ACCEPTED** | `DashboardScreen.kt` with live triage status cards, beneficiary search, and offline queue status. |
| 3 | **Ask Sahaaya Clinical Search** | YES | YES | YES | YES | YES | **ACCEPTED** | `AskScreen.kt` + `SahaayaPipeline.kt`. |
| 4 | **Multilingual Query Pipeline** | YES | YES | YES | YES | YES | **ACCEPTED** | `QueryUnderstandingEngine.kt`, supporting Telugu script, Devanagari Hindi, and Roman Hindi. |
| 5 | **Transliteration & Typo Tolerance** | YES | YES | YES | YES | YES | **ACCEPTED** | `TransliterationNormalizer.kt` with Damerau-Levenshtein distance and clinical concept lexicon. |
| 6 | **Deterministic Danger Detection** | YES | YES | YES | YES | YES | **ACCEPTED** | `DangerRuleEngine.kt` (100% recall on dev benchmark; emergency overrides all routine flows). |
| 7 | **Scope / Refusal Gate** | YES | YES | YES | YES | YES | **ACCEPTED** | `IntentGate.kt` (100% refusal accuracy citing `SCP-1.2` for out-of-scope requests). |
| 8 | **Leaf Protocol Citations** | YES | YES | YES | YES | YES | **ACCEPTED** | Ingested all 107 leaf protocol sections; cites max 2 leaf sections. |
| 9 | **Confidence & Evidence Display** | YES | YES | YES | YES | YES | **ACCEPTED** | Expandable evidence accordion with exact protocol source text. |
| 10 | **Household & Beneficiary Register** | YES | YES | YES | YES | YES | **ACCEPTED** | 2,234 members from `household_register.csv` parsed via `DataLoader.kt`. |
| 11 | **Daily Visit Prioritisation** | YES | YES | YES | YES | YES | **ACCEPTED** | `VisitListEngine.kt` (F1 = 1.000, Priority Accuracy = 1.000). |
| 12 | **Facility Directory & Referral Routing** | YES | YES | YES | YES | YES | **ACCEPTED** | `FacilityDirectory.kt` recommending nearest FRU / 24x7 CEmONC facility. |
| 13 | **Condition Criticality Triage** | YES | YES | YES | YES | YES | **ACCEPTED** | `ConditionCriticalityEngine.kt` (RED_CRITICAL, AMBER_URGENT, YELLOW_STANDARD, GREEN_ROUTINE). |
| 14 | **Emergency SOS (108 Dialer)** | YES | YES | YES | YES | YES | **ACCEPTED** | `EmergencySosEngine.kt` launching Android dialer via `ACTION_DIAL`. |
| 15 | **Referral Slip Generation & Sharing** | YES | YES | YES | YES | YES | **ACCEPTED** | `ReferralSlipDialog.kt` generating shareable slip via Android ShareSheet. |
| 16 | **Patient Advisory Engine** | YES | YES | YES | YES | YES | **ACCEPTED** | `PatientAdvisoryEngine.kt` (English, Hindi, Telugu localized guidance). |
| 17 | **Patient Profile Screen** | YES | YES | YES | YES | YES | **ACCEPTED** | `PatientProfileScreen.kt` displaying full history, category badges, and quick actions. |
| 18 | **Patient Image Attachments** | YES | YES | YES | YES | YES | **ACCEPTED** | `PatientImageRepository.kt` storing local JPEG observations & MCP document photos in app storage. |
| 19 | **Follow-Up Revisit Scheduling** | YES | YES | YES | YES | YES | **ACCEPTED** | `FollowUpScheduler.kt` + `FollowUpRepository.kt` with date-based follow-up reminders. |
| 20 | **Revisit Outcome Recording** | YES | YES | YES | YES | YES | **ACCEPTED** | UI dialog recording: Improved, Unchanged, Worsened, Referred, Not Available. |
| 21 | **Dynamic Visit Reordering** | YES | YES | YES | YES | YES | **ACCEPTED** | `DynamicVisitReorderEngine.kt` promoting high-risk emergency visits dynamically. |
| 22 | **Offline Sync Audit Queue** | YES | YES | YES | YES | YES | **ACCEPTED** | `OfflineSyncQueue.kt` tracking timestamped offline audit events. |
| 23 | **End-of-Day PHC Summary** | YES | YES | YES | YES | YES | **ACCEPTED** | `DailySummaryDialog.kt` aggregating daily completed, pending, and emergency visits. |
| 24 | **Language Switching (EN/HI/TE)** | YES | YES | YES | YES | YES | **ACCEPTED** | Live language switcher updating all strings instantly across screens. |
| 25 | **Dark / Bright Theme Toggle** | YES | YES | YES | YES | YES | **ACCEPTED** | Material 3 custom high-contrast dark and bright themes. |
