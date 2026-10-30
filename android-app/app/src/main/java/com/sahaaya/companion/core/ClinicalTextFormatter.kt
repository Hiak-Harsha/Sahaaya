package com.sahaaya.companion.core

import java.util.regex.Pattern

object ClinicalTextFormatter {

    /**
     * Dynamically cleans and formats raw protocol text into clean, structured clinical points
     * without modifying the underlying data files.
     *
     * Handles:
     * - Markdown 2-column and 3-column table rows -> Clean key: value bullets
     * - Removes table separators (|---|---|) and stray pipes (|)
     * - Bullet dashes (- item) and numbered lists
     * - Removes markdown bolding (**) and headers (###)
     * - Deduplicates against the short salient answer
     */
    fun formatToPoints(rawText: String, excludeText: String = ""): List<String> {
        if (rawText.isBlank()) return emptyList()

        val points = mutableListOf<String>()

        // 1. Strip leading section title prefix (e.g. "ANC-1.2 Minimum four ANC check-ups.")
        var text = rawText.replace(Regex("^[A-Z]{3,4}-\\d\\.\\d\\s+[^.]+\\.\\s*"), "")

        // 2. Extract 3-column table rows: | Col1 | Col2 | Col3 |
        val pattern3 = Pattern.compile("\\|\\s*([^|\\n]+?)\\s*\\|\\s*([^|\\n]+?)\\s*\\|\\s*([^|\\n]+?)\\s*\\|")
        val matcher3 = pattern3.matcher(text)
        val extracted3 = mutableListOf<String>()
        while (matcher3.find()) {
            val c1 = matcher3.group(1)?.trim() ?: ""
            val c2 = matcher3.group(2)?.trim() ?: ""
            val c3 = matcher3.group(3)?.trim() ?: ""
            if (!isTableSeparator(c1, c2) && !isTableHeader(c1, c2, c3)) {
                extracted3.add(format3ColRow(c1, c2, c3))
            }
        }

        // 3. Extract 2-column table rows: | Col1 | Col2 |
        val pattern2 = Pattern.compile("\\|\\s*([^|\\n]+?)\\s*\\|\\s*([^|\\n]+?)\\s*\\|")
        val matcher2 = pattern2.matcher(text)
        val extracted2 = mutableListOf<String>()
        while (matcher2.find()) {
            val c1 = matcher2.group(1)?.trim() ?: ""
            val c2 = matcher2.group(2)?.trim() ?: ""
            if (!isTableSeparator(c1, c2) && !isTableHeader(c1, c2)) {
                extracted2.add("$c1: $c2")
            }
        }

        // If table rows were extracted, add them
        if (extracted3.isNotEmpty()) {
            points.addAll(extracted3)
        } else if (extracted2.isNotEmpty()) {
            points.addAll(extracted2)
        }

        // 4. Remove all table structures and separators from narrative text
        text = text.replace(Regex("\\|\\s*[-:]+\\s*\\|\\s*[-:]+[\\s\\-\\|:]*"), "\n")
        text = text.replace(Regex("\\|[^|\\n]+\\|[^|\\n]+\\|(?:[^|\\n]+\\|)?"), "\n")
        text = text.replace("|", " ")

        // 5. Clean markdown bold/italic and normalize bullets
        text = text.replace("**", "").replace("__", "").replace("*", "")
        text = text.replace(Regex("\\s+-\\s+"), "\n• ")

        // 6. Split narrative into readable clinical sentences
        val segments = text.split("\n")
        for (seg in segments) {
            val cleanSeg = seg.trim()
            if (cleanSeg.isBlank()) continue

            // Split long narrative blocks into sentences
            val sentences = cleanSeg.split(Regex("(?<=[.?!])\\s+(?=[A-Z0-9])"))
            for (sent in sentences) {
                var s = sent.trim()
                s = s.replace(Regex("^[•\\-*\\s]+"), "").trim()
                s = s.replace(Regex("\\s+"), " ").trim()

                // Filter out headers, very short fragments, or separator leftovers
                if (s.length > 5 && !s.startsWith("---") && !s.contains("Check-up When")) {
                    points.add(s)
                }
            }
        }

        // 7. Deduplicate and filter out points that repeat the initial short summary
        val normExclude = excludeText.lowercase().replace(Regex("[^a-z0-9]"), "")
        val result = mutableListOf<String>()
        val seen = mutableSetOf<String>()

        for (pt in points) {
            val normPt = pt.lowercase().replace(Regex("[^a-z0-9]"), "")
            if (normPt.isBlank() || seen.contains(normPt)) continue
            seen.add(normPt)

            // Skip if it repeats the short salient answer
            if (normExclude.isNotBlank() && (normExclude.contains(normPt) || normPt.contains(normExclude))) {
                continue
            }
            result.add(pt)
        }

        // If all points got filtered out, fall back to cleaned non-empty points
        return if (result.isNotEmpty()) result else points.distinct()
    }

    private fun isTableSeparator(vararg cells: String): Boolean {
        return cells.all { it.contains("---") || it.contains("---:") || it.contains(":---") || it.replace("-", "").isEmpty() }
    }

    private fun isTableHeader(vararg cells: String): Boolean {
        val lowers = cells.map { it.lowercase() }
        val headerTokens = listOf("check-up", "when", "result", "muac", "colour", "age", "place of delivery", "visit days", "number of home visits", "dose")
        return headerTokens.any { h -> lowers.any { it.contains(h) } }
    }

    private fun format3ColRow(c1: String, c2: String, c3: String): String {
        return when {
            c3.equals("red", ignoreCase = true) || c3.equals("yellow", ignoreCase = true) || c3.equals("green", ignoreCase = true) ->
                "$c1 [$c3]: $c2"
            c2.contains("visit", ignoreCase = true) ->
                "$c1 ($c2): $c3"
            else ->
                "$c1 ($c2): $c3"
        }
    }
}
