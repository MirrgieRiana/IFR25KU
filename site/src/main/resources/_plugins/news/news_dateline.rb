# frozen_string_literal: true

# =============================================================================
# news_dateline.rb — News Dateline Tag for Jekyll
# =============================================================================
#
# ニュース記事の、日付と分野の行を表示するためのLiquidカスタムインラインタグ。
#
# ## 基本的な使い方
#
#   {% news_dateline "68年8月7日" "天文学" %}
#
# ## markup構文
#
#   {% news_dateline "<日付>" "<分野>" %}
#
#   - 日付: 記事の日付（必須）
#   - 分野: 記事の分野（必須）
#
# ## HTML出力構造
#
#   <div class="news__dateline">
#   <span class="news__dateline-date" markdown="span">（日付）</span>
#   <span class="news__dateline-field" markdown="span">（分野）</span>
#   </div>
#
# =============================================================================

module News

  # {% news_dateline ... %} インラインタグの実装。
  # 記事の日付と分野を、本文に先立つ行として出力する。
  class NewsDatelineTag < Liquid::Tag
    def initialize(tag_name, markup, options)
      super
      @date, @field = TagArguments.parse(markup)
    end

    def render(context)
      <<~HTML
        <div class="news__dateline">
        <span class="news__dateline-date" markdown="span">#{@date}</span>
        <span class="news__dateline-field" markdown="span">#{@field}</span>
        </div>
      HTML
    end
  end
end

# タグ "news_dateline" を Liquid に登録する
Liquid::Template.register_tag("news_dateline", News::NewsDatelineTag)
