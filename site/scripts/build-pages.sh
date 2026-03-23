#!/usr/bin/env bash
set -euo pipefail

# 非対話シェルでは .bashrc が読まれないため、rbenv があれば手動で初期化する
if [ -d "$HOME/.rbenv" ]; then
    eval "$("$HOME/.rbenv/bin/rbenv" init - bash)"
fi

cd -- "$(dirname -- "$0")/../build/pages"

bundle config set --local path vendor/bundle
bundle install
bundle exec jekyll build --destination _site

# CHANGELOG.mdをmd版として同封
cp CHANGELOG.md _site/CHANGELOG.md
