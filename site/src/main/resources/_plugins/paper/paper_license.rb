# frozen_string_literal: true

# =============================================================================
# paper_license.rb — Paper License Notice Tag for Jekyll
# =============================================================================
#
# 旧公式サイトから移植した紙面の、著作権・ライセンス情報へのリンクを表示するためのLiquidカスタムインラインタグ。
#
# 著作権とライセンスの詳細は README の Website Articles 節に記述されているため、
# このタグはその節へのリンクを表示する。
#
# ## 基本的な使い方
#
#   {% paper_license %}
#
# ## markup構文
#
#   {% paper_license %}
#
# ## HTML出力構造
#
#   <div class="paper__license">
#   <p>この記事の著作権とライセンスについては、<a href="...">READMEのWebsite Articles</a>に記載されています。</p>
#   </div>
#
# =============================================================================

module Paper

  # README の Website Articles 節へのURL。
  README_LICENSE_URL = "https://github.com/MirrgieRiana/IFR25KU/blob/main/README.md#website-articles"

  # {% paper_license %} インラインタグの実装。
  # 紙面の末尾に置く、著作権・ライセンス情報が記述された README 節へのリンクを出力する。
  class PaperLicenseTag < Liquid::Tag
    def render(context)
      <<~HTML
        <div class="paper__license">
        <p>この記事の著作権とライセンスについては、<a href="#{Paper::README_LICENSE_URL}">READMEのWebsite Articles</a>に記載されています。</p>
        </div>
      HTML
    end
  end
end

# タグ "paper_license" を Liquid に登録する
Liquid::Template.register_tag("paper_license", Paper::PaperLicenseTag)
