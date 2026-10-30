import argparse
import csv
import datetime
import random

def generate_register(today_str=None, num_families=30, output_file="data/register.csv"):
    random.seed(42)
    
    if today_str:
        today = datetime.datetime.strptime(today_str, "%Y-%m-%d").date()
    else:
        today = datetime.date.today()
        
    print(f"Generating register relative to base date 'today' = {today} for {num_families} families...")
    
    first_names_female = ["Sunita", "Anitha", "Lakshmi", "Kavitha", "Pooja", "Meena", "Rekha", "Geetha", "Sita", "Sarita", "Parvathi", "Radha", "Bhavani", "Swapna", "Manjula", "Padma", "Rani", "Lavanya", "Sirisha", "Kalyani", "Deepa", "Shanti", "Vani", "Renuka", "Gayatri", "Sujatha", "Bharathi", "Roopa", "Aparna", "Usha"]
    first_names_child = ["Aarav", "Sai", "Vihaan", "Aditya", "Rohan", "Chintu", "Bunty", "Arjun", "Krishna", "Teja", "Rishi", "Ananya", "Diya", "Isha", "Kavya", "Saanvi", "Pari", "Sneha", "Tanvi", "Navya"]
    last_names = ["Devi", "Reddy", "Sharma", "Rao", "Naidu", "Kumari", "Verma", "Patel", "Goud", "Singh", "Yadav", "Babu", "Chowdary", "Bai", "Begum"]

    rows = []
    member_counter = 1
    
    # Pre-select family indices for danger flags
    danger_flag_specs = {
        3: "Vaginal bleeding reported in 3rd trimester",
        8: "Newborn poor sucking and baby feels cold to touch",
        15: "Diarrhoea with blood in stool and sunken eyes",
        22: "Severe headache with blurred vision and high BP",
        27: "Chest wall indrawing with fast breathing and fever"
    }

    for fam_idx in range(1, num_families + 1):
        fam_id = f"FAM-{fam_idx:03d}"
        surname = random.choice(last_names)
        
        # Balance family types
        if fam_idx in [8, 12, 19, 25]:
            fam_type = "newborn"
        elif fam_idx in [3, 7, 11, 17, 22, 28]:
            fam_type = "pregnant"
        elif fam_idx in [5, 14, 20]:
            fam_type = "pregnant_and_child"
        else:
            fam_type = "child"
        
        # 1. Pregnant woman
        if "pregnant" in fam_type:
            mem_id = f"MEM-{member_counter:03d}"
            member_counter += 1
            mother_name = f"{random.choice(first_names_female)} {surname}"
            
            edd_offset = random.randint(-5, 120)
            edd = today + datetime.timedelta(days=edd_offset)
            last_visit_offset = random.randint(5, 30)
            last_visit = today - datetime.timedelta(days=last_visit_offset)
            
            danger_note = "NONE"
            if fam_idx in danger_flag_specs and ("bleeding" in danger_flag_specs[fam_idx] or "headache" in danger_flag_specs[fam_idx]):
                danger_note = danger_flag_specs[fam_idx]
                due_date = today - datetime.timedelta(days=1)
                due_item = "Emergency ANC Danger Follow-up (REFER TO FRU)"
            else:
                due_offset = random.randint(-8, 14)
                due_date = today + datetime.timedelta(days=due_offset)
                due_item = "ANC Routine Checkup & IFA Distribution"
                
            rows.append({
                "family_id": fam_id,
                "member_id": mem_id,
                "name": mother_name,
                "member_type": "pregnant_woman",
                "target_date": edd.isoformat(),
                "last_visit_date": last_visit.isoformat(),
                "danger_flag": danger_note,
                "vaccines_given": "TT-1, TT-2",
                "next_due_item": due_item,
                "due_date": due_date.isoformat()
            })
            
        # 2. Newborn
        if fam_type == "newborn":
            mem_id = f"MEM-{member_counter:03d}"
            member_counter += 1
            mother_name = f"{random.choice(first_names_female)} {surname}"
            baby_name = f"Baby of {mother_name}"
            
            age_days = random.randint(2, 35)
            dob = today - datetime.timedelta(days=age_days)
            last_visit = today - datetime.timedelta(days=random.randint(1, 5))
            
            danger_note = "NONE"
            if fam_idx in danger_flag_specs and "Newborn" in danger_flag_specs[fam_idx]:
                danger_note = danger_flag_specs[fam_idx]
                due_date = today - datetime.timedelta(days=1)
                due_item = "URGENT: Sick Newborn Referral Follow-up (SNCU)"
            else:
                hbnc_days = [3, 7, 14, 21, 28, 42]
                next_day = 42
                for d in hbnc_days:
                    if d >= age_days:
                        next_day = d
                        break
                due_date = dob + datetime.timedelta(days=next_day)
                due_item = f"HBNC Day {next_day} Home Visit"
                
            rows.append({
                "family_id": fam_id,
                "member_id": mem_id,
                "name": baby_name,
                "member_type": "newborn",
                "target_date": dob.isoformat(),
                "last_visit_date": last_visit.isoformat(),
                "danger_flag": danger_note,
                "vaccines_given": "BCG, OPV-0, HepB-0",
                "next_due_item": due_item,
                "due_date": due_date.isoformat()
            })
            
        # 3. Child under 5
        if "child" in fam_type:
            mem_id = f"MEM-{member_counter:03d}"
            member_counter += 1
            child_name = f"{random.choice(first_names_child)} {surname}"
            
            age_months = random.randint(2, 48)
            dob = today - datetime.timedelta(days=int(age_months * 30.4))
            last_visit = today - datetime.timedelta(days=random.randint(5, 30))
            
            danger_note = "NONE"
            if fam_idx in danger_flag_specs and ("Diarrhoea" in danger_flag_specs[fam_idx] or "Chest wall" in danger_flag_specs[fam_idx]):
                danger_note = danger_flag_specs[fam_idx]
                due_date = today - datetime.timedelta(days=1)
                due_item = "URGENT: Pediatric Danger Follow-up (PHC)"
                vaccines_done = "BCG, OPV-1, Pentavalent-1"
            else:
                if age_months < 4:
                    due_item = "Immunisation: OPV-2, Pentavalent-2 (10 weeks)"
                    vaccines_done = "BCG, OPV-1, Pentavalent-1"
                    due_date = dob + datetime.timedelta(days=70)
                elif age_months < 8:
                    due_item = "Immunisation: OPV-3, Pentavalent-3 (14 weeks)"
                    vaccines_done = "BCG, OPV-1, OPV-2, Pentavalent-1, Pentavalent-2"
                    due_date = dob + datetime.timedelta(days=98)
                elif age_months < 15:
                    due_item = "Immunisation: Measles-1, Vitamin A-1 (9-12 months)"
                    vaccines_done = "BCG, OPV 1-3, Pentavalent 1-3"
                    due_date = dob + datetime.timedelta(days=270)
                elif age_months < 26:
                    due_item = "Immunisation: DPT Booster 1, OPV Booster (16-24 months)"
                    vaccines_done = "Primary Series Complete"
                    due_date = dob + datetime.timedelta(days=500)
                else:
                    due_item = "Growth Monitoring & Vitamin A Biannual Dose"
                    vaccines_done = "Primary Series + Booster 1 Complete"
                    due_offset = random.randint(-12, 14)
                    due_date = today + datetime.timedelta(days=due_offset)

            rows.append({
                "family_id": fam_id,
                "member_id": mem_id,
                "name": child_name,
                "member_type": "child_under_5",
                "target_date": dob.isoformat(),
                "last_visit_date": last_visit.isoformat(),
                "danger_flag": danger_note,
                "vaccines_given": vaccines_done,
                "next_due_item": due_item,
                "due_date": due_date.isoformat()
            })

    fieldnames = [
        "family_id", "member_id", "name", "member_type", "target_date",
        "last_visit_date", "danger_flag", "vaccines_given", "next_due_item", "due_date"
    ]
    
    with open(output_file, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)
        
    print(f"Generated {len(rows)} member records across {num_families} families into {output_file}.")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Generate synthetic household register for Sahaaya.")
    parser.add_argument("--today", type=str, default=None, help="Configurable 'today' in YYYY-MM-DD format (default: system date)")
    parser.add_argument("--output", type=str, default="data/register.csv", help="Path to output CSV")
    args = parser.parse_args()
    
    generate_register(today_str=args.today, output_file=args.output)
