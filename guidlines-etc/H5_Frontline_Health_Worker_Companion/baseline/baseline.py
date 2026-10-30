#!/usr/bin/env python3
"""H5 baseline — simple, honest, fully offline, CPU only, no neural model.

What it does
  1. Questions: BM25 keyword retrieval over protocol sections (with a tiny Hinglish->English
     word map), a keyword danger-sign list for escalation, a keyword list for refusal.
     The answer is the top section's text (trimmed). One section is cited.
  2. Visit list: a SIMPLIFIED rule engine (ANC, vaccines, NCD follow-up, MUAC) — it ignores
     several rules in the VIS document on purpose (HBNC visits, Td, upper age limits,
     messy vaccine codes, high-risk priority), so there is room to improve.
  3. Footprint: measures its own latency and peak memory.

Usage (from the package root):
    python baseline/baseline.py --split dev  --out submission_dev
    python baseline/baseline.py --split test --out submission
Needs: Python >= 3.9, pandas.
"""

from __future__ import annotations

import argparse
import json
import math
import re
import time
from collections import Counter
from datetime import date, timedelta
from pathlib import Path

import pandas as pd

ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / "data"
TODAY = date(2026, 10, 26)

# ---- tiny Hinglish -> English word map (extend it!) -------------------------------------------
WORD_MAP = {
    "dast": "diarrhoea", "loose": "diarrhoea", "bukhar": "fever", "taap": "fever", "tika": "vaccine",
    "teeka": "vaccine", "tike": "vaccine", "khoon": "bleeding", "jhatke": "convulsions fits",
    "saans": "breathing", "pasli": "chest indrawing", "sujan": "swelling oedema", "ulti": "vomiting",
    "doodh": "breastfeeding", "naal": "cord umbilicus", "peela": "jaundice", "peeliya": "jaundice",
    "pregnant": "pregnancy", "garbhvati": "pregnancy", "prasav": "delivery labour", "namak": "salt",
    "sugar": "sugar diabetes", "goli": "tablet", "lal": "red ifa", "nehla": "bathe", "mahine": "months",
    "hafte": "weeks", "saal": "years", "keede": "deworming albendazole", "kami": "anaemia",
}
DANGER = ["bleeding", "khoon aa", "jhatke", "convuls", "fits", "behosh", "unconscious", "blurr", "dhundhla",
          "chehra suja", "pasli", "chest indrawing", "neela", "blue", "stiff neck", "gardan akad",
          "hil nahi", "movement", "thaili", "water bag", "sab ulti", "vomits everything", "doodh nahi",
          "suck", "sust", "lethargic", "seene", "chest pain", "ek taraf", "one side", "dast me khoon",
          "blood in", "badbu", "foul", "pad", "1.6 kg", "1.5 kg", "peep"]
REFUSE = ["dose", "kitni goli", "kitna dena", "syrup", "amoxicillin", "cefixime", "antibiotic", "insulin",
          "metformin", "amlodipine", "paracetamol", "salary", "incentive", "login", "app ", "ration",
          "ayushman", "certificate", "praman", "tb ", "dengue", "typhoid", "hiv", "thyroid", "cream",
          "copper-t", "mala-n", "recharge", "covid", "rabies", "abortion", "garbhpat"]


def tokenize(text: str) -> list[str]:
    toks = re.findall(r"[a-z0-9]+(?:\.[0-9]+)?", text.lower())
    out = []
    for t in toks:
        out.append(t)
        if t in WORD_MAP:
            out.extend(WORD_MAP[t].split())
    return out


def load_sections() -> dict[str, str]:
    sec_re = re.compile(r"^###\s+([A-Z]{2,4}-\d+\.\d+)\s")
    sections, cur = {}, None
    for path in sorted((DATA / "protocols").glob("*.md")):
        for line in path.read_text(encoding="utf-8").splitlines():
            m = sec_re.match(line)
            if m:
                cur = m.group(1)
                sections[cur] = line
            elif line.startswith("#"):
                cur = None
            elif cur:
                sections[cur] += "\n" + line
    return sections


class BM25:
    def __init__(self, docs: dict[str, str], k1=1.5, b=0.75):
        self.ids = list(docs)
        self.toks = [tokenize(docs[i]) for i in self.ids]
        self.avg = sum(map(len, self.toks)) / len(self.toks)
        self.df = Counter(t for d in self.toks for t in set(d))
        self.tf = [Counter(d) for d in self.toks]
        self.k1, self.b, self.n = k1, b, len(self.toks)

    def search(self, query: str, k=3):
        q = tokenize(query)
        scores = []
        for i, tf in enumerate(self.tf):
            s = 0.0
            dl = len(self.toks[i])
            for t in q:
                if t not in tf:
                    continue
                idf = math.log(1 + (self.n - self.df[t] + 0.5) / (self.df[t] + 0.5))
                s += idf * tf[t] * (self.k1 + 1) / (tf[t] + self.k1 * (1 - self.b + self.b * dl / self.avg))
            scores.append((s, self.ids[i]))
        scores.sort(reverse=True)
        return scores[:k]


def answer_questions(split: str):
    sections = load_sections()
    bm = BM25(sections)
    qs = pd.read_csv(DATA / f"questions_{split}.csv", dtype=str)
    out, lat = [], []
    for _, r in qs.iterrows():
        t0 = time.perf_counter()
        text = str(r["question_text"])
        low = text.lower()
        escalate = any(k in low for k in DANGER)            # hard-coded layer, runs first
        refused = (not escalate) and any(k in low for k in REFUSE)
        if refused:
            ans, cited = "This is outside my protocol book. Please ask the Medical Officer / ANM (SCP-1.2).", ["SCP-1.2"]
        else:
            hits = bm.search(text, k=1)
            sid = hits[0][1]
            body = re.sub(r"[#*|>]", " ", sections[sid])
            body = re.sub(r"\s+", " ", body).strip()
            prefix = "DANGER SIGN - REFER IMMEDIATELY (call 108/102). " if escalate else ""
            ans, cited = (prefix + body)[:400], [sid]
        ms = (time.perf_counter() - t0) * 1000
        lat.append(ms)
        out.append({"question_id": r["question_id"], "answer_text": ans, "cited_section_ids": cited,
                    "escalate": bool(escalate), "refused": bool(refused), "latency_ms": round(ms, 3)})
    return out, lat


# ---- simplified visit list -------------------------------------------------------------------
VACCINES = [  # code, due age (days), previous dose — simplified from IMM/VIS (no upper limits!)
    ("OPV-1", 42, None), ("OPV-2", 70, "OPV-1"), ("OPV-3", 98, "OPV-2"),
    ("Penta-1", 42, None), ("Penta-2", 70, "Penta-1"), ("Penta-3", 98, "Penta-2"),
    ("RVV-1", 42, None), ("RVV-2", 70, "RVV-1"), ("RVV-3", 98, "RVV-2"),
    ("PCV-1", 42, None), ("PCV-2", 98, "PCV-1"), ("MR-1", 270, None), ("MR-2", 480, "MR-1"),
]
ANC = {1: (0, 84), 2: (98, 182), 3: (196, 238), 4: (252, 280)}


def d(s):
    try:
        return date.fromisoformat(str(s)) if isinstance(s, str) and s else None
    except ValueError:
        return None


def status(due: date, late_after: date):
    if due > TODAY + timedelta(days=6):
        return None
    if due > TODAY:
        return 3
    return 2  # baseline does not distinguish high-risk members


def visit_list() -> pd.DataFrame:
    reg = pd.read_csv(DATA / "household_register.csv", dtype=str, keep_default_na=False)
    rows = []
    for _, r in reg.iterrows():
        dob = d(r["dob"])
        if r["pregnant"] == "Y" and d(r["lmp_date"]):
            lmp = d(r["lmp_date"])
            k = len([x for x in r["anc_visit_dates"].split(";") if x]) + 1
            if k in ANC:
                p = status(lmp + timedelta(days=ANC[k][0]), lmp + timedelta(days=ANC[k][1]))
                if p:
                    rows.append((r["member_id"], f"ANC-{k}", p))
        if dob and (TODAY - dob).days < 5 * 365:
            got = {x.split(":")[0] for x in r["child_vaccines"].split(";") if ":" in x}
            for code, age, prev in VACCINES:
                if code in got or (prev and prev not in got):
                    continue
                p = status(dob + timedelta(days=age), dob + timedelta(days=age + 28))
                if p:
                    rows.append((r["member_id"], code, p))
        if "HTN" in r["ncd_condition"] and d(r["last_bp_check_date"]):
            p = status(d(r["last_bp_check_date"]) + timedelta(days=30), TODAY)
            if p:
                rows.append((r["member_id"], "BP-followup", p))
        if "DM" in r["ncd_condition"] and d(r["last_sugar_check_date"]):
            p = status(d(r["last_sugar_check_date"]) + timedelta(days=30), TODAY)
            if p:
                rows.append((r["member_id"], "Sugar-followup", p))
        if d(r["last_muac_date"]):
            p = status(d(r["last_muac_date"]) + timedelta(days=30), TODAY)
            if p:
                rows.append((r["member_id"], "MUAC-check", p))
    return pd.DataFrame(rows, columns=["member_id", "due_item", "priority"]).drop_duplicates(["member_id", "due_item"])


def peak_ram_mb():
    try:
        import resource
        import sys
        rss = resource.getrusage(resource.RUSAGE_SELF).ru_maxrss
        return round(rss / (1024 * 1024) if sys.platform == "darwin" else rss / 1024, 1)
    except Exception:  # noqa: BLE001  (Windows has no `resource`)
        return None


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--split", choices=["dev", "test"], default="dev")
    ap.add_argument("--out", default="submission")
    a = ap.parse_args()
    out = Path(a.out)
    out.mkdir(parents=True, exist_ok=True)
    answers, lat = answer_questions(a.split)
    with open(out / "H5_answers.jsonl", "w", encoding="utf-8") as f:
        for rec in answers:
            f.write(json.dumps(rec, ensure_ascii=False) + "\n")
    vl = visit_list()
    vl.to_csv(out / "H5_visit_list.csv", index=False)
    lat.sort()
    fp = {"model_names": ["bm25-keyword-baseline (no neural model)"], "model_size_mb": 0.0,
          "peak_ram_mb": peak_ram_mb(), "median_latency_ms_cpu": round(lat[len(lat) // 2], 3), "offline": True}
    (out / "H5_footprint.json").write_text(json.dumps(fp, indent=2), encoding="utf-8")
    print(f"Wrote {len(answers)} answers, {len(vl)} visit-list rows and footprint to {out}/")


if __name__ == "__main__":
    main()
