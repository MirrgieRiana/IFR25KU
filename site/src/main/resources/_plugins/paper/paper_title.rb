# frozen_string_literal: true

# =============================================================================
# paper_title.rb — Paper Title Tag for Jekyll
# =============================================================================
#
# 論文のタイトルを表示するためのLiquidカスタムブロックタグ。
#
# 目次の生成対象にしないために、見出し要素ではなくdivとして出力する。
#
# ## 基本的な使い方
#
#   {% paper_title %}
#   アストラル線バースト
#   {% endpaper_title %}
#
# ## HTML出力構造
#
#   <div class="paper__title" markdown="span">
#     （ブロック内のテキスト、kramdownによりインラインのMarkdownとして処理される）
#   </div>
#
# =============================================================================

module Paper

  # {% paper_title %}...{% endpaper_title %} ブロックタグの実装。
  # ブロック内容を、中央揃えのタイトルとして出力する。
  class PaperTitleTag < Liquid::Block
    def render(context)
      content = super.strip
      <<~HTML
        <div class="paper__title" markdown="span">#{content}</div>
      HTML
    end
  end
end

# タグ "paper_title" を Liquid に登録する
Liquid::Template.register_tag("paper_title", Paper::PaperTitleTag)
