# frozen_string_literal: true

# =============================================================================
# paper_section.rb — Paper Section Heading Tag for Jekyll
# =============================================================================
#
# 論文のセクション見出しを表示するためのLiquidカスタムブロックタグ。
#
# 目次の生成対象にしないために、見出し要素ではなくdivとして出力する。
#
# ## 基本的な使い方
#
#   {% paper_section %}
#   1. 背景
#   {% endpaper_section %}
#
# ## HTML出力構造
#
#   <div class="paper__section" markdown="span">
#     （ブロック内のテキスト、kramdownによりインラインのMarkdownとして処理される）
#   </div>
#
# =============================================================================

module Paper

  # {% paper_section %}...{% endpaper_section %} ブロックタグの実装。
  # ブロック内容を、本文中のセクション見出しとして出力する。
  class PaperSectionTag < Liquid::Block
    def render(context)
      content = super.strip
      <<~HTML
        <div class="paper__section" markdown="span">#{content}</div>
      HTML
    end
  end
end

# タグ "paper_section" を Liquid に登録する
Liquid::Template.register_tag("paper_section", Paper::PaperSectionTag)
