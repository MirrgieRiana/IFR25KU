# frozen_string_literal: true

# =============================================================================
# news_license.rb — News License Notice Tag for Jekyll
# =============================================================================
#
# 旧公式サイトから移植した記事の、著作権・ライセンス情報へのリンクを表示するためのLiquidカスタムインラインタグ。
#
# 著作権とライセンスの詳細は README の Website Articles 節に記述されているため、
# このタグはその節へのリンクを表示する。
#
# ## 基本的な使い方
#
#   {% news_license %}
#
# ## markup構文
#
#   {% news_license %}
#
# ## HTML出力構造
#
#   <div class="news__license">
#   <p>この記事の著作権とライセンスについては、<a href="...">READMEのWebsite Articles</a>に記載されています。</p>
#   </div>
#
# =============================================================================

module News

  # README の Website Articles 節へのURL。
  README_LICENSE_URL = "https://github.com/MirrgieRiana/IFR25KU/blob/main/README.md#website-articles"

  # {% news_license %} インラインタグの実装。
  # 記事の末尾に置く、著作権・ライセンス情報が記述された README 節へのリンクを出力する。
  class NewsLicenseTag < Liquid::Tag
    def render(context)
      <<~HTML
        <div class="news__license">
        <p>この記事の著作権とライセンスについては、<a href="#{News::README_LICENSE_URL}">READMEのWebsite Articles</a>に記載されています。</p>
        </div>
      HTML
    end
  end
end

# タグ "news_license" を Liquid に登録する
Liquid::Template.register_tag("news_license", News::NewsLicenseTag)
