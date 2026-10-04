# =============================================================================
# Makefile — サラセニア寸劇動画を組み立てる段を並べたものなのだ～🌱
# -----------------------------------------------------------------------------
# video/ は 3 つのパートに分かれているのだ～🌱：
#   1. renderer/ と extract-portrait/ … 動画に依存しない汎用のツールなのだ～🌱
#   2. projects/   … 動画ごとの台本・テンプレート・構成jsonl を作る「動画プロジェクト」なのだ～🌱
#   3. video/ 直下 … 音声合成・動画合成などの「雑多な部分」で、main.xa1 が 1 と 2 を呼び出すのだ～🌱
#
# 段の中身は main.xa1 の generate～ が持っていて、前の段は、その段の関数が自分で呼ぶのだ～🌱
# だから、どの段を叩いても、足りない生成物だけが作られるのだ～🌱
#
# 前提（先に済ませておくのだ～🌱 詳しくは README.md を読むのだ～🌱）：
#   1. 外部取得リソースを projects/common/resources/ 以下の所定パスに置くのだ～🌱（*.md5 がその置き場所と中身を示すのだ～🌱）
#   2. VOICEVOX ENGINE を起動しておくのだ～🌱（既定は http://127.0.0.1:50021）
#   3. Node.js / ffmpeg / curl を使える状態にしておくのだ～🌱
#
# 出力：
#   build/movie/sarracenia.mp4 … 完成動画
# =============================================================================

# この Makefile の置き場所（video/）の絶対パスなのだ～🌱
# どのディレクトリから make を呼んでも、同じ場所を指すのだ～🌱
VIDEO := $(patsubst %/,%,$(dir $(abspath $(lastword $(MAKEFILE_LIST)))))
XARPITE := $(VIDEO)/../xarpite/xarpite

# 差し替え可能な設定なのだ～🌱
# VOICEVOX_HOST と CHROMIUM_PATH と CHROMIUM_LD_PATH は、環境変数のまま xa1 と render.sh へ渡るのだ～🌱
FFMPEG ?= ffmpeg
BGM_VOL ?= 0.45

.PHONY: all script audio timeline scene frames movie clean

all: movie

# --- 生成物の段を 1 個ずつ叩けるようにするのだ～🌱 --------------------------------
script:
	$(XARPITE) -A 5 -q -e 'USE("$(VIDEO)/main").generateScript()'

audio:
	$(XARPITE) -A 5 -q -e 'USE("$(VIDEO)/main").generateAudio()'

timeline:
	$(XARPITE) -A 5 -q -e 'USE("$(VIDEO)/main").generateTimeline()'

scene:
	$(XARPITE) -A 5 -q -e 'USE("$(VIDEO)/main").generateScene()'

frames:
	$(XARPITE) -A 5 -q -e 'USE("$(VIDEO)/main").generateFrames()'

# --- フレームとナレーションと BGM を合成して mp4 にする段なのだ～🌱 --------------
movie:
	FFMPEG=$(FFMPEG) $(XARPITE) -A 5 -q -e 'USE("$(VIDEO)/main").generateMovie()' $(BGM_VOL)

# --- 生成物をまとめて捨てる段なのだ～🌱 -------------------------------------------
clean:
	$(XARPITE) -A 5 -q -e 'USE("$(VIDEO)/main").clean()'
