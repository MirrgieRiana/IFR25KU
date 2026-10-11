# frozen_string_literal: true

# =============================================================================
# paper_figure.rb — Paper Figure Tag for Jekyll
# =============================================================================
#
# 論文中の、キャプション付きの画像を表示するためのLiquidカスタムブロックタグなのだ～🌱
#
# 画像そのものは、ブロックの中で {% image %} タグを呼んで掲げるのだ～🌱
# 画像の掲げ方は image.rb が 1 か所で持って、このタグはその周りの figure とキャプションだけを組むのだ～🌱
#
# 画像は、既定では紙面や段の幅いっぱいに掲げるのだ～🌱
# actual_size を添えると、代わりに画像自身が持つ寸法で掲げるのだ～🌱
#
# 拡大したときに画素を補間するか否かは、掲げ方ではなく画像の形式が決めるのだ～🌱
# pngは画素を保ったまま拡大されて、webpは補間されるのだ～🌱
#
# ## 基本的な使い方
#
#   {% paper_figure "図１　ミラジウムの斧" %}{% image "./miragium-axe.webp" alt="ミラジウムの斧" %}{% endpaper_figure %}
#   {% paper_figure %}{% image "./deep-space-field.webp" %}{% endpaper_figure %}
#   {% paper_figure actual_size "「辞書」の項目の一例" %}{% image "./dictionary-entry.png" alt="「辞書」の項目の一例" %}{% endpaper_figure %}
#
# ## markup構文
#
#   {% paper_figure [actual_size] ["<キャプション>"] %}<画像を掲げる中身>{% endpaper_figure %}
#
#   - actual_size:  添えると、幅に合わせず、画像自身が持つ寸法で掲げるのだ～🌱
#   - キャプション: 画像の下に置かれる説明なのだ～🌱
#                 省略するとキャプションを出力しないのだ～🌱
#   - 中身:         figure の中へそのまま置かれるのだ～🌱 {% image %} タグを呼ぶのだ～🌱
#
# ## HTML出力構造
#
#   <figure class="paper__figure">
#   （ブロックの中身）
#   <figcaption class="paper__caption" markdown="span">（キャプション）</figcaption>
#   </figure>
#
#   actual_size を添えると、figure に paper__figure--actual-size が加わるのだ～🌱
#
# =============================================================================

module Paper

  # {% paper_figure %}...{% endpaper_figure %} ブロックタグの実装。
  # ブロックの中身の画像と、その下に置くキャプションを構成するのだ～🌱
  class PaperFigureTag < Liquid::Block
    # Liquidは内容が空白のみのブロックの描画自体を省くから、中身を持たない図が消えちゃうのだぁ…🌧️
    def blank?
      false
    end

    def initialize(tag_name, markup, options)
      super
      @caption = TagArguments.parse(markup).first
      @actual_size = TagArguments.flag?(markup, "actual_size")
    end

    def render(context)
      caption_html = @caption ? %(<figcaption class="paper__caption" markdown="span">#{@caption}</figcaption>\n) : ""
      class_names = @actual_size ? "paper__figure paper__figure--actual-size" : "paper__figure"
      <<~HTML
        <figure class="#{class_names}">
        #{super.strip}
        #{caption_html}</figure>
      HTML
    end
  end
end

# タグ "paper_figure" を Liquid に登録するのだ～🌱
Liquid::Template.register_tag("paper_figure", Paper::PaperFigureTag)
