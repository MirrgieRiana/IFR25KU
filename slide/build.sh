#!/usr/bin/env bash
# ============================================================
#   スフィアクラフト紹介スライドのビルドツール
#   中で xarpite（API5）を起動して、build/site/ に slides.html と style.css を生成する。
#   - src/main/xa1/global.xa1: 共有の土台（templates・pageGenerators・cssLines）。
#   - src/plugins/xa1/*.xa1:    パーツ（素材・矢印・見出しなど）。templates と cssLines を登録する。
#   - src/pages/xa1/*.xa1:      出力ページ。pageGenerators にファイル名→内容の関数を登録する。
#   xarpite ランタイムはリポジトリ同梱の ../xarpite を相対パスで使うので、PATH 指定は不要。
# ============================================================
set -euo pipefail

# このスクリプトの場所をプロジェクトルートとして扱う。
cd "$(dirname "$0")"

SRC="src/site/resources"
OUT="build/site"

# リポジトリ同梱の xarpite ランタイム（相対パス）。
XARPITE="../xarpite/xarpite"

# 出力先を用意し、素材（png/svg）を build/site/ にそのまま配置する。
# HTML/CSS は素材を相対URL（./name.png など）で参照するので、同じディレクトリに置く。
# フォントはコミットせず、ページのheadからCDNで読み込むため、ここでは配置しない。
mkdir -p "$OUT"
cp "$SRC"/* "$OUT"/

# xarpite（API5）でプラグイン・ページを読み込み、HTML と CSS を組み立てる。
# メインは stdin 実行なので、相対パスの起点は cd 済みのプロジェクトルート（PWD）になる。
"$XARPITE" -A 5 -q -f - <<'XARPITE_EOF'
# ============================================================
#   スフィアクラフト紹介スライドのビルドメイン（xarpite / API5）
#   main・plugins・pages を INC に入れ、bare 名で USE してロードする。
#   その後、pageGenerators を回して各ページを書き出し、組み上げた CSS を style.css に書き出す。
# ============================================================

DIR := "build/site"

# main・plugins・pages を INC に入れ、bare 名の USE を各ソースセットから解決できるようにする。
INC::push << "src/main/xa1"
INC::push << "src/plugins/xa1"
INC::push << "src/pages/xa1"

# 登録簿 templates・pageGenerators・CSS行の箱 cssLines・ヘルパー（esc, 色）をマウントする。
@USE("global")

# プラグインとページを名前順に列挙して、すべて USE で読み込む。
# 各プラグインは templates と cssLines へ、各ページは pageGenerators へ登録する。
# トップレベルのストリームは -q でも消費されるので、そのまま置けば副作用が走る。
FILE_NAMES("src/plugins/xa1") >> SORT | f => USE(f)
FILE_NAMES("src/pages/xa1") >> SORT | f => USE(f)

# 各ページを書き出す（キー＝出力ファイル名、値＝内容を返す関数）。
pageGenerators() | e => WRITE[DIR & "/" & e.0] << e.1()

# 組み上げた CSS を書き出す（各行末に改行を付けたいので WRITEL）。
WRITEL[DIR & "/style.css"] << CSS()
XARPITE_EOF

echo "ビルド完了なのだ : $OUT/slides.html, $OUT/style.css"
