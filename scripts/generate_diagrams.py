import matplotlib.pyplot as plt
import matplotlib.patches as patches
import os

os.makedirs("docs", exist_ok=True)

def generate_architecture_diagram():
    fig, ax = plt.subplots(figsize=(10.5, 6.8), dpi=300)
    ax.set_facecolor("#F8F9FA")
    fig.patch.set_facecolor("#F8F9FA")

    # Pipeline Boxes
    # 1. Input Box (Voice & Multilingual Text)
    box_query = patches.FancyBboxPatch((0.4, 5.1), 3.2, 1.1, boxstyle="round,pad=0.2", facecolor="#E1F5FE", edgecolor="#0288D1", linewidth=2)
    ax.add_patch(box_query)
    ax.text(2.0, 5.8, "Frontline Worker Input", ha="center", va="center", fontsize=10, fontweight="bold", color="#01579B")
    ax.text(2.0, 5.35, "Offline Voice / Text Query\nTrilingual: EN / Hindi / Telugu", ha="center", va="center", fontsize=8, color="#0277BD")

    # Arrow 1 to Layer 1
    ax.annotate("", xy=(4.4, 5.65), xytext=(3.6, 5.65), arrowprops=dict(arrowstyle="->", lw=2.5, color="#37474F"))

    # 2. Layer 1: Danger Rule Engine
    box_l1 = patches.FancyBboxPatch((4.4, 5.1), 3.4, 1.1, boxstyle="round,pad=0.2", facecolor="#FFEBEE", edgecolor="#D32F2F", linewidth=2.5)
    ax.add_patch(box_l1)
    ax.text(6.1, 5.75, "LAYER 1: DANGER RULE ENGINE", ha="center", va="center", fontsize=10, fontweight="bold", color="#B71C1C")
    ax.text(6.1, 5.35, "Deterministic Regex / Multilingual Tokens\n(100% Zero-Delay Escalation)", ha="center", va="center", fontsize=8, color="#555555")

    # Branch 1: Danger Triggered -> REFER NOW + 1-Tap Referral Slip
    ax.annotate("", xy=(8.2, 5.65), xytext=(7.8, 5.65), arrowprops=dict(arrowstyle="->", lw=2, color="#D32F2F"))
    box_refer = patches.FancyBboxPatch((8.2, 5.05), 2.0, 1.2, boxstyle="round,pad=0.2", facecolor="#D32F2F", edgecolor="#B71C1C", linewidth=2)
    ax.add_patch(box_refer)
    ax.text(9.2, 5.8, "REFER NOW", ha="center", va="center", fontsize=9.5, fontweight="bold", color="#FFFFFF")
    ax.text(9.2, 5.4, "+ Emergency Referral\nSlip (SMS / 108 Alert)", ha="center", va="center", fontsize=7.5, color="#FFEBEE")

    # Arrow 1 down to Layer 2 (Safe query)
    ax.annotate("No Danger", xy=(6.1, 4.3), xytext=(6.1, 5.1), arrowprops=dict(arrowstyle="->", lw=1.8, color="#2E7D32"), fontsize=8, fontweight="bold", color="#2E7D32", ha="right")

    # 3. Layer 2: Intent Gate
    box_l2 = patches.FancyBboxPatch((4.4, 3.2), 3.4, 1.1, boxstyle="round,pad=0.2", facecolor="#FFF3E0", edgecolor="#EF6C00", linewidth=2)
    ax.add_patch(box_l2)
    ax.text(6.1, 3.85, "LAYER 2: INTENT SAFETY GATE", ha="center", va="center", fontsize=10, fontweight="bold", color="#E65100")
    ax.text(6.1, 3.45, "Detects Dosage / Prescription / Diagnosis\nEnforces ASHA Scope Boundaries", ha="center", va="center", fontsize=8, color="#555555")

    # Branch 2: Out of scope -> Refusal
    ax.annotate("", xy=(8.2, 3.75), xytext=(7.8, 3.75), arrowprops=dict(arrowstyle="->", lw=2, color="#EF6C00"))
    box_refuse = patches.FancyBboxPatch((8.2, 3.3), 2.0, 0.9, boxstyle="round,pad=0.2", facecolor="#FFF3E0", edgecolor="#EF6C00", linewidth=1.8)
    ax.add_patch(box_refuse)
    ax.text(9.2, 3.75, "REFUSAL\n(Doctor Redirect)", ha="center", va="center", fontsize=9, fontweight="bold", color="#E65100")

    # Arrow 2 down to Layer 3 (In scope)
    ax.annotate("In Scope", xy=(6.1, 2.4), xytext=(6.1, 3.2), arrowprops=dict(arrowstyle="->", lw=1.8, color="#2E7D32"), fontsize=8, fontweight="bold", color="#2E7D32", ha="right")

    # 4. Layer 3: BM25 Retriever
    box_l3 = patches.FancyBboxPatch((4.4, 1.3), 3.4, 1.1, boxstyle="round,pad=0.2", facecolor="#E0F2F1", edgecolor="#00695C", linewidth=2)
    ax.add_patch(box_l3)
    ax.text(6.1, 1.95, "LAYER 3: PURE KOTLIN BM25", ha="center", va="center", fontsize=10, fontweight="bold", color="#004D40")
    ax.text(6.1, 1.55, "Synonym Expansion + TF-IDF Grounding\nThreshold Gating (No Hallucination)", ha="center", va="center", fontsize=8, color="#555555")

    # Branch 3: Score high -> Cited Answer
    ax.annotate("", xy=(8.2, 1.85), xytext=(7.8, 1.85), arrowprops=dict(arrowstyle="->", lw=2, color="#00695C"))
    box_answer = patches.FancyBboxPatch((8.2, 1.4), 2.0, 0.9, boxstyle="round,pad=0.2", facecolor="#E8F5E9", edgecolor="#2E7D32", linewidth=2)
    ax.add_patch(box_answer)
    ax.text(9.2, 1.85, "CITED ANSWER\n(Doc + Page ID)", ha="center", va="center", fontsize=9, fontweight="bold", color="#1B5E20")

    # 5. Visit List Engine (Parallel module)
    box_visit = patches.FancyBboxPatch((0.4, 1.5), 3.2, 2.4, boxstyle="round,pad=0.2", facecolor="#EDE7F6", edgecolor="#512DA8", linewidth=2)
    ax.add_patch(box_visit)
    ax.text(2.0, 3.5, "VISIT-LIST ENGINE", ha="center", va="center", fontsize=10, fontweight="bold", color="#311B92")
    ax.text(2.0, 2.8, "Household Register Parser\n1. Danger Flags (URGENT)\n2. Overdue Dues (OVERDUE)\n3. Nearest Upcoming (UPCOMING)", ha="center", va="center", fontsize=8, color="#333333")
    ax.text(2.0, 2.05, "1-Tap End-of-Day PHC Summary\n(Transmissible via 2G SMS)", ha="center", va="center", fontsize=8, fontweight="bold", color="#512DA8")

    # 6. Future planned box at bottom
    box_future = patches.FancyBboxPatch((4.4, 0.2), 5.8, 0.7, boxstyle="round,pad=0.15", facecolor="#ECEFF1", edgecolor="#90A4AE", linestyle="--", linewidth=1.5)
    ax.add_patch(box_future)
    ax.text(7.3, 0.55, "PLANNED FINALE EXTENSION: On-Device SLM Rephraser (Using Pre-cached Organizer Models)", ha="center", va="center", fontsize=8, fontstyle="italic", color="#455A64")

    ax.set_xlim(0, 10.5)
    ax.set_ylim(0, 6.8)
    ax.axis("off")

    plt.tight_layout()
    plt.savefig("docs/architecture_diagram.png", dpi=300, bbox_inches="tight")
    plt.close()
    print("Saved docs/architecture_diagram.png")

def generate_app_mockup():
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(10, 5.5), dpi=300)
    fig.patch.set_facecolor("#F8F9FA")

    # --- Screen 1: Ask Screen Mockup ---
    ax1.set_facecolor("#FFFFFF")
    ax1.set_xlim(0, 10)
    ax1.set_ylim(0, 16)
    ax1.axis("off")

    # Outer phone border
    phone1 = patches.FancyBboxPatch((0.2, 0.2), 9.6, 15.6, boxstyle="round,pad=0.3", facecolor="#FFFFFF", edgecolor="#37474F", linewidth=3)
    ax1.add_patch(phone1)

    # Top app bar with Language & Theme
    bar1 = patches.Rectangle((0.2, 14.0), 9.6, 1.8, facecolor="#00695C")
    ax1.add_patch(bar1)
    ax1.text(3.5, 15.1, "Sahaaya Companion", ha="center", va="center", fontsize=11, fontweight="bold", color="#FFFFFF")
    ax1.text(3.5, 14.4, "100% Offline • EN / HI / TE", ha="center", va="center", fontsize=7.5, color="#E0F2F1")
    ax1.text(8.5, 14.9, "[TE | Theme]", ha="center", va="center", fontsize=8.5, fontweight="bold", color="#80CBC4")

    # Query Input Box with Voice Mic Icon
    input_box = patches.FancyBboxPatch((0.8, 12.4), 8.4, 1.2, boxstyle="round,pad=0.1", facecolor="#F5F5F5", edgecolor="#00695C", linewidth=1.5)
    ax1.add_patch(input_box)
    ax1.text(1.2, 13.0, "garbhavathi ki raktham paduthondi...", fontsize=8.5, color="#212121")
    ax1.text(8.4, 13.0, "[MIC]", fontsize=8.5, fontweight="bold", color="#00695C")

    # Search Button
    btn_box = patches.FancyBboxPatch((0.8, 10.9), 8.4, 1.0, boxstyle="round,pad=0.12", facecolor="#00695C", edgecolor="#004D40", linewidth=1)
    ax1.add_patch(btn_box)
    ax1.text(5.0, 11.4, "SEARCH PROTOCOL", ha="center", va="center", fontsize=9.5, fontweight="bold", color="#FFFFFF")

    # Red Emergency Card with 1-Tap Referral Slip
    em_card = patches.FancyBboxPatch((0.8, 3.8), 8.4, 6.6, boxstyle="round,pad=0.2", facecolor="#FFEBEE", edgecolor="#D32F2F", linewidth=2.5)
    ax1.add_patch(em_card)
    
    # Red badge
    badge = patches.FancyBboxPatch((1.2, 9.4), 4.6, 0.75, boxstyle="round,pad=0.1", facecolor="#D32F2F", edgecolor="#B71C1C")
    ax1.add_patch(badge)
    ax1.text(3.5, 9.75, "EMERGENCY: REFER NOW", ha="center", va="center", fontsize=8.5, fontweight="bold", color="#FFFFFF")

    ax1.text(1.2, 8.85, "Danger Sign: Vaginal bleeding in pregnancy", fontsize=8.5, fontweight="bold", color="#C62828")
    ax1.text(1.2, 8.35, "Category: PREGNANCY EMERGENCY", fontsize=7.5, color="#555555")
    ax1.text(1.2, 7.3, "Immediate Action:\nEscort mother immediately to First Referral Unit (FRU)\nor Hospital. Do not wait or self-treat.", fontsize=7.2, color="#212121")

    # Citation box
    cit_box = patches.FancyBboxPatch((1.2, 5.6), 7.6, 0.85, boxstyle="round,pad=0.08", facecolor="#FFFFFF", edgecolor="#B0BEC5")
    ax1.add_patch(cit_box)
    ax1.text(5.0, 6.0, "Source: MCP-DS-01, Induction Module for ASHAs, p.69", ha="center", va="center", fontsize=7, fontweight="bold", color="#1F4E8C")

    # 1-Tap Emergency Referral Slip Button
    ref_btn = patches.FancyBboxPatch((1.2, 4.3), 7.6, 0.9, boxstyle="round,pad=0.08", facecolor="#D32F2F", edgecolor="#B71C1C")
    ax1.add_patch(ref_btn)
    ax1.text(5.0, 4.75, "GENERATE REFERRAL SLIP (SMS / 108)", ha="center", va="center", fontsize=8, fontweight="bold", color="#FFFFFF")

    # Bottom bar
    bbar1 = patches.Rectangle((0.2, 0.2), 9.6, 1.4, facecolor="#ECEFF1")
    ax1.add_patch(bbar1)
    ax1.text(2.6, 0.9, "[ Ask Sahaaya ]", ha="center", va="center", fontsize=9, fontweight="bold", color="#00695C")
    ax1.text(7.4, 0.9, "Today's Visits", ha="center", va="center", fontsize=9, color="#78909C")
    ax1.text(5.0, 16.3, "SCREEN 1: SAFETY ESCALATION & VOICE", ha="center", va="center", fontsize=10, fontweight="bold", color="#1F4E8C")


    # --- Screen 2: Today's Visits Screen Mockup ---
    ax2.set_facecolor("#FFFFFF")
    ax2.set_xlim(0, 10)
    ax2.set_ylim(0, 16)
    ax2.axis("off")

    phone2 = patches.FancyBboxPatch((0.2, 0.2), 9.6, 15.6, boxstyle="round,pad=0.3", facecolor="#FFFFFF", edgecolor="#37474F", linewidth=3)
    ax2.add_patch(phone2)

    bar2 = patches.Rectangle((0.2, 14.0), 9.6, 1.8, facecolor="#00695C")
    ax2.add_patch(bar2)
    ax2.text(3.5, 15.1, "Today's Prioritised Visits", ha="center", va="center", fontsize=11, fontweight="bold", color="#FFFFFF")
    ax2.text(3.5, 14.4, "Date: 18 Oct 2026 | 33 Beneficiaries", ha="center", va="center", fontsize=7.5, color="#E0F2F1")
    ax2.text(8.2, 14.9, "[Daily Report]", ha="center", va="center", fontsize=8, fontweight="bold", color="#80CBC4")

    # Summary Badges Row
    b_danger = patches.FancyBboxPatch((0.8, 12.3), 2.5, 1.3, boxstyle="round,pad=0.1", facecolor="#FFEBEE", edgecolor="#D32F2F", linewidth=1.5)
    ax2.add_patch(b_danger)
    ax2.text(2.05, 13.2, "5", ha="center", va="center", fontsize=12, fontweight="bold", color="#D32F2F")
    ax2.text(2.05, 12.6, "DANGER", ha="center", va="center", fontsize=8, fontweight="bold", color="#D32F2F")

    b_overdue = patches.FancyBboxPatch((3.75, 12.3), 2.5, 1.3, boxstyle="round,pad=0.1", facecolor="#FFF3E0", edgecolor="#EF6C00", linewidth=1.5)
    ax2.add_patch(b_overdue)
    ax2.text(5.0, 13.2, "14", ha="center", va="center", fontsize=12, fontweight="bold", color="#EF6C00")
    ax2.text(5.0, 12.6, "OVERDUE", ha="center", va="center", fontsize=8, fontweight="bold", color="#EF6C00")

    b_up = patches.FancyBboxPatch((6.7, 12.3), 2.5, 1.3, boxstyle="round,pad=0.1", facecolor="#E3F2FD", edgecolor="#1976D2", linewidth=1.5)
    ax2.add_patch(b_up)
    ax2.text(7.95, 13.2, "14", ha="center", va="center", fontsize=12, fontweight="bold", color="#1976D2")
    ax2.text(7.95, 12.6, "UPCOMING", ha="center", va="center", fontsize=8, fontweight="bold", color="#1976D2")

    # Visit Card 1: Danger
    vc1 = patches.FancyBboxPatch((0.8, 8.8), 8.4, 3.0, boxstyle="round,pad=0.15", facecolor="#FFFFFF", edgecolor="#D32F2F", linewidth=2)
    ax2.add_patch(vc1)
    ax2.text(1.2, 11.2, "Kavitha Babu (FAM-003)", fontsize=9, fontweight="bold", color="#1F4E8C")
    ax2.text(1.2, 10.6, "PREGNANT WOMAN | EDD: 2026-11-20", fontsize=7.5, color="#616161")
    bd1 = patches.FancyBboxPatch((6.8, 10.7), 2.1, 0.7, boxstyle="round,pad=0.08", facecolor="#D32F2F", edgecolor="#B71C1C")
    ax2.add_patch(bd1)
    ax2.text(7.85, 11.05, "URGENT DANGER", ha="center", va="center", fontsize=7, fontweight="bold", color="#FFFFFF")
    ax2.text(1.2, 9.6, "Reason: Vaginal bleeding reported in 3rd trimester.\nImmediate FRU referral required.", fontsize=7.5, fontweight="bold", color="#C62828")
    ax2.text(1.2, 9.1, "Action: [Slip] [Refer Now] [Protocol]", fontsize=7, color="#757575")

    # Visit Card 2: Overdue
    vc2 = patches.FancyBboxPatch((0.8, 5.4), 8.4, 3.0, boxstyle="round,pad=0.15", facecolor="#FFFFFF", edgecolor="#EF6C00", linewidth=1.5)
    ax2.add_patch(vc2)
    ax2.text(1.2, 7.8, "Baby of Shanti Kumari (FAM-012)", fontsize=9, fontweight="bold", color="#1F4E8C")
    ax2.text(1.2, 7.2, "NEWBORN | DOB: 2026-09-29", fontsize=7.5, color="#616161")
    bd2 = patches.FancyBboxPatch((6.8, 7.3), 2.1, 0.7, boxstyle="round,pad=0.08", facecolor="#EF6C00", edgecolor="#E65100")
    ax2.add_patch(bd2)
    ax2.text(7.85, 7.65, "OVERDUE (3d)", ha="center", va="center", fontsize=7, fontweight="bold", color="#FFFFFF")
    ax2.text(1.2, 6.2, "Reason: HBNC Day 14 Home Visit was due on 2026-10-15\n(3 days overdue).", fontsize=7.5, color="#E65100")
    ax2.text(1.2, 5.7, "Due: 2026-10-15 | Vaccines: BCG, OPV-0, HepB-0", fontsize=7, color="#757575")

    # Visit Card 3: Upcoming
    vc3 = patches.FancyBboxPatch((0.8, 2.0), 8.4, 3.0, boxstyle="round,pad=0.15", facecolor="#FFFFFF", edgecolor="#1976D2", linewidth=1.5)
    ax2.add_patch(vc3)
    ax2.text(1.2, 4.4, "Vihaan Begum (FAM-013)", fontsize=9, fontweight="bold", color="#1F4E8C")
    ax2.text(1.2, 3.8, "CHILD UNDER 5 | Age: 14 weeks", fontsize=7.5, color="#616161")
    bd3 = patches.FancyBboxPatch((6.8, 3.9), 2.1, 0.7, boxstyle="round,pad=0.08", facecolor="#1976D2", edgecolor="#0D47A1")
    ax2.add_patch(bd3)
    ax2.text(7.85, 4.25, "UPCOMING (2d)", ha="center", va="center", fontsize=7, fontweight="bold", color="#FFFFFF")
    ax2.text(1.2, 2.8, "Reason: OPV-3, Pentavalent-3 due on 2026-10-20.", fontsize=7.5, color="#0D47A1")
    ax2.text(1.2, 2.3, "Due: 2026-10-20 | Vaccines: OPV 1-2, Penta 1-2", fontsize=7, color="#757575")

    # Bottom bar
    bbar2 = patches.Rectangle((0.2, 0.2), 9.6, 1.4, facecolor="#ECEFF1")
    ax2.add_patch(bbar2)
    ax2.text(2.6, 0.9, "Ask Sahaaya", ha="center", va="center", fontsize=9, color="#78909C")
    ax2.text(7.4, 0.9, "[ Today's Visits ]", ha="center", va="center", fontsize=9, fontweight="bold", color="#00695C")
    ax2.text(5.0, 16.3, "SCREEN 2: PRIORITISED VISITS & REPORT", ha="center", va="center", fontsize=10, fontweight="bold", color="#1F4E8C")

    plt.tight_layout()
    plt.savefig("docs/app_mockup.png", dpi=300, bbox_inches="tight")
    plt.close()
    print("Saved docs/app_mockup.png")

if __name__ == "__main__":
    generate_architecture_diagram()
    generate_app_mockup()

