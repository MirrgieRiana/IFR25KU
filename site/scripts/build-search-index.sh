#!/usr/bin/env bash
set -euo pipefail

SITE_DIR="$(cd -- "$(dirname -- "$0")/.." && pwd)"

rm -rf "$SITE_DIR/build/searchIndex"
npx --yes pagefind@1.5.2 --site "$SITE_DIR/build/siteWithoutSearchIndex" --output-path "$SITE_DIR/build/searchIndex/pagefind" --force-language ja
