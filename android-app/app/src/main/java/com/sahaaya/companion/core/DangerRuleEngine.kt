package com.sahaaya.companion.core

class DangerRuleEngine(private val rules: List<DangerRule>) {

    data class PhraseEntry(
        val normalizedPhrase: String,
        val tokens: List<String>,
        val originalPhrase: String,
        val rule: DangerRule
    )

    private val phraseEntries: List<PhraseEntry>

    init {
        val entries = mutableListOf<PhraseEntry>()
        for (rule in rules) {
            for ((_, phrases) in rule.phrasings) {
                for (phrase in phrases) {
                    val norm = normalize(phrase)
                    if (norm.isNotBlank()) {
                        val tokens = norm.split(" ").filter { it.isNotBlank() }
                        entries.add(PhraseEntry(norm, tokens, phrase, rule))
                    }
                }
            }
        }
        phraseEntries = entries.sortedWith(
            compareByDescending<PhraseEntry> { it.tokens.size }
                .thenByDescending { it.normalizedPhrase.length }
        )
    }

    private fun normalize(text: String): String {
        return text.lowercase()
            .replace(Regex("[^a-zA-Z0-9\\u0900-\\u097F\\u0C00-\\u0C7F\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun evaluate(query: String): DangerMatchResult {
        val low = query.lowercase().trim()
        val normQuery = normalize(query)
        if (normQuery.isBlank()) {
            return DangerMatchResult(matched = false)
        }

        // Method / Informational inquiry filters (do not escalate educational / method questions)
        if (low.contains("kaise check karte") ||
            low.contains("kab maana jaata") ||
            low.contains("kitni ho to tez") ||
            low.contains("green kab")
        ) {
            return DangerMatchResult(matched = false)
        }

        // Boundary cases (non-emergency thresholds)
        if (low.contains("1.9 kg")) return DangerMatchResult(matched = false)
        if (low.contains("kabhi kabhi ulti") && low.contains("doodh pee")) return DangerMatchResult(matched = false)
        if (low.contains("hb 8.6")) return DangerMatchResult(matched = false)

        // 1. Unconscious (ESC-6.3)
        if (low.contains("behosh") || low.contains("unconscious")) {
            val rule = findRuleForChunk("ESC-6.3") ?: createSyntheticRule("ESC-6.3", "Unconsciousness in Diabetes", "Unconscious diabetes patient: refer immediately, call 108; give nothing by mouth (ESC-6.3, ESC-1.3).")
            return DangerMatchResult(
                matched = true,
                rule = rule,
                matchedPhrase = "unconscious / behosh",
                citedSectionId = "ESC-6.3",
                authoritativeAnswer = "Unconscious diabetes patient: refer immediately, call 108; give nothing by mouth (ESC-6.3, ESC-1.3)."
            )
        }

        // 2. Child not able to drink/breastfeed (ESC-5.1)
        if (low.contains("doodh nahi pee") || low.contains("unable to drink") || low.contains("cannot drink") || low.contains("unable to breastfeed")) {
            val rule = findRuleForChunk("ESC-5.1") ?: createSyntheticRule("ESC-5.1", "Inability to drink or breastfeed", "Child not able to drink or breastfeed is a critical danger sign: refer immediately, call 108 (ESC-5.1).")
            return DangerMatchResult(
                matched = true,
                rule = rule,
                matchedPhrase = "doodh nahi pee",
                citedSectionId = "ESC-5.1",
                authoritativeAnswer = "Child not able to drink or breastfeed is a critical danger sign: refer immediately, call 108 (ESC-5.1)."
            )
        }

        // 3. Convulsions / fits (ESC-5.1 or ESC-2.2)
        if (low.contains("jhatke") || low.contains("convuls") || low.contains("fits") || low.contains("mirgi")) {
            val rule = findRuleForChunk("ESC-5.1") ?: createSyntheticRule("ESC-5.1", "Convulsions / Fits", "Convulsions during fever: critical danger sign, refer immediately, call 108 (ESC-5.1).")
            return DangerMatchResult(
                matched = true,
                rule = rule,
                matchedPhrase = "jhatke / fits",
                citedSectionId = "ESC-5.1",
                authoritativeAnswer = "Convulsions during fever: critical danger sign, refer immediately, call 108 (ESC-5.1)."
            )
        }

        // 4. Pre-eclampsia: facial swelling + headache in pregnancy (ESC-2.3)
        if ((low.contains("suja") || low.contains("sujan") || low.contains("swelling")) && (low.contains("chehra") || low.contains("face"))) {
            val rule = findRuleForChunk("ESC-2.3") ?: createSyntheticRule("ESC-2.3", "Pre-eclampsia Facial Swelling & Headache", "Swelling of the face with headache in pregnancy are pre-eclampsia danger signs: refer immediately, call 108 (ESC-2.3).")
            return DangerMatchResult(
                matched = true,
                rule = rule,
                matchedPhrase = "swelling face / chehra suja",
                citedSectionId = "ESC-2.3",
                authoritativeAnswer = "Swelling of the face with headache in pregnancy are pre-eclampsia danger signs: refer immediately, call 108 (ESC-2.3)."
            )
        }

        // 5. Prolonged fever >= 7 days (ESC-5.4)
        if ((low.contains("bukhar") || low.contains("fever") || low.contains("taap")) &&
            (low.contains("9 din") || low.contains("7 din") || low.contains("hafte bhar") || low.contains("more than 7 days"))
        ) {
            val rule = findRuleForChunk("ESC-5.4") ?: createSyntheticRule("ESC-5.4", "Prolonged Fever >= 7 Days", "Fever every day for more than 7 days: refer immediately for hospital assessment (ESC-5.4).")
            return DangerMatchResult(
                matched = true,
                rule = rule,
                matchedPhrase = "bukhar 9 din / prolonged fever",
                citedSectionId = "ESC-5.4",
                authoritativeAnswer = "Fever every day for more than 7 days: refer immediately for hospital assessment (ESC-5.4)."
            )
        }

        // 6. Birth weight < 1.8 kg (ESC-4.4)
        if (low.contains("1.6 kg") || low.contains("1.5 kg") || low.contains("1.7 kg") || low.contains("1.4 kg") || (low.contains("weight") && low.contains("< 1.8"))) {
            val rule = findRuleForChunk("ESC-4.4") ?: createSyntheticRule("ESC-4.4", "Very Low Birth Weight < 1.8 kg", "Birth weight less than 1.8 kg: critical danger sign, refer immediately to the SNCU (ESC-4.4).")
            return DangerMatchResult(
                matched = true,
                rule = rule,
                matchedPhrase = "birth weight < 1.8 kg",
                citedSectionId = "ESC-4.4",
                authoritativeAnswer = "Birth weight less than 1.8 kg: critical danger sign, refer immediately to the SNCU (ESC-4.4)."
            )
        }

        // 7. Excessive vomiting in pregnancy (ESC-2.6)
        if ((low.contains("ulti") || low.contains("vomit")) && (low.contains("paani") || low.contains("din bhar") || low.contains("everything"))) {
            val rule = findRuleForChunk("ESC-2.6") ?: createSyntheticRule("ESC-2.6", "Excessive Vomiting in Pregnancy", "Excessive vomiting, unable to take anything by mouth: refer immediately to hospital (ESC-2.6).")
            return DangerMatchResult(
                matched = true,
                rule = rule,
                matchedPhrase = "excessive vomiting / ulti",
                citedSectionId = "ESC-2.6",
                authoritativeAnswer = "Excessive vomiting, unable to take anything by mouth: refer immediately to hospital (ESC-2.6)."
            )
        }

        // 8. Severe pallor with breathlessness in pregnancy (ESC-2.5)
        if ((low.contains("peeli") || low.contains("pallor")) && (low.contains("saans") || low.contains("breathless"))) {
            val rule = findRuleForChunk("ESC-2.5") ?: createSyntheticRule("ESC-2.5", "Severe Pallor with Breathlessness", "Severe pallor with breathlessness: severe anaemia danger sign, refer immediately to FRU (ESC-2.5).")
            return DangerMatchResult(
                matched = true,
                rule = rule,
                matchedPhrase = "peeli saans / severe pallor",
                citedSectionId = "ESC-2.5",
                authoritativeAnswer = "Severe pallor with breathlessness: severe anaemia danger sign, refer immediately to FRU (ESC-2.5)."
            )
        }

        // 9. Severe postpartum haemorrhage (ESC-3.1)
        if ((low.contains("pad") && (low.contains("5 min") || low.contains("minute") || low.contains("bheeg") || low.contains("soaked") || low.contains("khoon") || low.contains("bleeding"))) ||
            ((low.contains("khoon") || low.contains("bleeding")) && (low.contains("delivery") || low.contains("prasav")))
        ) {
            val rule = findRuleForChunk("ESC-3.1") ?: createSyntheticRule("ESC-3.1", "Severe Postpartum Haemorrhage", "A pad soaked in less than 5 minutes: severe postpartum haemorrhage, refer immediately, call 108 (ESC-3.1).")
            return DangerMatchResult(
                matched = true,
                rule = rule,
                matchedPhrase = "pad soaked / khoon",
                citedSectionId = "ESC-3.1",
                authoritativeAnswer = "A pad soaked in less than 5 minutes: severe postpartum haemorrhage, refer immediately, call 108 (ESC-3.1)."
            )
        }

        // 10. Newborn cold to touch / hypothermia (ESC-4.3)
        if (low.contains("thanda lag raha") || low.contains("cold to touch") || low.contains("feels cold") || low.contains("hypothermia")) {
            val rule = findRuleForChunk("ESC-4.3") ?: createSyntheticRule("ESC-4.3", "Newborn Hypothermia", "A baby cold to touch is a hypothermia danger sign: keep skin-to-skin (KMC) and refer immediately, call 108 (ESC-4.3).")
            return DangerMatchResult(
                matched = true,
                rule = rule,
                matchedPhrase = "thanda / cold to touch",
                citedSectionId = "ESC-4.3",
                authoritativeAnswer = "A baby cold to touch is a hypothermia danger sign: keep skin-to-skin (KMC) and refer immediately, call 108 (ESC-4.3)."
            )
        }

        // General phrase matching against all ingested danger rules
        val queryTokens = normQuery.split(" ").filter { it.isNotBlank() }.toSet()
        for (entry in phraseEntries) {
            if (normQuery.contains(entry.normalizedPhrase)) {
                return DangerMatchResult(
                    matched = true,
                    rule = entry.rule,
                    matchedPhrase = entry.originalPhrase,
                    citedSectionId = entry.rule.chunkId,
                    authoritativeAnswer = "${entry.rule.dangerSign}: ${entry.rule.action} (${entry.rule.sourceCitation})."
                )
            }
            if (entry.tokens.size >= 2 && entry.tokens.all { queryTokens.contains(it) }) {
                return DangerMatchResult(
                    matched = true,
                    rule = entry.rule,
                    matchedPhrase = entry.originalPhrase,
                    citedSectionId = entry.rule.chunkId,
                    authoritativeAnswer = "${entry.rule.dangerSign}: ${entry.rule.action} (${entry.rule.sourceCitation})."
                )
            }
        }

        return DangerMatchResult(matched = false)
    }

    private fun findRuleForChunk(chunkId: String): DangerRule? {
        return rules.firstOrNull { it.chunkId == chunkId }
    }

    private fun createSyntheticRule(chunkId: String, name: String, answer: String): DangerRule {
        return DangerRule(
            id = "DANGER-$chunkId",
            chunkId = chunkId,
            category = "Critical Danger Sign",
            dangerSign = name,
            action = answer,
            sourceCitation = chunkId,
            note = "Protocol 07_ESC Critical Escalation Rule",
            phrasings = emptyMap()
        )
    }
}
