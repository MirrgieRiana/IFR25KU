#!/usr/bin/env bash

# 画像をwebp形式に変換し、build/ 以下に出力する。
#
# 使い方:
#   scripts/convert-image.sh <input-file> [slug]
#
# 引数:
#   input-file  変換元の画像ファイルパス
#   slug        出力ファイル名（拡張子なし、[a-zA-Z0-9_.-]+ のみ）
#               省略時は入力ファイルの拡張子を除いた名前を使用
#
# 出力:
#   build/<slug>.webp
#
# 必要なツール:
#   ImageMagick (convert)
#
# 例:
#   scripts/convert-image.sh /path/to/image.png banner
#   -> build/banner.webp
#
#   scripts/convert-image.sh /path/to/2026-03-22_15.11.57.png
#   -> build/2026-03-22_15.11.57.webp

set -euo pipefail

if [ $# -lt 1 ] || [ $# -gt 2 ]; then
    echo "Usage: $0 <input-file> [slug]" >&2
    exit 1
fi

input="$1"

if [ $# -ge 2 ]; then
    slug="$2"
else
    slug="$(basename "$input")"
    slug="${slug%.*}"
fi

if [[ ! "$slug" =~ ^[a-zA-Z0-9_.-]+$ ]]; then
    echo "Error: slug must match [a-zA-Z0-9_.-]+" >&2
    exit 1
fi

if [ ! -f "$input" ]; then
    echo "Error: file not found: $input" >&2
    exit 1
fi

if ! command -v convert > /dev/null 2>&1; then
    echo "Error: ImageMagick (convert) is not installed" >&2
    exit 1
fi

output="build/${slug}.webp"
mkdir -p build

convert "$input" -quality 80 "$output"
echo "$output"
