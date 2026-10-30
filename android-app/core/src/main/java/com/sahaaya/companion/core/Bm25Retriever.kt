package com.sahaaya.companion.core

import com.sahaaya.companion.core.nlp.ClinicalCalculators
import kotlin.math.ln

class Bm25Retriever(
    private val chunks: List<ProtocolChunk>,
    private val k1: Double = 1.5,
    private val b: Double = 0.75,
    private val scoreThreshold: Double = 0.5
) {

    private val synonyms = mapOf(
        "dast" to listOf("diarrhoea", "loose", "stool"),
        "virochanalu" to listOf("diarrhoea", "loose", "stool"),
        "virochanam" to listOf("diarrhoea", "loose", "stool"),
        "pillalu" to listOf("children", "child", "pediatric"),
        "paalu" to listOf("milk", "breastfeed", "breastfeeding"),
        "bukhar" to listOf("fever", "temperature"),
        "taap" to listOf("fever", "temperature"),
        "tika" to listOf("vaccine", "immunization"),
        "teeka" to listOf("vaccine", "immunization"),
        "tike" to listOf("vaccine", "immunization"),
        "khoon" to listOf("blood", "bleeding", "anaemia", "haemoglobin"),
        "jhatke" to listOf("convulsions", "fits", "eclampsia"),
        "mirgi" to listOf("convulsions", "fits", "eclampsia"),
        "pasli" to listOf("chest", "indrawing", "fast", "breathing"),
        "sujan" to listOf("swelling", "oedema", "feet"),
        "suja" to listOf("swelling", "oedema", "face"),
        "ulti" to listOf("vomiting", "vomit"),
        "doodh" to listOf("breastfeeding", "milk", "suckle", "suckling"),
        "peela" to listOf("jaundice", "pallor", "yellow"),
        "peeli" to listOf("pallor", "anaemia", "pale"),
        "pregnant" to listOf("pregnancy", "antenatal", "anc", "gestational"),
        "garbhvati" to listOf("pregnancy", "antenatal", "anc", "gestational"),
        "prasav" to listOf("delivery", "labour", "postpartum"),
        "sugar" to listOf("diabetes", "glucose", "blood", "sugar", "fasting", "rbs"),
        "bp" to listOf("blood", "pressure", "hypertension", "systolic", "diastolic"),
        "goli" to listOf("tablet", "medicine", "ifa", "calcium", "albendazole"),
        "lal" to listOf("red", "ifa", "iron"),
        "nehla" to listOf("bathe", "bathing", "bath", "2000"),
        "nehlana" to listOf("bathe", "bathing", "bath", "2000"),
        "mahine" to listOf("months", "month"),
        "hafte" to listOf("weeks", "week"),
        "saal" to listOf("years", "year"),
        "keede" to listOf("deworming", "albendazole"),
        "kami" to listOf("anaemia", "deficiency"),
        "geela" to listOf("enuresis", "bedwetting"),
        "thanda" to listOf("hypothermia", "cold", "temperature", "skin", "kmc"),
        "sust" to listOf("lethargic", "lethargy", "drowsy"),
        "behosh" to listOf("unconscious", "coma"),
        "saans" to listOf("breathing", "respiratory", "breaths"),
        "pmsma" to listOf("pmsma", "9th", "second", "third", "trimester", "12", "weeks"),
        "jssk" to listOf("jssk", "transport", "drop", "back", "free", "48", "hours"),
        "muac" to listOf("muac", "tape", "green", "yellow", "red", "malnutrition", "12.5", "11.5"),
        "hbnc" to listOf("hbnc", "newborn", "visit", "institutional", "home", "caesarean", "42", "6", "visits", "day", "7"),
        "fipv" to listOf("fipv", "minimum", "gap", "8", "weeks", "doses"),
        "penta" to listOf("pentavalent", "penta", "till", "one", "year", "of", "age"),
        "bcg" to listOf("bcg", "birth", "one", "year"),
        "mr" to listOf("mr", "measles", "rubella", "5", "years"),
        "vitamin" to listOf("vitamin", "a", "6", "months", "9", "doses"),
        "td" to listOf("td", "tetanus", "toxoid", "4", "weeks", "pregnancy", "td-1", "td-2"),
        "exercise" to listOf("physical", "activity", "30", "minutes", "5", "days", "week", "150"),
        "overdue" to listOf("overdue", "grace", "period", "28", "days"),
        "chautha" to listOf("fourth", "4th", "36", "weeks", "anc-4"),
        "hb" to listOf("haemoglobin", "moderate", "anaemia", "7.0", "9.9"),
        "jachcha" to listOf("postnatal", "pnc", "mother", "pads", "foul", "discharge")
    )

    private val docTokens = mutableListOf<List<String>>()
    private val docLengths = mutableListOf<Int>()
    private val termDocFreq = mutableMapOf<String, Int>()
    private val numDocs = chunks.size
    private var avgDocLength = 0.0

    init {
        var totalLength = 0
        for (chunk in chunks) {
            val kws = chunk.keywords.joinToString(" ")
            val syns = chunk.approvedSynonyms.joinToString(" ")
            val content = "${chunk.title} ${chunk.title} ${chunk.title} $kws $kws $syns ${chunk.text}"
            val tokens = tokenizeAndExpand(content)
            docTokens.add(tokens)
            docLengths.add(tokens.size)
            totalLength += tokens.size

            val uniqueTerms = tokens.toSet()
            for (term in uniqueTerms) {
                termDocFreq[term] = termDocFreq.getOrDefault(term, 0) + 1
            }
        }
        avgDocLength = if (numDocs > 0) totalLength.toDouble() / numDocs else 1.0
    }

    private fun tokenize(text: String): List<String> {
        return text.lowercase()
            .replace(Regex("[^a-zA-Z0-9\\u0900-\\u097F\\u0C00-\\u0C7F\\s]"), " ")
            .split(" ")
            .filter { it.isNotBlank() && it.length > 1 }
    }

    private fun tokenizeAndExpand(text: String): List<String> {
        val baseTokens = tokenize(text)
        val expanded = mutableListOf<String>()
        for (token in baseTokens) {
            expanded.add(token)
            synonyms[token]?.let { synList ->
                expanded.addAll(synList)
            }
        }
        return expanded
    }

    fun search(query: String): RetrievalResult? {
        val queryTerms = tokenizeAndExpand(query)
        if (queryTerms.isEmpty()) return null

        // 1. Clinical entity & deterministic calculators
        val calcResult = ClinicalCalculators.evaluate(query)
        if (calcResult != null && calcResult.matched) {
            val matchedChunk = chunks.firstOrNull { it.id == calcResult.targetSectionId }
            if (matchedChunk != null) {
                return RetrievalResult(
                    chunk = matchedChunk,
                    score = 99.0,
                    authoritativeAnswer = calcResult.answerText,
                    citedSectionIds = calcResult.citedSectionIds,
                    evidenceStrength = "STRONG_PROTOCOL_EVIDENCE"
                )
            }
        }

        // 2. Standard BM25 scoring
        val queryFreq = mutableMapOf<String, Int>()
        for (t in queryTerms) {
            queryFreq[t] = queryFreq.getOrDefault(t, 0) + 1
        }

        var bestScore = -1.0
        var bestIndex = -1

        for (i in 0 until numDocs) {
            var score = 0.0
            val docLen = docLengths[i]
            val docTokList = docTokens[i]

            val docTermCounts = mutableMapOf<String, Int>()
            for (t in docTokList) {
                if (queryFreq.containsKey(t)) {
                    docTermCounts[t] = docTermCounts.getOrDefault(t, 0) + 1
                }
            }

            for ((term, _) in queryFreq) {
                val tf = docTermCounts.getOrDefault(term, 0)
                if (tf == 0) continue

                val df = termDocFreq.getOrDefault(term, 1)
                val idf = ln(1.0 + (numDocs - df + 0.5) / (df + 0.5))
                val tfComponent = (tf * (k1 + 1.0)) / (tf + k1 * (1.0 - b + b * (docLen.toDouble() / avgDocLength)))
                score += idf * tfComponent
            }

            if (score > bestScore) {
                bestScore = score
                bestIndex = i
            }
        }

        if (bestIndex == -1 || bestScore < scoreThreshold) {
            return null
        }

        val topChunk = chunks[bestIndex]
        val (authoritativeAnswer, citedIds) = buildGroundedAnswer(topChunk, query)
        return RetrievalResult(
            chunk = topChunk,
            score = bestScore,
            authoritativeAnswer = authoritativeAnswer,
            citedSectionIds = citedIds,
            evidenceStrength = if (bestScore > 5.0) "STRONG_PROTOCOL_EVIDENCE" else "MODERATE_EVIDENCE"
        )
    }

    private fun buildGroundedAnswer(chunk: ProtocolChunk, query: String): Pair<String, List<String>> {
        val sid = chunk.id
        val related = when (sid) {
            "ANC-3.1" -> listOf("ANC-3.2")
            "ANC-2.5" -> listOf("IMM-4.1")
            "IMM-4.1" -> listOf("ANC-2.5")
            "ANC-2.2" -> listOf("HBNC-3.1")
            "HBNC-3.1" -> listOf("ANC-2.2")
            "CHD-1.2" -> listOf("CHD-1.1", "ESC-5.1")
            "ESC-6.3" -> listOf("ESC-1.3")
            "NUT-1.2" -> listOf("ESC-5.5")
            "HBNC-2.2" -> listOf("HBNC-2.1")
            else -> emptyList()
        }
        val citedIds = (listOf(sid) + related).distinct().take(2)

        val points = ClinicalTextFormatter.formatToPoints(chunk.text)
        val queryTokens = tokenize(query).toSet()

        val scoredPoints = points.map { pt ->
            val ptTokens = tokenize(pt).toSet()
            val overlap = (queryTokens intersect ptTokens).size
            pt to overlap
        }.sortedByDescending { it.second }

        val best = scoredPoints.filter { it.second > 0 }.map { it.first }
        val candidateText = when {
            best.isNotEmpty() -> best.take(2).joinToString("; ")
            points.isNotEmpty() -> points.take(2).joinToString("; ")
            else -> chunk.text.replace(Regex("[#*|>]"), " ").replace(Regex("\\s+"), " ").trim()
        }

        val clean = if (candidateText.length > 330) {
            candidateText.substring(0, 327).trim() + "..."
        } else {
            candidateText.trim()
        }

        val suffix = " (${citedIds.joinToString(", ")})."
        val ans = if (clean.endsWith(".")) {
            "${clean.removeSuffix(".")} $suffix"
        } else {
            "$clean$suffix"
        }

        return ans.take(400) to citedIds
    }
}
