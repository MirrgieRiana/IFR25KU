# frozen_string_literal: true

# =============================================================================
# paper_license.rb — Paper License Notice Tag for Jekyll
# =============================================================================
#
# 旧公式サイトから移植した紙面の、ライセンス表記を表示するためのLiquidカスタムインラインタグ。
#
# 旧公式サイトは、著者名を原則としてすべて「夜のかけら」と定め、
# 二次利用のライセンスをCC BY 4.0を含む3個から選べるものと定めている。
# 残る2個には非推奨と添えられているため、このタグはCC BY 4.0を表示する。
#
# ## 基本的な使い方
#
#   {% paper_license "アストラル線バースト" %}
#
# ## markup構文
#
#   {% paper_license "<作品名>" %}
#
#   - 作品名: 紙面の題名（必須）
#
# ## HTML出力構造
#
#   <div class="paper__license">
#   <p>本項目の内容は『<a href="...">クリエイティブ・コモンズ 表示4.0国際ライセンス</a>』に従います。</p>
#   <p>『（作品名）』<br />by 夜のかけら</p>
#   </div>
#
# =============================================================================

module Paper

  # ライセンスの名称と、その全文へのURL。
  LICENSE_NAME = "クリエイティブ・コモンズ 表示4.0国際ライセンス"
  LICENSE_URL = "https://creativecommons.org/licenses/by/4.0/deed.ja"

  # 旧公式サイトが原則として定める著者名。
  LICENSE_AUTHOR = "夜のかけら"

  # {% paper_license ... %} インラインタグの実装。
  # 紙面の末尾に置く、二次利用のためのライセンス表記を出力する。
  class PaperLicenseTag < Liquid::Tag
    def initialize(tag_name, markup, options)
      super
      @title, = Paper.parse_arguments(markup)
    end

    def render(context)
      <<~HTML
        <div class="paper__license">
        <p>本項目の内容は『<a href="#{Paper::LICENSE_URL}" rel="license">#{Paper::LICENSE_NAME}</a>』に従います。</p>
        <p>『#{@title}』<br />by #{Paper::LICENSE_AUTHOR}</p>
        </div>
      HTML
    end
  end
end

# タグ "paper_license" を Liquid に登録する
Liquid::Template.register_tag("paper_license", Paper::PaperLicenseTag)
