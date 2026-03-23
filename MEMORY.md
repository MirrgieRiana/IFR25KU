# MEMORY.md

AIアシスタントが自由に編集できる、コミットされる永続的なメモ用ファイル。
プロジェクトに関するノウハウや調査結果を蓄積する。

## ビルドフロー

1. `syncPages`（Gradleタスク、siteサブプロジェクト）: `site/pages/` → `site/build/pages/` にコピー。`lang_table.html` の `<%= trs %>` を展開し、`lang_table.json` / `lang_table.csv` を生成
2. `buildPages`（Gradleタスク、siteサブプロジェクト）: `site/scripts/build-pages.sh` を実行
3. `build-pages.sh`: `site/build/pages/` で `bundle install` → `bundle exec jekyll build --destination _site` → `CHANGELOG.md` を `_site/` にコピー
4. 出力先: `site/build/pages/_site/`

`servePages` / `site/scripts/serve-pages.sh` でローカルプレビュー可能（`bundle exec jekyll serve`）。

## テーマオーバーライド

minimal-mistakesテーマのファイルは `site/build/pages/vendor/bundle/ruby/3.3.0/gems/minimal-mistakes-jekyll-4.28.0/` にある。

オーバーライドするには、同じ相対パスで `site/pages/` 内にファイルを配置する。

### _layouts/

- `single.html` — 各コンテンツページ。ヘッダー画像3パターン対応、Grid化
- `splash.html` — トップページ・記事一覧。ヘッダーなし時に `content-wrap` で囲む

### _includes/

- `masthead.html` — ドロップダウンメニュー（`children` キー対応、外部リンクアイコン付き）
- `page__hero.html` — ヒーロー画像（背景画像・overlay_filter・actions対応）
- `recent-posts.html` — ブログ新着カード表示（`limit` / `more` パラメータ）
- `page-cards.html` — 指定ページをカード表示（`include.pages` でCSV指定）
- `post_pagination.html` — 記事の前後ナビゲーション
- `section-header.html` — フルワイドのセクションヘッダー（`title` / `subtitle` パラメータ）
- `scripts.html` — Gumshoeスクロールスパイの無効化処理
- `footer.html` — テーマデフォルト（social-icons、RSS、Sitemap）
- `footer/custom.html` — GitHubソースリンク、生ファイルリンク、ページURL表示
- `head/custom.html` — Favicon設定（webp形式）

### _sass/

- `minimal-mistakes/skins/_catppuccin-mocha.scss` — カスタムスキン（未使用）

**注意**: `_sass/` のパーシャルはこの方法ではオーバーライドできない。Sassの `@import` はインポート元ファイルのディレクトリを最初に検索するため、テーマのパーシャルが常に優先される。CSSのカスタマイズは `site/pages/assets/css/main.scss` の `@import "minimal-mistakes"` の後に記述する。

## ペインレイアウト

テーマのfloatベースのレイアウトをFlexbox/Gridに全面移行済み。`main.scss` でテーマのfloatを打ち消している。

- `#main`: `display: flex`（左サイドバー + メインコンテンツの横並び）。テーマのclearfix `::after` は `display: none` で除去。
- `.sidebar`: `float: none; flex-shrink: 0;`
- `.page`, `.splash`: `float: none; flex: 1; min-width: 0;`
- `.page__inner-wrap`: `display: grid`（コンテンツ + TOCの2カラム）。TOCがあれば暗黙的な2列目が生成される。
- `.sidebar__right`: `grid-column: 2; grid-row: 1 / span 999; align-self: start;` で右カラムに配置。`position: sticky` でスクロール追従。
- `.page__related`, `.archive`, `.breadcrumbs ol`, `.page__comments`: floatを解除済み。

テーマの `.greedy-nav a { display: block; margin: 0 1rem; }` はナビリンク用だがバナーにも適用される。`main.scss` で `.greedy-nav a` を `display: revert; margin: revert;` でリセットし、`.greedy-nav .visible-links a, .greedy-nav .hidden-links a` にのみ再適用。バナーは `a.site-title` で特定性を揃えて `display: flex` を適用。

## ページレイアウト

### singleレイアウト（各コンテンツページ）

ヘッダー画像の設定方式で3パターンのDOM構造が生じる。

**パターン1: ヘッダー画像なし**

```
page__inner-wrap > header.page__header--plain > h1.page__title
                                                > page__meta
                 > section.page__content
```

**パターン2: `header.image`（非overlay）**

ヒーロー画像がimgとして表示され、タイトルはその下。

```
div.page__hero > img.page__hero-image
...
page__inner-wrap > header > h1.page__title
                           > page__meta
                 > section.page__content
```

**パターン3: `header.overlay_image`**

ヒーロー画像がbackground-imageとして表示され、タイトルが画像の上に重なる。
`page__inner-wrap` 内の `<header>` はレンダリングされない（h1はヒーロー内のみ）。

```
div.page__hero--overlay > div.wrapper > h1.page__title
                                       > page__meta
...
page__inner-wrap > section.page__content（headerなし）
```

条件分岐: `{% unless page.header.overlay_color or page.header.overlay_image %}` で `<header>` の出力を制御。

### splashレイアウト（トップページ・記事一覧）

`index.md` で使用。`page__hero--overlay` でヒーロー画像を表示し、feature_rowやrecent-postsを配置。
ヘッダー画像がない場合は通常のh1を `<div class="content-wrap">` で囲んで表示（`site/pages/_layouts/splash.html` でオーバーライド済み）。

## CSS構造

`site/pages/assets/css/main.scss` にテーマの変数定義とカスタムスタイルを記述（約620行）。

### カラーパレット

```scss
$sans-serif: sans-serif;

$background-color: #faf5ff;           // 淡紫
$text-color: #2d1f3d;                 // 濃紫
$muted-text-color: #8a72a8;           // くすみ紫
$primary-color: #c840e0;              // マゼンタ
$border-color: #dcc0f0;               // 薄紫ボーダー
$code-background-color: #f0e4fa;      // ライトバイオレット
$code-background-color-dark: #dcc0f0; // ダークバイオレット
$form-background-color: #f0e4fa;      // フォーム背景
$footer-background-color: #e4d0f4;    // パステルバイオレット
$link-color: #FF2DAB;                 // ホットピンク
$link-color-hover: #8a20a0;           // ダークパープル
$link-color-visited: #CB62A1;         // ピンクパープル
$masthead-link-color: #2d1f3d;        // mastheadリンク
$masthead-link-color-hover: #a83dba;  // mastheadリンクホバー
$navicon-link-color-hover: #a83dba;   // ナビアイコンホバー
```

### 主要なカスタムスタイル

- `.masthead { border-bottom: 4px solid #FF2DAB; background: #000; }` — mastheadの装飾
- `.greedy-nav { background: transparent; }` — mastheadの背景を透過
- `body::before` — `background.webp` を `blur(24px)` で全面背景に
- `.initial-content { background: rgba(255, 255, 255, 0.80); }` — 半透明白の本文背景
- `.page__hero--overlay` — min-height: 600px、flex-end配置
- `.page__header--plain { margin-bottom: 1em; }` — ヘッダー画像なし時のh1下マージン
- `.recent-posts__grid` — `repeat(auto-fill, minmax(250px, 1fr))` のカードグリッド
- `.section-header` — フルワイド黒背景、上下 `4px #FF2DAB` ボーダー
- `html { font-size: 14px; }` — テーマのデフォルトから縮小

### テーマのデフォルトで注意すべき点

- `.masthead` にはデフォルトで `border-bottom: 1px solid $border-color` がある
- `.page__hero--overlay` にはデフォルトで `margin-bottom: 2em` がある
- `.greedy-nav a` の `display: block; margin: 0 1rem;` がバナーにも適用される（テーマのセレクタが広すぎる）

## ナビゲーション

`site/pages/_data/navigation.yml` で定義。

### mastheadメニュー

テーマのデフォルトmastheadはドロップダウン非対応（フラットなliリスト）。
`site/pages/_includes/masthead.html` でオーバーライドし、`children` キーに対応。

```yaml
main:
  - title: DOWNLOADS
    children:
      - title: Modrinth
        url: https://modrinth.com/mod/ifr25ku
      - title: CurseForge
        url: https://www.curseforge.com/minecraft/mc-mods/ifr25ku
  - title: ARTICLES
    children:
      - title: 記事一覧
        url: /posts.html
  - title: INFO
    children:
      - title: CHANGELOG
        url: /CHANGELOG.html
      - title: GitHub
        url: https://github.com/MirrgieRiana/IFR25KU
      - title: Lang Table
        url: /lang-table-index.html
      - title: IFRKU Official Web Site (Old)
        url: https://kakera-unofficial.notion.site/ifrku
```

ドロップダウンCSS: `.masthead__menu-item--dropdown` でホバー時に `.masthead__dropdown` を表示。

### サイドバー

```yaml
sidebar:
  - title: ページ一覧
    children:
      - title: Home
        url: /
      - title: CHANGELOG
        url: /CHANGELOG.html
      - title: Lang Table
        url: /lang_table.html
      - title: Lang Table (JSON)
        url: /lang_table.json
      - title: Lang Table (CSV)
        url: /lang_table.csv
```

テーマの `nav_list` includeがchildrenに対応済み。

## Jekyllの挙動

### front matterとファイル変換

- front matter（`---`で囲まれたブロック）を持つファイルはJekyllに「処理対象」として扱われる
- `.md` ファイルはfront matterがあるとHTMLに変換される（元の.mdは_siteに残らない）
- front matterがないファイルは静的ファイルとしてそのままコピーされる

### _config.yml

```yaml
locale: "ja-JP"
title: IFR25KU
url: https://mirrgieriana.github.io
baseurl: /IFR25KU
theme: minimal-mistakes-jekyll
minimal_mistakes_skin: default

plugins:
  - jekyll-include-cache

defaults:
  - scope:
      path: ""
    values:
      layout: single
      toc: true
      toc_sticky: true
      sidebar:
        nav: sidebar
```

全ページにsingleレイアウト・TOC・サイドバーがデフォルト適用される。

## テーマのJS

`site/pages/_includes/scripts.html` でオーバーライド済み。テーマの `main.min.js` 読み込み後に、`gumshoeActivate` イベントのキャプチャフェーズで `stopImmediatePropagation()` して無効化。これにより、GumshoeのスクロールスパイがアクティブなTOC項目へ自動スクロールする挙動（`scrollTocToContent`）を抑制。

## 画像

### convert-image.sh

`site/scripts/convert-image.sh <入力ファイル> [slug]` で `build/<slug>.webp` に変換。
slug省略時は入力ファイルの拡張子を除いた名前を使用。
ImageMagickの `convert` を使用（-quality 80）。

### 画像変換ツール比較

| ツール | パッケージ | 用途 | 標準インストール |
|---|---|---|---|
| convert | imagemagick | 画像全般（最もメジャー） | No |
| ffmpeg | ffmpeg | 動画・音声・画像 | No |
| cwebp | webp | webp変換専用（Google公式） | No |

いずれもUbuntuで標準インストールされない。事前に変換してコミットすればビルド環境に依存しない。

### 画像配置

```
assets/images/
├── background.webp                    — 全面背景（blur処理）
├── ifr25ku_banner-black-gothic.webp   — ロゴバナー
├── miragefairy_face_256.webp          — Favicon
├── index/
│   └── banner1.webp                   — トップページヒーロー
├── changelog/
│   └── changelog-header.svg           — CHANGELOGページヘッダー
├── lang-table-index/
│   └── lang-table-header.svg          — Lang Tableページヘッダー
└── posts/
    └── YYYY-MM-DD-slug/               — 各記事の画像
```

## ブログ記事

`site/pages/_posts/YYYY-MM-DD-slug.md` に配置。

front matter例:

```yaml
---
title: 記事タイトル
layout: single
header:
  teaser: /assets/images/posts/YYYY-MM-DD-slug/image.webp
tags: [お知らせ]
---
```

- `header.image`: ヒーロー画像（非overlay、タイトルは画像の下）
- `header.teaser`: 一覧カードのサムネイル
- `header.overlay_image`: overlay型（タイトルが画像に重なる）

トップページの `{% include recent-posts.html %}` で新着5件をカード表示。

## CHANGELOG

- `site/pages/CHANGELOG.md`: front matterあり → JekyllがHTMLに変換 → `CHANGELOG.html` として出力
- `build-pages.sh` でJekyllビルド後に `CHANGELOG.md` を `_site/` にコピーしてmd版も配信
- CHANGELOG.html冒頭に「Markdown版はこちら」リンクあり

## Lang Table

- `site/pages/lang_table.html`: スタンドアロンHTML（テーマレイアウトなし）。`<%= trs %>` はGradleの `syncPages` タスクで展開。JavaScript検索機能付き
- `site/pages/lang-table-index.md`: テーマレイアウトを使った特設ページ。各形式（HTML/JSON/CSV）へのリンクを配置
