#!/usr/bin/env bash
# Node の依存を用意してから、extract_portrait.js で立ち絵のレイヤーを切り出すのだ～🌱
# 使い方は、README.md に書いてあるのだ～🌱
set -euo pipefail

if [ "$#" -ne 4 ]; then
  echo 'usage: bash extract-portrait.sh <char> <psd> <ids> <outDir>' >&2
  exit 1
fi

# 引数のパスは呼び出し元のカレントディレクトリを基準にするから、ここへは移動せずに場所だけを覚えるのだ～🌱
EXTRACT_DIR="$(cd -- "$(dirname -- "$0")" && pwd)"

# --- Node の依存 ---
if [ ! -d "$EXTRACT_DIR/node_modules" ]; then
  echo "== npm install (extract-portrait) =="
  (cd "$EXTRACT_DIR" && npm install)
fi

# --- 切り出し ---
node "$EXTRACT_DIR/extract_portrait.js" "$1" "$2" "$3" "$4"
