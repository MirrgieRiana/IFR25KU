# frozen_string_literal: true

# =============================================================================
# paper_dateline.rb — Paper Dateline Tag for Jekyll
# =============================================================================
#
# ニュース記事の、日付と分野の行を表示するためのLiquidカスタムインラインタグ。
#
# ## 基本的な使い方
#
#   {% paper_dateline "68年8月7日" "天文学" %}
#
# ## markup構文
#
#   {% paper_dateline "<日付>" "<分野>" %}
#
#   - 日付: 記事の日付（必須）
#   - 分野: 記事の分野（必須）
#
# ## HTML出力構造
#
#   <div class="paper__dateline">
#   <span class="paper__dateline-date" markdown="span">（日付）</span>
#   <span class="paper__dateline-field" markdown="span">（分野）</span>
#   </div>
#
# =============================================================================

module Paper

  # {% paper_dateline ... %} インラインタグの実装。
  # 記事の日付と分野を、本文に先立つ行として出力する。
  class PaperDatelineTag < Liquid::Tag
    def initialize(tag_name, markup, options)
      super
      @date, @field = Paper.parse_arguments(markup)
    end

    def render(context)
      <<~HTML
        <div class="paper__dateline">
        <span class="paper__dateline-date" markdown="span">#{@date}</span>
        <span class="paper__dateline-field" markdown="span">#{@field}</span>
        </div>
      HTML
    end
  end
end

# タグ "paper_dateline" を Liquid に登録する
Liquid::Template.register_tag("paper_dateline", Paper::PaperDatelineTag)
