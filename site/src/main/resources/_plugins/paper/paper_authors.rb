# frozen_string_literal: true

# =============================================================================
# paper_authors.rb — Paper Authors Tag for Jekyll
# =============================================================================
#
# 論文の著者欄を表示するためのLiquidカスタムブロックタグ。
#
# ## 基本的な使い方
#
#   {% paper_authors %}
#   シャイメツノフェ・ソディア（妖精研究所魔法植物学部）
#   {% endpaper_authors %}
#
# ## HTML出力構造
#
#   <div class="paper__authors" markdown="span">
#     （ブロック内のテキスト、kramdownによりインラインのMarkdownとして処理される）
#   </div>
#
# =============================================================================

module Paper

  # {% paper_authors %}...{% endpaper_authors %} ブロックタグの実装。
  # ブロック内容を、タイトルの下に置く中央揃えの著者欄として出力する。
  class PaperAuthorsTag < Liquid::Block
    def render(context)
      content = super.strip
      <<~HTML
        <div class="paper__authors" markdown="span">#{content}</div>
      HTML
    end
  end
end

# タグ "paper_authors" を Liquid に登録する
Liquid::Template.register_tag("paper_authors", Paper::PaperAuthorsTag)
