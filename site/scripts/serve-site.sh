#!/usr/bin/env bash
set -euo pipefail

# 非対話シェルでは .bashrc が読まれないため、rbenv があれば手動で初期化する
if [ -d "$HOME/.rbenv" ]; then
    eval "$("$HOME/.rbenv/bin/rbenv" init - bash)"
fi

cd -- "$(dirname -- "$0")/../build/jekyllSource"

bundle exec jekyll serve --skip-initial-build --no-watch --destination ../site
