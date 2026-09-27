# frozen_string_literal: true

# =============================================================================
# news_figure.rb — News Figure Tag for Jekyll
# =============================================================================
#
# ニュース記事中の、キャプション付きの画像を表示するためのLiquidカスタムインラインタグ。
#
# 画像は、既定では記事や段の幅いっぱいに掲げる。
# actual_size を添えると、代わりに画像自身が持つ寸法で掲げる。
#
# 拡大したときに画素を補間するか否かは、掲げ方ではなく画像の形式が決める。
# pngは画素を保ったまま拡大され、webpは補間される。
#
# ## 基本的な使い方
#
#   {% news_figure "deep-space-field.webp" "図１　深宇宙" %}
#   {% news_figure "parallel-universe-collection.webp" %}
#   {% news_figure actual_size "dictionary-entry.png" "「辞書」の項目の一例" %}
#
# ## markup構文
#
#   {% news_figure [actual_size] "<画像のパス>" ["<キャプション>"] %}
#
#   - actual_size:  添えると、幅に合わせず、画像自身が持つ寸法で掲げる
#   - 画像のパス:   記事のディレクトリからの相対パス（必須）
#   - キャプション: 画像の下に置かれる説明。省略した場合はキャプションを出力しない
#
# ## HTML出力構造
#
#   <figure class="news__figure">
#   <img src="（画像のパス）" alt="（キャプション）">
#   <figcaption class="news__caption" markdown="span">（キャプション）</figcaption>
#   </figure>
#
#   actual_size を添えた場合、figure に news__figure--actual-size が加わる。
#
# =============================================================================

module News

  # {% news_figure ... %} インラインタグの実装。
  # 画像と、その下に置くキャプションを組み立てる。
  class NewsFigureTag < Liquid::Tag
    def initialize(tag_name, markup, options)
      super
      @source, @caption = TagArguments.parse(markup)
      @actual_size = TagArguments.flag?(markup, "actual_size")
    end

    def render(context)
      # キャプションは代替テキストを兼ねる。省略された場合、画像は装飾として扱う。
      caption_html = @caption ? %(<figcaption class="news__caption" markdown="span">#{@caption}</figcaption>\n) : ""
      class_names = @actual_size ? "news__figure news__figure--actual-size" : "news__figure"
      <<~HTML
        <figure class="#{class_names}">
        <img src="#{@source}" alt="#{@caption}">
        #{caption_html}</figure>
      HTML
    end
  end
end

# タグ "news_figure" を Liquid に登録する
Liquid::Template.register_tag("news_figure", News::NewsFigureTag)
