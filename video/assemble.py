#!/usr/bin/env python3
# 各行wavを、タイトル保持→本編（行間の無音）→クレジット保持、の順に結合するのだぁ🌱
import json, wave, os

B = os.path.dirname(os.path.abspath(__file__))
# 台本は動画タイトルのディレクトリ（sarracenia/）から読み、音声・タイムラインはこの雑多パート（video/直下）に置くのだぁ。
script = json.load(open(os.path.join(B, "sarracenia", "script.json"), encoding="utf-8"))
moras = {m["i"]: m["moras"] for m in json.load(open(os.path.join(B, "moras.json"), encoding="utf-8"))}
SR = 24000

# 尺の設計なのだぁ
TITLE_HOLD = 1.6      # タイトル画面を見せる時間
TITLE_FADE = 0.6      # タイトル→本編のクロスフェード
PRE_SPEAK  = 0.5      # 本編に入ってから喋りだすまでの間
GAP        = 0.5      # 台詞と台詞のあいだの無音（間）
SUB_TAIL   = 0.14     # 字幕を音声より少しだけ長く残す余韻
PRE_CREDIT = 1.3      # 最後の台詞からクレジットまでの間
CREDIT_HOLD= 5.5      # クレジット画面を見せる時間

def read_pcm(path):
    with wave.open(path) as w:
        assert w.getframerate() == SR and w.getnchannels() == 1 and w.getsampwidth() == 2
        return w.readframes(w.getnframes())

def sil(sec):
    return b"\x00\x00" * int(round(sec * SR))

pcm = bytearray()
pcm += sil(TITLE_HOLD + TITLE_FADE + PRE_SPEAK)   # タイトル区間の無音
t = TITLE_HOLD + TITLE_FADE + PRE_SPEAK

lead = t                         # 本編（シーン）が始まる時刻＝タイトル保持＋フェード＋間
title_fade_start = TITLE_HOLD
title_end = TITLE_HOLD + TITLE_FADE

lines_tl = []
for i, line in enumerate(script["lines"]):
    data = read_pcm(os.path.join(B, "audio", f"line{i:02d}.wav"))
    dur = len(data) // 2 / SR
    start = t
    end = t + dur
    lines_tl.append({
        "i": i, "start": round(start, 4), "end": round(end, 4),
        "subEnd": round(end + SUB_TAIL, 4),
        "speaker": line["speaker"], "sub": line["sub"], "big": line["big"],
        "scene": line["scene"], "item": line["item"],
        "moras": moras[i],   # 口パク用（wav先頭からの秒。scene.html で start を足して絶対時刻にするのだぁ）
    })
    pcm += data
    t = end
    if i < len(script["lines"]) - 1:
        pcm += sil(GAP); t += GAP

skit_end = t
credit_start = t + PRE_CREDIT
pcm += sil(PRE_CREDIT + CREDIT_HOLD)
total = credit_start + CREDIT_HOLD

# シーン区間（連続する同一シーンをまとめる。本編開始leadから）
scenes = []
for ln in lines_tl:
    s = ln["scene"]
    if scenes and scenes[-1]["scene"] == s:
        scenes[-1]["end"] = ln["end"]
    else:
        scenes.append({"scene": s, "start": ln["start"], "end": ln["end"]})
scenes[0]["start"] = lead          # 最初のシーンはタイトル明け直後から
scenes[-1]["end"] = skit_end       # 最後のシーンは寸劇終わりまで
for k in range(1, len(scenes)):    # シーン境界はひとつ前の終わりに合わせて連続に
    scenes[k]["start"] = scenes[k-1]["end"]

# アイテム区間（AF: 隣接する同一アイテムを行間もまたいで連続表示・出しっぱなし）
items = []
for ln in lines_tl:
    it = ln["item"]
    if not it:
        continue
    if items and items[-1]["item"] == it and abs(items[-1]["end"] - ln["start"]) < GAP + 0.3:
        items[-1]["end"] = ln["end"]
    else:
        items.append({"item": it, "start": ln["start"], "end": ln["end"]})
# 葉は寸劇の最後まで出しっぱなしにするのだぁ
for it in items:
    if it["item"] == "leaf":
        it["end"] = skit_end

with wave.open(os.path.join(B, "full.wav"), "w") as w:
    w.setnchannels(1); w.setsampwidth(2); w.setframerate(SR)
    w.writeframes(bytes(pcm))

tl = {
    "total": round(total, 4), "lead": round(lead, 4),
    "titleFadeStart": round(title_fade_start, 4), "titleEnd": round(title_end, 4),
    "skitEnd": round(skit_end, 4), "creditStart": round(credit_start, 4),
    "title": script["title"], "subtitle": script["subtitle"],
    "lines": lines_tl, "scenes": scenes, "items": items,
    "sceneFade": 0.6,
}
json.dump(tl, open(os.path.join(B, "timeline.json"), "w"), ensure_ascii=False, indent=1)
print("total:", round(total, 2), "skitEnd:", round(skit_end, 2), "creditStart:", round(credit_start, 2))
print("scenes:", [(s["scene"], round(s["start"],1), round(s["end"],1)) for s in scenes])
print("items:", [(x["item"], round(x["start"],1), round(x["end"],1)) for x in items])
