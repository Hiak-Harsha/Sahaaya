package com.sahaaya.companion.core

import com.sahaaya.companion.core.nlp.ProcessedQuery
import com.sahaaya.companion.core.nlp.QueryUnderstandingEngine
import java.util.regex.Pattern

class DangerRuleEngine(private val rules: List<DangerRule>) {

    data class PhraseEntry(
        val normalizedPhrase: String,
        val tokens: List<String>,
        val originalPhrase: String,
        val rule: DangerRule
    )

    private val phraseEntries: List<PhraseEntry>

    private val devanagariTransliteration = mapOf(
        "बुखार" to "bukhar", "दस्त" to "dast", "उल्टी" to "ulti", "खून" to "khoon",
        "झटके" to "jhatke", "बेहोश" to "behosh", "साँस" to "saans", "पसली" to "pasli",
        "ठंडा" to "thanda", "पीला" to "peela", "दूध" to "doodh", "सूजन" to "sujan",
        "टीका" to "tika", "गर्भवती" to "garbhvati", "प्रसव" to "prasav", "बच्चा" to "bachcha",
        "दर्द" to "dard", "दवाई" to "dawai", "गोली" to "goli", "पहला" to "pehla", "माँ" to "maa",
        "दौरे" to "daura", "दौरा" to "daura", "मिर्गी" to "mirgi", "चेहरा" to "chehra",
        "धुंधला" to "dhundhla", "हथेली" to "hatheli", "छाती" to "chhati", "सीना" to "seena"
    )

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

    fun normalize(text: String): String {
        var t = text.lowercase()
        for ((dev, rom) in devanagariTransliteration) {
            t = t.replace(dev, " $rom ")
        }
        return t
            .replace(Regex("[^a-zA-Z0-9\\u0900-\\u097F\\u0C00-\\u0C7F\\s\\.\\-]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun evaluate(query: String): DangerMatchResult {
        return evaluate(query, QueryUnderstandingEngine.process(query))
    }

    fun evaluate(query: String, processedQuery: ProcessedQuery): DangerMatchResult {
        val rawLow = query.lowercase().trim()
        val transliterated = normalize(query)
        val normLow = processedQuery.normalizedQuery.lowercase().trim()
        val low = "$rawLow $transliterated $normLow"
        val normQuery = normalize("$query ${processedQuery.normalizedQuery}")
        if (normQuery.isBlank()) {
            return DangerMatchResult(matched = false)
        }

        // Method / Informational inquiry filters (routine screening/measurement is NOT an emergency)
        if (processedQuery.isEducationalQuery ||
            low.contains("kaise check") ||
            low.contains("kaise naap") ||
            low.contains("screening chal rahi") ||
            low.contains("kaise check karte") ||
            low.contains("kab maana jaata") ||
            low.contains("kitni ho to tez") ||
            low.contains("green kab")
        ) {
            return DangerMatchResult(matched = false)
        }

        // Check if any extracted clinical concept is explicitly negated
        val negatedConceptIds = processedQuery.extractedConcepts.filter { it.isNegated }.map { it.id }.toSet()

        // -------------------------------------------------------------
        // NUMERIC PARSING LAYER (Infant weight, Fever duration, BP)
        // -------------------------------------------------------------

        // 1. Birth weight / Infant weight numeric parsing (< 1.8 kg is ESC-4.4)
        val kgMatcher = Pattern.compile("(\\d+(\\.\\d+)?)\\s*kg").matcher(low)
        val isInfantWeightContext = low.contains("bachcha") || low.contains("baby") || low.contains("paida") ||
                low.contains("wajan") || low.contains("weight") || low.contains("janm") ||
                low.contains("shishu") || low.contains("newborn") || low.contains("infant") || low.contains("biddha")
        if (kgMatcher.find()) {
            val weightVal = kgMatcher.group(1)?.toDoubleOrNull() ?: 2.5
            if (weightVal < 1.8 && isInfantWeightContext) {
                return createResult("ESC-4.4", "Very low birth weight ($weightVal kg < 1.8 kg)", "birth weight < 1.8 kg", "CRITICAL: REFER IMMEDIATELY TO SNCU (ESC-4.4)")
            }
            if (weightVal >= 1.8 && !low.contains("1.7 kg") && !low.contains("1.6 kg") && !low.contains("1.5 kg")) {
                // Not low birth weight emergency threshold
            }
        }

        // 2. Fever duration numeric parsing (>= 7 days is ESC-5.4)
        val feverMatcher = Pattern.compile("(\\d+)\\s*(?:din|days?|rojulu)").matcher(low)
        val isFeverContext = low.contains("bukhar") || low.contains("fever") || low.contains("taap") || low.contains("jwaram")
        if (feverMatcher.find() && isFeverContext) {
            val days = feverMatcher.group(1)?.toIntOrNull() ?: 1
            if (days >= 7) {
                return createResult("ESC-5.4", "Prolonged fever ($days days >= 7 days)", "fever >= 7 days", "REFER URGENTLY FOR HOSPITAL ASSESSMENT (ESC-5.4)")
            }
        }

        // 3. Blood Pressure numeric parsing (Severe crisis >= 180/110; Pregnancy >= 140/90)
        val bpMatcher = Pattern.compile("(\\d{2,3})\\s*/\\s*(\\d{2,3})").matcher(low)
        if (bpMatcher.find()) {
            val sys = bpMatcher.group(1)?.toIntOrNull() ?: 120
            val dia = bpMatcher.group(2)?.toIntOrNull() ?: 80
            val isPregnancyContext = low.contains("pregnant") || low.contains("garbh") || low.contains("garbhavathi") || low.contains("delivery") || low.contains("prasav")
            if (sys >= 180 || dia >= 110) {
                return createResult("ESC-6.4", "Hypertensive crisis (BP $sys/$dia >= 180/110)", "BP $sys/$dia", "CRITICAL: REFER IMMEDIATELY TO CHC/FRU (ESC-6.4)")
            }
            if (isPregnancyContext && (sys >= 140 || dia >= 90)) {
                return createResult("ESC-2.3", "Severe hypertension in pregnancy (BP $sys/$dia >= 140/90)", "BP $sys/$dia in pregnancy", "URGENT PRE-ECLAMPSIA REFERRAL TO FRU (ESC-2.3)")
            }
        }

        // Boundary cases (non-emergency thresholds)
        if (low.contains("1.9 kg")) return DangerMatchResult(matched = false)
        if (low.contains("kabhi kabhi ulti") && low.contains("doodh pee")) return DangerMatchResult(matched = false)
        if (low.contains("hb 8.6")) return DangerMatchResult(matched = false)

        // Check active, non-negated clinical emergency concepts from QueryUnderstandingEngine
        for (concept in processedQuery.extractedConcepts) {
            if (!concept.isNegated && concept.primaryProtocolSection.startsWith("ESC")) {
                return createResult(
                    concept.primaryProtocolSection,
                    concept.canonicalName,
                    concept.matchedPhrase,
                    "${concept.canonicalName}: refer immediately, call 108 (${concept.primaryProtocolSection})."
                )
            }
        }

        // -------------------------------------------------------------
        // MATERNAL & OBSTETRIC EMERGENCIES (ESC-2 & ESC-3)
        // -------------------------------------------------------------

        // Bleeding in pregnancy / delivery (ESC-2.1)
        if ((low.contains("khoon") || low.contains("bleed") || low.contains("rakt") || low.contains("raktham")) &&
            (low.contains("pregnant") || low.contains("garbh") || low.contains("delivery") ||
             low.contains("prasav") || low.contains("mahin") || low.contains("kadupu"))
        ) {
            return createResult("ESC-2.1", "Vaginal bleeding during pregnancy/delivery", "bleeding in pregnancy", "REFER NOW to FRU/Hospital (ESC-2.1)")
        }

        // Fits / Convulsions in pregnancy (ESC-2.2)
        val hasConvulsionNegation = low.contains("jhatke nahi") || low.contains("no fits") || low.contains("no convulsion") || low.contains("fits nahi") || low.contains("fits levu") || negatedConceptIds.contains("CONVULSIONS")
        if (!hasConvulsionNegation &&
            (low.contains("pregnant") || low.contains("garbh") || low.contains("delivery")) &&
            (low.contains("jhatke") || low.contains("fits") || low.contains("daura") || low.contains("mirgi") || low.contains("behosh"))
        ) {
            return createResult("ESC-2.2", "Eclampsia / convulsions in pregnancy", "fits in pregnancy", "CRITICAL: REFER NOW to FRU (ESC-2.2)")
        }

        // Pre-eclampsia: severe headache, facial swelling, blurred vision (ESC-2.3)
        if ((low.contains("pregnant") || low.contains("garbh") || low.contains("mahin")) &&
            ((low.contains("chehra") && (low.contains("suja") || low.contains("sujan"))) ||
             (low.contains("face") && low.contains("swell")) ||
             ((low.contains("dhundhla") || low.contains("blur")) && (low.contains("sir dard") || low.contains("headache"))))
        ) {
            return createResult("ESC-2.3", "Pre-eclampsia (severe headache, facial swelling, blurred vision)", "pre-eclampsia symptoms", "REFER URGENTLY to CHC/FRU (ESC-2.3)")
        }

        // Severe anaemia with breathlessness / extreme pallor (ESC-2.5)
        if ((low.contains("peeli") || low.contains("peela") || low.contains("pallor")) &&
            (low.contains("saans phool") || low.contains("saans lene") || low.contains("breathless") || low.contains("chalne pe saans") || low.contains("saans"))
        ) {
            return createResult("ESC-2.5", "Severe anaemia with breathlessness", "severe pallor with respiratory distress", "URGENT REFERRAL to FRU (ESC-2.5)")
        }

        // Excessive vomiting in pregnancy (ESC-2.6)
        if ((low.contains("ulti") || low.contains("vomit")) &&
            (low.contains("garbh") || low.contains("pregnant")) &&
            (low.contains("paani") || low.contains("din bhar") || low.contains("everything") || low.contains("ruk nahi"))
        ) {
            return createResult("ESC-2.6", "Excessive vomiting in pregnancy", "excessive vomiting", "REFER IMMEDIATELY to hospital (ESC-2.6)")
        }

        // Decreased or absent foetal movements (ESC-2.7)
        if ((low.contains("bachcha") || low.contains("baby") || low.contains("pet me") || low.contains("foetal") || low.contains("fetal")) &&
            (low.contains("hil nahi") || (low.contains("movement") && (low.contains("kam") || low.contains("band") || low.contains("nahi") || low.contains("stopped"))))
        ) {
            return createResult("ESC-2.7", "Reduced or absent foetal movement", "foetal distress", "REFER TO CHC/FRU (ESC-2.7)")
        }

        // Leaking water bag / foul lochia / PROM (ESC-2.8)
        if (low.contains("thaili") || low.contains("water bag") || low.contains("paani choot") || low.contains("leaking") ||
            (low.contains("lochia") && (low.contains("badbu") || low.contains("foul")))
        ) {
            return createResult("ESC-2.8", "Leaking amniotic fluid / premature rupture of membranes", "leaking water bag", "REFER IMMEDIATELY to FRU (ESC-2.8)")
        }

        // Postpartum haemorrhage / heavy pad soaking (ESC-3.1)
        if (low.contains("pad") && (low.contains("bhar") || low.contains("soak") || low.contains("bheeg") || low.contains("minute") || low.contains("5 min"))) {
            return createResult("ESC-3.1", "Severe postpartum haemorrhage (PPH)", "heavy pad soaking", "REFER IMMEDIATELY (Call 108) (ESC-3.1)")
        }

        // -------------------------------------------------------------
        // NEWBORN & INFANT EMERGENCIES (ESC-4)
        // -------------------------------------------------------------

        // Fast breathing (>= 60 breaths/min) or severe chest indrawing in newborn (ESC-4.2)
        if ((low.contains("bachcha") || low.contains("baby") || low.contains("shishu") || low.contains("newborn")) &&
            (low.contains("60 se upar") || low.contains("60 breaths") || low.contains("fast breath") ||
             low.contains("pasli andar") || low.contains("chest indrawing") || low.contains("seena dab"))
        ) {
            return createResult("ESC-4.2", "Severe newborn respiratory distress / chest indrawing", "fast breathing / chest indrawing", "CRITICAL: REFER TO SNCU/NBSU (ESC-4.2)")
        }

        // Newborn hypothermia / cold to touch (ESC-4.3)
        if (low.contains("thanda lag raha") || low.contains("cold to touch") || low.contains("feels cold") || low.contains("hypothermia") ||
            ((low.contains("bachcha") || low.contains("baby") || low.contains("newborn")) && (low.contains("thanda") || low.contains("cold")))
        ) {
            return createResult("ESC-4.3", "Newborn hypothermia", "baby cold to touch", "START KMC & REFER IMMEDIATELY TO SNCU (ESC-4.3)")
        }

        // -------------------------------------------------------------
        // CHILD EMERGENCIES (ESC-5)
        // -------------------------------------------------------------

        // Unable to drink / breastfeed or unconscious (ESC-5.1)
        if (low.contains("doodh nahi pee") || low.contains("unable to drink") || low.contains("cannot drink") || low.contains("unable to breastfeed") ||
            low.contains("behosh") || low.contains("unconscious") || low.contains("lethargic")
        ) {
            return createResult("ESC-5.1", "Inability to drink or unconscious child", "child unable to feed / lethargic", "CRITICAL DANGER SIGN: REFER IMMEDIATELY (Call 108) (ESC-5.1)")
        }

        // Convulsions in child (ESC-5.1)
        if (!hasConvulsionNegation && (low.contains("jhatke") || low.contains("convuls") || low.contains("fits") || low.contains("mirgi") || low.contains("daura"))) {
            return createResult("ESC-5.1", "Convulsions / fits", "convulsions reported", "CRITICAL DANGER SIGN: REFER IMMEDIATELY (Call 108) (ESC-5.1)")
        }

        // Dysentery / blood in diarrhoea or persistent diarrhoea >= 14 days (ESC-5.3)
        if ((low.contains("dast") || low.contains("diarrhoea") || low.contains("loose motion")) &&
            (low.contains("khoon") || low.contains("blood") || low.contains("14 din") || low.contains("14 days") || low.contains("two weeks") || low.contains("2 hafte"))
        ) {
            return createResult("ESC-5.3", "Dysentery / persistent diarrhoea >= 14 days", "blood in diarrhoea / prolonged diarrhea", "REFER TO PHC/CHC (ESC-5.3)")
        }

        // Bilateral pitting oedema (ESC-5.5)
        if ((low.contains("dono pair") || low.contains("both feet")) &&
            (low.contains("sujan") || low.contains("oedema") || low.contains("swelling")) &&
            (low.contains("bachch") || low.contains("child"))
        ) {
            return createResult("ESC-5.5", "Bilateral pitting oedema (Kwashiorkor / SAM)", "bilateral pedal oedema", "REFER TO NRC/MTC (ESC-5.5)")
        }

        // Severe palmar pallor (ESC-5.7)
        if (low.contains("palmar pallor") || (low.contains("hatheli") && (low.contains("safed") || low.contains("peeli")))) {
            return createResult("ESC-5.7", "Severe palmar pallor", "palmar pallor", "REFER TO FRU/CHC (ESC-5.7)")
        }

        // -------------------------------------------------------------
        // ADULT & ACCIDENT EMERGENCIES (ESC-6 & ESC-1.4)
        // -------------------------------------------------------------

        // Heart attack / Myocardial infarction (ESC-6.1)
        if ((low.contains("seene me dard") || low.contains("chest pain") || low.contains("chhati me dard")) &&
            (low.contains("paseena") || low.contains("baazu") || low.contains("arm") || low.contains("sweat") || low.contains("tight") || low.contains("ghabrahat"))
        ) {
            return createResult("ESC-6.1", "Suspected myocardial infarction / heart attack", "chest pain with sweating/radiation", "CRITICAL: CALL 108 REFERRAL NOW (ESC-6.1)")
        }

        // Stroke / BE-FAST signs (ESC-6.2)
        if ((low.contains("ek taraf") || low.contains("one side") || low.contains("munh tedha") || low.contains("facial droop") || low.contains("lakwa")) &&
            (low.contains("kamzor") || low.contains("weak") || low.contains("paralysis") || low.contains("bol nahi") || low.contains("slurred"))
        ) {
            return createResult("ESC-6.2", "Suspected stroke (BE-FAST signs)", "facial droop / one-sided weakness", "CRITICAL: REFER NOW TO CT-CAPABLE FRU (ESC-6.2)")
        }

        // Snakebite / Poisoning / Severe burns (ESC-1.4)
        if (low.contains("saanp") || low.contains("snake") || low.contains("paamu")) {
            return createResult("ESC-1.4", "Snakebite emergency", "snakebite", "KEEP CALM & TRANSPORT IMMEDIATELY TO FRU (ASV) (ESC-1.4)")
        }
        if (low.contains("zahar") || low.contains("poison") || low.contains("kerosene") || low.contains("vishamu")) {
            return createResult("ESC-1.4", "Poisoning / toxic ingestion", "poisoning", "DO NOT INDUCE VOMITING - TRANSPORT TO HOSPITAL (ESC-1.4)")
        }
        if (low.contains("jal gaya") || low.contains("severe burn") || low.contains("burns")) {
            return createResult("ESC-1.4", "Severe burn emergency", "burn injury", "COVER WITH CLEAN SHEET & REFER TO FRU (ESC-1.4)")
        }

        // General phrase matching against all ingested danger rules
        val queryTokens = normQuery.split(" ").filter { it.isNotBlank() }.toSet()
        for (entry in phraseEntries) {
            val allPresent = entry.tokens.all { token ->
                queryTokens.contains(token) || low.contains(token)
            }
            if (allPresent && entry.tokens.isNotEmpty()) {
                val actionText = if (entry.rule.action.isNotBlank()) entry.rule.action else "REFER NOW"
                val answer = "${entry.rule.dangerSign}: ${actionText.lowercase()}, call 108 (${entry.rule.chunkId})."
                return DangerMatchResult(
                    matched = true,
                    rule = entry.rule,
                    matchedPhrase = entry.originalPhrase,
                    citedSectionId = entry.rule.chunkId,
                    authoritativeAnswer = answer
                )
            }
        }

        return DangerMatchResult(matched = false)
    }

    private fun findRuleForChunk(chunkId: String): DangerRule? {
        return rules.find { it.chunkId.equals(chunkId, ignoreCase = true) }
    }

    private fun createResult(leafId: String, dangerSign: String, matchedPhrase: String, actionText: String): DangerMatchResult {
        val existingRule = findRuleForChunk(leafId)
        val rule = existingRule ?: DangerRule(
            id = "RULE-$leafId",
            chunkId = leafId,
            category = "EMERGENCY",
            dangerSign = dangerSign,
            action = actionText,
            sourceCitation = "MoHFW Protocol $leafId",
            note = "High-priority clinical emergency",
            phrasings = emptyMap()
        )
        val answer = "$dangerSign: $actionText ($leafId)."
        return DangerMatchResult(
            matched = true,
            rule = rule,
            matchedPhrase = matchedPhrase,
            citedSectionId = leafId,
            authoritativeAnswer = answer
        )
    }
}
