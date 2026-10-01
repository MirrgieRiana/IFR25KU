# frozen_string_literal: true

# =============================================================================
# post_count_title.rb — タイトルへ記事の個数を足すジェネレーターなのだ～🌱
# =============================================================================
#
# 記事の一覧を載せるページのタイトルの末尾へ、そのページが載せる記事の個数を足す Jekyll ジェネレーターなのだ～🌱
#
# ## 対象なのだ～🌱
#
#   front matter の post_count_in_title が true のページなのだ～🌱
#   tag が設定されていればそのタグの付いた記事を、無ければ全記事を数えるのだ～🌱
#
# ## 出力例なのだ～🌱
#
#   タグ: 木炭 → タグ: 木炭 (4)
#   記事一覧 → 記事一覧 (12)
#
# =============================================================================

module PostCountTitle

  # タイトルへ記事の個数を足すジェネレーターなのだ～🌱
  # タグごとの記事一覧ページを追加するジェネレーターよりも後に走る必要があるため、優先度を下げるのだ～🌱
  class PostCountTitleGenerator < Jekyll::Generator
    safe true
    priority :low

    def generate(site)
      site.pages.each do |page|
        next unless page.data["post_count_in_title"]

        tag = page.data["tag"]
        posts = tag ? site.posts.docs.select { |post| (post.data["tags"] || []).include?(tag) } : site.posts.docs
        page.data["title"] = "#{page.data["title"]} (#{posts.size})"
      end
    end
  end
end
