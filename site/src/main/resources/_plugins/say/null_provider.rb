# frozen_string_literal: true

# =============================================================================
# say/null_provider.rb — "null" キャラクター Provider
# =============================================================================
#
# 顔アイコンを持たない吹き出し用の Provider なのだ～🌱
# resolve が空文字列を返して、tail? が false を返すから、吹き出しのトゲも出ないのだ～🌱
# .say__face div 自体は say.rb が空のまま出力して、flex レイアウトの幅を確保するのだ～🌱
#
# ## 使用例
#
#   {% say null %}*ドーン！*{% endsay %}
#   {% say null:color=#ff0000 %}*ドーン！*{% endsay %}
#
# ## パラメータ
#
#   "color" — 吹き出しの枠線色（16進数カラーコード、デフォルト: "#cccccc"）
#
# =============================================================================

module Say
  class NullProvider
    # プリセット名からパラメータハッシュへのマッピングを返すのだ～🌱
    def presets
      {}
    end

    # 吹き出し枠線に使うキャラクター色を返すのだ～🌱
    def color(params)
      params["color"] || "#999999"
    end

    # 吹き出しのトゲを表示するか否かを返すのだ～🌱
    def tail?
      false
    end

    # 顔部分の HTML を返すのだ～🌱
    # 空文字列を返すから、顔アイコンは出ないのだ～🌱
    def resolve(_params, _context = nil)
      ""
    end
  end
end

# "null" キャラクターを登録するのだ～🌱
Say.register_character("null", Say::NullProvider.new)
