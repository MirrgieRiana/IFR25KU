# frozen_string_literal: true

# =============================================================================
# paper_dateline.rb — Paper Dateline Tag for Jekyll
# =============================================================================
#
# ニュース記事の、日付と分野の行を表示するためのLiquidカスタムインラインタグ。
#
# ## 基本的な使い方
#
#   {% paper_dateline "68年8月7日　天文学" %}
#
# ## markup構文
#
#   {% paper_dateline "<日付と分野>" %}
#
# ## HTML出力構造
#
#   <div class="paper__dateline" markdown="span">（日付と分野）</div>
#
# =============================================================================

module Paper

  # {% paper_dateline ... %} インラインタグの実装。
  # 記事の日付と分野を、本文に先立つ行として出力する。
  class PaperDatelineTag < Liquid::Tag
    def initialize(tag_name, markup, options)
      super
      @dateline, = Paper.parse_arguments(markup)
    end

    def render(context)
      %(<div class="paper__dateline" markdown="span">#{@dateline}</div>\n)
    end
  end
end

# タグ "paper_dateline" を Liquid に登録する
Liquid::Template.register_tag("paper_dateline", Paper::PaperDatelineTag)
