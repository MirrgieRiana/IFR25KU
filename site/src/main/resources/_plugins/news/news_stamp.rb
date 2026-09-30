# frozen_string_literal: true

# =============================================================================
# news_stamp.rb — News Production Stamp Tag for Jekyll
# =============================================================================
#
# 記事の末尾に置かれる、制作日と制作者の行を表示するためのLiquidカスタムブロックタグ。
#
# ## 基本的な使い方
#
#   {% news_stamp %}
#   2019年7月15日　MirageFairy Server創作部
#   {% endnews_stamp %}
#
# ## HTML出力構造
#
#   <div class="news__stamp" markdown="span">（ブロック内のテキスト）</div>
#
# =============================================================================

module News

  # {% news_stamp %}...{% endnews_stamp %} ブロックタグの実装。
  # ブロック内容を、記事末尾の制作日スタンプとして出力する。
  class NewsStampTag < Liquid::Block
    def render(context)
      content = super.strip
      <<~HTML
        <div class="news__stamp" markdown="span">#{content}</div>
      HTML
    end
  end
end

# タグ "news_stamp" を Liquid に登録する
Liquid::Template.register_tag("news_stamp", News::NewsStampTag)
