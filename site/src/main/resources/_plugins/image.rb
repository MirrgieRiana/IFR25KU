# frozen_string_literal: true

# =============================================================================
# image.rb — Image Tag for Jekyll
# =============================================================================
#
# 画像を img 要素として掲げるLiquidカスタムインラインタグ。
#
# site の中で画像を掲げる手段は、このタグに統一されている。
# Markdownのリンク構文や生のimg要素を使うと、img要素の組み立て方が複数の場所に散り、
# 属性の追加や配置先の規則の変更が、その全部へ波及する。
#
# 画像のパスを引数へ直接書くほか、Liquidの変数を渡すこともできる。
# 変数を渡す形は、front matter から画像を受け取るレイアウトやインクルードで使う。
#
# ## 基本的な使い方
#
#   {% image "miragium-axe.webp" %}
#   {% image "miragium-axe.webp" alt="ミラジウムの斧" %}
#   {% image "miragium-axe.webp" class="encyclopedia-card__picture" %}
#   {% image page.header.teaser alt="{{ page.title }}" %}
#
# ## markup構文
#
#   {% image "<画像のパス>"|<変数名> [alt="<代替テキスト>"] [class="<クラス名>"] [aria_hidden] %}
#
#   - 画像のパス:   引用符で囲んだパス、または引用符で囲まないLiquidの変数名（必須）
#   - 代替テキスト: img の alt に入る文字列。省略すると空文字列になる
#   - クラス名:     img に付く class 属性。省略すると class 属性を出力しない
#   - aria_hidden:  添えると aria-hidden="true" を出力する
#
#   代替テキストとクラス名の中では、{{ ... }} の形でLiquidの変数を参照できる。
#
# ## HTML出力構造
#
#   <img src="（解決された画像のパス）" alt="（代替テキスト）">
#
# =============================================================================

module Images

  # 画像のパスを、生成されたサイトから引ける形へ直すのだ～🌱
  #
  # サイトの根から辿るパスには baseurl を前に付けるのだ～🌱
  # これは Jekyll の relative_url フィルターと同じ扱いで、サイトがドメインの直下でない場所へ置かれても引けるようにするのだ～🌱
  # 記事のディレクトリに並ぶ画像は、記事と同じ場所へ配られるから、何も足さずにそのまま返すのだ～🌱
  # 記事の配置先の規則は syncJekyllSource タスクが持っていて、ここでは持たないのだ～🌱
  def self.resolve(context, source)
    resolve_with_baseurl(context.registers[:site]&.config&.fetch("baseurl", nil), source)
  end

  # resolve と同じことを、Liquid の context を持たない呼び出し元のために、baseurl を直接受け取る形で行うのだ～🌱
  def self.resolve_with_baseurl(baseurl, source)
    return source if source.nil? || source.start_with?("http://", "https://", "//", "data:")
    return source unless source.start_with?("/")

    baseurl.nil? || baseurl.empty? ? source : "#{baseurl.chomp("/")}#{source}"
  end

  # img 要素を組み立てるのだ～🌱
  # paper_figure や news_figure のように、画像を内側に抱える他のタグからも呼ばれるのだ～🌱
  def self.render_img(context, source, alt: "", class_name: nil, aria_hidden: false)
    render_img_with_baseurl(
      context.registers[:site]&.config&.fetch("baseurl", nil),
      source,
      alt: alt,
      class_name: class_name,
      aria_hidden: aria_hidden,
    )
  end

  # render_img と同じことを、Liquid の context を持たない呼び出し元のために、baseurl を直接受け取る形で行うのだ～🌱
  def self.render_img_with_baseurl(baseurl, source, alt: "", class_name: nil, aria_hidden: false)
    attributes = +""
    attributes << %( class="#{class_name}") if class_name
    attributes << %( src="#{resolve_with_baseurl(baseurl, source)}")
    attributes << %( alt="#{alt}")
    attributes << %( aria-hidden="true") if aria_hidden
    "<img#{attributes}>"
  end

  # {% image ... %} インラインタグの実装。
  # 画像を img 要素として掲げる。
  class ImageTag < Liquid::Tag
    def initialize(tag_name, markup, options)
      super
      @source = TagArguments.parse(markup).first
      # 引用符で囲まれた引数が無いときは、残りをLiquidの変数名として扱うのだ～🌱
      @source_variable = @source ? nil : TagArguments.rest(markup)
      @alt = TagArguments.named(markup, "alt") || ""
      @class_name = TagArguments.named(markup, "class")
      @aria_hidden = TagArguments.flag?(markup, "aria_hidden")
    end

    def render(context)
      source = @source_variable ? context[@source_variable] : @source
      Images.render_img(
        context,
        source,
        alt: TagArguments.interpolate(@alt, context),
        class_name: @class_name && TagArguments.interpolate(@class_name, context),
        aria_hidden: @aria_hidden,
      )
    end
  end
end

# タグ "image" を Liquid に登録する
Liquid::Template.register_tag("image", Images::ImageTag)
