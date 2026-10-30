from pathlib import Path

p = Path("android-app/core/src/main/java/com/sahaaya/companion/core/nlp/QueryUnderstandingEngine.kt")
txt = p.read_text(encoding="utf-8")
old_str = 'category = ClinicalCategory.NEWBORN,\n                    primaryProtocolSection = "ESC-3.1"'
new_str = 'category = ClinicalCategory.NEWBORN,\n                    primaryProtocolSection = "ESC-4.4"'

if old_str in txt:
    txt = txt.replace(old_str, new_str)
elif old_str.replace('\n', '\r\n') in txt:
    txt = txt.replace(old_str.replace('\n', '\r\n'), new_str.replace('\n', '\r\n'))

p.write_text(txt, encoding="utf-8")
print("Replaced VERY_LOW_BIRTH_WEIGHT in QueryUnderstandingEngine.kt successfully!")
