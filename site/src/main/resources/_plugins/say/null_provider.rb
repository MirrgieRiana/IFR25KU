# frozen_string_literal: true

# =============================================================================
# say/null_provider.rb — "null" キャラクター Provider
# =============================================================================
#
# 顔アイコンを持たない吹き出し用の Provider。
# resolve は空の .say__face div（プレースホルダー）を返し、
# tail? が false を返すことで吹き出しのトゲも表示されない。
#
# ## 使用例
#
#   {% say null %}*ドーン！*{% endsay %}
#   {% say null:color=#ff0000 %}*ドーン！*{% endsay %}
#
# ## パラメータ
#
#   "color" — 吹き出しの枠線色（16進数カラーコード）
#             未指定時はテーマのデフォルト枠線色が使用される
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
      params["color"]
    end

    # 吹き出しのトゲを表示するか否かを返す。
    def tail?
      false
    end

    # 顔部分の HTML を返す。空の .say__face div を返すことで、
    # flex レイアウトの幅を確保しつつ顔アイコンは表示しない。
    def resolve(_params, _context = nil)
      Say.face_html
    end
  end
end

# "null" キャラクターを登録する
Say.register_character("null", Say::NullProvider.new)
