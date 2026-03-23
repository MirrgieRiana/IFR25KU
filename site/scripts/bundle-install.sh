#!/usr/bin/env bash
set -euo pipefail

# 非対話シェルでは .bashrc が読まれないため、rbenv があれば手動で初期化する
if [ -d "$HOME/.rbenv" ]; then
    eval "$("$HOME/.rbenv/bin/rbenv" init - bash)"
fi

cd -- "$(dirname -- "$0")/../src/main/bundle"

bundle config set --local path vendor/bundle
bundle install
