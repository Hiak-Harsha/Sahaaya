package com.sahaaya.companion.core.nlp

enum class DetectedScript {
    LATIN,
    DEVANAGARI,
    TELUGU,
    MIXED,
    UNKNOWN
}

enum class DetectedLanguage {
    ENGLISH,
    HINDI,
    ROMAN_HINDI,
    TELUGU,
    TELUGU_LATIN,
    HINGLISH,
    TELUGU_ENGLISH_MIXED,
    UNKNOWN
}

enum class ClinicalCategory {
    MATERNAL,
    NEWBORN,
    CHILD_HEALTH,
    NCD,
    NUTRITION,
    IMMUNIZATION,
    GENERAL_EMERGENCY
}

data class CanonicalClinicalConcept(
    val id: String,
    val canonicalName: String,
    val matchedPhrase: String,
    val confidence: Double,
    val isNegated: Boolean,
    val category: ClinicalCategory,
    val primaryProtocolSection: String
)

data class ProcessedQuery(
    val originalQuery: String,
    val normalizedQuery: String,
    val primaryScript: DetectedScript,
    val detectedLanguages: List<DetectedLanguage>,
    val extractedConcepts: List<CanonicalClinicalConcept>,
    val hasNegation: Boolean,
    val negationTerms: List<String>,
    val isEducationalQuery: Boolean,
    val isEmergencyCandidate: Boolean,
    val searchTokens: List<String>
)

object QueryUnderstandingEngine {

    private val devanagariRegex = Regex("[\\u0900-\\u097F]")
    private val teluguRegex = Regex("[\\u0C00-\\u0C7F]")
    private val latinRegex = Regex("[a-zA-Z]")

    private val negationHindi = listOf("nahi", "nahin", "na", "mat", "bina", "नहीं", "ना", "मत", "बिना")
    private val negationTelugu = listOf("ledu", "kaadu", "raaledu", "లేవు", "లేదు", "కాదు", "రావట్లేదు")
    private val negationEnglish = listOf("no", "not", "without", "denies", "unable", "refusing", "cannot", "absence")

    private val educationalPhrases = listOf(
        "kaise check karte",
        "kaise naapte",
        "kab maana jaata",
        "kitni ho to",
        "green kab",
        "how to measure",
        "normal range",
        "schedule kya hai",
        "what is the dose",
        "how to check",
        "definition of",
        "kab dena chahiye",
        "ela cheppali",
        "ela kolustaru"
    )

    fun identifyScript(text: String): DetectedScript {
        val hasDev = devanagariRegex.containsMatchIn(text)
        val hasTel = teluguRegex.containsMatchIn(text)
        val hasLat = latinRegex.containsMatchIn(text)

        val scriptCount = (if (hasDev) 1 else 0) + (if (hasTel) 1 else 0) + (if (hasLat) 1 else 0)
        return when {
            scriptCount > 1 -> DetectedScript.MIXED
            hasDev -> DetectedScript.DEVANAGARI
            hasTel -> DetectedScript.TELUGU
            hasLat -> DetectedScript.LATIN
            else -> DetectedScript.UNKNOWN
        }
    }

    fun identifyLanguages(text: String, script: DetectedScript): List<DetectedLanguage> {
        val low = text.lowercase()
        val languages = mutableListOf<DetectedLanguage>()

        val teluguTerms = listOf("virochanalu", "paalu", "aayasam", "jwaram", "vaanthulu", "kadupu", "noppi", "ledu", "pilladu", "challaga")
        val hindiTerms = listOf("bukhar", "jhatke", "pasli", "doodh", "nahi", "bachcha", "garbhvati", "prasav", "ulti", "dast", "khoon", "sujan", "behosh")
        val englishTerms = listOf("fever", "baby", "child", "pregnant", "delivery", "blood", "vomiting", "cough", "pressure", "sugar", "month", "days")

        val hasTeluguVocab = teluguTerms.any { low.contains(it) } || script == DetectedScript.TELUGU
        val hasHindiVocab = hindiTerms.any { low.contains(it) } || script == DetectedScript.DEVANAGARI
        val hasEnglishVocab = englishTerms.any { low.contains(it) }

        if (script == DetectedScript.TELUGU) {
            languages.add(DetectedLanguage.TELUGU)
        }
        if (script == DetectedScript.DEVANAGARI) {
            languages.add(DetectedLanguage.HINDI)
        }

        if (hasTeluguVocab && hasEnglishVocab) {
            languages.add(DetectedLanguage.TELUGU_ENGLISH_MIXED)
        } else if (hasTeluguVocab) {
            languages.add(DetectedLanguage.TELUGU_LATIN)
        }

        if (hasHindiVocab && hasEnglishVocab) {
            languages.add(DetectedLanguage.HINGLISH)
        } else if (hasHindiVocab && script == DetectedScript.LATIN) {
            languages.add(DetectedLanguage.ROMAN_HINDI)
        }

        if (languages.isEmpty()) {
            languages.add(DetectedLanguage.ENGLISH)
        }
        return languages.distinct()
    }

    private fun detectNegation(normalizedText: String): Pair<Boolean, List<String>> {
        val tokens = normalizedText.split(" ")
        val found = mutableListOf<String>()
        val allNegations = negationHindi + negationTelugu + negationEnglish
        for (tok in tokens) {
            if (allNegations.contains(tok)) {
                found.add(tok)
            }
        }
        return Pair(found.isNotEmpty(), found)
    }

    private fun extractConcepts(normalizedText: String, hasNegation: Boolean): List<CanonicalClinicalConcept> {
        val concepts = mutableListOf<CanonicalClinicalConcept>()
        val low = normalizedText.lowercase()

        // 1. Chest Indrawing (ESC-5.2)
        if (low.contains("pasli") || low.contains("chest indrawing") || (low.contains("ribs") && low.contains("andar")) || low.contains("seena dab")) {
            val isNeg = hasNegation && (low.contains("pasli nahi") || low.contains("no chest indrawing"))
            concepts.add(
                CanonicalClinicalConcept(
                    id = "CHEST_INDRAWING",
                    canonicalName = "Severe Chest Indrawing / Respiratory Distress",
                    matchedPhrase = "pasli dhansna / chest indrawing",
                    confidence = 0.98,
                    isNegated = isNeg,
                    category = ClinicalCategory.CHILD_HEALTH,
                    primaryProtocolSection = "ESC-5.2"
                )
            )
        }

        // 2. Convulsions / Fits (ESC-5.1 or ESC-2.2)
        if (low.contains("jhatke") || low.contains("convuls") || low.contains("fits") || low.contains("mirgi") || low.contains("murcha")) {
            val isNeg = hasNegation && (low.contains("jhatke nahi") || low.contains("no fits") || low.contains("no convulsion"))
            val isMaternal = low.contains("pregnant") || low.contains("garbhvati") || low.contains("delivery")
            concepts.add(
                CanonicalClinicalConcept(
                    id = "CONVULSIONS",
                    canonicalName = if (isMaternal) "Eclampsia / Convulsions in Pregnancy" else "Convulsions / Fits with Fever",
                    matchedPhrase = "jhatke / fits",
                    confidence = 0.99,
                    isNegated = isNeg,
                    category = if (isMaternal) ClinicalCategory.MATERNAL else ClinicalCategory.CHILD_HEALTH,
                    primaryProtocolSection = if (isMaternal) "ESC-2.2" else "ESC-5.1"
                )
            )
        }

        // 3. Child not feeding / drinking (ESC-5.1)
        if ((low.contains("doodh") || low.contains("milk") || low.contains("breastfeed") || low.contains("paalu")) &&
            (low.contains("nahi") || low.contains("unable") || low.contains("cannot") || low.contains("refus") || low.contains("ledu"))
        ) {
            concepts.add(
                CanonicalClinicalConcept(
                    id = "INABILITY_TO_FEED",
                    canonicalName = "Child unable to drink or breastfeed",
                    matchedPhrase = "doodh nahi pee raha / unable to breastfeed",
                    confidence = 0.97,
                    isNegated = false, // The feeding failure itself is the symptom
                    category = ClinicalCategory.CHILD_HEALTH,
                    primaryProtocolSection = "ESC-5.1"
                )
            )
        }

        // 4. Hypothermia / Cold to touch (ESC-3.2)
        if ((low.contains("thanda") || low.contains("cold") || low.contains("challaga")) &&
            (low.contains("chhune") || low.contains("touch") || low.contains("skin") || low.contains("body") || low.contains("bachcha"))
        ) {
            concepts.add(
                CanonicalClinicalConcept(
                    id = "NEONATAL_HYPOTHERMIA",
                    canonicalName = "Newborn Cold to Touch / Hypothermia",
                    matchedPhrase = "thanda lag raha / cold to touch",
                    confidence = 0.96,
                    isNegated = false,
                    category = ClinicalCategory.NEWBORN,
                    primaryProtocolSection = "ESC-3.2"
                )
            )
        }

        // 5. Unconscious / Coma (ESC-6.3)
        if (low.contains("behosh") || low.contains("unconscious") || low.contains("coma") || low.contains("spruha tappadam")) {
            concepts.add(
                CanonicalClinicalConcept(
                    id = "UNCONSCIOUSNESS",
                    canonicalName = "Unconsciousness / Coma",
                    matchedPhrase = "behosh / unconscious",
                    confidence = 0.99,
                    isNegated = false,
                    category = ClinicalCategory.NCD,
                    primaryProtocolSection = "ESC-6.3"
                )
            )
        }

        // 6. Postpartum / Obstetric Haemorrhage (ESC-1.2 or ESC-1.1)
        if ((low.contains("pad") && low.contains("minute") && (low.contains("bheeg") || low.contains("soak"))) ||
            (low.contains("khoon") && low.contains("beh") && (low.contains("tez") || low.contains("heavy") || low.contains("delivery")))
        ) {
            concepts.add(
                CanonicalClinicalConcept(
                    id = "POSTPARTUM_HAEMORRHAGE",
                    canonicalName = "Postpartum Haemorrhage (PPH) / Heavy Bleeding",
                    matchedPhrase = "pad bheeg gaya / heavy bleeding",
                    confidence = 0.99,
                    isNegated = false,
                    category = ClinicalCategory.MATERNAL,
                    primaryProtocolSection = "ESC-1.2"
                )
            )
        }

        // 7. Prolonged Fever >= 7 days (ESC-5.3)
        if ((low.contains("9 din") || low.contains("7 din") || low.contains("8 din") || low.contains("10 din") || low.contains("hafte se") || low.contains("week") || low.contains("prolonged")) &&
            (low.contains("bukhar") || low.contains("fever") || low.contains("taap") || low.contains("jwaram"))
        ) {
            concepts.add(
                CanonicalClinicalConcept(
                    id = "PROLONGED_FEVER",
                    canonicalName = "Fever lasting 7 days or more",
                    matchedPhrase = "din se bukhar / prolonged fever",
                    confidence = 0.98,
                    isNegated = false,
                    category = ClinicalCategory.CHILD_HEALTH,
                    primaryProtocolSection = "ESC-5.3"
                )
            )
        }

        // 8. Severe Pallor / Anaemia with breathlessness in 3rd trimester (ESC-2.5)
        if ((low.contains("peeli") || low.contains("peela") || low.contains("pallor") || low.contains("anaemia")) &&
            (low.contains("saans phool") || low.contains("breathless") || low.contains("chalne pe")) &&
            (low.contains("mahina") || low.contains("trimester") || low.contains("pregnant") || low.contains("garbhvati"))
        ) {
            concepts.add(
                CanonicalClinicalConcept(
                    id = "SEVERE_PALLOR_PREGNANCY",
                    canonicalName = "Severe Pallor with Breathlessness in Pregnancy",
                    matchedPhrase = "peeli saans phoolna / severe pallor",
                    confidence = 0.98,
                    isNegated = false,
                    category = ClinicalCategory.MATERNAL,
                    primaryProtocolSection = "ESC-2.5"
                )
            )
        }

        // 9. Hyperemesis Gravidarum (ESC-2.4)
        if ((low.contains("ulti") || low.contains("vomit") || low.contains("vaanthulu")) &&
            (low.contains("paani nahi") || low.contains("pet me nahi") || low.contains("unable to keep") || low.contains("din bhar")) &&
            (low.contains("pregnant") || low.contains("mahine") || low.contains("garbhvati"))
        ) {
            concepts.add(
                CanonicalClinicalConcept(
                    id = "HYPEREMESIS_GRAVIDARUM",
                    canonicalName = "Severe Vomiting / Inability to retain fluids in Pregnancy",
                    matchedPhrase = "din bhar ulti paani nahi tik raha",
                    confidence = 0.98,
                    isNegated = false,
                    category = ClinicalCategory.MATERNAL,
                    primaryProtocolSection = "ESC-2.4"
                )
            )
        }

        // 10. Low Birth Weight < 1.8 kg (ESC-3.1)
        if (low.contains("1.5 kg") || low.contains("1.6 kg") || low.contains("1.4 kg") || low.contains("1.2 kg") || low.contains("1.0 kg") || low.contains("< 1.8")) {
            concepts.add(
                CanonicalClinicalConcept(
                    id = "VERY_LOW_BIRTH_WEIGHT",
                    canonicalName = "Very Low Birth Weight (< 1800g)",
                    matchedPhrase = "vajan < 1.8 kg",
                    confidence = 0.99,
                    isNegated = false,
                    category = ClinicalCategory.NEWBORN,
                    primaryProtocolSection = "ESC-3.1"
                )
            )
        }

        return concepts
    }

    /**
     * Executes the comprehensive query understanding pipeline.
     */
    fun process(query: String): ProcessedQuery {
        val original = query.trim()
        val script = identifyScript(original)
        val normalized = TransliterationNormalizer.normalize(original)
        val languages = identifyLanguages(original, script)

        val (hasNeg, negTerms) = detectNegation(normalized)
        val isEdu = educationalPhrases.any { normalized.lowercase().contains(it) }

        val concepts = extractConcepts(normalized, hasNeg)
        val isEmergency = concepts.any { !it.isNegated && it.primaryProtocolSection.startsWith("ESC") } && !isEdu

        val tokens = normalized.lowercase()
            .split(" ")
            .filter { it.isNotBlank() && it.length > 1 }

        return ProcessedQuery(
            originalQuery = original,
            normalizedQuery = normalized,
            primaryScript = script,
            detectedLanguages = languages,
            extractedConcepts = concepts,
            hasNegation = hasNeg,
            negationTerms = negTerms,
            isEducationalQuery = isEdu,
            isEmergencyCandidate = isEmergency,
            searchTokens = tokens
        )
    }
}
