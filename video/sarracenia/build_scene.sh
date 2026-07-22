#!/usr/bin/env bash
# =============================================================================
# build_scene.sh — サラセニア寸劇の「構成」を組み立てるのだぁ🌱（動画タイトルのディレクトリの本体なのだぁ）
# -----------------------------------------------------------------------------
# このディレクトリ（sarracenia/）は、汎用レンダラー（../renderer/）にも、雑多パート（../）にも
# 依存しない、自己完結したパートなのだぁ。役割は「タイムラインを受け取って、レンダラーに渡す材料
# （assets.js・立ち絵 tachie/・構成jsonl frames.jsonl）をぜんぶ作る」ことなのだぁ🌱
#
# 入力：
#   $1 … timeline.json のパス（省略時は ../timeline.json）。尺・台詞区間・シーン・アイテムが入っているのだぁ。
#        ※ タイムラインの作り手（音声合成・結合）は雑多パートの担当で、ここはその結果を受け取るだけなのだぁ。
# 出力（このディレクトリ配下）：
#   assets.js     … scene.html が読む、フォント・絵文字・テクスチャ・タイムラインの束
#   tachie/       … 立ち絵 PSD から切り出したレイヤー PNG
#   frames.jsonl  … 1 行 = 1 フレームの構成jsonl（レンダラーへの入力）
#
# 前提：外部取得リソースを resources/ の所定パスに置くこと（*.md5 が置き場所と中身を示すのだぁ）。
# =============================================================================
set -euo pipefail
cd "$(dirname "$0")"   # sarracenia/

TIMELINE="${1:-../timeline.json}"

# --- 立ち絵 PSD から切り出すレイヤー ID の一覧なのだぁ -------------------------
# この並びは scene.html の TACHIE 定義と一致していなければならないのだぁ。
# ID 体系は劇場の layers.json と同じで、兄弟レイヤーの 1 始まりインデックスを "-" で連結したものなのだぁ
# （グループもインデックスを 1 個消費するのだぁ）。
ZUNDA_IDS="1,4-2,8-5,8-2,8-3,7-14-3,7-14-4-9,7-6,5-8,5-2,5-1,6-6,9-2,4-4-8,4-4-3,4-4-6,4-4-5,4-3-8,4-3-3,10-3"
TSUMUGI_IDS="2,3,4,12,13-3,13-4,9-4,9-1,9-2,9-3,8-6-2,8-6-3-6,8-4,10-14,10-8,10-10,7-2,6-3,5-5,5-1,5-3,13-2"

# --- 外部取得リソース（このシーンの立ち絵・フォント・絵文字）の存在を確認するのだぁ ---
need() { [ -f "$1" ] || { echo "ERROR: リソースが見つからないのだぁ: $1 (README.md の指示どおり置いてほしいのだぁ)"; exit 1; }; }
need resources/psd/zundamon23.psd
need resources/psd/tsumugi3.psd
need resources/font/ZenMaruGothic-Black.ttf
need resources/font/ZenMaruGothic-Bold.ttf
need resources/emoji/seedling.svg

# --- Node 依存（ag-psd / pngjs）をそろえるのだぁ ------------------------------
if [ ! -d node_modules ]; then
  echo "== npm install (sarracenia) =="
  npm install
fi

# --- 1. scene.html が読む assets.js を焼くのだぁ（フォント・絵文字・テクスチャ・タイムライン） ---
echo "== bake assets.js =="
python3 bake_assets.py "$TIMELINE"

# --- 2. 立ち絵 PSD から必要なレイヤーを PNG に切り出すのだぁ（→ tachie/<char>/<id>.png） -----
echo "== extract tachie =="
node extract_tachie.js zundamon resources/psd/zundamon23.psd "$ZUNDA_IDS" tachie/zundamon
node extract_tachie.js tsumugi  resources/psd/tsumugi3.psd  "$TSUMUGI_IDS" tachie/tsumugi

# --- 3. 構成jsonl を組み立てるのだぁ（→ frames.jsonl） -------------------------
echo "== compose frames.jsonl =="
python3 compose.py "$TIMELINE" frames.jsonl

echo "== scene ready: assets.js, tachie/, frames.jsonl =="
