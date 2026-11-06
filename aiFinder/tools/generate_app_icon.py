"""Regenerate launcher assets with Python 3 and Pillow."""

import json
from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[2]
ANDROID = ROOT / "aiFinder/src/androidMain"
CATALOG = ROOT / "aiFinderIos/aiFinderIos/Assets.xcassets"
TEAL = "#174A48"
MINT = "#DDF3EB"
FRONT = "#A5DBCB"
SIDE = "#69B7A7"
ACCENT = "#7BD8CB"

# Shared normalized geometry for raster exports and Android vector drawables.
SHAPES = [
    ("path", MINT, "M230,410 L465,290 L705,410 L470,540 Z"),
    ("path", "#28625C", "M280,410 L465,320 L655,410 L470,502 Z"),
    ("path", SIDE, "M230,410 L470,540 L470,755 L230,625 Z"),
    ("path", FRONT, "M470,540 L705,410 L705,625 L470,755 Z"),
    ("path", MINT, "M230,410 L255,397 L470,514 L680,397 L705,410 L470,540 Z"),
    ("path", TEAL, "M535,584 L632,531 L632,557 L535,610 Z"),
    ("line", TEAL, (290, 509, 290, 529, 15)),
    ("line", TEAL, (389, 563, 389, 583, 15)),
    ("line", TEAL, (308, 565, 320, 591, 12)),
    ("line", TEAL, (320, 591, 345, 605, 12)),
    ("line", TEAL, (345, 605, 370, 603, 12)),
    ("line", TEAL, (680, 437, 788, 555, 49)),
    ("line", ACCENT, (710, 470, 788, 555, 48)),
    ("circle", TEAL, (599, 357, 151)),
    ("circle", MINT, (599, 357, 126)),
    ("circle", TEAL, (599, 357, 92)),
    ("path", ACCENT, "M599,296 L616,340 L660,357 L616,374 L599,418 L582,374 L538,357 L582,340 Z"),
    ("path", MINT, "M663,284 L669,301 L686,307 L669,313 L663,330 L657,313 L640,307 L657,301 Z"),
    ("line", ACCENT, (207, 358, 207, 320, 12)),
    ("line", ACCENT, (207, 320, 245, 320, 12)),
    ("line", ACCENT, (741, 661, 741, 699, 12)),
    ("line", ACCENT, (741, 699, 703, 699, 12)),
]


def artwork():
    image = Image.new("RGB", (2048, 2048))
    draw = ImageDraw.Draw(image)
    for y in range(2048):
        t = y / 2047
        draw.line((0, y, 2048, y), fill=(
            round(23 + (14 - 23) * t),
            round(74 + (51 - 74) * t),
            round(72 + (51 - 72) * t),
        ))
    for kind, color, geometry in SHAPES:
        if kind == "path":
            points = [
                tuple(float(v) * 2 for v in pair.split(","))
                for pair in geometry.replace("M", "").replace("L", "").replace("Z", "").split()
            ]
            draw.polygon(points, fill=color)
        elif kind == "circle":
            x, y, radius = geometry
            draw.ellipse(((x - radius) * 2, (y - radius) * 2,
                          (x + radius) * 2, (y + radius) * 2), fill=color)
        else:
            x1, y1, x2, y2, width = geometry
            draw.line((x1 * 2, y1 * 2, x2 * 2, y2 * 2), fill=color, width=width * 2)
            for x, y in ((x1, y1), (x2, y2)):
                draw.ellipse((x * 2 - width, y * 2 - width,
                              x * 2 + width, y * 2 + width), fill=color)
    return image


def vector(monochrome=False):
    paths = []
    for kind, color, geometry in SHAPES:
        if monochrome and color in (TEAL, "#28625C"):
            continue
        color = "#FFFFFF" if monochrome else color
        if kind == "path":
            paths.append(f'<path android:fillColor="{color}" android:pathData="{geometry}"/>')
        elif kind == "circle":
            x, y, radius = geometry
            data = (f"M{x-radius},{y} a{radius},{radius} 0,1 0,{radius*2},0 "
                    f"a{radius},{radius} 0,1 0,{-radius*2},0")
            # A ring preserves the lens opening in the monochrome silhouette.
            if monochrome:
                if radius != 126:
                    continue
                paths.append(f'<path android:strokeColor="{color}" android:strokeWidth="34" '
                             f'android:fillColor="@android:color/transparent" android:pathData="{data}"/>')
            else:
                paths.append(f'<path android:fillColor="{color}" android:pathData="{data}"/>')
        else:
            x1, y1, x2, y2, width = geometry
            paths.append(f'<path android:strokeColor="{color}" android:strokeWidth="{width}" '
                         f'android:strokeLineCap="round" android:pathData="M{x1},{y1} L{x2},{y2}"/>')
    return ('<vector xmlns:android="http://schemas.android.com/apk/res/android" '
            'android:width="108dp" android:height="108dp" '
            'android:viewportWidth="108" android:viewportHeight="108">\n'
            '    <group android:scaleX="0.09" android:scaleY="0.09" '
            'android:translateX="7.92" android:translateY="7.92">\n        '
            + "\n        ".join(paths) + "\n    </group>\n</vector>\n")


def write(path, text):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text)


def main():
    image = artwork()
    for density, size in (("mdpi", 48), ("hdpi", 72), ("xhdpi", 96),
                          ("xxhdpi", 144), ("xxxhdpi", 192)):
        directory = ANDROID / f"res/mipmap-{density}"
        directory.mkdir(parents=True, exist_ok=True)
        icon = image.resize((size, size), Image.Resampling.LANCZOS).convert("RGBA")
        mask = Image.new("L", (size, size))
        ImageDraw.Draw(mask).rounded_rectangle((0, 0, size - 1, size - 1), radius=size * 0.22, fill=255)
        icon.putalpha(mask)
        icon.save(directory / "ic_launcher.png")
    image.resize((512, 512), Image.Resampling.LANCZOS).save(ANDROID / "ic_launcher-playstore.png")
    write(ANDROID / "res/drawable/ic_launcher_foreground.xml", vector())
    write(ANDROID / "res/drawable/ic_launcher_monochrome.xml", vector(monochrome=True))
    write(CATALOG / "Contents.json", json.dumps({"info": {"author": "xcode", "version": 1}}, indent=2) + "\n")
    appicon = CATALOG / "AppIcon.appiconset"
    appicon.mkdir(parents=True, exist_ok=True)
    images = []
    for idiom, sizes in (("iphone", ((20, (2, 3)), (29, (2, 3)), (40, (2, 3)), (60, (2, 3)))),
                         ("ipad", ((20, (1, 2)), (29, (1, 2)), (40, (1, 2)), (76, (1, 2)), (83.5, (2,))))):
        for size, scales in sizes:
            for scale in scales:
                pixels = int(size * scale)
                filename = f"Icon-{pixels}.png"
                image.resize((pixels, pixels), Image.Resampling.LANCZOS).save(appicon / filename)
                images.append({"idiom": idiom, "size": f"{size}x{size}",
                               "scale": f"{scale}x", "filename": filename})
    image.resize((1024, 1024), Image.Resampling.LANCZOS).save(appicon / "Icon-1024.png")
    images.append({"idiom": "ios-marketing", "size": "1024x1024",
                   "scale": "1x", "filename": "Icon-1024.png"})
    write(appicon / "Contents.json", json.dumps(
        {"images": images, "info": {"author": "xcode", "version": 1}}, indent=2) + "\n")


if __name__ == "__main__":
    main()
