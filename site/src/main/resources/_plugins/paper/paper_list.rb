# frozen_string_literal: true

# =============================================================================
# paper_list.rb — Paper List Tag for Jekyll
# =============================================================================
#
# 論文中の箇条書きを表示するためのLiquidカスタムブロックタグ。
#
# ## 基本的な使い方
#
#   {% paper_list %}
#   - 人型で人間の死体状のもの
#   - 巨大な蜘蛛型のもの
#   {% endpaper_list %}
#
# ## HTML出力構造
#
#   <div class="paper__list" markdown="1">
#     （ブロック内の箇条書き、kramdownによりMarkdownとして処理される）
#   </div>
#
# =============================================================================

module Paper

  # {% paper_list %}...{% endpaper_list %} ブロックタグの実装。
  # ブロック内容を、論文の紙面に合わせた体裁の箇条書きとして出力する。
  class PaperListTag < Liquid::Block
    def render(context)
      content = super.strip
      <<~HTML
        <div class="paper__list" markdown="1">
        #{content}

        </div>
      HTML
    end
  end
end

# タグ "paper_list" を Liquid に登録する
Liquid::Template.register_tag("paper_list", Paper::PaperListTag)
