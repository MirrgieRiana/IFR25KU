#!/usr/bin/env bash

# 画像をwebp形式に変換し、site/build/convertedWebp/ 以下に出力する。
#
# 使い方:
#   site/scripts/convert-to-webp.sh <input-file> [slug]
#
# 引数:
#   input-file  変換元の画像ファイルパス
#   slug        出力ファイル名（拡張子なし、[a-zA-Z0-9_.-]+ のみ）
#               省略時は入力ファイルの拡張子を除いた名前を使用
#
# 出力:
#   site/build/convertedWebp/<slug>.webp
#
# 必要なツール:
#   ImageMagick (convert)
#
# 例:
#   site/scripts/convert-to-webp.sh /path/to/image.png banner
#   -> site/build/convertedWebp/banner.webp
#
#   site/scripts/convert-to-webp.sh /path/to/2026-03-22_15.11.57.png
#   -> site/build/convertedWebp/2026-03-22_15.11.57.webp

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

output="site/build/convertedWebp/${slug}.webp"
mkdir -p site/build/convertedWebp

convert "$input" -quality 80 "$output"
echo "$output"
