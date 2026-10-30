package com.sahaaya.companion.features.advisory

import android.content.Intent
import com.sahaaya.companion.ui.i18n.AppLanguage

data class PatientAdvisory(
    val title: String,
    val plainMessage: String,
    val actionableGuidance: List<String>,
    val warningSign: String,
    val citedProtocol: String,
    val language: AppLanguage
)

object PatientAdvisoryEngine {

    fun generateAdvisory(
        protocolChunkId: String,
        language: AppLanguage
    ): PatientAdvisory {
        return when (protocolChunkId) {
            "ANC-2.2", "ANC-2.3" -> generateIraCalciumAdvisory(language)
            "CHD-1.2", "CHD-1.3" -> generateDiarrhoeaAdvisory(language)
            "IMM-1.1", "IMM-2.1", "IMM-2.2", "IMM-2.3" -> generateImmunizationAdvisory(language)
            "HBNC-1.1", "HBNC-2.2" -> generateNewbornCareAdvisory(language)
            "NCD-1.3", "NCD-3.1" -> generateDiabetesLifestyleAdvisory(language)
            else -> generateGeneralAdvisory(protocolChunkId, language)
        }
    }

    private fun generateIraCalciumAdvisory(lang: AppLanguage): PatientAdvisory {
        return when (lang) {
            AppLanguage.HINDI -> PatientAdvisory(
                title = "आयरन और कैल्शियम गोलियों का सही नियम",
                plainMessage = "गर्भावस्था और प्रसव के बाद शरीर में खून की कमी न हो, इसके लिए रोज एक लाल आयरन (IFA) गोली 180 दिन तक लें।",
                actionableGuidance = listOf(
                    "आयरन की गोली खाना खाने के बाद नींबू पानी या पानी के साथ लें।",
                    "कैल्शियम की गोली हमेशा भोजन के साथ लें, खाली पेट कभी न लें।",
                    "आयरन और कैल्शियम की गोलियों में कम से कम 2 घंटे का अंतर रखें। चाय या दूध के साथ न लें।"
                ),
                warningSign = "यदि अत्यधिक कमजोरी, सांस फूलना या चेहरे पर पीलापन दिखे तो तुरंत अस्पताल जाएं।",
                citedProtocol = "ANC-2.2, ANC-2.3",
                language = lang
            )
            AppLanguage.TELUGU -> PatientAdvisory(
                title = "ఐరన్ మరియు కాల్షియం మాత్రల సరైన పద్ధతి",
                plainMessage = "రక్తహీనత రాకుండా ప్రసవం తర్వాత 180 రోజుల పాటు ప్రతిరోజూ ఒక ఎర్ర ఐరన్ మాత్ర తీసుకోవాలి.",
                actionableGuidance = listOf(
                    "ఐరన్ మాత్రను భోజనం తర్వాత నీటితో లేదా నిమ్మరసంతో వేసుకోవాలి.",
                    "కాల్షియం మాత్రను ఖాళీ కడుపుతో తీసుకోకూడదు, భోజనంతో పాటు తీసుకోవాలి.",
                    "ఐరన్ మరియు కాల్షియం మాత్రల మధ్య కనీసం 2 గంటల వ్యవధి ఉండాలి. టీ లేదా పాలతో తీసుకోకూడదు."
                ),
                warningSign = "తీవ్రమైన బలహీనత, ఆయాసం కనిపిస్తే వెంటనే ఆసుపత్రికి వెళ్ళండి.",
                citedProtocol = "ANC-2.2, ANC-2.3",
                language = lang
            )
            AppLanguage.ENGLISH -> PatientAdvisory(
                title = "Iron & Calcium Tablet Guidelines",
                plainMessage = "To prevent anaemia, take one Red IFA tablet daily for 180 days during pregnancy and postpartum.",
                actionableGuidance = listOf(
                    "Take IFA tablet after meals with water or lemon water for better absorption.",
                    "Take Calcium tablets with meals, never on an empty stomach.",
                    "Maintain at least a 2-hour gap between Iron and Calcium tablets. Do not take with tea or milk."
                ),
                warningSign = "If severe breathlessness or extreme pallor develops, visit hospital immediately.",
                citedProtocol = "ANC-2.2, ANC-2.3",
                language = lang
            )
        }
    }

    private fun generateDiarrhoeaAdvisory(lang: AppLanguage): PatientAdvisory {
        return when (lang) {
            AppLanguage.HINDI -> PatientAdvisory(
                title = "दस्त (Diarrhoea) में बच्चे की देखभाल",
                plainMessage = "दस्त होने पर शरीर में पानी और नमक की कमी न होने दें। ओआरएस (ORS) और जिंक का घोल तुरंत शुरू करें।",
                actionableGuidance = listOf(
                    "हर दस्त के बाद बच्चे को ओआरएस का घोल घूंट-घूंट करके पिलाएं।",
                    "यदि बच्चा उल्टी कर दे, तो 10 मिनट रुकें और फिर धीरे-धीरे पिलाएं।",
                    "जिंक की 1 गोली 14 दिनों तक लगातार पानी या मां के दूध में घोलकर दें।"
                ),
                warningSign = "यदि बच्चा दूध न पी सके, आंखें धंस जाएं, या लगातार उल्टी करे तो तुरंत 108 पर कॉल करें।",
                citedProtocol = "CHD-1.2, CHD-1.3",
                language = lang
            )
            AppLanguage.TELUGU -> PatientAdvisory(
                title = "విరేచనాల సమయంలో శిశువు సంరక్షణ",
                plainMessage = "విరేచనాలు అయినప్పుడు శరీరంలో నీటిశాతం తగ్గకుండా ORS ద్రావణం మరియు జింక్ మాత్రలు ఇవ్వాలి.",
                actionableGuidance = listOf(
                    "ప్రతి విరేచనం తర్వాత ఓఆర్ఎస్ ద్రావణాన్ని కొద్దికొద్దిగా తాగించండి.",
                    "వాంతి చేసుకుంటే 10 నిమిషాలు ఆగి, నెమ్మదిగా మళ్లీ తాగించండి.",
                    "జింక్ మాత్రను 14 రోజుల పాటు క్రమం తప్పకుండా నీటిలో కరిగించి ఇవ్వాలి."
                ),
                warningSign = "శిశువు పాలు తాగలేకపోతే లేదా కళ్లు గుంటలు పడితే వెంటనే 108 కి కాల్ చేయండి.",
                citedProtocol = "CHD-1.2, CHD-1.3",
                language = lang
            )
            AppLanguage.ENGLISH -> PatientAdvisory(
                title = "Child Diarrhoea Management & Hydration",
                plainMessage = "Prevent dehydration by starting ORS solution and Zinc dispersible tablets immediately.",
                actionableGuidance = listOf(
                    "Offer ORS solution in frequent small sips after every loose stool.",
                    "If child vomits, wait 10 minutes, then continue ORS slowly.",
                    "Complete the full 14-day course of Zinc dispersible tablets."
                ),
                warningSign = "If child cannot drink/breastfeed or is lethargic, refer immediately and call 108.",
                citedProtocol = "CHD-1.2, CHD-1.3",
                language = lang
            )
        }
    }

    private fun generateImmunizationAdvisory(lang: AppLanguage): PatientAdvisory {
        return when (lang) {
            AppLanguage.HINDI -> PatientAdvisory(
                title = "टीकाकरण समय सारणी",
                plainMessage = "समय पर सभी टीके लगवाने से बच्चा 12 जानलेवा बीमारियों से सुरक्षित रहता है।",
                actionableGuidance = listOf(
                    "जन्म पर बीसीजी, हेपेटाइटिस-बी 0 और ओपीवी 0 अवश्य लगवाएं।",
                    "6, 10 और 14 हफ्ते पर पेंटावेलेंट, पोलियो और रोटावायरस टीके पूरे कराएं।",
                    "टीका कार्ड (MCP Card) को हमेशा सुरक्षित रखें और हर सत्र में साथ लाएं।"
                ),
                warningSign = "यदि कोई टीका छूट गया है, तो अगले सत्र में एएनएम दीदी से तुरंत कैच-अप टीका लगवाएं।",
                citedProtocol = "IMM-1.1, IMM-2.1",
                language = lang
            )
            AppLanguage.TELUGU -> PatientAdvisory(
                title = "సకాలంలో టీకాల సమాచారం",
                plainMessage = "పిల్లలకు సకాలంలో టీకాలు వేయించడం ద్వారా ప్రాణాంతక వ్యాధుల నుండి కాపాడుకోవచ్చు.",
                actionableGuidance = listOf(
                    "పుట్టిన వెంటనే BCG, HepB-0, OPV-0 టీకాలు తప్పనిసరిగా వేయించాలి.",
                    "6, 10, 14 వారాలకు పెంటావాలెంట్ మరియు పోలియో టీకాలు వేయించాలి.",
                    "టీకా కార్డును భద్రంగా ఉంచి ప్రతి సమావేశానికి వెంట తీసుకురావాలి."
                ),
                warningSign = "ఏదైనా టీకా ఆలస్యమైతే, సమీప సబ్-సెంటర్‌లో వెంటనే వేయించండి.",
                citedProtocol = "IMM-1.1, IMM-2.1",
                language = lang
            )
            AppLanguage.ENGLISH -> PatientAdvisory(
                title = "Timely Childhood Immunization",
                plainMessage = "Complete all childhood immunizations according to the national schedule to protect against severe infections.",
                actionableGuidance = listOf(
                    "Ensure birth doses (BCG, HepB-0, OPV-0) within the first 24-48 hours.",
                    "Complete primary series at 6, 10, and 14 weeks.",
                    "Always bring and safeguard the MCP card at every healthcare session."
                ),
                warningSign = "If vaccines are overdue by > 28 days, visit the Sub-Centre session immediately for catch-up.",
                citedProtocol = "IMM-1.1, IMM-2.1",
                language = lang
            )
        }
    }

    private fun generateNewbornCareAdvisory(lang: AppLanguage): PatientAdvisory {
        return when (lang) {
            AppLanguage.HINDI -> PatientAdvisory(
                title = "नवजात शिशु की देखभाल",
                plainMessage = "नवजात शिशु को गर्म रखना और केवल मां का दूध देना सबसे महत्वपूर्ण है।",
                actionableGuidance = listOf(
                    "जन्म के पहले 6 महीने केवल मां का दूध दें, पानी या घुट्टी बिल्कुल न दें।",
                    "कम वजन वाले बच्चे को नहलाने में तब तक देरी करें जब तक वजन 2000 ग्राम न हो जाए।",
                    "बच्चे को मां की छाती से सटाकर रखें (कंगारू मदर केयर) ताकि ठंड न लगे।"
                ),
                warningSign = "यदि शिशु छूने में ठंडा लगे या दूध न पी सके, तो तुरंत 108 पर कॉल करें।",
                citedProtocol = "HBNC-1.1, HBNC-2.2",
                language = lang
            )
            AppLanguage.TELUGU -> PatientAdvisory(
                title = "నవజాత శిశు సంరక్షణ",
                plainMessage = "శిశువును వెచ్చగా ఉంచడం మరియు తల్లిపాలు మాత్రమే ఇవ్వడం అత్యంత ముఖ్యం.",
                actionableGuidance = listOf(
                    "మొదటి 6 నెలలు తల్లిపాలు మాత్రమే ఇవ్వాలి, ఇతర ఆహారాలు ఇవ్వకూడదు.",
                    "తక్కువ బరువున్న శిశువు బరువు 2000 గ్రాములు అయ్యే వరకు స్నానం చేయించకూడదు.",
                    "శిశువును తల్లి ఛాతీకి ఆనించి వెచ్చగా ఉంచాలి (కంగారూ మదర్ కేర్)."
                ),
                warningSign = "శిశువు శరీరం చల్లగా మారినా లేదా పాలు తాగలేకపోయినా వెంటనే 108 కి కాల్ చేయండి.",
                citedProtocol = "HBNC-1.1, HBNC-2.2",
                language = lang
            )
            AppLanguage.ENGLISH -> PatientAdvisory(
                title = "Essential Newborn Care & Thermal Protection",
                plainMessage = "Keep newborn baby warm and practice exclusive breastfeeding for optimal survival.",
                actionableGuidance = listOf(
                    "Exclusive breastfeeding for the first 6 months; no water, honey, or animal milk.",
                    "Delay bathing until baby weighs at least 2000 g and gains weight steadily.",
                    "Maintain continuous skin-to-skin contact (KMC) to prevent hypothermia."
                ),
                warningSign = "If baby feels cold to touch or cannot suckle, call 108 immediately.",
                citedProtocol = "HBNC-1.1, HBNC-2.2",
                language = lang
            )
        }
    }

    private fun generateDiabetesLifestyleAdvisory(lang: AppLanguage): PatientAdvisory {
        return when (lang) {
            AppLanguage.HINDI -> PatientAdvisory(
                title = "डायबिटीज (शुगर) और बीपी नियंत्रण",
                plainMessage = "नियमित शारीरिक गतिविधि और उचित खानपान से शुगर और रक्तचाप नियंत्रित रहता है।",
                actionableGuidance = listOf(
                    "हफ्ते में कम से कम 5 दिन, रोज 30 मिनट मध्यम व्यायाम या तेज पैदल चलें (कुल 150 मिनट)।",
                    "खाने में नमक, चीनी और तले हुए खाद्य पदार्थों की मात्रा कम करें।",
                    "हर महीने पीएचसी या सब-सेंटर पर जाकर अपनी बीपी और शुगर की जांच कराएं।"
                ),
                warningSign = "यदि बेहोशी, अत्यधिक चक्कर, या छाती में दर्द हो तो तुरंत अस्पताल पहुंचें।",
                citedProtocol = "NCD-1.3, NCD-3.1",
                language = lang
            )
            AppLanguage.TELUGU -> PatientAdvisory(
                title = "షుగర్ మరియు బిపి నియంత్రణ",
                plainMessage = "క్రమంతప్పకుండా నడక మరియు ఆరోగ్యకరమైన ఆహారంతో బిపి, షుగర్ అదుపులో ఉంచుకోవచ్చు.",
                actionableGuidance = listOf(
                    "వారానికి కనీసం 5 రోజులు, రోజుకు 30 నిమిషాలు వేగంగా నడవాలి (మొత్తం 150 నిమిషాలు).",
                    "ఉప్పు, చక్కెర మరియు నూనె పదార్థాలను తగ్గించాలి.",
                    "ప్రతినెలా పీహెచ్‌సీలో బిపి, షుగర్ పరీక్షలు చేయించుకోవాలి."
                ),
                warningSign = "స్పృహ తప్పినా లేదా ఛాతీ నొప్పి వచ్చినా వెంటనే ఆసుపత్రికి వెళ్ళండి.",
                citedProtocol = "NCD-1.3, NCD-3.1",
                language = lang
            )
            AppLanguage.ENGLISH -> PatientAdvisory(
                title = "Diabetes & Hypertension Lifestyle Guidance",
                plainMessage = "Regular physical activity and reduced sodium/sugar intake keep blood pressure and glucose controlled.",
                actionableGuidance = listOf(
                    "Engage in at least 30 minutes of moderate physical activity 5 days a week (150 mins total).",
                    "Limit dietary salt, refined sugars, and fried foods.",
                    "Attend monthly non-communicable disease follow-up at the Sub-Centre or PHC."
                ),
                warningSign = "If chest tightness, sudden dizziness, or loss of consciousness occurs, seek emergency care.",
                citedProtocol = "NCD-1.3, NCD-3.1",
                language = lang
            )
        }
    }

    private fun generateGeneralAdvisory(protocolChunkId: String, lang: AppLanguage): PatientAdvisory {
        return PatientAdvisory(
            title = "Official Frontline Health Advisory ($protocolChunkId)",
            plainMessage = "Follow national frontline health protocols closely for maternal, child, and adult wellbeing.",
            actionableGuidance = listOf(
                "Attend scheduled visits at your local Health & Wellness Sub-Centre.",
                "Consult the ANM or Medical Officer at the PHC for medical prescriptions.",
                "Contact your community ASHA worker for any health guidance."
            ),
            warningSign = "In case of any danger signs, call 108 Emergency Ambulance immediately.",
            citedProtocol = protocolChunkId,
            language = lang
        )
    }

    fun formatShareableText(advisory: PatientAdvisory): String {
        val guidance = advisory.actionableGuidance.mapIndexed { idx, s -> "${idx + 1}. $s" }.joinToString("\n")
        return """
            📋 *${advisory.title}*
            -----------------------------------------
            ${advisory.plainMessage}
            
            *महत्वपूर्ण निर्देश / Key Instructions:*
            $guidance
            
            ⚠️ *सावधानी / Warning Sign:*
            ${advisory.warningSign}
            
            (Source: Official Health Protocol ${advisory.citedProtocol})
            - Sahaaya Offline Frontline Health Companion
        """.trimIndent()
    }

    fun createShareIntent(advisoryText: String): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Sahaaya Health Advisory")
            putExtra(Intent.EXTRA_TEXT, advisoryText)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
}

