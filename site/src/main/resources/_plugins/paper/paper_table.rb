# frozen_string_literal: true

# =============================================================================
# paper_table.rb — Paper Table Tag for Jekyll
# =============================================================================
#
# 論文中の、キャプション付き表を表示するためのLiquidカスタムブロックタグなのだ～🌱
#
# ## 基本的な使い方
#
#   {% paper_table "表２　ミラージュの成分" %}
#   | 成分 | 質量％ |
#   |---|---|
#   | ガラス質ミラジウム | 42% |
#   {% endpaper_table %}
#
# ## markup構文
#
#   {% paper_table ["<キャプション>"] %}
#   表（Markdownのパイプ記法）
#   {% endpaper_table %}
#
#   - キャプション: 表の上に置かれる説明なのだ～🌱
#                 省略するとキャプションを出力しないのだ～🌱
#
# ## HTML出力構造
#
#   <div class="paper__table" markdown="1">
#   <div class="paper__caption" markdown="span">（キャプション）</div>
#   （ブロック内の表、kramdownによりMarkdownとして処理される）
#   </div>
#
# =============================================================================

module Paper

  # {% paper_table ... %}...{% endpaper_table %} ブロックタグの実装。
  # キャプションと、その下に置く表を構成するのだ～🌱
  class PaperTableTag < Liquid::Block
    def initialize(tag_name, markup, options)
      super
      @caption, = TagArguments.parse(markup)
    end

    def render(context)
      content = super.strip
      caption_html = @caption ? %(<div class="paper__caption" markdown="span">#{@caption}</div>\n\n) : ""
      # 中身と閉じタグの間の空行が無いと、kramdown が末尾のブロックを表として解釈してくれないのだぁ…🌧️
      <<~HTML
        <div class="paper__table" markdown="1">
        #{caption_html}#{content}

        </div>
      HTML
    end
  end
end

# タグ "paper_table" を Liquid に登録するのだ～🌱
Liquid::Template.register_tag("paper_table", Paper::PaperTableTag)
