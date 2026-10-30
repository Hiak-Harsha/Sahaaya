import json
from pathlib import Path

p = Path("android-app/app/src/main/assets/danger_rules.json")
rules = json.load(open(p, encoding="utf-8"))
short_phrases = []
for r in rules:
    for lang, phs in r.get("phrasings", {}).items():
        for phrase in phs:
            tokens = phrase.strip().split()
            if len(tokens) <= 2:
                short_phrases.append((r["id"], r["chunk_id"], phrase))

print(f"Total short phrases (<= 2 words): {len(short_phrases)}")
for rid, cid, ph in short_phrases[:30]:
    print(f"  {rid} ({cid}): '{ph}'")
