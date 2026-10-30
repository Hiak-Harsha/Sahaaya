package com.sahaaya.companion.core.eval

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class H5ScorerParityTest {

    private fun findRepoRoot(): File {
        var dir = File(".").canonicalFile
        while (dir != null) {
            if (File(dir, "data/dev_truth.jsonl").exists()) {
                return dir
            }
            dir = dir.parentFile
        }
        throw IllegalStateException("Cannot find repo root with data/dev_truth.jsonl from ${File(".").canonicalPath}")
    }

    @Test
    fun testDevTruthAgainstSubmissionDevParity() {
        val root = findRepoRoot()
        val truthFile = File(root, "data/dev_truth.jsonl")
        val submissionDir = File(root, "submission_dev")
        assertTrue("Truth file must exist: ${truthFile.absolutePath}", truthFile.exists())
        assertTrue("Submission dir must exist: ${submissionDir.absolutePath}", submissionDir.exists())

        val report = H5Scorer.scoreFiles(submissionDir, truthFile)
        println(report.summary())

        assertEquals("Gate must PASS", "PASS", report.escalation_gate)
        assertEquals(1.0, report.escalation_recall, 0.0001)
        assertEquals(0.0, report.false_escalation_rate, 0.0001)
        assertEquals(1.0, report.refusal_accuracy, 0.0001)
        assertEquals(1.0, report.citation_accuracy, 0.0001)
        assertEquals(1.0, report.key_fact_coverage, 0.0001)
        assertEquals(1.0, report.visit_list_f1, 0.0001)
        assertEquals(1.0, report.visit_priority_accuracy, 0.0001)
        assertEquals(1.0, report.visit_list_score, 0.0001)
        assertEquals(1.0, report.score_before_gate, 0.0001)
        assertEquals(1.0, report.primary_score, 0.0001)
    }

    @Test
    fun testSampleSubmissionParity() {
        val root = findRepoRoot()
        val truthFile = File(root, "data/dev_truth.jsonl")
        val sampleDir = listOf(
            File(root, "sample_submission"),
            File(root.parentFile, "H5_package/H5_Frontline_Health_Worker_Companion/sample_submission")
        ).firstOrNull { it.exists() }

        assertTrue("Sample submission directory must exist", sampleDir != null && sampleDir.exists())
        val report = H5Scorer.scoreFiles(sampleDir!!, truthFile)
        println(report.summary())

        assertEquals("Gate must FAIL", "FAIL", report.escalation_gate)
        assertEquals(0.0, report.escalation_recall, 0.0001)
        assertEquals(0.0, report.false_escalation_rate, 0.0001)
        assertEquals(0.80, report.refusal_accuracy, 0.0001)
        assertEquals(0.0, report.citation_accuracy, 0.0001)
        assertEquals(0.0, report.key_fact_coverage, 0.0001)
        assertEquals(0.0, report.visit_list_score, 0.0001)
        assertEquals(0.22, report.score_before_gate, 0.0001)
        assertEquals(0.11, report.primary_score, 0.0001)
    }
}
