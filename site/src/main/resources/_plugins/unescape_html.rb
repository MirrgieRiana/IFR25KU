# frozen_string_literal: true

require "cgi"

# =============================================================================
# unescape_html.rb — HTML Entity Decoding Filter for Jekyll
# =============================================================================
#
# 実体参照を、それが表す文字そのものへ戻す、Liquid のカスタムフィルターなのだ～🌱
#
# markdownify は Markdown を HTML へ変換するから、その出力には実体参照が現れるのだ～🌱
# strip_html はタグだけを落とすから、実体参照はそのまま残るのだ～🌱
# image タグは受け取った値をエスケープしてから属性へ入れるから、実体参照の残った文字列を渡すと二重に掛かるのだぁ…🌧️
# だから、markdownify を経た文字列を image タグへ渡す前に、このフィルターで素のテキストへ戻すのだ～🌱
#
# ## 基本的な使い方なのだ～🌱
#
#   {{ page.title | markdownify | strip_html | unescape_html }}
#
# =============================================================================

module UnescapeHtml

  # 実体参照を、それが表す文字そのものへ戻すのだ～🌱
  def unescape_html(input)
    CGI.unescapeHTML(input.to_s)
  end
end

# フィルター "unescape_html" を Liquid に登録するのだ～🌱
Liquid::Template.register_filter(UnescapeHtml)
