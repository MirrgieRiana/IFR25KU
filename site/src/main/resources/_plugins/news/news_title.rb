# frozen_string_literal: true

# =============================================================================
# news_title.rb — News Title Tag for Jekyll
# =============================================================================
#
# ニュース記事のタイトルを表示するためのLiquidカスタムブロックタグ。
#
# 目次の生成対象にしないために、見出し要素ではなくdivとして出力する。
#
# ## 基本的な使い方
#
#   {% news_title %}
#   アストラル線バースト
#   {% endnews_title %}
#
# ## HTML出力構造
#
#   <div class="news__title" markdown="span">
#     （ブロック内のテキスト、kramdownによりインラインのMarkdownとして処理される）
#   </div>
#
# =============================================================================

module News

  # {% news_title %}...{% endnews_title %} ブロックタグの実装。
  # ブロック内容を、中央揃えのタイトルとして出力する。
  class NewsTitleTag < Liquid::Block
    def render(context)
      content = super.strip
      <<~HTML
        <div class="news__title" markdown="span">#{content}</div>
      HTML
    end
  end
end

# タグ "news_title" を Liquid に登録する
Liquid::Template.register_tag("news_title", News::NewsTitleTag)
