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

        val teluguTerms = listOf("virochanalu", "paalu", "aayasam", "jwaram", "vaanthulu", "kadupu", "noppi", "ledu", "pilladu", "challaga", "spruha", "bidda")
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
        if (low.contains("pasli") || low.contains("chest indrawing") || (low.contains("ribs") && low.contains("andar")) || low.contains("seena dab") || low.contains("rib cage lopalki") || low.contains("chest lopala")) {
            val isNeg = hasNegation && (low.contains("pasli nahi") || low.contains("no chest indrawing") || low.contains("no indrawing"))
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
            val isNeg = hasNegation && (low.contains("jhatke nahi") || low.contains("no fits") || low.contains("no convulsion") || low.contains("fits levu"))
            val isMaternal = low.contains("pregnant") || low.contains("garbhvati") || low.contains("delivery") || low.contains("garbiniki")
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

        // 3. Child / Newborn not feeding / drinking (ESC-4.1 / ESC-5.1)
        val hasFeedingOk = low.contains("latching well") || low.contains("drinking well") || low.contains("doodh pee raha") || low.contains("active")
        if (!hasFeedingOk && (
                (low.contains("doodh") || low.contains("milk") || low.contains("breastfeed") || low.contains("paalu") || low.contains("feed") || low.contains("suckle") || low.contains("sthanpan")) &&
                (low.contains("nahi") || low.contains("unable") || low.contains("cannot") || low.contains("refus") || low.contains("ledu") || low.contains("stopped") || low.contains("weak"))
            )
        ) {
            val isNeonatal = low.contains("newborn") || low.contains("navjaat") || low.contains("shishu") || low.contains("bidda") || low.contains("infant")
            concepts.add(
                CanonicalClinicalConcept(
                    id = "INABILITY_TO_FEED",
                    canonicalName = if (isNeonatal) "Neonatal Inability to Suckle / Feed" else "Child unable to drink or breastfeed",
                    matchedPhrase = "unable to feed / suckle",
                    confidence = 0.98,
                    isNegated = false,
                    category = if (isNeonatal) ClinicalCategory.NEWBORN else ClinicalCategory.CHILD_HEALTH,
                    primaryProtocolSection = if (isNeonatal) "ESC-4.1" else "ESC-5.1"
                )
            )
        }

        // 4. Hypothermia / Cold to touch (ESC-4.3)
        if ((low.contains("thanda") || low.contains("cold") || low.contains("challaga") || low.contains("hypothermia")) &&
            (low.contains("chhune") || low.contains("touch") || low.contains("skin") || low.contains("body") || low.contains("bachcha") || low.contains("baby") || low.contains("bidda") || low.contains("infant") || low.contains("shishu") || low.contains("newborn"))
        ) {
            concepts.add(
                CanonicalClinicalConcept(
                    id = "NEONATAL_HYPOTHERMIA",
                    canonicalName = "Newborn Cold to Touch / Hypothermia",
                    matchedPhrase = "thanda lag raha / cold to touch",
                    confidence = 0.98,
                    isNegated = false,
                    category = ClinicalCategory.NEWBORN,
                    primaryProtocolSection = "ESC-4.3"
                )
            )
        }

        // 5. Unconscious / Coma / Severe Hypoglycaemia (ESC-6.3)
        if (low.contains("behosh") || low.contains("unconscious") || low.contains("coma") || low.contains("spruha") || low.contains("hosh nahi") || low.contains("unresponsive")) {
            concepts.add(
                CanonicalClinicalConcept(
                    id = "UNCONSCIOUSNESS",
                    canonicalName = "Unconsciousness / Diabetic Coma",
                    matchedPhrase = "behosh / unconscious",
                    confidence = 0.99,
                    isNegated = false,
                    category = ClinicalCategory.NCD,
                    primaryProtocolSection = "ESC-6.3"
                )
            )
        }

        // 6. Newborn Not Breathing / Asphyxia / Apnoea (ESC-4.2)
        val isNewbornContext = low.contains("newborn") || low.contains("navjaat") || low.contains("shishu") || low.contains("bidda") || low.contains("bachcha") || low.contains("baby") || low.contains("paida") || low.contains("birth") || low.contains("puttina")
        if (isNewbornContext && (
                low.contains("saans nahi") || low.contains("not breathing") || low.contains("shwasa teesuko") ||
                low.contains("ro nahi") || low.contains("not crying") || low.contains("edavadam ledu") ||
                low.contains("asphyxia") || low.contains("apnoe") || low.contains("limp and blue") || low.contains("gasping")
            )
        ) {
            concepts.add(
                CanonicalClinicalConcept(
                    id = "BIRTH_ASPHYXIA",
                    canonicalName = "Neonatal Asphyxia / Respiratory Arrest",
                    matchedPhrase = "not breathing / not crying",
                    confidence = 0.99,
                    isNegated = false,
                    category = ClinicalCategory.NEWBORN,
                    primaryProtocolSection = "ESC-4.2"
                )
            )
        }

        // 7. Suspected Stroke / BE-FAST signs (ESC-6.2)
        val hasStrokeSign = low.contains("munh tedha") || low.contains("mukham vankara") || low.contains("facial droop") ||
                low.contains("haath kaam nahi") || low.contains("cheyi levaledu") || low.contains("arm drift") ||
                low.contains("matalu ravatledu") || low.contains("bolti band") || low.contains("slurred speech") ||
                low.contains("stroke") || low.contains("hemiplegia")
        if (hasStrokeSign) {
            concepts.add(
                CanonicalClinicalConcept(
                    id = "STROKE_BEFAST",
                    canonicalName = "Suspected Acute Stroke (BE-FAST)",
                    matchedPhrase = "stroke / facial droop / arm weakness",
                    confidence = 0.99,
                    isNegated = false,
                    category = ClinicalCategory.NCD,
                    primaryProtocolSection = "ESC-6.2"
                )
            )
        }

        // 8. Child Vomiting Everything (ESC-5.1)
        val hasSingleVomit = low.contains("ek baar") || low.contains("single") || low.contains("after cough")
        if (!hasSingleVomit && (
                low.contains("vomit everything") || low.contains("vomiting everything") || low.contains("vomits everything") ||
                low.contains("sab ulti") || low.contains("har baar ulti") || low.contains("lagatar ulti") ||
                low.contains("kuch nahi thehar") || low.contains("kuch nahi bach") || low.contains("em thinna vomiting")
            )
        ) {
            concepts.add(
                CanonicalClinicalConcept(
                    id = "VOMITING_EVERYTHING",
                    canonicalName = "Child Vomiting Everything",
                    matchedPhrase = "vomiting everything",
                    confidence = 0.98,
                    isNegated = false,
                    category = ClinicalCategory.CHILD_HEALTH,
                    primaryProtocolSection = "ESC-5.1"
                )
            )
        }

        // 9. Postpartum / Obstetric Haemorrhage (ESC-3.1)
        if ((low.contains("pad") && low.contains("minute") && (low.contains("bheeg") || low.contains("soak"))) ||
            (low.contains("khoon") && low.contains("beh") && (low.contains("tez") || low.contains("heavy") || low.contains("delivery") || low.contains("prasav"))) ||
            ((low.contains("delivery") || low.contains("prasav")) && low.contains("bleeding") && (low.contains("excessive") || low.contains("heavy")))
        ) {
            concepts.add(
                CanonicalClinicalConcept(
                    id = "POSTPARTUM_HAEMORRHAGE",
                    canonicalName = "Postpartum Haemorrhage (PPH) / Heavy Bleeding",
                    matchedPhrase = "pad bheeg gaya / heavy bleeding",
                    confidence = 0.99,
                    isNegated = false,
                    category = ClinicalCategory.MATERNAL,
                    primaryProtocolSection = "ESC-3.1"
                )
            )
        }

        // 10. Prolonged Fever >= 7 days (ESC-5.4)
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
                    primaryProtocolSection = "ESC-5.4"
                )
            )
        }

        // 11. Severe Pallor / Anaemia with breathlessness in 3rd trimester (ESC-2.5)
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

        // 12. Hyperemesis Gravidarum (ESC-2.6)
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
                    primaryProtocolSection = "ESC-2.6"
                )
            )
        }

        // 13. Low Birth Weight < 1.8 kg (ESC-4.4)
        if (low.contains("1.5 kg") || low.contains("1.6 kg") || low.contains("1.4 kg") || low.contains("1.2 kg") || low.contains("1.0 kg") || low.contains("< 1.8")) {
            concepts.add(
                CanonicalClinicalConcept(
                    id = "VERY_LOW_BIRTH_WEIGHT",
                    canonicalName = "Very Low Birth Weight (< 1800g)",
                    matchedPhrase = "vajan < 1.8 kg",
                    confidence = 0.99,
                    isNegated = false,
                    category = ClinicalCategory.NEWBORN,
                    primaryProtocolSection = "ESC-4.4"
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
        val voiceNormalized = VoiceInputNormalizer.normalize(original)
        val script = identifyScript(voiceNormalized)
        val normalized = TransliterationNormalizer.normalize(voiceNormalized)
        val languages = identifyLanguages(voiceNormalized, script)

        val (hasNeg, negTerms) = detectNegation(normalized)
        val isEdu = educationalPhrases.any { normalized.lowercase().contains(it) }

        val concepts = extractConcepts(normalized, hasNeg)
        val hasAcuteEmergency = concepts.any { !it.isNegated && it.primaryProtocolSection.startsWith("ESC") }
        val isEmergency = hasAcuteEmergency

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
            isEducationalQuery = isEdu && !hasAcuteEmergency,
            isEmergencyCandidate = isEmergency,
            searchTokens = tokens
        )
    }
}
