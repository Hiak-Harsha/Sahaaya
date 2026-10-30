package com.sahaaya.companion.ui.i18n

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "Hindi", "हिन्दी"),
    TELUGU("te", "Telugu", "తెలుగు")
}

object Strings {

    // App Bar & Global
    fun appTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Sahaaya"
        AppLanguage.HINDI -> "सहाय"
        AppLanguage.TELUGU -> "సహాయ"
    }

    fun appSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Frontline Clinical Companion"
        AppLanguage.HINDI -> "अग्रिम पंक्ति स्वास्थ्य साथी"
        AppLanguage.TELUGU -> "ఫ్రంట్‌లైన్ ఆరోగ్య సహచరి"
    }

    fun tabAsk(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Ask Sahaaya"
        AppLanguage.HINDI -> "सहाय से पूछें"
        AppLanguage.TELUGU -> "సహాయను అడగండి"
    }

    fun tabVisits(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Today's Visits"
        AppLanguage.HINDI -> "आज की गृह भेंट"
        AppLanguage.TELUGU -> "నేటి సందర్శనలు"
    }

    // Ask Screen
    fun askHeader(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Ask Sahaaya Protocol Guide"
        AppLanguage.HINDI -> "सहाय प्रोटोकॉल मार्गदर्शिका"
        AppLanguage.TELUGU -> "సహాయ ప్రోటోకాల్ గైడ్"
    }

    fun askSubheader(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "100% Offline | MoHFW & NHM Protocols | Safety-First Escalation"
        AppLanguage.HINDI -> "100% ऑफ़लाइन | स्वास्थ्य मंत्रालय प्रोटोकॉल | सुरक्षा-प्रथम रेफरल"
        AppLanguage.TELUGU -> "100% ఆఫ్‌లైన్ | NHM & MoHFW ప్రోటోకాల్స్ | అత్యవసర రిఫరల్"
    }

    fun searchPlaceholder(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Enter symptom, sign or question (English / Hinglish / Telugu)..."
        AppLanguage.HINDI -> "लक्षण, संकेत या प्रश्न लिखें (हिन्दी / English)..."
        AppLanguage.TELUGU -> "లక్షణం, సమస్య లేదా ప్రశ్నను నమోదు చేయండి..."
    }

    fun searchButton(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "SEARCH PROTOCOL"
        AppLanguage.HINDI -> "प्रोटोकॉल खोजें"
        AppLanguage.TELUGU -> "ప్రోటోకాల్ శోధించండి"
    }

    fun quickSamplesLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Quick Sample Queries:"
        AppLanguage.HINDI -> "त्वरित नमूना प्रश्न:"
        AppLanguage.TELUGU -> "త్వరిత నమూనా ప్రశ్నలు:"
    }

    fun quickPrompts(lang: AppLanguage): List<String> = when (lang) {
        AppLanguage.ENGLISH -> listOf(
            "Vaginal bleeding pregnant",
            "Baby ribs sinking fast breathing",
            "How much ORS for diarrhoea",
            "What vaccines at birth",
            "Amoxicillin syrup dose 1 yr"
        )
        AppLanguage.HINDI -> listOf(
            "गर्भावस्था में रक्तस्राव (Vaginal bleeding)",
            "शिशु की पसलियां चलना तेज सांस",
            "दस्त में ओआरएस की सही मात्रा",
            "जन्म के समय कौन से टीके लगते हैं",
            "एंटीबायोटिक सिरप की खुराक"
        )
        AppLanguage.TELUGU -> listOf(
            "గర్భిణీకి రక్తస్రావం (Vaginal bleeding)",
            "శిశువు పక్కటెముకలు లోపలికి లాగడం",
            "విరేచనాలలో ORS ఎంత ఇవ్వాలి",
            "పుట్టిన వెంటనే వేయాల్సిన టీకాలు",
            "యాంటీబయాటిక్ మోతాదు ఎంత"
        )
    }

    fun welcomeTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Sahaaya Clinical Companion"
        AppLanguage.HINDI -> "सहाय क्लिनिकल साथी"
        AppLanguage.TELUGU -> "సహాయ క్లినికల్ సహచరి"
    }

    fun welcomeDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Ask clinical queries in English, Hinglish, or Telugu.\nStrict protocol citations • Hardcoded danger escalations • Zero hallucination"
        AppLanguage.HINDI -> "अंग्रेजी, हिंग्लिश या तेलुगु में क्लिनिकल प्रश्न पूछें।\nसटीक प्रोटोकॉल संदर्भ • स्वचालित खतरे का अलर्ट • शून्य भ्रांति"
        AppLanguage.TELUGU -> "ఇంగ్లీష్, హింగ్లీష్ లేదా తెలుగులో ప్రశ్నలు అడగండి.\nఖచ్చితమైన ప్రోటోకాల్ ఆధారాలు • అత్యవసర హెచ్చరికలు • పూర్తి ఆఫ్‌లైన్"
    }

    // Results
    fun emergencyTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "EMERGENCY: REFER NOW"
        AppLanguage.HINDI -> "आपातकाल: तुरंत रेफर करें"
        AppLanguage.TELUGU -> "అత్యవసరం: వెంటనే రిఫర్ చేయండి"
    }

    fun dangerDetected(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Danger Sign Detected:"
        AppLanguage.HINDI -> "खतरे का लक्षण पाया गया:"
        AppLanguage.TELUGU -> "ప్రమాద సంకేతం గుర్తించబడింది:"
    }

    fun categoryLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Category:"
        AppLanguage.HINDI -> "श्रेणी:"
        AppLanguage.TELUGU -> "వర్గం:"
    }

    fun immediateAction(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Immediate Action: Escort woman/child immediately to the nearest First Referral Unit (FRU) or Hospital. Do not wait or self-treat."
        AppLanguage.HINDI -> "तत्काल कार्रवाई: तुरंत नजदीकी अस्पताल या प्रथम रेफरल केंद्र (FRU) ले जाएं। प्रतीक्षा या स्वयं उपचार न करें।"
        AppLanguage.TELUGU -> "తక్షణ చర్య: వెంటనే సమీప ఆసుపత్రి లేదా FRU కి తరలించండి. ఏమాత్రం ఆలస్యం చేయవద్దు లేదా సొంత వైద్యం చేయవద్దు."
    }

    fun outOfScopeTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Out of ASHA / ANM Scope"
        AppLanguage.HINDI -> "आशा / एएनएम कार्यक्षेत्र से बाहर"
        AppLanguage.TELUGU -> "ఆశా / ANM పరిధికి వెలుపల"
    }

    fun outOfScopeDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "ASHAs and ANMs provide counseling, preventive care, and emergency first-response. Antibiotics, prescription drugs, and medical diagnosis require an MBBS Medical Officer."
        AppLanguage.HINDI -> "आशा व एएनएम स्वास्थ्य परामर्श, रोकथाम व आपातकालीन रेफरल के लिए हैं। दवाएं लिखना व निदान केवल एमबीबीएस डॉक्टर कर सकते हैं।"
        AppLanguage.TELUGU -> "ఆశా & ANM కార్యకర్తలు కేవలం ప్రాథమిక సంరక్షణ మరియు సలహా ఇవ్వడానికి మాత్రమే. మందుల మోతాదు లేదా నిర్ధారణ కోసం MBBS డాక్టర్‌ను సంప్రదించాలి."
    }

    fun verifiedAnswerHeader(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Verified Protocol Answer"
        AppLanguage.HINDI -> "सत्यापित प्रोटोकॉल उत्तर"
        AppLanguage.TELUGU -> "ధృవీకరించబడిన ప్రోటోకాల్ సమాధానం"
    }

    fun notFoundMessage(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Not found in standard protocols. Please consult your Medical Officer or Supervisor."
        AppLanguage.HINDI -> "मानक प्रोटोकॉल में नहीं मिला। कृपया अपने चिकित्सा अधिकारी या सुपरवाइजर से पूछें।"
        AppLanguage.TELUGU -> "ప్రమాణిక ప్రోటోకాల్‌లలో సమాచారం కనుగొనబడలేదు. దయచేసి వైద్యాధికారిని సంప్రదించండి."
    }

    // Visits Screen
    fun visitsHeader(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Today's Visit Schedule"
        AppLanguage.HINDI -> "आज की गृह भेंट सूची"
        AppLanguage.TELUGU -> "నేటి గృహ సందర్శనలు"
    }

    fun visitsSubheader(count: Int, total: Int, lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Showing $count of $total Households | 18 Oct 2026"
        AppLanguage.HINDI -> "कुल $total में से $count परिवार प्रदर्शित | 18 अक्टूबर 2026"
        AppLanguage.TELUGU -> "మొత్తం $total లో $count గృహాలు | 18 అక్టోబర్ 2026"
    }

    fun statDangerTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "High Alert"
        AppLanguage.HINDI -> "खतरा / रेफर"
        AppLanguage.TELUGU -> "తీవ్ర ప్రమాదం"
    }

    fun statDangerSub(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Immediate referral"
        AppLanguage.HINDI -> "तत्काल रेफरल आवश्यक"
        AppLanguage.TELUGU -> "తక్షణ రిఫరల్"
    }

    fun statOverdueTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Overdue"
        AppLanguage.HINDI -> "अतिदेय भेंट"
        AppLanguage.TELUGU -> "గడువు దాటినవి"
    }

    fun statOverdueSub(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Visits delayed"
        AppLanguage.HINDI -> "देरी से चल रही भेंट"
        AppLanguage.TELUGU -> "ఆలస్యమైనవి"
    }

    fun statRoutineTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Routine Due"
        AppLanguage.HINDI -> "नियमित नियत"
        AppLanguage.TELUGU -> "సాధారణ"
    }

    fun statRoutineSub(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Scheduled today"
        AppLanguage.HINDI -> "आज निर्धारित"
        AppLanguage.TELUGU -> "నేడు నిర్ణయించినవి"
    }

    fun filterAll(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "All"
        AppLanguage.HINDI -> "सभी"
        AppLanguage.TELUGU -> "అన్నీ"
    }

    fun filterDanger(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Danger Signs"
        AppLanguage.HINDI -> "खतरे के संकेत"
        AppLanguage.TELUGU -> "ప్రమాద సంకేతాలు"
    }

    fun filterOverdue(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Overdue"
        AppLanguage.HINDI -> "अतिदेय"
        AppLanguage.TELUGU -> "గడువు దాటినవి"
    }

    fun filterRoutine(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Routine"
        AppLanguage.HINDI -> "नियमित"
        AppLanguage.TELUGU -> "సాధారణ"
    }

    fun searchHouseholdPlaceholder(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Search by family name or household ID..."
        AppLanguage.HINDI -> "परिवार का नाम या आईडी खोजें..."
        AppLanguage.TELUGU -> "కుటుంబం పేరు లేదా ఐడీతో శోధించండి..."
    }

    fun memberPregnant(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Pregnant Mother"
        AppLanguage.HINDI -> "गर्भवती माता"
        AppLanguage.TELUGU -> "గర్భిణీ తల్లి"
    }

    fun memberNewborn(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Newborn Infant"
        AppLanguage.HINDI -> "नवजात शिशु"
        AppLanguage.TELUGU -> "నవజాత శిశువు"
    }

    fun memberChild(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Child under 5"
        AppLanguage.HINDI -> "5 वर्ष से कम आयु"
        AppLanguage.TELUGU -> "5 ఏళ్ల లోపు బిడ్డ"
    }

    fun dangerFlagAlert(sign: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "DANGER FLAG REPORTED: $sign"
        AppLanguage.HINDI -> "खतरे का संकेत दर्ज: $sign"
        AppLanguage.TELUGU -> "ప్రమాద సంకేతం నమోదైంది: $sign"
    }

    fun btnMarkDone(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Mark Visited"
        AppLanguage.HINDI -> "भेंट पूर्ण दर्ज करें"
        AppLanguage.TELUGU -> "సందర్శించినట్లు నమోదు"
    }

    fun btnDoneChecked(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Visited ✓"
        AppLanguage.HINDI -> "भेंट पूर्ण ✓"
        AppLanguage.TELUGU -> "సందర్శించారు ✓"
    }

    fun btnViewProtocol(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Protocol"
        AppLanguage.HINDI -> "प्रोटोकॉल"
        AppLanguage.TELUGU -> "ప్రోటోకాల్"
    }

    fun btnReferNow(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "ESCALATE / REFER"
        AppLanguage.HINDI -> "तुरंत रेफर करें"
        AppLanguage.TELUGU -> "వెంటనే రిఫర్ చేయండి"
    }

    fun noVisitsFound(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "No visits match the current filter or search."
        AppLanguage.HINDI -> "इस फ़िल्टर या खोज में कोई भेंट नहीं मिली।"
        AppLanguage.TELUGU -> "ఈ ఫిల్టర్‌లో ఎటువంటి సందర్శనలు కనిపించలేదు."
    }

    // Benchmark
    fun benchmarkTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Offline Performance Benchmark"
        AppLanguage.HINDI -> "ऑफ़लाइन प्रदर्शन बेंचमार्क"
        AppLanguage.TELUGU -> "ఆఫ్‌లైన్ పనితీరు బెంచ్‌మార్క్"
    }

    fun benchmarkClose(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Close"
        AppLanguage.HINDI -> "बंद करें"
        AppLanguage.TELUGU -> "మూసివేయండి"
    }

    fun benchmarkRunSuite(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Run Benchmark (30 Questions)"
        AppLanguage.HINDI -> "बेंचमार्क चलाएं (30 प्रश्न)"
        AppLanguage.TELUGU -> "బెంచ్‌మార్క్ పరీక్ష (30 ప్రశ్నలు)"
    }

    // Voice Input Feature
    fun voicePrompt(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Speak symptom in English, Hindi, or Telugu..."
        AppLanguage.HINDI -> "लक्षण बोलें (हिन्दी / English)..."
        AppLanguage.TELUGU -> "లక్షణాలు చెప్పండి..."
    }

    fun voiceButtonDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Voice Input"
        AppLanguage.HINDI -> "आवाज से बोलें"
        AppLanguage.TELUGU -> "వాయిస్ ఇన్‌పుట్"
    }

    // Emergency Referral Slip Feature
    fun generateReferralSlip(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "GENERATE REFERRAL SLIP"
        AppLanguage.HINDI -> "रेफरल पर्ची बनाएं"
        AppLanguage.TELUGU -> "రిఫెరల్ స్లిప్ రూపొందించండి"
    }

    fun referralSlipHeader(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "EMERGENCY REFERRAL SLIP"
        AppLanguage.HINDI -> "आपातकालीन रेफरल पर्ची"
        AppLanguage.TELUGU -> "అత్యవసర రిఫెరల్ స్లిప్"
    }

    fun referralSubcentre(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "ASHA Beat: Sub-Centre Health Post"
        AppLanguage.HINDI -> "आशा क्षेत्र: उप-स्वास्थ्य केंद्र"
        AppLanguage.TELUGU -> "ఆశా బీట్: ఉప-ఆరోగ్య కేంద్రం"
    }

    fun patientNameLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Patient / Beneficiary:"
        AppLanguage.HINDI -> "मरीज / लाभार्थी:"
        AppLanguage.TELUGU -> "రోగి / లబ్ధిదారు:"
    }

    fun patientWalkin(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Walk-in Emergency Beneficiary"
        AppLanguage.HINDI -> "आपातकालीन मरीज"
        AppLanguage.TELUGU -> "అత్యవసర లబ్ధిదారు"
    }

    fun shareSlipSms(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Share Slip / SMS to 108"
        AppLanguage.HINDI -> "SMS या 108 पर भेजें"
        AppLanguage.TELUGU -> "SMS లేదా 108 కి పంపండి"
    }

    fun copySlip(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Copy Details"
        AppLanguage.HINDI -> "कॉपी करें"
        AppLanguage.TELUGU -> "కాపీ చేయండి"
    }

    // Daily PHC Summary Report Feature
    fun endOfDayReportButton(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Daily Report"
        AppLanguage.HINDI -> "दैनिक रिपोर्ट"
        AppLanguage.TELUGU -> "నేటి నివేదిక"
    }

    fun dailySummaryHeader(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Daily Field Summary — PHC Sync"
        AppLanguage.HINDI -> "दैनिक क्षेत्र सारांश रिपोर्ट"
        AppLanguage.TELUGU -> "నేటి ఫీల్డ్ సారాంశ నివేదిక"
    }

    fun visitedTodayLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Completed Visits:"
        AppLanguage.HINDI -> "पूर्ण भेंट:"
        AppLanguage.TELUGU -> "పూర్తయిన సందర్శనలు:"
    }

    fun dangerCasesLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Flagged Danger Cases:"
        AppLanguage.HINDI -> "गंभीर खतरे के मामले:"
        AppLanguage.TELUGU -> "గుర్తించిన ప్రమాద కేసులు:"
    }

    fun overduePendingLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Pending Overdue Follow-ups:"
        AppLanguage.HINDI -> "लंबित छूटी हुई भेंट:"
        AppLanguage.TELUGU -> "బాకీ ఉన్న సందర్శనలు:"
    }

    fun sendToAnm(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Send to ANM / Doctor (SMS)"
        AppLanguage.HINDI -> "एएनएम / डॉक्टर को SMS भेजें"
        AppLanguage.TELUGU -> "ANM / డాక్టర్‌కు SMS పంపండి"
    }
}

