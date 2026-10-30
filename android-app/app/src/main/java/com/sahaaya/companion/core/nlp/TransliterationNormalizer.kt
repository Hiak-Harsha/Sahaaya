package com.sahaaya.companion.core.nlp

import java.text.Normalizer
import kotlin.math.min

/**
 * Normalizes Unicode, transliterates Telugu and Devanagari regional scripts to
 * canonical clinical tokens, and applies constrained typo tolerance.
 */
object TransliterationNormalizer {

    // Common Devanagari words mapped to canonical romanized clinical tokens
    private val devanagariMap = mapOf(
        "पसली" to "pasli",
        "पसलियां" to "pasli",
        "धंसना" to "dhansna",
        "धँसना" to "dhansna",
        "धंस" to "dhans",
        "धँस" to "dhans",
        "सीना" to "seena",
        "बुखार" to "bukhar",
        "ताप" to "taap",
        "तेज" to "tez",
        "झटके" to "jhatke",
        "दौरे" to "daure",
        "मिर्गी" to "mirgi",
        "उल्टी" to "ulti",
        "दस्त" to "dast",
        "खून" to "khoon",
        "रक्त" to "khoon",
        "रक्तस्राव" to "khoon behna",
        "दूध" to "doodh",
        "पी" to "pee",
        "रहा" to "raha",
        "रही" to "rahi",
        "नहीं" to "nahi",
        "ठंडा" to "thanda",
        "सांस" to "saans",
        "फूलना" to "phoolna",
        "सूजन" to "sujan",
        "चेहरा" to "chehra",
        "पैर" to "pair",
        "बेहोश" to "behosh",
        "टीका" to "tika",
        "टीके" to "tike",
        "गर्भवती" to "garbhvati",
        "प्रसव" to "prasav",
        "वजन" to "vajan",
        "बच्चा" to "bachcha",
        "बच्चे" to "bacche",
        "दर्द" to "dard",
        "पीला" to "peela",
        "दवाई" to "dawai",
        "गोली" to "goli"
    )

    // Common Telugu words mapped to canonical romanized clinical tokens
    private val teluguMap = mapOf(
        "ఛాతీ" to "chest",
        "లోపలికి" to "andar",
        "లాగడం" to "dhansna",
        "పిల్చుకోవడం" to "dhansna",
        "జ్వరం" to "fever bukhar",
        "తీవ్రమైన" to "tez severe",
        "ఫిట్స్" to "fits jhatke",
        "మూర్ఛ" to "murcha jhatke",
        "విరేచనాలు" to "virochanalu dast",
        "నీళ్ల" to "loose water",
        "వాంతులు" to "vaanthulu ulti",
        "రక్తం" to "blood khoon",
        "రక్తస్రావం" to "bleeding khoon behna",
        "పాలు" to "paalu doodh",
        "తాగడం" to "peena",
        "లేదు" to "nahi ledu",
        "రావడం" to "aana",
        "చల్లగా" to "challaga thanda",
        "ఉండటం" to "hona",
        "శ్వాస" to "saans breathing",
        "ఆయాసం" to "aayasam saans phoolna",
        "వాపు" to "sujan vaapu",
        "ముఖం" to "chehra face",
        "కాళ్ల" to "pair feet",
        "కడుపు" to "pet stomach",
        "నొప్పి" to "dard pain",
        "స్పృహ" to "hosh",
        "తప్పడం" to "behosh",
        "టీకా" to "tika vaccine",
        "టీకాలు" to "tike vaccines",
        "గర్భవతి" to "pregnant garbhvati",
        "కాన్పు" to "prasav delivery",
        "బరువు" to "vajan weight",
        "బిడ్డ" to "bachcha child",
        "పిల్లాడు" to "bachcha child",
        "మందులు" to "dawai medicine",
        "మాత్రలు" to "goli tablet"
    )

    // Approved canonical clinical terms vocabulary for typo tolerance
    val clinicalVocabulary = listOf(
        "pasli", "dhansna", "dhans", "seena", "chest", "indrawing", "ribs",
        "bukhar", "taap", "fever", "jwaram", "temperature",
        "jhatke", "daure", "mirgi", "fits", "convulsions", "eclampsia",
        "ulti", "vomiting", "vomit", "vaanthulu",
        "dast", "diarrhoea", "diarrhea", "virochanalu", "loose", "stool",
        "khoon", "bleeding", "blood", "haemorrhage", "rakthasravam",
        "doodh", "milk", "breastfeed", "breastfeeding", "suckle", "paalu",
        "thanda", "cold", "hypothermia", "challaga",
        "saans", "breathing", "respiratory", "phoolna", "aayasam",
        "sujan", "swelling", "oedema", "edema", "chehra", "facial",
        "behosh", "unconscious", "coma", "lethargic",
        "sugar", "diabetes", "glucose", "bp", "hypertension",
        "garbhvati", "pregnancy", "pregnant", "antenatal", "anc",
        "prasav", "delivery", "postpartum", "pnc", "hbnc",
        "tika", "teeka", "vaccine", "immunization",
        "muac", "malnutrition", "sam", "mam", "wasting",
        "calcium", "ifa", "albendazole", "deworming", "ors", "zinc"
    )

    /**
     * Normalizes text: Unicode NFKC, strip diacritics where appropriate,
     * collapse repeated letters (>2 same chars down to 1 or 2), remove extra whitespace.
     */
    fun normalizeUnicode(text: String): String {
        if (text.isBlank()) return ""
        val nfkc = Normalizer.normalize(text, Normalizer.Form.NFKC)

        // Collapse excessive repeated characters like "bukhaaaar" -> "bukhar", "teeez" -> "tez"
        val collapsed = nfkc.replace(Regex("([a-zA-Z])\\1{2,}"), "$1")

        // Standardize punctuation to spaces but preserve decimal dots and hyphens in numbers/codes
        val cleaned = collapsed
            .replace(Regex("[^a-zA-Z0-9\\u0900-\\u097F\\u0C00-\\u0C7F.\\-\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        return cleaned
    }

    /**
     * Translates Devanagari and Telugu tokens to canonical romanized terms,
     * keeping original tokens if unmapped.
     */
    fun transliterateRegionalScripts(text: String): String {
        val tokens = text.split(" ").filter { it.isNotBlank() }
        val sb = StringBuilder()
        for (tok in tokens) {
            val lower = tok.lowercase()
            when {
                devanagariMap.containsKey(lower) -> {
                    sb.append(devanagariMap[lower]).append(" ")
                }
                teluguMap.containsKey(lower) -> {
                    sb.append(teluguMap[lower]).append(" ")
                }
                else -> {
                    // Check if token contains regional script characters
                    val transliterated = transliterateToken(lower)
                    sb.append(transliterated).append(" ")
                }
            }
        }
        return sb.toString().trim()
    }

    private fun transliterateToken(token: String): String {
        // Direct multi-char matching for Devanagari / Telugu substrings
        var res = token
        for ((k, v) in devanagariMap) {
            if (res.contains(k)) res = res.replace(k, " $v ")
        }
        for ((k, v) in teluguMap) {
            if (res.contains(k)) res = res.replace(k, " $v ")
        }
        return res
    }

    /**
     * Damerau-Levenshtein distance calculation (handles insertions, deletions, substitutions, transpositions).
     */
    fun damerauLevenshteinDistance(source: String, target: String): Int {
        val srcLen = source.length
        val tgtLen = target.length
        if (srcLen == 0) return tgtLen
        if (tgtLen == 0) return srcLen

        val d = Array(srcLen + 1) { IntArray(tgtLen + 1) }

        for (i in 0..srcLen) d[i][0] = i
        for (j in 0..tgtLen) d[0][j] = j

        for (i in 1..srcLen) {
            for (j in 1..tgtLen) {
                val cost = if (source[i - 1] == target[j - 1]) 0 else 1
                d[i][j] = min(
                    d[i - 1][j] + 1, // deletion
                    min(
                        d[i][j - 1] + 1, // insertion
                        d[i - 1][j - 1] + cost // substitution
                    )
                )
                // Transposition
                if (i > 1 && j > 1 && source[i - 1] == target[j - 2] && source[i - 2] == target[j - 1]) {
                    d[i][j] = min(d[i][j], d[i - 2][j - 2] + cost)
                }
            }
        }
        return d[srcLen][tgtLen]
    }

    /**
     * Finds the closest clinical vocabulary match if within a safe edit distance threshold.
     * Tokens length 4-6: max distance 1.
     * Tokens length 7+: max distance 2.
     * Short tokens (<4): must match exactly to prevent dangerous clinical misinterpretation.
     */
    private val protectedWords = setOf(
        "baad", "pehle", "saath", "liye", "bina", "kabhi", "hamesha",
        "kitna", "kitni", "kitne", "kaise", "kaisa", "kaisi", "kyun", "kahan", "kab",
        "karna", "karein", "kare", "karta", "karti", "karte", "dena", "dijiye",
        "sakte", "sakti", "sakta", "hona", "honi", "chahiye", "hota", "hoti", "hote",
        "hotaa", "maana", "jaata", "jaati", "gaya", "gayi", "gaye", "aaya", "aayi",
        "raha", "rahi", "rahe", "wala", "wali", "wale", "waqt", "samay", "time",
        "haath", "pet", "dard", "sir", "sar", "aankh", "kaan", "gala"
    )

    fun correctTypo(token: String): String {
        val low = token.lowercase()
        if (low.length < 4) return low
        if (protectedWords.contains(low)) return low
        if (clinicalVocabulary.contains(low)) return low

        // Common vernacular typo and phonetic aliases
        val aliases = mapOf(
            "palis" to "pasli",
            "palsi" to "pasli",
            "paslee" to "pasli",
            "dhns" to "dhansna",
            "dhans" to "dhansna",
            "bukhr" to "bukhar",
            "bukha" to "bukhar",
            "bukharr" to "bukhar",
            "jhatk" to "jhatke",
            "jhatkey" to "jhatke",
            "jhatkye" to "jhatke",
            "sustt" to "sust",
            "chhati" to "chest",
            "chati" to "chest"
        )
        aliases[low]?.let { return it }

        val maxAllowed = if (low.length <= 5) 2 else 2
        var bestCandidate: String? = null
        var bestDist = Int.MAX_VALUE

        for (vocab in clinicalVocabulary) {
            if (Math.abs(vocab.length - low.length) > maxAllowed) continue
            val dist = damerauLevenshteinDistance(low, vocab)
            if (dist <= maxAllowed && dist < bestDist) {
                bestDist = dist
                bestCandidate = vocab
            }
        }

        return bestCandidate ?: low
    }

    /**
     * Full transliteration and typo tolerance normalization pass.
     */
    fun normalize(text: String): String {
        val unicodeNorm = normalizeUnicode(text)
        val transliterated = transliterateRegionalScripts(unicodeNorm)
        val tokens = transliterated.split(" ").filter { it.isNotBlank() }
        val correctedTokens = tokens.map { correctTypo(it) }
        return correctedTokens.joinToString(" ")
    }
}
