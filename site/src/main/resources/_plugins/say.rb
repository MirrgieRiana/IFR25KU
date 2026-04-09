# frozen_string_literal: true

# =============================================================================
# say.rb — Character Balloon (Speech Bubble) Plugin for Jekyll
# =============================================================================
#
# Markdown上でキャラクターの顔付き吹き出しを表示するためのLiquidカスタムブロックタグ。
#
# ## 基本的な使い方
#
#   {% say empty:zundamon %}
#   公式サイトが公開されたのだ！
#   {% endsay %}
#
# ## markup構文
#
#   {% say <キャラ名>[:<プリセット>...][:<key>=<value>...] %}
#   テキスト（Markdown記法使用可能）
#   {% endsay %}
#
#   - キャラ名:   Say.register_character で登録されたキャラクターの識別名（必須）
#   - プリセット: キャラクターが定義するパラメータのプリセット名（複数指定可能）
#   - key=value:  個別パラメータの直接指定（プリセットを上書きする）
#                 値には ":" 以外の任意の文字が使用可能
#
# ## markup構文の例
#
#   {% say empty %}                    — デフォルトパラメータで表示
#   {% say empty:zundamon %}           — "zundamon" プリセットを適用
#   {% say empty:color=#ff0000 %}      — 色を直接指定
#   {% say empty:zundamon:color=#fff %} — プリセット適用後に色を上書き
#
# ## パラメータの解決順序
#
#   1. 空のハッシュから開始
#   2. 指定されたプリセットを左から順にマージ
#   3. key=value の直接指定で上書き
#   4. 解決済みパラメータをキャラクターの resolve メソッドに渡す
#
# ## キャラクターの追加方法
#
#   1. 別ファイルに Provider クラスを作成し、presets と resolve を実装する
#   2. Say.register_character でキャラ名とインスタンスを登録する
#      （_plugins/ 内の .rb ファイルは Jekyll が自動的に読み込む）
#
# ## Provider インターフェース
#
#   presets  — プリセット名からパラメータハッシュへのマッピングを返す
#   resolve(params, context) — 解決済みパラメータとLiquidコンテキストを受け取り、顔部分のHTML文字列を返す
#
# ## HTML出力構造
#
#   <div class="say">
#     <div class="say__face">
#       （Provider#resolve が返すHTML）
#     </div>
#     <div class="say__balloon" markdown="1">
#       （ブロック内のテキスト、kramdownによりMarkdown処理される）
#     </div>
#   </div>
#
# =============================================================================

module Say

  # ===========================================================================
  # markup パーサー
  # ===========================================================================

  # Liquid タグの markup 文字列をパースし、キャラ名・プリセット・直接指定に分解する。
  #
  # markup の形式: "<キャラ名>[:<プリセット>...][:<key>=<value>...]"
  #   - ":" で区切られた各セグメントのうち、先頭はキャラ名として扱う
  #   - "=" を含むセグメントは key=value の直接指定として扱う
  #   - "=" を含まないセグメントはプリセット名として扱う
  #
  # 戻り値: [character_name, presets, overrides]
  #   - character_name: String — キャラクターの識別名
  #   - presets:        Array<String> — プリセット名のリスト（指定順）
  #   - overrides:      Hash<String, String> — 直接指定されたパラメータ
  def self.parse_markup(markup)
    parts = markup.strip.split(":")
    character = parts.shift

    presets = []
    overrides = {}

    parts.each do |part|
      if part.include?("=")
        # "key=value" 形式 → 直接指定として記録
        key, value = part.split("=", 2)
        overrides[key] = value
      else
        # プリセット名として記録
        presets << part
      end
    end

    [character, presets, overrides]
  end

  # ===========================================================================
  # Liquid ブロックタグ
  # ===========================================================================

  # {% say ... %}...{% endsay %} ブロックタグの実装。
  #
  # 初期化時に markup をパースし、レンダリング時に以下の処理を行う:
  #   1. キャラ名から Provider を引く
  #   2. プリセットと直接指定からパラメータを解決する
  #   3. Provider#resolve で顔部分の HTML を生成する
  #   4. ブロック内容と合わせて吹き出しの HTML を組み立てる
  class SayTag < Liquid::Block
    def initialize(tag_name, markup, tokens)
      super
      @character_name, @presets, @overrides = Say.parse_markup(markup)
    end

    def render(context)
      # キャラクター Provider の取得
      provider = CHARACTERS[@character_name]
      unless provider
        return "<div class=\"say say--error\">Unknown character: #{@character_name}</div>"
      end

      # パラメータの解決: プリセットを順にマージし、直接指定で上書き
      params = {}
      @presets.each do |preset_name|
        preset = provider.presets[preset_name]
        params.merge!(preset) if preset
      end
      params.merge!(@overrides)

      # 顔部分の HTML を生成
      face_html = provider.resolve(params, context)

      # キャラクター色を取得し、CSS カスタムプロパティとして .say に付与する
      character_color = provider.color(params)
      style = character_color ? %( style="--say-color: #{character_color}") : ""

      # ブロック内容を取得（Liquid の処理済み、Markdown は未処理）
      content = super

      # 吹き出しの HTML を組み立てる
      # say__balloon に markdown="1" を付けることで、
      # kramdown がブロック内容を Markdown として処理する
      <<~HTML
        <div class="say"#{style}>
          <div class="say__face">
            #{face_html}
          </div>
          <div class="say__balloon" markdown="1">
        #{content}
          </div>
        </div>
      HTML
    end
  end
end

# タグ "say" を Liquid に登録する
Liquid::Template.register_tag("say", Say::SayTag)
