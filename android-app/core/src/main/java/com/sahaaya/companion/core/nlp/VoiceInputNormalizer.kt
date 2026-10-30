package com.sahaaya.companion.core.nlp

object VoiceInputNormalizer {

    private val fillerPatterns = listOf(
        Regex("(?i)\\b(hello didi|didi suniye|suno didi|uh didi|aawaz aa rahi hai kya|gaadi ka shor hai thoda|signal weak hai|basically|matlab ki)\\b"),
        Regex("(?i)\\b(um+|uh+|er+|ah+)\\b")
    )

    private val acousticReplacements = listOf(
        Regex("(?i)\\b(painter\\s+violent|penta\\s+violent|painter\\s+valant|painta\\s+violent)\\b") to "pentavalent",
        Regex("(?i)\\b(mivac|mu\\s*ack|meowac)\\b") to "muac",
        Regex("(?i)\\b(f\\s+i\\s+p\\s+v|eff\\s+ipv|f\\s+ipv)\\b") to "fipv",
        Regex("(?i)\\b(b\\s+c\\s+g|bee\\s+see\\s+gee)\\b") to "bcg",
        Regex("(?i)\\b(o\\s+p\\s+v|oh\\s+pv)\\b") to "opv",
        Regex("(?i)\\b(p\\s+c\\s+v|pee\\s+see\\s+vee)\\b") to "pcv",
        Regex("(?i)\\b(p\\s+m\\s+s\\s+m\\s+a|pm\\s+sma)\\b") to "pmsma",
        Regex("(?i)\\b(j\\s+s\\s+s\\s+k)\\b") to "jssk",
        Regex("(?i)\\b(j\\s+s\\s+y)\\b") to "jsy",
        Regex("(?i)\\b(r\\s+v\\s+v)\\b") to "rvv",
        Regex("(?i)\\b(d\\s+p\\s+t)\\b") to "dpt",
        Regex("(?i)\\b(h\\s+b\\s+n\\s+c)\\b") to "hbnc",
        Regex("(?i)\\b(a\\s+n\\s+c)\\b") to "anc",
        Regex("(?i)\\b(i\\s+f\\s+a)\\b") to "ifa",
        Regex("(?i)\\b(o\\s+r\\s+s)\\b") to "ors",
        Regex("(?i)\\bhb(\\d+\\.?\\d*)\\b") to "hb $1",
        Regex("(?i)\\bifa(\\d+)\\b") to "ifa $1",
        Regex("(?i)\\borspackit\\b") to "ors packet"
    )

    private val compoundNumberReplacements = listOf(
        Regex("(?i)\\baath\\s+point\\s+chhah\\b") to "8.6",
        Regex("(?i)\\baath\\s+point\\s+che\\b") to "8.6",
        Regex("(?i)\\bsaat\\s+point\\s+chhah\\b") to "7.6",
        Regex("(?i)\\bsaat\\s+point\\s+che\\b") to "7.6",
        Regex("(?i)\\bdedh\\s+sau\\b") to "150",
        Regex("(?i)\\bdhai\\s+sau\\b") to "250",
        Regex("(?i)\\bek\\s+sau\\s+assi\\b") to "180",
        Regex("(?i)\\bek\\s+sau\\s+chaalees\\b") to "140",
        Regex("(?i)\\bek\\s+sau\\s+bees\\b") to "120",
        Regex("(?i)\\bek\\s+sau\\b") to "100",
        Regex("(?i)\\bdo\\s+sau\\b") to "200"
    )

    private val wordToDigit = mapOf(
        "ek" to "1", "do" to "2", "teen" to "3", "chaar" to "4", "paanch" to "5",
        "chhah" to "6", "che" to "6", "saat" to "7", "aath" to "8", "nau" to "9", "das" to "10",
        "gyarah" to "11", "baarah" to "12", "terah" to "13", "chaudah" to "14", "pandrah" to "15",
        "solah" to "16", "satrah" to "17", "atharah" to "18", "unnees" to "19", "bees" to "20",
        "okati" to "1", "rendu" to "2", "moodu" to "3", "naalugu" to "4", "aidu" to "5",
        "aaru" to "6", "yedu" to "7", "enimidi" to "8", "thommidi" to "9", "padi" to "10"
    )

    fun normalize(text: String): String {
        var result = text

        // 1. Strip acoustic fillers
        for (pattern in fillerPatterns) {
            result = pattern.replace(result, " ")
        }

        // 2. ASR acoustic substitutions
        for ((regex, replacement) in acousticReplacements) {
            result = regex.replace(result, replacement)
        }

        // 3. Spoken compound numbers to digits
        for ((regex, replacement) in compoundNumberReplacements) {
            result = regex.replace(result, replacement)
        }

        // 4. Single number words before temporal/clinical units
        for ((word, digit) in wordToDigit) {
            val reg = Regex("(?i)\\b$word\\s+(hafte|mahine|saal|din|hours|ghante|weeks|months|years|days|tika|teeka|dose)\\b")
            result = reg.replace(result, "$digit $1")
        }

        return result.replace(Regex("\\s+"), " ").trim()
    }
}
