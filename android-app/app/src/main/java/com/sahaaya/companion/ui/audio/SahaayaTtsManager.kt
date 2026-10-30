package com.sahaaya.companion.ui.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import com.sahaaya.companion.ui.i18n.AppLanguage
import java.util.Locale

class SahaayaTtsManager(context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                tts?.setPitch(1.0f)
                tts?.setSpeechRate(0.95f) // slightly slower for clinical clarity
            }
        }
    }

    fun speak(text: String, language: AppLanguage) {
        if (!isInitialized || tts == null) return

        val locale = when (language) {
            AppLanguage.HINDI -> Locale("hi", "IN")
            AppLanguage.TELUGU -> Locale("te", "IN")
            AppLanguage.ENGLISH -> Locale.ENGLISH
        }

        try {
            tts?.language = locale
        } catch (e: Exception) {
            tts?.language = Locale.ENGLISH
        }

        val speechClean = preparePhoneticText(text, language)
        tts?.speak(speechClean, TextToSpeech.QUEUE_FLUSH, null, "SAHAAYA_CLINICAL_SPEECH")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }

    private fun preparePhoneticText(raw: String, language: AppLanguage): String {
        var clean = raw
            .replace("#", "")
            .replace("*", "")
            .replace("|", " ")
            .replace(">", "")
            .replace("`", "")

        // Clinical terminology expansion for natural speech pronunciation
        clean = clean
            .replace(Regex("(?i)\\bbp\\b"), if (language == AppLanguage.HINDI) "blood pressure" else "raktha potu")
            .replace(Regex("(?i)\\bhb\\b"), "haemoglobin")
            .replace(Regex("(?i)\\bg/dl\\b"), "gram per decilitre")
            .replace(Regex("(?i)\\banc\\b"), "antenatal checkup")
            .replace(Regex("(?i)\\bifa\\b"), if (language == AppLanguage.HINDI) "iron goli" else "iron tablet")
            .replace(Regex("(?i)\\bors\\b"), "ORS jeevan jal")
            .replace(Regex("(?i)\\bmuac\\b"), "muac tape")
            .replace(Regex("(?i)\\bfipv\\b"), "fractional IPV")
            .replace(Regex("(?i)\\bopv\\b"), "polio drops")
            .replace(Regex("(?i)\\bmg/dl\\b"), "milligram per decilitre")
            .replace(Regex("(?i)\\bmmhg\\b"), "millimeter of mercury")

        return clean.replace(Regex("\\s+"), " ").trim()
    }
}
