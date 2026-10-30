import json

# Load chunks to verify all chunk_ids exist
with open("assets/protocol_chunks.json", "r", encoding="utf-8") as f:
    chunks = json.load(f)
chunk_ids = {c["id"] for c in chunks}

danger_rules = [
    # --- PREGNANCY DANGER SIGNS (linked to MCP-DS-01) ---
    {
        "id": "RULE-PREG-BLEED",
        "chunk_id": "MCP-DS-01",
        "category": "pregnancy",
        "danger_sign": "Vaginal bleeding during pregnancy",
        "action": "REFER NOW",
        "source_citation": "Induction Training Module for ASHAs, p.69",
        "note": "phrasings need native-speaker review",
        "phrasings": {
            "en": [
                "vaginal bleeding", "bleeding during pregnancy", "blood coming from vagina",
                "pregnant woman bleeding", "bleeding heavily in pregnancy", "spotting blood pregnant",
                "vaginal bliding", "blod discharge in pregnancy", "pregnant bleeding"
            ],
            "hinglish": [
                "pregnant aurat ko khoon aa raha hai", "garbhwati mahila ko bleeding ho rahi hai",
                "khoon nikal raha hai pregnancy me", "yoni se khoon aana", "pregnancy me blood aa raha",
                "garbhavastha me khun beh raha hai"
            ],
            "telugu_latin": [
                "garbhavathi ki raktham paduthondi", "pregnancy lo raktham vasthondi",
                "raktha sravam jaruguthondi", "kadupu tho unnapudu blood padadam",
                "raktham karuthondi garbham lo"
            ]
        }
    },
    {
        "id": "RULE-PREG-CONV",
        "chunk_id": "MCP-DS-01",
        "category": "pregnancy",
        "danger_sign": "Convulsions or fits in pregnancy (eclampsia)",
        "action": "REFER NOW",
        "source_citation": "Induction Training Module for ASHAs, p.69",
        "note": "phrasings need native-speaker review",
        "phrasings": {
            "en": [
                "convulsions in pregnancy", "fits in pregnant woman", "seizures during pregnancy",
                "shaking violently pregnant", "pregnant mother having fits", "convulsion pregnant",
                "seizure while pregnant"
            ],
            "hinglish": [
                "garbhwati ko daura pad gaya", "pregnancy me jhatke aa rahe hai",
                "mirgi jaisa daura pregnancy", "aurat behosh hoke kaamp rahi hai",
                "pregnant mahila ko fits aa rahe hai"
            ],
            "telugu_latin": [
                "garbhavathi ki murcha vachindi", "pregnancy lo fits vachayi",
                "kadupulo unnapudu jhallu vachindi", "garbham lo unnapudu fits raavadam",
                "murcha rogam pregnant"
            ]
        }
    },
    {
        "id": "RULE-PREG-HEADACHE-VISION",
        "chunk_id": "MCP-DS-01",
        "category": "pregnancy",
        "danger_sign": "Severe headache with blurred vision or dizziness",
        "action": "REFER NOW",
        "source_citation": "Induction Training Module for ASHAs, p.69",
        "note": "phrasings need native-speaker review",
        "phrasings": {
            "en": [
                "severe headache with blurred vision", "pregnant woman dizzy with severe headache",
                "cannot see clearly and high bp", "blurred vision in pregnancy", "severe headache pregnancy",
                "headache blurred eyes pregnant", "high blood pressure with headache pregnant"
            ],
            "hinglish": [
                "tez sar dard aur dhundhla dikhai dena", "pregnancy me sar ghoom raha aur andhera chha raha",
                "garbhwati ka bp bahut high hai aur sar dard", "aankhon ke samne dhundhla pan sar dard",
                "sar me bahut tez dard aankh me dhundhlapan"
            ],
            "telugu_latin": [
                "theevramaina thala noppi mariyu kanti choopu sarigga ledu", "pregnancy lo thala thiruguthondi choopu mandaginchindi",
                "kallu thiruguthunnayi thala noppi", "garbhavathi ki kanti choopu thakkuva thalanoppi"
            ]
        }
    },
    {
        "id": "RULE-PREG-NO-MOVE",
        "chunk_id": "MCP-DS-01",
        "category": "pregnancy",
        "danger_sign": "Decreased or absent fetal movements",
        "action": "REFER NOW",
        "source_citation": "Induction Training Module for ASHAs, p.69",
        "note": "phrasings need native-speaker review",
        "phrasings": {
            "en": [
                "baby stopped moving", "no movement inside womb", "baby not kicking",
                "foetal movements stopped", "baby not moving in stomach", "no kicks from baby",
                "baby not moving today pregnant"
            ],
            "hinglish": [
                "pet me bacha hil nahi raha", "garbh me bacche ki movement band ho gayi",
                "baccha chal nahi raha pet me", "bachhe ki laat mehsoos nahi ho rahi",
                "baby kick nahi kar raha"
            ],
            "telugu_latin": [
                "kadupulo bidda kadaladam ledu", "baby katha kadaladam aagipoindi",
                "kadupulo bidda thantaledu", "bidda kadalika ledu", "kadalika aagipoindi garbham lo"
            ]
        }
    },
    {
        "id": "RULE-PREG-SWELL",
        "chunk_id": "MCP-DS-01",
        "category": "pregnancy",
        "danger_sign": "Severe swelling of face and hands",
        "action": "REFER NOW",
        "source_citation": "Induction Training Module for ASHAs, p.69",
        "note": "phrasings need native-speaker review",
        "phrasings": {
            "en": [
                "swelling of face and hands", "swollen face and fingers pregnant", "facial puffiness in pregnancy",
                "hands and face swollen suddenly", "severe oedema face pregnancy"
            ],
            "hinglish": [
                "chehre aur haatho par bahut soojan hai", "muh phool gaya pregnancy me",
                "garbhwati ke chehre par sujan aagayi", "haath muh sooj gaye hain"
            ],
            "telugu_latin": [
                "mukham chethulu baaga vachipoyayi", "face swelling vachindi pregnancy lo",
                "mukham meeda vaapu vachindi", "chethulu mukham vaapulu"
            ]
        }
    },
    {
        "id": "RULE-PREG-MALPRESENT",
        "chunk_id": "MCP-DS-01",
        "category": "pregnancy",
        "danger_sign": "Mal-presentation or abnormal baby position",
        "action": "REFER NOW",
        "source_citation": "Induction Training Module for ASHAs, p.69",
        "note": "phrasings need native-speaker review",
        "phrasings": {
            "en": [
                "malpresentation", "baby is upside down", "abnormal position inside uterus",
                "transverse lie", "breech baby near delivery", "baby position wrong"
            ],
            "hinglish": [
                "bachha ulta hai pet me", "bacche ki position ulti hai",
                "bachha seedha nahi hai", "delivery se pehle baby tedha hai"
            ],
            "telugu_latin": [
                "bidda thalakinda ga undi", "bidda thirigi poyindi",
                "kadupulo bidda thappudu position lo undi", "bidda addamga undi"
            ]
        }
    },
    {
        "id": "RULE-PREG-FEVER",
        "chunk_id": "MCP-DS-01",
        "category": "pregnancy",
        "danger_sign": "High fever with chills in pregnant woman",
        "action": "REFER NOW",
        "source_citation": "Induction Training Module for ASHAs, p.69",
        "note": "phrasings need native-speaker review",
        "phrasings": {
            "en": [
                "high fever in pregnancy", "pregnant woman shivering with fever", "malaria in pregnant woman",
                "pregnant high temperature chills", "fever burning urine pregnant"
            ],
            "hinglish": [
                "garbhwati ko tez bukhar aur thand", "pregnancy me kapkapi ke sath bukhar",
                "pregnant mahila ko high fever hai", "peshab me jalan aur bukhar pregnancy"
            ],
            "telugu_latin": [
                "garbhavathi ki theevramaina jwaram", "pregnancy lo vanukutho jwaram",
                "chali jwaram vachindi pregnant sthreeki", "garbham lo unnapudu jwaram"
            ]
        }
    },

    # --- NEWBORN DANGER SIGNS (linked to NB-DS-01) ---
    {
        "id": "RULE-NB-POOR-SUCK",
        "chunk_id": "NB-DS-01",
        "category": "newborn",
        "danger_sign": "Poor sucking or unable to feed at breast",
        "action": "REFER NOW",
        "source_citation": "Induction Training Module for ASHAs, p.77",
        "note": "phrasings need native-speaker review",
        "phrasings": {
            "en": [
                "newborn not sucking breast", "baby unable to drink milk", "poor sucking in newborn",
                "infant refuses breast feed", "baby not latching or feeding", "new born cant feed",
                "baby cannot suckle"
            ],
            "hinglish": [
                "navjaat bacha doodh nahi pee raha", "baby dudh nahi chus raha",
                "baccha chhati se doodh nahi le raha", "baby feed nahi kar raha",
                "baccha sthanpaan nahi kar pa raha"
            ],
            "telugu_latin": [
                "puttina bidda paalu thaagadam ledu", "baby paalu cheekadam ledu",
                "bidda sthanyam thaagadaniki kashtapaduthondi", "paalu thaagatam maanesindi"
            ]
        }
    },
    {
        "id": "RULE-NB-CONV",
        "chunk_id": "NB-DS-01",
        "category": "newborn",
        "danger_sign": "Newborn convulsions or abnormal movements",
        "action": "REFER NOW",
        "source_citation": "Induction Training Module for ASHAs, p.77",
        "note": "phrasings need native-speaker review",
        "phrasings": {
            "en": [
                "newborn convulsions", "baby having fits", "abnormal movements in baby",
                "infant twitching and jerking", "baby seizures", "newborn fits"
            ],
            "hinglish": [
                "navjaat shishu ko daura pad gaya", "bacche ke haath paanv ainth rahe hain",
                "baby ko jhatke aa rahe hai", "baccha achanak akad raha hai"
            ],
            "telugu_latin": [
                "puttina biddaku murcha vachindi", "chinna biddaki fits vachayi",
                "baby chethulu kaallu theevranga vanukuthunnayi", "biddaku jhallu vachindi"
            ]
        }
    },
    {
        "id": "RULE-NB-FAST-BREATH",
        "chunk_id": "NB-DS-01",
        "category": "newborn",
        "danger_sign": "Newborn fast breathing or difficulty in breathing",
        "action": "REFER NOW",
        "source_citation": "Induction Training Module for ASHAs, p.77",
        "note": "phrasings need native-speaker review",
        "phrasings": {
            "en": [
                "newborn breathing very fast", "rapid breathing in infant", "baby gasping for breath",
                "fast breathing baby", "respiratory distress in newborn", "breathing difficulty baby"
            ],
            "hinglish": [
                "navjaat bacha tez saans le raha hai", "baby ki saans bahut tej chal rahi hai",
                "bacche ko saans lene me takleef ho rahi hai", "baccha haanph raha hai"
            ],
            "telugu_latin": [
                "bidda chala veganga swasa theesukuntundi", "biddaku aayasam ga undi",
                "swasa theesukovatam lo kashtam", "chinna biddaki aayasam"
            ]
        }
    },
    {
        "id": "RULE-NB-CHEST-INDRAW",
        "chunk_id": "NB-DS-01",
        "category": "newborn",
        "danger_sign": "Severe chest wall indrawing in newborn",
        "action": "REFER NOW",
        "source_citation": "Induction Training Module for ASHAs, p.77",
        "note": "phrasings need native-speaker review",
        "phrasings": {
            "en": [
                "chest wall indrawing", "baby ribs sinking in while breathing", "subcostal indrawing",
                "chest in-drawing baby", "ribs sucking in newborn"
            ],
            "hinglish": [
                "saans lete waqt chhati andar dhas rahi hai", "bacche ki pasliyan chal rahi hain",
                "chhati me gaddhe pad rahe hain", "pasli andar khinch rahi hai"
            ],
            "telugu_latin": [
                "swasa theesukuntunnappudu rommu lopalki pothondi", "praka emukalu lopalki pothunnayi",
                "chhati lopalki poyi swasa theesukuntundi"
            ]
        }
    },
    {
        "id": "RULE-NB-FEVER",
        "chunk_id": "NB-DS-01",
        "category": "newborn",
        "danger_sign": "Newborn fever above 99°F / feels hot to touch",
        "action": "REFER NOW",
        "source_citation": "Induction Training Module for ASHAs, p.52, 77",
        "note": "phrasings need native-speaker review",
        "phrasings": {
            "en": [
                "newborn fever", "baby temperature above 99", "infant hot to touch",
                "new born high temperature", "fever in 2 week old baby", "baby fever 100 degrees"
            ],
            "hinglish": [
                "navjaat bacche ko bukhar hai", "baby ka badan bahut garam hai",
                "chote bacche ka temperature 99 se jyada hai", "shishu ko garam lag raha hai"
            ],
            "telugu_latin": [
                "puttina biddaku jwaram vachindi", "baby ki ontlo vedi ekkuva undi",
                "chinna biddaku jwaram kaaluthondi", "biddaku temperature undi"
            ]
        }
    },
    {
        "id": "RULE-NB-COLD",
        "chunk_id": "NB-DS-01",
        "category": "newborn",
        "danger_sign": "Newborn hypothermia / baby feels cold to touch",
        "action": "REFER NOW",
        "source_citation": "Induction Training Module for ASHAs, p.77",
        "note": "phrasings need native-speaker review",
        "phrasings": {
            "en": [
                "baby feels cold to touch", "newborn hypothermia", "baby body cold like ice",
                "infant hands and feet freezing", "cold baby body"
            ],
            "hinglish": [
                "navjaat bacha thanda pad gaya", "bacche ka shareer thanda ho gaya hai",
                "baby thandi lag rahi hai chhoone par", "bachhe ke hath per thande hain"
            ],
            "telugu_latin": [
                "bidda sareeram challaga aipoindi", "baby chala challaga undi",
                "biddaku ontlo challaga undi kaallu chethulu", "challabadipoina bidda"
            ]
        }
    },
    {
        "id": "RULE-NB-PUS-UMBILICUS",
        "chunk_id": "NB-DS-01",
        "category": "newborn",
        "danger_sign": "Pus or redness on the umbilicus",
        "action": "REFER NOW",
        "source_citation": "Induction Training Module for ASHAs, p.77",
        "note": "phrasings need native-speaker review",
        "phrasings": {
            "en": [
                "pus on umbilicus", "baby belly button red and leaking pus", "umbilical cord infected",
                "pus coming from navel", "foul smelling umbilical cord baby"
            ],
            "hinglish": [
                "bacche ki naabhi se peep nikal rahi hai", "navjaat ki naal pak gayi hai",
                "naabhi laal hai aur pus nikal raha", "baby ki nabhi me infection ho gaya"
            ],
            "telugu_latin": [
                "boddu daggara cheemu karuthondi", "baby boddu kandipoindi cheemu vasthondi",
                "boddu lo infection vachindi", "chinna biddaku boddu lo cheemu"
            ]
        }
    },
    {
        "id": "RULE-NB-BOILS",
        "chunk_id": "NB-DS-01",
        "category": "newborn",
        "danger_sign": "Pus filled boils or severe skin pustules",
        "action": "REFER NOW",
        "source_citation": "Induction Training Module for ASHAs, p.77",
        "note": "phrasings need native-speaker review",
        "phrasings": {
            "en": [
                "pus filled boils baby", "blisters with pus on newborn", "skin pustules infant",
                "many boils on baby skin"
            ],
            "hinglish": [
                "bacche ke sharir par peep wale phode hain", "baby ke jism par phunsiyan nikal aayi",
                "sharir par dane aur pus hai"
            ],
            "telugu_latin": [
                "bidda meeda cheemu poyina gundlu", "ontimeeda cheemu tho koodina bobbalu",
                "chinna biddaki charmam meeda bobbalu"
            ]
        }
    },
    {
        "id": "RULE-NB-JAUNDICE",
        "chunk_id": "NB-DS-01",
        "category": "newborn",
        "danger_sign": "Pallor or yellow soles and palms (severe jaundice)",
        "action": "REFER NOW",
        "source_citation": "Induction Training Module for ASHAs, p.77",
        "note": "phrasings need native-speaker review",
        "phrasings": {
            "en": [
                "yellow palms and soles baby", "severe jaundice newborn", "baby yellow eyes and feet",
                "yellow color spread to soles", "newborn yellow skin"
            ],
            "hinglish": [
                "bacche ki hatheliyan aur talwe peele pad gaye", "navjaat ko peeliya ho gaya hai",
                "baby ka rang bohot peela dikh raha hai", "aankhe aur talwe peele hain"
            ],
            "telugu_latin": [
                "bidda chethulu kaalla arachethulu pasupuga marayi", "baby ki pasirikalu vachayi",
                "arakaallu arapedalu pasupu pachaga aipoyayi", "puttina biddaku yellow jaundice"
            ]
        }
    },
    {
        "id": "RULE-NB-CYANOSIS",
        "chunk_id": "NB-DS-01",
        "category": "newborn",
        "danger_sign": "Blue palms, soles, or lips (cyanosis)",
        "action": "REFER NOW",
        "source_citation": "Induction Training Module for ASHAs, p.77",
        "note": "phrasings need native-speaker review",
        "phrasings": {
            "en": [
                "blue palms and soles", "baby turning blue", "blue lips in newborn",
                "cyanosis infant", "baby lips and tongue blue"
            ],
            "hinglish": [
                "baccha neela pad raha hai", "hoth aur haath neele ho gaye baby ke",
                "navjaat ka rang neela ho gaya", "bacche ke talwe neele hain"
            ],
            "telugu_latin": [
                "bidda neelamga maruthondi", "baby pedavulu chethulu neelam aipoyayi",
                "chinna biddaku neela rangulo aipoindi"
            ]
        }
    },
    {
        "id": "RULE-NB-BLEED",
        "chunk_id": "NB-DS-01",
        "category": "newborn",
        "danger_sign": "Bleeding from any site in newborn",
        "action": "REFER NOW",
        "source_citation": "Induction Training Module for ASHAs, p.77",
        "note": "phrasings need native-speaker review",
        "phrasings": {
            "en": [
                "bleeding from umbilical cord", "blood coming from baby nose", "blood in newborn stool",
                "baby bleeding from skin", "infant bleeding"
            ],
            "hinglish": [
                "bacche ki naabhi se khoon aa raha hai", "baby ke potty me khoon hai",
                "shishu ke naak se khoon nikal raha hai", "bacche ko bleeding ho rahi hai"
            ],
            "telugu_latin": [
                "boddu nundi raktham karuthondi", "baby stool lo raktham paduthondi",
                "chinna biddaki raktham vasthondi"
            ]
        }
    },
    {
        "id": "RULE-NB-LETHARGY",
        "chunk_id": "NB-DS-01",
        "category": "newborn",
        "danger_sign": "Excessive drowsiness, lethargy, or incessant crying",
        "action": "REFER NOW",
        "source_citation": "Induction Training Module for ASHAs, p.77",
        "note": "phrasings need native-speaker review",
        "phrasings": {
            "en": [
                "baby excessively drowsy", "newborn cannot wake up", "infant lethargic limp",
                "baby crying incessantly without stopping", "unresponsive baby", "baby sluggish not waking"
            ],
            "hinglish": [
                "baccha jag hi nahi raha", "baby behosh jaisa pada hai",
                "baccha bina ruke lagatar roye ja raha hai", "baby bilkul sust aur dheela hai"
            ],
            "telugu_latin": [
                "bidda levaledu nidra pothune undi", "baby theevramga edusthune undi aagatledu",
                "bidda chala neerasamga spruha lekunda undi", "spruha leni bidda"
            ]
        }
    },
    {
        "id": "RULE-NB-DISTENSION",
        "chunk_id": "NB-DS-01",
        "category": "newborn",
        "danger_sign": "Abdominal distension or frequent projectile vomiting",
        "action": "REFER NOW",
        "source_citation": "Induction Training Module for ASHAs, p.77",
        "note": "phrasings need native-speaker review",
        "phrasings": {
            "en": [
                "abdominal distension in baby", "baby belly swollen hard", "frequent vomiting newborn",
                "baby vomiting green bile", "distended stomach infant"
            ],
            "hinglish": [
                "bacche ka pet phool gaya hai", "baby baar baar ulti kar raha hai",
                "pet tight aur bada ho gaya hai shishu ka", "har baar peene par ulti"
            ],
            "telugu_latin": [
                "bidda kadupu baaga vachindi", "baby tharachu vanthulu chesukuntundi",
                "kadupu ubbipoindi biddaku", "vanthulu aagatledu"
            ]
        }
    },
    {
        "id": "RULE-NB-NO-URINE",
        "chunk_id": "NB-DS-01",
        "category": "newborn",
        "danger_sign": "No urine passed in first 48 hours of life",
        "action": "REFER NOW",
        "source_citation": "Induction Training Module for ASHAs, p.77",
        "note": "phrasings need native-speaker review",
        "phrasings": {
            "en": [
                "baby has not passed urine in 48 hours", "no urine 2 days newborn",
                "infant not peeing since 2 days", "no wet diapers 48 hours", "baby not urinating"
            ],
            "hinglish": [
                "bacche ne 2 din se peshab nahi kiya", "48 ghante se shishu ne mutra nahi kiya",
                "baby ne do din se susu nahi kiya", "baccha peshab nahi kar raha"
            ],
            "telugu_latin": [
                "bidda rendu rojulu ga uchha poyaledu", "48 gantaluga baby urine poyaledu",
                "puttina bidda muthram poyaledu", "uchha raledu"
            ]
        }
    },

    # --- CHILD DANGER SIGNS (linked to STOP-DIA-01, FEV-ARI-01, ANAEMIA-01) ---
    {
        "id": "RULE-CHILD-BLOOD-STOOL",
        "chunk_id": "STOP-DIA-01",
        "category": "child",
        "danger_sign": "Diarrhoea with blood in stool (dysentery)",
        "action": "REFER NOW",
        "source_citation": "Guidance note under IDCF, p.2; Induction Module p.97",
        "note": "phrasings need native-speaker review",
        "phrasings": {
            "en": [
                "blood in stool child", "diarrhoea with blood", "dysentery in child",
                "bloody diarrhoea toddler", "red blood in motion child", "blood passing in diarrhoea"
            ],
            "hinglish": [
                "dast me khoon aa raha hai bacche ko", "potty me laal khoon nikal raha hai",
                "bacche ko khooni dast lag gaye", "child loose motion me blood"
            ],
            "telugu_latin": [
                "virochanalalo raktham paduthondi pillavallaki", "pilla stool lo blood vasthondi",
                "raktha virochanalu pillalaku", "virochanam lo raktham"
            ]
        }
    },
    {
        "id": "RULE-CHILD-SEVERE-DEHYDRATION",
        "chunk_id": "STOP-DIA-01",
        "category": "child",
        "danger_sign": "Severe dehydration, sunken eyes, unable to drink",
        "action": "REFER NOW",
        "source_citation": "Guidance note under IDCF, p.2",
        "note": "phrasings need native-speaker review",
        "phrasings": {
            "en": [
                "severe dehydration child", "child unable to drink and sunken eyes",
                "unconscious child diarrhoea", "skin pinch goes back very slowly diarrhoea",
                "child extremely limp loose motions"
            ],
            "hinglish": [
                "bacche ka sharir sookh gaya dast se", "aankhe andar dhas gayi aur peena band kar diya",
                "baccha paani bhi nahi pee pa raha ulti dast me", "severe dehydration behosh ho raha hai"
            ],
            "telugu_latin": [
                "theevramaina neeti kottha pilladiki", "kallu lopalki poyi paalu neellu thaagalekapothunnadu",
                "virochanala valla pilla spruha thappindi", "neellu thaagalekapothundi bidda"
            ]
        }
    },
    {
        "id": "RULE-CHILD-STRIDOR-ARI",
        "chunk_id": "FEV-ARI-01",
        "category": "child",
        "danger_sign": "Severe Acute Respiratory Infection with stridor or chest indrawing",
        "action": "REFER NOW",
        "source_citation": "Induction Training Module for ASHAs, p.97-98",
        "note": "phrasings need native-speaker review",
        "phrasings": {
            "en": [
                "stridor in calm child", "severe pneumonia symptoms child", "chest indrawing with fast breathing",
                "child cannot breathe making whistling sound", "severe chest wall indrawing toddler"
            ],
            "hinglish": [
                "saans lene me seeti ki aawaz aa rahi hai", "bacche ki chhati tez dhas rahi hai pneumonia",
                "chote bacche ko saans phool rahi hai aur gharkhar aawaz"
            ],
            "telugu_latin": [
                "swasa theesukuntunnappudu reppalu veyatam", "swasa theesukuntunte peepi sabdam vasthundi",
                "pillavandiki swasa lo theevramaina kashtam"
            ]
        }
    },
    {
        "id": "RULE-CHILD-HIGH-FEVER-RIGOR",
        "chunk_id": "FEV-ARI-01",
        "category": "child",
        "danger_sign": "Fever above 103°F (39.5°C) with stiff neck or drowsiness",
        "action": "REFER NOW",
        "source_citation": "Induction Training Module for ASHAs, p.52",
        "note": "phrasings need native-speaker review",
        "phrasings": {
            "en": [
                "fever above 103 child", "high fever with stiff neck", "fever more than 39.5 degrees",
                "drowsy child high temperature", "fever with chills and altered consciousness"
            ],
            "hinglish": [
                "bacche ko 103 se upar tez bukhar hai", "gardan akad gayi aur tez bukhar",
                "high fever 104 degree baccha behosh ho raha", "bukhar ke sath jhatke aur susti"
            ],
            "telugu_latin": [
                "pilladiki 103 degree la paina theevra jwaram", "meda gattiga aipoindi jwaram tho",
                "theevramaina jwaram valla spruha lekunda undi", "chala ekkuva jwaram"
            ]
        }
    },
    {
        "id": "RULE-CHILD-FEVER-2DAYS",
        "chunk_id": "FEV-ARI-01",
        "category": "child",
        "danger_sign": "Fever lasting more than 2 days in child",
        "action": "REFER NOW",
        "source_citation": "Induction Training Module for ASHAs, p.52",
        "note": "phrasings need native-speaker review",
        "phrasings": {
            "en": [
                "fever more than 2 days", "fever lasting 3 days child", "fever not going after 2 days",
                "child continuous fever 3 days", "persistent fever 48 hours"
            ],
            "hinglish": [
                "bacche ka bukhar do din se zyada ho gaya", "teen din se bukhar kam nahi ho raha",
                "fever 2 din se utar nahi raha", "chote bacche ko do din se lagatar bukhar"
            ],
            "telugu_latin": [
                "pilladiki rendu rojulaku minchi jwaram taggaledu", "moodu rojulu ga jwaram vasthundi",
                "jwaram rendu rojula nunchi taggatledu"
            ]
        }
    },
    {
        "id": "RULE-PREG-SEVERE-ANAEMIA",
        "chunk_id": "ANAEMIA-01",
        "category": "pregnancy",
        "danger_sign": "Severe anaemia with extreme pallor and breathlessness in pregnancy",
        "action": "REFER NOW",
        "source_citation": "Induction Training Module for ASHAs, p.69",
        "note": "phrasings need native-speaker review",
        "phrasings": {
            "en": [
                "severe anaemia in pregnancy", "extreme pallor breathless pregnant",
                "pregnant woman severely pale cannot walk", "hemoglobin very low breathless pregnant",
                "severe anemia pregnant woman"
            ],
            "hinglish": [
                "garbhwati ko khoon ki bohot kami hai saans fool rahi hai", "pregnant aurat ka chehra bilkul safed pad gaya hai",
                "severe anaemia pregnancy chalne me saans phoolna"
            ],
            "telugu_latin": [
                "garbhavathi ki theevramaina rakthaheenatha aayasam", "kadupulo unnappudu raktham chaala thakkuva aipoindi",
                "pallor rakthaheenatha garbham lo"
            ]
        }
    }
]

# Validation
print(f"Total danger rules created: {len(danger_rules)}")
for r in danger_rules:
    assert r["chunk_id"] in chunk_ids, f"Invalid chunk_id {r['chunk_id']} in rule {r['id']}"
    assert r["action"] == "REFER NOW"
    assert "phrasings" in r
    assert len(r["phrasings"]["en"]) >= 3
    assert len(r["phrasings"]["hinglish"]) >= 3
    assert len(r["phrasings"]["telugu_latin"]) >= 3

with open("assets/danger_rules.json", "w", encoding="utf-8") as f:
    json.dump(danger_rules, f, indent=2, ensure_ascii=False)
print("Saved assets/danger_rules.json with 27 rules verified against protocol chunks.")
