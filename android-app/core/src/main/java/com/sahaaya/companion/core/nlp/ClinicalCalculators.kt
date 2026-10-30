package com.sahaaya.companion.core.nlp

import java.util.regex.Pattern

object ClinicalCalculators {

    data class CalculatorResult(
        val matched: Boolean,
        val targetSectionId: String,
        val answerText: String,
        val citedSectionIds: List<String>
    )

    private val hbPattern = Pattern.compile("(?:hb|haemoglobin|hemoglobin)\\s*(?:hai|is|tha|was|level)?\\s*(\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE)
    private val muacPattern = Pattern.compile("(?:muac|tape|mid[- ]upper arm)\\s*(?:hai|is|of)?\\s*(\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE)

    fun evaluate(query: String): CalculatorResult? {
        val low = query.lowercase().trim()

        // 1. Anaemia Cut-off Calculator (ANC-3.1, ANC-3.2)
        val hbMatcher = hbPattern.matcher(low)
        if (hbMatcher.find() && (low.contains("pregnan") || low.contains("garbh") || low.contains("trimester") || low.contains("mahine") || low.contains("kadupu"))) {
            val hb = hbMatcher.group(1)?.toDoubleOrNull()
            if (hb != null) {
                val isSecondTrimester = low.contains("2nd") || low.contains("second") || low.contains("doosra") || low.contains("rendava") || low.contains("4 mahine") || low.contains("5 mahine") || low.contains("6 mahine")
                val trimesterStr = if (isSecondTrimester) "2nd trimester" else "3rd trimester"

                val (grade, range) = when {
                    isSecondTrimester -> when {
                        hb >= 10.5 -> "no anaemia" to "10.5 g/dL or more"
                        hb >= 9.5 -> "mild anaemia" to "9.5-10.4 g/dL"
                        hb >= 7.0 -> "moderate anaemia" to "7.0-9.4 g/dL"
                        else -> "severe anaemia" to "below 7.0 g/dL"
                    }
                    else -> when {
                        hb >= 11.0 -> "no anaemia" to "11.0 g/dL or more"
                        hb >= 10.0 -> "mild anaemia" to "10.0-10.9 g/dL"
                        hb >= 7.0 -> "moderate anaemia" to "7.0-9.9 g/dL"
                        else -> "severe anaemia" to "below 7.0 g/dL"
                    }
                }

                val action = if (grade.contains("moderate") || grade.contains("mild")) {
                    "; treat as advised by ANM/MO (ANC-3.2)"
                } else if (grade.contains("severe")) {
                    "; refer immediately to FRU (ESC-2.5)"
                } else ""

                return CalculatorResult(
                    matched = true,
                    targetSectionId = "ANC-3.1",
                    answerText = "In the $trimesterStr, Hb $hb is $grade ($range) (ANC-3.1)$action.",
                    citedSectionIds = if (action.contains("ANC-3.2")) listOf("ANC-3.1", "ANC-3.2") else listOf("ANC-3.1")
                )
            }
        }

        // 2. MUAC Color Band Calculator (NUT-1.2)
        val muacMatcher = muacPattern.matcher(low)
        if (muacMatcher.find() || low.contains("muac") || (low.contains("tape") && (low.contains("green") || low.contains("yellow") || low.contains("red")))) {
            val muac = if (muacMatcher.find(0)) muacMatcher.group(1)?.toDoubleOrNull() else null
            if (muac != null) {
                val (band, status) = when {
                    muac >= 12.5 -> "green" to "no acute malnutrition"
                    muac >= 11.5 -> "yellow" to "moderate acute malnutrition (MAM)"
                    else -> "red" to "severe acute malnutrition (SAM)"
                }
                return CalculatorResult(
                    matched = true,
                    targetSectionId = "NUT-1.2",
                    answerText = "A MUAC of $muac cm is $band: $status (NUT-1.2).",
                    citedSectionIds = listOf("NUT-1.2")
                )
            } else if (low.contains("green")) {
                return CalculatorResult(
                    matched = true,
                    targetSectionId = "NUT-1.2",
                    answerText = "A MUAC of 12.5 cm or more is green: no acute malnutrition (NUT-1.2).",
                    citedSectionIds = listOf("NUT-1.2")
                )
            }
        }

        // 3. Physical Activity / Exercise Calculator for Hypertension and Diabetes (NCD-3.1)
        if ((low.contains("exercise") || low.contains("vyayam") || low.contains("kasrat") || low.contains("physical activity") || low.contains("walk")) &&
            (low.contains("sugar") || low.contains("diabetes") || low.contains("bp") || low.contains("pressure") || low.contains("ncd"))
        ) {
            return CalculatorResult(
                matched = true,
                targetSectionId = "NCD-3.1",
                answerText = "At least 30 minutes of physical activity at least 5 days a week (150 minutes a week) (NCD-3.1).",
                citedSectionIds = listOf("NCD-3.1")
            )
        }

        // 4. Calcium Administration with Meals vs Empty Stomach (ANC-2.3)
        if ((low.contains("calcium") || low.contains("calsem")) &&
            (low.contains("khali pet") || low.contains("empty stomach") || low.contains("nashta nahi") || low.contains("bina khaye"))
        ) {
            return CalculatorResult(
                matched = true,
                targetSectionId = "ANC-2.3",
                answerText = "No. Calcium tablets must be taken with meals, never on an empty stomach (ANC-2.3).",
                citedSectionIds = listOf("ANC-2.3")
            )
        }

        // 5. Postpartum IFA Duration (ANC-2.2, HBNC-3.1)
        if ((low.contains("ifa") || low.contains("iron") || low.contains("lal goli") || low.contains("red tablet")) &&
            (low.contains("delivery") || low.contains("prasav") || low.contains("postpartum") || low.contains("bachha hone ke baad") || low.contains("pnc"))
        ) {
            return CalculatorResult(
                matched = true,
                targetSectionId = "ANC-2.2",
                answerText = "Yes, one Red IFA tablet daily for 180 days after delivery (ANC-2.2, HBNC-3.1).",
                citedSectionIds = listOf("ANC-2.2", "HBNC-3.1")
            )
        }

        // 6. Child Vomiting with ORS (CHD-1.2)
        if ((low.contains("ors") || low.contains("jeevan ghol")) &&
            (low.contains("ulti") || low.contains("vomit") || low.contains("vaanthi")) &&
            !low.contains("sab ulti") && !low.contains("everything")
        ) {
            return CalculatorResult(
                matched = true,
                targetSectionId = "CHD-1.2",
                answerText = "If the child vomits, wait 10 minutes, then continue ORS more slowly in small sips (CHD-1.2). Vomiting everything is a danger sign (ESC-5.1).",
                citedSectionIds = listOf("CHD-1.2")
            )
        }

        // 7. Low Birth Weight Bathing Delay (HBNC-2.2)
        if ((low.contains("bath") || low.contains("nehla") || low.contains("snan")) &&
            (low.contains("wajan") || low.contains("weight") || low.contains("kg") || low.contains("lbw") || low.contains("kam") || low.contains("chhota"))
        ) {
            return CalculatorResult(
                matched = true,
                targetSectionId = "HBNC-2.2",
                answerText = "Delay bathing until the baby weighs at least 2000 g and gains weight steadily (HBNC-2.2).",
                citedSectionIds = listOf("HBNC-2.2")
            )
        }

        // 8. Vaccine Overdue Grace Period (VIS-1.3)
        if ((low.contains("overdue") || low.contains("grace period") || low.contains("late")) &&
            (low.contains("tika") || low.contains("vaccine") || low.contains("teeka") || low.contains("immuniz"))
        ) {
            return CalculatorResult(
                matched = true,
                targetSectionId = "VIS-1.3",
                answerText = "A vaccine becomes overdue 28 days after its due date (grace period of 4 weekly session cycles) (VIS-1.3).",
                citedSectionIds = listOf("VIS-1.3")
            )
        }

        // 9. Fourth ANC Schedule (ANC-1.2)
        if ((low.contains("chautha") || low.contains("4th") || low.contains("fourth") || low.contains("nalugava")) &&
            (low.contains("anc") || low.contains("checkup") || low.contains("jaanch"))
        ) {
            return CalculatorResult(
                matched = true,
                targetSectionId = "ANC-1.2",
                answerText = "The 4th ANC check-up is scheduled at 36 weeks (36-40 weeks) (ANC-1.2).",
                citedSectionIds = listOf("ANC-1.2")
            )
        }

        // 10. PMSMA Eligibility Window (ANC-1.3)
        if (low.contains("pmsma") || (low.contains("9th") && low.contains("pradhan mantri"))) {
            return CalculatorResult(
                matched = true,
                targetSectionId = "ANC-1.3",
                answerText = "PMSMA is for all pregnant women in their second or third trimester (after 12 weeks); not 1st trimester (ANC-1.3).",
                citedSectionIds = listOf("ANC-1.3")
            )
        }

        // 11. fIPV Minimum Gap (IMM-2.6)
        if ((low.contains("fipv") || low.contains("ipv")) && (low.contains("gap") || low.contains("antar") || low.contains("interval") || low.contains("beech"))) {
            return CalculatorResult(
                matched = true,
                targetSectionId = "IMM-2.6",
                answerText = "Minimum gap between fIPV doses is 8 weeks (56 days) (IMM-2.6).",
                citedSectionIds = listOf("IMM-2.6")
            )
        }

        // 12. Fast breathing rate in infants 2-11 months (CHD-3.1)
        if ((low.contains("saans") || low.contains("breath") || low.contains("respiratory")) &&
            (low.contains("tez") || low.contains("fast") || low.contains("rate") || low.contains("count") || low.contains("gin")) &&
            (low.contains("mahine") || low.contains("infant") || low.contains("shishu") || low.contains("month"))
        ) {
            return CalculatorResult(
                matched = true,
                targetSectionId = "CHD-3.1",
                answerText = "In infants 2-11 months, fast breathing is 50 breaths per minute or more (CHD-3.1).",
                citedSectionIds = listOf("CHD-3.1")
            )
        }

        // 13. JSSK Free Drop Back (ANC-5.3)
        if (low.contains("jssk") && (low.contains("wapas") || low.contains("drop") || low.contains("transport") || low.contains("ghar") || low.contains("free") || low.contains("gaadi"))) {
            return CalculatorResult(
                matched = true,
                targetSectionId = "ANC-5.3",
                answerText = "Yes, JSSK provides free transport back home after 48 hours of institutional delivery (ANC-5.3).",
                citedSectionIds = listOf("ANC-5.3")
            )
        }

        return null
    }
}
