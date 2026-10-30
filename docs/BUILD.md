# Sahaaya Build and Deployment Guide

This document describes how to build, test, and run **Sahaaya** (Offline Frontline Health-Worker Companion for ASHA and ANM workers).

---

## 1. Environment & Prerequisites

- **Operating System:** Windows 10/11, macOS, or Linux
- **Java Development Kit (JDK):** JDK 17 (or compatible Java 17+ LTS)
  - Configured at: `C:\Users\yoges\develop\jdk-17\jdk-17.0.20.1+1`
- **Android SDK:** Platforms `android-34`, `android-35`; Build-tools `35.0.0`
  - Configured at: `C:\Users\yoges\AppData\Local\Android\Sdk`
- **Gradle:** Gradle 8.14 (bundled via Gradle wrapper in `./android-app/gradlew.bat`)
- **Kotlin:** 2.0.21 (with Compose Multiplatform / Android Compose Plugin)

---

## 2. Running Standalone JVM Tests (Zero Android Dependency)

The core domain logic (`DangerRuleEngine`, `IntentGate`, `Bm25Retriever`, `VisitListEngine`, and `SahaayaPipeline`) is written in pure, dependency-free Kotlin classes. It can be compiled and verified on any standard JVM without requiring an Android emulator or device.

To execute the JVM suite and on-device benchmark harness:

```powershell
# Set Java environment
$env:JAVA_HOME = "C:\Users\yoges\develop\jdk-17\jdk-17.0.20.1+1"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Run precompiled test runner jar
java -jar test-runner.jar
```

Or re-compile and run directly:

```powershell
$files = Get-ChildItem -Path android-app\app\src\main\java\com\sahaaya\companion\core\*.kt, android-app\app\src\test\java\com\sahaaya\companion\CoreLogicTest.kt | Select-Object -ExpandProperty FullName

& "C:\Program Files\Android\Android Studio\plugins\Kotlin\kotlinc\bin\kotlinc.bat" @files -include-runtime -d test-runner.jar
java -jar test-runner.jar
```

### Measured Test Results:
- **Total Tests:** 15
- **Tests Passed:** 15 (100.0% Success Rate)
- **Danger Rule Recall:** 367 / 367 multilingual phrasings triggered immediate escalation (100.0%)
- **Escalation Questions:** 10 / 10 escalated to `REFER NOW`
- **Out-of-Scope Intent Refusal:** 6 / 6 clinical dosage & prescription requests refused with doctor redirect
- **Protocol Grounded Retrieval:** 14 / 14 questions retrieved the exact protocol section with full citation
- **Visit List Prioritization:** Strict sorting: 5 Danger members -> 14 Overdue members (sorted by days overdue descending) -> 14 Upcoming members (sorted by nearest due date)
- **Median CPU Latency:** 0.53 ms (531 µs)
- **P95 CPU Latency:** 5.38 ms
- **Heap Footprint:** ~901 KB (< 1 MB)

---

## 3. Building the Android APK

To assemble the complete debug APK using Gradle:

```powershell
cd android-app

# Set Java 17 path
$env:JAVA_HOME = "C:\Users\yoges\develop\jdk-17\jdk-17.0.20.1+1"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Build debug APK
.\gradlew.bat assembleDebug
```

The output APK is generated at:
```
android-app/app/build/outputs/apk/debug/app-debug.apk
```
- **Measured APK Size:** `16,413,158 bytes` (~15.65 MB, uncompressed debug package with all bundled assets).
- **Network Permissions:** `0` (No `android.permission.INTERNET` declared in `AndroidManifest.xml`).

---

## 4. Running Gradle Unit Tests

To run the unit test suite through Gradle's Android test runner:

```powershell
cd android-app
.\gradlew.bat testDebugUnitTest
```

---

## 5. Installing and Running on a Physical Phone in Airplane Mode

1. Connect your Android smartphone to your computer via USB.
2. Enable **Developer Options** and **USB Debugging** on the phone:
   - Go to *Settings > About Phone > Tap 'Build Number' 7 times*.
   - Go to *Settings > Developer Options > Enable 'USB Debugging'*.
3. Install the generated APK directly over ADB:
   ```powershell
   adb install -r android-app/app/build/outputs/apk/debug/app-debug.apk
   ```
   *(Or copy `app-debug.apk` to phone storage and install via File Manager).*
4. **Switch the phone to Airplane Mode** (disable Mobile Data and Wi-Fi).
5. Open **Sahaaya** from the app drawer:
   - **Ask Screen:** Type symptoms or tap chips (e.g., "Vaginal bleeding", "Baby ribs sinking", "How much ORS", "Amoxicillin dose"). Observe immediate offline responses with red danger escalation, amber doctor redirects, and cited protocol cards.
   - **Today's Visits Screen:** Tap bottom tab to view the prioritized household visit list with danger badges, overdue days, and upcoming follow-ups.
   - **Benchmark Menu:** Tap the speedometer icon on the top right to run the live 30-question evaluation and view on-device latency and memory stats.
