# frozen_string_literal: true

# =============================================================================
# news_link.rb — News Page Link Tag for Jekyll
# =============================================================================
#
# 他の記事へ誘導するページリンクを表示するためのLiquidカスタムインラインタグ。
#
# ## 基本的な使い方
#
#   {% news_link "g2-mfa-observation-satellite.html" "G2-MFA　外命研 文献探しに並行世界に 観測衛星打ち上げ" %}
#
# ## markup構文
#
#   {% news_link "<リンク先>" "<表示するテキスト>" %}
#
#   - リンク先:           遷移先のURL（必須）
#   - 表示するテキスト:   リンクとして表示される文字列（必須）
#
# ## HTML出力構造
#
#   <div class="news__link">
#   <a href="（リンク先）" markdown="span">（表示するテキスト）</a>
#   </div>
#
# =============================================================================

module News

  # {% news_link ... %} インラインタグの実装。
  # 他の記事へのリンクを、独立した1行として出力する。
  class NewsLinkTag < Liquid::Tag
    def initialize(tag_name, markup, options)
      super
      @href, @text = TagArguments.parse(markup)
    end

    def render(context)
      <<~HTML
        <div class="news__link">
        <a href="#{@href}" markdown="span">#{@text}</a>
        </div>
      HTML
    end
  end
end

# タグ "news_link" を Liquid に登録する
Liquid::Template.register_tag("news_link", News::NewsLinkTag)
