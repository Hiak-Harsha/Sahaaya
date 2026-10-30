import os
from pptx import Presentation
from pptx.util import Inches, Pt
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.enum.shapes import MSO_SHAPE
from PIL import Image

def build_pptx():
    out_pptx = "TEAM-001245_Sahaaya_Round1_Submission.pptx"
    print(f"Building {out_pptx}...")

    # Colors
    c_navy = RGBColor(31, 70, 135)        # #1F4687 (official theme navy)
    c_white = RGBColor(255, 255, 255)
    c_dark = RGBColor(38, 38, 38)         # #262626
    c_gray_bg = RGBColor(246, 248, 251)   # #F6F8FB
    c_border = RGBColor(208, 217, 229)    # #D0D9E5
    c_light_blue = RGBColor(79, 163, 227) # #4FA3E3
    c_link_blue = RGBColor(0, 75, 165)    # #004BA5
    c_yt_red = RGBColor(204, 0, 0)        # #CC0000
    c_green = RGBColor(36, 125, 46)       # #247D2E
    c_sub_label = RGBColor(173, 205, 238) # #ADCDEE

    prs = Presentation()
    prs.slide_width = Inches(13.333333)
    prs.slide_height = Inches(7.5)
    blank_layout = prs.slide_layouts[6]

    # Helper: Add Standard Top Banner
    def add_top_banner(slide, title_text):
        banner = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0), Inches(0), Inches(13.333333), Inches(1.08))
        banner.fill.solid()
        banner.fill.fore_color.rgb = c_navy
        banner.line.color.rgb = c_navy
        
        tf = banner.text_frame
        tf.word_wrap = True
        tf.margin_left = Inches(0.8)
        tf.margin_top = Inches(0.18)
        tf.vertical_anchor = MSO_ANCHOR.MIDDLE
        p = tf.paragraphs[0]
        p.text = title_text
        p.font.name = "Calibri"
        p.font.size = Pt(28)
        p.font.bold = True
        p.font.color.rgb = c_white

    # Helper: Add Card Box
    def add_card(slide, left, top, width, height, bg_color=c_gray_bg, border_color=c_border, shape_type=MSO_SHAPE.ROUNDED_RECTANGLE):
        card = slide.shapes.add_shape(shape_type, left, top, width, height)
        card.fill.solid()
        card.fill.fore_color.rgb = bg_color
        card.line.color.rgb = border_color
        card.line.width = Pt(1)
        return card

    # Helper: Add Bullet Block to TextFrame
    def add_bullet_point(tf, title, desc, title_color=c_navy, font_size=10.5, space_after=5):
        p = tf.add_paragraph() if tf.paragraphs[0].text else tf.paragraphs[0]
        p.space_after = Pt(space_after)
        p.line_spacing = 1.15
        p.alignment = PP_ALIGN.LEFT
        
        r1 = p.add_run()
        r1.text = title + " "
        r1.font.name = "Calibri"
        r1.font.bold = True
        r1.font.size = Pt(font_size)
        r1.font.color.rgb = title_color
        
        r2 = p.add_run()
        r2.text = desc
        r2.font.name = "Calibri"
        r2.font.size = Pt(font_size)
        r2.font.color.rgb = c_dark

    # Optimize media
    def get_optimized_image(src_path, opt_path, max_dim=1600, quality=92):
        if not os.path.exists(opt_path) and os.path.exists(src_path):
            img = Image.open(src_path)
            if img.mode in ("RGBA", "P"):
                img = img.convert("RGB")
            if max(img.size) > max_dim:
                scale = max_dim / max(img.size)
                new_size = (int(img.size[0] * scale), int(img.size[1] * scale))
                img = img.resize(new_size, Image.Resampling.LANCZOS)
            img.save(opt_path, "JPEG", quality=quality, optimize=True)
        return opt_path if os.path.exists(opt_path) else src_path

    mockup_img = get_optimized_image("docs/app_mockup.png", "docs/app_mockup_opt.jpg", max_dim=1600, quality=92)
    arch_img = get_optimized_image("docs/architecture_diagram.png", "docs/arch_diag_opt.jpg", max_dim=1500, quality=92)
    demo_img = get_optimized_image("docs/p7_demo_embed.png", "docs/demo_embed_opt.jpg", max_dim=1400, quality=92)

    # =========================================================================
    # SLIDE 1: TITLE SLIDE
    # =========================================================================
    s1 = prs.slides.add_slide(blank_layout)
    bg1 = s1.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0), Inches(0), Inches(13.333333), Inches(7.5))
    bg1.fill.solid()
    bg1.fill.fore_color.rgb = c_navy
    bg1.line.color.rgb = c_navy

    # Title & Subtitle Box
    t_box = s1.shapes.add_textbox(Inches(1.0), Inches(1.3), Inches(11.333), Inches(1.8))
    tf1 = t_box.text_frame
    tf1.word_wrap = True
    p_main = tf1.paragraphs[0]
    p_main.alignment = PP_ALIGN.CENTER
    r_main = p_main.add_run()
    r_main.text = "ROUND 1 REVIEW"
    r_main.font.name = "Calibri"
    r_main.font.size = Pt(44)
    r_main.font.bold = True
    r_main.font.color.rgb = c_white

    p_sub = tf1.add_paragraph()
    p_sub.alignment = PP_ALIGN.CENTER
    p_sub.space_before = Pt(8)
    r_sub = p_sub.add_run()
    r_sub.text = "AI Frontier Challenge 2026 Round 1 Submission"
    r_sub.font.name = "Calibri"
    r_sub.font.size = Pt(22)
    r_sub.font.bold = True
    r_sub.font.color.rgb = c_light_blue

    # 4 Metadata Fields Cards
    meta_cards = [
        ("Team Name", "Vizag Mavericks (TEAM-001245)", Inches(1.4), Inches(3.9), Inches(5.1), Inches(1.25)),
        ("Team Leader", "Amara Yogeswararao", Inches(6.8), Inches(3.9), Inches(5.1), Inches(1.25)),
        ("Track", "H5. Frontline Health-Worker Companion for Low-Connectivity Areas", Inches(1.4), Inches(5.35), Inches(5.1), Inches(1.25)),
        ("Date", "18/10/2026", Inches(6.8), Inches(5.35), Inches(5.1), Inches(1.25))
    ]

    for label, val, left, top, width, height in meta_cards:
        card = add_card(s1, left, top, width, height, bg_color=RGBColor(24, 56, 108), border_color=RGBColor(48, 92, 160))
        tf = card.text_frame
        tf.word_wrap = True
        tf.margin_left = Inches(0.3)
        tf.margin_top = Inches(0.2)
        
        p_lbl = tf.paragraphs[0]
        p_lbl.alignment = PP_ALIGN.LEFT
        r_lbl = p_lbl.add_run()
        r_lbl.text = label
        r_lbl.font.name = "Calibri"
        r_lbl.font.size = Pt(12)
        r_lbl.font.bold = True
        r_lbl.font.color.rgb = c_sub_label
        
        p_val = tf.add_paragraph()
        p_val.alignment = PP_ALIGN.LEFT
        p_val.space_before = Pt(4)
        r_val = p_val.add_run()
        r_val.text = val
        r_val.font.name = "Calibri"
        r_val.font.size = Pt(14)
        r_val.font.bold = True
        r_val.font.color.rgb = c_white

    # =========================================================================
    # SLIDE 2: USE CASE (Problem Statement & Market Opportunity)
    # =========================================================================
    s2 = prs.slides.add_slide(blank_layout)
    add_top_banner(s2, "Use Case")

    # Section 1: Problem Statement
    h1_box = s2.shapes.add_textbox(Inches(0.8), Inches(1.15), Inches(11.7), Inches(0.35))
    h1_tf = h1_box.text_frame
    h1_tf.margin_left = 0
    p = h1_tf.paragraphs[0]
    p.text = "Problem Statement"
    p.font.name = "Calibri"
    p.font.size = Pt(16.5)
    p.font.bold = True
    p.font.color.rgb = c_navy

    prob_box = s2.shapes.add_textbox(Inches(0.8), Inches(1.48), Inches(11.7), Inches(1.6))
    prob_tf = prob_box.text_frame
    prob_tf.word_wrap = True
    prob_tf.margin_left = 0
    prob_tf.margin_top = 0

    prob_bullets = [
        ("• Frontline Overload & Zero Mobile Reception:", "India's 10.29 lakh ASHAs and 2.5 lakh ANMs manage up to 28 paper registers [5]; in Andhra Pradesh alone, 1,700+ remote villages (1,000+ in ASR district) have complete zero mobile reception [4]."),
        ("• Preventable Maternal & Neonatal Mortality:", "Obstetric haemorrhage causes 47% of maternal deaths [3]; unrecognised newborn danger signs (hypothermia, convulsions, chest indrawing) lead to fatal referral delays [1, 2]."),
        ("• AI Safety & Frontline Malpractice Risks:", "General-purpose AI can produce inaccurate or unsafe medical guidance without appropriate safeguards, risking non-prescribing ASHA boundary violations [1], failing offline, and lacking WHO clinical guardrails [6].")
    ]
    for title, desc in prob_bullets:
        add_bullet_point(prob_tf, title, desc, font_size=10.2, space_after=4)

    # Section 2: Market Opportunity
    h2_box = s2.shapes.add_textbox(Inches(0.8), Inches(3.22), Inches(11.7), Inches(0.35))
    h2_tf = h2_box.text_frame
    h2_tf.margin_left = 0
    p = h2_tf.paragraphs[0]
    p.text = "Market Opportunity"
    p.font.name = "Calibri"
    p.font.size = Pt(16.5)
    p.font.bold = True
    p.font.color.rgb = c_navy

    mkt_box = s2.shapes.add_textbox(Inches(0.8), Inches(3.55), Inches(11.7), Inches(1.6))
    mkt_tf = mkt_box.text_frame
    mkt_tf.word_wrap = True
    mkt_tf.margin_left = 0
    mkt_tf.margin_top = 0

    mkt_bullets = [
        ("• Frontline Scale & National Need:", "10.29 lakh ASHAs and ~2.5 lakh Sub-Centre ANMs across India serving 1,000 population norms, requiring an intuitive, reliable paramedical companion [1, 2]."),
        ("• Critical Gap in Existing Systems:", "Existing state tools (ANMOL, Kilkari) focus on administrative reporting or outbound audio broadcasts; they rely on mobile networks [4] and lack on-demand offline clinical danger triage or strict scope gating [5]."),
        ("• Sahaaya High-Impact Value:", "100% offline, protocol-grounded companion: deterministic danger-sign escalation [1], physician redirects [6], prioritized visits, emergency referral slips, and 2G SMS field sync [4].")
    ]
    for title, desc in mkt_bullets:
        add_bullet_point(mkt_tf, title, desc, font_size=10.2, space_after=4)

    # Reference Citations Card
    add_card(s2, Inches(0.8), Inches(5.35), Inches(11.733), Inches(1.8), bg_color=c_gray_bg, border_color=c_border)
    
    # Header for citations card
    ref_title_box = s2.shapes.add_textbox(Inches(1.0), Inches(5.42), Inches(11.3), Inches(0.3))
    rt_tf = ref_title_box.text_frame
    rt_tf.margin_left, rt_tf.margin_top = 0, 0
    p_ref = rt_tf.paragraphs[0]
    p_ref.text = "Official Protocol Sources & Clinical Evidence Citations:"
    p_ref.font.name = "Calibri"
    p_ref.font.size = Pt(11)
    p_ref.font.bold = True
    p_ref.font.color.rgb = c_navy

    citations = [
        ("[1] MoHFW National Guidelines: ", "Revised India MCP Card (2018) & ASHA Modules 6-7", "https://hbnc-hbyc.mohfw.gov.in/assets/document/Revised%20India%20MCP%20Card%20English_(28-05-2018).pdf"),
        ("[2] NHM Clinical Guidance: ", "ASHA Induction Module (pp. 51-97) & STOP Diarrhoea", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf"),
        ("[3] PMC9292773 Study: ", "Obstetric haemorrhage causes 47% maternal deaths in India", "https://pmc.ncbi.nlm.nih.gov/articles/PMC9292773/"),
        ("[4] TelecomTalk AP Survey: ", "1,700 remote hamlets (ASR dist) zero cellular signal", "https://telecomtalk.info/?p=654511"),
        ("[5] New Lines / IJCMPH 2024: ", "Frontline overload (28 paper registers, 23 apps)", "https://newlinesmag.com/reportage/indias-digital-health-push-is-overworking-its-front-line-women/"),
        ("[6] WHO AI Safety Standards: ", "Multi-modal AI guidance for clinical decision support", "https://www.downtoearth.org.in/science-technology/who-releases-guidelines-for-multi-modal-generative-ai-in-healthcare-resonates-with-recommendations-for-other-sectors-93991"),
    ]

    col1_box = s2.shapes.add_textbox(Inches(1.0), Inches(5.75), Inches(5.6), Inches(1.3))
    c1_tf = col1_box.text_frame
    c1_tf.word_wrap = True
    c1_tf.margin_left, c1_tf.margin_top = 0, 0
    for i in range(3):
        tag, desc, url = citations[i]
        p = c1_tf.add_paragraph() if c1_tf.paragraphs[0].text else c1_tf.paragraphs[0]
        p.space_after = Pt(3)
        r1 = p.add_run()
        r1.text = tag
        r1.font.bold = True
        r1.font.size = Pt(9.5)
        r1.font.color.rgb = c_navy
        r2 = p.add_run()
        r2.text = desc
        r2.font.size = Pt(9.5)
        r2.font.color.rgb = c_link_blue
        r2.font.underline = True
        r2.hyperlink.address = url

    col2_box = s2.shapes.add_textbox(Inches(6.8), Inches(5.75), Inches(5.6), Inches(1.3))
    c2_tf = col2_box.text_frame
    c2_tf.word_wrap = True
    c2_tf.margin_left, c2_tf.margin_top = 0, 0
    for i in range(3, 6):
        tag, desc, url = citations[i]
        p = c2_tf.add_paragraph() if c2_tf.paragraphs[0].text else c2_tf.paragraphs[0]
        p.space_after = Pt(3)
        r1 = p.add_run()
        r1.text = tag
        r1.font.bold = True
        r1.font.size = Pt(9.5)
        r1.font.color.rgb = c_navy
        r2 = p.add_run()
        r2.text = desc
        r2.font.size = Pt(9.5)
        r2.font.color.rgb = c_link_blue
        r2.font.underline = True
        r2.hyperlink.address = url

    # =========================================================================
    # SLIDE 3: PROPOSED SOLUTION
    # =========================================================================
    s3 = prs.slides.add_slide(blank_layout)
    add_top_banner(s3, "Proposed Solution")

    s3_h = s3.shapes.add_textbox(Inches(0.8), Inches(1.15), Inches(11.7), Inches(0.35))
    s3_tf = s3_h.text_frame
    s3_tf.margin_left = 0
    p = s3_tf.paragraphs[0]
    p.text = "Solution Overview"
    p.font.name = "Calibri"
    p.font.size = Pt(16.5)
    p.font.bold = True
    p.font.color.rgb = c_navy

    sol_bullets = [
        ("• Deterministic Danger Escalation + Referral Slip:", "Scans queries across English, Hinglish & Telugu-Latin; triggers red screen (100% recall on 367 phrasings [1, 2]) + 1-tap 108 Emergency Referral Slip."),
        ("• Strict Non-Prescribing Scope Gate:", "Hard-blocks antibiotic & dosage questions with physician redirects, ensuring paramedical workers operate strictly within ASHA guidelines [1, 6]."),
        ("• Extractive BM25 Protocol Retrieval:", "Pure Kotlin BM25 matches questions to official MoHFW/NHM manuals [1, 2] with exact document & page citations (0.53 ms latency, 0% hallucination)."),
        ("• Prioritised Daily Household Action Plan & 2G Sync:", "Triage ranking: Danger Flags > Overdue Follow-ups > Routine Due Dates + 1-Tap End-of-Day PHC Summary Report (transmissible via 2G SMS [4, 5]).")
    ]
    sol_box = s3.shapes.add_textbox(Inches(0.8), Inches(1.48), Inches(11.7), Inches(1.7))
    sol_tf = sol_box.text_frame
    sol_tf.word_wrap = True
    sol_tf.margin_left, sol_tf.margin_top = 0, 0
    for title, desc in sol_bullets:
        add_bullet_point(sol_tf, title, desc, font_size=10.2, space_after=3)

    if os.path.exists(mockup_img):
        add_card(s3, Inches(0.8), Inches(3.28), Inches(11.733), Inches(3.9), bg_color=c_white, border_color=c_border)
        img_w = Inches(7.5)
        img_h = Inches(3.8)
        img_left = Inches(0.8) + (Inches(11.733) - img_w) / 2
        s3.shapes.add_picture(mockup_img, img_left, Inches(3.33), img_w, img_h)

    # =========================================================================
    # SLIDE 4: DESIGN STEPS & ARCHITECTURE
    # =========================================================================
    s4 = prs.slides.add_slide(blank_layout)
    add_top_banner(s4, "Design Steps & Architecture")

    step_h = s4.shapes.add_textbox(Inches(0.8), Inches(1.15), Inches(5.6), Inches(0.35))
    st_tf = step_h.text_frame
    st_tf.margin_left = 0
    p = st_tf.paragraphs[0]
    p.text = "Technical Implementation"
    p.font.name = "Calibri"
    p.font.size = Pt(16.5)
    p.font.bold = True
    p.font.color.rgb = c_navy

    steps = [
        ("1. Input & Speech Recognition:", "Trilingual voice mic and text (EN, Hindi, Telugu-Latin) for instant frontline accessibility."),
        ("2. Danger-Sign Rule Engine [Current]:", "Deterministic regex on 27 danger signs (100% recall [1, 2]); immediately triggers Red Alert + 108 Referral Slip."),
        ("3. Intent Safety Gate [Current]:", "Intercepts dosage & prescription queries; enforces non-prescribing boundaries [1, 6] with physician redirects."),
        ("4. Offline BM25 Retrieval [Current]:", "Pure Kotlin search over local on-device MoHFW/NHM protocol chunks; exact page citations (0% hallucination)."),
        ("5. Parallel Household Engine [Current]:", "Ingests offline household register -> Urgency triage (Danger > Overdue > Scheduled) -> Daily Prioritised Visit List."),
        ("6. Planned Extensions [Roadmap]:", "On-device SLM rephraser for natural conversational synthesis; automated 2G SMS field summary sync.")
    ]
    st_box = s4.shapes.add_textbox(Inches(0.8), Inches(1.48), Inches(5.6), Inches(5.6))
    st_b_tf = st_box.text_frame
    st_b_tf.word_wrap = True
    st_b_tf.margin_left, st_b_tf.margin_top = 0, 0
    for title, desc in steps:
        add_bullet_point(st_b_tf, title, desc, font_size=10.0, space_after=6)

    diag_h = s4.shapes.add_textbox(Inches(6.6), Inches(1.15), Inches(5.9), Inches(0.35))
    dg_tf = diag_h.text_frame
    dg_tf.margin_left = 0
    p = dg_tf.paragraphs[0]
    r1 = p.add_run()
    r1.text = "Architecture Diagram "
    r1.font.bold = True
    r1.font.size = Pt(16.5)
    r1.font.color.rgb = c_navy
    r2 = p.add_run()
    r2.text = "(Safety-First & Parallel Workflows)"
    r2.font.size = Pt(11.5)
    r2.font.color.rgb = RGBColor(100, 115, 130)

    if os.path.exists(arch_img):
        add_card(s4, Inches(6.6), Inches(1.55), Inches(5.933), Inches(5.45), bg_color=c_white, border_color=c_border)
        dw = Inches(5.8)
        dh = Inches(3.86)
        dy = Inches(1.55) + (Inches(5.45) - dh) / 2
        s4.shapes.add_picture(arch_img, Inches(6.66), dy, dw, dh)

    # =========================================================================
    # SLIDE 5: CHALLENGES & MITIGATION
    # =========================================================================
    s5 = prs.slides.add_slide(blank_layout)
    add_top_banner(s5, "Challenges & Mitigation")

    challenges = [
        (
            "Challenge 1: Zero Connectivity & Mobile Resource Constraints",
            "Zero mobile reception in rural hamlets and severe RAM/battery constraints on frontline smartphones.",
            "100% on-device architecture with zero network permissions. Measured across 30 diagnostic queries on physical hardware (OnePlus CPH2613, Android 15 in Airplane Mode) and standalone JVM: APK size is 16.9 MB, median query latency is 0.53 ms (P95: 5.38 ms), and peak heap RAM footprint is < 1 MB."
        ),
        (
            "Challenge 2: Hallucination Risks & Clinical Safety Boundary Violations",
            "Medical hallucination risks and catastrophic delays from missed pregnancy and newborn danger signs.",
            "Deterministic danger rules execute BEFORE any retrieval logic. Tested across a 367-phrase benchmark derived from 27 MoHFW/NHM emergency signs across English, Hinglish & Telugu-Latin colloquial variants: achieved 100% recall on the test set, triggering instant Red Alerts + 108 referral slips without retrieval delay."
        ),
        (
            "Challenge 3: Vernacular Dialects & Heavy Paper Administrative Friction",
            "Code-mixed vernacular queries (English, Hinglish, Telugu-Latin) with dialectal variations, typos, and paper register friction.",
            "[Implemented]: Robust phonetic tokenisation, code-mixed synonym mapping, and clinical urgency triage (Danger > Overdue > Scheduled) with 1-tap referral slips. [Planned]: On-device SLM rephraser using pre-cached organizer models and background 2G SMS gateway sync for daily PHC reporting."
        )
    ]

    c_tops = [Inches(1.25), Inches(3.25), Inches(5.25)]
    for idx, (ch_title, prob, mit) in enumerate(challenges):
        card = add_card(s5, Inches(0.8), c_tops[idx], Inches(11.733), Inches(1.8), bg_color=c_gray_bg, border_color=c_border)
        tf = card.text_frame
        tf.word_wrap = True
        tf.margin_left = Inches(0.28)
        tf.margin_top = Inches(0.14)
        
        # Header (Left-aligned)
        p0 = tf.paragraphs[0]
        p0.alignment = PP_ALIGN.LEFT
        r0 = p0.add_run()
        r0.text = ch_title
        r0.font.bold = True
        r0.font.size = Pt(12.5)
        r0.font.color.rgb = c_navy
        
        # Problem
        p1 = tf.add_paragraph()
        p1.alignment = PP_ALIGN.LEFT
        p1.space_before = Pt(4)
        r_p_tag = p1.add_run()
        r_p_tag.text = "Problem: "
        r_p_tag.font.bold = True
        r_p_tag.font.size = Pt(10.2)
        r_p_tag.font.color.rgb = c_navy
        r_p_val = p1.add_run()
        r_p_val.text = prob
        r_p_val.font.size = Pt(10.2)
        r_p_val.font.color.rgb = c_dark
        
        # Mitigation
        p2 = tf.add_paragraph()
        p2.alignment = PP_ALIGN.LEFT
        p2.space_before = Pt(3)
        r_m_tag = p2.add_run()
        r_m_tag.text = "Mitigation: "
        r_m_tag.font.bold = True
        r_m_tag.font.size = Pt(10.2)
        r_m_tag.font.color.rgb = c_green
        r_m_val = p2.add_run()
        r_m_val.text = mit
        r_m_val.font.size = Pt(10.2)
        r_m_val.font.color.rgb = c_dark

    # =========================================================================
    # SLIDE 6: UNIQUE SELLING PROPOSITION (USP)
    # =========================================================================
    s6 = prs.slides.add_slide(blank_layout)
    add_top_banner(s6, "Unique Selling Proposition (USP)")

    diffs = [
        (
            "Deterministic Safety Pipeline",
            "• Emergency rules execute BEFORE any AI logic.\n• 100% danger recall on 367 benchmark phrasings [1, 2].\n• Hard non-prescribing gate [6] with extractive, source-cited responses & 108 slips."
        ),
        (
            "100% Offline & Measured Speed",
            "• Zero network permissions (built for [4]).\n• OnePlus CPH2613 (Airplane Mode):\n   - Median Query Latency: 0.53 ms\n   - P95 Benchmark Latency: 5.38 ms\n   - APK: 16.9 MB | Peak RAM: < 1 MB\n• Runs natively without SIM or signal."
        ),
        (
            "Prioritised Household Action Plan",
            "• Transforms 30+ static paper records into an actionable daily visit list.\n• Ensures high-risk pregnant women & unimmunized infants are visited first (Danger > Overdue > Scheduled).\n• [Planned Extension: 2G SMS sync]."
        )
    ]

    col_w = Inches(3.75)
    col_x = [Inches(0.8), Inches(4.79), Inches(8.78)]
    for idx, (title, content) in enumerate(diffs):
        card = add_card(s6, col_x[idx], Inches(1.25), col_w, Inches(3.45), bg_color=c_gray_bg, border_color=c_border)
        tf = card.text_frame
        tf.word_wrap = True
        tf.margin_left = Inches(0.22)
        tf.margin_top = Inches(0.18)
        
        p = tf.paragraphs[0]
        p.alignment = PP_ALIGN.LEFT
        r = p.add_run()
        r.text = title
        r.font.bold = True
        r.font.size = Pt(12.5)
        r.font.color.rgb = c_navy
        
        for line in content.split("\n"):
            p_line = tf.add_paragraph()
            p_line.alignment = PP_ALIGN.LEFT
            p_line.space_before = Pt(3)
            r_l = p_line.add_run()
            r_l.text = line
            r_l.font.size = Pt(9.8)
            r_l.font.color.rgb = c_dark

    # Competitive Advantage Summary Card
    adv_card = add_card(s6, Inches(0.8), Inches(4.88), Inches(11.733), Inches(2.2), bg_color=c_white, border_color=c_border)
    adv_tf = adv_card.text_frame
    adv_tf.word_wrap = True
    adv_tf.margin_left = Inches(0.28)
    adv_tf.margin_top = Inches(0.15)

    p_adv_h = adv_tf.paragraphs[0]
    p_adv_h.alignment = PP_ALIGN.LEFT
    r_ah = p_adv_h.add_run()
    r_ah.text = "Competitive Advantage Summary"
    r_ah.font.bold = True
    r_ah.font.size = Pt(13.5)
    r_ah.font.color.rgb = c_navy

    adv_bullets = [
        ("• Integrated Frontline Triad:", "Sahaaya uniquely unifies three essential frontline capabilities in one zero-network app: deterministic emergency danger escalation [1], offline MoHFW/NHM protocol retrieval with page citations [2], and clinical urgency-ranked household visit planning."),
        ("• Clinically Safe & Field-Hardened:", "Guarantees paramedical boundary safety via hard physician redirects on prescriptions [1, 6], operates 100% offline in zero-connectivity hamlets [4], and delivers instant 1-tap referral slips to eliminate fatal transport delays [3].")
    ]
    for title, desc in adv_bullets:
        add_bullet_point(adv_tf, title, desc, font_size=10.2, space_after=4)

    # =========================================================================
    # SLIDE 7: DEMO & POC VIDEO
    # =========================================================================
    s7 = prs.slides.add_slide(blank_layout)
    add_top_banner(s7, "Demo & POC Video")

    s7_h = s7.shapes.add_textbox(Inches(0.8), Inches(1.15), Inches(11.7), Inches(0.35))
    s7_tf = s7_h.text_frame
    s7_tf.margin_left = 0
    p = s7_tf.paragraphs[0]
    p.text = "Live Demonstration"
    p.font.name = "Calibri"
    p.font.size = Pt(16.5)
    p.font.bold = True
    p.font.color.rgb = c_navy

    # Video Embed Picture (16:9 ratio, width 8.2 in, height 4.61 in)
    vw = Inches(8.2)
    vh = Inches(4.61)
    vx = Inches(0.8) + (Inches(11.733) - vw) / 2
    vy = Inches(1.52)

    if os.path.exists(demo_img):
        # Clean rectangular border behind image
        card = s7.shapes.add_shape(MSO_SHAPE.RECTANGLE, vx - Inches(0.02), vy - Inches(0.02), vw + Inches(0.04), vh + Inches(0.04))
        card.fill.solid()
        card.fill.fore_color.rgb = c_white
        card.line.color.rgb = c_navy
        card.line.width = Pt(1.5)

        pic = s7.shapes.add_picture(demo_img, vx, vy, vw, vh)
        pic.click_action.hyperlink.address = "https://youtu.be/_Yz6aoMcbP4"

    # Action & Links Bar below Video (3 cleanly spaced clickable pill cards)
    bar_w = Inches(11.733)
    bar_left = Inches(0.8)
    bar_top = Inches(6.25)
    
    # 3 Separate Pill Cards for Links
    # Pill 1: YouTube Demo Link (Width: 4.6 in)
    p1_card = add_card(s7, bar_left, bar_top, Inches(4.6), Inches(0.55), bg_color=c_gray_bg, border_color=c_border)
    p1_card.click_action.hyperlink.address = "https://youtu.be/_Yz6aoMcbP4"
    p1_tf = p1_card.text_frame
    p1_tf.word_wrap = True
    p1_tf.margin_left = Inches(0.18)
    p1_tf.margin_top = Inches(0.1)
    p1 = p1_tf.paragraphs[0]
    p1.alignment = PP_ALIGN.LEFT
    r = p1.add_run()
    r.text = "YouTube Demo: "
    r.font.bold = True
    r.font.size = Pt(10.5)
    r.font.color.rgb = c_yt_red
    r2 = p1.add_run()
    r2.text = "https://youtu.be/_Yz6aoMcbP4"
    r2.font.bold = True
    r2.font.size = Pt(10.5)
    r2.font.color.rgb = c_link_blue
    r2.font.underline = True
    r2.hyperlink.address = "https://youtu.be/_Yz6aoMcbP4"

    # Pill 2: GitHub Repository (Width: 4.2 in)
    p2_card = add_card(s7, bar_left + Inches(4.75), bar_top, Inches(4.2), Inches(0.55), bg_color=c_gray_bg, border_color=c_border)
    p2_card.click_action.hyperlink.address = "https://github.com/yogeswar142/Sahaaya"
    p2_tf = p2_card.text_frame
    p2_tf.word_wrap = True
    p2_tf.margin_left = Inches(0.18)
    p2_tf.margin_top = Inches(0.1)
    p2 = p2_tf.paragraphs[0]
    p2.alignment = PP_ALIGN.LEFT
    r = p2.add_run()
    r.text = "GitHub: "
    r.font.bold = True
    r.font.size = Pt(10.5)
    r.font.color.rgb = c_navy
    r2 = p2.add_run()
    r2.text = "github.com/yogeswar142/Sahaaya"
    r2.font.size = Pt(10.5)
    r2.font.color.rgb = c_link_blue
    r2.font.underline = True
    r2.hyperlink.address = "https://github.com/yogeswar142/Sahaaya"

    # Pill 3: APK Download (Width: 2.63 in)
    p3_card = add_card(s7, bar_left + Inches(9.1), bar_top, Inches(2.633), Inches(0.55), bg_color=c_gray_bg, border_color=c_border)
    p3_card.click_action.hyperlink.address = "https://github.com/yogeswar142/Sahaaya/releases/latest"
    p3_tf = p3_card.text_frame
    p3_tf.word_wrap = True
    p3_tf.margin_left = Inches(0.18)
    p3_tf.margin_top = Inches(0.1)
    p3 = p3_tf.paragraphs[0]
    p3.alignment = PP_ALIGN.LEFT
    r = p3.add_run()
    r.text = "APK: "
    r.font.bold = True
    r.font.size = Pt(10.5)
    r.font.color.rgb = c_navy
    r2 = p3.add_run()
    r2.text = "v1.0.0 (16.9 MB)"
    r2.font.bold = True
    r2.font.size = Pt(10.5)
    r2.font.color.rgb = c_green
    r2.font.underline = True
    r2.hyperlink.address = "https://github.com/yogeswar142/Sahaaya/releases/latest"

    # Bottom POC benchmark text
    poc_box = s7.shapes.add_textbox(Inches(0.8), Inches(6.92), Inches(11.733), Inches(0.35))
    poc_tf = poc_box.text_frame
    poc_tf.margin_left = 0
    p_poc = poc_tf.paragraphs[0]
    p_poc.text = "Working Offline Android POC | Grounded on MoHFW/NHM Protocols [1-3] | 100% Danger Recall | Zero Cloud Permissions"
    p_poc.font.name = "Calibri"
    p_poc.font.size = Pt(10)
    p_poc.font.color.rgb = RGBColor(90, 90, 90)

    prs.save(out_pptx)
    file_size_mb = os.path.getsize(out_pptx) / (1024 * 1024)
    print(f"Successfully generated {out_pptx} ({file_size_mb:.2f} MB)")

if __name__ == "__main__":
    build_pptx()
