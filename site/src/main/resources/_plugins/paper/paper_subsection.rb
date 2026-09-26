# frozen_string_literal: true

# =============================================================================
# paper_subsection.rb — Paper Subsection Heading Tag for Jekyll
# =============================================================================
#
# 論文のセクションの下位の見出しを表示するためのLiquidカスタムブロックタグ。
#
# 目次の生成対象にしないために、見出し要素ではなくdivとして出力する。
#
# ## 基本的な使い方
#
#   {% paper_subsection %}
#   3-1 凝縮
#   {% endpaper_subsection %}
#
# ## HTML出力構造
#
#   <div class="paper__subsection" markdown="span">
#     （ブロック内のテキスト、kramdownによりインラインのMarkdownとして処理される）
#   </div>
#
# =============================================================================

module Paper

  # {% paper_subsection %}...{% endpaper_subsection %} ブロックタグの実装。
  # ブロック内容を、セクションよりも下位の見出しとして出力する。
  class PaperSubsectionTag < Liquid::Block
    def render(context)
      content = super.strip
      <<~HTML
        <div class="paper__subsection" markdown="span">#{content}</div>
      HTML
    end
  end
end

# タグ "paper_subsection" を Liquid に登録する
Liquid::Template.register_tag("paper_subsection", Paper::PaperSubsectionTag)
