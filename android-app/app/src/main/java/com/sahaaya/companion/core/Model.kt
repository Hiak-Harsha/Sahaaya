package com.sahaaya.companion.core

data class ProtocolChunk(
    val id: String,
    val title: String,
    val language: String = "en",
    val text: String,
    val sourceDocument: String = "",
    val sourceUrl: String = "",
    val pageNumber: Int = 1,
    val parentId: String = "",
    val keywords: List<String> = emptyList(),
    val approvedSynonyms: List<String> = emptyList(),
    val protocolVersion: String = "v1.0",
    val contentHash: String = ""
)

data class DangerRule(
    val id: String,
    val chunkId: String,
    val category: String,
    val dangerSign: String,
    val action: String,
    val sourceCitation: String,
    val note: String,
    val phrasings: Map<String, List<String>>
)

data class DangerMatchResult(
    val matched: Boolean,
    val rule: DangerRule? = null,
    val matchedPhrase: String? = null,
    val citedSectionId: String? = null,
    val authoritativeAnswer: String? = null
)

data class GateResult(
    val isRefusal: Boolean,
    val refusalMessage: String? = null,
    val reason: String? = null,
    val citedSectionId: String = "SCP-1.2"
)

data class RetrievalResult(
    val chunk: ProtocolChunk,
    val score: Double,
    val authoritativeAnswer: String? = null,
    val citedSectionIds: List<String> = listOf(chunk.id),
    val evidenceStrength: String = "STRONG_PROTOCOL_EVIDENCE"
)

sealed class PipelineOutput {
    data class DangerEscalation(
        val rule: DangerRule,
        val matchedPhrase: String,
        val actionText: String = "REFER NOW",
        val citation: String,
        val citedSectionIds: List<String> = listOf(rule.chunkId),
        val authoritativeAnswer: String = "",
        val recommendedFacility: String = "CHC / FRU (First Referral Unit)",
        val evidenceStrength: String = "STRONG_PROTOCOL_EVIDENCE"
    ) : PipelineOutput()

    data class ScopeRefusal(
        val message: String,
        val reason: String,
        val citedSectionIds: List<String> = listOf("SCP-1.2"),
        val recommendedFacility: String = "PHC / CHC (Medical Officer / ANM)"
    ) : PipelineOutput()

    data class ProtocolAnswer(
        val chunk: ProtocolChunk,
        val score: Double,
        val citationText: String,
        val citedSectionIds: List<String> = listOf(chunk.id),
        val authoritativeAnswer: String = "",
        val evidenceStrength: String = "STRONG_PROTOCOL_EVIDENCE",
        val recommendedFacility: String = "Sub-Centre / PHC"
    ) : PipelineOutput()

    data class OutOfCorpus(
        val message: String = "Not in my protocols, ask your ANM/doctor.",
        val citedSectionIds: List<String> = emptyList()
    ) : PipelineOutput()
}

data class RegisterMember(
    val familyId: String = "",
    val memberId: String,
    val name: String,
    val memberType: String = "",
    val targetDate: String = "",
    val lastVisitDate: String = "",
    val dangerFlag: String = "NONE",
    val vaccinesGiven: String = "",
    val nextDueItem: String = "",
    val dueDate: String = "",
    // Full challenge dataset fields:
    val relationToHead: String = "",
    val sex: String = "",
    val dob: String = "",
    val pregnant: String = "N",
    val lmpDate: String = "",
    val eddDate: String = "",
    val deliveryDate: String = "",
    val deliveryPlace: String = "",
    val gravida: String = "",
    val parity: String = "",
    val lastBp: String = "",
    val lastBloodSugar: String = "",
    val reportedSymptoms: String = "",
    val riskFlags: String = "",
    val phone: String = "",
    val assignedAshaId: String = ""
)

enum class PriorityGroup {
    DANGER,   // Priority 1
    OVERDUE,  // Priority 2
    UPCOMING  // Priority 3
}

data class PrioritizedVisit(
    val member: RegisterMember,
    val priorityGroup: PriorityGroup,
    val priorityRank: Int, // 1, 2, or 3
    val urgencyBadge: String,
    val reason: String,
    val daysDiff: Long,
    val actionProtocolId: String = "",
    val recommendedFacility: String = "Sub-Centre / PHC"
)
