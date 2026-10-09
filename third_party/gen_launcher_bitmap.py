"""Renders the Saku launcher icon as legacy square bitmaps for API 24-25.

Mirrors third_party/saku-logo.svg: a rounded gradient tile with the wallet mark
on top. Adaptive icons (API 26+) use res/drawable/ic_launcher_*.xml instead.
"""

import os

from PIL import Image, ImageDraw

RES = os.path.join("app", "src", "main", "res")

VIOLET = (124, 108, 246, 255)
GREEN = (110, 231, 160, 255)

TILE_RADIUS_RATIO = 112 / 512
CIRCLE_RATIO = 0.5

SS = 4  # supersample factor for smooth edges

DENSITIES = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}


def lerp(a, b, t):
    return tuple(round(x + (y - x) * t) for x, y in zip(a, b))


def gradient(size, top_left, bottom_right):
    width, height = size
    img = Image.new("RGBA", size)
    draw = ImageDraw.Draw(img)
    span = (width - 1) + (height - 1)
    for y in range(height):
        row = [
            lerp(top_left, bottom_right, (x + y) / span) for x in range(width)
        ]
        for x, color in enumerate(row):
            draw.point((x, y), fill=color)
    return img


def wallet(layer, scale):
    draw = ImageDraw.Draw(layer)
    w, h = layer.size

    def px(v):
        return v * scale

    def box(x, y, bw, bh):
        return [px(x), px(y), px(x + bw), px(y + bh)]

    body = box(112, 150, 288, 212)
    draw.rounded_rectangle(body, radius=px(44), fill=(255, 255, 255, 82))
    draw.rounded_rectangle(box(112, 150, 288, 56), radius=px(28), fill=(255, 255, 255, 153))
    draw.rectangle(box(112, 178, 288, 28), fill=(255, 255, 255, 153))
    draw.rounded_rectangle(box(152, 256, 124, 18), radius=px(9), fill=(255, 255, 255, 255))
    draw.rounded_rectangle(box(152, 290, 84, 18), radius=px(9), fill=(255, 255, 255, 217))
    draw.ellipse(
        box(338 - 26 - 7, 296 - 26 - 7, (26 + 7) * 2, (26 + 7) * 2),
        outline=(255, 255, 255, 255),
        width=max(1, round(px(14))),
    )


def render(size, round_icon):
    edge = size * SS
    canvas = Image.new("RGBA", (edge, edge), (0, 0, 0, 0))
    tile = gradient((edge, edge), VIOLET, GREEN)

    art = Image.new("RGBA", (edge, edge), (0, 0, 0, 0))
    wallet(art, edge / 512)
    tile = Image.alpha_composite(tile, art)

    mask = Image.new("L", (edge, edge), 0)
    mask_draw = ImageDraw.Draw(mask)
    if round_icon:
        inset = round(edge * 0.03)
        mask_draw.ellipse([inset, inset, edge - inset, edge - inset], fill=255)
    else:
        mask_draw.rounded_rectangle(
            [0, 0, edge, edge], radius=round(edge * TILE_RADIUS_RATIO), fill=255
        )

    canvas.paste(tile, (0, 0), mask)
    return canvas.resize((size, size), Image.LANCZOS)


for density, size in DENSITIES.items():
    folder = f"{RES}/mipmap-{density}"
    os.makedirs(folder, exist_ok=True)
    for name, round_icon in (("ic_launcher", False), ("ic_launcher_round", True)):
        path = f"{folder}/{name}.webp"
        render(size, round_icon).save(path, "WEBP", lossless=True, method=6)
        print("wrote", path)