# frozen_string_literal: true

# =============================================================================
# news_section.rb — News Section Heading Tag for Jekyll
# =============================================================================
#
# ニュース記事のセクション見出しを表示するためのLiquidカスタムブロックタグ。
#
# 目次の生成対象にしないために、見出し要素ではなくdivとして出力する。
# no_search を添えると、その見出しを検索の索引から外すのだ～🌱
#
# ## 基本的な使い方
#
#   {% news_section %}
#   1. 背景
#   {% endnews_section %}
#   {% news_section no_search %}関連記事{% endnews_section %}
#
# ## markup構文
#
#   {% news_section [no_search] %}...{% endnews_section %}
#
#   - no_search: 添えると、見出しを検索の索引から外すのだ～🌱
#
# ## HTML出力構造
#
#   <div class="news__section" markdown="span">
#     （ブロック内のテキスト、kramdownによりインラインのMarkdownとして処理される）
#   </div>
#
#   no_search を添えた場合、div に data-pagefind-ignore が加わるのだ～🌱
#
# =============================================================================

module News

  # {% news_section %}...{% endnews_section %} ブロックタグの実装。
  # ブロック内容を、本文中のセクション見出しとして出力する。
  class NewsSectionTag < Liquid::Block
    def initialize(tag_name, markup, options)
      super
      @no_search = TagArguments.flag?(markup, "no_search")
    end

    def render(context)
      content = super.strip
      attributes = @no_search ? " data-pagefind-ignore" : ""
      <<~HTML
        <div class="news__section"#{attributes} markdown="span">#{content}</div>
      HTML
    end
  end
end

# タグ "news_section" を Liquid に登録する
Liquid::Template.register_tag("news_section", News::NewsSectionTag)
