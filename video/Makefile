# =============================================================================
# Makefile — サラセニア寸劇動画を組み立てる段を並べたものなのだ～🌱
# -----------------------------------------------------------------------------
# video/ は 3 つのパートに分かれているのだ～🌱：
#   1. renderer/   … 構成jsonl と HTML テンプレートから画像を撮る「汎用レンダラー」（動画に依存しないのだ～🌱）
#   2. projects/   … 動画ごとの台本・シーン・構成jsonl を作る「動画プロジェクト」（自己完結なのだ～🌱）
#   3. video/ 直下 … 音声合成・動画合成などの「雑多な部分」で、この Makefile が 1 と 2 を呼び出すのだ～🌱
#
# 段の並びなのだ～🌱：
#   scene  … main.xa1 の build（BGM の確認 → 音声合成 → 結合 → プロジェクトの構成づくり）
#   frames … renderer/render.sh（scene.html + frames.jsonl → プロジェクトの frames/）
#   movie  … scripts/build_video.py（フレーム＋音声＋BGM → sarracenia.mp4）
#
# 前提（先に済ませておくのだ～🌱 詳しくは README.md を読むのだ～🌱）：
#   1. 外部取得リソースを projects/2026-04-12-sarracenia/resources/ 以下の所定パスに置くのだ～🌱（*.md5 がその置き場所と中身を示すのだ～🌱）
#   2. VOICEVOX ENGINE を起動しておくのだ～🌱（既定は http://127.0.0.1:50021）
#   3. Node.js / Python3 / ffmpeg / curl を使える状態にしておくのだ～🌱
#
# 出力：
#   sarracenia.mp4 … 完成動画（video/ 直下）
# =============================================================================

# この Makefile の置き場所（video/）の絶対パスなのだ～🌱
# どのディレクトリから make を呼んでも、同じ場所を指すのだ～🌱
VIDEO := $(patsubst %/,%,$(dir $(abspath $(lastword $(MAKEFILE_LIST)))))
XARPITE := $(VIDEO)/../xarpite/xarpite

# 差し替え可能な設定なのだ～🌱
# VOICEVOX_HOST と CHROMIUM_PATH と CHROMIUM_LD_PATH は、環境変数のまま xa1 と render.sh へ渡るのだ～🌱
FFMPEG ?= ffmpeg
BGM_VOL ?= 0.45

.PHONY: all scene frames movie

all: movie

# --- 構成jsonl ができるまでの段なのだ～🌱 ----------------------------------------
# main.xa1 は、自分の居場所から video/ を解決して、INC と読み書きのパスをそこから組むのだ～🌱
scene:
	$(XARPITE) -A 5 -q -e 'USE("$(VIDEO)/main").build()'

# --- 汎用レンダラーで 1 フレームずつ撮る段なのだ～🌱 -----------------------------
# render.sh が、Node の依存と Chromium の用意も面倒を見るのだ～🌱
frames: scene
	rm -rf $(VIDEO)/projects/2026-04-12-sarracenia/frames
	bash $(VIDEO)/renderer/render.sh $(VIDEO)/projects/2026-04-12-sarracenia/frames.jsonl $(VIDEO)/projects/2026-04-12-sarracenia/frames

# --- フレームとナレーションと BGM を合成して mp4 にする段なのだ～🌱 --------------
movie: frames
	FFMPEG=$(FFMPEG) python3 $(VIDEO)/scripts/build_video.py $(BGM_VOL)
	@echo "== done: $(VIDEO)/sarracenia.mp4 =="
