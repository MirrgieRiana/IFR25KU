# frozen_string_literal: true

# =============================================================================
# paper_columns.rb — Paper Multi-Column Tag for Jekyll
# =============================================================================
#
# 論文の段組みを表示するためのLiquidカスタムブロックタグ。
#
# 個々の段は paper_column タグで表し、このタグはそれらを横に並べる役割を持つ。
# 段の個数は、中に置かれた paper_column の個数によって決まる。
#
# ## 基本的な使い方
#
#   {% paper_columns %}
#   {% paper_column %}
#   左の段の本文（Markdown記法使用可能）
#   {% endpaper_column %}
#   {% paper_column %}
#   右の段の本文（Markdown記法使用可能）
#   {% endpaper_column %}
#   {% endpaper_columns %}
#
# ## HTML出力構造
#
#   <div class="paper__columns" markdown="1">
#     （ブロック内の paper_column、kramdownによりMarkdownとして処理される）
#   </div>
#
# =============================================================================

module Paper

  # {% paper_columns %}...{% endpaper_columns %} ブロックタグの実装。
  # 中に置かれた段を、横に並べる。
  class PaperColumnsTag < Liquid::Block
    def render(context)
      content = super.strip
      <<~HTML
        <div class="paper__columns" markdown="1">
        #{content}

        </div>
      HTML
    end
  end
end

# タグ "paper_columns" を Liquid に登録する
Liquid::Template.register_tag("paper_columns", Paper::PaperColumnsTag)
