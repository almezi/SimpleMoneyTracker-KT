#!/usr/bin/env python3
"""Generate the iOS app icon (Stage 4, item 4.12).

The design matches the Android adaptive icon so the app is recognisable across
both platforms:

  * background — the same linear gradient `#7C6CF6` -> `#6EE7A0` read out of
    `app/src/main/res/drawable/ic_launcher_background.xml`
  * foreground — the same white wallet glyph from
    `app/src/main/res/drawable/ic_launcher_foreground.xml`, redrawn here with
    PIL primitives rather than parsed from the vector path data

iOS only needs one icon: a 1024x1024 universal image, which the system scales
for every device and context. Generate that and the catalog is one file.

Run:  ./iosApp/generate-icon.py
"""

from __future__ import annotations

import pathlib
import sys

from PIL import Image, ImageDraw

SIZE = 1024
GRADIENT_FROM = (0x7C, 0x6C, 0xF6)
GRADIENT_TO = (0x6E, 0xE7, 0xA0)

HERE = pathlib.Path(__file__).resolve().parent
ASSET_DIR = HERE / "iosApp" / "Assets.xcassets" / "AppIcon.appiconset"

CONTENTS_JSON = """{
  "images" : [
    {
      "filename" : "AppIcon.png",
      "idiom" : "universal",
      "platform" : "ios",
      "size" : "1024x1024"
    }
  ],
  "info" : {
    "author" : "xcode",
    "version" : 1
  }
}
"""

ASSETS_ROOT_JSON = """{
  "info" : {
    "author" : "xcode",
    "version" : 1
  }
}
"""


def diagonal_gradient(size: int, top: tuple[int, int, int], bottom: tuple[int, int, int]) -> Image.Image:
    """Linear gradient along the diagonal, matching the Android vector's start/end points."""
    image = Image.new("RGB", (size, size))
    pixels = image.load()
    for y in range(size):
        for x in range(size):
            # Diagonal projection, 0 at top-left -> 1 at bottom-right.
            t = (x + y) / (2 * (size - 1))
            pixels[x, y] = (
                round(top[0] + (bottom[0] - top[0]) * t),
                round(top[1] + (bottom[1] - top[1]) * t),
                round(top[2] + (bottom[2] - top[2]) * t),
            )
    return image


def rounded_rect(draw: ImageDraw.ImageDraw, box, radius, fill) -> None:
    draw.rounded_rectangle(box, radius=radius, fill=fill)


def draw_wallet(size: int) -> Image.Image:
    """The wallet glyph on a transparent layer, so it can be composited over the gradient.

    The Android adaptive icon authors its glyph in a 108-unit viewport and
    applies a group transform:

        <group android:scaleX="0.22222" android:translateX="-2.8883" ...>

    Reproducing that transform is what keeps the iOS icon looking like the
    Android one. The glyph's paths are written in a ~400-unit space, so the
    scale is what pulls them down to viewport size — skipping it draws the
    wallet three times too large and it runs off the right edge.
    """
    layer = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)

    viewport = 108.0
    scale = 0.22222
    translate = -2.8883

    def to_canvas(v: float) -> float:
        """Map a viewport coordinate onto the icon's pixel grid."""
        return (v * scale + translate) * (size / viewport)

    def box(x0, y0, x1, y1):
        return [to_canvas(x0), to_canvas(y0), to_canvas(x1), to_canvas(y1)]

    # Corner radii are authored in the same coordinate space as the paths, so
    # they take the scale but NOT the translate — adding -2.8883 to a radius
    # makes it negative, which PIL rejects.
    def r(v: float) -> float:
        return v * scale * (size / viewport)

    # Body — the Android path draws it at 32% opacity.
    rounded_rect(d, box(112, 150, 400, 362), radius=r(44), fill=(255, 255, 255, 82))

    # Top flap and its lower edge, 60% opacity.
    rounded_rect(d, box(112, 150, 400, 206), radius=r(28), fill=(255, 255, 255, 153))
    d.rectangle(box(112, 178, 400, 206), fill=(255, 255, 255, 153))

    # Two opaque detail lines.
    rounded_rect(d, box(152, 256, 276, 274), radius=r(9), fill=(255, 255, 255, 255))
    rounded_rect(d, box(152, 290, 236, 308), radius=r(9), fill=(255, 255, 255, 255))

    return layer


def main() -> int:
    if not ASSET_DIR.parent.exists():
        print(f"missing {ASSET_DIR.parent}", file=sys.stderr)
        return 1

    (ASSET_DIR.parent / "Contents.json").write_text(ASSETS_ROOT_JSON)

    background = diagonal_gradient(SIZE, GRADIENT_FROM, GRADIENT_TO).convert("RGBA")
    background.alpha_composite(draw_wallet(SIZE))

    # Flatten onto opaque so the PNG has no alpha channel. iOS rejects icons
    # with transparency: "App Store Icon ... can't be transparent".
    background.convert("RGB").save(ASSET_DIR / "AppIcon.png", "PNG")

    ASSET_DIR.joinpath("Contents.json").write_text(CONTENTS_JSON)
    print(f"wrote {ASSET_DIR / 'AppIcon.png'} ({SIZE}x{SIZE}, no alpha)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())