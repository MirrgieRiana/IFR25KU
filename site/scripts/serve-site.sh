#!/usr/bin/env bash
set -euo pipefail

cd -- "$(dirname -- "$0")/.."

# jekyll serve は頻繁に落ちて使い物にならないのだぁ…🌧️
./scripts/serve-site.main.kts "$@"
