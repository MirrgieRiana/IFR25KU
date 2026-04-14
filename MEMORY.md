# MEMORY.md

AIアシスタントが自由に編集できる、コミットされる永続的なメモ用ファイル。
プロジェクトに関するノウハウや調査結果を蓄積する。

## ビルドシステム

### タスクフロー

`site/build.gradle.kts` で定義。主要なタスクの依存関係:

1. `makeLangTable`（generate）: 言語JSONとHTMLテンプレートから `site/build/langTable/` に生成
2. `makeRecipeTable`（generate）: レシピデータを生成
3. `installJekyllBundle`（other）: `site/scripts/bundle-install.sh` を実行。gemは `site/build/bundleVendor/` にインストール
4. `generateOgImages`（generate）: `src/pages/resources/` 内の `.md` からOG画像を `src/ogImages/resources/assets/images/` に生成。入力ハッシュで差分スキップ（`-Pregenerate` で強制再生成）
5. `syncJekyllSource`（other）: 複数ソースを `site/build/jekyllSource/` に統合。`dependsOn(generateOgImages)`
    - `src/main/resources/` — テーマオーバーライド、レイアウト、プラグイン、共通画像
    - `src/ogImages/resources`（`**/*.webp`） — 生成済みOG画像
    - `src/external/resources` — 外部リソース
    - `src/pages/resources` — ページと記事（.mdは `_posts/` へ、画像は `YYYY/MM/DD/filename` にフラット配置。衝突チェックあり）
    - `src/main/bundle` — Gemfile等
6. `jekyllBuild`（build）: `site/scripts/build-site.sh` を実行。`dependsOn(installJekyllBundle)` だが、UP-TO-DATE判定コスト削減のため敢えてinputsにしていない
7. `buildSite`（build）: `jekyllBuild` + `makeLangTable` + `makeRecipeTable` + `src/pages/resources/` 内の `.md` を `site/build/site/` に統合

CI出力先は `site/build/site/`。

`serveSite`（application）: Ktor Nettyサーバーで `site/build/site/` を `http://localhost:4000/IFR25KU/` に配信。Jekyll標準の `jekyll serve` ではなくKtor Nettyを使用する理由は、jekyll serveが頻繁に落ちて使い物にならないため。

### buildscript依存関係

`site/build.gradle.kts` の `buildscript` で定義。SnakeYAML（front matter解析）、Playwright（OG画像のChromiumスクリーンショット）、WebP変換ライブラリを使用。具体的なライブラリとバージョンはファイルを参照。

## Jekyll

### テーマオーバーライドの仕組み

minimal-mistakesテーマのファイルは `site/build/bundleVendor/` 配下にある。オーバーライドするには、同じ相対パスで `site/src/main/resources/` 内にファイルを配置する。

**注意**: `_sass/` のパーシャルはオーバーライドできない。Sassの `@import` はインポート元ファイルのディレクトリを最初に検索するため、テーマのパーシャルが常に優先される。CSSのカスタマイズは `site/src/main/resources/assets/css/main.scss` の `@import "minimal-mistakes"` の後に記述する。

### _layouts/

`site/src/main/resources/_layouts/` に配置。

- `single.html` — 各コンテンツページ。ヘッダー画像の設定方式で3パターンのDOM構造が生じる（overlay / image / なし）
- `splash.html` — トップページ・記事一覧。`page.carousel` 定義時はカルーセル表示
- `theater.html` — 劇場記事専用。`single.html` から分離し、右ペインをTOCから関連記事に置き換え、本文下部にも推薦グリッドを配置。末尾にインラインJSを持つ

### _includes/

`site/src/main/resources/_includes/` に配置。各ファイルの役割はファイル先頭を読めば分かる。以下は読んだだけでは分かりにくい設計上のポイント:

- `masthead.html` — テーマデフォルトはドロップダウン非対応。`children` キーに対応するためオーバーライドしている
- `head/custom.html` — Favicon設定に加えて `window.ifr25ku` グローバルオブジェクト（`loadJson`/`saveJson`/`stripBaseUrl`/`events`）を定義。他の複数includeがこれに依存する
- `scripts.html` — greedy-navのoverflowをJS側で `visible` に書き換えている理由は、CSS初期値のままだとドロップダウンが切れるため。Gumshoeのスクロールスパイはキャプチャフェーズで無効化している（TOC項目への自動スクロールが邪魔になるため）
- `consent-banner.html` — `window.ifr25ku.consent` を公開。`visit-tracker.html` がこの同意状態に依存する
- `visit-tracker.html` — 訪問記録（`ifr25ku:visits:pages`）を保存。劇場レイアウトの関連記事抽選の重み調整に使用される

### _plugins/

`site/src/main/resources/_plugins/` に配置。主要な機能:

- `say.rb` + `say/` 配下のprovider群 — `{% say %}` ブロックタグでキャラクター会話を表示。VOICEVOX立ち絵のレイヤー合成を行う。キャラクターは `_data/voicevox.yml` で登録
- `posts_generator.rb` — 全記事メタを `posts.json` に書き出す。劇場レイアウトの関連記事抽選が依存
- `space.rb` — `{% space %}` タグで場面転換の区切りを挿入

### _config.yml

`site/src/main/resources/_config.yml` で定義。読んだだけでは分かりにくい設定:

- `future: true` — ビルド時に未来日付の記事も出力する（デフォルトでは未来日付は除外される）
- `defaults` で全ページに `single` レイアウト・TOC・サイドバーをデフォルト適用。テーマの `toc_sticky` は未使用（TOCのsticky挙動は `main.scss` 側の `.toc { position: sticky; }` で実装）

### front matterとファイル変換

- front matter（`---`で囲まれたブロック）を持つファイルはJekyllに「処理対象」として扱われる
- `.md` ファイルはfront matterがあるとHTMLに変換される（元の.mdはJekyll出力に残らない）
- front matterがないファイルは静的ファイルとしてそのままコピーされる

## ナビゲーション

`site/src/main/resources/_data/navigation.yml` で定義。テーマデフォルトのmastheadはドロップダウン非対応（フラットなliリスト）だが、`masthead.html` のオーバーライドで `children` キーに対応させている。

## コンテンツ

### ページの配置

全ページは `site/src/pages/resources/<name>/<name>.md` に配置される。`syncJekyllSource` がJekyllソースに変換する:

- ブログ記事（`YYYY-MM-DD-slug/` 形式のディレクトリ）: .mdは `_posts/` へ、画像は `YYYY/MM/DD/filename` にフラット配置
- その他のページ: .mdはルートへ、画像は `assets/images/<name>/` へ

各ページのレイアウトやfront matterの詳細は、個々のファイルを参照。

### ブログ記事

`site/src/pages/resources/YYYY-MM-DD-slug/YYYY-MM-DD-slug.md` に配置。画像も同じディレクトリ内に同梱する。

front matterの要点:

- `header.teaser`: パスは `/YYYY/MM/DD/ファイル名.webp` 形式（`/assets/images/` プレフィックスなし、slug階層なし）。ビルド時に `syncJekyllSource` がフラット配置するため
- `header.overlay_image` / `header.image` / `header.teaser`: OG画像生成のベース画像としても使用される（優先順位はこの順）

インライン画像は相対パスで参照: `![](filename.webp)`（`syncJekyllSource` が記事の出力先と同じ階層にフラット配置するため、`relative_url` フィルタ不要）

### 劇場記事

`site/src/pages/resources/` 配下のブログ記事はすべて劇場形式（つむぎとずんだもんの会話劇）で書かれている。`layout: theater` とカスタム Liquid タグを使用する。

**theater-creator スキル**

`site/theater-creator/` は Claude Code スキル定義。劇場記事の制作時にはこのスキルを呼び出す。詳細は原本を参照する（MEMORY.md に複写すると陳腐化する）。

**`{% say %}` タグ**

キャラクターの会話を表示するブロックタグ。構文:

```
{% say <char>[:<preset>...][:<key>=<value>...] %}
本文（Markdown可）
{% endsay %}
```

例:

```
{% say zundamon23 %}のだ。{% endsay %}
{% say tsumugi3:口=わあーい:まゆ=困り眉 %}センパーイ！{% endsay %}
```

- 登録済みキャラは `_data/voicevox.yml` で定義
- 属性のキー・値は `site/build/jekyllSource/assets/images/voicevox/extracted/{slug}/presets.json` を参照
- `radio` パーツは1つだけ選択（未指定時は default 要素）、`checkbox` パーツはカンマ区切りで複数指定可

**`{% space %}` タグ** — 場面転換の区切りを挿入するインラインタグ。

**タグの設計**

劇場レイアウトの関連記事抽選はタグ一致数で重み付けされるため、タグの付け方が関連記事の質に直結する。

- 必須: `ミラージュフェアリー劇場`（カテゴリ分類）
- 機能タグ: 登場する具体的アイテム名。重なりが多い記事ほど関連度が高く抽選される
- メタタグ: `アップデート`, `お知らせ` 等（必要に応じて）

### 特殊ファイル

標準的なJekyll変換では済まない、独自のビルドパイプラインを持つファイル。

- **CHANGELOG** (`site/src/pages/resources/CHANGELOG/CHANGELOG.md`): JekyllがHTMLに変換し、`buildSite` が `.md` もそのままコピーしてmd版も配信
- **Lang Table**: `site/src/langTable/html/lang_table.html` はテンプレートHTML。`<%= trs %>` をGradleの `makeLangTable` タスクで展開。`site/src/pages/resources/lang-table-index/lang-table-index.md` はテーマレイアウトを使った特設ページ
- **posts.json**: `_plugins/posts_generator.rb` がJekyllビルド時に全記事メタを書き出す。劇場レイアウトのJSが読み込む

## 世界観リファレンス

IFR25KU の世界観用語・アイテム説明・ポエムはゲーム本体の lang ファイルとコード側の Module に定義されている。劇場記事を書くときの発想源として参照する。

### lang JSON

`common/src/generated/resources/assets/miragefairy2024/lang/ja_jp.json` に全テキストが集約されている（ビルド時にコードから自動生成）。主要なキーパターン: `item.miragefairy2024.<item>.name` / `.poem` / `.description`、`block.miragefairy2024.<block>.name`、`advancements.miragefairy2024.<item>.title` / `.description`。

### コード側の定義

ポエムや説明文は Kotlin コードのハードコードとして定義されている。`common/src/main/kotlin/miragefairy2024/mod/` 配下の各種 Module ファイルに `.poem` と `.description` の定義が散在。

### 制作時の注意

**世界観テキストを劇中キャラが直接喋ってはいけない**（`site/theater-creator/SKILL.md` に明記）。劇中キャラクターはゲーム内 GUI や公式テキストを知らないため、ポエムをそのまま口にすると不自然になる。世界観資料は「発想源」として扱い、キャラクターの視点に翻訳して表現する必要がある。

## レイアウト

### ペイン構造

テーマのfloatベースのレイアウトをFlexbox/Gridに全面移行済み。`main.scss` でテーマのfloatを打ち消している。具体的なプロパティは `main.scss` 冒頭を参照。

設計上の注意点:

- `#main` は `display: flex` で左サイドバーとメインコンテンツを横並びにしている（テーマのclearfix `::after` は除去）
- `.page__inner-wrap` は `display: grid` で右ペイン（TOCまたは関連記事）を配置
- TOCのsticky挙動は `.sidebar__right` ではなく子要素の `.toc` が `position: sticky` を持つことで実現

### singleレイアウト

ヘッダー画像の設定方式で3パターンのDOM構造が生じる。`{% unless page.header.overlay_color or page.header.overlay_image %}` で分岐する。具体的な構造は `_layouts/single.html` を参照。

### 劇場レイアウト

`site/src/main/resources/_layouts/theater.html` で定義。`single.html` から分離したもので、以下の点が異なる:

- 右ペインがTOCではなく関連記事リスト
- 本文下部にも推薦グリッドを配置
- teaser画像をヒーロー画像なし時にバナー表示（`page__teaser-banner`）
- `page.header.video`, `page.link`, `site.comments`, `page__related` は不要なため削除済み

**関連記事の抽選**: タグ一致数と訪問履歴で重み付けした非復元ランダムサンプリング。サイドバーと本文下部はそれぞれ独立に抽選する。具体的な重み計算や閾値は `theater.html` 末尾のインラインJSを参照。

## CSS

`site/src/main/resources/assets/css/main.scss` にテーマの変数オーバーライドとカスタムスタイルを記述する。紫系を基調とし、アクセントにホットピンクを多用。具体的な変数値はファイル冒頭を参照。

### 設計上の注意点

- `.greedy-nav a` はテーマの広すぎるセレクタを打ち消すため `display: revert; margin: revert;` でリセットし、`.visible-links a` と `.hidden-links a` にのみ再適用している
- `.site-title__face` と `.site-title__logo` にCSS明示幅を設定している理由: greedy-navは `outerWidth()` で幅計測してアイテム移動を判定するが、このプロジェクトでは `.site-logo` を使わず `.site-title` 内に画像を2枚配置しているため、テーマの画像ロード待ちが効かない。明示幅により画像ロード前でもレイアウトを確定させている
- `body::before` で `background.webp` を `blur(24px)` で全面背景にしている
- `.masthead` にはテーマデフォルトで `border-bottom: 1px solid $border-color` がある（カスタムスタイルで上書き）
- `.page__hero--overlay` にはテーマデフォルトで `margin-bottom: 2em` がある（カスタムスタイルで上書き）

### 手動コンパイル

Gradleを経由せずにSCSSの変更を素早く確認する方法:

```bash
cp site/src/main/resources/assets/css/main.scss site/build/jekyllSource/assets/css/main.scss && (cd site/build/jekyllSource && BUNDLE_APP_CONFIG="$(pwd)/../bundleConfig" bundle exec jekyll build --destination ../jekyllBuild)
```

WSL2の `/mnt/` ドライブでは `inotify` が動作しないため、`jekyll serve` の自動リビルドは使えない。

## 画像

### 配置規則

画像のソースと出力先の関係:

- **共通画像**: `site/src/main/resources/assets/images/` に配置 → そのまま `assets/images/` に出力。Favicon、ロゴ、mastheadアイコン、背景等
- **通常ページ画像**: `site/src/pages/resources/<ページ名>/` に .md と同梱 → `syncJekyllSource` が `assets/images/<ページ名>/` に配置
- **ブログ記事画像**: `site/src/pages/resources/YYYY-MM-DD-slug/` に .md と同梱 → `syncJekyllSource` が `YYYY/MM/DD/ファイル名` にフラット配置（slug階層なし、`assets/images/` なし）。ファイル名の衝突チェックあり
- **OG画像**: `generateOgImages` が `site/src/ogImages/resources/assets/images/` に生成 → `syncJekyllSource` が取り込み
- **OG画像デフォルト背景**: `site/src/ogImages/assets/default-background.svg` に配置（`generateOgImages` タスクが直接参照。Jekyllパイプラインには含まれない）

### OG画像

`seo.html` が `page.url` から `/assets/images/` + url + `.og.webp` のパスを自動導出する。Gradle側の `generateOgImages` タスクも同じ規則で出力先を決定する。

ベース画像の優先順位: `header.og_background` > `header.overlay_image` > `header.image` > `header.teaser` > デフォルト背景SVG。front matterの画像パスからファイル名を抽出し、.mdファイルと同じディレクトリ内のローカルファイルを参照する。

### 画像変換ユーティリティ

`site/scripts/convert-to-webp.sh` でImageMagickによるWebP変換が可能。ImageMagick・ffmpeg・cwebp はいずれもUbuntuで標準インストールされないため、事前に変換してコミットすればビルド環境に依存しない。
