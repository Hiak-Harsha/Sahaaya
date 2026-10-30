"""
Enhanced, polished vector-grade logo generator for Sahaaya.
Uses cubic Bezier curves and smooth vector geometry for a clean,
minimalist maternal & child healthcare brand mark.
"""

import os
import math
from PIL import Image, ImageDraw

def bezier_point(p0, p1, p2, p3, t):
    mt = 1.0 - t
    mt2 = mt * mt
    mt3 = mt2 * mt
    t2 = t * t
    t3 = t2 * t
    x = mt3 * p0[0] + 3 * mt2 * t * p1[0] + 3 * mt * t2 * p2[0] + t3 * p3[0]
    y = mt3 * p0[1] + 3 * mt2 * t * p1[1] + 3 * mt * t2 * p2[1] + t3 * p3[1]
    return (x, y)

def draw_bezier_stroke(draw, p0, p1, p2, p3, width, color, steps=100):
    points = [bezier_point(p0, p1, p2, p3, i / float(steps)) for i in range(steps + 1)]
    for i in range(len(points) - 1):
        draw.line([points[i], points[i + 1]], fill=color, width=width)
    # Draw round caps at endpoints
    r = width / 2.0
    draw.ellipse([p0[0] - r, p0[1] - r, p0[0] + r, p0[1] + r], fill=color)
    draw.ellipse([p3[0] - r, p3[1] - r, p3[0] + r, p3[1] + r], fill=color)

def generate_sahaaya_brand_logo(size=1024, is_round=False, for_foreground=False):
    scale = 3  # 3x supersampling for ultra-crisp antialiasing
    c_size = size * scale
    img = Image.new("RGBA", (c_size, c_size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    cx = c_size / 2.0
    cy = c_size / 2.0

    # Colors
    bg_deep_teal = (15, 118, 110, 255)   # #0F766E
    bg_edge_teal = (17, 94, 89, 255)     # #115E59
    accent_mint  = (45, 212, 191, 255)   # #2DD4BF
    accent_light = (204, 251, 241, 255)  # #CCFBF1
    pure_white   = (255, 255, 255, 255)
    coral_red    = (239, 68, 68, 255)    # #EF4444

    # Safe margin & background
    if not for_foreground:
        bg_radius = c_size * 0.45 if is_round else c_size * 0.44
        rect = [cx - bg_radius, cy - bg_radius, cx + bg_radius, cy + bg_radius]
        
        # Soft drop shadow
        s_offset = int(c_size * 0.02)
        s_rect = [cx - bg_radius, cy - bg_radius + s_offset, cx + bg_radius, cy + bg_radius + s_offset]
        if is_round:
            draw.ellipse(s_rect, fill=(0, 25, 25, 60))
            draw.ellipse(rect, fill=bg_deep_teal)
            # Refined border
            draw.ellipse(rect, outline=accent_mint, width=int(c_size * 0.012))
        else:
            corner_r = int(c_size * 0.22)
            draw.rounded_rectangle(s_rect, radius=corner_r, fill=(0, 25, 25, 60))
            draw.rounded_rectangle(rect, radius=corner_r, fill=bg_deep_teal)
            draw.rounded_rectangle(rect, radius=corner_r, outline=accent_mint, width=int(c_size * 0.012))

    # Foreground scale factor (for adaptive icon safe zone)
    m_scale = 0.72 if for_foreground else 0.95
    center_y = cy - (c_size * 0.02 * m_scale)

    # 1. Mother's head (radiant white circle)
    m_r = 135 * m_scale * scale
    m_head_pos = (cx - (80 * m_scale * scale), center_y - (150 * m_scale * scale))
    draw.ellipse([m_head_pos[0] - m_r, m_head_pos[1] - m_r,
                  m_head_pos[0] + m_r, m_head_pos[1] + m_r], fill=pure_white)

    # 2. Baby's head (soft mint/teal circle nestled safely)
    b_r = 95 * m_scale * scale
    b_head_pos = (cx + (90 * m_scale * scale), center_y - (20 * m_scale * scale))
    draw.ellipse([b_head_pos[0] - b_r, b_head_pos[1] - b_r,
                  b_head_pos[0] + b_r, b_head_pos[1] + b_r], fill=accent_light)

    # 3. Outer Embracing Arms / Wings (forming a protective heart & cradle)
    # Left arm: cradling mother and baby from the left base up to the mother's shoulder
    arm_w = int(48 * m_scale * scale)
    
    # Left protective curve (pure white)
    p0_L = (cx - (260 * m_scale * scale), center_y - (60 * m_scale * scale))
    p1_L = (cx - (280 * m_scale * scale), center_y + (160 * m_scale * scale))
    p2_L = (cx - (130 * m_scale * scale), center_y + (280 * m_scale * scale))
    p3_L = (cx,                           center_y + (290 * m_scale * scale))
    draw_bezier_stroke(draw, p0_L, p1_L, p2_L, p3_L, arm_w, pure_white)

    # Right protective curve (accent mint) cradling the baby upwards
    p0_R = (cx,                           center_y + (290 * m_scale * scale))
    p1_R = (cx + (140 * m_scale * scale), center_y + (280 * m_scale * scale))
    p2_R = (cx + (260 * m_scale * scale), center_y + (180 * m_scale * scale))
    p3_R = (cx + (240 * m_scale * scale), center_y + (20 * m_scale * scale))
    draw_bezier_stroke(draw, p0_R, p1_R, p2_R, p3_R, arm_w, accent_mint)

    # 4. Central "S" ribbon curve (connecting mother to child with graceful sweep)
    # Curves from mother's chest down and under the baby
    p0_S = (m_head_pos[0] + (20 * m_scale * scale), m_head_pos[1] + (140 * m_scale * scale))
    p1_S = (cx - (40 * m_scale * scale),            center_y + (70 * m_scale * scale))
    p2_S = (cx + (40 * m_scale * scale),            center_y + (120 * m_scale * scale))
    p3_S = (b_head_pos[0] + (30 * m_scale * scale), center_y + (150 * m_scale * scale))
    draw_bezier_stroke(draw, p0_S, p1_S, p2_S, p3_S, int(36 * m_scale * scale), pure_white)

    # 5. Healthcare Cross Badge (Top Right)
    badge_cx = cx + (200 * m_scale * scale)
    badge_cy = center_y - (180 * m_scale * scale)
    badge_r  = 80 * m_scale * scale

    # Badge circular white base
    draw.ellipse([badge_cx - badge_r, badge_cy - badge_r,
                  badge_cx + badge_r, badge_cy + badge_r], fill=pure_white)

    # Red healthcare cross
    cross_bar = 52 * m_scale * scale
    cross_th  = 20 * m_scale * scale
    c_rad     = int(cross_th * 0.4)
    # Horizontal
    draw.rounded_rectangle([badge_cx - cross_bar, badge_cy - cross_th,
                            badge_cx + cross_bar, badge_cy + cross_th],
                           radius=c_rad, fill=coral_red)
    # Vertical
    draw.rounded_rectangle([badge_cx - cross_th, badge_cy - cross_bar,
                            badge_cx + cross_th, badge_cy + cross_bar],
                           radius=c_rad, fill=coral_red)

    # Downsample
    resampled = img.resize((size, size), Image.Resampling.LANCZOS)
    return resampled

if __name__ == "__main__":
    logo = generate_sahaaya_brand_logo(size=1024, is_round=False)
    logo.save("assets/logo.png", "PNG")
    logo_round = generate_sahaaya_brand_logo(size=1024, is_round=True)
    logo_round.save("assets/logo_round.png", "PNG")
    print("Generated assets/logo.png and assets/logo_round.png")
