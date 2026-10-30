package com.sahaaya.companion.core.eval

import com.sahaaya.companion.core.Bm25Retriever
import com.sahaaya.companion.core.DataLoader
import com.sahaaya.companion.core.DangerRuleEngine
import com.sahaaya.companion.core.IntentGate
import com.sahaaya.companion.core.ProtocolChunk
import com.sahaaya.companion.core.RegisterMember
import com.sahaaya.companion.core.SahaayaPipeline
import com.sahaaya.companion.core.VisitListEngine
import com.sahaaya.companion.core.retrieval.FuzzyLexicalRetriever
import com.sahaaya.companion.core.retrieval.HybridProtocolRetriever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class BaselineScorecardTest {

    private lateinit var pipeline: SahaayaPipeline
    private lateinit var chunks: List<ProtocolChunk>
    private lateinit var root: File
    private lateinit var registerMembers: List<RegisterMember>
    private lateinit var visitTruth: List<VisitPredictionRow>
    private lateinit var visitScope: Set<String>
    private lateinit var visitPreds: List<VisitPredictionRow>
    private val devQuestionTextMap = mutableMapOf<String, String>()

    private fun findRepoRoot(): File {
        var dir = File(".").canonicalFile
        while (dir != null) {
            if (File(dir, "data/dev_truth.jsonl").exists()) {
                return dir
            }
            dir = dir.parentFile
        }
        throw IllegalStateException("Cannot find repo root with data/dev_truth.jsonl")
    }

    @Before
    fun setUp() {
        root = findRepoRoot()
        val assetsDir = listOf(
            File(root, "android-app/app/src/main/assets"),
            File(root, "data")
        ).firstOrNull { it.exists() && File(it, "protocol_chunks.json").exists() }
            ?: throw IllegalStateException("Assets directory not found")

        val chunksJson = File(assetsDir, "protocol_chunks.json").readText(Charsets.UTF_8)
        val rulesJson = File(assetsDir, "danger_rules.json").readText(Charsets.UTF_8)

        chunks = DataLoader.parseProtocolChunks(chunksJson)
        val rules = DataLoader.parseDangerRules(rulesJson)

        val dangerEngine = DangerRuleEngine(rules)
        val intentGate = IntentGate()
        val bm25Retriever = Bm25Retriever(chunks)
        val fuzzyRetriever = FuzzyLexicalRetriever(chunks)
        val hybridRetriever = HybridProtocolRetriever(chunks, bm25Retriever, fuzzyRetriever)
        pipeline = SahaayaPipeline(dangerEngine, intentGate, bm25Retriever, hybridRetriever)

        val devCsv = File(root, "data/questions_dev.csv")
        if (devCsv.exists()) {
            val rows = DataLoader.parseCsv(devCsv.readText(Charsets.UTF_8))
            for (row in rows) {
                val qid = row["question_id"] ?: ""
                val text = row["question_text"] ?: row["question"] ?: ""
                if (qid.isNotBlank() && text.isNotBlank()) {
                    devQuestionTextMap[qid] = text
                }
            }
        }

        // Load Register & Visit List truth
        val registerFile = File(root, "data/household_register.csv")
        registerMembers = if (registerFile.exists()) {
            DataLoader.parseRegister(registerFile.readText(Charsets.UTF_8))
        } else emptyList()

        val visitTruthFile = File(root, "data/dev_truth_visit_list.csv")
        visitTruth = if (visitTruthFile.exists()) {
            H5Scorer.loadVisitCsv(visitTruthFile.readText(Charsets.UTF_8))
        } else emptyList()

        val visitScopeFile = File(root, "data/dev_truth_visit_scope.csv")
        visitScope = if (visitScopeFile.exists()) {
            H5Scorer.loadScopeCsv(visitScopeFile.readText(Charsets.UTF_8))
        } else emptySet()

        val visitEngine = VisitListEngine()
        visitPreds = visitEngine.generateDailyVisitList(registerMembers)
    }

    private fun runSuite(
        name: String,
        truthFile: File,
        includeVisits: Boolean = true
    ): ScoreReport {
        val truthQuestions = H5Scorer.loadTruthQuestions(truthFile.readText(Charsets.UTF_8))
        val answers = mutableMapOf<String, AnswerRecord>()
        for (q in truthQuestions) {
            val qText = if (q.question.isNotBlank()) q.question else devQuestionTextMap[q.question_id] ?: ""
            val sub = pipeline.answerForSubmission(qText, q.question_id)
            if (q.must_escalate && !sub.escalate) {
                println("MISSED ESC [$name]: ${q.question_id} -> '$qText'")
            }
            if (!q.must_escalate && sub.escalate) {
                println("FALSE ESC [$name]: ${q.question_id} -> '$qText'")
            }
            if (q.out_of_scope && !sub.refused) {
                println("MISSED OOS REFUSAL [$name]: ${q.question_id} -> '$qText'")
            }
            answers[q.question_id] = AnswerRecord(
                question_id = q.question_id,
                answer_text = sub.answer_text,
                cited_section_ids = sub.cited_section_ids,
                escalate = sub.escalate,
                refused = sub.refused,
                latency_ms = sub.latency_ms
            )
        }
        val report = H5Scorer.score(
            answers = answers,
            truthQuestions = truthQuestions,
            visitPredictions = if (includeVisits) visitPreds else null,
            visitTruth = if (includeVisits) visitTruth else emptyList(),
            visitScope = if (includeVisits) visitScope else emptySet()
        )
        println("=== SCORECARD FOR $name ===")
        println(report.summary())
        return report
    }

    @Test
    fun testAllSuites() {
        val devTruth = File(root, "data/dev_truth.jsonl")
        if (devTruth.exists()) {
            val rep = runSuite("DEV TRUTH (40 Qs)", devTruth, includeVisits = true)
            assertEquals("PASS", rep.escalation_gate)
            assertEquals("PASS", rep.gate_recall_condition)
            assertEquals("PASS", rep.gate_false_escalation_condition)
            assertEquals(1.0, rep.escalation_recall, 0.0001)
            assertTrue(rep.false_escalation_rate <= 0.10)
            assertTrue("Visit F1 must be >= 0.95, was ${rep.visit_list_f1}", rep.visit_list_f1 >= 0.95)
            assertTrue("Visit Priority Acc must be >= 0.95, was ${rep.visit_priority_accuracy}", rep.visit_priority_accuracy >= 0.95)
            assertTrue("Primary score must be >= 0.90, was ${rep.primary_score}", rep.primary_score >= 0.90)
        }
        val escSuite = File(root, "data/suites/esc_suite.jsonl")
        if (escSuite.exists()) {
            val rep = runSuite("ESC SUITE (120 Qs)", escSuite, includeVisits = false)
            assertEquals("PASS", rep.escalation_gate)
            assertEquals(1.0, rep.escalation_recall, 0.0001)
        }
        val oosSuite = File(root, "data/suites/oos_suite.jsonl")
        if (oosSuite.exists()) {
            val rep = runSuite("OOS SUITE (50 Qs)", oosSuite, includeVisits = false)
            assertEquals("PASS", rep.escalation_gate)
            assertTrue(rep.refusal_accuracy >= 0.90)
        }
        val covSuite = File(root, "data/suites/coverage_suite.jsonl")
        if (covSuite.exists()) {
            val rep = runSuite("COVERAGE SUITE (321 Qs)", covSuite, includeVisits = false)
            assertEquals("PASS", rep.escalation_gate)
            assertEquals(1.0, rep.escalation_recall, 0.0001)
            assertTrue(rep.false_escalation_rate <= 0.10)
        }
        val paraSuite = File(root, "data/suites/paraphrase_suite.jsonl")
        if (paraSuite.exists()) {
            val rep = runSuite("PARAPHRASE SUITE (200 Qs)", paraSuite, includeVisits = false)
            assertEquals("PASS", rep.escalation_gate)
            assertEquals(1.0, rep.escalation_recall, 0.0001)
            assertTrue(rep.false_escalation_rate <= 0.10)
        }
        val voiceSuite = File(root, "data/suites/voice_noise_suite.jsonl")
        if (voiceSuite.exists()) {
            val rep = runSuite("VOICE NOISE SUITE (25 Qs)", voiceSuite, includeVisits = false)
            assertEquals("PASS", rep.escalation_gate)
            assertEquals(1.0, rep.escalation_recall, 0.0001)
        }
    }
}
