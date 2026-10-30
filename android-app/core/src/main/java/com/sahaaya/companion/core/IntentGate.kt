package com.sahaaya.companion.core

class IntentGate {

    fun evaluate(query: String): GateResult {
        val lower = query.lowercase().replace(Regex("[^a-z0-9\\s]"), " ")

        // Explicit pharmaceutical dosing queries (e.g. "syrup kitna", "kitni goli", "kitna ml", "dosage", "dose kitna") are OUT OF SCOPE:
        val isExplicitDoseInquiry = lower.contains("syrup kitna") || lower.contains("kitni goli") ||
                lower.contains("kitna ml") || lower.contains("dose kitna") || lower.contains("kitna dena") ||
                lower.contains("drops calculation") || lower.contains("titrat")

        // Standard Frontline Kit Protocols (MUST NOT be refused unless asking for prescription dosing):
        val isAshaKitProtocol = !isExplicitDoseInquiry && (
                (lower.contains("ifa") || lower.contains("iron") || lower.contains("folic acid") || lower.contains("lal goli") || lower.contains("red ifa")) ||
                (lower.contains("zinc") && (lower.contains("tablet") || lower.contains("strip") || lower.contains("dast") || lower.contains("diarrhoea"))) ||
                (lower.contains("ors") && (lower.contains("how much") || lower.contains("volume") || lower.contains("packet") || lower.contains("sachet") || lower.contains("jeevan jal") || lower.contains("pani") || lower.contains("gholna"))) ||
                (lower.contains("calcium") || lower.contains("calcium goli")) ||
                (lower.contains("albendazole") || lower.contains("keede") || lower.contains("deworming")) ||
                (lower.contains("tepid") || lower.contains("sponge") || lower.contains("sponging")) ||
                (lower.contains("muac") || lower.contains("bicep") || lower.contains("midpoint")) ||
                (lower.contains("thermometer") || lower.contains("digital thermometer") || lower.contains("axilla")) ||
                (lower.contains("urine albumin") || (lower.contains("urine") && lower.contains("albumin"))) ||
                (lower.contains("glucometer") && lower.contains("fasting"))
        )

        // 1. PC-PNDT Sex determination (Strictly illegal)
        if (lower.contains("ladka") && (lower.contains("ladki") || lower.contains("sex") || lower.contains("gender") || lower.contains("jaanch"))) {
            return buildRefusal(
                "Sex determination is prohibited by law under the PC-PNDT Act. Every pregnant woman must receive complete antenatal care (SCP-1.2).",
                reason = "PCPNDT_PROHIBITED"
            )
        }

        // 2. Invasive surgical procedures & specialized interventions
        val invasiveProcedures = listOf(
            "iv drip", "saline bottle", "episiotomy", "tanke", "suture", "catheter",
            "urine pipe", "surgical incision", "incision", "phlebotomy", "blood sample nikal",
            "appendicitis", "surgery", "operation", "c section", "caesarean", "lumbar puncture",
            "spinal tap", "tooth extraction", "daant nikal", "plaster cast", "bone plaster", "plaster",
            "tracheostomy", "suctioning", "copper t", "copper-t", "iucd", "feeding pipe", "nasal feeding"
        )
        for (ip in invasiveProcedures) {
            if (lower.contains(ip)) {
                return buildRefusal(
                    "Invasive surgical procedures ($ip) are outside frontline worker scope. Please refer the patient to a Medical Officer or Doctor at PHC / CHC (SCP-1.2).",
                    reason = "PROCEDURE_OUT_OF_SCOPE"
                )
            }
        }

        // 3. Advanced diagnostic imaging, laboratory tests & specialized reports
        val diagnosticImaging = listOf(
            "ecg", "x ray", "xray", "ct scan", "mri", "ultrasound", "usg",
            "endoscopy", "biopsy", "blood culture", "widal", "malaria report",
            "liver function", "sgpt", "sgot", "hepatitis", "dexa scan", "bone density", "t score"
        )
        for (di in diagnosticImaging) {
            if (lower.contains(di)) {
                return buildRefusal(
                    "Advanced diagnostic test interpretation ($di) is outside frontline worker scope. Please refer the patient to a Medical Officer or Doctor at PHC / CHC (SCP-1.2).",
                    reason = "DIAGNOSIS_OUT_OF_SCOPE"
                )
            }
        }

        // 4. Specialty medicine (Oncology, Nephrology, Surgery)
        val specialtyMedicine = listOf(
            "chemotherapy", "cancer infusion", "dialysis", "kidney dialysis",
            "organ transplant", "transplant", "immunosuppressant"
        )
        for (sm in specialtyMedicine) {
            if (lower.contains(sm)) {
                return buildRefusal(
                    "Specialty care ($sm) is outside frontline protocol scope. Please refer the patient to a Medical Officer or Doctor at PHC / CHC (SCP-1.2).",
                    reason = "SPECIALTY_OUT_OF_SCOPE"
                )
            }
        }

        // 5. Legal, administrative, police disputes & certification
        val legalAdminTerms = listOf(
            "property", "jameen", "jaydad", "batwara", "domestic violence", "police fir",
            "chunav", "election", "voting booth", "bank loan", "guarantee", "caste certificate",
            "jaati praman", "court", "gawahi", "witness", "post mortem", "autopsy",
            "insurance company claim", "insurance claim", "passport", "driving license",
            "salary", "incentive", "manthan", "login", "otp", "u win", "uwin",
            "anmol", "ration", "ayushman", "medico legal", "mlc", "wound certificate",
            "death certificate", "cause of death", "handicap pass", "railway pass", "concession pass"
        )
        for (lat in legalAdminTerms) {
            if (lower.contains(lat)) {
                return buildRefusal(
                    "Legal, administrative, and external documentation queries ($lat) are outside clinical protocol scope. Please refer to Block Program Manager or appropriate authority (SCP-1.2).",
                    reason = "ADMINISTRATIVE_OUT_OF_SCOPE"
                )
            }
        }

        // 6. Uncovered adult chronic/infectious conditions
        val uncoveredConditions = listOf(
            "thyroid", "tsh", "tb ", "tuberculosis", "cancer", "dengue", "typhoid",
            "bistar geela", "bedwetting", "enuresis", "garbhpat", "abortion", "hiv", "covid", "corona", "rabies"
        )
        for (uc in uncoveredConditions) {
            if (lower.contains(uc)) {
                return buildRefusal(
                    "Diagnosis and management of $uc is outside frontline protocol scope. Please refer the patient to a Medical Officer or Doctor at PHC / CHC (SCP-1.2).",
                    reason = "DIAGNOSIS_OUT_OF_SCOPE"
                )
            }
        }

        // 7. Non-kit prescription pharmaceuticals & dosing titration
        val rxDrugs = listOf(
            "paracetamol syrup kitna", "paracetamol dose", "amoxicillin", "cefixime", "antibiotic", "azithromycin",
            "metformin", "amlodipine", "telmisartan", "insulin", "cough syrup", "syrup kitna",
            "kitni goli", "kitna ml", "dosage", "dose kitna", "kitna dena", "prescribe",
            "prescription", "drops calculation", "ciprofloxacin", "metronidazole", "steroid",
            "dexamethasone", "tramadol", "sleeping pills", "sleeping pill", "akt 4",
            "eye drops", "injection lagane", "prescribe cheyyocha"
        )
        if (!isAshaKitProtocol) {
            for (rx in rxDrugs) {
                if (lower.contains(rx)) {
                    return buildRefusal(
                        "Prescription pharmaceutical administration ($rx) is outside frontline worker scope. ASHAs/ANMs cannot prescribe non-kit drugs. Please refer the patient to a Medical Officer or Doctor at PHC / CHC (SCP-1.2).",
                        reason = "PRESCRIPTION_OUT_OF_SCOPE"
                    )
                }
            }
        }

        return GateResult(isRefusal = false)
    }

    private fun buildRefusal(detailMessage: String, reason: String = "OUT_OF_SCOPE"): GateResult {
        val standardMsg = "$detailMessage Frontline health workers must redirect non-protocol queries to a Medical Officer or Doctor at PHC / CHC (SCP-1.2)."
        return GateResult(
            isRefusal = true,
            refusalMessage = standardMsg,
            reason = reason,
            citedSectionId = "SCP-1.2"
        )
    }
}
