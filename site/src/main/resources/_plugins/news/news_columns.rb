# frozen_string_literal: true

# =============================================================================
# news_columns.rb — News Multi-Column Tag for Jekyll
# =============================================================================
#
# ニュース記事の段組みを表示するためのLiquidカスタムブロックタグ。
#
# 個々の段は news_column タグで表し、このタグはそれらを横に並べる役割を持つ。
# 段の個数は、中に置かれた news_column の個数によって決まる。
#
# ## 基本的な使い方
#
#   {% news_columns %}
#   {% news_column %}
#   左の段の本文（Markdown記法使用可能）
#   {% endnews_column %}
#   {% news_column %}
#   右の段の本文（Markdown記法使用可能）
#   {% endnews_column %}
#   {% endnews_columns %}
#
# ## HTML出力構造
#
#   <div class="news__columns" markdown="1">
#     （ブロック内の news_column、kramdownによりMarkdownとして処理される）
#   </div>
#
# =============================================================================

module News

  # {% news_columns %}...{% endnews_columns %} ブロックタグの実装。
  # 中に置かれた段を、横に並べる。
  class NewsColumnsTag < Liquid::Block
    def render(context)
      content = super.strip
      <<~HTML
        <div class="news__columns" markdown="1">
        #{content}

        </div>
      HTML
    end
  end
end

# タグ "news_columns" を Liquid に登録する
Liquid::Template.register_tag("news_columns", News::NewsColumnsTag)
