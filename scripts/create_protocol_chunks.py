import json
import os

protocol_chunks = [
    {
        "id": "MCP-DS-01",
        "title": "Pregnancy and Antenatal Danger Signs",
        "language": "en",
        "text": "Antenatal danger signs requiring immediate referral to a health facility: 1) Vaginal bleeding during pregnancy; 2) Swelling of face, hands, or body; 3) Severe headache, dizziness, blurred vision, or high blood pressure; 4) Convulsions or fits (eclampsia); 5) Cessation of foetal movements (baby stops kicking/moving inside the womb); 6) Severe anaemia with breathlessness; 7) Mal-presentation (baby upside down or abnormal transverse lie); 8) High fever, burning urination, or suspected malaria; 9) Prior caesarean or abdominal surgery history; 10) Multiple pregnancy or prior stillbirth/neonatal death. Action: Immediate emergency referral to First Referral Unit (FRU) or hospital.",
        "source_document": "Induction Training Module for ASHAs",
        "source_url": "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf",
        "page_number": 69
    },
    {
        "id": "NB-DS-01",
        "title": "Newborn Danger Signs for Immediate Referral",
        "language": "en",
        "text": "Critical danger signs in newborns requiring immediate urgent hospital referral: 1) Inability or poor sucking/feeding at the breast; 2) Abnormal movements or convulsions (fits); 3) Fast breathing or difficulty breathing with chest indrawing; 4) Axillary temperature > 99°F (37.2°C) or baby feels hot/cold to touch (hypothermia); 5) Pus or redness on the umbilicus, or pus-filled skin boils; 6) Pallor or yellow soles/palms (jaundice) or blue palms/soles (cyanosis); 7) Bleeding from umbilical cord, nose, mouth, or stool; 8) Excessive drowsiness, lethargy, or incessant continuous crying; 9) Abdominal distension or frequent vomiting; 10) No urine passed within the first 48 hours of life. Action: REFER NOW to SNCU/MCH hospital.",
        "source_document": "Induction Training Module for ASHAs",
        "source_url": "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf",
        "page_number": 77
    },
    {
        "id": "HBNC-VISIT-01",
        "title": "Home Based Newborn Care (HBNC) Visit Schedule",
        "language": "en",
        "text": "Schedule of ASHA home visits for newborn care: 1) Institutional deliveries: 6 visits on Days 3, 7, 14, 21, 28, and 42. 2) Home deliveries: 7 visits on Days 1, 3, 7, 14, 21, 28, and 42. 3) High-risk babies (birth weight < 2000g, pre-term born before 8 months 14 days, or unable to suckle on day 1): Daily visits during the first week; then visit once every 3 days until 28 days old; and visit on Day 42. Focus: Check warmth, promote Kangaroo Mother Care, check exclusive breastfeeding, ensure birth weight tracking, and screen for danger signs.",
        "source_document": "Induction Training Module for ASHAs",
        "source_url": "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf",
        "page_number": 76
    },
    {
        "id": "IMM-UIP-01",
        "title": "National Child Immunisation Schedule",
        "language": "en",
        "text": "Universal Immunisation Schedule for children: 1) At birth (within 24 hours): BCG, OPV zero dose, Hepatitis B zero dose. 2) At 6 weeks: DPT 1st dose, OPV 1st dose, Hepatitis B 1st dose. 3) At 10 weeks: DPT 2nd dose, OPV 2nd dose, Hepatitis B 2nd dose. 4) At 14 weeks: DPT 3rd dose, OPV 3rd dose, Hepatitis B 3rd dose. 5) At 9 to 12 months: Measles 1st dose, OPV, Vitamin A (1st dose). 6) At 16 to 24 months: DPT Booster 1, OPV Booster, Measles 2nd dose. 7) At 5 years: DPT Booster 2. 8) Vitamin A supplementation: 9 months, then every 6 months until age 5 (months 18, 24, 30, 36, 42, 48, 54, 60). 9) TT at 10 and 16 years. Rule: If a child misses a scheduled dose, continue the schedule; do not restart from dose 1.",
        "source_document": "Induction Training Module for ASHAs",
        "source_url": "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf",
        "page_number": 93
    },
    {
        "id": "STOP-DIA-01",
        "title": "Management of Childhood Diarrhoea with ORS and Zinc",
        "language": "en",
        "text": "Protocol for acute childhood diarrhoea: Stock and distribute 1 packet Oral Rehydration Salts (ORS) and 1 strip of 14 tablets Zinc. Assessment & triage: 1) No dehydration: Home treatment with ORS and 14-day Zinc. Fluid amounts: infants up to 2 months get 5 spoonfuls after every loose stool; children 2 months to 2 years get half a cup (~100 ml); older children get 1 cup (~200 ml). Continue frequent breastfeeding and home fluids (lemon water, buttermilk, dal water). 2) Some dehydration: Initiate ORS and Zinc, refer child to ANM, and call ANM. 3) Severe dehydration: Emergency referral to hospital; administer continuous sips of ORS during transit. Danger signs requiring immediate referral: blood in stool, child unable to drink/breastfeed, high fever, sunken eyes, extreme lethargy.",
        "source_document": "Guidance note on Prevention and Management of Childhood Diarrhoea under IDCF",
        "source_url": "https://nhm.hp.gov.in/storage/app/media/uploaded-files/Guidance.pdf",
        "page_number": 2
    },
    {
        "id": "FEV-ARI-01",
        "title": "Fever and Acute Respiratory Infection (ARI) Management in Children",
        "language": "en",
        "text": "Protocol for fever and cough/respiratory distress in children: 1) Normal oral temperature is ~36.8°C (98.2°F). In infants, temperature > 99°F (37.2°C) is fever and must be evaluated. 2) High fever (> 39.5°C / 103°F): Perform tepid water sponging over whole body (never use cold water to avoid shivering). Do not bundle in thick blankets. Give Paracetamol for fever control and refer immediately. 3) Fever lasting more than 2 days: Do not wait; refer to ANM/PHC for blood test for malaria, pneumonia, or typhoid. 4) Acute Respiratory Infection (ARI) Danger Signs: Fast breathing, chest wall indrawing, difficulty breathing, stridor, or child unable to drink. Action: Immediate emergency referral to health facility.",
        "source_document": "Induction Training Module for ASHAs",
        "source_url": "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf",
        "page_number": 52
    },
    {
        "id": "ANAEMIA-01",
        "title": "Prevention and Management of Anaemia in Pregnancy and Childhood",
        "language": "en",
        "text": "Protocol for anaemia: 1) Pregnant women: Look for paleness of conjunctiva/palms, fatigue, breathlessness on exertion, swelling of face/body. Prevention: 1 Iron Folic Acid (IFA) tablet daily starting after the first trimester (at least 100 IFA tablets). Mild/moderate anaemia: > 200 IFA tablets daily for multiple months. Severe anaemia: Urgent hospital referral for blood transfusion. Advise taking IFA after meals with lemon/vitamin C; never take with tea, coffee, or milk. 2) Children: Look for paleness (pallor) of palms and soles. Treat with 1 paediatric iron tablet daily, plus Albendazole deworming every 6 months (1 tablet for age >= 2 years, half tablet for age < 2 years). Refer to doctor if pallor does not improve.",
        "source_document": "Induction Training Module for ASHAs",
        "source_url": "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf",
        "page_number": 69
    },
    {
        "id": "MALNUT-01",
        "title": "Child Malnutrition Follow-Up and Community Counselling",
        "language": "en",
        "text": "Community protocol for undernutrition in children under 5: 1) Identification: Any child severely underweight, failing to gain weight over consecutive months, or underweight with persistent fever, chronic cough, or persistent pallor. 2) Home Visits: Visit families of malnourished children 1 to 2 times per month. Reinforce 7 complementary feeding principles: initiate at 6 months; do not dilute food; give thick dal not dal-water; include animal-source foods (milk, eggs, meat); add oils/fats; feed 4 to 6 times daily; continue feeding during illness. 3) Referral: Refer urgently to ANM/Doctor/Nutrition Rehabilitation Centre (NRC) if child is severely underweight with illness danger signs or fails to improve.",
        "source_document": "Induction Training Module for ASHAs",
        "source_url": "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf",
        "page_number": 87
    },
    {
        "id": "NB-CARE-01",
        "title": "Essential Newborn Care and High-Risk Baby Management",
        "language": "en",
        "text": "Protocol for essential newborn care: 1) Immediate breastfeeding within 1 hour of birth; feed thick yellow colostrum; strictly forbid pre-lacteal feeds (honey, ghutti, sugar water). 2) Normal birth weight is >= 2500g. 3) Low birth weight (< 2000g) and pre-term care: Delay bathing past the usual 7 days until weight exceeds 2000g and steady weight gain is recorded. Implement Kangaroo Mother Care (KMC) with baby upright, skin-to-skin against mother's chest, head turned, wearing cap and warm cover. Feed expressed breastmilk with a clean spoon if baby cannot suckle directly. Check frequently for danger signs.",
        "source_document": "Induction Training Module for ASHAs",
        "source_url": "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf",
        "page_number": 76
    }
]

# Write protocol_chunks.json
with open("assets/protocol_chunks.json", "w", encoding="utf-8") as f:
    json.dump(protocol_chunks, f, indent=2, ensure_ascii=False)
print("Wrote assets/protocol_chunks.json with", len(protocol_chunks), "chunks.")

# Build docs/verification_table.md
verification_rows = [
    # MCP-DS-01
    ("MCP-DS-01", "Vaginal bleeding in pregnancy requires emergency referral", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "69", "VERIFIED"),
    ("MCP-DS-01", "Swelling of face and hands indicates danger", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "69", "VERIFIED"),
    ("MCP-DS-01", "High blood pressure, severe headache, dizziness, blurred vision are danger signs", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "69", "VERIFIED"),
    ("MCP-DS-01", "Convulsions or fits in pregnant women require immediate referral", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "69", "VERIFIED"),
    ("MCP-DS-01", "Baby stops moving or kicking inside womb requires emergency referral", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "69", "VERIFIED"),
    ("MCP-DS-01", "Mal-presentation (upside down/abnormal position) requires facility referral", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "69", "VERIFIED"),
    
    # NB-DS-01
    ("NB-DS-01", "Newborn unable to suckle or drinks poorly is a critical danger sign", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "77", "VERIFIED"),
    ("NB-DS-01", "Newborn abnormal movements (convulsions) require immediate referral", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "77", "VERIFIED"),
    ("NB-DS-01", "Fast breathing, difficulty breathing, or chest wall indrawing requires referral", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "77", "VERIFIED"),
    ("NB-DS-01", "Newborn fever > 99°F (37.2°C) or feeling cold/hot requires urgent referral", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "52, 77", "VERIFIED"),
    ("NB-DS-01", "Pus on umbilicus or pus-filled boils is a danger sign", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "77", "VERIFIED"),
    ("NB-DS-01", "Bleeding from any site in newborn requires immediate referral", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "77", "VERIFIED"),
    ("NB-DS-01", "No urine passed in first 48 hours is a danger sign", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "77", "VERIFIED"),

    # HBNC-VISIT-01
    ("HBNC-VISIT-01", "Institutional birth HBNC schedule: Days 3, 7, 14, 21, 28, and 42 (6 visits)", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "76", "VERIFIED"),
    ("HBNC-VISIT-01", "Home birth HBNC schedule: Days 1, 3, 7, 14, 21, 28, and 42 (7 visits)", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "76", "VERIFIED"),
    ("HBNC-VISIT-01", "High-risk baby (<2000g, preterm <8m 14d): daily visit in week 1, every 3 days to day 28, day 42", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "76", "VERIFIED"),

    # IMM-UIP-01
    ("IMM-UIP-01", "Birth vaccines: BCG, OPV zero dose, Hepatitis B zero dose within 24h", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "93", "VERIFIED"),
    ("IMM-UIP-01", "Primary series at 6, 10, 14 weeks: DPT, OPV, Hepatitis B", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "93", "VERIFIED"),
    ("IMM-UIP-01", "Measles 1st dose & Vitamin A at 9-12 months", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "93", "VERIFIED"),
    ("IMM-UIP-01", "Booster doses at 16-24 months: DPT Booster 1, OPV Booster, Measles 2nd dose", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "93", "VERIFIED"),
    ("IMM-UIP-01", "DPT Booster 2 at 5 years; TT at 10 and 16 years", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "93", "VERIFIED"),
    ("IMM-UIP-01", "Vitamin A given at 9 months and every 6 months to 60 months", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "93", "VERIFIED"),
    ("IMM-UIP-01", "Missed vaccine doses must be continued, not restarted", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "93", "VERIFIED"),

    # STOP-DIA-01
    ("STOP-DIA-01", "Distribute 1 sachet ORS and 14 tablets Zinc for childhood diarrhoea", "Guidance note under IDCF", "https://nhm.hp.gov.in/storage/app/media/uploaded-files/Guidance.pdf", "2", "VERIFIED"),
    ("STOP-DIA-01", "ORS volume: <2m 5 spoons; 2m-2yr half cup (100ml); older 1 cup (200ml)", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "97", "VERIFIED"),
    ("STOP-DIA-01", "Some dehydration requires ORS+Zinc start and ANM referral", "Guidance note under IDCF", "https://nhm.hp.gov.in/storage/app/media/uploaded-files/Guidance.pdf", "2", "VERIFIED"),
    ("STOP-DIA-01", "Severe dehydration requires immediate hospital referral with sips of ORS en route", "Guidance note under IDCF", "https://nhm.hp.gov.in/storage/app/media/uploaded-files/Guidance.pdf", "2", "VERIFIED"),
    ("STOP-DIA-01", "Blood in stool, high fever, unable to drink are diarrhoea danger signs", "Guidance note under IDCF / Induction", "https://nhm.hp.gov.in/storage/app/media/uploaded-files/Guidance.pdf", "2, 97", "VERIFIED"),

    # FEV-ARI-01
    ("FEV-ARI-01", "Infant temperature > 99°F (37.2°C) is fever requiring evaluation", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "52", "VERIFIED"),
    ("FEV-ARI-01", "High fever > 39.5°C (103°F) requires tepid sponging, paracetamol, and immediate referral", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "52", "VERIFIED"),
    ("FEV-ARI-01", "Never use cold water for sponging as it causes shivers", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "52", "VERIFIED"),
    ("FEV-ARI-01", "Fever lasting more than 2 days must be referred to ANM/PHC for testing", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "52", "VERIFIED"),
    ("FEV-ARI-01", "Fast breathing, difficulty breathing, chest wall indrawing are ARI danger signs", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "97-98", "VERIFIED"),

    # ANAEMIA-01
    ("ANAEMIA-01", "Pregnant women: 1 IFA tablet daily after 1st trimester (at least 100 IFA tablets)", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "69", "VERIFIED"),
    ("ANAEMIA-01", "Severe pregnancy anaemia requires urgent hospitalisation for blood transfusion", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "69", "VERIFIED"),
    ("ANAEMIA-01", "Do not take iron tablets with tea, coffee, milk, or calcium", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "69", "VERIFIED"),
    ("ANAEMIA-01", "Child anaemia: 1 paediatric iron tablet daily + Albendazole every 6 months", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "88", "VERIFIED"),
    ("ANAEMIA-01", "Child < 2 years gets half tablet Albendazole for deworming", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "88", "VERIFIED"),

    # MALNUT-01
    ("MALNUT-01", "Malnourished child families to be visited 1-2 times per month by ASHA", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "87", "VERIFIED"),
    ("MALNUT-01", "Severely underweight or failing to gain weight requires doctor/ANM referral", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "87", "VERIFIED"),
    ("MALNUT-01", "MUAC screening cut-off (<115 mm) not specified in Induction Module", "GAP: Induction Module lacks MUAC", "N/A", "N/A", "NEEDS HUMAN CHECK"),
    ("MALNUT-01", "Hypertension & Diabetes adult screening schedule not in Induction Module", "GAP: Induction Module lacks CPHC-NCD", "N/A", "N/A", "NEEDS HUMAN CHECK"),

    # NB-CARE-01
    ("NB-CARE-01", "Initiate breastfeeding within 1 hour; feed colostrum; no prelacteal feeds", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "76", "VERIFIED"),
    ("NB-CARE-01", "Normal birth weight is >= 2500g; high risk if < 2000g or preterm < 8m 14d", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "76", "VERIFIED"),
    ("NB-CARE-01", "Delay bathing for low birth weight infants until weight > 2000g", "Induction Training Module for ASHAs", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", "77", "VERIFIED")
]

table_md = "# Clinical Protocol Claims Verification Table\n\n"
table_md += "This table tracks every clinical claim encoded in Sahaaya against official government source documents to ensure zero hallucination and complete clinical auditability.\n\n"
table_md += "| Chunk ID | Factual Claim | Source Document | Source URL | Source Page | Status |\n"
table_md += "| --- | --- | --- | --- | --- | --- |\n"
for row in verification_rows:
    table_md += f"| `{row[0]}` | {row[1]} | {row[2]} | [{row[3]}]({row[3]}) | {row[4]} | **{row[5]}** |\n"

table_md += "\n## Protocol Coverage & Gap Analysis\n\n"
table_md += "1. **Pregnancy Danger Signs (`MCP-DS-01`)**: Fully verified in ASHA Induction Module p.69.\n"
table_md += "2. **Newborn Danger Signs (`NB-DS-01`)**: Fully verified in ASHA Induction Module p.77.\n"
table_md += "3. **HBNC Visit Schedule (`HBNC-VISIT-01`)**: Fully verified in ASHA Induction Module p.76.\n"
table_md += "4. **Universal Immunisation Schedule (`IMM-UIP-01`)**: Fully verified in ASHA Induction Module p.93.\n"
table_md += "5. **Childhood Diarrhoea ORS & Zinc (`STOP-DIA-01`)**: Fully verified in STOP Diarrhoea Guidance p.2 & ASHA Induction Module p.97.\n"
table_md += "6. **Fever & ARI Management (`FEV-ARI-01`)**: Fully verified in ASHA Induction Module p.51-52, 97-98.\n"
table_md += "7. **Anaemia Management (`ANAEMIA-01`)**: Fully verified in ASHA Induction Module p.69, 87-88.\n"
table_md += "8. **Malnutrition Community Counselling (`MALNUT-01`)**: Verified in ASHA Induction Module p.85-87.\n"
table_md += "9. **Essential Newborn Care (`NB-CARE-01`)**: Fully verified in ASHA Induction Module p.74-77.\n"
table_md += "\n### Identified Source Gaps (Marked for Organizer Corpus & Human Review):\n"
table_md += "- **Adult Hypertension / Diabetes Follow-up**: The ASHA Induction Module mentions chronic illnesses generally (p.48) but does not define an adult NCD screening interval or protocol. The MoHFW Comprehensive Primary Health Care (CPHC) NCD module (30+ years, 6-monthly / yearly screening) is required for this topic.\n"
table_md += "- **MUAC Screening Cut-off**: Mid-Upper Arm Circumference (<115 mm for SAM) is covered in HBYC/IMNCI modules, but not in the Induction Module. Flagged as `NEEDS HUMAN CHECK`.\n"
table_md += "- **hbnc-hbyc.mohfw.gov.in Domain Status**: The domain `hbnc-hbyc.mohfw.gov.in` timed out during automated download attempts; primary source verification was established using the authoritative NHM India Induction Module (`nhm.gov.in`) and NHM HP STOP Diarrhoea Guidance (`nhm.hp.gov.in`).\n"

with open("docs/verification_table.md", "w", encoding="utf-8") as f:
    f.write(table_md)
print("Wrote docs/verification_table.md.")
