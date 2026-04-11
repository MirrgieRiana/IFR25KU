# MEMORY.md

AIアシスタントが自由に編集できる、コミットされる永続的なメモ用ファイル。
プロジェクトに関するノウハウや調査結果を蓄積する。

## ビルドシステム

### タスクフロー

1. `makeLangTable`（group: generate）: 言語JSONとHTMLテンプレートからlang_table.html/json/csvを `site/build/langTable/` に生成。`inputs`/`outputs` 宣言によりUP-TO-DATE判定あり
2. `installJekyllBundle`（Exec、group: other）: `site/scripts/bundle-install.sh` を実行。`inputs`（`src/main/bundle/Gemfile`, `Gemfile.lock`）/`outputs`（`build/bundleVendor`, `build/bundleConfig`）宣言によりUP-TO-DATE判定あり
    - `bundle-install.sh`: `BUNDLE_APP_CONFIG=$SITE_DIR/build/bundleConfig` を設定し、`site/src/main/bundle/` で `bundle config set --local path $SITE_DIR/build/bundleVendor` → `bundle install`。gemは `site/build/bundleVendor/` にインストールされる
3. `syncJekyllSource`（Sync、group: other）: `site/src/main/resources/` と `site/src/main/bundle/` を `site/build/jekyllSource/` に同期
4. `jekyllBuild`（Exec、group: build）: `site/scripts/build-site.sh` を実行。`dependsOn(installJekyllBundle)`（UP-TO-DATE判定コスト削減のため敢えてinputsにしない）。`inputs.files(syncJekyllSource)` / `outputs.dir(jekyllBuild/)` 宣言あり
    - `build-site.sh`: `BUNDLE_APP_CONFIG=$SITE_DIR/build/bundleConfig` を設定し、`site/build/jekyllSource/` で `bundle exec jekyll build --destination ../jekyllBuild`
5. `generateOgImages`（group: generate）: `src/main/resources/` 内の `.md` ファイルのfront matterからタイトルとヘッダー画像を読み取り、Playwrightで1200×630のスクリーンショット（PNG）を取得後、TwelveMonkeys imageio-webpライブラリでWebPに変換して `src/main/resources/assets/images/` 直下に出力。既存ファイルはスキップ（`-Pregenerate` で強制再生成）
6. `buildSite`（Sync、group: build）: `jekyllBuild` の出力と `makeLangTable` の出力と `src/main/resources/` 内の `.md` ファイルを `site/build/site/` に統合。OG画像は `src/main/resources/assets/images/` に直接書き込まれるため、Jekyllパイプライン（syncJekyllSource → jekyllBuild）を経由して自動的に最終出力に含まれる

CI出力先は `site/build/site/`（`pages.yml` で `buildSite` タスクを実行し、そこからデプロイ）。

`serveSite`（Exec、group: application）: `site/scripts/serve-site.sh` → `serve-site.main.kts`（Ktor Nettyサーバー）で `site/build/site/` を `http://localhost:4000/IFR25KU/` に配信。`inputs.files(buildSite)` による依存。Jekyll標準の `jekyll serve` ではなくKtor Nettyを使用する理由は、jekyll serveが頻繁に落ちて使い物にならないため。MIMEタイプ: `.md` を `text/plain; charset=utf-8` として配信。

### buildscript依存関係

```kotlin
classpath("org.yaml:snakeyaml:2.2")                        // front matter解析
classpath("com.microsoft.playwright:playwright:1.58.0")     // OG画像生成（Chromiumスクリーンショット）
classpath("com.twelvemonkeys.imageio:imageio-webp:3.12.0")  // WebP変換（~290KB）
```

## Jekyll

### テーマオーバーライドの仕組み

minimal-mistakesテーマのファイルは `site/build/bundleVendor/bundle/ruby/3.3.0/gems/minimal-mistakes-jekyll-4.28.0/` にある。オーバーライドするには、同じ相対パスで `site/src/main/resources/` 内にファイルを配置する。

**注意**: `_sass/` のパーシャルはオーバーライドできない。Sassの `@import` はインポート元ファイルのディレクトリを最初に検索するため、テーマのパーシャルが常に優先される。CSSのカスタマイズは `site/src/main/resources/assets/css/main.scss` の `@import "minimal-mistakes"` の後に記述する。

### _layouts/

- `single.html` — 各コンテンツページ。ヘッダー画像3パターン対応、Grid化
- `splash.html` — トップページ・記事一覧。`carousel` 配列対応（hero-carousel.htmlを呼び出し）。ヘッダーなし時に `content-wrap` で囲む
- `theater.html` — ミラージュフェアリー劇場記事用。`single.html` から分離。右サイドバーを TOC ではなく関連記事（`posts.json` + タグ重み付きランダム抽選）に置き換え。末尾にインラインJSを持つ

### _includes/

**ヘッダー・ヒーロー系**

- `masthead.html` — ドロップダウンメニュー（`children` キー対応、外部リンクアイコン付き）。ロゴはフェイス画像とバナー画像の2枚配置
- `hero-carousel.html` — ヒーローカルーセル（Embla Carousel、ドットナビ・プログレスバー・パンアニメーション）。`page.carousel` 配列のスライドをレンダリング
- `page__hero.html` — ヒーロー画像（背景画像・overlay_filter・actions対応）。overlay_filterは `gradient` / `rgba` / 数値に対応し自動変換
- `page__hero_actions.html` — ヒーロー内のアクションボタン表示。`page.header.actions` 配列をループ。外部リンク自動判定

**コンテンツ表示系**

- `recent-posts.html` — ブログ新着カード表示（`limit` / `more` パラメータ）
- `page-cards.html` — 指定ページをカード表示（`include.pages` でCSV指定）
- `post_pagination.html` — 記事の前後ナビゲーション（Grid 4列レイアウト）
- `section-header.html` — フルワイドのセクションヘッダー（`title` / `subtitle` パラメータ）

**ページ全体系**

- `head/custom.html` — Favicon設定（webp形式）
- `seo.html` — OG画像パスの自動導出（`page.url` ベース）。詳細は「画像 > OG画像」セクション参照
- `scripts.html` — greedy-navのvisible-links overflow解除、Gumshoeスクロールスパイの無効化、Embla Carouselの初期化、consent-banner.htmlのインクルード
- `consent-banner.html` — 同意バナー。localStorage `ifr25ku:consent` で状態管理し、`window.IFR25KU_CONSENT.hasAccepted` を公開
- `footer.html` — `site.copyright` 配列をループ表示（MirageFairy Server/Generation 7/Yoruno Kakera）、Apache 2.0ライセンス表記、social-icons・RSS・Sitemap
- `footer/custom.html` — GitHubソースリンク（`page.path` をリポジトリURLに組み込み）、Markdownファイルのダイレクトリンク

### _config.yml

```yaml
locale: "ja-JP"
title: IFR25KU
url: https://mirrgieriana.github.io
baseurl: /IFR25KU
theme: minimal-mistakes-jekyll
minimal_mistakes_skin: default
copyright:
  - year: 2019
    name: "MirageFairy Server"
    license: "CC BY-SA 3.0"
    license_url: "https://creativecommons.org/licenses/by-sa/3.0/"
  - year: 2024
    name: "The Developer of MirageFairy, Generation 7"
    license: "CC BY 4.0"
    license_url: "https://creativecommons.org/licenses/by/4.0/"
  - year: 2025
    name: "Yoruno Kakera"
    license: "CC BY 4.0"
    license_url: "https://creativecommons.org/licenses/by/4.0/"

plugins:
  - jekyll-include-cache

defaults:
  - scope:
      path: ""
    values:
      layout: single
      toc: true
      sidebar:
        nav: sidebar
```

全ページにsingleレイアウト・TOC・サイドバーがデフォルト適用される。テーマの `toc_sticky` は未使用（TOCのsticky挙動は「レイアウト > ペイン構造」で実装）。

### front matterとファイル変換

- front matter（`---`で囲まれたブロック）を持つファイルはJekyllに「処理対象」として扱われる
- `.md` ファイルはfront matterがあるとHTMLに変換される（元の.mdはJekyll出力に残らない）
- front matterがないファイルは静的ファイルとしてそのままコピーされる

## ナビゲーション

`site/src/main/resources/_data/navigation.yml` で定義。

### mastheadメニュー

テーマのデフォルトmastheadはドロップダウン非対応（フラットなliリスト）。`site/src/main/resources/_includes/masthead.html` でオーバーライドし、`children` キーに対応。

```yaml
main:
  - title: DOWNLOADS
    children:
      - title: Modrinth
        url: https://modrinth.com/mod/ifr25ku
        icon: /assets/images/modrinth.svg
      - title: CurseForge
        url: https://www.curseforge.com/minecraft/mc-mods/ifr25ku
        icon: /assets/images/curseforge.svg
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
      - title: Recipe Table
        url: /recipe-table-index.html
      - title: IFRKU Official Web Site (Old)
        url: https://kakera-unofficial.notion.site/ifrku
      - title: Discord
        url: https://discord.gg/bppQyAZtkA
        icon: /assets/images/discord.svg
```

ドロップダウンCSS: `.masthead__menu-item--dropdown` でホバー時に `.masthead__dropdown` を表示。

### 左サイドバー

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
      - title: Lang Table (JSONL)
        url: /lang_table.jsonl
      - title: Lang Table (CSV)
        url: /lang_table.csv
      - title: Recipe Table
        url: /recipe-table-index.html
      - title: Recipe Table (JSONL)
        url: /recipe_table.jsonl
```

テーマの `nav_list` includeがchildrenに対応済み。

## コンテンツ

### ブログ記事

`site/src/main/resources/_posts/YYYY-MM-DD-slug.md` に配置。

front matter例:

```yaml
---
title: "【アタノール】記事タイトル【つむぎ×ずんだもん】"
description: 記事の簡単な説明
layout: theater
header:
  teaser: /assets/images/2026/03/22/athanor/2026-03-22_15.11.57.webp
tags: [ミラージュフェアリー劇場, アタノール]
---
```

- `header.overlay_image`: overlay型（タイトルが画像に重なる）。OG画像生成のベース画像としても使用
- `header.image`: ヒーロー画像（非overlay、タイトルは画像の下）。OG画像生成のベース画像としても使用
- `header.teaser`: 一覧カードのサムネイル。OG画像生成のベース画像としても使用（最低優先度）
- `header.height`: ヒーロー画像の高さ指定（例: `200px`）

画像パスは `page.url` ベース: `/assets/images/YYYY/MM/DD/slug/ファイル名.webp`

トップページの `{% include recent-posts.html limit=8 more=true %}` で新着8件をカード表示。記事一覧ページ（`posts.md`）では `{% include recent-posts.html %}` でデフォルト10件表示。

### 特殊ファイル

標準的なJekyll変換では済まない、独自のビルドパイプラインを持つファイル。

- **CHANGELOG** (`site/src/main/resources/CHANGELOG.md`): front matterあり → JekyllがHTMLに変換 → `CHANGELOG.html` として出力。`buildSite` タスクで `src/main/resources/` 内の `.md` ファイルを `site/build/site/` にコピーしてmd版も配信。CHANGELOG.html冒頭に「Markdown版はこちら」リンクあり。
- **Lang Table**: `site/src/langTable/html/lang_table.html` はテンプレートHTML（テーマレイアウトなし）。`<%= trs %>` はGradleの `makeLangTable` タスクで展開。JavaScript検索機能付き（正規表現対応、URLパラメータ `?q=` で初期値復元）。出力先は `site/build/langTable/`。`site/src/main/resources/lang-table-index.md` はテーマレイアウトを使った特設ページで、各形式（HTML/JSON/JSONL/CSV）へのリンクを配置。

### ページ一覧

| ファイル | Layout | Header型 | 右ペイン | 左サイドバー |
|---|---|---|---|---|
| `index.md` | splash | overlay_color + carousel + actions | なし | なし |
| `CHANGELOG.md` | single（デフォルト） | image（非overlay）+ height | TOC | デフォルト |
| `posts.md` | splash | なし | なし | なし |
| `lang-table-index.md` | single（デフォルト） | image（非overlay）+ height | TOC | デフォルト |
| `recipe-table-index.md` | single（デフォルト） | image（非overlay）+ height | TOC | デフォルト |
| `_posts/*.md` | theater | teaserのみ（ヒーロー画像なし） | 関連記事リスト | デフォルト |

`index.md` 特有のfront matter:

```yaml
carousel:
  - overlay_image: /assets/images/index/banner1.webp
  - overlay_image: /assets/images/index/banner2.webp
  - overlay_image: /assets/images/index/banner3.webp
header:
  og_image: /assets/images/index/banner1.webp
  overlay_color: "#1a1a2e"
  overlay_filter: "linear-gradient(...)"
  actions:
    - label: "Modrinth"
      url: "https://modrinth.com/mod/ifr25ku"
      icon: "/assets/images/modrinth.svg"
```

## レイアウト

### ペイン構造

テーマのfloatベースのレイアウトをFlexbox/Gridに全面移行済み。`main.scss` でテーマのfloatを打ち消している。

- `#main`: `display: flex`（左サイドバー + メインコンテンツの横並び）。テーマのclearfix `::after` は `display: none` で除去
- `.sidebar`: `float: none; flex-shrink: 0;`
- `.page`, `.splash`: `float: none; flex: 1; min-width: 0;`
- `.page__inner-wrap`: `display: grid`（コンテンツ + 右ペインの2カラム）。右ペインがあれば暗黙的な2列目が生成される
- `.sidebar__right`: `grid-column: 2; grid-row: 1 / span 999; align-self: start;` で右カラムに配置
- 子要素の `.toc` は `position: sticky; max-height: calc(100vh - 4em); overflow-y: auto;` でスクロール追従する（stickyはsidebar自身ではなく `.toc` が持つ）
- 劇場レイアウトの `.theater__sidebar` はstickyを使わず通常配置
- `.page__related`, `.archive`, `.breadcrumbs ol`, `.page__comments`: floatを解除済み

### singleレイアウト

ヘッダー画像の設定方式で3パターンのDOM構造が生じる。条件分岐は `{% unless page.header.overlay_color or page.header.overlay_image %}` で `<header>` の出力を制御。

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

ヒーロー画像がbackground-imageとして表示され、タイトルが画像の上に重なる。`page__inner-wrap` 内の `<header>` はレンダリングされない（h1はヒーロー内のみ）。

```
div.page__hero--overlay > div.wrapper > h1.page__title
                                       > page__meta
...
page__inner-wrap > section.page__content（headerなし）
```

### splashレイアウト

`index.md` で使用。`page.carousel` が定義されている場合は `hero-carousel.html` でカルーセル表示、それ以外は `page__hero.html` で通常のヒーロー表示。ヘッダー画像がない場合は通常のh1を `<div class="content-wrap">` で囲んで表示（`site/src/main/resources/_layouts/splash.html` でオーバーライド済み）。

### 劇場レイアウト

ミラージュフェアリー劇場記事（`_posts/*.md`）専用のレイアウト。`site/src/main/resources/_layouts/theater.html` で定義。`single.html` を元に分離したもので、右ペインを TOC から関連記事リストに置き換えている。`page.header.video`, `page.link`, `site.comments`, `page__related` の各ブロックは劇場記事では不要なため削除済み。シェアボタンは常に表示（`if page.share` 分岐なし）。

**DOM構造**:

```
.theater__sidebar (aside, flex column, gap: 1em)
├── header
│   └── h4.nav__title ― 「関連記事」見出し
└── .theater-recommendations (flex column, gap: 1em)
    ├── a.recent-posts__card
    ├── a.recent-posts__card
    └── ...（最大8枚）
```

- `.theater__sidebar` は `display: flex; flex-direction: column; gap: 1em;` で見出しとカードリストを分離
- `.theater-recommendations` も `flex column + gap: 1em;` でカード間を分離
- sticky は使わず通常のスクロールに任せる
- カードはトップページの新着記事カードと同じ `recent-posts__card` / `recent-posts__teaser` / `recent-posts__body` / `recent-posts__title` クラスを共有してスタイルを統一
- `.nav__title` はグローバルに `padding: 0` に上書きされている（テーマの `padding: 0.5rem 0.75rem` を打ち消し、上下のギャップは親の `gap` に委ねる。`.toc .nav__title` は `display: none` なので影響なし）

**関連記事の抽選**: `theater.html` 末尾のインラインJSで次の手順を実装する。

1. `fetch('/posts.json')` で全記事メタ（title/url/teaser/tags）を取得
2. 現在ページ自身を除外
3. 各記事の重みを計算：`weight = (現在ページのタグと一致するタグ数) + 1`（+1で全記事の抽選機会を確保）
4. 非復元の重み付きランダムサンプリングで最大8件選出
5. `createCard` でDOM要素を生成し `container.replaceChildren(...)` でまとめて描画

テキストは `textContent` で設定するため HTML エスケープ不要、XSS リスクなし。JSはこの1レイアウトでしか使わず小規模なので外部ファイル化していない。

## CSS

`site/src/main/resources/assets/css/main.scss` にテーマの変数定義とカスタムスタイルを記述する。

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

- `.masthead { border-bottom: 4px solid #FF2DAB; box-shadow; background: #000; }` — mastheadの装飾
- `.greedy-nav { background: transparent; }` — mastheadの背景を透過
- `.greedy-nav .visible-links { overflow: clip visible; }` — ドロップダウン表示のためoverflowを解除
- `.greedy-nav a` — テーマの広すぎるセレクタを打ち消すため `display: revert; margin: revert;` でリセットし、`.greedy-nav .visible-links a, .greedy-nav .hidden-links a` にのみ再適用。バナーは `a.site-title` で特定性を揃えて `display: flex` を適用
- `.site-title__face { width: 3em; height: 3em; }` — 正方形、greedy-nav幅計測対策
- `.site-title__logo { width: 7.8em; }` — 109×35px画像の比率に基づく明示幅、greedy-nav幅計測対策
- `body::before` — `background.webp` を `blur(24px)` で全面背景に
- `.initial-content { background: rgba(255, 255, 255, 0.80); }` — 半透明白の本文背景
- `.page__hero--overlay` — min-height: 600px、flex-end配置
- `.page__header--plain { margin-bottom: 1em; }` — ヘッダー画像なし時のh1下マージン
- `.recent-posts__grid` — `repeat(auto-fill, minmax(250px, 1fr))` のカードグリッド
- `.section-header` — フルワイド黒背景、上下 `4px #FF2DAB` ボーダー、box-shadow
- `.section-separator` — フルワイド黒背景、上下 `4px #FF2DAB` ボーダー、box-shadow
- `.page__footer` — 黒背景、上 `4px #FF2DAB` ボーダー、box-shadow
- `.hidden-links .masthead__menu-item--dropdown { border: none; }` — ハンバーガーメニュー内のドロップダウン区切り線を除去
- `.hidden-links .masthead__dropdown li { border-bottom: none; }` — ハンバーガーメニュー内の子項目間ボーダーを除去
- `html { font-size: 14px; }` — テーマのデフォルトから縮小

### テーマデフォルトの注意点

- `.masthead` にはデフォルトで `border-bottom: 1px solid $border-color` がある
- `.page__hero--overlay` にはデフォルトで `margin-bottom: 2em` がある

### 手動コンパイル

Gradleを経由せずにSCSSの変更を素早く確認する方法:

```bash
cp site/src/main/resources/assets/css/main.scss site/build/jekyllSource/assets/css/main.scss && (cd site/build/jekyllSource && BUNDLE_APP_CONFIG="$(pwd)/../bundleConfig" bundle exec jekyll build --destination ../jekyllBuild)
```

WSL2の `/mnt/` ドライブでは `inotify` が動作しないため、`jekyll serve` の自動リビルドは使えない。

## JavaScript

### scripts.html

`site/src/main/resources/_includes/scripts.html` でオーバーライド済み。

- DOMContentLoaded後に `.greedy-nav .visible-links` の `overflow` を `visible` に書き換えてドロップダウン表示を可能にする
- テーマの `main.min.js` 読み込み後に、`gumshoeActivate` イベントのキャプチャフェーズで `stopImmediatePropagation()` して無効化。これにより、GumshoeのスクロールスパイがアクティブなTOC項目へ自動スクロールする挙動（`scrollTocToContent`）を抑制
- Embla Carousel（CDN読み込み、v8.6.0）: `.hero-carousel__viewport` に対してloop・autoplay（10秒間隔、`stopOnInteraction: false`）を初期化。ドットナビゲーション（JS動的生成）、プログレスバー（CSS animation `hero-carousel-progress` 10秒、スライド遷移時リセット）、ヒーロー背景パンアニメーション（Web Animations APIによるズーム＆パン、ResizeObserverで追従、スケール1/0.9→1.0 + X軸移動、画像サイズ動的検出）を実装
- `{% include consent-banner.html %}` で同意バナーを描画

### greedy-navの幅計測

greedy-navは `overflow` ではなく `outerWidth()` の明示的な幅比較でアイテム移動を判定する。テーマは `.site-logo img` のロード完了を待ってから `check()` を実行するが、このプロジェクトでは `.site-logo` を使わず `.site-title` 内に画像を2枚配置しているため、画像ロード待ちが効かない。対策として `.site-title__face` と `.site-title__logo` にCSS明示幅を設定し、画像ロード前でもレイアウトが確定するようにしている。

## 画像

### 配置規則

画像パスは `page.url` に基づくディレクトリ構造で配置する。

- 通常ページ: `assets/images/<ページ名>/` — 例: `assets/images/changelog/`
- ブログ記事: `assets/images/YYYY/MM/DD/<slug>/` — 例: `assets/images/2026/03/22/athanor/`
- OG画像: 同じ階層に `<slug>.og.webp` として配置 — 例: `assets/images/2026/03/22/athanor.og.webp`

```
assets/images/
├── background.webp                    — 全面背景（blur処理）
├── curseforge.svg                     — mastheadアイコン
├── discord.svg                        — mastheadアイコン
├── ifr25ku_banner-black-gothic.webp   — ロゴバナー
├── miragefairy_face_256.webp          — Favicon
├── modrinth.svg                       — mastheadアイコン
├── og-default-background.svg          — OG画像デフォルト背景（1200×630 SVG、淡紫グラデーション+六角形パターン）
├── index/
│   ├── banner1.webp                   — カルーセルヒーロー
│   ├── banner2.webp                   — カルーセルヒーロー
│   └── banner3.webp                   — カルーセルヒーロー
├── changelog/
│   └── changelog-header.svg           — CHANGELOGページヘッダー
├── lang-table-index/
│   └── lang-table-header.svg          — Lang Tableページヘッダー
└── YYYY/MM/DD/slug/                   — 各記事の画像（page.urlベース）
```

### OG画像

`seo.html` で `page.url` からOG画像パスを自動導出する。

```liquid
{%- assign og_page_url = page.url -%}
{%- if og_page_url == '/' or og_page_url == '' -%}
  {%- assign og_page_url = '/index.html' -%}
{%- endif -%}
{%- assign og_image_url = '/assets/images' | append: og_page_url | replace: '.html', '.og.webp' | absolute_url -%}
```

変換例:

| page.url | og_image_url |
|---|---|
| `/` | `/assets/images/index.og.webp` |
| `/CHANGELOG.html` | `/assets/images/CHANGELOG.og.webp` |
| `/2026/03/22/athanor.html` | `/assets/images/2026/03/22/athanor.og.webp` |

Gradle側の `generateOgImages` タスクは `.md` ファイルから同じ `page.url` ベースのパスを導出する:

- `_posts/YYYY-MM-DD-slug.md` → `src/main/resources/assets/images/YYYY/MM/DD/slug.og.webp`
- `page.md` → `src/main/resources/assets/images/page.og.webp`

`OgImageRenderer` の実装:

- Playwright Chromium で 1200×630 のビューポートにHTMLをレンダリング
- ベース画像の優先順位: `header.overlay_image` > `header.image` > `header.teaser` > `og-default-background.svg`
- ベース画像をData URI化してCSS `background: url(...) center/cover no-repeat` で表示
- タイトルを半透明黒帯（`rgba(0,0,0,0.5)`）の上に白文字36pxで表示
- スクリーンショットをPNG byte[]として取得 → `ImageIO.read()` で BufferedImage → `ImageIO.write(image, "webp", ...)` でWebP出力

### 画像変換ユーティリティ

`site/scripts/convert-to-webp.sh <入力ファイル> [slug]` で `site/build/convertedWebp/<slug>.webp` に変換。slug省略時は入力ファイルの拡張子を除いた名前を使用。ImageMagickの `convert` を使用（-quality 80）。スラグ検証: `^[a-zA-Z0-9_.-]+$`。

| ツール | パッケージ | 用途 | 標準インストール |
|---|---|---|---|
| convert | imagemagick | 画像全般（最もメジャー） | No |
| ffmpeg | ffmpeg | 動画・音声・画像 | No |
| cwebp | webp | webp変換専用（Google公式） | No |

いずれもUbuntuで標準インストールされない。事前に変換してコミットすればビルド環境に依存しない。
