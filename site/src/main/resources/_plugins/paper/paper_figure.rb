# frozen_string_literal: true

# =============================================================================
# paper_figure.rb — Paper Figure Tag for Jekyll
# =============================================================================
#
# 論文中の、キャプション付きの画像を表示するためのLiquidカスタムインラインタグ。
#
# 画像は、既定では紙面や段の幅いっぱいに掲げる。
# actual_size を添えると、代わりに画像自身が持つ寸法で掲げる。
#
# 拡大したときに画素を補間するか否かは、掲げ方ではなく画像の形式が決める。
# pngは画素を保ったまま拡大され、webpは補間される。
#
# ## 基本的な使い方
#
#   {% paper_figure "miragium-axe.webp" "図１　ミラジウムの斧" %}
#   {% paper_figure "deep-space-field.webp" %}
#   {% paper_figure actual_size "dictionary-entry.png" "「辞書」の項目の一例" %}
#
# ## markup構文
#
#   {% paper_figure [actual_size] "<画像のパス>" ["<キャプション>"] %}
#
#   - actual_size:  添えると、幅に合わせず、画像自身が持つ寸法で掲げる
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
#   actual_size を添えた場合、figure に paper__figure--actual-size が加わる。
#
# =============================================================================

module Paper

  # {% paper_figure ... %} インラインタグの実装。
  # 画像と、その下に置くキャプションを組み立てる。
  class PaperFigureTag < Liquid::Tag
    def initialize(tag_name, markup, options)
      super
      @source, @caption = TagArguments.parse(markup)
      @actual_size = TagArguments.flag?(markup, "actual_size")
    end

    def render(context)
      # キャプションは代替テキストを兼ねる。省略された場合、画像は装飾として扱う。
      caption_html = @caption ? %(<figcaption class="paper__caption" markdown="span">#{@caption}</figcaption>\n) : ""
      class_names = @actual_size ? "paper__figure paper__figure--actual-size" : "paper__figure"
      <<~HTML
        <figure class="#{class_names}">
        <img src="#{@source}" alt="#{@caption}">
        #{caption_html}</figure>
      HTML
    end
  end
end

# タグ "paper_figure" を Liquid に登録する
Liquid::Template.register_tag("paper_figure", Paper::PaperFigureTag)
