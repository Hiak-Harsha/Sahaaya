import pymupdf
import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

def ensure_demo_embed_image():
    out_path = "docs/p7_demo_embed.png"
    banner_path = "docs/Banner.png"
    if not os.path.exists(banner_path):
        return out_path
    
    # Target video screen size: 16:9 ratio (1400 x 788)
    banner = Image.open(banner_path).convert("RGBA")
    vw, vh = 1400, 788
    video_frame = banner.resize((vw, vh), Image.Resampling.LANCZOS)

    # Dark gradient overlays for top title and bottom scrubber
    overlay = Image.new("RGBA", (vw, vh), (0, 0, 0, 0))
    draw = ImageDraw.Draw(overlay)
    for y in range(120):
        alpha = int(170 * (1 - y / 120))
        draw.line([(0, y), (vw, y)], fill=(0, 0, 0, alpha))
    for y in range(80):
        alpha = int(150 * (y / 80))
        draw.line([(0, vh - 80 + y), (vw, vh - 80 + y)], fill=(0, 0, 0, alpha))
    video_frame = Image.alpha_composite(video_frame, overlay)

    # Center YouTube Play Button
    btn_w, btn_h = 150, 102
    btn_x = (vw - btn_w) // 2
    btn_y = (vh - btn_h) // 2

    # Shadow for play button
    shadow = Image.new("RGBA", (vw, vh), (0, 0, 0, 0))
    s_draw = ImageDraw.Draw(shadow)
    s_draw.rounded_rectangle([btn_x - 4, btn_y - 2, btn_x + btn_w + 4, btn_y + btn_h + 6], radius=30, fill=(0, 0, 0, 160))
    shadow = shadow.filter(ImageFilter.GaussianBlur(10))
    video_frame = Image.alpha_composite(video_frame, shadow)

    # Draw YouTube Red Button
    draw = ImageDraw.Draw(video_frame)
    draw.rounded_rectangle([btn_x, btn_y, btn_x + btn_w, btn_y + btn_h], radius=28, fill=(255, 0, 0, 240))

    # White Play Triangle
    tri_w, tri_h = 42, 48
    tx = btn_x + (btn_w - tri_w) // 2 + 4
    ty = btn_y + (btn_h - tri_h) // 2
    draw.polygon([(tx, ty), (tx, ty + tri_h), (tx + tri_w, ty + tri_h // 2)], fill=(255, 255, 255, 255))

    # Top Title Badge
    try:
        font_title = ImageFont.truetype("arialbd.ttf", 26)
        font_sub = ImageFont.truetype("arial.ttf", 17)
    except:
        font_title = ImageFont.load_default()
        font_sub = font_title

    draw.rounded_rectangle([30, 24, 760, 84], radius=12, fill=(0, 0, 0, 190))
    draw.ellipse([48, 44, 60, 56], fill=(255, 60, 60, 255))
    draw.text((72, 34), "Sahaaya - Frontline Health-Worker Companion", font=font_title, fill=(255, 255, 255, 255))
    draw.text((72, 62), "Official Live Field Demonstration & Benchmarks  |  YouTube: youtu.be/_Yz6aoMcbP4", font=font_sub, fill=(220, 230, 245, 255))

    # Bottom YouTube Scrubber
    draw.line([(0, vh - 6), (vw, vh - 6)], fill=(80, 80, 80, 200), width=6)
    draw.line([(0, vh - 6), (360, vh - 6)], fill=(255, 0, 0, 255), width=6)
    draw.ellipse([355, vh - 11, 365, vh - 1], fill=(255, 0, 0, 255))

    # Bottom Right Pill
    draw.rounded_rectangle([vw - 260, vh - 62, vw - 30, vh - 22], radius=10, fill=(0, 0, 0, 190))
    draw.text((vw - 242, vh - 52), "Watch on YouTube ->", font=font_sub, fill=(255, 255, 255, 255))

    video_frame = video_frame.convert("RGB")
    video_frame.save(out_path, quality=95)
    return out_path

def build_pdf():
    src_pdf = "scripts/template.pdf" if os.path.exists("scripts/template.pdf") else "Round1_Template submission.pdf"
    out_pdf = "TEAM-001245_Sahaaya_Round1_Submission.pdf"
    
    doc = pymupdf.open(src_pdf)
    print(f"Loaded {src_pdf} with {len(doc)} pages.")

    # Colors
    c_white = (1, 1, 1)
    c_slide1_bg = (0.1216, 0.2745, 0.5294) # Exact background RGB of slide 1 (31, 70, 135)
    c_dark_blue = (0.1216, 0.3059, 0.5490) # #1f4e8c
    c_text_dark = (0.15, 0.15, 0.15)
    c_green = (0.18, 0.49, 0.20)

    def draw_bullet_block(page, x, y, max_w, title, desc, title_color=c_dark_blue, font_size=8.4, line_height=10.4, spacing=3.0):
        page.insert_text((x, y), title, fontname="hebo", fontsize=font_size, color=title_color)
        y += line_height
        
        desc_x = x + 10
        desc_max_w = max_w - 10
        words = desc.strip().split()
        line = ""
        for w in words:
            test_line = (line + " " + w) if line else w
            if pymupdf.get_text_length(test_line, fontname="helv", fontsize=font_size) < desc_max_w:
                line = test_line
            else:
                if line:
                    page.insert_text((desc_x, y), line, fontname="helv", fontsize=font_size, color=c_text_dark)
                    y += line_height
                    line = w
                else:
                    page.insert_text((desc_x, y), test_line, fontname="helv", fontsize=font_size, color=c_text_dark)
                    y += line_height
                    line = ""
        if line:
            page.insert_text((desc_x, y), line, fontname="helv", fontsize=font_size, color=c_text_dark)
            y += line_height
        return y + spacing

    # -------------------------------------------------------------
    # PAGE 1: TITLE SLIDE
    # -------------------------------------------------------------
    p1 = doc[0]
    p1.add_redact_annot(pymupdf.Rect(75, 230, 320, 255), fill=c_slide1_bg)
    p1.add_redact_annot(pymupdf.Rect(400, 230, 640, 255), fill=c_slide1_bg)
    p1.add_redact_annot(pymupdf.Rect(75, 295, 395, 320), fill=c_slide1_bg)
    p1.add_redact_annot(pymupdf.Rect(400, 295, 640, 320), fill=c_slide1_bg)
    p1.apply_redactions()

    p1.insert_text((79.3, 246), "Vizag Mavericks (TEAM-001245)", fontsize=13, color=c_white, fontname="helv")
    p1.insert_text((403.6, 246), "Amara Yogeswararao", fontsize=13, color=c_white, fontname="helv")
    p1.insert_text((79.3, 310), "H5. Frontline Health-Worker Companion for Low-Connectivity Areas", fontsize=9.6, color=c_white, fontname="helv")
    p1.insert_text((403.6, 310), "18/10/2026", fontsize=13, color=c_white, fontname="helv")

    # -------------------------------------------------------------
    # -------------------------------------------------------------
    # PAGE 2: USE CASE (Problem Statement & Market Opportunity)
    # -------------------------------------------------------------
    p2 = doc[1]
    p2.add_redact_annot(pymupdf.Rect(45, 110, 700, 230), fill=c_white)
    p2.add_redact_annot(pymupdf.Rect(45, 260, 700, 400), fill=c_white)
    p2.apply_redactions()

    prob_bullets = [
        ("• Frontline Overload & Zero Mobile Reception:", "India's 10.29 lakh ASHAs and 2.5 lakh ANMs manage up to 28 paper registers [5]; in Andhra Pradesh alone, 1,700+ remote villages (1,000+ in ASR district) have complete zero mobile reception [4]."),
        ("• Preventable Maternal & Neonatal Mortality:", "Obstetric haemorrhage causes 47% of maternal deaths [3]; unrecognised newborn danger signs (hypothermia, convulsions, chest indrawing) lead to fatal referral delays [1, 2]."),
        ("• AI Safety & Frontline Malpractice Risks:", "General-purpose AI can produce inaccurate or unsafe medical guidance without appropriate safeguards, risking non-prescribing ASHA boundary violations [1], failing offline, and lacking WHO clinical guardrails [6].")
    ]
    y = 120
    for title, desc in prob_bullets:
        y = draw_bullet_block(p2, 55, y, 615, title, desc, font_size=8.1, line_height=10.0, spacing=2.5)

    mkt_bullets = [
        ("• Frontline Scale & National Need:", "10.29 lakh ASHAs and ~2.5 lakh Sub-Centre ANMs across India serving 1,000 population norms, requiring an intuitive, reliable paramedical companion [1, 2]."),
        ("• Critical Gap in Existing Systems:", "Existing state tools (ANMOL, Kilkari) focus on administrative reporting or outbound audio broadcasts; they rely on mobile networks [4] and lack on-demand offline clinical danger triage or strict scope gating [5]."),
        ("• Sahaaya High-Impact Value:", "100% offline, protocol-grounded companion: deterministic danger-sign escalation [1], physician redirects [6], prioritized visits, emergency referral slips, and 2G SMS field sync [4].")
    ]
    y = 265
    for title, desc in mkt_bullets:
        y = draw_bullet_block(p2, 55, y, 615, title, desc, font_size=8.1, line_height=10.0, spacing=2.5)

    # Reference Citations Box on Slide 2
    ref_box = pymupdf.Rect(45, 348, 675, 400)
    p2.draw_rect(ref_box, fill=(0.955, 0.968, 0.985), color=(0.80, 0.84, 0.90), width=0.75)
    p2.insert_text((52, 356.5), "Official Protocol Sources & Clinical Evidence Citations:", fontname="hebo", fontsize=7.2, color=c_dark_blue)

    citations = [
        ("[1] MoHFW National Guidelines:", "Revised India MCP Card (2018) & ASHA Modules 6-7", "https://hbnc-hbyc.mohfw.gov.in/assets/document/Revised%20India%20MCP%20Card%20English_(28-05-2018).pdf", 52, 368),
        ("[2] NHM Clinical Guidance:", "ASHA Induction Module (pp. 51-97) & STOP Diarrhoea", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", 52, 378),
        ("[3] PMC9292773 Study:", "Obstetric haemorrhage causes 47% maternal deaths in India", "https://pmc.ncbi.nlm.nih.gov/articles/PMC9292773/", 52, 388),
        ("[4] TelecomTalk AP Survey:", "1,700 remote hamlets (ASR dist) zero cellular signal", "https://telecomtalk.info/?p=654511", 365, 368),
        ("[5] New Lines / IJCMPH 2024:", "Frontline overload (28 paper registers, 23 apps)", "https://newlinesmag.com/reportage/indias-digital-health-push-is-overworking-its-front-line-women/", 365, 378),
        ("[6] WHO AI Safety Standards:", "Multi-modal AI guidance for clinical decision support", "https://www.downtoearth.org.in/science-technology/who-releases-guidelines-for-multi-modal-generative-ai-in-healthcare-resonates-with-recommendations-for-other-sectors-93991", 365, 388),
    ]

    for tag, desc, url, cx, cy in citations:
        p2.insert_text((cx, cy), tag, fontname="hebo", fontsize=6.8, color=c_dark_blue)
        tag_w = pymupdf.get_text_length(tag + " ", fontname="hebo", fontsize=6.8)
        p2.insert_text((cx + tag_w, cy), desc, fontname="helv", fontsize=6.8, color=c_text_dark)
        tot_w = tag_w + pymupdf.get_text_length(desc, fontname="helv", fontsize=6.8)
        p2.insert_link({"kind": pymupdf.LINK_URI, "from": pymupdf.Rect(cx, cy - 6.5, cx + tot_w, cy + 2.5), "uri": url})

    # -------------------------------------------------------------
    # PAGE 3: PROPOSED SOLUTION (Overview & Mockup Image)
    # -------------------------------------------------------------
    p3 = doc[2]
    p3.add_redact_annot(pymupdf.Rect(45, 110, 700, 205), fill=c_white)
    p3.add_redact_annot(pymupdf.Rect(36, 204, 684, 395), fill=c_white)
    p3.apply_redactions()

    sol_bullets = [
        ("• Deterministic Danger Escalation + Referral Slip:", "Scans queries across English, Hinglish & Telugu-Latin; triggers red screen (100% recall on 367 phrasings [1, 2]) + 1-tap 108 Emergency Referral Slip."),
        ("• Strict Non-Prescribing Scope Gate:", "Hard-blocks antibiotic & dosage questions with physician redirects, ensuring paramedical workers operate strictly within ASHA guidelines [1, 6]."),
        ("• Extractive BM25 Protocol Retrieval:", "Pure Kotlin BM25 matches questions to official MoHFW/NHM manuals [1, 2] with exact document & page citations (0.53 ms latency, 0% hallucination)."),
        ("• Prioritised Daily Household Action Plan & 2G Sync:", "Triage ranking: Danger Flags > Overdue Follow-ups > Routine Due Dates + 1-Tap End-of-Day PHC Summary Report (transmissible via 2G SMS [4, 5]).")
    ]
    y = 118
    for title, desc in sol_bullets:
        y = draw_bullet_block(p3, 55, y, 615, title, desc, font_size=7.8, line_height=9.8, spacing=2.0)

    mockup_img = "docs/app_mockup.png"
    if os.path.exists(mockup_img):
        p3.draw_rect(pymupdf.Rect(45, 204, 675, 395), fill=c_white, color=(0.82, 0.82, 0.82), width=1)
        p3.insert_image(pymupdf.Rect(48, 206, 672, 393), filename=mockup_img)

    # -------------------------------------------------------------
    # PAGE 4: DESIGN STEPS & ARCHITECTURE
    # -------------------------------------------------------------
    p4 = doc[3]
    p4.add_redact_annot(pymupdf.Rect(45, 105, 350, 398), fill=c_white)
    p4.add_redact_annot(pymupdf.Rect(350, 75, 695, 398), fill=c_white)
    p4.apply_redactions()

    # Re-draw Architecture Diagram Header aligned with wider diagram
    p4.insert_text((355, 96), "Architecture Diagram", fontname="hebo", fontsize=13.5, color=c_dark_blue)
    p4.insert_text((495, 96), "(Safety-First & Parallel Workflows)", fontname="helv", fontsize=8.8, color=(0.4, 0.45, 0.5))

    steps = [
        ("1. Input & Speech Recognition:", "Trilingual voice mic and text (EN, Hindi, Telugu-Latin) for instant frontline accessibility."),
        ("2. Danger-Sign Rule Engine [Current]:", "Deterministic regex on 27 danger signs (100% recall [1, 2]); immediately triggers Red Alert + 108 Referral Slip."),
        ("3. Intent Safety Gate [Current]:", "Intercepts dosage & prescription queries; enforces non-prescribing boundaries [1, 6] with physician redirects."),
        ("4. Offline BM25 Retrieval [Current]:", "Pure Kotlin search over local on-device MoHFW/NHM protocol chunks; exact page citations (0% hallucination)."),
        ("5. Parallel Household Engine [Current]:", "Ingests offline household register -> Urgency triage (Danger > Overdue > Scheduled) -> Daily Prioritised Visit List."),
        ("6. Planned Extensions [Roadmap]:", "On-device SLM rephraser for natural conversational synthesis; automated 2G SMS field summary sync.")
    ]
    y = 118
    for title, desc in steps:
        y = draw_bullet_block(p4, 50, y, 290, title, desc, font_size=7.8, line_height=9.8, spacing=3.0)

    arch_img = "docs/architecture_diagram.png"
    if os.path.exists(arch_img):
        # Image is 1536x1024 (aspect ratio 1.5). Width 336 pt -> Height 224 pt.
        img_rect = pymupdf.Rect(354, 122, 690, 346)
        border_rect = pymupdf.Rect(352, 120, 692, 348)
        p4.draw_rect(border_rect, fill=c_white, color=(0.82, 0.84, 0.88), width=1)
        p4.insert_image(img_rect, filename=arch_img)


    # -------------------------------------------------------------
    # PAGE 5: CHALLENGES & MITIGATION
    # -------------------------------------------------------------
    p5 = doc[4]
    p5.add_redact_annot(pymupdf.Rect(45, 105, 700, 168), fill=c_white)
    p5.add_redact_annot(pymupdf.Rect(45, 188, 700, 254), fill=c_white)
    p5.add_redact_annot(pymupdf.Rect(45, 275, 700, 385), fill=c_white)
    p5.apply_redactions()

    def draw_challenge_block(page, y_start, prob_desc, mit_desc):
        page.insert_text((55, y_start), "Problem: ", fontname="hebo", fontsize=8.2, color=c_dark_blue)
        words = prob_desc.strip().split()
        line = ""
        y = y_start
        cur_x = 100
        for w in words:
            test = line + " " + w if line else w
            if cur_x + pymupdf.get_text_length(test, fontname="helv", fontsize=8.0) < 665:
                line = test
            else:
                page.insert_text((cur_x, y), line, fontname="helv", fontsize=8.0, color=c_text_dark)
                y += 10.0
                cur_x = 65
                line = w
        if line:
            page.insert_text((cur_x, y), line, fontname="helv", fontsize=8.0, color=c_text_dark)
            y += 11.5

        page.insert_text((55, y), "Mitigation: ", fontname="hebo", fontsize=8.2, color=c_green)
        cur_x = 106
        words = mit_desc.strip().split()
        line = ""
        for w in words:
            test = line + " " + w if line else w
            if cur_x + pymupdf.get_text_length(test, fontname="helv", fontsize=8.0) < 665:
                line = test
            else:
                page.insert_text((cur_x, y), line, fontname="helv", fontsize=8.0, color=c_text_dark)
                y += 10.0
                cur_x = 65
                line = w
        if line:
            page.insert_text((cur_x, y), line, fontname="helv", fontsize=8.0, color=c_text_dark)

    draw_challenge_block(
        p5, 114,
        "Zero mobile reception in rural hamlets and severe RAM/battery constraints on frontline smartphones.",
        "100% on-device architecture with zero network permissions. Measured across 30 diagnostic queries on physical hardware (OnePlus CPH2613, Android 15 in Airplane Mode) and standalone JVM: APK size is 16.9 MB, median query latency is 0.53 ms (P95: 5.38 ms), and peak heap RAM footprint is < 1 MB."
    )
    draw_challenge_block(
        p5, 196,
        "Medical hallucination risks and catastrophic delays from missed pregnancy and newborn danger signs.",
        "Deterministic danger rules execute BEFORE any retrieval logic. Tested across a 367-phrase benchmark derived from 27 MoHFW/NHM emergency signs across English, Hinglish & Telugu-Latin colloquial variants: achieved 100% recall on the test set, triggering instant Red Alerts + 108 referral slips without retrieval delay."
    )
    draw_challenge_block(
        p5, 280,
        "Code-mixed vernacular queries (English, Hinglish, Telugu-Latin) with dialectal variations, typos, and paper register friction.",
        "[Implemented]: Robust phonetic tokenisation, code-mixed synonym mapping, and clinical urgency triage (Danger > Overdue > Scheduled) with 1-tap referral slips. [Planned]: On-device SLM rephraser using pre-cached organizer models and background 2G SMS gateway sync for daily PHC reporting."
    )

    # -------------------------------------------------------------
    # PAGE 6: UNIQUE SELLING PROPOSITION (USP)
    # -------------------------------------------------------------
    p6 = doc[5]
    p6.add_redact_annot(pymupdf.Rect(45, 118, 258, 250), fill=c_white)
    p6.add_redact_annot(pymupdf.Rect(270, 118, 483, 250), fill=c_white)
    p6.add_redact_annot(pymupdf.Rect(495, 118, 708, 250), fill=c_white)
    p6.add_redact_annot(pymupdf.Rect(45, 288, 700, 398), fill=c_white)
    p6.apply_redactions()

    d1_lines = [
        ("Deterministic Safety Pipeline:", True),
        ("Emergency rules execute BEFORE", False),
        ("any AI logic; 100% danger recall on", False),
        ("367 benchmark phrasings [1, 2]. Hard", False),
        ("non-prescribing gate [6] with extractive,", False),
        ("source-cited responses & 108 slips.", False)
    ]
    y = 133
    for text, is_bold in d1_lines:
        p6.insert_text((55, y), text, fontsize=8.0, fontname="hebo" if is_bold else "helv", color=c_dark_blue if is_bold else c_text_dark)
        y += 12.0

    d2_lines = [
        ("100% Offline & Measured Speed:", True),
        ("Zero network permissions (built for [4]).", False),
        ("OnePlus CPH2613 (Airplane Mode):", False),
        ("• Median Query Latency: 0.53 ms", False),
        ("• P95 Benchmark Latency: 5.38 ms", False),
        ("• APK: 16.9 MB | Peak RAM: < 1 MB", False),
        ("Runs natively without SIM or signal.", False)
    ]
    y = 133
    for text, is_bold in d2_lines:
        p6.insert_text((280, y), text, fontsize=8.0, fontname="hebo" if is_bold else "helv", color=c_dark_blue if is_bold else c_text_dark)
        y += 12.0

    d3_lines = [
        ("Prioritised Household Action Plan:", True),
        ("Transforms 30+ static paper records", False),
        ("into an actionable daily visit list.", False),
        ("Ensures high-risk pregnant women &", False),
        ("unimmunized infants are visited first", False),
        ("(Danger > Overdue > Scheduled).", False),
        ("[Planned Extension: 2G SMS sync].", False)
    ]
    y = 133
    for text, is_bold in d3_lines:
        p6.insert_text((505, y), text, fontsize=8.0, fontname="hebo" if is_bold else "helv", color=c_dark_blue if is_bold else c_text_dark)
        y += 12.0

    adv_bullets = [
        ("• Integrated Frontline Triad:", "Sahaaya uniquely unifies three essential frontline capabilities in one zero-network app: deterministic emergency danger escalation [1], offline MoHFW/NHM protocol retrieval with page citations [2], and clinical urgency-ranked household visit planning."),
        ("• Clinically Safe & Field-Hardened:", "Guarantees paramedical boundary safety via hard physician redirects on prescriptions [1, 6], operates 100% offline in zero-connectivity hamlets [4], and delivers instant 1-tap referral slips to eliminate fatal transport delays [3].")
    ]
    y = 296
    for title, desc in adv_bullets:
        y = draw_bullet_block(p6, 55, y, 615, title, desc, font_size=8.1, line_height=10.0, spacing=2.5)

    # -------------------------------------------------------------
    # PAGE 7: DEMO & POC VIDEO
    # -------------------------------------------------------------
    p7 = doc[6]
    c_yt_red = (0.80, 0.05, 0.05)
    
    # Redact the old template box starting at y=112 (leaving Live Demonstration at y=83-104 untouched)
    p7.add_redact_annot(pymupdf.Rect(100, 112, 620, 398), fill=c_white)
    p7.apply_redactions()

    # Ensure demo embed banner card is generated
    demo_card_img = ensure_demo_embed_image()

    # Video Embed Rect (16:9 ratio, width 426, height 240)
    # Centered horizontally: (720 - 426)/2 = 147 pt
    img_rect = pymupdf.Rect(147, 106, 573, 346)
    border_rect = pymupdf.Rect(145, 104, 575, 348)
    p7.draw_rect(border_rect, fill=c_white, color=(0.12, 0.31, 0.55), width=1.2)
    if os.path.exists(demo_card_img):
        p7.insert_image(img_rect, filename=demo_card_img)

    # Clickable link on the whole video embed
    p7.insert_link({"kind": pymupdf.LINK_URI, "from": border_rect, "uri": "https://youtu.be/_Yz6aoMcbP4"})

    # Action & Links bar below video
    bar_rect = pymupdf.Rect(55, 353, 665, 375)
    p7.draw_rect(bar_rect, fill=(0.955, 0.968, 0.985), color=(0.80, 0.84, 0.90), width=0.75)

    # 1. YouTube Link (Left)
    p7.insert_text((68, 367), "YouTube Demo: ", fontname="hebo", fontsize=8.4, color=c_yt_red)
    lbl_w1 = pymupdf.get_text_length("YouTube Demo: ", fontname="hebo", fontsize=8.4)
    yt_url_text = "https://youtu.be/_Yz6aoMcbP4"
    p7.insert_text((68 + lbl_w1, 367), yt_url_text, fontname="hebo", fontsize=8.4, color=(0.0, 0.35, 0.75))
    url_w1 = pymupdf.get_text_length(yt_url_text, fontname="hebo", fontsize=8.4)
    p7.insert_link({"kind": pymupdf.LINK_URI, "from": pymupdf.Rect(68, 356, 68 + lbl_w1 + url_w1, 371), "uri": "https://youtu.be/_Yz6aoMcbP4"})

    # 2. GitHub Link (Center)
    p7.insert_text((315, 367), "GitHub: ", fontname="hebo", fontsize=8.4, color=c_dark_blue)
    lbl_w2 = pymupdf.get_text_length("GitHub: ", fontname="hebo", fontsize=8.4)
    gh_url_text = "github.com/yogeswar142/Sahaaya"
    p7.insert_text((315 + lbl_w2, 367), gh_url_text, fontname="helv", fontsize=8.4, color=(0.0, 0.35, 0.75))
    url_w2 = pymupdf.get_text_length(gh_url_text, fontname="helv", fontsize=8.4)
    p7.insert_link({"kind": pymupdf.LINK_URI, "from": pymupdf.Rect(315, 356, 315 + lbl_w2 + url_w2, 371), "uri": "https://github.com/yogeswar142/Sahaaya"})

    # 3. APK Release Link (Right)
    p7.insert_text((535, 367), "APK: ", fontname="hebo", fontsize=8.4, color=c_dark_blue)
    lbl_w3 = pymupdf.get_text_length("APK: ", fontname="hebo", fontsize=8.4)
    apk_text = "v1.0.0 (16.9 MB)"
    p7.insert_text((535 + lbl_w3, 367), apk_text, fontname="hebo", fontsize=8.4, color=c_green)
    apk_w = pymupdf.get_text_length(apk_text, fontname="hebo", fontsize=8.4)
    p7.insert_link({"kind": pymupdf.LINK_URI, "from": pymupdf.Rect(535, 356, 535 + lbl_w3 + apk_w, 371), "uri": "https://github.com/yogeswar142/Sahaaya/releases/latest"})

    # Bottom POC Points
    poc_points = "Working Offline Android POC | Grounded on MoHFW/NHM Protocols [1-3] | 100% Danger Recall | Zero Cloud Permissions"
    p7.insert_text((68, 390), poc_points, fontsize=7.6, fontname="helv", color=(0.35, 0.35, 0.35))

    doc.save(out_pdf)
    doc.close()
    print(f"Successfully generated {out_pdf}.")

if __name__ == "__main__":
    build_pdf()
