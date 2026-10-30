package com.sahaaya.companion.core

class SahaayaPipeline(
    private val dangerRuleEngine: DangerRuleEngine,
    private val intentGate: IntentGate,
    private val bm25Retriever: Bm25Retriever
) {

    fun process(query: String): PipelineOutput {
        // Step 1: Hard-coded danger-sign escalation layer (ALWAYS FIRES FIRST)
        val dangerMatch = dangerRuleEngine.evaluate(query)
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

        // Step 2: Intent Gate (Refuses out-of-scope dosage, prescription, and diagnosis)
        val gateResult = intentGate.evaluate(query)
        if (gateResult.isRefusal) {
            return PipelineOutput.ScopeRefusal(
                message = gateResult.refusalMessage ?: "This request is outside the frontline protocol book. Please refer the patient to the Medical Officer or ANM at the PHC/CHC (SCP-1.2).",
                reason = gateResult.reason ?: "OUT_OF_SCOPE",
                citedSectionIds = listOf(gateResult.citedSectionId),
                recommendedFacility = "PHC / CHC (Medical Officer / ANM)"
            )
        }

        // Step 3 & 4: BM25 Retrieval over grounded official protocol chunks
        val retrieval = bm25Retriever.search(query)
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
}
