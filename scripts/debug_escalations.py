import json
from pathlib import Path

# Let's inspect the Kotlin logic of evaluate() directly by simulating it on the 30 non-danger questions
questions = [
    ("Q-2RCTOZA1", "ward 4 me 8 sugar aur BP ke marij hai, din bhar ghar pe baithe rehte hai, unko exercise kitna karna chahiye?"),
    ("Q-2UTKUNV8", "didi, meri ek beneficiary 8 mahine ki pregnant hai, kal jaanch me uska Hb 8.6 aaya hai, koi takleef nahi bata rahi"),
    ("Q-4PW0EY31", "ORS pilate hi kabhi kabhi ulti kar deta hai, baaki doodh pee raha hai. kya karu?"),
    ("Q-6B77SBTI", "meri ek beneficiary ki delivery 10 din pehle hui, maa ghar pe theek hai, koi takleef nahi, delivery ke baad bhi IFA khilana hai kya, kitne din?"),
    ("Q-73FF3VFJ", "sir, 2 saal ke bachche ko kal se halka bukhar aa raha hai, khel raha hai, paracetamol syrup kitna dena hai?"),
    ("Q-79STG2U2", "meri beneficiary subah jaldi kaam pe jaati hai, nashta nahi kar paati, calcium goli khali pet de skte h subah subah?"),
    ("Q-7TVYWY7F", "Anganwadi didi ke saath MUAC naap rahe the, tape pe green kab maana jaata hai?"),
    ("Q-D6FG3B8A", "7 saal ka bachcha bahut dino se raat ko bistar geela kar raha hai, din me theek hai, koi dawai?"),
    ("Q-DF5E5WA0", "ANM didi phone nahi utha rahi, ek parivar abhi bahar se laut ke aaya hai, MR-1 chhoot gaya tha, unka bachcha 3 saal ka hai, ghar pe theek hai"),
    ("Q-EMSSWK4N", "ward 3 se bol rahi hu, ek bachche ko fIPV-1 lag chuka hai, sui wali jagah thoda dard tha par ab theek hai, fIPV ke doses ke beech minimum gap kitna rakhna hai?"),
    ("Q-F7QWXR8M", "Anganwadi me screening chal rahi hai, bachchon ke pair me sujan kaise check karte hai?"),
    ("Q-FKH9A3G5", "kam wajan wala bachcha hai 1.9 kg, doodh accha pee raha hai, active hai, nehlana hai"),
    ("Q-FS220JQC", "list me ek bachche ka Penta-2 pichhle mahine se baaki dikh raha hai, wo ghar pe nahi milta, tika kab overdue maana jaata hai?"),
    ("Q-FUF3628J", "meri beneficiary ke 3 ANC ho chuke hai, koi takleef nahi, chautha ANC checkup kitne hafte pe karate hai?"),
    ("Q-GCVR4XES", "namaste didi, PMSMA me kis kis ko bhejna hai, ek 2 mahine ki pregnant ghar pe hai, koi dikkat nahi, usko bhi bhej du?"),
    ("Q-GF3N65YI", "sir, ek parivar mazdoori ke liye bahar gaya tha, 13 mahine ka bachcha hai, ghar pe khel raha hai, ek bhi penta nahi laga"),
    ("Q-GMHPHMLE", "didi, ek chachi ka random sugar camp me zyada aaya tha, unko koi takleef nahi, diabetes confirm kaise hota hai, fasting sugar kitna?"),
    ("Q-GOLP4S9X", "kal se U-WIN app me login nahi ho raha, OTP nahi aa raha, 3 baar try kiya"),
    ("Q-GZH04MX7", "ek parivar delivery ke baad maayke chala gaya tha, BCG chhoot gaya tha, unka bachcha ab 8 mahine ka hai, ghar pe khel raha hai"),
    ("Q-H31QER8B", "ward 2 me ek bachche ko 9 mahine pe vitamin A ki pehli dose di thi, dusri dose ke baad aage kab kab deni hai?"),
    ("Q-HWS1GXOO", "ek baat poochni thi, meri beneficiary ko Td-1 abhi laga hai, haath pe thoda dard ho raha hai, Td ka doosra tika pregnancy me kitne din baad lagta hai?"),
    ("Q-IWFLKZSM", "padosi block me JE ke tike lag rahe hai, JE ka tika hamare block me lagta hai kya?"),
    ("Q-K63VVZJR", "ek baat poochni thi, ek aadmi ka HIV test positive aaya hai, ab uska kya ilaaj hoga?"),
    ("Q-M2RNJZXU", "kal 3 jachcha ke ghar visit hai, ek ki delivery ghar pe hui thi, maa se visit pe kya kya poochna hai?"),
    ("Q-MYGICHQY", "didi, ek aadmi ko 5 din se bukhar aa raha hai, typhoid ka widal test positive aaya hai, iska kya matlab?"),
    ("Q-QAKWJCQE", "meri saas 70 saal ki hai, ghar pe hi rehti hai, unhone 2 COVID tike liye the, ab booster abhi bhi lagta hai kya?"),
    ("Q-RRHPLGQ5", "kal operation se delivery hui hai, maa aur bachcha theek hai, bachcha doodh pee raha hai, HBNC visit kab se shuru karein?"),
    ("S-2H46P5R", "abhi visit pe hu, HBNC visit 42 din tak hi karte hai na, hospital wali delivery me kitni visit karni hai?"),
    ("Q-UUQY4HD6", "ward 3 se bol rahi hu, Anganwadi me ek 8 mahine ke bachche ko 2 din se khansi hai, saans gin rahi thi, kitni ho to tez maane?"),
    ("Q-V7IBSWHL", "madam ji, ek beneficiary ka ghar door hai, wahan gaadi nahi milti, JSSK me delivery ke baad ghar wapas chhodne ki suvidha milti hai kya?")
]

print("Simulating DangerRuleEngine on non-danger questions...")
for qid, q in questions:
    low = q.lower()
    # Check what triggers:
    # 1. Q-UUQY4HD6: "saans gin rahi thi"
    # In DangerRuleEngine line 224:
    # if (low.contains("thanda lag raha") ... ((low.contains("bachcha") || low.contains("baby") || low.contains("newborn")) && (low.contains("thanda") || low.contains("cold")))
    # In line 216: if (low.contains("bachcha") && (low.contains("fast breath"))) ...
    # Let's check each rule!
    reasons = []
    if "bachcha" in low and ("saans" in low or "breath" in low):
        reasons.append("bachcha + saans")
    if "pregnant" in low and "dard" in low:
        reasons.append("pregnant + dard")
    if "delivery" in low and "khoon" in low:
        reasons.append("delivery + khoon")
    if "sugar" in low and "behosh" in low:
        reasons.append("sugar + behosh")
    if "doodh" in low and ("nahi" in low or "unable" in low):
        reasons.append("doodh + nahi")
    if "ulti" in low:
        reasons.append("ulti")
    if "bukhar" in low:
        reasons.append("bukhar")
    if "sujan" in low:
        reasons.append("sujan")
    if "1.9 kg" in low or "kam wajan" in low:
        reasons.append("kam wajan")
    if reasons:
        print(f"  {qid}: {reasons} -> '{q[:60]}...'")
