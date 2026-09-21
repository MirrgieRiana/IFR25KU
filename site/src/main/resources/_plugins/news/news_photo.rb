# frozen_string_literal: true

# =============================================================================
# news_photo.rb — News Photo Tag for Jekyll
# =============================================================================
#
# ニュース記事中の、キャプション付き写真を表示するためのLiquidカスタムインラインタグ。
#
# 写真は、拡大しても階調が保たれるため、記事や段の幅いっぱいに引き伸ばして掲げる。
# 画像自身の寸法で掲げるドット絵や図解には、news_figure タグを使う。
#
# ## 基本的な使い方
#
#   {% news_photo "mirage-above-canopy.png" "図１　樹冠上のミラージュ" %}
#   {% news_photo "deep-space-field.png" %}
#
# ## markup構文
#
#   {% news_photo "<画像のパス>" ["<キャプション>"] %}
#
#   - 画像のパス:   記事のディレクトリからの相対パス（必須）
#   - キャプション: 画像の下に置かれる説明。省略した場合はキャプションを出力しない
#
# ## HTML出力構造
#
#   <figure class="news__photo">
#   <img src="（画像のパス）" alt="（キャプション）">
#   <figcaption class="news__caption" markdown="span">（キャプション）</figcaption>
#   </figure>
#
# =============================================================================

module News

  # {% news_photo ... %} インラインタグの実装。
  # 写真と、その下に置くキャプションを組み立てる。
  class NewsPhotoTag < Liquid::Tag
    def initialize(tag_name, markup, options)
      super
      @source, @caption = TagArguments.parse(markup)
    end

    def render(context)
      # キャプションは代替テキストを兼ねる。省略された場合、写真は装飾として扱う。
      caption_html = @caption ? %(<figcaption class="news__caption" markdown="span">#{@caption}</figcaption>\n) : ""
      <<~HTML
        <figure class="news__photo">
        <img src="#{@source}" alt="#{@caption}">
        #{caption_html}</figure>
      HTML
    end
  end
end

# タグ "news_photo" を Liquid に登録する
Liquid::Template.register_tag("news_photo", News::NewsPhotoTag)
