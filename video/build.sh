#!/usr/bin/env bash
# =============================================================================
# build.sh — サラセニア寸劇動画を最初から最後まで組み立てるオーケストレーターなのだぁ🌱
# -----------------------------------------------------------------------------
# 前提（先に済ませておくこと。詳しくは README.md を読むのだぁ）：
#   1. 外部取得リソースを resources/ 以下の所定パスに置くこと（*.md5 がその置き場所と中身を示すのだぁ）。
#        resources/psd/zundamon23.psd
#        resources/psd/tsumugi3.psd
#        resources/font/ZenMaruGothic-Black.ttf
#        resources/font/ZenMaruGothic-Bold.ttf
#        resources/emoji/seedling.svg
#        resources/bgm/chopin_op10-4.flac
#   2. VOICEVOX ENGINE を起動しておくこと（既定は http://127.0.0.1:50021）。
#   3. Node.js / Python3 / ffmpeg が使えること。
#
# 差し替え可能な環境変数（無指定なら既定値）：
#   VOICEVOX_HOST  … VOICEVOX ENGINE の URL（既定 http://127.0.0.1:50021）※synth.py が参照
#   CHROMIUM_PATH  … Chrome/Chromium 実行ファイル（既定 /tmp/chromium＝setup_chromium.js の展開先）
#   CHROMIUM_LD_PATH … render 時に LD_LIBRARY_PATH へ前置するパス（サンドボックス環境で NSS 等を補うとき用）
#   FFMPEG         … ffmpeg 実行ファイル（既定 PATH 上の ffmpeg）
#   BGM_VOL        … BGM の基準音量 0〜1（既定 0.45）
#
# 出力：
#   sarracenia.mp4 … 完成動画
#   （その他 assets.js / full.wav / timeline.json / frames/ / tachie/ などは中間生成物なのだぁ）
# =============================================================================
set -euo pipefail
cd "$(dirname "$0")"

FFMPEG="${FFMPEG:-ffmpeg}"
BGM_VOL="${BGM_VOL:-0.45}"

# --- 立ち絵 PSD から切り出すレイヤー ID の一覧なのだぁ -------------------------
# この並びは scene.html の TACHIE 定義と一致していなければならないのだぁ。
# ID 体系は劇場の layers.json と同じで、兄弟レイヤーの 1 始まりインデックスを "-" で連結したものなのだぁ
# （グループもインデックスを 1 個消費するのだぁ）。
ZUNDA_IDS="1,4-2,8-5,8-2,8-3,7-14-3,7-14-4-9,7-6,5-8,5-2,5-1,6-6,9-2,4-4-8,4-4-3,4-4-6,4-4-5,4-3-8,4-3-3,10-3"
TSUMUGI_IDS="2,3,4,12,13-3,13-4,9-4,9-1,9-2,9-3,8-6-2,8-6-3-6,8-4,10-14,10-8,10-10,7-2,6-3,5-5,5-1,5-3,13-2"

# --- 0. 外部取得リソースが置かれているか確認するのだぁ -----------------------
need() { [ -f "$1" ] || { echo "ERROR: リソースが見つからないのだぁ: $1 (README.md の指示どおり置いてほしいのだぁ)"; exit 1; }; }
need resources/psd/zundamon23.psd
need resources/psd/tsumugi3.psd
need resources/font/ZenMaruGothic-Black.ttf
need resources/font/ZenMaruGothic-Bold.ttf
need resources/emoji/seedling.svg
need resources/bgm/chopin_op10-4.flac

# --- 1. Node 依存関係をそろえるのだぁ ----------------------------------------
if [ ! -d node_modules ]; then
  echo "== npm install =="
  npm install
fi

# --- 2. Chromium を用意するのだぁ（CHROMIUM_PATH 未指定かつ /tmp/chromium が無いときだけ） -----
if [ -z "${CHROMIUM_PATH:-}" ] && [ ! -x /tmp/chromium ]; then
  echo "== setup chromium =="
  node setup_chromium.js
fi

# --- 3. VOICEVOX で音声を合成するのだぁ（script.json → audio/*.wav, moras.json, kana.json） ---
echo "== synth (VOICEVOX) =="
python3 synth.py

# --- 4. 各 wav を結合してタイムラインを作るのだぁ（→ full.wav, timeline.json） -----------
echo "== assemble =="
python3 assemble.py

# --- 5. scene.html が読む assets.js を焼くのだぁ（フォント・絵文字・テクスチャ・タイムライン） ---
echo "== bake assets.js =="
python3 bake_assets.py

# --- 6. 立ち絵 PSD から必要なレイヤーを PNG に切り出すのだぁ（→ tachie/<char>/<id>.png） -----
echo "== extract tachie =="
node extract_tachie.js zundamon resources/psd/zundamon23.psd "$ZUNDA_IDS" tachie/zundamon
node extract_tachie.js tsumugi  resources/psd/tsumugi3.psd  "$TSUMUGI_IDS" tachie/tsumugi

# --- 7. Chromium で 1 フレームずつ撮って frames/ に書き出すのだぁ ---------------
echo "== render frames =="
if [ -n "${CHROMIUM_LD_PATH:-}" ]; then
  LD_LIBRARY_PATH="${CHROMIUM_LD_PATH}:${LD_LIBRARY_PATH:-}" node render.js
else
  node render.js
fi

# --- 8. フレーム＋ナレーション＋BGM を合成して mp4 にするのだぁ -----------------
echo "== build video =="
FFMPEG="$FFMPEG" python3 build_video.py "$BGM_VOL"

echo "== done: $(pwd)/sarracenia.mp4 =="
