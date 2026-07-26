#!/usr/bin/env bash
set -euo pipefail

# 非対話シェルでは .bashrc が読まれないため、rbenv があれば手動で初期化する
if [ -d "$HOME/.rbenv" ]; then
    eval "$("$HOME/.rbenv/bin/rbenv" init - bash)"
fi

SITE_DIR="$(cd -- "$(dirname -- "$0")/.." && pwd)"

export BUNDLE_APP_CONFIG="$SITE_DIR/build/bundleConfig"

cd -- "$SITE_DIR/build/jekyllSource"

bundle exec jekyll build --destination ../jekyllBuild
