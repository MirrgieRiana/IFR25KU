#!/usr/bin/env bash
set -euo pipefail

SITE_DIR="$(cd -- "$(dirname -- "$0")/.." && pwd)"

npx --yes pagefind@1.5.2 --site "$SITE_DIR/build/site" --force-language ja
