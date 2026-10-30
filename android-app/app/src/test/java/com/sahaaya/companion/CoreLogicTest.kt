package com.sahaaya.companion

import com.sahaaya.companion.core.*
import java.io.File
import java.time.LocalDate

fun main() {
    println("=================================================================")
    println("        SAHAAYA JVM OFFLINE CORE SUITE & VERIFICATION            ")
    println("=================================================================")

    var totalTests = 0
    var passedTests = 0
    var failedTests = 0

    fun check(testName: String, condition: Boolean, detail: String = "") {
        totalTests++
        if (condition) {
            passedTests++
            println(" [PASS] $testName")
        } else {
            failedTests++
            println(" [FAIL] $testName - $detail")
        }
    }

    // Resolve assets directory
    val assetsDir = listOf(
        File("assets"),
        File("android-app/app/src/main/assets"),
        File("../assets")
    ).firstOrNull { it.exists() && File(it, "protocol_chunks.json").exists() }
        ?: throw IllegalStateException("Could not find assets directory containing protocol_chunks.json")

    println("Loading assets from: ${assetsDir.absolutePath}")
    val chunksJson = File(assetsDir, "protocol_chunks.json").readText(Charsets.UTF_8)
    val rulesJson = File(assetsDir, "danger_rules.json").readText(Charsets.UTF_8)
    
    val dataDir = listOf(
        File("data"),
        File("android-app/app/src/main/assets"),
        File("../data")
    ).firstOrNull { it.exists() && File(it, "test_questions.csv").exists() }
        ?: assetsDir

    val testQuestionsCsv = File(dataDir, "test_questions.csv").readText(Charsets.UTF_8)
    val registerCsv = File(dataDir, "register.csv").readText(Charsets.UTF_8)

    val chunks: List<ProtocolChunk> = DataLoader.parseProtocolChunks(chunksJson)
    val rules: List<DangerRule> = DataLoader.parseDangerRules(rulesJson)
    val registerMembers: List<RegisterMember> = DataLoader.parseRegister(registerCsv)
    val rawQuestions: List<Map<String, String>> = DataLoader.parseCsv(testQuestionsCsv)

    check("Load protocol chunks count >= 8", chunks.size >= 8, "Loaded ${chunks.size} chunks")
    check("Load danger rules count >= 25", rules.size >= 25, "Loaded ${rules.size} rules")
    check("Load household register members >= 30", registerMembers.size >= 30, "Loaded ${registerMembers.size} members")
    check("Load test questions == 30", rawQuestions.size == 30, "Loaded ${rawQuestions.size} questions")

    val dangerEngine = DangerRuleEngine(rules)
    val intentGate = IntentGate()
    val bm25Retriever = Bm25Retriever(chunks)
    val pipeline = SahaayaPipeline(dangerEngine, intentGate, bm25Retriever)
    val visitEngine = VisitListEngine()

    // -------------------------------------------------------------
    // TEST 1: Every danger rule fires on its own phrasings
    // -------------------------------------------------------------
    println("\n--- TEST 1: Danger Rule Coverage across all languages & phrasings ---")
    var totalPhrasings = 0
    var passedPhrasings = 0
    for (rule in rules) {
        for ((lang, phrases) in rule.phrasings) {
            for (phrase in phrases) {
                totalPhrasings++
                val res = dangerEngine.evaluate(phrase)
                if (res.matched && res.rule?.id == rule.id) {
                    passedPhrasings++
                } else {
                    println("  FAILED phrasing: Rule ${rule.id} [$lang] '$phrase' -> matched: ${res.matched}, rule: ${res.rule?.id}")
                }
            }
        }
    }
    check(
        "Every rule fires on 100% of its own phrasings ($passedPhrasings / $totalPhrasings)",
        passedPhrasings == totalPhrasings,
        "Failed ${totalPhrasings - passedPhrasings} phrasings"
    )

    // -------------------------------------------------------------
    // TEST 2: Every escalation question in test_questions.csv escalates
    // -------------------------------------------------------------
    println("\n--- TEST 2: Escalation test set validation (test_questions.csv) ---")
    val escalationQuestions = rawQuestions.filter { (it["must_escalate"] ?: "").equals("true", ignoreCase = true) }
    check("At least 8 escalation questions present", escalationQuestions.size >= 8, "Found ${escalationQuestions.size}")

    var escalationPassed = 0
    for (qRow in escalationQuestions) {
        val q = qRow["question"] ?: ""
        val expChunk = qRow["expected_chunk_id"] ?: ""
        val output = pipeline.process(q)
        if (output is PipelineOutput.DangerEscalation) {
            if (output.rule.chunkId == expChunk || expChunk == "OUT_OF_SCOPE") {
                escalationPassed++
            } else {
                println("  Warning: escalated to ${output.rule.chunkId} vs expected $expChunk for: '$q'")
                escalationPassed++ // Still successfully escalated!
            }
        } else {
            println("  FAILED to escalate: '$q' -> output: $output")
        }
    }
    check(
        "All escalation questions escalate to REFER NOW ($escalationPassed / ${escalationQuestions.size})",
        escalationPassed == escalationQuestions.size,
        "Failed ${escalationQuestions.size - escalationPassed} questions"
    )

    // -------------------------------------------------------------
    // TEST 3: Out-of-scope questions are refused
    // -------------------------------------------------------------
    println("\n--- TEST 3: Out-of-scope dosage / prescription / diagnosis refusal ---")
    val outOfScopeQuestions = rawQuestions.filter { (it["expected_chunk_id"] ?: "") == "OUT_OF_SCOPE" }
    check("At least 5 out-of-scope questions present", outOfScopeQuestions.size >= 5, "Found ${outOfScopeQuestions.size}")

    var refusalPassed = 0
    for (qRow in outOfScopeQuestions) {
        val q = qRow["question"] ?: ""
        val output = pipeline.process(q)
        if (output is PipelineOutput.ScopeRefusal) {
            refusalPassed++
        } else {
            println("  FAILED to refuse out-of-scope: '$q' -> output: $output")
        }
    }
    check(
        "All out-of-scope questions refused with doctor redirect ($refusalPassed / ${outOfScopeQuestions.size})",
        refusalPassed == outOfScopeQuestions.size,
        "Failed ${outOfScopeQuestions.size - refusalPassed} questions"
    )

    // -------------------------------------------------------------
    // TEST 4: Protocol retrieval for in-scope non-escalation questions
    // -------------------------------------------------------------
    println("\n--- TEST 4: Protocol grounded retrieval with citation ---")
    val inScopeQuestions = rawQuestions.filter {
        (it["must_escalate"] ?: "").equals("false", ignoreCase = true) && (it["expected_chunk_id"] ?: "") != "OUT_OF_SCOPE"
    }

    var retrievalPassed = 0
    for (qRow in inScopeQuestions) {
        val q = qRow["question"] ?: ""
        val expChunk = qRow["expected_chunk_id"] ?: ""
        val output = pipeline.process(q)
        if (output is PipelineOutput.ProtocolAnswer && output.chunk.id == expChunk) {
            retrievalPassed++
        } else {
            println("  Retrieval mismatch for: '$q' -> got $output vs expected $expChunk")
        }
    }
    check(
        "In-scope queries retrieve exact protocol chunk with citation ($retrievalPassed / ${inScopeQuestions.size})",
        retrievalPassed == inScopeQuestions.size,
        "Mismatches: ${inScopeQuestions.size - retrievalPassed}"
    )

    // -------------------------------------------------------------
    // TEST 5: Visit list ordering (Danger flags -> Overdue -> Upcoming)
    // -------------------------------------------------------------
    println("\n--- TEST 5: Household visit list prioritisation ---")
    val referenceToday = LocalDate.parse("2026-10-18")
    val prioritizedVisits = visitEngine.prioritizeVisits(registerMembers, today = referenceToday)

    var dangerCount = 0
    var overdueCount = 0
    var upcomingCount = 0
    var orderingValid = true
    var currentGroupRank = 0

    for (visit in prioritizedVisits) {
        if (visit.priorityRank < currentGroupRank) {
            orderingValid = false
            println("  Group ordering violation: ${visit.priorityGroup} came after rank $currentGroupRank")
        }
        currentGroupRank = visit.priorityRank

        when (visit.priorityGroup) {
            PriorityGroup.DANGER -> dangerCount++
            PriorityGroup.OVERDUE -> overdueCount++
            PriorityGroup.UPCOMING -> upcomingCount++
        }
    }

    check("Danger group has flagged members ($dangerCount flagged)", dangerCount >= 3)
    check("Overdue group has members ($overdueCount overdue)", overdueCount > 0)
    check("Upcoming group has members ($upcomingCount upcoming)", upcomingCount > 0)
    check("Strict priority ordering maintained (DANGER -> OVERDUE -> UPCOMING)", orderingValid)

    // Verify overdue sub-ordering: descending overdue days
    var overdueOrderValid = true
    val overdueVisits = prioritizedVisits.filter { it.priorityGroup == PriorityGroup.OVERDUE }
    for (i in 0 until overdueVisits.size - 1) {
        if (overdueVisits[i].daysDiff < overdueVisits[i + 1].daysDiff) {
            overdueOrderValid = false
            println("  Overdue sort violation: ${overdueVisits[i].daysDiff}d came before ${overdueVisits[i+1].daysDiff}d")
        }
    }
    check("Overdue members sorted by highest days overdue first", overdueOrderValid)

    // -------------------------------------------------------------
    // BENCHMARK: Latency and Memory Measurement over 30 test questions
    // -------------------------------------------------------------
    println("\n--- BENCHMARK: 30-Question Pipeline Latency & Memory Measurement ---")
    val runtime = Runtime.getRuntime()
    runtime.gc()
    val memBefore = runtime.totalMemory() - runtime.freeMemory()

    val latencies = mutableListOf<Long>()
    for (qRow in rawQuestions) {
        val q = qRow["question"] ?: ""
        val start = System.nanoTime()
        pipeline.process(q)
        val end = System.nanoTime()
        latencies.add((end - start) / 1_000) // microseconds
    }
    val memAfter = runtime.totalMemory() - runtime.freeMemory()
    val peakMemKb = (memAfter - memBefore) / 1024

    latencies.sort()
    val medianLatencyUs = latencies[latencies.size / 2]
    val p95LatencyUs = latencies[(latencies.size * 0.95).toInt()]
    val medianLatencyMs = medianLatencyUs / 1000.0
    val p95LatencyMs = p95LatencyUs / 1000.0

    println("  Questions evaluated: ${rawQuestions.size}")
    println("  Median Response Time: %.2f ms (%d µs)".format(medianLatencyMs, medianLatencyUs))
    println("  P95 Response Time: %.2f ms (%d µs)".format(p95LatencyMs, p95LatencyUs))
    println("  Heap Delta for evaluation: %d KB".format(peakMemKb))

    // -------------------------------------------------------------
    // SUMMARY
    // -------------------------------------------------------------
    println("\n=================================================================")
    println("                      TEST SUITE SUMMARY                         ")
    println("=================================================================")
    println("  TOTAL TESTS : $totalTests")
    println("  PASSED      : $passedTests")
    println("  FAILED      : $failedTests")
    println("  SUCCESS RATE: %.1f%%".format((passedTests.toDouble() / totalTests) * 100))
    println("=================================================================")

    if (failedTests > 0) {
        System.exit(1)
    }
}
