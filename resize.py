import os
from PIL import Image

src_img = r"C:\Users\alens\.gemini\antigravity-ide\brain\60d3245b-bd50-4a96-abac-173dda491c1f\.user_uploaded\media_1789625411540.png"
base_dir = r"d:\projects\SportOS-watch\app\src\main\res"

sizes = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192
}

if not os.path.exists(src_img):
    print("Source image not found.")
    exit(1)

img = Image.open(src_img).convert("RGBA")
# Crop to square
width, height = img.size
new_size = min(width, height)
left = (width - new_size) / 2
top = (height - new_size) / 2
right = (width + new_size) / 2
bottom = (height + new_size) / 2
img = img.crop((left, top, right, bottom))

for mipmap_dir, size in sizes.items():
    dir_path = os.path.join(base_dir, mipmap_dir)
    os.makedirs(dir_path, exist_ok=True)
    
    resized = img.resize((size, size), Image.Resampling.LANCZOS)
    resized.save(os.path.join(dir_path, "ic_launcher.png"), "PNG")
    resized.save(os.path.join(dir_path, "ic_launcher_round.png"), "PNG")
    print(f"Saved {size}x{size} to {mipmap_dir}")
