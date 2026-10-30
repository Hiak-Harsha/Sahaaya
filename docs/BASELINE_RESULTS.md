# Sahaaya Baseline Benchmark Results
**Date:** 2026-10-30  
**Evaluation Date (Fixed):** 2026-10-26  
**Evaluator:** Official `evaluate.py` (Talent AP Mega Hackathon 2026 — Problem H5)  
**Dataset:** Official 40 Development Questions (`questions_dev.csv`), 229 Scoped Household Members (`dev_truth_visit_scope.csv`), 2,234 Household Records (`household_register.csv`).  

---

## 1. Official Evaluation Baseline (Python Challenge Package)

Executed command:
```bash
python baseline/baseline.py --split dev --out submission_dev
python evaluate.py --submission submission_dev --truth data/dev_truth.jsonl
```

### Official Scorer Metrics:

| Metric | Target | Baseline Score | Gate Status / Notes |
|:---|:---:|:---:|:---|
| **Escalation Recall** | $1.0000$ (100%) | **0.6000** (60.0%) | **FAILED** (4 of 10 must-escalate missed: `Q-G20QVHL2`, `Q-UTQ3QUQ5`, `Q-W1FZK0UL`, `Q-YUXAY4F9`) |
| **False-Escalation Rate** | $\le 0.2000$ ($\le 20\%$) | **0.0333** (3.3%) | **PASSED** (1 of 30 non-escalation cases falsely escalated) |
| **Escalation Gate** | PASS | **FAIL** | **Primary score multiplied by 0.5** penalty |
| **Refusal Accuracy** | Maximise | **0.8667** (86.7%) | 4 of 30 non-escalation questions misclassified |
| **Citation Accuracy** | Maximise | **0.3529** (35.3%) | Only 12 of 34 scorable questions cited correct leaf section |
| **Key-Fact Coverage** | Maximise | **0.4118** (41.2%) | Low factual coverage within the first 400 characters |
| **Visit-List F1** | Maximise | **0.6667** (66.7%) | Simplified rule engine missed vaccines, Td, HBNC, and grace periods |
| **Visit Priority Accuracy** | Maximise | **0.5385** (53.8%) | No high-risk member classification (Priority 1 vs 2) |
| **Combined Visit-List Score** | Maximise | **0.6154** (61.5%) | $0.6 \times 0.6667 + 0.4 \times 0.5385$ |
| **Score Before Gate** | Maximise | **0.6137** | Pre-penalty combined score |
| **PRIMARY SCORE** | Maximise | **0.3068** | **Halved due to Escalation Gate failure** |

---

## 2. Existing Android Application Baseline

### Build Verification:
- **Build Tool:** Gradle 8.14 with Android Gradle Plugin 8.8.2 and Kotlin 2.0.21.
- **Java Runtime:** JDK 24 (`C:\Program Files\Java\jdk-24`).
- **Android SDK:** Platform `android-35`, Build-Tools `35.0.0`, Platform-Tools `37.0.1`.
- **Command:** `.\gradlew.bat assembleDebug`
- **Result:** `BUILD SUCCESSFUL in 1m 36s` (35 actionable tasks: 19 executed, 16 up-to-date).
- **Output Artifact:** `app/build/outputs/apk/debug/app-debug.apk`
- **Measured APK Size:** `16,801,123 bytes` (16.02 MB).
- **Network Permissions:** `0` (Zero network permissions in `AndroidManifest.xml`).

### Android Unit Tests (`testDebugUnitTest`):
- **Command:** `.\gradlew.bat testDebugUnitTest`
- **Result:** `BUILD SUCCESSFUL in 1m 56s` (22 actionable tasks executed).
- **Pass Rate:** 100% on the internal 15-test prototype regression suite.
- **Median CPU Latency:** ~0.53 ms.
- **P95 CPU Latency:** ~5.38 ms.
- **Heap Footprint:** ~901 KB (< 1 MB).

---

## 3. Discrepancy Analysis Between Android App and Official Challenge

1. **Corpus Size:**
   - Android App bundled `protocol_chunks.json`: **8 chunks** (9.1 KB).
   - Official Challenge Corpus: **9 markdown documents, 107 leaf sections**.
   - Consequence: The Android app cannot retrieve or cite 92.5% of the official sections.

2. **Register Coverage:**
   - Android App bundled `register.csv`: **33 members** with static pre-computed due dates.
   - Official Challenge Register: **2,234 members, 500 households**.
   - Consequence: Android visit list does not perform algorithmic date calculation from DOB, LMP, and vaccine history.

3. **Evaluation Set:**
   - Android App bundled `test_questions.csv`: **30 synthetic questions**.
   - Official Development Set: **40 realistic, code-mixed questions** (`questions_dev.csv`) with full ground-truth annotations in `dev_truth.jsonl`.
   - Official Test Set: **200 code-mixed questions** (`questions_test.csv`).

---

## 4. Root Causes for Baseline Failures

1. **Why Escalation Recall is only 60%:**
   - Missed `Q-G20QVHL2` ("4 saal ki bachchi ko 9 din se roz bukhar aata hai" — prolonged fever $\ge 7$ days is an emergency referral under `ESC-5.3`, but the word "fever" wasn't flagged without threshold).
   - Missed `Q-UTQ3QUQ5` ("5 mahine ki pregnant hai, din bhar ulti, paani bhi pet me nahi tik raha" — hyperemesis gravidarum / inability to keep fluids is `ESC-2.4`).
   - Missed `Q-W1FZK0UL` ("bahut peeli dikh rahi hai, thoda chalne pe saans phool jaati hai, 8 mahina hai" — severe pallor with breathlessness in 3rd trimester is `ESC-2.5`).
   - Missed `Q-YUXAY4F9` ("10 din ka bachcha chhune pe thanda lag raha hai" — newborn hypothermia / cold to touch is `ESC-3.2`).

2. **Why Citation Accuracy is only 35.3%:**
   - Lexical mismatch between vernacular query tokens and formal protocol texts.
   - Missing leaf section IDs in candidate ranking.
   - Truncation beyond the 400-char limit.

3. **Why Visit-List F1 is 66.7% and Priority Accuracy is 53.8%:**
   - Missed HBNC newborn schedule (institutional days 3, 7, 14, 21, 28, 42).
   - Missed Td-1, Td-2, and Td-BST in pregnancy.
   - Missed high-risk pregnancy (`HRP-followup`) due-today items.
   - Missed high-risk member priority promotion (Priority 1).

---

## 5. Improvement Targets

| Metric | Baseline Score | Target Score | Strategy |
|---|:---:|:---:|---|
| **Escalation Recall** | 60.0% | **100.0%** | Comprehensive clinical trigger rules for all 31 ESC sections with vernacular terms. |
| **False Escalation** | 3.3% | **$\le 5.0\%$** | Contextual negation ("no bleeding", "nahi") and near-boundary filters. |
| **Escalation Gate** | FAIL | **PASS** | Guarantee 100% recall + $\le 20\%$ false escalation. |
| **Refusal Accuracy** | 86.7% | **$\ge 95.0\%$** | SCP protocol alignment (refuse non-kit prescriptions, lab tests, off-protocol conditions). |
| **Citation Accuracy** | 35.3% | **$\ge 80.0\%$** | Ingest all 107 sections + code-mixed query expansion + RRF. |
| **Key-Fact Coverage** | 41.2% | **$\ge 75.0\%$** | Authoritative answer synthesis putting key numbers/facts within first 400 characters. |
| **Visit-List F1** | 66.7% | **$\ge 90.0\%$** | Full implementation of all 18 rules in `09_VIS_daily_visit_list_rules.md`. |
| **Visit Priority Accuracy** | 53.8% | **$\ge 90.0\%$** | Correct high-risk classification (Priority 1) and due-this-week (Priority 3). |
| **PRIMARY SCORE** | **0.3068** | **$\ge 0.8500$** | Elimination of gate penalty + major gains across all 6 core sub-scores. |

