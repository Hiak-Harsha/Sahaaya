# Sahaaya SOURCE-to-BASE Merge Notes

**Date:** October 30, 2026  
**Status:** In Progress (Phase 0 Complete)  
**Target Branch:** `feature/sahaaya-multilingual-workflow`  
**Base Repository:** `C:\Users\madha\Desktop\Sahaaya-main\Sahaaya-main`  
**Source Repository:** `C:\Users\madha\Desktop\Sahaaya-main\SOURCE_REPO\Sahaaya-main`

---

## 1. Objectives & Scope

This document details the file-by-file merge of features F1 through F11 from SOURCE into BASE.
The merge preserves BASE's rich enterprise frontline health-worker architecture (including `features/*` modules, `core/nlp/QueryUnderstandingEngine`, `core/retrieval/HybridProtocolRetriever`, offline media capture, patient profiles, and official dev evaluation sets) while integrating SOURCE's clinical accuracy heuristics, protocol explorer, clinical text formatting, SMS-first integration, and compact UI polish.

---

## 2. File Impact & Action Plan

| File | Target Feature | Action Type | Details |
|---|---|---|---|
| `docs/MERGE_NOTES.md` | Phase 0 | Create | Merge documentation and architectural diff audit. |
| `android-app/app/src/main/java/com/sahaaya/companion/core/IntentGate.kt` | F8 | Adapt & Merge | Add kit exceptions (IFA, zinc, ORS, calcium, albendazole, tepid sponging) and specific out-of-scope reasons (`PCPNDT_PROHIBITED`, `ADMINISTRATIVE_OUT_OF_SCOPE`, `DIAGNOSIS_OUT_OF_SCOPE`, `PRESCRIPTION_OUT_OF_SCOPE`). Preserve BASE's `GateResult.citedSectionId`. |
| `android-app/app/src/main/java/com/sahaaya/companion/core/DangerRuleEngine.kt` | F7 | Adapt & Merge | Add Devanagari transliteration preprocessing, numeric parsing for infant weight (< 1.8 kg), fever duration (>= 7 days), severe hypertension BP parsing, and missing ESC leaves (`ESC-2.1, 2.2, 2.3, 2.5, 2.6, 2.7, 2.8, 3.1, 4.2, 4.3, 4.4, 5.1, 5.3, 5.4, 5.5, 5.7, 6.1, 6.2`). Preserve 100% recall and negation filtering. |
| `android-app/app/src/main/java/com/sahaaya/companion/core/retrieval/HybridProtocolRetriever.kt` & `Bm25Retriever.kt` | F9 | Adapt & Merge | Integrate clinical concept routing and key-fact extraction parity rules without duplicating answer extractors. Add context check on bathing (`nehlana/bath/snan`) requiring newborn/LBW context. |
| `android-app/app/src/main/java/com/sahaaya/companion/core/ClinicalTextFormatter.kt` | F2 | Port (New) | Copy 128-line table & markdown formatter from SOURCE to clean up chunk text and dedupe against short answers. |
| `android-app/app/src/main/java/com/sahaaya/companion/ui/ShareUtils.kt` | F4 | Create (New) | Implement `Context.shareViaSmsOrChooser(text, chooserTitle)` using `smsto:` first with `ACTION_SEND` fallback. |
| `android-app/app/src/main/java/com/sahaaya/companion/ui/GuideScreen.kt` | F1 | Port & Enhance | Port Guide tab with 107 section cards, category filter chips (CHD, IMM, MATERNAL, ESC, SCP, OTHER), search bar, `ClinicalTextFormatter`, and "Query Protocol" action. |
| `android-app/app/src/main/java/com/sahaaya/companion/ui/AskScreen.kt` | F2, F4, F5 | Adapt & Merge | Add collapsible "View detailed protocol" card, SMS-first sharing via `ShareUtils`, and compact sample questions dropdown. |
| `android-app/app/src/main/java/com/sahaaya/companion/ui/VisitsScreen.kt` | F3, F4 | Adapt & Merge | Add 48h re-visit per-card toggle and Priority 1 badge. Wire into `FollowUpScheduler` and persistent state. Use `ShareUtils`. |
| `android-app/app/src/main/java/com/sahaaya/companion/ui/MainActivity.kt` | F1, F6 | Adapt & Merge | Add Guide tab (4 tabs total: Home/Dashboard, Visits, Ask, Guide), polish top bar (36dp logo, 38dp buttons, ellipsis, teal indicator). |
| `android-app/app/src/main/java/com/sahaaya/companion/ui/ReferralSlipDialog.kt` | F4 | Adapt & Merge | Use `ShareUtils` for SMS-first referral dispatch. |
| `android-app/app/src/main/java/com/sahaaya/companion/ui/DailySummaryDialog.kt` | F4 | Adapt & Merge | Use `ShareUtils` for SMS-first daily summary dispatch. |
| `android-app/app/src/main/java/com/sahaaya/companion/ui/i18n/AppLanguage.kt` & res | F1, F2, F3, F5, F6 | Extend | Add all new string keys in EN, HI, and TE. |
| `android-app/app/src/main/java/com/sahaaya/companion/features/scheduling/FollowUpScheduler.kt` | F3 | Extend | Support persistent 48h re-visit creation and cancellation. |
| `android-app/app/src/main/java/com/sahaaya/companion/features/reordering/DynamicVisitReorderEngine.kt` | F3 | Extend | Pin 48h-scheduled members to top of priority list. |
| `assets/protocols_canonical.json` & `android-app/app/src/main/assets/protocols_canonical.json` | F10 | Port | Canonical 107-chunk protocol dataset. |
| `scripts/clinical_pipeline.py`, `generate_visit_list.py`, `index_protocols.py`, `test_parity.py` | F10 | Port & Fix | Fix hardcoded paths in Python pipeline to use `ROOT / "data"`. Add automated Kotlin-Python parity test. |
| `SAHAAYA_PRESENTATION_BRIEF.md` & `SAHAAYA_TECHNICAL_DEFENSE_AND_GLOSSARY.md` | F11 | Port & Update | Copy presentation briefs, document merged architecture. |
| `android-app/app/build.gradle.kts` | F11 | Bump | Update versionCode to 7, versionName to `1.6.0-H5`. |
| Tests | F2, F7, F8, F9 | Create/Update | Add `ClinicalTextFormatterTest`, Devanagari danger input test, numeric threshold tests, IntentGate kit-exception tests, retrieval parity tests, and safety order test. |

---

## 3. Deliberately Skipped or Adapted Elements

1. **Asset Loading**: SOURCE loaded raw chunks without rich metadata; BASE's asset loader with fallback and hash validation is preserved.
2. **Ephemeral 48h Scheduling**: SOURCE stored 48h scheduled members in an in-memory Compose `mutableStateListOf`. BASE persists this state to `SharedPreferences` and registers it in `FollowUpScheduler` and `DynamicVisitReorderEngine`.
3. **Hard-coded advisory text**: SOURCE used hard-coded strings for SMS sharing. BASE uses `PatientAdvisoryEngine` and `EmergencySosEngine` with citations.
4. **Bathing regex over-match**: SOURCE matched "snan/bath/nehlana" without infant context, which could misclassify general hygiene questions. BASE enforces newborn/LBW context.
