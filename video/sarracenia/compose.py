#!/usr/bin/env python3
# =============================================================================
# compose.py — この動画の「構成jsonl」を組み立てるのだぁ🌱
# -----------------------------------------------------------------------------
# 構成jsonl とは：
#   1 行 = 1 フレームに対応する、フレーム設定オブジェクトを並べたファイルなのだぁ。
#   汎用レンダラー（../renderer/render.js）が、これを頭から 1 行ずつ scene.html の
#   window.applyFrame(cfg) に渡して、1 行につき 1 枚の画像を撮るのだぁ。
#
#   今のこの動画では、フレームの見た目は「時刻 t」だけで決まる（scene.html の seek(t) が
#   タイムラインを見て全要素を組み立てる）ので、各行は {"t": 秒} だけなのだぁ。
#   ＝この動画の「構成」は、いまは 30fps 刻みの時刻列そのものなのだぁ🌱
#
#   ゆくゆくは（Issue #179 トピック BP の構想）、各行に「どの要素へどんな CSS・属性・
#   テキストを入れるか」をフラットに書き込んで、seek の計算そのものを構成jsonl 側へ
#   追い出すこともできるのだぁ。そのときレンダラー側は一切変えなくてよいのだぁ（applyFrame の
#   契約は同じ）。今回はまず、その入口となる「構成jsonl という境界」を作るところまでなのだぁ。
#
# 入力：
#   timeline.json … assemble.py（雑多パート）が作る尺情報。total（総尺・秒）を使うのだぁ。
# 出力：
#   frames.jsonl  … 1 行 = 1 フレームの構成
#
# 使い方：
#   python3 compose.py [timeline.json] [frames.jsonl]
#     省略時は ../timeline.json を読み、./frames.jsonl へ書くのだぁ。
# =============================================================================
import json, math, os, sys

FPS = 30   # build_video.py の -framerate と、render 時のフレーム割り当てに一致させるのだぁ

B = os.path.dirname(os.path.abspath(__file__))
timeline_path = sys.argv[1] if len(sys.argv) > 1 else os.path.join(B, "..", "timeline.json")
out_path = sys.argv[2] if len(sys.argv) > 2 else os.path.join(B, "frames.jsonl")

tl = json.load(open(timeline_path, encoding="utf-8"))
total = tl["total"]
frames = math.ceil(total * FPS)   # 総フレーム数（旧 render.js の Math.ceil(TOTAL*FPS) と同じ）

with open(out_path, "w", encoding="utf-8") as f:
    for i in range(frames):
        # 時刻は i/FPS（旧 render.js の t=i/FPS と同じ値）。seek(t) がこの時刻の画面を決めるのだぁ。
        f.write(json.dumps({"t": i / FPS}, ensure_ascii=False) + "\n")

print(f"composed {out_path}: {frames} frames (total={total}s @ {FPS}fps)")
