# frozen_string_literal: true

# =============================================================================
# tag_pages_generator.rb — Tag Archive Page Generator
# =============================================================================
#
# 全記事の tags を集めて、タグごとの記事一覧ページを生成する Jekyll ジェネレーター。
#
# ## 出力先
#
#   /tag/<タグ名>.html
#
# ## 生成されるページ
#
#   レイアウトは記事一覧ページと同じ splash で、本文は recent-posts.html の
#   タグ絞り込みの呼び出し1行のみ。
#
# =============================================================================

module TagPages

  # タグ1個分の記事一覧ページ。
  # 実体のファイルを持たず、ビルド時にメモリ上で組み立てる。
  class TagPage < Jekyll::PageWithoutAFile
    def initialize(site, tag)
      super(site, site.source, "tag", "#{tag}.html")
      data["title"] = "タグ: #{tag}"
      data["description"] = "「#{tag}」のタグが付いた記事の一覧"
      data["layout"] = "splash"
      data["sidebar"] = false
      data["toc"] = false
      data["tag"] = tag
      self.content = <<~CONTENT
        <div class="content-wrap" markdown="1">

        {% include recent-posts.html tag=page.tag %}

        </div>
      CONTENT
    end
  end

  # 全記事のタグ集合から、タグごとのページを追加するジェネレーター。
  class TagPagesGenerator < Jekyll::Generator
    safe true

    def generate(site)
      tags = site.posts.docs.flat_map { |post| post.data["tags"] || [] }.uniq.sort
      tags.each do |tag|
        site.pages << TagPage.new(site, tag)
      end
    end
  end
end
