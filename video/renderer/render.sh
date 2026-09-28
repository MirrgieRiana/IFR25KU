#!/usr/bin/env bash
# Node の依存と Chromium を用意してから、render.js で連番のフレーム画像をレンダリングするのだ～🌱
# 使い方と環境変数は、README.md に書いてあるのだ～🌱
set -euo pipefail

if [ "$#" -ne 3 ]; then
  echo 'usage: bash render.sh <template.html> <frames.jsonl> <outDir>' >&2
  exit 1
fi

# 引数のパスは呼び出し元のカレントディレクトリを基準にするから、ここへは移動せずに場所だけを覚えるのだ～🌱
RENDERER_DIR="$(cd -- "$(dirname -- "$0")" && pwd)"

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

# --- レンダリング ---
echo "== render frames =="
if [ -n "${CHROMIUM_LD_PATH:-}" ]; then
  LD_LIBRARY_PATH="${CHROMIUM_LD_PATH}:${LD_LIBRARY_PATH:-}" node "$RENDERER_DIR/render.js" "$1" "$2" "$3"
else
  node "$RENDERER_DIR/render.js" "$1" "$2" "$3"
fi
