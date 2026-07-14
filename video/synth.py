#!/usr/bin/env python3
# AI: 読み上げは VOICEVOX の「カタカナ原稿（is_kana）」記法を経由するのだぁ🌱
# 手順: text から audio_query → accent_phrases をカナ原稿にシリアライズ → 誤読だけ手直し
#       → is_kana=true で読み直して差し込み → 合成。使ったカナ原稿は kana.json に保存するのだぁ。
import json, urllib.request, urllib.parse, wave, os

HOST = os.environ.get("VOICEVOX_HOST", "http://127.0.0.1:50021")   # VOICEVOX ENGINE の URL（環境変数で差し替え可）
B = os.path.dirname(os.path.abspath(__file__))
script = json.load(open(os.path.join(B, "script.json"), encoding="utf-8"))

def post(path, data=None):
    req = urllib.request.Request(HOST + path, data=data, method="POST")
    if data is not None:
        req.add_header("Content-Type", "application/json")
    with urllib.request.urlopen(req, timeout=60) as r:
        return r.read()

def wav_dur(path):
    with wave.open(path) as w:
        return w.getnframes() / w.getframerate()

# accent_phrases を、スキル(voicevox-kana)の標準記法のカナ原稿にシリアライズするのだぁ
#   アクセント核 '（下降位置。平板は単位末尾）／ アクセント区切り / ／ 小休止 、 ／ 語尾上げ ？
def serialize_kana(aps):
    out = []
    for pi, ap in enumerate(aps):
        s = ""
        a = ap["accent"]
        for mi, m in enumerate(ap["moras"]):
            s += m["text"]
            if mi == a - 1:
                s += "'"
        if ap.get("is_interrogative"):
            s += "？"
        out.append(s)
        if pi < len(aps) - 1:
            out.append("、" if ap.get("pause_mora") else "/")
    return "".join(out)

# 口パク(AV)用に、合成に使う最終クエリからモーラ区間を割り出すのだぁ🌱
# 返すのは [cs, vs, ve, vowel]（wav先頭からの秒。speedScaleで割り済み）。
#   cs=モーラ開始（子音頭）, vs=母音開始, ve=母音終わり。子音区間は閉口、母音区間は母音で開き具合を決めるのだぁ。
def mora_segments(q):
    speed = q["speedScale"]
    cur = q["prePhonemeLength"]
    segs = []
    for ap in q["accent_phrases"]:
        for m in ap["moras"]:
            cs = cur
            cl = m.get("consonant_length") or 0.0
            vl = m["vowel_length"]
            vs = cs + cl
            ve = vs + vl
            segs.append([round(cs / speed, 4), round(vs / speed, 4), round(ve / speed, 4), m["vowel"]])
            cur = ve
        pm = ap.get("pause_mora")
        if pm:
            cur += pm["vowel_length"]
    return segs

os.makedirs(os.path.join(B, "audio"), exist_ok=True)
results = []
kana_out = []
moras_out = []
for i, line in enumerate(script["lines"]):
    sid = line["sid"]
    q = json.loads(post("/audio_query?" + urllib.parse.urlencode({"speaker": sid, "text": line["text"]})))
    kana = line.get("kana") or serialize_kana(q["accent_phrases"])
    aps = json.loads(post("/accent_phrases?" + urllib.parse.urlencode(
        {"speaker": sid, "text": kana, "is_kana": "true"})))
    q["accent_phrases"] = aps
    q["speedScale"] = line["speed"]
    q["prePhonemeLength"] = 0.08
    q["postPhonemeLength"] = 0.08
    wav = post("/synthesis?" + urllib.parse.urlencode({"speaker": sid}), json.dumps(q).encode())
    out = os.path.join(B, "audio", f"line{i:02d}.wav")
    open(out, "wb").write(wav)
    d = wav_dur(out)
    results.append({"i": i, "dur": round(d, 4)})
    kana_out.append({"i": i, "speaker": line["speaker"], "kana": kana})
    moras_out.append({"i": i, "moras": mora_segments(q)})
    print(f"line{i:02d} sid={sid} dur={d:.3f} kana={kana}")

json.dump(results, open(os.path.join(B, "durations.json"), "w"), ensure_ascii=False, indent=1)
json.dump(kana_out, open(os.path.join(B, "kana.json"), "w"), ensure_ascii=False, indent=1)
json.dump(moras_out, open(os.path.join(B, "moras.json"), "w"), ensure_ascii=False, indent=1)
print("total speech:", round(sum(r["dur"] for r in results), 2), "sec")
