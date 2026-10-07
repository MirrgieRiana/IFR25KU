# frozen_string_literal: true

# =============================================================================
# image.rb — Image Tag for Jekyll
# =============================================================================
#
# 記事のディレクトリに並ぶ画像を、img 要素として掲げるLiquidカスタムインラインタグなのだ～🌱
#
# 記事の原本では、画像は記事の .md と同じディレクトリに並んでいるのだ～🌱
# でも、生成されたサイトでは、記事と画像が別々の場所へ配られるのだ～🌱
# だから、記事の中に書いた相対パスは、そのままでは画像に届かないのだぁ…🌧️
#
# このタグは、front matter の image_dir を基準にして、画像の配置先を組み立てるのだ～🌱
# image_dir は、syncJekyllSource タスクが記事ごとに書き足す値なのだ～🌱
#
# ## 基本的な使い方
#
#   {% image "./miragium-axe.webp" %}
#   {% image "./miragium-axe.webp" alt="ミラジウムの斧" %}
#   {% image "./miragium-axe.webp" class="encyclopedia-card__picture" %}
#
# ## markup構文
#
#   {% image "<画像のパス>" [alt="<代替テキスト>"] [class="<クラス名>"] %}
#
#   - 画像のパス:   記事のディレクトリからの相対パス（必須）
#     . から始めたものだけが配置先へ解決されるのだ～🌱
#   - 代替テキスト: img の alt に入る文字列なのだ～🌱
#     省略すると空文字列になるのだ～🌱
#   - クラス名:     img に付く class 属性なのだ～🌱
#     省略すると class 属性を出力しないのだ～🌱
#
# ## HTML出力構造
#
#   <img src="（解決された画像のパス）" alt="（代替テキスト）">
#
# =============================================================================

module Images

  # 記事のディレクトリからの相対パスを、生成されたサイトでの配置先へ直すのだ～🌱
  #
  # 基準になる image_dir は、syncJekyllSource タスクが front matter へ書き足した値なのだ～🌱
  # Kotlin 側とこちらで同じ規則を二重に持たないように、計算の結果だけを受け取る形にしてあるのだ～🌱
  #
  # 相対パスでないものは、外部のURLや、既に解決済みのパスだから、そのまま返すのだ～🌱
  #
  # 組み立てた配置先はサイトの根から始まるから、baseurl を前に継ぐのだ～🌱
  def self.resolve(context, source)
    return source unless source.start_with?(".")

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
    "#{context.registers[:site].baseurl}/#{resolved.join("/")}"
  end

  # {% image ... %} インラインタグの実装なのだ～🌱
  # 記事のディレクトリに並ぶ画像を、img 要素として掲げるのだ～🌱
  class ImageTag < Liquid::Tag
    def initialize(tag_name, markup, options)
      super
      @source = TagArguments.parse(markup).first
      @alt = TagArguments.named(markup, "alt") || ""
      @class_name = TagArguments.named(markup, "class")
    end

    def render(context)
      class_attribute = @class_name ? %( class="#{@class_name}") : ""
      %(<img#{class_attribute} src="#{Images.resolve(context, @source)}" alt="#{@alt}">)
    end
  end
end

# タグ "image" を Liquid に登録するのだ～🌱
Liquid::Template.register_tag("image", Images::ImageTag)
