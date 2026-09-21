# frozen_string_literal: true

# =============================================================================
# paper_callout.rb — Paper Callout Tag for Jekyll
# =============================================================================
#
# 論文中の囲み記事を表示するためのLiquidカスタムブロックタグ。
#
# 本文の流れから独立した内容を、枠で囲って示す。
#
# ## 基本的な使い方
#
#   {% paper_callout %}
#   本文（Markdown記法使用可能）
#   {% endpaper_callout %}
#
# ## HTML出力構造
#
#   <div class="paper__callout" markdown="1">
#     （ブロック内のテキスト、kramdownによりMarkdownとして処理される）
#   </div>
#
# =============================================================================

module Paper

  # {% paper_callout %}...{% endpaper_callout %} ブロックタグの実装。
  # ブロック内容を、本文から独立した囲み記事として出力する。
  class PaperCalloutTag < Liquid::Block
    def render(context)
      content = super.strip
      <<~HTML
        <div class="paper__callout" markdown="1">
        #{content}

        </div>
      HTML
    end
  end
end

# タグ "paper_callout" を Liquid に登録する
Liquid::Template.register_tag("paper_callout", Paper::PaperCalloutTag)
