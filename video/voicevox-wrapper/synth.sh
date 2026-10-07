#!/usr/bin/env bash
# synth.js で、合成のリクエストを並べた jsonl から、VOICEVOX ENGINE で行ごとの音声を合成するのだ～🌱
# 使い方と環境変数は、README.md に書いてあるのだ～🌱
set -euo pipefail

if [ "$#" -ne 2 ]; then
  echo 'usage: bash synth.sh <requests.jsonl> <outDir>' >&2
  exit 1
fi

# 引数のパスは呼び出し元のカレントディレクトリを基準にするから、ここへは移動せずに場所だけを覚えるのだ～🌱
WRAPPER_DIR="$(cd -- "$(dirname -- "$0")" && pwd)"

node "$WRAPPER_DIR/synth.js" "$1" "$2"
