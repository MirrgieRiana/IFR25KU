# frozen_string_literal: true

# =============================================================================
# say/empty_provider.rb — "empty" キャラクター Provider
# =============================================================================
#
# スタブ用のキャラクター Provider。
# 枠線付きの単色円を SVG で描画する。
# 将来キャラクターの顔素材が用意されるまでの仮実装として使用する。
#
# ## 使用例
#
#   {% say empty %}              — デフォルト色（グレー）の円
#   {% say empty:zundamon %}     — ずんだもんカラー（緑）の円
#   {% say empty:color=#ff0000 %} — 赤い円
#
# ## パラメータ
#
#   "color" — 円の塗りつぶし色（16進数カラーコード、デフォルト: "#cccccc"）
#             枠線色は塗りつぶし色から自動導出される
#
# ## プリセット
#
#   キャラクター名や色名で、よく使う色を簡単に指定できる。
#
# =============================================================================

module Say
  class EmptyProvider
    PRESETS = {
      "zundamon" => { "color" => "#90ee90" },
      "metan"    => { "color" => "#ffccdd" },
      "red"      => { "color" => "#ff6666" },
      "blue"     => { "color" => "#6666ff" },
      "green"    => { "color" => "#66ff66" },
      "yellow"   => { "color" => "#ffff66" },
      "white"    => { "color" => "#ffffff" },
      "black"    => { "color" => "#333333" },
      "gray"     => { "color" => "#cccccc" },
    }.freeze

    # プリセット名からパラメータハッシュへのマッピングを返す。
    def presets
      PRESETS
    end

    # 吹き出し枠線に使うキャラクター色を返す。
    def color(params)
      fill = params["color"] || "#cccccc"
      Say.derive_border_color(fill)
    end

    # 吹き出しのトゲを表示するか否かを返す。
    def tail?
      true
    end

    # 解決済みパラメータから、顔部分の HTML（SVG）を生成して返す。
    def resolve(params, _context = nil)
      color = params["color"] || "#cccccc"
      border_color = Say.derive_border_color(color)
      <<~SVG
        <svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" viewBox="0 0 100 100">
          <circle cx="50" cy="50" r="44" fill="#{color}" stroke="#{border_color}" stroke-width="6"/>
        </svg>
      SVG
    end
  end
end

# "empty" キャラクターを登録する
Say.register_character("empty", Say::EmptyProvider.new)
