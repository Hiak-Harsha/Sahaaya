"""
Configures the user's dark-mode-logo.png and bright-mode-logo.png into Android mipmap and drawable assets.
"""

import os
from PIL import Image, ImageDraw

def process_logos():
    dark_path = 'assets/dark-mode-logo.png'
    bright_path = 'assets/bright-mode-logo.png'
    
    dark_img = Image.open(dark_path).convert('RGBA')
    bright_img = Image.open(bright_path).convert('RGBA')

    # Ensure output directories exist
    res_base = 'android-app/app/src/main/res'
    densities = {
        'mipmap-mdpi': 48,
        'mipmap-hdpi': 72,
        'mipmap-xhdpi': 96,
        'mipmap-xxhdpi': 144,
        'mipmap-xxxhdpi': 192
    }

    # 1. Prepare circular mask for ic_launcher_round
    w, h = dark_img.size
    mask = Image.new('L', (w, h), 0)
    draw_mask = ImageDraw.Draw(mask)
    draw_mask.ellipse([0, 0, w - 1, h - 1], fill=255)
    
    round_img = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    round_img.paste(dark_img, (0, 0), mask=mask)

    # 2. Export to all mipmap densities
    for folder, size in densities.items():
        out_dir = os.path.join(res_base, folder)
        os.makedirs(out_dir, exist_ok=True)
        
        # Standard launcher icon
        icon_resized = dark_img.resize((size, size), Image.Resampling.LANCZOS)
        icon_resized.save(os.path.join(out_dir, 'ic_launcher.png'), 'PNG')
        
        # Round launcher icon
        round_resized = round_img.resize((size, size), Image.Resampling.LANCZOS)
        round_resized.save(os.path.join(out_dir, 'ic_launcher_round.png'), 'PNG')
        print(f"Exported {folder}: {size}x{size}")

    # 3. Create drawable resources for in-app display
    drawable_dir = os.path.join(res_base, 'drawable')
    os.makedirs(drawable_dir, exist_ok=True)

    # In-app app logo
    in_app_logo = dark_img.resize((256, 256), Image.Resampling.LANCZOS)
    in_app_logo.save(os.path.join(drawable_dir, 'ic_sahaaya_logo.png'), 'PNG')

    # Bright symbol with transparent background
    # Make background transparent if RGB > 245
    bright_data = bright_img.getdata()
    new_bright_data = []
    for pixel in bright_data:
        if pixel[0] > 240 and pixel[1] > 240 and pixel[2] > 240:
            new_bright_data.append((255, 255, 255, 0))
        else:
            new_bright_data.append(pixel)
    bright_trans = Image.new('RGBA', bright_img.size)
    bright_trans.putdata(new_bright_data)
    bright_resized = bright_trans.resize((256, int(256 * bright_img.size[1] / bright_img.size[0])), Image.Resampling.LANCZOS)
    bright_resized.save(os.path.join(drawable_dir, 'ic_sahaaya_symbol_bright.png'), 'PNG')

    # Save to assets/logo.png for documentation / web / preview
    dark_img.resize((512, 512), Image.Resampling.LANCZOS).save('assets/logo.png', 'PNG')
    print("Saved assets/logo.png (512x512)")
    print("Done processing logos!")

if __name__ == '__main__':
    process_logos()
