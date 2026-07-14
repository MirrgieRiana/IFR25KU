#!/usr/bin/env python3
# =============================================================================
# bake_assets.py — scene.html が読み込む assets.js を生成するのだぁ🌱
# -----------------------------------------------------------------------------
# なぜこれが必要かというと：
#   レンダリングは Chromium に file://.../scene.html を開かせて 1 フレームずつ撮る方式なのだぁ。
#   file:// で開いた HTML は、セキュリティ上、外部ファイルを fetch() で読めないのだぁ。
#   そこで、フォント・絵文字・テクスチャ画像・タイムラインを、ぜんぶ 1 個の JS ファイル
#   （assets.js）に「焼き込んで」おいて、scene.html から <script> で読み込むのだぁ。
#
#   assets.js の中身は、たった 2 行なのだぁ：
#     1 行目： window.ASSETS   = { 画像やフォントを base64 data-URL 化したもの ほか }
#     2 行目： window.TIMELINE = { assemble.py が作った timeline.json の中身そのまま }
#
# 入力：
#   - ../timeline.json                      … assemble.py（雑多パート）の出力（尺・台詞区間・シーン・アイテム）
#   - ../../common/.../*.png（IFR25KU リポジトリのテクスチャ）… plant / leaf / logo / bg
#   - resources/emoji/seedling.svg          … クレジットの🌱（Fluent Emoji, 外部取得リソース）
#   - resources/font/ZenMaruGothic-*.ttf    … 字幕フォント（外部取得リソース）
#
# 出力：
#   - assets.js
# =============================================================================
import base64, json, os, sys

B = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.dirname(os.path.dirname(B))   # video/sarracenia/ の 2 つ上＝IFR25KU リポジトリのルートなのだぁ

# IFR25KU リポジトリ内のテクスチャ（＝コミット済みの素材なので、リポジトリから直接読むのだぁ）
TEX = os.path.join(REPO, "common", "src", "main", "resources", "assets", "miragefairy2024")
IMG = {
    "plant": os.path.join(TEX, "textures", "block", "magic_plant", "sarracenia_age3.png"),
    "leaf":  os.path.join(TEX, "textures", "item", "sarracenia_leaf.png"),
    "logo":  os.path.join(TEX, "icon.png"),
    "bg":    os.path.join(TEX, "textures", "block", "haimeviska_log.png"),
}

# 外部取得リソース（resources/ 以下に置いてもらうもの）
SEEDLING = os.path.join(B, "resources", "emoji", "seedling.svg")
FONT_BLACK = os.path.join(B, "resources", "font", "ZenMaruGothic-Black.ttf")
FONT_BOLD = os.path.join(B, "resources", "font", "ZenMaruGothic-Bold.ttf")


def png_data_url(path):
    with open(path, "rb") as f:
        return "data:image/png;base64," + base64.b64encode(f.read()).decode("ascii")


def ttf_data_url(path):
    with open(path, "rb") as f:
        return "data:font/ttf;base64," + base64.b64encode(f.read()).decode("ascii")


assets = {}
for key, path in IMG.items():
    assets[key] = png_data_url(path)
# seedling は data-URL ではなく「生の SVG 文字列」なのだぁ。scene.html が innerHTML に直接差し込むのだぁ。
with open(SEEDLING, encoding="utf-8") as f:
    assets["seedling"] = f.read().strip()
assets["fontBlack"] = ttf_data_url(FONT_BLACK)
assets["fontBold"] = ttf_data_url(FONT_BOLD)

# timeline.json は雑多パート（assemble.py）の出力なのだぁ。既定は video/ 直下（../timeline.json）を読むのだぁ。
timeline_path = sys.argv[1] if len(sys.argv) > 1 else os.path.join(B, "..", "timeline.json")
timeline = json.load(open(timeline_path, encoding="utf-8"))

with open(os.path.join(B, "assets.js"), "w", encoding="utf-8") as f:
    f.write("window.ASSETS=" + json.dumps(assets, ensure_ascii=False) + ";\n")
    f.write("window.TIMELINE=" + json.dumps(timeline, ensure_ascii=False) + ";\n")

print("baked assets.js:", ", ".join(assets.keys()))
print("  timeline: total=", timeline["total"], "lines=", len(timeline["lines"]))
