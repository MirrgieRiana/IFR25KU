#!/usr/bin/env bash
# =============================================================================
# build.sh — サラセニア寸劇動画を最初から最後まで組み立てるオーケストレーターなのだぁ🌱
# -----------------------------------------------------------------------------
# video/ は 3 つのパートに分かれているのだぁ：
#   1. renderer/   … 構成jsonl と HTML テンプレートから画像を撮る「汎用レンダラー」（動画に依存しないのだぁ）
#   2. sarracenia/ … この動画の台本・シーン・構成jsonl を作る「動画タイトルのディレクトリ」（自己完結なのだぁ）
#   3. video/ 直下 … 音声合成・動画合成などの「雑多な部分」。この build.sh が 1 と 2 を呼び出すのだぁ。
#
# この build.sh（＝3 の一部）が、全体を次の順で配線するのだぁ：
#   build.xa1 … synth.xa1 → assemble.xa1（音声・タイムライン）
#               → sarracenia/build_scene.sh（timeline → assets.js, tachie/, frames.jsonl）
#     → renderer/render.js（scene.html + frames.jsonl → sarracenia/frames/）
#     → build_video.py（フレーム＋音声＋BGM → mp4）
#
# 前提（先に済ませておくこと。詳しくは README.md を読むのだぁ）：
#   1. 外部取得リソースを sarracenia/resources/ 以下の所定パスに置くこと（*.md5 がその置き場所と中身を示すのだぁ）。
#        sarracenia/resources/psd/zundamon23.psd
#        sarracenia/resources/psd/tsumugi3.psd
#        sarracenia/resources/font/ZenMaruGothic-Black.ttf
#        sarracenia/resources/font/ZenMaruGothic-Bold.ttf
#        sarracenia/resources/emoji/seedling.svg
#        sarracenia/resources/bgm/chopin_op10-4.flac
#   2. VOICEVOX ENGINE を起動しておくこと（既定は http://127.0.0.1:50021）。
#   3. Node.js / Python3 / ffmpeg / curl / base64 が使えること。
#
# 差し替え可能な環境変数（無指定なら既定値）：
#   VOICEVOX_HOST  … VOICEVOX ENGINE の URL（既定 http://127.0.0.1:50021）※synth.xa1 が参照
#   CHROMIUM_PATH  … Chrome/Chromium 実行ファイル（既定 /tmp/chromium＝renderer/setup_chromium.js の展開先）
#   CHROMIUM_LD_PATH … render 時に LD_LIBRARY_PATH へ前置するパス（サンドボックス環境で NSS 等を補うとき用）
#   FFMPEG         … ffmpeg 実行ファイル（既定 PATH 上の ffmpeg）
#   BGM_VOL        … BGM の基準音量 0〜1（既定 0.45）
#
# 出力：
#   sarracenia.mp4 … 完成動画（video/ 直下）
#   （その他 full.wav / timeline.json / sarracenia/assets.js / sarracenia/frames.jsonl /
#     sarracenia/frames/ などは中間生成物なのだぁ）
# =============================================================================
set -euo pipefail
cd "$(dirname "$0")"   # video/

FFMPEG="${FFMPEG:-ffmpeg}"
BGM_VOL="${BGM_VOL:-0.45}"

# --- 0～3. 構成jsonl ができるまでの段（BGM の確認・音声合成・結合・scene）なのだぁ ---
#        リポジトリ同梱の xarpite（相対パス）で build.xa1 を実行するのだ～🌱
../xarpite/xarpite -A 5 -q -f build.xa1

# --- 4. 汎用レンダラーの準備（Node 依存と Chromium）なのだぁ ---------------------
if [ ! -d renderer/node_modules ]; then
  echo "== npm install (renderer) =="
  (cd renderer && npm install)
fi
if [ -z "${CHROMIUM_PATH:-}" ] && [ ! -x /tmp/chromium ]; then
  echo "== setup chromium =="
  node renderer/setup_chromium.js
fi

# --- 5. レンダラーで 1 フレームずつ撮って sarracenia/frames/ に書き出すのだぁ -----
echo "== render frames =="
rm -rf sarracenia/frames
if [ -n "${CHROMIUM_LD_PATH:-}" ]; then
  LD_LIBRARY_PATH="${CHROMIUM_LD_PATH}:${LD_LIBRARY_PATH:-}" node renderer/render.js sarracenia/frames.jsonl sarracenia/frames
else
  node renderer/render.js sarracenia/frames.jsonl sarracenia/frames
fi

# --- 6. フレーム＋ナレーション＋BGM を合成して mp4 にするのだぁ -----------------
echo "== build video =="
FFMPEG="$FFMPEG" python3 build_video.py "$BGM_VOL"

echo "== done: $(pwd)/sarracenia.mp4 =="
