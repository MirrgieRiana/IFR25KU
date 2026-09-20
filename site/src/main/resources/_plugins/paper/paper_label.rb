# frozen_string_literal: true

# =============================================================================
# paper_label.rb — Paper Label Tag for Jekyll
# =============================================================================
#
# 論文タイトルの上に置かれる、紙面のラベルを表示するためのLiquidカスタムインラインタグ。
#
# ## 基本的な使い方
#
#   {% paper_label "レター" "妖精研究誌" %}
#
# ## markup構文
#
#   {% paper_label "<紙面の種別>" "<掲載誌名>" %}
#
#   - 紙面の種別: 行の左端に配置される（必須）
#   - 掲載誌名:   行の右端に配置される（必須）
#
# ## HTML出力構造
#
#   <div class="paper__label">
#   <span class="paper__label-kind" markdown="span">（紙面の種別）</span>
#   <span class="paper__label-journal" markdown="span">（掲載誌名）</span>
#   </div>
#
# =============================================================================

module Paper

  # {% paper_label ... %} インラインタグの実装。
  # 2個の引数を、それぞれ行の左端と右端に配置する。
  class PaperLabelTag < Liquid::Tag
    def initialize(tag_name, markup, options)
      super
      @kind, @journal = Paper.parse_arguments(markup)
    end

    def render(context)
      <<~HTML
        <div class="paper__label">
        <span class="paper__label-kind" markdown="span">#{@kind}</span>
        <span class="paper__label-journal" markdown="span">#{@journal}</span>
        </div>
      HTML
    end
  end
end

# タグ "paper_label" を Liquid に登録する
Liquid::Template.register_tag("paper_label", Paper::PaperLabelTag)
