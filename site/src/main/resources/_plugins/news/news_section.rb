# frozen_string_literal: true

# =============================================================================
# news_section.rb — News Section Heading Tag for Jekyll
# =============================================================================
#
# ニュース記事のセクション見出しを表示するためのLiquidカスタムブロックタグ。
#
# 目次の生成対象にしないために、見出し要素ではなくdivとして出力する。
#
# ## 基本的な使い方
#
#   {% news_section %}
#   1. 背景
#   {% endnews_section %}
#
# ## HTML出力構造
#
#   <div class="news__section" markdown="span">
#     （ブロック内のテキスト、kramdownによりインラインのMarkdownとして処理される）
#   </div>
#
# =============================================================================

module News

  # {% news_section %}...{% endnews_section %} ブロックタグの実装。
  # ブロック内容を、本文中のセクション見出しとして出力する。
  class NewsSectionTag < Liquid::Block
    def render(context)
      content = super.strip
      <<~HTML
        <div class="news__section" markdown="span">#{content}</div>
      HTML
    end
  end
end

# タグ "news_section" を Liquid に登録する
Liquid::Template.register_tag("news_section", News::NewsSectionTag)
