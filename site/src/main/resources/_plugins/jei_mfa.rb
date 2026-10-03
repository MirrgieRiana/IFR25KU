# frozen_string_literal: true

# =============================================================================
# jei_mfa.rb — JEI-MFA Block Tag for Jekyll
# =============================================================================
#
# JEI-MFAの作品本体を表示するためのLiquidカスタムブロックタグ。
#
# ## 基本的な使い方
#
#   {% jei_mfa %}
#   作品の本文（Markdown記法使用可能）
#   {% endjei_mfa %}
#
# ## HTML出力構造
#
#   <div class="jei-mfa" markdown="1">
#     （ブロック内のテキスト、kramdownによりMarkdown処理される）
#   </div>
#
# =============================================================================

module JeiMfa

  # {% jei_mfa %}...{% endjei_mfa %} ブロックタグの実装。
  # ブロック内容を <div class="jei-mfa" markdown="1"> で包んで出力する。
  # markdown="1" を付けることで、kramdownがブロック内容をMarkdownとして処理する。
  class JeiMfaTag < Liquid::Block
    def render(context)
      content = super
      <<~HTML
        <div class="jei-mfa" markdown="1">
        #{content}
        </div>
      HTML
    end
  end
end

# タグ "jei_mfa" を Liquid に登録する
Liquid::Template.register_tag("jei_mfa", JeiMfa::JeiMfaTag)
