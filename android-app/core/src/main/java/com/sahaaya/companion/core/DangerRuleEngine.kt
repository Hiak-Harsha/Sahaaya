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

    private val regionalTransliteration = mapOf(
        "बुखार" to "bukhar", "दस्त" to "dast", "उल्टी" to "ulti", "खून" to "khoon",
        "झटके" to "jhatke", "बेहोश" to "behosh", "साँस" to "saans", "पसली" to "pasli",
        "ठंडा" to "thanda", "पीला" to "peela", "दूध" to "doodh", "सूजन" to "sujan",
        "टीका" to "tika", "गर्भवती" to "garbhvati", "प्रसव" to "prasav", "बच्चा" to "bachcha",
        "दर्द" to "dard", "दवाई" to "dawai", "गोली" to "goli", "पहला" to "pehla", "माँ" to "maa",
        "दौरे" to "daura", "दौरा" to "daura", "मिर्गी" to "mirgi", "चेहरा" to "chehra",
        "धुंधला" to "dhundhla", "हथेली" to "hatheli", "छाती" to "chhati", "सीना" to "seena",
        // Telugu common emergency terms
        "జ్వరం" to "bukhar fever", "ఫిట్స్" to "fits jhatke", "రక్తం" to "khoon blood",
        "రక్తస్రావం" to "bleeding khoon", "వాంతులు" to "ulti vomiting", "చల్లగా" to "thanda cold",
        "శ్వాస" to "saans breathing", "స్పృహ" to "hosh", "తప్పడం" to "behosh unconscious",
        "బిడ్డ" to "bachcha baby child", "పిల్లాడు" to "bachcha child", "ముఖం" to "chehra face",
        "వాపు" to "sujan swelling", "నొప్పి" to "dard pain"
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
        for ((dev, rom) in regionalTransliteration) {
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

        // Check if an explicit ESC section is queried in an emergency/alert context
        val escCodeMatcher = Pattern.compile("\\b(esc-\\d+\\.\\d+)\\b").matcher(low)
        val hasAlertContext = low.contains("emergency") || low.contains("critical") || low.contains("alert") ||
                low.contains("gambhir") || low.contains("danger") || low.contains("refer")
        if (escCodeMatcher.find() && hasAlertContext) {
            val matchedCode = escCodeMatcher.group(1).uppercase()
            val existingRule = findRuleForChunk(matchedCode)
            val title = existingRule?.dangerSign ?: "Emergency protocol sign"
            return createResult(matchedCode, title, matchedCode, "CRITICAL: REFER IMMEDIATELY TO HOSPITAL (Call 108) ($matchedCode)")
        }

        // Severe acute emergency flags that override educational inquiry filters
        val hasSevereAcuteSign = (low.contains("jhatke") || low.contains("convuls") || low.contains("fits") ||
                low.contains("seizure") || low.contains("twitching") || low.contains("fitting") ||
                low.contains("behosh") || low.contains("unconscious") || low.contains("spruha") ||
                low.contains("not breathing") || low.contains("saans nahi") || low.contains("shwasa teesuko") ||
                low.contains("not crying") || low.contains("edavadam ledu") || low.contains("edavatam ledu") ||
                low.contains("soak") || low.contains("bheeg") || low.contains("heavy bleed") || low.contains("geela") ||
                low.contains("munh tedha") || low.contains("mukham vankara") || low.contains("stroke") ||
                low.contains("vomit everything") || low.contains("sab ulti") || low.contains("kuch nahi thehar") ||
                low.contains("every sip") || low.contains("pre-eclampsia") || low.contains("preeclampsia") ||
                low.contains("sar fatne"))

        // Educational inquiry filter: only suppress if no severe acute emergency sign is present
        if (!hasSevereAcuteSign && (
                processedQuery.isEducationalQuery ||
                low.contains("kaise check") ||
                low.contains("kaise naap") ||
                low.contains("screening chal rahi") ||
                low.contains("kaise check karte") ||
                low.contains("kab maana jaata") ||
                low.contains("kitni ho to tez") ||
                low.contains("green kab")
            )
        ) {
            return DangerMatchResult(matched = false)
        }

        // Check if any extracted clinical concept is explicitly negated
        val negatedConceptIds = processedQuery.extractedConcepts.filter { it.isNegated }.map { it.id }.toSet()

        // -------------------------------------------------------------
        // NUMERIC PARSING LAYER (Infant weight, Fever duration, BP, Hb)
        // -------------------------------------------------------------

        // 1. Birth weight / Infant weight numeric parsing (< 1.8 kg is ESC-4.4)
        val kgMatcher = Pattern.compile("(\\d+(\\.\\d+)?)\\s*kg").matcher(low)
        val isInfantWeightContext = low.contains("bach") || low.contains("bacch") || low.contains("baby") || low.contains("paida") ||
                low.contains("wajan") || low.contains("vajan") || low.contains("weight") || low.contains("janm") ||
                low.contains("navjaat") || low.contains("shishu") || low.contains("newborn") || low.contains("infant") || low.contains("bidda")
        if (kgMatcher.find()) {
            val weightVal = kgMatcher.group(1)?.toDoubleOrNull() ?: 2.5
            if (weightVal < 1.8 && isInfantWeightContext) {
                return createResult("ESC-4.4", "Very low birth weight ($weightVal kg < 1.8 kg)", "birth weight < 1.8 kg", "CRITICAL: REFER IMMEDIATELY TO SNCU (ESC-4.4)")
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
            val isPregnancyContext = low.contains("pregnan") || low.contains("garbh") || low.contains("garbhavathi") ||
                    low.contains("delivery") || low.contains("prasav") || low.contains("garbhavastha") ||
                    low.contains("pre-eclampsia") || low.contains("preeclampsia")
            if (sys >= 180 || dia >= 110) {
                return createResult("ESC-6.4", "Hypertensive crisis (BP $sys/$dia >= 180/110)", "BP $sys/$dia", "CRITICAL: REFER IMMEDIATELY TO CHC/FRU (ESC-6.4)")
            }
            if (isPregnancyContext && (sys >= 140 || dia >= 90)) {
                return createResult("ESC-2.3", "Severe hypertension in pregnancy (BP $sys/$dia >= 140/90)", "BP $sys/$dia in pregnancy", "URGENT PRE-ECLAMPSIA REFERRAL TO FRU (ESC-2.3)")
            }
        }

        // 4. Haemoglobin numeric parsing: severe anaemia is Hb < 7.0 g/dL (ESC-2.5)
        val hbMatcher = Pattern.compile("(?:hb|haemoglobin|hemoglobin)\\s*(?:hai|is|tha|was|level)?\\s*(\\d+(?:\\.\\d+)?)").matcher(low)
        if (hbMatcher.find()) {
            val hbVal = hbMatcher.group(1)?.toDoubleOrNull() ?: 11.0
            if (hbVal < 7.0) {
                return createResult("ESC-2.5", "Severe anaemia (Hb $hbVal < 7.0 g/dL)", "Hb < 7.0", "CRITICAL: REFER IMMEDIATELY FOR SEVERE ANAEMIA (ESC-2.5)")
            }
        }

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
        val hasBleedingNegation = low.contains("no bleeding") || low.contains("bleeding stopped") || low.contains("khoon band") || low.contains("bleed nahi")
        if (!hasBleedingNegation &&
            (low.contains("khoon") || low.contains("bleed") || low.contains("rakt") || low.contains("raktham")) &&
            (low.contains("pregnan") || low.contains("garbh") || low.contains("delivery") ||
             low.contains("prasav") || low.contains("mahin") || low.contains("kadupu") || low.contains("garbhavastha"))
        ) {
            return createResult("ESC-2.1", "Vaginal bleeding during pregnancy/delivery", "bleeding in pregnancy", "REFER NOW to FRU/Hospital (ESC-2.1)")
        }

        // Fits / Convulsions in pregnancy (ESC-2.2)
        val hasConvulsionNegation = low.contains("jhatke nahi") || low.contains("no fits") || low.contains("no convulsion") || low.contains("fits nahi") || low.contains("fits levu") || negatedConceptIds.contains("CONVULSIONS")
        if (!hasConvulsionNegation &&
            (low.contains("pregnan") || low.contains("garbh") || low.contains("delivery") || low.contains("garbiniki") || low.contains("garbhavastha") || low.contains("labor") || low.contains("labour")) &&
            (low.contains("jhatke") || low.contains("fits") || low.contains("daura") || low.contains("daure") || low.contains("mirgi") || low.contains("behosh") || low.contains("jerking") || low.contains("seizure") || low.contains("twitching") || low.contains("fitting"))
        ) {
            return createResult("ESC-2.2", "Eclampsia / convulsions in pregnancy", "fits in pregnancy", "CRITICAL: REFER NOW to FRU (ESC-2.2)")
        }

        // Pre-eclampsia: severe headache, facial swelling, blurred vision (ESC-2.3)
        val hasPregContext = low.contains("pregnan") || low.contains("garbh") || low.contains("mahin") || low.contains("garbiniki") || low.contains("garbhavastha") ||
                low.contains("pre-eclampsia") || low.contains("preeclampsia") || low.contains("eclampsia") || (low.contains("chehr") && low.contains("sujan"))
        val hasPreEclampSigns = ((low.contains("chehra") || low.contains("face") || low.contains("munh") || low.contains("chehr")) && (low.contains("suja") || low.contains("sujan") || low.contains("swell"))) ||
            (low.contains("dhundhla") || low.contains("blur") || low.contains("kallu mabbulu") || low.contains("kanapadadam ledu") || low.contains("flashing lights") || low.contains("visual disturbance")) ||
            (low.contains("epigastric") || low.contains("upper stomach") || low.contains("sar fatne") || low.contains("pre-eclampsia") || low.contains("preeclampsia"))
        if (hasPregContext && hasPreEclampSigns && (low.contains("dard") || low.contains("headache") || low.contains("thalanoppi") || low.contains("bp") || low.contains("160") || low.contains("sar fatne") || low.contains("pre-eclampsia") || low.contains("preeclampsia") || low.contains("visual disturbance"))) {
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
            (low.contains("garbh") || low.contains("pregnan") || low.contains("garbhavastha")) &&
            (low.contains("paani") || low.contains("din bhar") || low.contains("everything") || low.contains("ruk nahi") || low.contains("tik nahi"))
        ) {
            return createResult("ESC-2.6", "Excessive vomiting in pregnancy", "excessive vomiting", "REFER IMMEDIATELY to hospital (ESC-2.6)")
        }

        // Decreased or absent foetal movements (ESC-2.7)
        if ((low.contains("bachch") || low.contains("baby") || low.contains("pet me") || low.contains("foetal") || low.contains("fetal")) &&
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
        if ((low.contains("pad") && (low.contains("bhar") || low.contains("soak") || low.contains("bheeg") || low.contains("minute") || low.contains("5 min"))) ||
            ((low.contains("delivery") || low.contains("prasav") || low.contains("prasavam") || low.contains("pnc") || low.contains("maa ka")) &&
             (low.contains("bleeding") || low.contains("khoon") || low.contains("raktasravam")) &&
             (low.contains("heavy") || low.contains("bohot zyada") || low.contains("excessive") || low.contains("ekkuva") || low.contains("ruk nahi") || low.contains("geela") || low.contains("kapda")))
        ) {
            return createResult("ESC-3.1", "Severe postpartum haemorrhage (PPH)", "heavy pad soaking", "REFER IMMEDIATELY (Call 108) (ESC-3.1)")
        }

        // -------------------------------------------------------------
        // NEWBORN & INFANT RESUSCITATION & EMERGENCIES (ESC-4)
        // -------------------------------------------------------------
        val isNewborn = isInfantWeightContext || low.contains("newborn") || low.contains("navjaat") ||
                low.contains("shishu") || low.contains("bidda") || low.contains("puttina") || low.contains("paida") ||
                low.contains("bach") || low.contains("bacch")

        // 1. Newborn not breathing / not crying / birth asphyxia / apnoea (ESC-4.2)
        if (isNewborn && (
                low.contains("saans nahi") || low.contains("not breathing") || low.contains("shwasa teesuko") ||
                low.contains("shwasa aagipoyindi") || low.contains("no breath") || low.contains("apnoe") || low.contains("gasping") ||
                low.contains("ro nahi") || low.contains("not crying") || low.contains("edavadam ledu") || low.contains("edavatam ledu") ||
                low.contains("asphyxia") ||
                ((low.contains("limp and blue") || low.contains("pale blue") || low.contains("neela dikh")) && (low.contains("saans") || low.contains("breath") || low.contains("shwasa") || low.contains("cry") || low.contains("ro nahi"))) ||
                (low.contains("limp") && low.contains("blue")) ||
                (low.contains("sust") && (low.contains("ro nahi") || low.contains("saans nahi")))
            )
        ) {
            return createResult("ESC-4.2", "Neonatal asphyxia / not breathing or crying at birth", "not breathing / not crying at birth", "CRITICAL: BEGIN RESUSCITATION & REFER IMMEDIATELY (Call 108) (ESC-4.2)")
        }

        // 2. Newborn hypothermia / cold to touch (ESC-4.3)
        if (isNewborn && (
                low.contains("thanda lag raha") || low.contains("cold to touch") || low.contains("feels cold") ||
                low.contains("hypothermia") || low.contains("challaga") || low.contains("thanda pad gaya") ||
                low.contains("barf jaisa") || low.contains("cold stress") ||
                (low.contains("temperature") && (low.contains("thakkuva") || low.contains("kam") || low.contains("below 35"))) ||
                (low.contains("thanda") && (low.contains("chhoone") || low.contains("touch") || low.contains("shareer") || low.contains("kapde")))
            )
        ) {
            return createResult("ESC-4.3", "Newborn hypothermia (cold to touch)", "baby cold to touch", "START KMC & REFER IMMEDIATELY TO SNCU (ESC-4.3)")
        }

        // 3. Neonatal inability to suckle / feed (ESC-4.1)
        val hasFeedingOk = low.contains("latching well") || low.contains("drinking well") || low.contains("doodh pee raha") || low.contains("active")
        if (isNewborn && !hasFeedingOk && (
                low.contains("unable to feed") || low.contains("unable to suckle") || low.contains("stopped suckling") ||
                low.contains("cannot latch") || low.contains("cannot feed") || low.contains("not taking feeds") ||
                low.contains("too weak to suckle") || low.contains("inability to feed") || low.contains("doodh nahi pee") ||
                low.contains("sthanpan nahi") || low.contains("chhati se nahi lag") || low.contains("paalu taagadam ledu") ||
                low.contains("paalu muttukovatledu") || low.contains("doodh nahi piya")
            )
        ) {
            return createResult("ESC-4.1", "Neonatal inability to feed / suckle", "unable to suckle / feed", "CRITICAL NEWBORN DANGER SIGN: REFER IMMEDIATELY (ESC-4.1)")
        }

        // Fast breathing (>= 60 breaths/min) in newborn (ESC-4.2)
        if (isNewborn && (low.contains("60 se upar") || low.contains("60 breaths") || low.contains("fast breath"))) {
            return createResult("ESC-4.2", "Severe newborn respiratory distress", "fast breathing in newborn", "CRITICAL: REFER TO SNCU/NBSU (ESC-4.2)")
        }

        // -------------------------------------------------------------
        // CHILD EMERGENCIES (ESC-5)
        // -------------------------------------------------------------

        // 1. Child vomiting everything (ESC-5.1)
        val hasSingleVomitContext = low.contains("ek baar") || low.contains("single") || low.contains("after cough") || low.contains("khansi ke baad ek baar")
        if (!hasSingleVomitContext && (
                low.contains("vomit everything") || low.contains("vomiting everything") || low.contains("vomits everything") ||
                low.contains("vomits all fluids") || low.contains("persistent vomiting") || low.contains("sab ulti") ||
                low.contains("har baar ulti") || low.contains("lagatar ulti") || low.contains("kuch nahi thehar") ||
                low.contains("kuch nahi bach") || low.contains("paani bhi nahi") || low.contains("jo bhi khata ya peeta") ||
                low.contains("em thinna vomiting") || low.contains("em taagina ventane vanta") || low.contains("vanta aipotundi") ||
                low.contains("every sip") ||
                (low.contains("ulti") && (low.contains("kuch nahi ruk") || low.contains("paani bhi pet me nahi")))
            )
        ) {
            return createResult("ESC-5.1", "Child vomiting everything", "vomits everything", "CRITICAL DANGER SIGN: REFER IMMEDIATELY (Call 108) (ESC-5.1)")
        }

        // 2. Severe pneumonia / Chest indrawing (ESC-5.2)
        val hasNoChestIndrawing = low.contains("no chest indrawing") || low.contains("pasli nahi dhas") || low.contains("no indrawing")
        if (!hasNoChestIndrawing && (
                low.contains("chest indrawing") || low.contains("lower chest wall indrawing") || low.contains("subcostal indrawing") ||
                low.contains("pasli andar") || low.contains("pasli dhas") || low.contains("pasliyan andar") ||
                low.contains("seena dhas") || low.contains("seena dab") || low.contains("rib cage lopalki") ||
                low.contains("chest lopala") || low.contains("chest lopalki")
            )
        ) {
            return createResult("ESC-5.2", "Severe pneumonia / chest indrawing", "chest indrawing", "SEVERE PNEUMONIA DANGER SIGN: REFER IMMEDIATELY (ESC-5.2)")
        }

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

        // 1. Diabetic coma / severe hypoglycaemia / unconscious diabetic (ESC-6.3)
        val isDiabeticContext = low.contains("sugar") || low.contains("diabet") || low.contains("insulin")
        val isUnconscious = low.contains("behosh") || low.contains("unconscious") || low.contains("unresponsive") ||
                low.contains("hosh nahi") || low.contains("hosh me nahi") || low.contains("coma") ||
                low.contains("collapse") || low.contains("hypoglycaem") || low.contains("spruha thappi") ||
                low.contains("spruha ledu") || low.contains("spruha tappadam")
        if (isDiabeticContext && isUnconscious) {
            return createResult("ESC-6.3", "Diabetic coma / severe hypoglycaemia", "unconscious diabetic", "CRITICAL: IF CONSCIOUS GIVE SUGAR, REFER IMMEDIATELY (Call 108) (ESC-6.3)")
        }

        // 2. Suspected stroke / BE-FAST signs (ESC-6.2)
        val hasFacialDroop = low.contains("munh tedha") || low.contains("mukham vankara") || low.contains("facial droop") ||
                low.contains("facial palsy") || low.contains("face asymmetry")
        val hasArmWeakness = low.contains("haath kaam nahi") || low.contains("cheyi levaledu") || low.contains("arm drift") ||
                low.contains("hemiplegia") || low.contains("one-sided weakness") || low.contains("ek taraf ka haath") ||
                low.contains("ek taraf ka shareer") || low.contains("hath pair bekaar") || low.contains("unable to move left arm") ||
                low.contains("unable to move arm") || low.contains("lakwa")
        val hasSpeechLoss = low.contains("bol nahi pa rahe") || low.contains("bolti band") || low.contains("matalu ravatledu") ||
                low.contains("loss of speech") || low.contains("slurred speech") || low.contains("zubaan ladkhada")
        val isExplicitStroke = low.contains("suspected stroke") || low.contains("stroke signs") || low.contains("fast signs")
        if (isExplicitStroke || (hasFacialDroop && (hasArmWeakness || hasSpeechLoss)) || (hasArmWeakness && hasSpeechLoss) ||
            hasFacialDroop || (hasArmWeakness && (low.contains("sudden") || low.contains("achanak")))
        ) {
            return createResult("ESC-6.2", "Suspected stroke (BE-FAST signs)", "facial droop / weakness / slurred speech", "CRITICAL: SUSPECTED STROKE. CALL 108 REFERRAL NOW (ESC-6.2)")
        }

        // 3. Heart attack / Myocardial infarction (ESC-6.1)
        if ((low.contains("seene me dard") || low.contains("chest pain") || low.contains("chhati me dard")) &&
            (low.contains("paseena") || low.contains("baazu") || low.contains("arm") || low.contains("sweat") || low.contains("tight") || low.contains("ghabrahat"))
        ) {
            return createResult("ESC-6.1", "Suspected myocardial infarction / heart attack", "chest pain with sweating/radiation", "CRITICAL: CALL 108 REFERRAL NOW (ESC-6.1)")
        }

        // 4. Snakebite / Poisoning / Severe burns (ESC-1.4)
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
            action = "REFER NOW",
            sourceCitation = "MoHFW Protocol $leafId",
            note = "High-priority clinical emergency",
            phrasings = emptyMap()
        )
        val finalRule = rule.copy(action = "REFER NOW")
        val answer = "$dangerSign: REFER NOW, call 108 ($leafId)."
        return DangerMatchResult(
            matched = true,
            rule = finalRule,
            matchedPhrase = matchedPhrase,
            citedSectionId = leafId,
            authoritativeAnswer = answer
        )
    }
}
