#!/usr/bin/env python3
"""Generate protocol_index.json from the 9 official markdown protocol files.

For each leaf section (### ID Title):
- Extracts ID, title, parent section, doc name
- Extracts key numbers, thresholds, dosages, time windows
- Extracts clinical keywords and synonyms
- Generates clean clinical summary cards for concise retrieval answers
- Stores in:
  1. android-app/core/src/main/resources/protocol_index.json
  2. android-app/app/src/main/assets/protocol_chunks.json
"""

import json
import re
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
PROTO_DIR = REPO_ROOT / "data" / "protocols"
CORE_RES_DIR = REPO_ROOT / "android-app" / "core" / "src" / "main" / "resources"
APP_ASSETS_DIR = REPO_ROOT / "android-app" / "app" / "src" / "main" / "assets"

RELATED_MAP = {
    "ANC-3.1": ["ANC-3.2"],
    "ANC-2.5": ["IMM-4.1"],
    "IMM-4.1": ["ANC-2.5"],
    "ANC-2.2": ["HBNC-3.1"],
    "HBNC-3.1": ["ANC-2.2"],
    "CHD-1.2": ["CHD-1.1", "ESC-5.1"],
    "ESC-6.3": ["ESC-1.3"],
    "NUT-1.2": ["ESC-5.5"],
    "HBNC-2.2": ["HBNC-2.1"]
}

def clean_markdown(text):
    t = text
    # remove markdown tables
    t = re.sub(r"\|[^\n]+\|", " ", t)
    # remove markdown bold/headers
    t = re.sub(r"[#*|>]", " ", t)
    # collapse whitespace
    return re.sub(r"\s+", " ", t).strip()

def extract_numbers(text):
    nums = re.findall(r"\b\d+(?:\.\d+)?(?:\s*-\s*\d+(?:\.\d+)?)?\s*(?:days?|weeks?|months?|years?|mg|g|kg|cm|doses?|visits?|tablets?|hours?|bpm|breaths|min(?:utes?)?)\b", text, re.I)
    return list(set(n.strip() for n in nums))

def extract_salient_summary(text, title, cid):
    lines = [l.strip() for l in text.split("\n") if l.strip()]
    bullet_lines = [l for l in lines if l.startswith("-") or l.startswith("*")]
    clean_bullets = [re.sub(r"^[-*]\s*", "", b).replace("**", "") for b in bullet_lines]
    
    # Check if there are table rows
    table_rows = re.findall(r"\|\s*([^|\n]+?)\s*\|\s*([^|\n]+?)\s*\|\s*([^|\n]+?)\s*\|", text)
    valid_tables = [f"{r[0].strip()}: {r[1].strip()} - {r[2].strip()}" for r in table_rows if not r[0].startswith("-") and not r[0].lower().startswith("trimester") and not r[0].lower().startswith("colour")]

    candidates = clean_bullets + valid_tables
    if not candidates:
        narrative = [l for l in lines if not l.startswith("#")]
        if narrative:
            candidates = narrative

    if candidates:
        first = candidates[0]
        # Clean up
        clean_first = re.sub(r"\s+", " ", first).strip()
        if len(clean_first) > 300:
            clean_first = clean_first[:297] + "..."
        return f"{clean_first} ({cid})."
    return f"{title} guidance ({cid})."

def parse_protocol_files():
    sections = []
    for md_file in sorted(PROTO_DIR.glob("*.md")):
        doc_name = md_file.name
        content = md_file.read_text(encoding="utf-8")
        
        # Split by leaf sections: ### ID Title
        leaf_pattern = re.compile(r"^###\s+([A-Z]+-\d+\.\d+)\s+(.+)$", re.M)
        matches = list(leaf_pattern.finditer(content))
        
        for i, match in enumerate(matches):
            cid = match.group(1).strip()
            title = match.group(2).strip()
            start_pos = match.end()
            end_pos = matches[i + 1].start() if i + 1 < len(matches) else len(content)
            
            raw_text = content[start_pos:end_pos].strip()
            # Clean section text: include title
            full_text = f"### {cid} {title}\n{raw_text}"
            clean_txt = clean_markdown(raw_text)
            summary = extract_salient_summary(raw_text, title, cid)
            nums = extract_numbers(raw_text)
            
            parent_id = cid.split(".")[0]
            related = RELATED_MAP.get(cid, [])
            
            # Extract keywords
            words = re.findall(r"\b[a-zA-Z]{3,}\b", f"{title} {clean_txt}".lower())
            freq = {}
            for w in words:
                if w not in ["the", "and", "for", "with", "from", "that", "this", "are", "not"]:
                    freq[w] = freq.get(w, 0) + 1
            top_kws = [k for k, _ in sorted(freq.items(), key=lambda x: x[1], reverse=True)[:15]]
            
            sections.append({
                "id": cid,
                "title": title,
                "language": "en",
                "text": full_text,
                "clean_text": clean_txt,
                "summary": summary,
                "source_document": doc_name,
                "source_url": "",
                "page_number": 1,
                "parent_id": parent_id,
                "parent_title": parent_id,
                "document_name": doc_name,
                "authority": "MoHFW Government of India",
                "keywords": top_kws,
                "approved_synonyms": [],
                "content_hash": "",
                "protocol_version": "v2.0",
                "source_status": "OFFICIAL_TALENTAP_CANONICAL",
                "key_numbers": nums,
                "related_sections": related
            })
    return sections

def main():
    CORE_RES_DIR.mkdir(parents=True, exist_ok=True)
    APP_ASSETS_DIR.mkdir(parents=True, exist_ok=True)
    
    sections = parse_protocol_files()
    print(f"Parsed {len(sections)} leaf sections from {PROTO_DIR}")
    
    # 1. Write protocol_index.json
    index_path = CORE_RES_DIR / "protocol_index.json"
    with open(index_path, "w", encoding="utf-8") as f:
        json.dump(sections, f, ensure_ascii=False, indent=2)
    print(f"Wrote {index_path}")
    
    # 2. Update protocol_chunks.json
    chunks_path = APP_ASSETS_DIR / "protocol_chunks.json"
    with open(chunks_path, "w", encoding="utf-8") as f:
        json.dump(sections, f, ensure_ascii=False, indent=2)
    print(f"Updated {chunks_path}")
    
    # Also update protocols_canonical.json
    canon_path = APP_ASSETS_DIR / "protocols_canonical.json"
    with open(canon_path, "w", encoding="utf-8") as f:
        json.dump(sections, f, ensure_ascii=False, indent=2)
    print(f"Updated {canon_path}")

if __name__ == "__main__":
    main()
