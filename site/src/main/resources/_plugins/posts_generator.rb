# frozen_string_literal: true

require "json"

# =============================================================================
# posts_generator.rb — Posts JSON Generator
# =============================================================================
#
# 全記事のメタデータを JSON ファイルとしてビルド成果物に書き出す Jekyll フック。
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
#       "url":    "/IFR25KU/2026/04/09/aqua-vitae.html",
#       "teaser": "/IFR25KU/assets/images/.../teaser.webp",  // null の場合あり
#       "tags":   ["アップデート", "ミラージュフェアリー劇場"]
#     },
#     ...
#   ]
#
# =============================================================================

Jekyll::Hooks.register :site, :post_write do |site|
  baseurl = site.config["baseurl"].to_s

  posts = site.posts.docs.map do |post|
    teaser = post.data.dig("header", "teaser")
    {
      "title"  => post.data["title"],
      "url"    => baseurl + post.url,
      "teaser" => teaser ? baseurl + teaser : nil,
      "tags"   => post.data["tags"] || [],
    }
  end

  dest = File.join(site.dest, "posts.json")
  File.write(dest, JSON.generate(posts))
end
