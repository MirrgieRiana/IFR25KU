# frozen_string_literal: true

# =============================================================================
# paper_figure.rb — Paper Figure Tag for Jekyll
# =============================================================================
#
# 論文中の、キャプション付き画像を表示するためのLiquidカスタムインラインタグ。
#
# ## 基本的な使い方
#
#   {% paper_figure "Untitled 963.png" "図１　樹冠上のミラージュ" %}
#
# ## markup構文
#
#   {% paper_figure "<画像のパス>" ["<キャプション>"] %}
#
#   - 画像のパス:   記事のディレクトリからの相対パス（必須）
#   - キャプション: 画像の下に置かれる説明。省略した場合はキャプションを出力しない
#
# ## HTML出力構造
#
#   <figure class="paper__figure">
#   <img src="（画像のパス）" alt="（キャプション）">
#   <figcaption class="paper__caption" markdown="span">（キャプション）</figcaption>
#   </figure>
#
# =============================================================================

module Paper

  # {% paper_figure ... %} インラインタグの実装。
  # 画像と、その下に置くキャプションを組み立てる。
  class PaperFigureTag < Liquid::Tag
    def initialize(tag_name, markup, options)
      super
      @source, @caption = Paper.parse_arguments(markup)
    end

    def render(context)
      # キャプションは代替テキストを兼ねる。省略された場合、画像は装飾として扱う。
      caption_html = @caption ? %(<figcaption class="paper__caption" markdown="span">#{@caption}</figcaption>\n) : ""
      <<~HTML
        <figure class="paper__figure">
        <img src="#{@source}" alt="#{@caption}">
        #{caption_html}</figure>
      HTML
    end
  end
end

# タグ "paper_figure" を Liquid に登録する
Liquid::Template.register_tag("paper_figure", Paper::PaperFigureTag)
