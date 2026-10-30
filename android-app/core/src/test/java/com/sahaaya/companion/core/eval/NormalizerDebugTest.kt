package com.sahaaya.companion.core.eval

import com.sahaaya.companion.core.nlp.TransliterationNormalizer
import org.junit.Test

class NormalizerDebugTest {

    @Test
    fun testFindWhichTokenBecameComa() {
        val q = "ek baat poochni thi, ek aadmi ka HIV test positive aaya hai, ab uska kya ilaaj hoga?"
        println("Original: $q")
        println("Normalized: ${TransliterationNormalizer.normalize(q)}")

        for (token in q.split(" ")) {
            val clean = token.lowercase().replace(Regex("[^a-z]"), "")
            val normTok = TransliterationNormalizer.correctTypo(clean)
            if (normTok != clean) {
                println("  Token '$clean' -> '$normTok'")
            }
        }
    }
}
