#!/usr/bin/env python3
"""Sahaaya Exact Clinical Visit List Engine.

Faithfully implements the operational rules of Chandrapuri Block from
guidlines-etc/H5_Frontline_Health_Worker_Companion/data/protocols/09_VIS_daily_visit_list_rules.md

Deterministic, zero-data-leakage, designed to generalize to any household register.
"""

from __future__ import annotations

import argparse
import re
from datetime import date, timedelta
from pathlib import Path
import pandas as pd

TODAY = date(2026, 10, 26)
UPCOMING_HORIZON_DAYS = 6
GRACE_DAYS = 28
FOLLOWUP_INTERVAL_DAYS = 30
MAX_VACCINE_AGE_DAYS = 2554
MAX_HBNC_AGE_DAYS = 42
MIN_MUAC_AGE_DAYS = 184
MAX_MUAC_AGE_DAYS = 1824

ANC_WINDOWS = {
    1: (0, 84),
    2: (98, 182),
    3: (196, 238),
    4: (252, 280),
}

# (item, due_from_age_days, not_after_age_days, prev_dose, min_gap_days)
VACCINE_RULES = [
    ("BCG", 0, 365, None, None),
    ("HepB-0", 0, 0, None, None),
    ("OPV-0", 0, 15, None, None),
    ("OPV-1", 42, 1825, None, None),
    ("OPV-2", 70, 1825, "OPV-1", 28),
    ("OPV-3", 98, 1825, "OPV-2", 28),
    ("Penta-1", 42, 365, None, None),
    ("Penta-2", 70, 365, "Penta-1", 28),
    ("Penta-3", 98, 365, "Penta-2", 28),
    ("RVV-1", 42, 365, None, None),
    ("RVV-2", 70, 365, "RVV-1", 28),
    ("RVV-3", 98, 365, "RVV-2", 28),
    ("fIPV-1", 42, 365, None, None),
    ("fIPV-2", 98, 365, "fIPV-1", 56),
    ("fIPV-3", 276, 729, "fIPV-2", 56),
    ("PCV-1", 42, 365, None, None),
    ("PCV-2", 98, 729, "PCV-1", 60),
    ("PCV-B", 276, 729, "PCV-2", 60),
    ("MR-1", 276, 1825, None, None),
    ("MR-2", 490, 1825, "MR-1", 28),
    ("OPV-B", 490, 1825, "OPV-3", 180),
    ("DPT-B1", 490, 2554, "Penta-3", 180),
    ("DPT-B2", 1827, 2554, "DPT-B1", None),
]

HBNC_SCHEDULES = {
    "institution": [3, 7, 14, 21, 28, 42],
    "home": [1, 3, 7, 14, 21, 28, 42],
}


def parse_date(val) -> date | None:
    if not val or pd.isna(val):
        return None
    s = str(val).strip()
    if not s:
        return None
    try:
        return date.fromisoformat(s)
    except ValueError:
        return None


def parse_bp(bp_str: str) -> tuple[int, int] | None:
    if not bp_str or pd.isna(bp_str):
        return None
    s = str(bp_str).strip()
    if s in ("", "0/0"):
        return None
    m = re.match(r"^(\d+)\s*/\s*(\d+)$", s)
    if not m:
        return None
    try:
        return int(m.group(1)), int(m.group(2))
    except ValueError:
        return None


def norm_item(item: str) -> str:
    return re.sub(r"[^a-z0-9]", "", str(item).lower())


def parse_doses(doses_str: str) -> dict[str, date]:
    """Parse semicolon separated doses like 'BCG:2026-09-27;OPV-0:2026-09-27'.
    
    If duplicates exist, keep the earliest date (VIS-1.4).
    """
    if not doses_str or pd.isna(doses_str):
        return {}
    res = {}
    for part in str(doses_str).split(";"):
        part = part.strip()
        if not part or ":" not in part:
            continue
        code, dstr = part.split(":", 1)
        k = norm_item(code)
        d = parse_date(dstr)
        if d:
            if k not in res or d < res[k]:
                res[k] = d
    return res


def is_high_risk_member(r: pd.Series, dob: date | None, age_days: int | None) -> bool:
    """Clinical High-Risk determination per VIS-5.1."""
    # 1. Pregnant woman
    if r.get("pregnant") == "Y":
        # last_hb_gdl < 7.0
        try:
            hb = float(r.get("last_hb_gdl"))
            if hb < 7.0:
                return True
        except (ValueError, TypeError):
            pass
        # last_anc_bp >= 140/90
        bp = parse_bp(r.get("last_anc_bp"))
        if bp and (bp[0] >= 140 or bp[1] >= 90):
            return True

    # 2. Newborn <= 42 days old
    if age_days is not None and 0 <= age_days <= MAX_HBNC_AGE_DAYS:
        return True

    # 3. Child 184–1824 days with MUAC < 11.5 cm or bilateral pitting oedema = Y
    if age_days is not None and MIN_MUAC_AGE_DAYS <= age_days <= MAX_MUAC_AGE_DAYS:
        if str(r.get("bilateral_pitting_oedema", "")).strip().upper() == "Y":
            return True
        try:
            muac = float(r.get("last_muac_cm"))
            if muac < 11.5:
                return True
        except (ValueError, TypeError):
            pass

    # 4. NCD patient with last recorded BP >= 140/90
    ncd = str(r.get("ncd_condition", "")).strip()
    if "HTN" in ncd or "DM" in ncd:
        bp = parse_bp(r.get("last_bp_reading"))
        if bp and (bp[0] >= 140 or bp[1] >= 90):
            return True

    return False


def determine_item_status(due_date: date, overdue_date: date | None = None) -> tuple[str, int] | None:
    """Computes status ('overdue', 'due_today', 'due_this_week') and default priority tier.
    
    overdue_date: date on or after which item is overdue. If None, uses due_date + GRACE_DAYS + 1.
    """
    if due_date > TODAY + timedelta(days=UPCOMING_HORIZON_DAYS):
        return None

    if due_date > TODAY:
        return "due_this_week", 3

    if overdue_date is None:
        # Grace period: overdue when today is MORE than 28 days after due date
        overdue_date = due_date + timedelta(days=GRACE_DAYS + 1)

    if TODAY >= overdue_date:
        return "overdue", 2

    return "due_today", 2


def process_member(r: pd.Series, today: date = TODAY) -> list[tuple[str, str, int, str, date]]:
    """Generates all due items for a single member record."""
    member_id = str(r["member_id"]).strip()
    dob = parse_date(r.get("dob"))
    age_days = (today - dob).days if dob else None
    high_risk = is_high_risk_member(r, dob, age_days)

    items = []

    def add_item(code: str, due_d: date, overdue_d: date | None = None):
        res = determine_item_status(due_d, overdue_d)
        if not res:
            return
        status, default_prio = res
        # High risk priority escalation
        if high_risk and status in ("overdue", "due_today"):
            priority = 1
        else:
            priority = default_prio
        items.append((member_id, code, priority, status, due_d))

    # =========================================================================
    # 1. PREGNANT WOMEN (VIS-2)
    # =========================================================================
    if r.get("pregnant") == "Y":
        # 1.1 High-Risk Pregnancy Follow-up (VIS-2.3)
        # Any pregnant woman with Hb < 7 or BP >= 140/90 was referred and gets HRP-followup daily as due_today (priority 1)
        hb_val = None
        try:
            hb_val = float(r.get("last_hb_gdl"))
        except (ValueError, TypeError):
            pass
        bp_anc = parse_bp(r.get("last_anc_bp"))
        is_hrp = (hb_val is not None and hb_val < 7.0) or (bp_anc is not None and (bp_anc[0] >= 140 or bp_anc[1] >= 90))
        if is_hrp:
            items.append((member_id, "HRP-followup", 1, "due_today", today))

        # 1.2 ANC Check-ups (VIS-2.1)
        lmp = parse_date(r.get("lmp_date"))
        if lmp:
            anc_dates_str = str(r.get("anc_visit_dates", "")).strip()
            done_anc = [p for p in anc_dates_str.split(";") if p.strip()]
            k = len(done_anc) + 1  # Next due ANC is checkups_done + 1
            if k in ANC_WINDOWS:
                opens_d = lmp + timedelta(days=ANC_WINDOWS[k][0])
                closes_d = lmp + timedelta(days=ANC_WINDOWS[k][1])
                overdue_d = closes_d + timedelta(days=1)
                
                # Check status
                if opens_d > today + timedelta(days=UPCOMING_HORIZON_DAYS):
                    pass
                elif opens_d > today:
                    items.append((member_id, f"ANC-{k}", 3, "due_this_week", opens_d))
                elif today <= closes_d:
                    prio = 1 if high_risk else 2
                    items.append((member_id, f"ANC-{k}", prio, "due_today", opens_d))
                else:
                    # today > closes_d -> overdue
                    prio = 1 if high_risk else 2
                    items.append((member_id, f"ANC-{k}", prio, "overdue", opens_d))

        # 1.3 Td in pregnancy (VIS-2.2)
        td_eligible = str(r.get("td_booster_eligible", "")).strip().upper() == "Y"
        td_given = parse_doses(r.get("td_doses"))
        anc_dates_str = str(r.get("anc_visit_dates", "")).strip()
        first_anc_dates = [parse_date(p) for p in anc_dates_str.split(";") if parse_date(p)]
        first_anc_date = sorted(first_anc_dates)[0] if first_anc_dates else None

        if td_eligible:
            # Needs only Td-BST
            if norm_item("Td-BST") not in td_given and norm_item("TdBST") not in td_given:
                if first_anc_date:
                    add_item("Td-BST", first_anc_date)
        else:
            # Needs Td-1 and Td-2
            has_td1 = norm_item("Td-1") in td_given or norm_item("Td1") in td_given
            has_td2 = norm_item("Td-2") in td_given or norm_item("Td2") in td_given

            if not has_td1:
                if first_anc_date:
                    add_item("Td-1", first_anc_date)
            elif not has_td2:
                # Td-2 due 28 days after Td-1
                td1_date = td_given.get(norm_item("Td-1")) or td_given.get(norm_item("Td1"))
                if td1_date:
                    td2_due = td1_date + timedelta(days=28)
                    add_item("Td-2", td2_due)

    # =========================================================================
    # 2. NEWBORN HOME VISITS (VIS-4)
    # =========================================================================
    if age_days is not None and 0 <= age_days <= MAX_HBNC_AGE_DAYS and dob:
        pod = str(r.get("place_of_delivery", "")).strip().lower()
        sched = HBNC_SCHEDULES.get(pod, HBNC_SCHEDULES["institution"])
        
        done_raw = str(r.get("hbnc_visits_done", "")).strip()
        done_days = set()
        for p in done_raw.split(";"):
            p = p.strip()
            if p.isdigit():
                done_days.add(int(p))

        # Rule VIS-4.1: List at most one HBNC item per baby:
        # 1. Take the latest scheduled visit dated on or before today. If not recorded in hbnc_visits_done -> list it
        # 2. Otherwise take next scheduled visit dated after today. If within next 6 days -> due_this_week
        past_due_visit = None
        for d in sched:
            v_date = dob + timedelta(days=d - 1)
            if v_date <= today:
                if d not in done_days:
                    past_due_visit = (d, v_date)
            else:
                break

        if past_due_visit:
            d, v_date = past_due_visit
            st = "due_today" if v_date == today else "overdue"
            prio = 1 if high_risk else 2
            items.append((member_id, f"HBNC-D{d}", prio, st, v_date))
        else:
            # Check next upcoming visit
            for d in sched:
                v_date = dob + timedelta(days=d - 1)
                if v_date > today:
                    if d not in done_days and v_date <= today + timedelta(days=UPCOMING_HORIZON_DAYS):
                        items.append((member_id, f"HBNC-D{d}", 3, "due_this_week", v_date))
                    break

    # =========================================================================
    # 3. CHILDREN: VACCINES (VIS-3)
    # =========================================================================
    if dob and age_days is not None and 0 <= age_days <= MAX_VACCINE_AGE_DAYS:
        child_vax = parse_doses(r.get("child_vaccines"))

        for code, due_age, not_after, prev_code, min_gap in VACCINE_RULES:
            k = norm_item(code)
            # 1. Has child already had it?
            if k in child_vax:
                continue

            # 2. Has previous dose needed been given?
            prev_date = None
            if prev_code:
                pk = norm_item(prev_code)
                if pk not in child_vax:
                    continue
                prev_date = child_vax[pk]

            # 3. Due date calculation: later of (dob + due_age) and (prev_date + min_gap)
            d1 = dob + timedelta(days=due_age)
            if prev_date and min_gap:
                d2 = prev_date + timedelta(days=min_gap)
                due_d = max(d1, d2)
            else:
                due_d = d1

            # 4. Upper age check
            # "Doses past the 'not after' age are not listed. A dose whose due date falls after not after age is not listed."
            not_after_date = dob + timedelta(days=not_after)
            if age_days > not_after or due_d > not_after_date:
                continue

            # 5. Overdue date: earlier of (due_d + 29) and (dob + not_after)
            overdue_d = min(due_d + timedelta(days=GRACE_DAYS + 1), not_after_date)
            add_item(code, due_d, overdue_d)

    # =========================================================================
    # 4. CHILDREN 6-59 MONTHS: MUAC CHECK (VIS-5.2)
    # =========================================================================
    if dob and age_days is not None and 178 <= age_days <= MAX_MUAC_AGE_DAYS:
        last_muac_d = parse_date(r.get("last_muac_date"))
        if last_muac_d:
            muac_due = last_muac_d + timedelta(days=FOLLOWUP_INTERVAL_DAYS)
        else:
            muac_due = dob + timedelta(days=MIN_MUAC_AGE_DAYS)
        add_item("MUAC-check", muac_due)

    # =========================================================================
    # 5. NCD PATIENTS: HTN & DM FOLLOW-UP (VIS-5.3)
    # =========================================================================
    ncd = str(r.get("ncd_condition", "")).strip()
    if "HTN" in ncd:
        last_bp_d = parse_date(r.get("last_bp_check_date"))
        if last_bp_d:
            bp_due = last_bp_d + timedelta(days=FOLLOWUP_INTERVAL_DAYS)
            add_item("BP-followup", bp_due)
        else:
            # "If no check date is recorded, the follow-up is overdue."
            prio = 1 if high_risk else 2
            items.append((member_id, "BP-followup", prio, "overdue", today))

    if "DM" in ncd:
        last_sugar_d = parse_date(r.get("last_sugar_check_date"))
        if last_sugar_d:
            sugar_due = last_sugar_d + timedelta(days=FOLLOWUP_INTERVAL_DAYS)
            add_item("Sugar-followup", sugar_due)
        else:
            prio = 1 if high_risk else 2
            items.append((member_id, "Sugar-followup", prio, "overdue", today))

    return items


def generate_visit_list(register_path: Path, today: date = TODAY) -> pd.DataFrame:
    df = pd.read_csv(register_path, dtype=str, keep_default_na=False)
    all_rows = []
    for _, row in df.iterrows():
        items = process_member(row, today)
        all_rows.extend(items)

    res_df = pd.DataFrame(all_rows, columns=["member_id", "due_item", "priority", "status", "due_date"])
    # Drop duplicates if any
    res_df = res_df.drop_duplicates(subset=["member_id", "due_item"])
    return res_df


def main():
    parser = argparse.ArgumentParser(description="Generate Sahaaya H5 Visit List.")
    parser.add_argument("--register", default=("data/household_register.csv" if Path("data/household_register.csv").exists() else "guidlines-etc/H5_Frontline_Health_Worker_Companion/data/household_register.csv"))
    parser.add_argument("--out", default="submission/H5_visit_list.csv")
    args = parser.parse_args()

    reg_path = Path(args.register)
    out_path = Path(args.out)
    out_path.parent.mkdir(parents=True, exist_ok=True)

    print(f"Generating Visit List from: {reg_path}")
    df = generate_visit_list(reg_path, TODAY)
    print(f"Total visit rows generated across full register: {len(df)}")

    # Save to submission CSV format (member_id, due_item, priority)
    submission_df = df[["member_id", "due_item", "priority"]]
    submission_df.to_csv(out_path, index=False, encoding="utf-8")
    print(f"Wrote submission file to: {out_path}")

    # Also save detailed version with status and due_date for diagnostics/UI
    detailed_path = out_path.parent / "H5_visit_list_detailed.csv"
    df.to_csv(detailed_path, index=False, encoding="utf-8")
    print(f"Wrote detailed inspection file to: {detailed_path}")


if __name__ == "__main__":
    main()
