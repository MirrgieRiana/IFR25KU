# frozen_string_literal: true

# =============================================================================
# paper_columns.rb — Paper Multi-Column Tag for Jekyll
# =============================================================================
#
# 論文の段組みを表示するためのLiquidカスタムブロックタグ。
#
# 段の分かれ目を記事の側で指定するのではなく、ブロック内容が通常フローのまま
# 左の段から右の段へ流れる、CSSの段組みによって実現する。
#
# ## 基本的な使い方
#
#   {% paper_columns %}
#   本文（Markdown記法使用可能）
#   {% endpaper_columns %}
#
# ## HTML出力構造
#
#   <div class="paper__columns" markdown="1">
#     （ブロック内のテキスト、kramdownによりMarkdownとして処理される）
#   </div>
#
# =============================================================================

module Paper

  # {% paper_columns %}...{% endpaper_columns %} ブロックタグの実装。
  # ブロック内容を、左右に分かれた段へ流し込む。
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
