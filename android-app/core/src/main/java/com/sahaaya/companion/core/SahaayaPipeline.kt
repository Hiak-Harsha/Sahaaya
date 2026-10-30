package com.sahaaya.companion.core

import com.sahaaya.companion.core.nlp.ProcessedQuery
import com.sahaaya.companion.core.nlp.QueryUnderstandingEngine
import com.sahaaya.companion.core.retrieval.HybridProtocolRetriever

class SahaayaPipeline(
    private val dangerRuleEngine: DangerRuleEngine,
    private val intentGate: IntentGate,
    private val bm25Retriever: Bm25Retriever,
    private val hybridRetriever: HybridProtocolRetriever? = null
) {

    fun process(query: String): PipelineOutput {
        // Step 1: Multilingual Query Understanding & Normalization
        val processedQuery = QueryUnderstandingEngine.process(query)

        // Step 2: Hard-coded danger-sign escalation layer (ALWAYS FIRES FIRST)
        val dangerMatch = dangerRuleEngine.evaluate(query, processedQuery)
        if (dangerMatch.matched && dangerMatch.rule != null) {
            val rule = dangerMatch.rule
            val citedId = dangerMatch.citedSectionId ?: rule.chunkId
            val ans = dangerMatch.authoritativeAnswer ?: "${rule.dangerSign}: ${rule.action} (${rule.sourceCitation})."
            return PipelineOutput.DangerEscalation(
                rule = rule,
                matchedPhrase = dangerMatch.matchedPhrase ?: query,
                actionText = "REFER NOW",
                citation = "Source: $citedId, ${rule.sourceCitation}",
                citedSectionIds = listOf(citedId),
                authoritativeAnswer = ans,
                recommendedFacility = "CHC / FRU Hospital (Call 108)",
                evidenceStrength = "CRITICAL_DANGER_ESCALATION"
            )
        }

        // Step 2b: Conversational Guidance Gate (greetings and pleasantries)
        if (isConversational(query)) {
            return PipelineOutput.ConversationalGuidance()
        }

        // Step 3: Intent Gate (Refuses out-of-scope dosage, prescription, and diagnosis)
        val gateResult = intentGate.evaluate(query)
        if (gateResult.isRefusal) {
            return PipelineOutput.ScopeRefusal(
                message = gateResult.refusalMessage ?: "This request is outside the frontline protocol book. Please refer the patient to the Medical Officer or ANM at the PHC/CHC (SCP-1.2).",
                reason = gateResult.reason ?: "OUT_OF_SCOPE",
                citedSectionIds = listOf(gateResult.citedSectionId),
                recommendedFacility = "PHC / CHC (Medical Officer / ANM)"
            )
        }

        // Step 4: Grounded Retrieval
        // 1. Try baseline BM25 on original query first (guarantees zero regression on baseline dev set)
        // 2. Try hybrid retriever (combining clinical concepts, fuzzy lexical matching and BM25)
        // 3. Fallback to BM25 on transliterated & normalized query for multilingual/typo queries
        val retrieval = bm25Retriever.search(query)
            ?: hybridRetriever?.search(processedQuery)
            ?: bm25Retriever.search(processedQuery.normalizedQuery)

        if (retrieval == null) {
            return PipelineOutput.OutOfCorpus(
                message = "Not in my protocols, ask your ANM/doctor."
            )
        }

        val chunk = retrieval.chunk
        val citation = "Source: ${chunk.id}, ${chunk.sourceDocument}, p.${chunk.pageNumber}"
        return PipelineOutput.ProtocolAnswer(
            chunk = chunk,
            score = retrieval.score,
            citationText = citation,
            citedSectionIds = retrieval.citedSectionIds,
            authoritativeAnswer = retrieval.authoritativeAnswer ?: chunk.text,
            evidenceStrength = retrieval.evidenceStrength,
            recommendedFacility = if (chunk.id.startsWith("ESC")) "CHC / FRU" else "Sub-Centre / PHC"
        )
    }

    /**
     * Single official submission entry point required by WS0.
     * Guaranteed to match the exact evaluation format of evaluate.py.
     */
    fun answerForSubmission(query: String, questionId: String = ""): SubmissionRow {
        val t0 = System.nanoTime()
        val out = process(query)
        val latencyMs = (System.nanoTime() - t0) / 1_000_000.0
        val roundedLat = Math.round(latencyMs * 1000.0) / 1000.0

        return when (out) {
            is PipelineOutput.DangerEscalation -> SubmissionRow(
                question_id = questionId,
                answer_text = out.authoritativeAnswer.take(400),
                cited_section_ids = out.citedSectionIds.take(2),
                escalate = true,
                refused = false,
                latency_ms = roundedLat
            )
            is PipelineOutput.ScopeRefusal -> SubmissionRow(
                question_id = questionId,
                answer_text = out.message.take(400),
                cited_section_ids = out.citedSectionIds.take(2),
                escalate = false,
                refused = true,
                latency_ms = roundedLat
            )
            is PipelineOutput.ProtocolAnswer -> SubmissionRow(
                question_id = questionId,
                answer_text = out.authoritativeAnswer.take(400),
                cited_section_ids = out.citedSectionIds.take(2),
                escalate = false,
                refused = false,
                latency_ms = roundedLat
            )
            is PipelineOutput.OutOfCorpus -> SubmissionRow(
                question_id = questionId,
                answer_text = out.message.take(400),
                cited_section_ids = emptyList(),
                escalate = false,
                refused = true,
                latency_ms = roundedLat
            )
            is PipelineOutput.ConversationalGuidance -> SubmissionRow(
                question_id = questionId,
                answer_text = out.promptMessage.take(400),
                cited_section_ids = emptyList(),
                escalate = false,
                refused = false,
                latency_ms = roundedLat
            )
        }
    }

    fun isConversational(query: String): Boolean {
        val raw = query.trim()
        if (raw.isEmpty()) return true

        // Weakness fix (a): Do not strip Indian scripts into empty string.
        // If raw query contains Devanagari or Telugu characters, it is a clinical/domain query, not conversational.
        val hasIndicScript = raw.any { it in '\u0900'..'\u097F' || it in '\u0C00'..'\u0C7F' }
        if (hasIndicScript) return false

        val trimmed = raw.lowercase().replace(Regex("[^a-z0-9\\s]"), "")
        val words = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return false

        // Weakness fix (b): Clinical allowlist so short medical acronyms are not swallowed
        val clinicalAllowlist = setOf(
            "bp", "hb", "ors", "anc", "tt", "bcg", "opv", "mr", "ifa", "hbnc", "muac", "108", "102"
        )
        if (clinicalAllowlist.contains(trimmed) || words.any { clinicalAllowlist.contains(it) }) {
            return false
        }

        val exactPhrases = setOf(
            "hi", "hello", "hey", "hlo", "namaste", "namaskar", "namaskaram",
            "vanakkam", "pranam", "pranaam", "good morning", "good afternoon",
            "good evening", "shubh prabhat", "shubh sandhya", "kaise ho",
            "how are you", "sahaaya", "sahaya", "kya haal hai", "help",
            "madad", "sahayata", "test", "testing", "ok", "theek hai",
            "thank you", "thanks", "dhanyawad", "dhanyavadalu", "shukriya",
            "bye", "alvida", "good night", "hie", "helo", "hy", "hii", "hiii",
            "asdf"
        )
        if (exactPhrases.contains(trimmed)) return true

        val singleTokens = setOf(
            "hi", "hello", "hey", "hlo", "namaste", "namaskar", "namaskaram",
            "vanakkam", "pranam", "pranaam", "sahaaya", "sahaya", "hii", "hiii", "helo",
            "asdf"
        )
        if (words.size == 1 && singleTokens.contains(words[0])) return true

        if (trimmed.length < 3 && !trimmed.all { it.isDigit() } && !clinicalAllowlist.contains(trimmed)) {
            return true
        }

        return false
    }
}
