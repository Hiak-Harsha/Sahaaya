package com.sahaaya.companion.core

class IntentGate {

    private val inScopeKitTerms = listOf(
        "ifa khilana", "calcium goli", "ors pilate", "ors", "zinc",
        "red ifa", "iron tablet", "tepid sponge", "sponging"
    )

    private val refusalKeywords = listOf(
        // Non-kit dosage calculation
        "syrup kitna", "paracetamol syrup", "paracetamol dose", "kitni dawai", "koi dawai",
        "dosage of", "dose of", "kitna dose", "how much dose", "drops calculation", "syrup dosage",
        // Prescription drugs & antibiotics
        "antibiotic", "amoxicillin", "cefixime", "ciprofloxacin", "metformin", "amlodipine",
        "azithromycin", "metronidazole", "telmisartan", "insulin", "steroid", "prescribe cheyyocha",
        // Technical / Admin / Non-clinical disputes
        "u-win", "login nahi", "otp nahi", "salary", "incentive", "ration card", "ayushman card",
        "certificate", "praman patra",
        // Out-of-scope diagnostics & conditions
        "hiv", "typhoid", "widal", "dengue", "malaria treatment", "covid", "corona", "rabies",
        "bistar geela", "enuresis", "garbhpat", "abortion"
    )

    fun evaluate(query: String): GateResult {
        val low = query.lowercase().trim()

        // 1. Explicit exceptions for frontline ASHA kit protocols
        val isAshaKitProtocol = inScopeKitTerms.any { low.contains(it) }
        val hasDosageRequest = low.contains("syrup kitna") || low.contains("kitni dawai") ||
                low.contains("paracetamol syrup") || low.contains("paracetamol dose") ||
                low.contains("dose of") || low.contains("antibiotic")

        if (isAshaKitProtocol && !hasDosageRequest) {
            return GateResult(isRefusal = false)
        }

        // 2. Check refusal triggers
        for (kw in refusalKeywords) {
            if (low.contains(kw)) {
                return GateResult(
                    isRefusal = true,
                    refusalMessage = "This request is outside the frontline protocol book. Please refer the patient to the Medical Officer or ANM at the PHC/CHC (SCP-1.2).",
                    reason = "OUT_OF_SCOPE",
                    citedSectionId = "SCP-1.2"
                )
            }
        }

        return GateResult(isRefusal = false)
    }
}
