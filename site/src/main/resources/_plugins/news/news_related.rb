# frozen_string_literal: true

# =============================================================================
# news_related.rb — News Related Articles Tag for Jekyll
# =============================================================================
#
# 名指しした記事を、関連記事のカードとして並べるためのLiquidカスタムインラインタグ。
#
# 記事の抽選によって関連記事を出す recommendations.html と違い、こちらは
# 紹介する記事を書き手が選ぶ。カードの見た目は両者で共通である。
#
# ## 基本的な使い方
#
#   {% news_related "g2-mfa-observation-satellite" %}
#   {% news_related "g2-mfa-mail-from-space" "g1-mfa-mirage-fairy-ecology" %}
#
# ## markup構文
#
#   {% news_related "<記事のスラッグ>" ... %}
#
#   - 記事のスラッグ: 記事のURLの、拡張子を除いた末尾の部分（1個以上）
#
# ## HTML出力構造
#
#   <div class="page__recommend-grid">
#   <a href="（記事のURL）" class="recent-posts__card">
#   <div class="recent-posts__teaser"><img src="（サムネイル）" alt=""></div>
#   <div class="recent-posts__body"><h3 class="recent-posts__title">（タイトル）</h3></div>
#   </a>
#   </div>
#
# =============================================================================

module News

  # {% news_related ... %} インラインタグの実装。
  # 名指しされた記事を site.posts から引いて、カードとして並べる。
  class NewsRelatedTag < Liquid::Tag
    def initialize(tag_name, markup, options)
      super
      @slugs = TagArguments.parse(markup)
    end

    def render(context)
      site = context.registers[:site]
      cards = @slugs.map do |slug|
        post = site.posts.docs.find { |doc| File.basename(doc.url, ".html") == slug }
        # 名指しした記事が見つからないまま静かに空の枠を出すと、綴りの誤りに気付けない
        raise ArgumentError, "news_related: 記事が見つからないのだ～🌧️ スラッグ: #{slug}" if post.nil?
        render_card(site, post)
      end
      <<~HTML
        <div class="page__recommend-grid">
        #{cards.join}</div>
      HTML
    end

    private

    # 記事1件分のカードを組み立てる。
    # サムネイルを持たない記事でも、カードの高さを揃えるために枠だけは置く。
    def render_card(site, post)
      teaser = post.data.dig("header", "teaser")
      image_html = teaser ? %(<img src="#{site.baseurl}#{teaser}" alt="">) : ""
      <<~HTML
        <a href="#{site.baseurl}#{post.url}" class="recent-posts__card">
        <div class="recent-posts__teaser">#{image_html}</div>
        <div class="recent-posts__body"><h3 class="recent-posts__title">#{post.data["title"]}</h3></div>
        </a>
      HTML
    end
  end
end

# タグ "news_related" を Liquid に登録する
Liquid::Template.register_tag("news_related", News::NewsRelatedTag)
