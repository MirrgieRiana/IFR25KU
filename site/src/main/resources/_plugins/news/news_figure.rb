# frozen_string_literal: true

# =============================================================================
# news_figure.rb — News Figure Tag for Jekyll
# =============================================================================
#
# ニュース記事中の、キャプション付きの画像を表示するためのLiquidカスタムインラインタグなのだ～🌱
#
# 画像は、既定では記事や段の幅いっぱいに掲げるのだ～🌱
# actual_size を添えると、代わりに画像自身が持つ寸法で掲げるのだ～🌱
#
# 拡大したときに画素を補間するか否かは、掲げ方ではなく画像の形式が決めるのだ～🌱
# pngは画素を保ったまま拡大されて、webpは補間されるのだ～🌱
#
# ## 基本的な使い方
#
#   {% news_figure "deep-space-field.webp" "図１　深宇宙" %}
#   {% news_figure "parallel-universe-collection.webp" %}
#   {% news_figure actual_size "dictionary-entry.png" "「辞書」の項目の一例" %}
#   {% news_figure "night-sky.webp" "図３　夜空<br>第4恒星の近傍" alt="図３　夜空　第4恒星の近傍" %}
#
# ## markup構文
#
#   {% news_figure [actual_size] "<画像のパス>" ["<キャプション>"] [alt="<代替テキスト>"] %}
#
#   - actual_size:  添えると、幅に合わせず、画像自身が持つ寸法で掲げるのだ～🌱
#   - 画像のパス:   記事のディレクトリからの相対パス（必須）
#     配置先への解決は、Images.resolve が front matter の image_dir を基準に行うのだ～🌱
#   - キャプション: 画像の下に置かれる説明なのだ～🌱
#                 省略するとキャプションを出力しないのだ～🌱
#   - 代替テキスト: img の alt に入る文字列なのだ～🌱
#     省略すると、キャプションがそのまま入るのだ～🌱
#
# ## HTML出力構造
#
#   <figure class="news__figure">
#   <img src="（画像のパス）" alt="（代替テキスト、無ければキャプション）">
#   <figcaption class="news__caption" markdown="span">（キャプション）</figcaption>
#   </figure>
#
#   actual_size を添えると、figure に news__figure--actual-size が加わるのだ～🌱
#
# =============================================================================

module News

  # {% news_figure ... %} インラインタグの実装。
  # 画像と、その下に置くキャプションを構成するのだ～🌱
  class NewsFigureTag < Liquid::Tag
    def initialize(tag_name, markup, options)
      super
      @source, @caption = TagArguments.parse(markup)
      @actual_size = TagArguments.flag?(markup, "actual_size")
      @alt = TagArguments.named(markup, "alt") || @caption
    end

    def render(context)
      # 代替テキストもキャプションも無い画像は、装飾として扱うのだ～🌱
      caption_html = @caption ? %(<figcaption class="news__caption" markdown="span">#{@caption}</figcaption>\n) : ""
      class_names = @actual_size ? "news__figure news__figure--actual-size" : "news__figure"
      <<~HTML
        <figure class="#{class_names}">
        <img src="#{Images.resolve(context, @source)}" alt="#{@alt}">
        #{caption_html}</figure>
      HTML
    end
  end
end

# タグ "news_figure" を Liquid に登録するのだ～🌱
Liquid::Template.register_tag("news_figure", News::NewsFigureTag)
