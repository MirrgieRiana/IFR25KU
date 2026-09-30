# frozen_string_literal: true

# =============================================================================
# paper.rb — Paper Block Tag for Jekyll
# =============================================================================
#
# 論文状のブロックを表示するためのLiquidカスタムブロックタグ。
#
# ## 基本的な使い方
#
#   {% paper %}
#   論文の本文（Markdown記法使用可能）
#   {% endpaper %}
#
# ## HTML出力構造
#
#   <div class="paper" markdown="1">
#     （ブロック内のテキスト、kramdownによりMarkdown処理される）
#   </div>
#
# =============================================================================

module Paper

  # {% paper %}...{% endpaper %} ブロックタグの実装。
  # ブロック内容を <div class="paper" markdown="1"> で包んで出力する。
  # markdown="1" を付けることで、kramdownがブロック内容をMarkdownとして処理する。
  class PaperTag < Liquid::Block
    def render(context)
      content = super
      <<~HTML
        <div class="paper" markdown="1">
        #{content}
        </div>
      HTML
    end
  end
end

# タグ "paper" を Liquid に登録する
Liquid::Template.register_tag("paper", Paper::PaperTag)
