package com.sahaaya.companion.core.retrieval

import com.sahaaya.companion.core.ProtocolChunk
import com.sahaaya.companion.core.RetrievalResult
import com.sahaaya.companion.core.nlp.ProcessedQuery

data class ScoredCandidate(
    val chunk: ProtocolChunk,
    val score: Double,
    val rank: Int = 0,
    val sourceEngine: String = "BM25"
)

/**
 * Common modular retrieval contract for protocol evidence discovery.
 */
interface ProtocolRetriever {
    val retrieverName: String

    /**
     * Finds the best matching protocol section for a processed query.
     */
    fun search(processedQuery: ProcessedQuery): RetrievalResult?

    /**
     * Returns top ranked candidate sections for multi-evidence evaluation.
     */
    fun rankCandidates(processedQuery: ProcessedQuery, topK: Int = 5): List<ScoredCandidate>
}
