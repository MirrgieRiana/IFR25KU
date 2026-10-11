# frozen_string_literal: true

require "cgi"

# =============================================================================
# image.rb — Image Tag for Jekyll
# =============================================================================
#
# 画像を img 要素として掲げる、Liquid のカスタムインラインタグなのだ～🌱
#
# site の中で画像を掲げる手段は、このタグに統一されているのだ～🌱
# Markdown のリンク構文や生の img 要素を使うと、img 要素の組み立て方が複数の場所に散って、
# 属性の追加や配置先の規則の変更が、その全部へ波及しちゃうのだ～🌧️
#
# 記事の原本では、画像は記事の .md と同じディレクトリに並んでいるのだ～🌱
# でも、生成されたサイトでは、記事と画像が別々の場所へ配られるのだ～🌱
# だから、記事の中へ書いた相対パスを、front matter の image_dir を基準にして、画像の配置先へ組み直すのだ～🌱
# image_dir は、syncJekyllSource タスクが記事ごとに書き足す値なのだ～🌱
#
# 画像のパスを引数へ直接書くほか、Liquid の変数を渡すこともできるのだ～🌱
# 変数を渡す形は、front matter から画像を受け取るレイアウトやインクルードで使うのだ～🌱
#
# ## 基本的な使い方なのだ～🌱
#
#   {% image "./miragium-axe.webp" %}
#   {% image "./miragium-axe.webp" alt="ミラジウムの斧" %}
#   {% image "./miragium-axe.webp" class="encyclopedia-card__picture" %}
#   {% image page.header.teaser alt="{{ page.title }}" %}
#
# ## markup 構文なのだ～🌱
#
#   {% image "<画像のパス>"|<変数名> [alt="<代替テキスト>"] [class="<クラス名>"] [aria_hidden] %}
#
#   - 画像のパス:   引用符で囲んだパスか、引用符で囲まない Liquid の変数名で、これは省略できないのだ～🌱
#                   . から始まるものは記事のディレクトリからの相対パスで、/ から始まるものはサイトの根からのパスなのだ～🌱
#   - 代替テキスト: img の alt に入る文字列で、省略すると空文字列になるのだ～🌱
#   - クラス名:     img に付く class 属性で、省略すると class 属性そのものを出力しないのだ～🌱
#   - aria_hidden:  添えると aria-hidden="true" を出力するのだ～🌱
#
#   代替テキストとクラス名の中では、{{ ... }} の形で Liquid の変数を参照できるのだ～🌱
#
# ## HTML の出力構造なのだ～🌱
#
#   <img src="（解決された画像のパス）" alt="（代替テキスト）">
#
# =============================================================================

module Images

  # 画像のパスを、生成されたサイトから引ける形へ直すのだ～🌱
  #
  # 記事のディレクトリからの相対パスは、サイトの根からのパスへ組み直すのだ～🌱
  # そのうえで、サイトの根から辿るパスには baseurl を前に付けるのだ～🌱
  # これは Jekyll の relative_url フィルターと同じ扱いで、サイトがドメインの直下でない場所へ置かれても引けるようにするのだ～🌱
  def self.resolve(context, source)
    resolve_with_baseurl(
      context.registers[:site]&.config&.fetch("baseurl", nil),
      expand_article_relative(context, source),
    )
  end

  # 記事のディレクトリからの相対パスを、サイトの根からのパスへ組み直すのだ～🌱
  #
  # 基準になる image_dir は、syncJekyllSource タスクが front matter へ書き足した値なのだ～🌱
  # Kotlin 側とこちらで同じ規則を二重に持たないように、計算の結果だけを受け取る形にしてあるのだ～🌱
  #
  # 相対パスでないものは、外部の URL か、既にサイトの根から書かれたパスだから、そのまま返すのだ～🌱
  def self.expand_article_relative(context, source)
    return source if source.nil? || !source.start_with?(".")

    image_dir = context.registers[:page]&.fetch("image_dir", nil)
    raise "image_dir is missing in the front matter" if image_dir.nil?

    # 記事のディレクトリの外を指す .. も、ここで畳むのだ～🌱
    segments = "#{image_dir}/#{source}".split("/")
    resolved = segments.each_with_object([]) do |segment, stack|
      case segment
      when ".", "" then next
      when ".." then stack.pop
      else stack.push(segment)
      end
    end
    "/#{resolved.join("/")}"
  end

  # resolve のうち baseurl を前に付ける部分だけを、Liquid の context を持たない呼び出し元のために切り出したものなのだ～🌱
  # 記事の文脈を持たない呼び出し元が渡すのは、サイトの根から書かれたパスだから、相対パスの組み直しは要らないのだ～🌱
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
      expand_article_relative(context, source),
      alt: alt,
      class_name: class_name,
      aria_hidden: aria_hidden,
    )
  end

  # render_img と同じことを、Liquid の context を持たない呼び出し元のために、baseurl を直接受け取る形で行うのだ～🌱
  #
  # 属性の値は、素のテキストとして受け取って、ここで HTML としてエスケープするのだ～🌱
  # 代替テキストに引用符が入ると属性が途中で閉じてしまうし、呼び出し元ごとにエスケープを書くと、書き忘れた所だけが壊れるのだぁ…🌧️
  def self.render_img_with_baseurl(baseurl, source, alt: "", class_name: nil, aria_hidden: false)
    attributes = +""
    attributes << %( class="#{CGI.escapeHTML(class_name.to_s)}") if class_name
    attributes << %( src="#{CGI.escapeHTML(resolve_with_baseurl(baseurl, source).to_s)}")
    attributes << %( alt="#{CGI.escapeHTML(alt.to_s)}")
    attributes << %( aria-hidden="true") if aria_hidden
    "<img#{attributes}>"
  end

  # {% image ... %} インラインタグの実装なのだ～🌱
  # 画像を img 要素として掲げるのだ～🌱
  class ImageTag < Liquid::Tag
    def initialize(tag_name, markup, options)
      super
      @source = TagArguments.parse(markup).first
      # 引用符で囲まれた引数が無いときは、残りを Liquid の変数名として扱うのだ～🌱
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

# タグ "image" を Liquid に登録するのだ～🌱
Liquid::Template.register_tag("image", Images::ImageTag)
