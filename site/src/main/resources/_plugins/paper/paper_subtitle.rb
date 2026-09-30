# frozen_string_literal: true

# =============================================================================
# paper_subtitle.rb — Paper Subtitle Tag for Jekyll
# =============================================================================
#
# 論文のサブタイトルを表示するためのLiquidカスタムブロックタグ。
#
# 目次の生成対象にしないために、見出し要素ではなくdivとして出力する。
#
# ## 基本的な使い方
#
#   {% paper_subtitle %}
#   ～なぜミラージュは樹上にスポーンするのか？～
#   {% endpaper_subtitle %}
#
# ## HTML出力構造
#
#   <div class="paper__subtitle" markdown="span">
#     （ブロック内のテキスト、kramdownによりインラインのMarkdownとして処理される）
#   </div>
#
# =============================================================================

module Paper

  # {% paper_subtitle %}...{% endpaper_subtitle %} ブロックタグの実装。
  # ブロック内容を、タイトルの下に置く中央揃えのサブタイトルとして出力する。
  class PaperSubtitleTag < Liquid::Block
    def render(context)
      content = super.strip
      <<~HTML
        <div class="paper__subtitle" markdown="span">#{content}</div>
      HTML
    end
  end
end

# タグ "paper_subtitle" を Liquid に登録する
Liquid::Template.register_tag("paper_subtitle", Paper::PaperSubtitleTag)
