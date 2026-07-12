#!/usr/bin/env bash
# ============================================================
#   スフィアクラフト紹介スライドのビルドツールなのだぁ🌱
#   中で xarpite（API5）を起動して、build/site/ に slides.html と style.css を生成するのだぁ〜
#   - src/main/xa1/global.xa1: 共有の土台（templates・pageGenerators・cssLines）なのだぁ🌱
#   - src/plugins/xa1/*.xa1:    パーツ（素材・矢印・見出しなど）なのだぁ🌱。templates と cssLines を登録するのだぁ〜
#   - src/pages/xa1/*.xa1:      出力ページなのだぁ🌱。pageGenerators にファイル名→内容の関数を登録するのだぁ〜
#   xarpite ランタイムはリポジトリ同梱の ../xarpite を相対パスで使うので、PATH 指定は不要なのだぁ🌱
# ============================================================
set -euo pipefail

# このスクリプトの場所をプロジェクトルートとして扱うのだぁ🌱
# 先頭が - のパスでも壊れないように、cd と dirname の両方に -- を付けるのだぁ〜
cd -- "$(dirname -- "$0")"

SRC="src/site/resources"
OUT="build/site"

# リポジトリ同梱の xarpite ランタイム（相対パス）なのだぁ🌱
XARPITE="../xarpite/xarpite"

# 出力先を用意して、素材（png/svg）を build/site/ に配置するのだぁ🌱
# 素材はライセンスごとにサブディレクトリに分けてあるので、その構造をそのまま build/site/ に持ち込むのだぁ🌱
# HTML/CSS は素材を相対URL（./item-texture/name.png など）でサブディレクトリごと参照するのだぁ〜
# フォントはコミットせず、ページのheadからCDNで読み込むため、ここでは配置しないのだぁ🌱
mkdir -p "$OUT"
cp -r -- "$SRC"/. "$OUT"/

# xarpite（API5）で global をマウントして、その build 関数にビルド一切をまかせるのだぁ🌱
# メインは stdin 実行なので、相対パスの起点は cd 済みのプロジェクトルート（PWD）になるのだぁ〜
"$XARPITE" -A 5 -q -f - <<'XARPITE_EOF'
# ============================================================
#   スフィアクラフト紹介スライドのビルドメイン（xarpite / API5）なのだぁ🌱
#   global を INC 経由でマウントして、その build 関数を呼ぶだけなのだぁ〜
#   プラグイン・ページの読み込みと書き出しは、ぜんぶ build 関数の中でやるのだぁ🌱
# ============================================================

# global を bare 名の USE で解決できるように、main のソースセットを INC に入れるのだぁ🌱
INC::push << "src/main/xa1"

# global をマウントして build 関数を取り出すのだぁ🌱
@USE("global")

# ビルド本体をまるごと build 関数にまかせるのだぁ🌱
build()
XARPITE_EOF

echo "ビルド完了なのだぁ🌱 : $OUT/slides.html, $OUT/style.css"
