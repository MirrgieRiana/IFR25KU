# frozen_string_literal: true

# =============================================================================
# mfa_footer.rb — MFA Footer Tag for Jekyll
# =============================================================================
#
# 旧公式サイトから移植した記事の末尾に置く、注意書きを表示するためのLiquidカスタムインラインタグ。
#
# 注意書きの内容はMFAの記事の全体に共通するため、紙面の体裁ごとに分けず、1個のタグで受け持つ。
#
# ## 基本的な使い方
#
#   {% mfa_footer %}
#
# ## HTML出力構造
#
#   <div class="mfa-footer">
#   <p>この記事の内容は、<a href="...">並行宇宙ルール</a>に基づいています。</p>
#   </div>
#
# =============================================================================

module MfaFooter

  # 並行宇宙ルールの記事へのURL。
  PARALLEL_UNIVERSE_RULE_URL = "/g2-mfa-parallel-universe-rule.html"

  # {% mfa_footer %} インラインタグの実装。
  # 記事の末尾に置く、MFAの記事に共通する注意書きを出力する。
  class MfaFooterTag < Liquid::Tag
    def render(context)
      <<~HTML
        <div class="mfa-footer">
        <p>この記事の内容は、<a href="#{MfaFooter::PARALLEL_UNIVERSE_RULE_URL}">並行宇宙ルール</a>に基づいています。</p>
        </div>
      HTML
    end
  end
end

# タグ "mfa_footer" を Liquid に登録する
Liquid::Template.register_tag("mfa_footer", MfaFooter::MfaFooterTag)
