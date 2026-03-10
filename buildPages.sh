#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")"

./gradlew buildPages

cd build/pages
bundle install
bundle exec jekyll build --destination _site
