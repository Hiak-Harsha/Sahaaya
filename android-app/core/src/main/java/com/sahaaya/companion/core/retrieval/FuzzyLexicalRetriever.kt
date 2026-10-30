package com.sahaaya.companion.core.retrieval

import com.sahaaya.companion.core.ProtocolChunk
import com.sahaaya.companion.core.RetrievalResult
import com.sahaaya.companion.core.nlp.ProcessedQuery
import kotlin.math.max

/**
 * Fuzzy lexical retrieval using character n-grams (trigrams) and token Jaccard similarity.
 * Resilient to typos, character drops, vowel variations, and regional spelling noise.
 */
class FuzzyLexicalRetriever(
    private val chunks: List<ProtocolChunk>
) : ProtocolRetriever {

    override val retrieverName: String = "FuzzyLexicalRetriever"

    private data class IndexedChunk(
        val chunk: ProtocolChunk,
        val tokenSet: Set<String>,
        val trigramSet: Set<String>
    )

    private val indexedChunks: List<IndexedChunk>

    init {
        indexedChunks = chunks.map { chunk ->
            val content = "${chunk.title} ${chunk.keywords.joinToString(" ")} ${chunk.approvedSynonyms.joinToString(" ")} ${chunk.text}".lowercase()
            val tokens = content.split(Regex("[^a-zA-Z0-9\\u0900-\\u097F\\u0C00-\\u0C7F]+")).filter { it.length > 1 }.toSet()
            val trigrams = extractTrigrams(content)
            IndexedChunk(chunk, tokens, trigrams)
        }
    }

    private fun extractTrigrams(text: String): Set<String> {
        val trigrams = mutableSetOf<String>()
        val clean = text.replace(Regex("\\s+"), " ")
        if (clean.length < 3) {
            trigrams.add(clean)
            return trigrams
        }
        for (i in 0 until clean.length - 2) {
            trigrams.add(clean.substring(i, i + 3))
        }
        return trigrams
    }

    private fun jaccardSimilarity(set1: Set<String>, set2: Set<String>): Double {
        if (set1.isEmpty() || set2.isEmpty()) return 0.0
        val intersectionSize = set1.intersect(set2).size
        val unionSize = set1.union(set2).size
        return if (unionSize > 0) intersectionSize.toDouble() / unionSize else 0.0
    }

    override fun rankCandidates(processedQuery: ProcessedQuery, topK: Int): List<ScoredCandidate> {
        val queryText = processedQuery.normalizedQuery.lowercase()
        val queryTokens = processedQuery.searchTokens.toSet()
        val queryTrigrams = extractTrigrams(queryText)

        val scored = mutableListOf<ScoredCandidate>()

        for (item in indexedChunks) {
            val tokenSim = jaccardSimilarity(queryTokens, item.tokenSet)
            val trigramSim = jaccardSimilarity(queryTrigrams, item.trigramSet)

            // Weight keywords & title
            var boost = 1.0
            val titleLow = item.chunk.title.lowercase()
            for (tok in queryTokens) {
                if (titleLow.contains(tok)) {
                    boost += 0.3
                }
            }

            val finalScore = (0.4 * tokenSim + 0.6 * trigramSim) * boost
            if (finalScore > 0.01) {
                scored.add(ScoredCandidate(item.chunk, finalScore, sourceEngine = retrieverName))
            }
        }

        return scored.sortedByDescending { it.score }
            .take(topK)
            .mapIndexed { idx, cand -> cand.copy(rank = idx + 1) }
    }

    override fun search(processedQuery: ProcessedQuery): RetrievalResult? {
        val candidates = rankCandidates(processedQuery, 1)
        if (candidates.isEmpty()) return null
        val best = candidates.first()
        return RetrievalResult(
            chunk = best.chunk,
            score = best.score,
            authoritativeAnswer = best.chunk.text.take(380),
            citedSectionIds = listOf(best.chunk.id),
            evidenceStrength = "FUZZY_LEXICAL_MATCH"
        )
    }
}
