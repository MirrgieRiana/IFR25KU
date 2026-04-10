# frozen_string_literal: true

require "json"

# =============================================================================
# theater_posts_generator.rb — Theater Posts JSON Generator
# =============================================================================
#
# "ミラージュフェアリー劇場" タグを持つ全記事のメタデータを JSON ファイルとして
# ビルド成果物に書き出す Jekyll フック。
#
# ## 出力先
#
#   <dest>/theater-posts.json
#
# ## 出力形式
#
#   [
#     {
#       "title":  "記事タイトル",
#       "url":    "/IFR25KU/2026/04/09/aqua_vitae.html",
#       "teaser": "/IFR25KU/assets/images/.../teaser.webp",  // null の場合あり
#       "tags":   ["アップデート", "ミラージュフェアリー劇場"]
#     },
#     ...
#   ]
#
# =============================================================================

THEATER_TAG = "ミラージュフェアリー劇場"

Jekyll::Hooks.register :site, :post_write do |site|
  baseurl = site.config["baseurl"].to_s

  posts = (site.tags[THEATER_TAG] || []).map do |post|
    teaser = post.data.dig("header", "teaser")
    {
      "title"  => post.data["title"],
      "url"    => baseurl + post.url,
      "teaser" => teaser ? baseurl + teaser : nil,
      "tags"   => post.data["tags"] || [],
    }
  end

  dest = File.join(site.dest, "theater-posts.json")
  File.write(dest, JSON.generate(posts))
end
