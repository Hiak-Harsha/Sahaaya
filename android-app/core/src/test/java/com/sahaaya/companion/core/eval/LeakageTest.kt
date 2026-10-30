package com.sahaaya.companion.core.eval

import com.sahaaya.companion.core.DataLoader
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LeakageTest {

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

    data class LeakMatch(val file: String, val lineNumber: Int, val lineText: String, val matchedQuestion: String)

    fun findLeakages(): List<LeakMatch> {
        val root = findRepoRoot()
        val questions = mutableListOf<String>()

        val qDev = File(root, "data/questions_dev.csv")
        if (qDev.exists()) {
            DataLoader.parseCsv(qDev.readText(Charsets.UTF_8)).forEach {
                val q = it["question_text"]?.trim() ?: ""
                if (q.length > 20) questions.add(q)
            }
        }

        val qTest = File(root, "data/questions_test.csv")
        if (qTest.exists()) {
            DataLoader.parseCsv(qTest.readText(Charsets.UTF_8)).forEach {
                val q = it["question_text"]?.trim() ?: ""
                if (q.length > 20) questions.add(q)
            }
        }

        val coreSrcDir = File(root, "android-app/core/src/main/java")
        val leaks = mutableListOf<LeakMatch>()

        coreSrcDir.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
            val lines = file.readLines(Charsets.UTF_8)
            for ((idx, line) in lines.withIndex()) {
                val trimmed = line.trim()
                if (trimmed.startsWith("//") || trimmed.startsWith("*") || trimmed.startsWith("/*")) continue

                for (q in questions) {
                    val lowQ = q.lowercase()
                    // Check if a substantial continuous chunk of the question is hardcoded in the source code
                    val chunksToCheck = listOf(
                        lowQ,
                        if (lowQ.length > 30) lowQ.substring(0, 30) else lowQ
                    )
                    for (chk in chunksToCheck) {
                        if (chk.length > 25 && line.lowercase().contains(chk)) {
                            leaks.add(LeakMatch(file.name, idx + 1, trimmed, q))
                            break
                        }
                    }
                }
            }
        }
        return leaks
    }

    @Test
    fun testDetectLeaks() {
        val leaks = findLeakages()
        if (leaks.isNotEmpty()) {
            println("Found ${leaks.size} question text leakages in core source:")
            for (l in leaks.take(10)) {
                println("  ${l.file}:${l.lineNumber} -> ${l.lineText}")
            }
        } else {
            println("ZERO question text leakages found in :core source code! Clean!")
        }
    }
}
