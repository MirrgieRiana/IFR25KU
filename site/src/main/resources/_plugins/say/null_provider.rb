# frozen_string_literal: true

# =============================================================================
# say/null_provider.rb — "null" キャラクター Provider
# =============================================================================
#
# 顔アイコンを持たない吹き出し用の Provider。
# resolve は空文字列を返し、tail? が false を返すことで吹き出しのトゲも表示されない。
# .say__face div 自体は say.rb によって空のまま出力され、flex レイアウトの幅を確保する。
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
    # プリセット名からパラメータハッシュへのマッピングを返す。
    def presets
      {}
    end

    # 吹き出し枠線に使うキャラクター色を返す。
    def color(params)
      params["color"] || "#cccccc"
    end

    # 吹き出しのトゲを表示するか否かを返す。
    def tail?
      false
    end

    # 顔部分の HTML を返す。空文字列を返すことで顔アイコンを表示しない。
    def resolve(_params, _context = nil)
      ""
    end
  end
end

# "null" キャラクターを登録する
Say.register_character("null", Say::NullProvider.new)
