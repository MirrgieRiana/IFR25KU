# frozen_string_literal: true

# =============================================================================
# mfa_footer.rb — MFA Footer Tag for Jekyll
# =============================================================================
#
# 旧公式サイトから移植した記事の末尾に置く、注意書きを表示するためのLiquidカスタムインラインタグなのだ～🌱
#
# 注意書きの内容は、並行宇宙ルールに基づく記事に共通するから、紙面の体裁ごとに分けず、1個のタグで受け持つのだ～🌱
#
# ## 基本的な使い方
#
#   {% mfa_footer %}
#
# ## HTML出力構造
#
#   <div class="mfa-footer" data-pagefind-ignore>
#   <p>この記事の内容は、<a href="...">並行宇宙ルール</a>に基づいています。</p>
#   </div>
#
# =============================================================================

module MfaFooter

  # 並行宇宙ルールの記事の、site.baseurl より後ろのURLなのだ～🌱
  PARALLEL_UNIVERSE_RULE_URL = "/g2-mfa-parallel-universe-rule.html"

  # {% mfa_footer %} インラインタグの実装。
  # 記事の末尾に置く、MFAの記事に共通する注意書きを出力するのだ～🌱
  class MfaFooterTag < Liquid::Tag
    def render(context)
      site = context.registers[:site]
      <<~HTML
        <div class="mfa-footer" data-pagefind-ignore>
        <p>この記事の内容は、<a href="#{site.baseurl}#{MfaFooter::PARALLEL_UNIVERSE_RULE_URL}">並行宇宙ルール</a>に基づいています。</p>
        </div>
      HTML
    end
  end
end

# タグ "mfa_footer" を Liquid に登録するのだ～🌱
Liquid::Template.register_tag("mfa_footer", MfaFooter::MfaFooterTag)
