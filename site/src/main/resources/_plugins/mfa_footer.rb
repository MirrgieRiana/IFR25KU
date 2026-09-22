# frozen_string_literal: true

# =============================================================================
# mfa_footer.rb — MFA Footer Tag for Jekyll
# =============================================================================
#
# 旧公式サイトから移植した記事の末尾に置く、注意書きをまとめて表示するためのLiquidカスタムインラインタグ。
#
# 注意書きは、記事の内容が並行宇宙ルールに基づく旨と、著作権とライセンスの詳細がREADMEにある旨の2件。
#
# 注意書きの内容はMFAの記事の全体に共通するため、紙面の体裁ごとに分けず、1個のタグで受け持つ。
#
# ## 基本的な使い方
#
#   {% mfa_footer %}
#
# ## markup構文
#
#   {% mfa_footer [no_parallel_universe_rule] %}
#
#   no_parallel_universe_rule: 並行宇宙ルールに基づく旨の注意書きを省略する
#
# ## HTML出力構造
#
#   <div class="mfa-footer">
#   <p>この記事の内容は、<a href="...">並行宇宙ルール</a>に基づいています。</p>
#   <p>この記事の著作権とライセンスについては、<a href="...">README</a>に記載されています。</p>
#   </div>
#
# =============================================================================

module MfaFooter

  # READMEへのURL。
  # 見出しへのリンクにすると、READMEの見出し構文を束縛する。
  README_URL = "https://github.com/MirrgieRiana/IFR25KU/blob/main/README.md"

  # 並行宇宙ルールの記事へのURL。
  PARALLEL_UNIVERSE_RULE_URL = "/g2-mfa-parallel-universe-rule.html"

  # {% mfa_footer %} インラインタグの実装。
  # 記事の末尾に置く、MFAの記事に共通する注意書きを出力する。
  class MfaFooterTag < Liquid::Tag
    def initialize(tag_name, markup, tokens)
      super
      @no_parallel_universe_rule = TagArguments.flag?(markup, "no_parallel_universe_rule")
    end

    def render(context)
      notices = []
      unless @no_parallel_universe_rule
        notices << %(<p>この記事の内容は、<a href="#{MfaFooter::PARALLEL_UNIVERSE_RULE_URL}">並行宇宙ルール</a>に基づいています。</p>)
      end
      notices << %(<p>この記事の著作権とライセンスについては、<a href="#{MfaFooter::README_URL}">README</a>に記載されています。</p>)
      <<~HTML
        <div class="mfa-footer">
        #{notices.join("\n")}
        </div>
      HTML
    end
  end
end

# タグ "mfa_footer" を Liquid に登録する
Liquid::Template.register_tag("mfa_footer", MfaFooter::MfaFooterTag)
