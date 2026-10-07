# frozen_string_literal: true

require "json"

# =============================================================================
# posts_generator.rb — Posts JSON Generator
# =============================================================================
#
# 全記事のメタデータを JSON ファイルとしてビルド成果物へ書き出す Jekyll フックなのだ～🌱
#
# ## 収録の対象
#
# 日付付きのディレクトリに置かれた記事は、全部収録するのだ～🌱
# 日付を持たない固定ページは、front matter に recommendations を持つものだけを収録するのだ～🌱
# 関連記事を表示するページも、関連記事として表示される側へ加えるためなのだ～🌱
#
# ## 出力先
#
#   <dest>/posts.json
#
# ## 出力形式
#
#   [
#     {
#       "title":  "記事タイトル",
#       "url":    "/2026/04/09/aqua-vitae.html",
#       "teaser": "/assets/images/.../teaser.webp",  // null の場合あり
#       "tags":   ["アップデート", "IFR劇場"]
#     },
#     ...
#   ]
#
# =============================================================================

Jekyll::Hooks.register :site, :post_write do |site|
  baseurl = site.config["baseurl"].to_s

  documents = site.posts.docs + site.pages.select { |page| page.data["recommendations"] }

  posts = documents.map do |document|
    teaser = document.data.dig("header", "teaser")
    {
      "title"  => document.data["title"],
      "url"    => baseurl + document.url,
      "teaser" => teaser ? baseurl + teaser : nil,
      "tags"   => document.data["tags"] || [],
    }
  end

  dest = File.join(site.dest, "posts.json")
  File.write(dest, JSON.generate(posts))
end
