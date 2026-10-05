# =============================================================================
# Makefile — video/ の雑多パートの、動画プロジェクトに依存しない段を並べたものなのだ～🌱
# -----------------------------------------------------------------------------
# video/ は 3 つのパートに分かれているのだ～🌱：
#   1. renderer/ と extract-portrait/ … 動画に依存しない汎用のツールなのだ～🌱
#   2. projects/   … 動画ごとの台本・テンプレート・構成jsonl を作る「動画プロジェクト」なのだ～🌱
#   3. video/ 直下 … 音声合成・動画合成などの「雑多な部分」で、main.xa1 が 1 と 2 を呼び出すのだ～🌱
#
# 動画を作るときは、その動画プロジェクトの Makefile を叩くのだ～🌱
# この Makefile は、その動画プロジェクトの Makefile から include されて、段の定義を渡すのだ～🌱
#
# 前提（先に済ませておくのだ～🌱 詳しくは README.md を読むのだ～🌱）：
#   1. 外部取得リソースを projects/common/resources/ 以下の所定パスに置くのだ～🌱（*.md5 がその置き場所と中身を示すのだ～🌱）
#   2. VOICEVOX ENGINE を起動しておくのだ～🌱（既定は http://127.0.0.1:50021）
#   3. Node.js / ffmpeg / curl を使える状態にしておくのだ～🌱
# =============================================================================

# この Makefile の置き場所（video/）の絶対パスなのだ～🌱
# どのディレクトリから make を呼んでも、同じ場所を指すのだ～🌱
VIDEO := $(patsubst %/,%,$(dir $(abspath $(lastword $(MAKEFILE_LIST)))))
XARPITE := $(VIDEO)/../xarpite/xarpite

# 差し替え可能な設定なのだ～🌱
# VOICEVOX_HOST と CHROMIUM_PATH と CHROMIUM_LD_PATH は、環境変数のまま xa1 と render.sh へ渡るのだ～🌱
FFMPEG ?= ffmpeg
BGM_VOL ?= 0.45

# 段の中身は、動画プロジェクトの main.xa1 が返す関数なのだ～🌱
# PROJECT は、include する側の Makefile が、自分の置き場所を入れるのだ～🌱
XA = $(XARPITE) -A 5 -q -e 'USE("$(PROJECT)/main")::run("$(1)")'

.PHONY: all script audio timeline scene frames movie clean

all: movie

# --- 生成物の段を 1 個ずつ叩けるようにするのだ～🌱 --------------------------------
script:
	$(call XA,generateScript)

audio:
	$(call XA,generateAudio)

timeline:
	$(call XA,generateTimeline)

scene:
	$(call XA,generateScene)

frames:
	$(call XA,generateFrames)

# --- フレームとナレーションと BGM を合成して mp4 にする段なのだ～🌱 --------------
movie:
	FFMPEG=$(FFMPEG) $(XARPITE) -A 5 -q -e 'USE("$(PROJECT)/main")::run("generateMovie")' $(BGM_VOL)

# --- 生成物をまとめて捨てる段なのだ～🌱 -------------------------------------------
clean:
	$(call XA,clean)
