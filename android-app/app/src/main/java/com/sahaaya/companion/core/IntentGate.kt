package com.sahaaya.companion.core

class IntentGate {

    fun evaluate(query: String): GateResult {
        val lower = query.lowercase().replace(Regex("[^a-z0-9\\s]"), " ")

        // Explicit exceptions for standard ASHA kit protocols:
        val isAshaKitProtocol = (lower.contains("ifa") || lower.contains("iron") || lower.contains("folic acid") || lower.contains("lal goli") || lower.contains("red ifa")) ||
                (lower.contains("zinc") && (lower.contains("tablet") || lower.contains("strip") || lower.contains("dast") || lower.contains("diarrhoea") || lower.contains("dose") || lower.contains("dosing"))) ||
                (lower.contains("ors") && (lower.contains("how much") || lower.contains("volume") || lower.contains("packet") || lower.contains("sachet") || lower.contains("jeevan jal") || lower.contains("pani") || lower.contains("pilate"))) ||
                (lower.contains("calcium") || lower.contains("calcium goli")) ||
                (lower.contains("albendazole") || lower.contains("keede") || lower.contains("deworming")) ||
                (lower.contains("tepid") || lower.contains("sponge") || lower.contains("sponging"))

        // 1. PC-PNDT Sex determination
        if (lower.contains("ladka") && (lower.contains("ladki") || lower.contains("sex") || lower.contains("gender") || lower.contains("jaanch"))) {
            return GateResult(
                isRefusal = true,
                refusalMessage = "Sex determination is prohibited by law under the PC-PNDT Act. Every pregnant woman must receive complete antenatal care (SCP-1.2).",
                reason = "PCPNDT_PROHIBITED",
                citedSectionId = "SCP-1.2"
            )
        }

        // 2. Administrative queries (salary, app issues, login, portal)
        val adminTerms = listOf(
            "salary", "incentive", "manthan", "login", "otp", "u-win", "uwin",
            "anmol", "app me", "ration", "ayushman", "praman", "certificate", "praman patra"
        )
        for (at in adminTerms) {
            if (lower.contains(at)) {
                return GateResult(
                    isRefusal = true,
                    refusalMessage = "Administrative and application support queries are outside clinical protocol scope. Please contact your Block Program Manager (SCP-1.2).",
                    reason = "ADMINISTRATIVE_OUT_OF_SCOPE",
                    citedSectionId = "SCP-1.2"
                )
            }
        }

        // 3. Uncovered adult conditions & specialized lab diagnostics
        val uncoveredDiag = listOf(
            "thyroid", "tsh", "tb ", "tuberculosis", "cancer", "dengue", "typhoid",
            "malaria report", "ultrasound", "usg", "x-ray", "xray", "bistar geela",
            "bedwetting", "enuresis", "garbhpat", "abortion", "hiv", "widal", "covid", "corona", "rabies"
        )
        for (ud in uncoveredDiag) {
            if (lower.contains(ud)) {
                return GateResult(
                    isRefusal = true,
                    refusalMessage = "Diagnosis and treatment of $ud is outside frontline ASHA protocol scope. Please refer the patient to a Medical Officer at the PHC (SCP-1.2).",
                    reason = "DIAGNOSIS_OUT_OF_SCOPE",
                    citedSectionId = "SCP-1.2"
                )
            }
        }

        // 4. Non-kit prescription medications & pharmaceutical dosing
        val rxDrugs = listOf(
            "paracetamol", "amoxicillin", "cefixime", "antibiotic", "azithromycin",
            "metformin", "amlodipine", "telmisartan", "insulin", "cough syrup", "syrup kitna",
            "kitni goli", "kitna ml", "dosage", "dose kitna", "kitna dena", "prescribe",
            "prescription", "drops calculation", "ciprofloxacin", "metronidazole", "steroid",
            "prescribe cheyyocha"
        )
        if (!isAshaKitProtocol) {
            for (rx in rxDrugs) {
                if (lower.contains(rx)) {
                    return GateResult(
                        isRefusal = true,
                        refusalMessage = "Medicine prescription ($rx) is outside frontline protocol scope. ASHAs/ANMs cannot prescribe non-kit pharmaceuticals. Please refer to a Medical Officer (SCP-1.2).",
                        reason = "PRESCRIPTION_OUT_OF_SCOPE",
                        citedSectionId = "SCP-1.2"
                    )
                }
            }
        }

        return GateResult(isRefusal = false)
    }
}
