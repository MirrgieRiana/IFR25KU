# frozen_string_literal: true

# =============================================================================
# paper_column.rb — Paper Column Tag for Jekyll
# =============================================================================
#
# 論文の段組みの、1個の段を表すLiquidカスタムブロックタグ。
#
# paper_columns の直下に、段の個数だけ並べて使用する。
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
#   <div class="paper__column" markdown="1">
#     （ブロック内のテキスト、kramdownによりMarkdownとして処理される）
#   </div>
#
# =============================================================================

module Paper

  # {% paper_column %}...{% endpaper_column %} ブロックタグの実装。
  # ブロック内容を、段組みの中の1個の段として出力する。
  class PaperColumnTag < Liquid::Block
    # Liquidは内容が空白のみのブロックの描画自体を省くため、中身を持たない段が消える
    def blank?
      false
    end

    def render(context)
      content = super.strip
      <<~HTML
        <div class="paper__column" markdown="1">
        #{content}

        </div>
      HTML
    end
  end
end

# タグ "paper_column" を Liquid に登録する
Liquid::Template.register_tag("paper_column", Paper::PaperColumnTag)
