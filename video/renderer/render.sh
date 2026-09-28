#!/usr/bin/env bash
# =============================================================================
# render.sh — Node の依存と Chromium を用意してから、render.js で連番フレーム画像を撮るのだ～🌱
# -----------------------------------------------------------------------------
# 使い方：
#   bash render.sh <template.html> <frames.jsonl> <outDir>
#     引数の意味は render.js と同じなのだ～🌱
#
# 環境変数：
#   CHROMIUM_PATH    … Chrome/Chromium 実行ファイルなのだ～🌱
#                      指定が無くて /tmp/chromium も無いときは、setup_chromium.js で /tmp/chromium に展開するのだ～🌱
#   CHROMIUM_LD_PATH … render.js を走らせるときに LD_LIBRARY_PATH へ前置するパスなのだ～🌱
#   VIDEO_WIDTH / VIDEO_HEIGHT … そのまま render.js が読むのだ～🌱
# =============================================================================
set -euo pipefail

if [ "$#" -ne 3 ]; then
  echo 'usage: bash render.sh <template.html> <frames.jsonl> <outDir>' >&2
  exit 1
fi

# 引数のパスは呼び出し元のカレントディレクトリを基準にするから、ここへは移動せずに場所だけを覚えるのだ～🌱
RENDERER_DIR="$(cd "$(dirname "$0")" && pwd)"

# --- Node の依存 ---
if [ ! -d "$RENDERER_DIR/node_modules" ]; then
  echo "== npm install (renderer) =="
  (cd "$RENDERER_DIR" && npm install)
fi

# --- Chromium ---
if [ -z "${CHROMIUM_PATH:-}" ] && [ ! -x /tmp/chromium ]; then
  echo "== setup chromium =="
  node "$RENDERER_DIR/setup_chromium.js"
fi

# --- 撮影 ---
echo "== render frames =="
if [ -n "${CHROMIUM_LD_PATH:-}" ]; then
  LD_LIBRARY_PATH="${CHROMIUM_LD_PATH}:${LD_LIBRARY_PATH:-}" node "$RENDERER_DIR/render.js" "$1" "$2" "$3"
else
  node "$RENDERER_DIR/render.js" "$1" "$2" "$3"
fi
