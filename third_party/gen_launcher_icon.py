import os
import math

RES = "app/src/main/res"

VIOLET = "#7C6CF6"
GREEN = "#6EE7A0"


def rrect(x, y, w, h, r):
    if r <= 0:
        return f"M{x},{y} H{x + w} V{y + h} H{x} Z"
    return (
        f"M{x + r},{y} H{x + w - r} A{r},{r} 0 0 1 {x + w},{y + r} "
        f"V{y + h - r} A{r},{r} 0 0 1 {x + w - r},{y + h} "
        f"H{x + r} A{r},{r} 0 0 1 {x},{y + h - r} "
        f"V{y + r} A{r},{r} 0 0 1 {x + r},{y} Z"
    )


def circle(cx, cy, r):
    return (
        f"M{cx - r},{cy} "
        f"a{r},{r} 0 1,0 {2 * r},0 "
        f"a{r},{r} 0 1,0 {-2 * r},0 Z"
    )


def write(path, content):
    with open(path, "w") as f:
        f.write(content)
    print("wrote", path)


# --- adaptive icon background: the gradient tile, full bleed -------------------
background = f'''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path android:pathData="M0,0h108v108h-108z">
        <aapt:attr name="android:fillColor">
            <gradient
                android:type="linear"
                android:startX="0"
                android:startY="0"
                android:endX="108"
                android:endY="108">
                <item
                    android:color="{VIOLET}"
                    android:offset="0" />
                <item
                    android:color="{GREEN}"
                    android:offset="1" />
            </gradient>
        </aapt:attr>
    </path>
</vector>
'''
write(f"{RES}/drawable/ic_launcher_background.xml", background)


# --- adaptive icon foreground: the wallet mark inside the 72dp safe zone -------
SCALE = 0.22222
TRANSLATE = round(-256 * SCALE + 54, 4)

wallet_paths = f'''        <path
            android:pathData="{rrect(112, 150, 288, 212, 44)}"
            android:fillColor="#FFFFFF"
            android:fillAlpha="0.32" />
        <path
            android:pathData="{rrect(112, 150, 288, 56, 28)}"
            android:fillColor="#FFFFFF"
            android:fillAlpha="0.6" />
        <path
            android:pathData="{rrect(112, 178, 288, 28, 0)}"
            android:fillColor="#FFFFFF"
            android:fillAlpha="0.6" />
        <path
            android:pathData="{rrect(152, 256, 124, 18, 9)}"
            android:fillColor="#FFFFFF"
            android:fillAlpha="1.0" />
        <path
            android:pathData="{rrect(152, 290, 84, 18, 9)}"
            android:fillColor="#FFFFFF"
            android:fillAlpha="0.85" />
        <path
            android:pathData="{circle(338, 296, 26)}"
            android:fillColor="#00000000"
            android:strokeColor="#FFFFFF"
            android:strokeWidth="14" />'''

foreground = f'''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <group
        android:scaleX="{SCALE}"
        android:scaleY="{SCALE}"
        android:translateX="{TRANSLATE}"
        android:translateY="{TRANSLATE}">
{wallet_paths}
    </group>
</vector>
'''
write(f"{RES}/drawable/ic_launcher_foreground.xml", foreground)


# --- monochrome: single-colour silhouette for themed icons --------------------
monochrome = f'''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108"
    android:tint="#FFFFFF">
    <group
        android:scaleX="{SCALE}"
        android:scaleY="{SCALE}"
        android:translateX="{TRANSLATE}"
        android:translateY="{TRANSLATE}">
        <path
            android:pathData="{rrect(112, 150, 288, 212, 44)}"
            android:fillColor="#00000000"
            android:strokeColor="#FFFFFF"
            android:strokeWidth="26" />
        <path
            android:pathData="{rrect(152, 256, 124, 18, 9)}"
            android:fillColor="#FFFFFF" />
        <path
            android:pathData="{rrect(152, 296, 84, 18, 9)}"
            android:fillColor="#FFFFFF" />
        <path
            android:pathData="{circle(338, 296, 26)}"
            android:fillColor="#00000000"
            android:strokeColor="#FFFFFF"
            android:strokeWidth="26" />
    </group>
</vector>
'''
write(f"{RES}/drawable/ic_launcher_monochrome.xml", monochrome)


# --- adaptive icon wiring ----------------------------------------------------
adaptive = '''<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />
</adaptive-icon>
'''
write(f"{RES}/mipmap-anydpi-v26/ic_launcher.xml", adaptive)
write(f"{RES}/mipmap-anydpi-v26/ic_launcher_round.xml", adaptive)