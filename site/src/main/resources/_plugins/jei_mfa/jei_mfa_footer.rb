# frozen_string_literal: true

# =============================================================================
# jei_mfa_footer.rb — JEI-MFA Footer Tag for Jekyll
# =============================================================================
#
# JEI-MFAの記事の末尾に置く、転載元と著作権の説明を表示するためのLiquidカスタムインラインタグ。
#
# 説明の内容はどのJEI-MFAの記事でも同一だから、記事ごとに書き写さず、1個のタグで受け持つのだ～🌱
#
# ## 基本的な使い方
#
#   {% jei_mfa_footer %}
#
# ## HTML出力構造
#
#   <div class="mfa-footer" data-pagefind-ignore>
#   <p>この MFA の本文は、<a href="...">MirageFairy2019</a> の <code>miragefairy2019.mfa.*</code> の翻訳エントリーを、細部の調整を除き、そのまま転載したものです。</p>
#   <p>この本文は、© 2019 MirageFairy Server の著作物で、<a href="...">CC BY-SA 3.0</a> で提供されています。</p>
#   </div>
#
# =============================================================================

module JeiMfa

  # 転載元のMirageFairy2019のリポジトリのURLなのだ～🌱
  SOURCE_REPOSITORY_URL = "https://github.com/MirageFairy/MirageFairy2019"

  # 本文の提供条件であるライセンスのURLなのだ～🌱
  LICENSE_URL = "https://creativecommons.org/licenses/by-sa/3.0/"

  # {% jei_mfa_footer %} インラインタグの実装。
  # 記事の末尾に置く、JEI-MFAの記事に共通する転載元と著作権の説明を出力する。
  class JeiMfaFooterTag < Liquid::Tag
    def render(context)
      <<~HTML
        <div class="mfa-footer" data-pagefind-ignore>
        <p>この MFA の本文は、<a href="#{JeiMfa::SOURCE_REPOSITORY_URL}">MirageFairy2019</a> の <code>miragefairy2019.mfa.*</code> の翻訳エントリーを、細部の調整を除き、そのまま転載したものです。</p>
        <p>この本文は、© 2019 MirageFairy Server の著作物で、<a href="#{JeiMfa::LICENSE_URL}">CC BY-SA 3.0</a> で提供されています。</p>
        </div>
      HTML
    end
  end
end

# タグ "jei_mfa_footer" を Liquid に登録する
Liquid::Template.register_tag("jei_mfa_footer", JeiMfa::JeiMfaFooterTag)
