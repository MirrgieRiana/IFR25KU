# frozen_string_literal: true

# =============================================================================
# paper_mfa_footer.rb — Paper MFA Footer Tag for Jekyll
# =============================================================================
#
# 旧公式サイトから移植した紙面の末尾に置く、注意書きをまとめて表示するためのLiquidカスタムインラインタグ。
#
# 注意書きは、記事の内容が並行宇宙ルールに基づく旨と、著作権とライセンスの詳細がREADMEにある旨の2件。
#
# ## 基本的な使い方
#
#   {% paper_mfa_footer %}
#
# ## markup構文
#
#   {% paper_mfa_footer [no_parallel_universe_rule] %}
#
#   no_parallel_universe_rule: 並行宇宙ルールに基づく旨の注意書きを省略する
#
# ## HTML出力構造
#
#   <div class="paper__mfa-footer">
#   <p>この記事の内容は、<a href="...">並行宇宙ルール</a>に基づいています。</p>
#   <p>この記事の著作権とライセンスについては、<a href="...">README</a>に記載されています。</p>
#   </div>
#
# =============================================================================

module Paper

  # READMEへのURL。
  # 見出しへのリンクにすると、READMEの見出し構文を束縛する。
  README_URL = "https://github.com/MirrgieRiana/IFR25KU/blob/main/README.md"

  # 並行宇宙ルールの記事へのURL。
  PARALLEL_UNIVERSE_RULE_URL = "/g2-mfa-parallel-universe-rule.html"

  # {% paper_mfa_footer %} インラインタグの実装。
  # 紙面の末尾に置く、MFAの記事に共通する注意書きを出力する。
  class PaperMfaFooterTag < Liquid::Tag
    def initialize(tag_name, markup, tokens)
      super
      @no_parallel_universe_rule = TagArguments.flag?(markup, "no_parallel_universe_rule")
    end

    def render(context)
      notices = []
      unless @no_parallel_universe_rule
        notices << %(<p>この記事の内容は、<a href="#{Paper::PARALLEL_UNIVERSE_RULE_URL}">並行宇宙ルール</a>に基づいています。</p>)
      end
      notices << %(<p>この記事の著作権とライセンスについては、<a href="#{Paper::README_URL}">README</a>に記載されています。</p>)
      <<~HTML
        <div class="paper__mfa-footer">
        #{notices.join("\n")}
        </div>
      HTML
    end
  end
end

# タグ "paper_mfa_footer" を Liquid に登録する
Liquid::Template.register_tag("paper_mfa_footer", Paper::PaperMfaFooterTag)
