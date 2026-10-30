"""
Crops the user-supplied branding.png into:
1. assets/symbol_bright.png - Bright mode symbol logo
2. assets/name_logo.png - Sahaaya typography name logo
3. assets/logo_full_horizontal.png - Full horizontal brand lockup
4. assets/icon_dark_squircle.png - Dark mode squircle app icon
5. assets/icon_dark_round.png - Dark mode round app icon
6. android-app mipmap resources (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi)
"""

import os
from PIL import Image, ImageDraw

def crop_branding():
    img = Image.open('assets/branding.png').convert('RGBA')
    w, h = img.size

    # 1. Bright mode symbol logo
    # Box: [125, 270, 580, 588]
    symbol_bright = img.crop((125, 270, 580, 588))
    # Make near-white background transparent
    datas = symbol_bright.getdata()
    new_data = []
    for item in datas:
        # If pixel is almost white (RGB > 248)
        if item[0] > 248 and item[1] > 248 and item[2] > 248:
            new_data.append((255, 255, 255, 0))
        else:
            new_data.append(item)
    symbol_bright.putdata(new_data)
    symbol_bright.save('assets/symbol_bright.png', 'PNG')

    # 2. Name logo ('Sahaaya')
    # Box: [575, 360, 1272, 575]
    name_logo = img.crop((575, 360, 1272, 575))
    datas_name = name_logo.getdata()
    new_data_name = []
    for item in datas_name:
        if item[0] > 248 and item[1] > 248 and item[2] > 248:
            new_data_name.append((255, 255, 255, 0))
        else:
            new_data_name.append(item)
    name_logo.putdata(new_data_name)
    name_logo.save('assets/name_logo.png', 'PNG')

    # 3. Full horizontal lockup (Symbol + Sahaaya)
    # Box: [125, 270, 1272, 588]
    full_logo = img.crop((125, 270, 1272, 588))
    datas_full = full_logo.getdata()
    new_data_full = []
    for item in datas_full:
        if item[0] > 248 and item[1] > 248 and item[2] > 248:
            new_data_full.append((255, 255, 255, 0))
        else:
            new_data_full.append(item)
    full_logo.putdata(new_data_full)
    full_logo.save('assets/logo_full_horizontal.png', 'PNG')

    # 4. Dark mode app icon (squircle)
    # Exact box from previous analysis: [1347, 271, 1677, 601] (330x330)
    dark_icon = img.crop((1347, 271, 1677, 601))
    # Outer background outside the rounded corners is white, make it transparent
    # The corner pixels of the 330x330 box outside the squircle are white
    datas_dark = dark_icon.getdata()
    new_data_dark = []
    for item in datas_dark:
        if item[0] > 245 and item[1] > 245 and item[2] > 245:
            new_data_dark.append((255, 255, 255, 0))
        else:
            new_data_dark.append(item)
    dark_icon.putdata(new_data_dark)
    dark_icon.save('assets/icon_dark_squircle.png', 'PNG')

    # 5. Round version of dark icon (circular mask)
    round_size = dark_icon.size[0]
    # To get anti-aliased circular crop, render mask at 4x
    mask_mult = 4
    mask = Image.new('L', (round_size * mask_mult, round_size * mask_mult), 0)
    draw_mask = ImageDraw.Draw(mask)
    draw_mask.ellipse([0, 0, round_size * mask_mult - 1, round_size * mask_mult - 1], fill=255)
    mask = mask.resize((round_size, round_size), Image.Resampling.LANCZOS)
    
    # Fill the square with the squircle's background color (#071836 approx)
    # Sample background color near center top:
    bg_sample = dark_icon.getpixel((round_size // 2, 20))
    round_base = Image.new('RGBA', (round_size, round_size), bg_sample)
    # Paste dark_icon on round_base
    round_base.alpha_composite(dark_icon)
    # Apply circular mask
    dark_round = Image.new('RGBA', (round_size, round_size), (0, 0, 0, 0))
    dark_round.paste(round_base, (0, 0), mask=mask)
    dark_round.save('assets/icon_dark_round.png', 'PNG')

    # 6. Generate Android Mipmap densities
    # Densities:
    # mdpi: 48x48
    # hdpi: 72x72
    # xhdpi: 96x96
    # xxhdpi: 144x144
    # xxxhdpi: 192x192
    densities = {
        'mipmap-mdpi': 48,
        'mipmap-hdpi': 72,
        'mipmap-xhdpi': 96,
        'mipmap-xxhdpi': 144,
        'mipmap-xxxhdpi': 192
    }

    res_dir = 'android-app/app/src/main/res'
    for folder, d_size in densities.items():
        out_folder = os.path.join(res_dir, folder)
        os.makedirs(out_folder, exist_ok=True)
        # Regular icon
        sq_resized = dark_icon.resize((d_size, d_size), Image.Resampling.LANCZOS)
        sq_resized.save(os.path.join(out_folder, 'ic_launcher.png'), 'PNG')
        # Round icon
        rd_resized = dark_round.resize((d_size, d_size), Image.Resampling.LANCZOS)
        rd_resized.save(os.path.join(out_folder, 'ic_launcher_round.png'), 'PNG')
        print(f"Generated {folder}/ic_launcher.png and ic_launcher_round.png ({d_size}x{d_size})")

    # Also save standard 512x512 store/preview icon
    store_icon = dark_icon.resize((512, 512), Image.Resampling.LANCZOS)
    store_icon.save('assets/ic_launcher_512.png', 'PNG')
    print("All branding assets and Android mipmaps cropped and exported successfully!")

if __name__ == '__main__':
    crop_branding()
