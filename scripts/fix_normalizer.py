from pathlib import Path

p = Path("android-app/core/src/main/java/com/sahaaya/companion/core/nlp/TransliterationNormalizer.kt")
txt = p.read_text(encoding="utf-8")

old_protected = """    private val protectedWords = setOf(
        "baad", "pehle", "saath", "liye", "bina", "kabhi", "hamesha",
        "kitna", "kitni", "kitne", "kaise", "kaisa", "kaisi", "kyun", "kahan", "kab",
        "karna", "karein", "kare", "karta", "karti", "karte", "dena", "dijiye",
        "sakte", "sakti", "sakta", "hona", "honi", "chahiye", "hota", "hoti", "hote",
        "hotaa", "maana", "jaata", "jaati", "gaya", "gayi", "gaye", "aaya", "aayi",
        "raha", "rahi", "rahe", "wala", "wali", "wale", "waqt", "samay", "time",
        "haath", "pet", "dard", "sir", "sar", "aankh", "kaan", "gala"
    )"""

new_protected = """    private val protectedWords = setOf(
        "list", "line", "form", "card", "test", "camp", "ward", "room", "door", "paas",
        "dikh", "dikha", "dikhana", "milta", "milti", "milte", "mile", "khel", "khelta", "khelte",
        "raat", "subah", "shaam", "dopahar", "din", "ghar", "naam", "saal", "mahina", "mahine",
        "aana", "jaana", "aaya", "aayi", "aaye", "gaya", "gayi", "gaye", "bhejna", "bheju", "bheja",
        "poochna", "poocha", "batana", "batayein", "dekha", "dekh", "hua", "hui", "hue",
        "baad", "pehle", "saath", "liye", "bina", "kabhi", "hamesha",
        "kitna", "kitni", "kitne", "kaise", "kaisa", "kaisi", "kyun", "kahan", "kab",
        "karna", "karein", "kare", "karta", "karti", "karte", "dena", "dijiye",
        "sakte", "sakti", "sakta", "hona", "honi", "chahiye", "hota", "hoti", "hote",
        "hotaa", "maana", "jaata", "jaati", "raha", "rahi", "rahe", "wala", "wali", "wale",
        "waqt", "samay", "time", "haath", "pet", "dard", "sir", "sar", "aankh", "kaan", "gala"
    )"""

old_max = 'val maxAllowed = if (low.length <= 5) 2 else 2'
new_max = """val maxAllowed = when {
            low.length <= 4 -> 0
            low.length <= 6 -> 1
            else -> 2
        }
        if (maxAllowed == 0) return low"""

if old_protected in txt:
    txt = txt.replace(old_protected, new_protected)
elif old_protected.replace('\n', '\r\n') in txt:
    txt = txt.replace(old_protected.replace('\n', '\r\n'), new_protected.replace('\n', '\r\n'))

txt = txt.replace(old_max, new_max)

p.write_text(txt, encoding="utf-8")
print("TransliterationNormalizer.kt updated successfully!")
