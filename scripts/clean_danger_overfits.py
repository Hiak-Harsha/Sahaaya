from pathlib import Path

p = Path("android-app/core/src/main/java/com/sahaaya/companion/core/DangerRuleEngine.kt")
txt = p.read_text(encoding="utf-8")

old_boundary = """        // Boundary cases (non-emergency thresholds)
        if (low.contains("1.9 kg")) return DangerMatchResult(matched = false)
        if (low.contains("kabhi kabhi ulti") && low.contains("doodh pee")) return DangerMatchResult(matched = false)
        if (low.contains("hb 8.6")) return DangerMatchResult(matched = false)"""

new_boundary = """        // 4. Haemoglobin numeric parsing: severe anaemia is Hb < 7.0 g/dL (ESC-2.5)
        val hbMatcher = Pattern.compile("(?:hb|haemoglobin|hemoglobin)\\\\s*(?:hai|is|tha|was|level)?\\\\s*(\\\\d+(?:\\\\.\\\\d+)?)").matcher(low)
        if (hbMatcher.find()) {
            val hbVal = hbMatcher.group(1)?.toDoubleOrNull() ?: 11.0
            if (hbVal < 7.0) {
                return createResult("ESC-2.5", "Severe anaemia (Hb $hbVal < 7.0 g/dL)", "Hb < 7.0", "CRITICAL: REFER IMMEDIATELY FOR SEVERE ANAEMIA (ESC-2.5)")
            }
        }"""

if old_boundary in txt:
    txt = txt.replace(old_boundary, new_boundary)
elif old_boundary.replace('\n', '\r\n') in txt:
    txt = txt.replace(old_boundary.replace('\n', '\r\n'), new_boundary.replace('\n', '\r\n'))
else:
    print("Warning: old_boundary not found!")

old_vomit_target = '// Unable to drink / breastfeed or unconscious (ESC-5.1)'
new_vomit_addition = """        // Child vomits everything (ESC-5.1)
        if (low.contains("vomit everything") || low.contains("vomiting everything") ||
            low.contains("sab ulti") || low.contains("har baar ulti") ||
            (low.contains("ulti") && (low.contains("kuch nahi ruk") || low.contains("paani bhi nahi")))
        ) {
            return createResult("ESC-5.1", "Child vomiting everything", "vomits everything", "CRITICAL DANGER SIGN: REFER IMMEDIATELY (Call 108) (ESC-5.1)")
        }

        // Unable to drink / breastfeed or unconscious (ESC-5.1)"""

txt = txt.replace(old_vomit_target, new_vomit_addition)

p.write_text(txt, encoding="utf-8")
print("Cleaned danger overfits in DangerRuleEngine.kt successfully!")
