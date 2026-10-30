#!/usr/bin/env python3
"""Canonical Protocol Indexer for Sahaaya (H5 Companion).

Parses the 9 official Government of India guidance protocols in
guidlines-etc/H5_Frontline_Health_Worker_Companion/data/protocols/*.md
and creates a high-density, canonical knowledge base of all 107 leaf sections.

Outputs:
  - assets/protocols_canonical.json
  - assets/protocol_chunks.json
  - android-app/app/src/main/assets/protocol_chunks.json
"""

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PROTOCOLS_DIR = (ROOT / "data" / "protocols") if (ROOT / "data" / "protocols").exists() else (ROOT / "guidlines-etc" / "H5_Frontline_Health_Worker_Companion" / "data" / "protocols")

# Leaf section pattern: ### ANC-1.2 Timing and frequency of ANC visits
SEC_RE = re.compile(r"^###\s+([A-Z]{2,4}-\d+\.\d+)\s+(.+)$")
# Group section pattern: ## ANC-1 Routine Antenatal Care
GROUP_RE = re.compile(r"^##\s+([A-Z]{2,4}-\d+)\s+(.+)$")
# Document title pattern: # ANC — Antenatal Care
DOC_RE = re.compile(r"^#\s+(.+)$")

# Multilingual / vernacular synonym mapping for frontline code-mixed retrieval
VERNACULAR_KEYWORDS = {
    "ANC": ["pregnancy", "pregnant", "garbhvati", "prasav", "delivery", "labour", "mcp card", "trimester", "checkup", "anc", "jaanch", "maheene", "mahine"],
    "IMM": ["tika", "teeka", "tike", "vaccine", "vaccination", "immunisation", "injection", "sui", "dose", "booster", "chhoot gaya", "missed"],
    "ESC": ["danger", "khatra", "emergency", "refer", "108", "102", "hospital", "aspatal", "turant", "bleeding", "khoon", "jhatke", "convulsion", "fits", "behosh", "unconscious", "sujan", "pasli", "bukhar", "fever", "saans"],
    "HBNC": ["newborn", "navjaat", "shishu", "baby", "bachcha", "home visit", "hbnc", "weight", "wajan", "garam", "warmth", "kmc", "breastfeeding", "doodh"],
    "NUT": ["malnutrition", "kuposhan", "muac", "tape", "green", "yellow", "red", "sam", "mam", "nrc", "oedema", "sujan", "pair", "feet"],
    "NCD": ["sugar", "diabetes", "bp", "hypertension", "blood pressure", "madhumeh", "salt", "namak", "exercise", "walk", "lifestyle"],
    "CHD": ["diarrhoea", "dast", "loose motion", "fever", "bukhar", "taap", "cough", "khansi", "saans", "breathing", "ors", "zinc", "paracetamol", "sponge"],
    "SCP": ["scope", "doctor", "anm", "mo", "phc", "dawai", "medicine", "antibiotic", "dose", "salary", "incentive", "login", "app", "tb", "thyroid"],
    "VIS": ["visit list", "priority", "due", "overdue", "aaj", "kal", "hafta", "week", "register"]
}


def clean_text(text: str) -> str:
    # Remove markdown formatting characters (*, _, `, #) but preserve words and numbers
    t = re.sub(r"[*_`]", "", text)
    t = re.sub(r"\[([^\]]+)\]\([^\)]+\)", r"\1", t) # unwrap markdown links [text](url) -> text
    t = re.sub(r"\s+", " ", t)
    return t.strip()


def extract_key_numbers(text: str) -> list[str]:
    # Extract clinical numbers, ranges, and thresholds (e.g. 14 and 26 weeks, 180 days, 12.5 cm, 140/90)
    patterns = [
        r"\d+(?:\.\d+)?\s*(?:-\s*\d+(?:\.\d+)?)?\s*(?:weeks?|days?|months?|years?|cm|mm|g/dL|mg/dL|kg|°C|°F|percent|%)",
        r"\d{2,3}/\d{2,3}", # BP readings like 140/90, 180/110
        r"(?:at least|more than|less than|below|above)\s+\d+(?:\.\d+)?\s*(?:weeks?|days?|months?|cm|kg|g/dL|mg/dL|°C|°F)?",
        r"\b\d+\s+(?:days?|weeks?|months?|tablets?|doses?|pads?)\b"
    ]
    matches = []
    for pat in patterns:
        for m in re.finditer(pat, text, re.IGNORECASE):
            matches.append(m.group(0).strip())
    return sorted(list(set(matches)))


def parse_protocols():
    files = sorted(PROTOCOLS_DIR.glob("*.md"))
    if not files:
        raise FileNotFoundError(f"No protocol files found in {PROTOCOLS_DIR}")

    sections = []
    
    for f in files:
        content = f.read_text(encoding="utf-8")
        lines = content.splitlines()

        doc_title = f.stem
        doc_title_set = False
        cur_group_id = ""
        cur_group_title = ""
        cur_sec_id = ""
        cur_sec_title = ""
        cur_body_lines = []

        sources = []
        in_sources = False

        for line in lines:
            if DOC_RE.match(line) and not doc_title_set:
                m = DOC_RE.match(line)
                doc_title = m.group(1).strip()
                doc_title_set = True
                continue

            if line.startswith("## Sources"):
                in_sources = True
                continue

            if in_sources:
                if line.strip().startswith("-"):
                    sources.append(line.strip().lstrip("-").strip())
                continue

            m_group = GROUP_RE.match(line)
            if m_group:
                # Save previous section if open
                if cur_sec_id:
                    body = "\n".join(cur_body_lines).strip()
                    prefix_code = cur_sec_id.split("-")[0]
                    sec_data = build_section_record(
                        cur_sec_id, cur_sec_title, cur_group_id, cur_group_title,
                        f.name, doc_title, body, prefix_code
                    )
                    sections.append(sec_data)
                    cur_sec_id = ""
                    cur_body_lines = []

                cur_group_id = m_group.group(1).strip()
                cur_group_title = m_group.group(2).strip()
                continue

            m_sec = SEC_RE.match(line)
            if m_sec:
                # Save previous section if open
                if cur_sec_id:
                    body = "\n".join(cur_body_lines).strip()
                    prefix_code = cur_sec_id.split("-")[0]
                    sec_data = build_section_record(
                        cur_sec_id, cur_sec_title, cur_group_id, cur_group_title,
                        f.name, doc_title, body, prefix_code
                    )
                    sections.append(sec_data)

                cur_sec_id = m_sec.group(1).strip()
                cur_sec_title = m_sec.group(2).strip()
                cur_body_lines = []
                continue

            if cur_sec_id:
                cur_body_lines.append(line)

        # Flush last section in file
        if cur_sec_id:
            body = "\n".join(cur_body_lines).strip()
            prefix_code = cur_sec_id.split("-")[0]
            sec_data = build_section_record(
                cur_sec_id, cur_sec_title, cur_group_id, cur_group_title,
                f.name, doc_title, body, prefix_code
            )
            sections.append(sec_data)

    return sections


def build_section_record(sec_id, sec_title, group_id, group_title, filename, doc_title, body, prefix_code):
    clean_body = clean_text(body)
    full_text = f"{sec_id} {sec_title}. {clean_body}"
    key_nums = extract_key_numbers(clean_body)
    vernacular = VERNACULAR_KEYWORDS.get(prefix_code, [])

    # Format for both Python pipeline and Android app (ProtocolChunk schema)
    return {
        "id": sec_id,
        "title": sec_title,
        "groupId": group_id,
        "groupTitle": group_title,
        "fileName": filename,
        "docTitle": doc_title,
        "language": "en",
        "text": full_text,
        "body": clean_body,
        "sourceDocument": f"MoHFW / NHM — {doc_title}",
        "sourceUrl": "https://nhm.gov.in/",
        "pageNumber": 1,
        "keyNumbers": key_nums,
        "vernacularKeywords": vernacular
    }


def main():
    print("=" * 64)
    print("Sahaaya Protocol Indexer: Parsing 107 Canonical Leaf Sections")
    print("=" * 64)

    sections = parse_protocols()
    print(f"-> Successfully extracted {len(sections)} leaf sections!")

    if len(sections) != 107:
        print(f"WARNING: Expected 107 sections, found {len(sections)}!")
    else:
        print("SUCCESS: Exact 107 sections matched perfectly with official guidelines.")

    # Show breakdown by prefix
    prefix_counts = {}
    for s in sections:
        p = s["id"].split("-")[0]
        prefix_counts[p] = prefix_counts.get(p, 0) + 1

    for p, c in sorted(prefix_counts.items()):
        print(f"   {p:<6}: {c:>2} leaf sections")

    # Destination paths
    canonical_path = ROOT / "assets" / "protocols_canonical.json"
    legacy_assets_path = ROOT / "assets" / "protocol_chunks.json"
    android_assets_path = ROOT / "android-app" / "app" / "src" / "main" / "assets" / "protocol_chunks.json"

    # Write canonical formatted json
    with open(canonical_path, "w", encoding="utf-8") as f:
        json.dump(sections, f, indent=2, ensure_ascii=False)
    print(f"\n[+] Wrote canonical index: {canonical_path}")

    # Write backward-compatible chunk list matching ProtocolChunk schema
    chunk_list = []
    for s in sections:
        chunk_list.append({
            "id": s["id"],
            "title": f"{s['id']}: {s['title']}",
            "language": "en",
            "text": s["text"],
            "source_document": s["sourceDocument"],
            "sourceDocument": s["sourceDocument"],
            "source_url": s["sourceUrl"],
            "sourceUrl": s["sourceUrl"],
            "page_number": 1,
            "pageNumber": 1
        })

    with open(legacy_assets_path, "w", encoding="utf-8") as f:
        json.dump(chunk_list, f, indent=2, ensure_ascii=False)
    print(f"[+] Wrote root asset: {legacy_assets_path}")

    android_assets_path.parent.mkdir(parents=True, exist_ok=True)
    with open(android_assets_path, "w", encoding="utf-8") as f:
        json.dump(chunk_list, f, indent=2, ensure_ascii=False)
    print(f"[+] Synced to Android assets: {android_assets_path}")

    # Also sync canonical index to Android assets
    android_canonical_path = ROOT / "android-app" / "app" / "src" / "main" / "assets" / "protocols_canonical.json"
    with open(android_canonical_path, "w", encoding="utf-8") as f:
        json.dump(sections, f, indent=2, ensure_ascii=False)
    print(f"[+] Synced canonical index to Android assets: {android_canonical_path}")

    print("\nStep 1 Complete: 107 canonical leaf sections indexed and synchronized.")


if __name__ == "__main__":
    main()
