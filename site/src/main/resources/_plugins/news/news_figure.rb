# frozen_string_literal: true

# =============================================================================
# news_figure.rb — News Figure Tag for Jekyll
# =============================================================================
#
# ニュース記事中の、キャプション付きの図を表示するためのLiquidカスタムインラインタグ。
#
# 図は、ドット絵や、色数の少ない図解を指す。
# 引き伸ばすと画素の粗が出るため、画像自身が持つ寸法で掲げる。
# 記事や段の幅いっぱいに引き伸ばす写真には、news_photo タグを使う。
#
# ## 基本的な使い方
#
#   {% news_figure "incident-map.png" "図１　事案の発生地点" %}
#   {% news_figure "dictionary-entry.png" %}
#
# ## markup構文
#
#   {% news_figure "<画像のパス>" ["<キャプション>"] %}
#
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
# =============================================================================

module News

  # {% news_figure ... %} インラインタグの実装。
  # 図と、その下に置くキャプションを組み立てる。
  class NewsFigureTag < Liquid::Tag
    def initialize(tag_name, markup, options)
      super
      @source, @caption = TagArguments.parse(markup)
    end

    def render(context)
      # キャプションは代替テキストを兼ねる。省略された場合、図は装飾として扱う。
      caption_html = @caption ? %(<figcaption class="news__caption" markdown="span">#{@caption}</figcaption>\n) : ""
      <<~HTML
        <figure class="news__figure">
        <img src="#{@source}" alt="#{@caption}">
        #{caption_html}</figure>
      HTML
    end
  end
end

# タグ "news_figure" を Liquid に登録する
Liquid::Template.register_tag("news_figure", News::NewsFigureTag)
