# frozen_string_literal: true

# =============================================================================
# space.rb — Scene Break Tag for Jekyll
# =============================================================================
#
# Markdown上で場面転換を示す区切りを挿入するためのLiquidカスタムインラインタグなのだ～🌱
#
# ## 基本的な使い方
#
#   {% space %}
#
# ## HTML出力構造
#
#   <div class="space"></div>
#
# =============================================================================

module Space

  # {% space %} インラインタグの実装。
  # パラメータは無くて、呼び出すだけで場面転換の区切りHTMLを挿入するのだ～🌱
  class SpaceTag < Liquid::Tag
    def render(context)
      '<div class="space"></div>'
    end
  end
end

# タグ "space" を Liquid に登録するのだ～🌱
Liquid::Template.register_tag("space", Space::SpaceTag)
