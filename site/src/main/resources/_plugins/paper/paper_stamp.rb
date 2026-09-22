# frozen_string_literal: true

# =============================================================================
# paper_stamp.rb — Paper Production Stamp Tag for Jekyll
# =============================================================================
#
# 紙面の末尾に置かれる、制作日と制作者の行を表示するためのLiquidカスタムブロックタグ。
#
# ## 基本的な使い方
#
#   {% paper_stamp %}
#   2019年7月15日　MirageFairy Server創作部
#   {% endpaper_stamp %}
#
# ## HTML出力構造
#
#   <div class="paper__stamp" markdown="span">（ブロック内のテキスト）</div>
#
# =============================================================================

module Paper

  # {% paper_stamp %}...{% endpaper_stamp %} ブロックタグの実装。
  # ブロック内容を、紙面末尾の制作日スタンプとして出力する。
  class PaperStampTag < Liquid::Block
    def render(context)
      content = super.strip
      <<~HTML
        <div class="paper__stamp" markdown="span">#{content}</div>
      HTML
    end
  end
end

# タグ "paper_stamp" を Liquid に登録する
Liquid::Template.register_tag("paper_stamp", Paper::PaperStampTag)
