# frozen_string_literal: true

# =============================================================================
# news_column.rb — News Column Tag for Jekyll
# =============================================================================
#
# ニュース記事の段組みの、1個の段を表すLiquidカスタムブロックタグなのだ～🌱
#
# news_columns の直下に、段の個数だけ並べて使うのだ～🌱
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
#   <div class="news__column" markdown="1">
#     （ブロック内のテキスト、kramdownによりMarkdownとして処理される）
#   </div>
#
# =============================================================================

module News

  # {% news_column %}...{% endnews_column %} ブロックタグの実装。
  # ブロック内容を、段組みの中の1個の段として出力するのだ～🌱
  class NewsColumnTag < Liquid::Block
    # Liquidは内容が空白のみのブロックの描画自体を省くから、中身を持たない段が消えちゃうのだぁ…🌧️
    def blank?
      false
    end

    def render(context)
      content = super.strip
      <<~HTML
        <div class="news__column" markdown="1">
        #{content}

        </div>
      HTML
    end
  end
end

# タグ "news_column" を Liquid に登録するのだ～🌱
Liquid::Template.register_tag("news_column", News::NewsColumnTag)
