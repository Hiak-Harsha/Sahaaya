package com.sahaaya.companion.core.retrieval

import com.sahaaya.companion.core.Bm25Retriever
import com.sahaaya.companion.core.ProtocolChunk
import com.sahaaya.companion.core.RetrievalResult
import com.sahaaya.companion.core.nlp.ProcessedQuery

/**
 * Hybrid retrieval engine combining:
 * 1. BM25 inverted index with TF-IDF and length normalization
 * 2. Character n-gram and token fuzzy lexical matching
 * 3. Canonical clinical concept routing from multilingual query understanding
 * 4. Reciprocal Rank Fusion (RRF) for explainable consensus ranking
 */
class HybridProtocolRetriever(
    private val chunks: List<ProtocolChunk>,
    private val bm25Retriever: Bm25Retriever,
    private val fuzzyRetriever: FuzzyLexicalRetriever
) : ProtocolRetriever {

    override val retrieverName: String = "HybridProtocolRetriever"

    private val chunkMap = chunks.associateBy { it.id }

    // Weighting parameters for Reciprocal Rank Fusion
    private val rrfK = 60.0
    private val weightBm25 = 1.0
    private val weightFuzzy = 0.8
    private val weightConcept = 1.5

    override fun rankCandidates(processedQuery: ProcessedQuery, topK: Int): List<ScoredCandidate> {
        val rrfScores = mutableMapOf<String, Double>()

        // 1. Concept-based direct candidate boost
        for (concept in processedQuery.extractedConcepts) {
            val sectionId = concept.primaryProtocolSection
            if (chunkMap.containsKey(sectionId)) {
                val current = rrfScores.getOrDefault(sectionId, 0.0)
                rrfScores[sectionId] = current + (weightConcept / (rrfK + 1))
            }
        }

        // 2. Fuzzy Lexical Candidates
        val fuzzyCandidates = fuzzyRetriever.rankCandidates(processedQuery, topK = 10)
        for (cand in fuzzyCandidates) {
            val sid = cand.chunk.id
            val current = rrfScores.getOrDefault(sid, 0.0)
            rrfScores[sid] = current + (weightFuzzy / (rrfK + cand.rank))
        }

        // 3. BM25 Candidates (via search)
        val bm25Result = bm25Retriever.search(processedQuery.normalizedQuery)
        if (bm25Result != null) {
            val sid = bm25Result.chunk.id
            val current = rrfScores.getOrDefault(sid, 0.0)
            rrfScores[sid] = current + (weightBm25 / (rrfK + 1))
        }

        // Sort descending by merged RRF score
        val sorted = rrfScores.entries
            .mapNotNull { entry ->
                chunkMap[entry.key]?.let { chunk ->
                    ScoredCandidate(
                        chunk = chunk,
                        score = entry.value,
                        sourceEngine = retrieverName
                    )
                }
            }
            .sortedByDescending { it.score }
            .take(topK)
            .mapIndexed { idx, cand -> cand.copy(rank = idx + 1) }

        return sorted
    }

    override fun search(processedQuery: ProcessedQuery): RetrievalResult? {
        // First check BM25 direct result
        val bm25Result = bm25Retriever.search(processedQuery.normalizedQuery)
        if (bm25Result != null && bm25Result.score >= 50.0) {
            return bm25Result
        }

        val candidates = rankCandidates(processedQuery, topK = 3)
        if (candidates.isEmpty()) {
            return bm25Result
        }

        val bestCandidate = candidates.first()
        // If BM25 found something with moderate score and matches top candidate, prefer BM25 grounded answer
        if (bm25Result != null && bm25Result.chunk.id == bestCandidate.chunk.id) {
            return bm25Result
        }

        // Build fallback answer from best candidate
        return RetrievalResult(
            chunk = bestCandidate.chunk,
            score = bestCandidate.score,
            authoritativeAnswer = bestCandidate.chunk.text.take(380),
            citedSectionIds = listOf(bestCandidate.chunk.id),
            evidenceStrength = "HYBRID_RRF_CONSENSUS"
        )
    }
}
