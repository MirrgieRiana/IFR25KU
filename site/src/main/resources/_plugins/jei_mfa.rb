# frozen_string_literal: true

# =============================================================================
# jei_mfa.rb — JEI-MFA Block Tag for Jekyll
# =============================================================================
#
# JEI-MFAの作品本体を表示するためのLiquidカスタムブロックタグ。
#
# ## 基本的な使い方
#
#   {% jei_mfa 入力アイテムの名前 | 2個目の名前 | 3個目の名前 %}
#   作品の本文（Markdown記法使用可能）
#   {% endjei_mfa %}
#
# 引数は、MirageFairy2019のJEI MFAで、このページを開くためのレシピの入力アイテムの名前なのだ～🌱
# 縦棒で区切って、最初がレシピの主材料で、続きが副材料なのだ～🌱
# 引数を省略すると、名前の部分は出力されないのだ～🌱
#
# ## HTML出力構造
#
#   <div class="jei-mfa" markdown="1">
#   <div class="jei-mfa__ingredients">
#   <p class="jei-mfa__ingredient jei-mfa__ingredient--main">入力アイテムの名前</p>
#   <p class="jei-mfa__ingredient">2個目の名前</p>
#   <p class="jei-mfa__ingredient">3個目の名前</p>
#   </div>
#     （ブロック内のテキスト、kramdownによりMarkdown処理される）
#   </div>
#
# =============================================================================

module JeiMfa

  # {% jei_mfa %}...{% endjei_mfa %} ブロックタグの実装。
  # ブロック内容を <div class="jei-mfa" markdown="1"> で包んで出力する。
  # markdown="1" を付けることで、kramdownがブロック内容をMarkdownとして処理する。
  class JeiMfaTag < Liquid::Block
    def initialize(tag_name, markup, options)
      super
      # 縦棒で区切られた入力アイテムの名前を、前後の空白を落として受け取るのだ～🌱
      @ingredients = markup.split("|").map(&:strip).reject(&:empty?)
    end

    def render(context)
      content = super
      <<~HTML
        <div class="jei-mfa" markdown="1">
        #{render_ingredients}
        #{content}
        </div>
      HTML
    end

    private

    # 入力アイテムの名前を、縦に並べた段落として出力する。
    # markdown="1" の中では空行が段落の区切りになってしまうから、HTMLを直接組んで、間に空行を挟まない。
    def render_ingredients
      return "" if @ingredients.empty?

      lines = @ingredients.each_with_index.map do |name, index|
        # 最初の名前はレシピの主材料で、2個目との間を広く開けるために、別のクラスを足すのだ～🌱
        classes = index.zero? ? "jei-mfa__ingredient jei-mfa__ingredient--main" : "jei-mfa__ingredient"
        "<p class=\"#{classes}\">#{name}</p>"
      end
      "<div class=\"jei-mfa__ingredients\">\n#{lines.join("\n")}\n</div>"
    end
  end
end

# タグ "jei_mfa" を Liquid に登録する
Liquid::Template.register_tag("jei_mfa", JeiMfa::JeiMfaTag)
