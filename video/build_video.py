#!/usr/bin/env python3
# =============================================================================
# build_video.py — 連番フレーム画像＋ナレーション＋BGM を 1 本の mp4 に合成するのだぁ🌱
# -----------------------------------------------------------------------------
# このスクリプトの役割：
#   render.js が書き出した frames/f_00000.png … を 30fps の映像トラックにして、
#   ナレーション音声 full.wav と、BGM を重ねて、最終的な mp4 を作るのだぁ。
#
# 入力（すべてこのスクリプトと同じディレクトリ、または resources/ 以下）：
#   - frames/f_%05d.png  … render.js が書き出したフレーム画像（中間生成物）
#   - full.wav           … assemble.py が作ったナレーション（中間生成物）
#   - timeline.json      … assemble.py が作ったタイムライン（尺・区切り時刻）
#   - resources/bgm/chopin_op10-4.flac … BGM（外部取得リソース。ここに置いてもらうのだぁ）
#
# 出力：
#   - sarracenia.mp4     … 完成した動画
#
# 実行：
#   python3 build_video.py [BGMの基準音量(0〜1)]
#   ffmpeg のパスは環境変数 FFMPEG で上書きできるのだぁ（既定は PATH 上の "ffmpeg"）。
# =============================================================================
import json, os, subprocess, sys

B = os.path.dirname(os.path.abspath(__file__))
FF = os.environ.get("FFMPEG", "ffmpeg")               # ffmpeg 実行ファイル（環境変数で差し替え可）
tl = json.load(open(os.path.join(B, "timeline.json")))
total = tl["total"]
T0 = tl["lead"]                                        # 開幕（タイトル）から寸劇（初回発話）へ切り替わる時刻
CS = tl["creditStart"]                                 # クレジット開始時刻

BGM = os.path.join(B, "resources", "bgm", "chopin_op10-4.flac")
BASE = float(sys.argv[1]) if len(sys.argv) > 1 else 0.45   # BGM の基準音量
OUT = os.path.join(B, "sarracenia.mp4")

# -----------------------------------------------------------------------------
# BGM の音量は「2 ステートの線形補間」なのだぁ🌱
#   冒頭（タイトル）＝A（やや強め） → 寸劇＝B（弱め） → クレジット＝A（冒頭と同じ）へ戻すのだぁ。
#   T0（初回発話）の前後 0.5 秒で A→B へ落とし、CS（クレジット開始）の手前 0.5 秒で B→A へ戻すのだぁ。
#   お尻だけ 2 秒フェードアウトするのだぁ。
#   BGM はショパン 練習曲 作品10-4「Torrent（激流）」（Edward Neeman 演奏, パブリックドメイン）で、
#   冒頭から旋律が鳴る曲なので、頭のフェードインは無しなのだぁ。
# -----------------------------------------------------------------------------
A_VOL = BASE * 0.60          # 冒頭＝クレジット（強）
B_VOL = BASE * 0.40 * 0.70   # 寸劇（弱）
RAMP = 0.5
volexpr = (
    f"if(lt(t,{T0:.3f}),{A_VOL:.4f},"
    f"if(lt(t,{T0+RAMP:.3f}),{A_VOL:.4f}+({B_VOL:.4f}-{A_VOL:.4f})*(t-{T0:.3f})/{RAMP:.3f},"
    f"if(lt(t,{CS-RAMP:.3f}),{B_VOL:.4f},"
    f"if(lt(t,{CS:.3f}),{B_VOL:.4f}+({A_VOL:.4f}-{B_VOL:.4f})*(t-{CS-RAMP:.3f})/{RAMP:.3f},"
    f"{A_VOL:.4f}))))"
)
# フィルタグラフ：ナレーション[1:a]と、音量エンベロープを掛けた BGM[2:a]を足し合わせるのだぁ。
#   BGM は曲が動画尺より短い場合に備えて -stream_loop -1 でループ入力にしているのだぁ。
#   amix の normalize=0 は「単純な足し算」で、勝手に音量を正規化させないためなのだぁ。
fc = (
    f"[1:a]aresample=48000[nar];"
    f"[2:a]aresample=48000,atrim=0:{total:.3f},"
    f"volume=eval=frame:volume='{volexpr}',"
    f"afade=t=out:st={total-2.2:.3f}:d=2.0[bgm];"
    f"[nar][bgm]amix=inputs=2:duration=first:normalize=0[aout]"
)
cmd = [
    FF, "-y", "-hide_banner",
    "-framerate", "30", "-i", os.path.join(B, "frames", "f_%05d.png"),
    "-i", os.path.join(B, "full.wav"),
    "-stream_loop", "-1", "-i", BGM,
    "-filter_complex", fc,
    "-map", "0:v", "-map", "[aout]",
    "-c:v", "libx264", "-pix_fmt", "yuv420p", "-crf", "20",
    "-c:a", "aac", "-b:a", "192k", "-shortest",
    OUT,
]
print(f"BGM base={BASE} A(open/credit)={A_VOL:.3f} B(skit)={B_VOL:.3f} T0={T0} CS={CS}")
r = subprocess.run(cmd, capture_output=True, text=True)
if r.returncode != 0:
    print(r.stderr[-2500:]); sys.exit(1)
print("built:", OUT)
