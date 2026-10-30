from PIL import Image, ImageDraw, ImageFont
import os

def create_mockup(mode="2-phone"):
    W, H = 2800, 1420
    bg_color = (255, 255, 255)
    canvas = Image.new("RGB", (W, H), bg_color)
    draw = ImageDraw.Draw(canvas)

    try:
        font_header_num = ImageFont.truetype("arialbd.ttf", 36)
        font_header_title = ImageFont.truetype("arialbd.ttf", 40)
        font_header_sub = ImageFont.truetype("arial.ttf", 26)
    except:
        font_header_num = ImageFont.load_default()
        font_header_title = ImageFont.load_default()
        font_header_sub = ImageFont.load_default()

    bezel_color = (15, 23, 42) # #0F172A

    def frame_phone(img, target_h):
        w_orig, h_orig = img.size
        aspect = w_orig / h_orig
        target_w = int(target_h * aspect)
        resized = img.resize((target_w, target_h), Image.Resampling.LANCZOS)

        padding = 16
        phone_w = target_w + padding * 2
        phone_h = target_h + padding * 2
        phone = Image.new("RGBA", (phone_w, phone_h), (0, 0, 0, 0))
        p_draw = ImageDraw.Draw(phone)

        # Outer rounded body
        p_draw.rounded_rectangle([0, 0, phone_w, phone_h], radius=44, fill=(245, 247, 250), outline=bezel_color, width=8)

        # Screen mask
        mask = Image.new("L", (target_w, target_h), 0)
        m_draw = ImageDraw.Draw(mask)
        m_draw.rounded_rectangle([0, 0, target_w, target_h], radius=32, fill=255)

        phone.paste(resized, (padding, padding), mask)
        return phone

    if mode == "2-phone":
        s1 = Image.open("docs/screen1_ask_raw.png")
        s2 = Image.open("docs/screen2_visits_raw.png")

        phone_h = 1180
        p1 = frame_phone(s1, phone_h)
        p2 = frame_phone(s2, phone_h)

        # Spread the two phones with generous gap
        gap = 460
        total_w = p1.width + p2.width + gap
        x1 = (W - total_w) // 2
        x2 = x1 + p1.width + gap

        # Phone 1 Header
        c1 = x1 + p1.width // 2
        t1_title = "SCREEN 1: ASK SAHAAYA"
        t1_sub = "Offline Voice & Deterministic Protocol Search"
        b1 = draw.textbbox((0, 0), t1_title, font=font_header_title)
        draw.text((c1 - (b1[2] - b1[0]) // 2, 38), t1_title, fill=(31, 78, 140), font=font_header_title)
        b1_s = draw.textbbox((0, 0), t1_sub, font=font_header_sub)
        draw.text((c1 - (b1_s[2] - b1_s[0]) // 2, 90), t1_sub, fill=(100, 116, 139), font=font_header_sub)

        # Phone 2 Header
        c2 = x2 + p2.width // 2
        t2_title = "SCREEN 2: TODAY'S VISITS"
        t2_sub = "Prioritised Household Action Plan & Triage"
        b2 = draw.textbbox((0, 0), t2_title, font=font_header_title)
        draw.text((c2 - (b2[2] - b2[0]) // 2, 38), t2_title, fill=(31, 78, 140), font=font_header_title)
        b2_s = draw.textbbox((0, 0), t2_sub, font=font_header_sub)
        draw.text((c2 - (b2_s[2] - b2_s[0]) // 2, 90), t2_sub, fill=(100, 116, 139), font=font_header_sub)

        canvas.paste(p1, (x1, 145), p1)
        canvas.paste(p2, (x2, 145), p2)

        out_path = "docs/app_mockup_2phone.png"
        canvas.save(out_path, dpi=(300, 300))
        print(f"Saved {out_path}")

    elif mode == "3-phone":
        s1 = Image.open("docs/screen1_ask_raw.png")
        s_em = Image.open("docs/screen1_ask_emergency_card.png")
        s2 = Image.open("docs/screen2_visits_raw.png")

        phone_h = 1180
        p1 = frame_phone(s1, phone_h)
        p_em = frame_phone(s_em, phone_h)
        p2 = frame_phone(s2, phone_h)

        gap = 140
        total_w = p1.width + p_em.width + p2.width + gap * 2
        x1 = (W - total_w) // 2
        x_em = x1 + p1.width + gap
        x2 = x_em + p_em.width + gap

        # Phone 1
        c1 = x1 + p1.width // 2
        t1_title = "SCREEN 1: SEARCH & VOICE"
        t1_sub = "Offline Trilingual Voice & Text"
        b1 = draw.textbbox((0, 0), t1_title, font=font_header_title)
        draw.text((c1 - (b1[2] - b1[0]) // 2, 38), t1_title, fill=(31, 78, 140), font=font_header_title)
        b1_s = draw.textbbox((0, 0), t1_sub, font=font_header_sub)
        draw.text((c1 - (b1_s[2] - b1_s[0]) // 2, 90), t1_sub, fill=(100, 116, 139), font=font_header_sub)

        # Phone 2 (Emergency Escalation)
        c_em = x_em + p_em.width // 2
        t_em_title = "SCREEN 2: SAFETY ESCALATION"
        t_em_sub = "100% Danger Recall + 108 Slip"
        b_em = draw.textbbox((0, 0), t_em_title, font=font_header_title)
        draw.text((c_em - (b_em[2] - b_em[0]) // 2, 38), t_em_title, fill=(183, 28, 28), font=font_header_title)
        b_em_s = draw.textbbox((0, 0), t_em_sub, font=font_header_sub)
        draw.text((c_em - (b_em_s[2] - b_em_s[0]) // 2, 90), t_em_sub, fill=(100, 116, 139), font=font_header_sub)

        # Phone 3
        c2 = x2 + p2.width // 2
        t2_title = "SCREEN 3: TODAY'S VISITS"
        t2_sub = "Urgency Triage & Action Plan"
        b2 = draw.textbbox((0, 0), t2_title, font=font_header_title)
        draw.text((c2 - (b2[2] - b2[0]) // 2, 38), t2_title, fill=(31, 78, 140), font=font_header_title)
        b2_s = draw.textbbox((0, 0), t2_sub, font=font_header_sub)
        draw.text((c2 - (b2_s[2] - b2_s[0]) // 2, 90), t2_sub, fill=(100, 116, 139), font=font_header_sub)

        canvas.paste(p1, (x1, 145), p1)
        canvas.paste(p_em, (x_em, 145), p_em)
        canvas.paste(p2, (x2, 145), p2)

        out_path = "docs/app_mockup_3phone.png"
        canvas.save(out_path, dpi=(300, 300))
        print(f"Saved {out_path}")

if __name__ == "__main__":
    create_mockup("2-phone")
    create_mockup("3-phone")
