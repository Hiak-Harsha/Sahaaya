#!/usr/bin/env python3
"""
Sahaaya Official Protocol Ingestion Script (Phase 2)
Ingests all 9 official Markdown protocols from the H5 Challenge Corpus,
extracts all 107 leaf sections, verifies data integrity, calculates hashes,
and generates canonical JSON assets for Sahaaya companion.
"""

import os
import sys
import glob
import re
import json
import hashlib
from pathlib import Path

sys.stdout.reconfigure(encoding='utf-8')

PROJECT_ROOT = Path(__file__).resolve().parents[1]
PROTOCOLS_DIR = Path("H5_package/H5_Frontline_Health_Worker_Companion/data/protocols")
if not PROTOCOLS_DIR.exists():
    PROTOCOLS_DIR = PROJECT_ROOT / "data" / "protocols"

OUTPUT_ASSETS = PROJECT_ROOT / "assets" / "protocol_chunks.json"
OUTPUT_APP_ASSETS = PROJECT_ROOT / "android-app" / "app" / "src" / "main" / "assets" / "protocol_chunks.json"
OUTPUT_DATA_PROTOCOLS = PROJECT_ROOT / "data" / "protocols"
OUTPUT_CATALOG_MD = PROJECT_ROOT / "docs" / "PROTOCOL_CATALOG.md"

DOCUMENT_SOURCES = {
    "01_ANC_antenatal_care.md": {
        "source_document": "Guidelines for Antenatal Care and Skilled Attendance at Birth by ANMs/LHVs/SNs (April 2010)",
        "source_url": "https://nhm.gov.in/images/pdf/programmes/maternal-health/guidelines/sba_guidelines_for_skilled_attendance_at_birth.pdf",
        "authority": "Ministry of Health and Family Welfare (MoHFW), Govt of India",
        "page_number": 12
    },
    "02_IMM_immunisation_schedule.md": {
        "source_document": "National Immunization Schedule (NIS) for Infants, Children and Pregnant Women",
        "source_url": "https://nhm.gov.in/New_Updates_2018/NHM_Components/Immunization/report/National_%20Immunization_Schedule.pdf",
        "authority": "National Health Mission (NHM), MoHFW",
        "page_number": 1
    },
    "03_ESC_danger_signs_refer_immediately.md": {
        "source_document": "MCP Card and Induction Training Module for ASHAs - Obstetric and Neonatal Emergencies",
        "source_url": "https://nhm.gov.in/New_Updates_2018/NHM_Components/Immunization/Guildelines_for_immunization/MCP_Card_English_version.pdf",
        "authority": "MoHFW & NHSRC",
        "page_number": 69
    },
    "04_HBNC_newborn_and_postnatal_care.md": {
        "source_document": "Revised Home Based Newborn Care Operational Guidelines (2014)",
        "source_url": "https://nhm.gov.in/images/pdf/programmes/child-health/guidelines/Revised_Home_Based_New_Born_Care_Operational_Guidelines_2014.pdf",
        "authority": "MoHFW Child Health Division",
        "page_number": 18
    },
    "05_NUT_malnutrition_screening.md": {
        "source_document": "IMNCI Chart Booklet for Medical Officers (2023) - Nutritional Screening",
        "source_url": "https://nhm.gov.in/images/pdf/programmes/child-health/guidelines/IMNCI_Chart_Booklet.pdf",
        "authority": "MoHFW & WHO/UNICEF",
        "page_number": 32
    },
    "06_NCD_hypertension_diabetes.md": {
        "source_document": "NP-NCD Operational Guidelines 2023-2030 (Population Based Screening)",
        "source_url": "https://www.mohfw.gov.in/sites/default/files/NPNCD_Guidelines.pdf",
        "authority": "MoHFW NCD Division",
        "page_number": 45
    },
    "07_CHD_diarrhoea_fever_breathing.md": {
        "source_document": "IMNCI Chart Booklet & IDCF Childhood Diarrhoea Guidelines (2023)",
        "source_url": "https://nhm.hp.gov.in/storage/app/media/uploaded-files/Guidance.pdf",
        "authority": "MoHFW Child Health Division",
        "page_number": 24
    },
    "08_SCP_scope_and_redirect.md": {
        "source_document": "MoHFW Frontline Health Worker Scope Guidelines & PC-PNDT Act",
        "source_url": "https://pndt.mohfw.gov.in/",
        "authority": "MoHFW Community Processes Division",
        "page_number": 5
    },
    "09_VIS_daily_visit_list_rules.md": {
        "source_document": "VIS Daily Visit List Operational Rules (Chandrapuri Block Specification 2026)",
        "source_url": "https://talentap.in/2026/MegaHackathon-Oct26/H5",
        "authority": "TalentAP 2026 Clinical Protocol Committee",
        "page_number": 1
    }
}

SECTION_METADATA = {
    # Key concept associations for clinical accuracy
    "ANC-1.1": {"keywords": ["early registration", "edd", "lmp", "gestation", "mcp card"], "synonyms": ["pahla checkup", "pehli jaanch", "date of delivery"]},
    "ANC-1.2": {"keywords": ["four anc", "minimum checkups", "anc timing", "trimester", "anc schedule"], "synonyms": ["chaar anc", "chautha anc", "kitne hafte"]},
    "ANC-1.3": {"keywords": ["pmsma", "9th of month", "pradhan mantri surakshit matritva", "second third trimester", "high risk pregnancy"], "synonyms": ["pmsma me kis kis ko bhejna", "9 taareekh"]},
    "ANC-2.2": {"keywords": ["ifa", "red ifa", "iron folic acid", "180 days postpartum", "anaemia prevention"], "synonyms": ["lal goli", "delivery ke baad ifa", "180 din"]},
    "ANC-2.3": {"keywords": ["calcium tablets", "500 mg", "empty stomach forbidden", "take with meals", "milk calcium"], "synonyms": ["calcium khali pet", "khana khane ke baad"]},
    "ANC-3.1": {"keywords": ["haemoglobin", "hb 8.6", "mild anaemia", "moderate anaemia 7.0-9.9", "severe anaemia <7"], "synonyms": ["khoon ki jaanch", "madhyam anaemia"]},
    "ANC-3.2": {"keywords": ["severe anaemia treatment", "hb below 7", "referral to fru", "blood transfusion"], "synonyms": ["bahut kam khoon", "turant refer"]},
    "ESC-2.1": {"keywords": ["vaginal bleeding pregnancy", "ante-partum haemorrhage", "bleeding placenta"], "synonyms": ["khoon behna", "pregnancy me bleeding", "raktasravam"]},
    "ESC-2.2": {"keywords": ["convulsions pregnancy", "eclampsia", "fits"], "synonyms": ["jhatke", "mirgi", "pregnancy fits"]},
    "ESC-2.3": {"keywords": ["pre-eclampsia", "swelling face hands", "severe headache", "blurred vision", "high bp"], "synonyms": ["chehra suja", "pair me sujan", "sir dard", "dhundhla dikhna"]},
    "ESC-2.4": {"keywords": ["severe vomiting pregnancy", "hyperemesis gravidarum", "unable to keep fluids"], "synonyms": ["din bhar ulti", "paani nahi tik raha"]},
    "ESC-2.5": {"keywords": ["severe anaemia pregnancy", "hb < 7", "severe pallor", "breathlessness walking"], "synonyms": ["bahut peeli", "saans phoolna", "kamzori"]},
    "ESC-3.1": {"keywords": ["newborn respiratory distress", "chest indrawing", "fast breathing newborn", "grunting"], "synonyms": ["pasli chalna", "saans tez", "chhati andar"]},
    "ESC-3.2": {"keywords": ["newborn hypothermia", "cold to touch", "feels cold", "temperature below 35.5", "hypothermia"], "synonyms": ["bachcha thanda lag raha", "chhune pe thanda", "garam kapde"]},
    "ESC-5.1": {"keywords": ["child unable to drink or breastfeed", "vomits everything", "convulsions child", "lethargic child"], "synonyms": ["doodh nahi pee pa raha", "sab ulti", "bachcha sust hai"]},
    "ESC-5.3": {"keywords": ["fever >= 7 days", "prolonged fever child", "fever with stiff neck", "fever with petechiae"], "synonyms": ["9 din se roz bukhar", "hafte bhar se bukhar", "gardan akadna"]},
    "ESC-6.3": {"keywords": ["unconscious diabetes patient", "hypoglycemia coma", "insulin shock", "give nothing by mouth"], "synonyms": ["sugar marij behosh", "insulin leta hai behosh"]},
    "NUT-1.2": {"keywords": ["muac tape", "green >= 12.5 cm", "yellow 11.5-12.4 cm mam", "red < 11.5 cm sam", "acute malnutrition"], "synonyms": ["tape pe green", "muac green", "malnutrition screening"]},
    "NUT-1.3": {"keywords": ["bilateral pitting oedema", "pressing both feet 3 seconds", "severe malnutrition"], "synonyms": ["pair me sujan check", "dono pair sujan"]},
    "NCD-2.1": {"keywords": ["hypertension bp >= 140/90", "diabetes fasting >= 126", "random blood sugar >= 200", "monthly check"], "synonyms": ["sugar camp", "diabetes confirm", "fasting sugar"]},
    "NCD-3.1": {"keywords": ["exercise", "30 minutes activity", "5 days a week", "150 minutes", "salt reduction"], "synonyms": ["exercise kitna karna", "sugar bp marij exercise"]},
    "CHD-1.2": {"keywords": ["ors preparation", "vomiting wait 10 minutes", "small sips", "continue breastfeeding"], "synonyms": ["ors ulti", "ors ke baad ulti", "10 minute wait"]},
    "CHD-2.1": {"keywords": ["fever assessment", "fast breathing counts", "2-11 months >= 50", "12-59 months >= 40"], "synonyms": ["saans ginna", "tez saans kitni", "8 mahine saans count"]},
    "SCP-1.2": {"keywords": ["doctor redirect", "out of scope", "medicine dosage not permitted", "antibiotics prohibited", "laboratory interpretation outside scope"], "synonyms": ["paracetamol syrup kitna dena", "dawai kitni deni", "prescription"]},
}


def parse_protocols():
    files = sorted(glob.glob(str(PROTOCOLS_DIR / "*.md")))
    if not files:
        raise FileNotFoundError(f"No protocol markdown files found in {PROTOCOLS_DIR}")

    sec_re = re.compile(r"^###\s+([A-Z]{2,4}-\d+\.\d+)\s*(.*)")
    parent_re = re.compile(r"^##\s+([A-Z]{2,4}-\d+)\s*(.*)")
    doc_re = re.compile(r"^#\s+(.*)")

    leaf_sections = []
    seen_ids = set()

    for file_path in files:
        file_name = os.path.basename(file_path)
        source_meta = DOCUMENT_SOURCES.get(file_name, {
            "source_document": file_name,
            "source_url": "",
            "authority": "MoHFW / NHM",
            "page_number": 1
        })

        content = Path(file_path).read_text(encoding="utf-8")
        lines = content.splitlines()

        cur_parent_id = ""
        cur_parent_title = ""
        cur_leaf_id = None
        cur_leaf_title = ""
        cur_text_lines = []

        for line in lines:
            # Check parent header (Level 2)
            pm = parent_re.match(line)
            if pm:
                # Flush previous leaf section if open
                if cur_leaf_id:
                    text_body = "\n".join(cur_text_lines).strip()
                    leaf_sections.append(build_chunk(
                        cur_leaf_id, cur_leaf_title, cur_parent_id, cur_parent_title,
                        file_name, source_meta, text_body
                    ))
                    seen_ids.add(cur_leaf_id)
                    cur_leaf_id = None
                    cur_text_lines = []
                cur_parent_id = pm.group(1).strip()
                cur_parent_title = pm.group(2).strip()
                continue

            # Check leaf header (Level 3)
            sm = sec_re.match(line)
            if sm:
                if cur_leaf_id:
                    text_body = "\n".join(cur_text_lines).strip()
                    leaf_sections.append(build_chunk(
                        cur_leaf_id, cur_leaf_title, cur_parent_id, cur_parent_title,
                        file_name, source_meta, text_body
                    ))
                    seen_ids.add(cur_leaf_id)
                cur_leaf_id = sm.group(1).strip()
                cur_leaf_title = sm.group(2).strip()
                cur_text_lines = [line]  # Keep header line in text
                continue

            # Other headers end the current leaf section (e.g. ## Sources or # Doc Title)
            if line.startswith("#"):
                if cur_leaf_id:
                    text_body = "\n".join(cur_text_lines).strip()
                    leaf_sections.append(build_chunk(
                        cur_leaf_id, cur_leaf_title, cur_parent_id, cur_parent_title,
                        file_name, source_meta, text_body
                    ))
                    seen_ids.add(cur_leaf_id)
                    cur_leaf_id = None
                    cur_text_lines = []
                continue

            if cur_leaf_id:
                cur_text_lines.append(line)

        # Flush last section if open
        if cur_leaf_id:
            text_body = "\n".join(cur_text_lines).strip()
            leaf_sections.append(build_chunk(
                cur_leaf_id, cur_leaf_title, cur_parent_id, cur_parent_title,
                file_name, source_meta, text_body
            ))
            seen_ids.add(cur_leaf_id)

    return leaf_sections


def build_chunk(leaf_id, leaf_title, parent_id, parent_title, doc_name, source_meta, full_text):
    hasher = hashlib.sha256()
    hasher.update(full_text.encode("utf-8"))
    content_hash = hasher.hexdigest()

    meta = SECTION_METADATA.get(leaf_id, {})
    keywords = meta.get("keywords", [])
    synonyms = meta.get("synonyms", [])

    return {
        "id": leaf_id,
        "title": leaf_title,
        "language": "en",
        "text": full_text,
        "source_document": source_meta["source_document"],
        "source_url": source_meta["source_url"],
        "page_number": source_meta["page_number"],
        "parent_id": parent_id,
        "parent_title": parent_title,
        "document_name": doc_name,
        "authority": source_meta["authority"],
        "keywords": keywords,
        "approved_synonyms": synonyms,
        "content_hash": content_hash,
        "protocol_version": "1.0",
        "source_status": "OFFICIAL_CHALLENGE_CORPUS"
    }


def validate_integrity(chunks):
    errors = []
    seen = set()

    if len(chunks) != 107:
        errors.append(f"Expected exactly 107 leaf sections, found {len(chunks)}")

    for idx, c in enumerate(chunks):
        cid = c["id"]
        if not re.match(r"^[A-Z]{2,4}-\d+\.\d+$", cid):
            errors.append(f"Invalid section ID format: {cid}")
        if cid in seen:
            errors.append(f"Duplicate section ID found: {cid}")
        seen.add(cid)

        if not c["title"]:
            errors.append(f"Missing title in section {cid}")
        if not c["text"] or len(c["text"].strip()) < 10:
            errors.append(f"Empty or insufficient text in section {cid}")
        if not c["parent_id"]:
            errors.append(f"Missing parent_id in section {cid}")
        if not c["source_document"]:
            errors.append(f"Missing source_document in section {cid}")
        if c["page_number"] <= 0:
            errors.append(f"Invalid page_number in section {cid}")

    if errors:
        print("DATA INTEGRITY FAILURES DETECTED:")
        for e in errors:
            print(" -", e)
        raise ValueError(f"Integrity check failed with {len(errors)} errors.")
    else:
        print(f"Data integrity verified: All {len(chunks)} leaf sections passed 100% of validation checks.")


def main():
    print("=" * 60)
    print("      SAHAAYA OFFICIAL PROTOCOL INGESTION PIPELINE")
    print("=" * 60)
    
    chunks = parse_protocols()
    print(f"Parsed {len(chunks)} leaf sections across all 9 protocol markdown files.")
    
    validate_integrity(chunks)

    # Calculate corpus hash
    corpus_hasher = hashlib.sha256()
    for c in chunks:
        corpus_hasher.update(c["content_hash"].encode("utf-8"))
    corpus_hash = corpus_hasher.hexdigest()
    print(f"Canonical Protocol Corpus SHA-256 Hash: {corpus_hash}")

    # Ensure output directories exist
    OUTPUT_ASSETS.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT_APP_ASSETS.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT_DATA_PROTOCOLS.mkdir(parents=True, exist_ok=True)
    OUTPUT_CATALOG_MD.parent.mkdir(parents=True, exist_ok=True)

    # Save to assets/protocol_chunks.json
    with open(OUTPUT_ASSETS, "w", encoding="utf-8") as f:
        json.dump(chunks, f, indent=2, ensure_ascii=False)
    print(f"Wrote canonical assets to {OUTPUT_ASSETS} ({OUTPUT_ASSETS.stat().st_size} bytes)")

    # Save to android-app/app/src/main/assets/protocol_chunks.json
    with open(OUTPUT_APP_ASSETS, "w", encoding="utf-8") as f:
        json.dump(chunks, f, indent=2, ensure_ascii=False)
    print(f"Wrote Android app assets to {OUTPUT_APP_ASSETS} ({OUTPUT_APP_ASSETS.stat().st_size} bytes)")

    # Copy markdown protocols to data/protocols
    for md_file in glob.glob(str(PROTOCOLS_DIR / "*.md")):
        dest = OUTPUT_DATA_PROTOCOLS / os.path.basename(md_file)
        dest.write_text(Path(md_file).read_text(encoding="utf-8"), encoding="utf-8")
    print(f"Preserved all 9 source Markdown documents in {OUTPUT_DATA_PROTOCOLS}")

    # Generate docs/PROTOCOL_CATALOG.md
    with open(OUTPUT_CATALOG_MD, "w", encoding="utf-8") as f:
        f.write("# Sahaaya Protocol Catalog (Official Corpus v1.0)\n\n")
        f.write(f"**Corpus Hash (SHA-256):** `{corpus_hash}`  \n")
        f.write("**Effective Date:** 2026-10-21  \n")
        f.write("**Source Authority:** Ministry of Health and Family Welfare (MoHFW) / National Health Mission (NHM), Govt of India  \n")
        f.write(f"**Total Numbered Leaf Sections:** {len(chunks)}  \n\n")
        f.write("| Section ID | Parent | Title | Document | Source Page | Hash |\n")
        f.write("|:---|:---|:---|:---|:---:|:---|\n")
        for c in chunks:
            f.write(f"| **{c['id']}** | {c['parent_id']} | {c['title']} | `{c['document_name']}` | p.{c['page_number']} | `{c['content_hash'][:10]}...` |\n")

    print(f"Generated protocol documentation in {OUTPUT_CATALOG_MD}")
    print("\nPhase 2 Ingestion Completed Successfully.")


if __name__ == "__main__":
    main()

