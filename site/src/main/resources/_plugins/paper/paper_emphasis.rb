# frozen_string_literal: true

# =============================================================================
# paper_emphasis.rb — Paper Emphasis Tag for Jekyll
# =============================================================================
#
# 論文の本文中の強調を表示するためのLiquidカスタムインラインタグ。
#
# Markdownの強調の記法は、前後を空白で区切る必要がある処理系があるため、
# 空白で語を区切らない日本語の文の途中では使いにくい。
# このタグは、その制約を受けずに、文の任意の位置へ強調を置く。
#
# ## 基本的な使い方
#
#   2-1 {% paper_emphasis "手順" %}
#
# ## markup構文
#
#   {% paper_emphasis "<強調するテキスト>" %}
#
#   - 強調するテキスト: 強調して表示される文字列（必須）
#
# ## HTML出力構造
#
#   <strong markdown="span">（強調するテキスト）</strong>
#
# =============================================================================

module Paper

  # {% paper_emphasis ... %} インラインタグの実装。
  # 引数のテキストを、本文の流れの中の強調として出力する。
  class PaperEmphasisTag < Liquid::Tag
    def initialize(tag_name, markup, options)
      super
      @text, = Paper.parse_arguments(markup)
    end

    def render(context)
      %(<strong markdown="span">#{@text}</strong>)
    end
  end
end

# タグ "paper_emphasis" を Liquid に登録する
Liquid::Template.register_tag("paper_emphasis", Paper::PaperEmphasisTag)
