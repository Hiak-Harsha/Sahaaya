package com.sahaaya.companion.core.eval

import com.sahaaya.companion.core.DataLoader
import com.sahaaya.companion.core.ProtocolChunk
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.regex.Pattern

class NumberGroundingTest {

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

    private val numberRegex = Pattern.compile("\\b\\d+(?:\\.\\d+)?\\b")

    fun extractNumbers(text: String): Set<String> {
        val matches = mutableSetOf<String>()
        val m = numberRegex.matcher(text)
        while (m.find()) {
            matches.add(m.group())
        }
        return matches
    }

    @Test
    fun testProtocolChunkNumbersAreConsistent() {
        val root = findRepoRoot()
        val chunksJson = File(root, "android-app/app/src/main/assets/protocol_chunks.json").readText(Charsets.UTF_8)
        val chunks = DataLoader.parseProtocolChunks(chunksJson)

        assertTrue("Chunks must be ingested", chunks.isNotEmpty())
        for (chunk in chunks) {
            // Verify chunk text has numbers for leaf guidance if applicable
            val nums = extractNumbers(chunk.text)
            if (chunk.id in listOf("ANC-1.2", "ANC-2.2", "IMM-2.6", "NUT-1.2")) {
                assertTrue("Chunk ${chunk.id} must contain numerical thresholds", nums.isNotEmpty())
            }
        }
    }
}
