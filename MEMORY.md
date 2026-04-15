# MEMORY.md

AIアシスタントが自由に編集できる、コミットされる永続的なメモ用ファイル。
ソースコード上のドキュメントやコメントから読み取れない暗黙知と、リポジトリのファイル構造の俯瞰を蓄積する。

## ファイル構造

### サイト関連 (`site/`)

- `build.gradle.kts` — Jekyllビルド・OG画像生成・サイト配信のタスク定義
- `src/main/resources/` — Jekyllのテーマオーバーライド配置先（`_layouts/`, `_includes/`, `_plugins/`, `_data/`, `_config.yml`, `assets/`）
- `src/main/bundle/` — Gemfile等
- `src/pages/resources/<name>/<name>.md` — 全ページ・記事のソース。`syncJekyllSource` がJekyll形式に変換
- `src/external/resources/` — 外部リソース
- `src/ogImages/` — OG画像生成のソース・出力先・デフォルト背景SVG
- `src/langTable/html/lang_table.html` — `<%= trs %>` を `makeLangTable` タスクで展開するテンプレート
- `scripts/` — `bundle-install.sh`, `build-site.sh`, `convert-to-webp.sh`
- `build/site/` — CIの最終出力先
- `build/bundleVendor/` — minimal-mistakesテーマを含む gem のインストール先
- `ifr25ku-site-claude-marketplace/ifr25ku-site/` — Claude Code プラグイン `ifr25ku-site` の本体。`skills/theater-creator/` は劇場記事制作スキル

### 世界観テキスト (`common/`)

劇場記事を書くときの発想源として参照する。

- `src/generated/resources/assets/miragefairy2024/lang/ja_jp.json` — 全テキストの集約先（コードから自動生成）。`item.miragefairy2024.<item>.name`/`.poem`/`.description`、`block.miragefairy2024.<block>.name`、`advancements.miragefairy2024.<item>.title`/`.description` 等のキーパターン
- `src/main/kotlin/miragefairy2024/mod/` — Module ファイル群。ポエムや説明文のハードコード定義が散在

## ビルド・サイト配信

### `serveSite` がKtor Nettyを使う理由

Jekyll標準の `jekyll serve` は頻繁にクラッシュするため、`site/build/site/` を `http://localhost:4000/IFR25KU/` に配信する独自のKtor Nettyサーバーを使用している。

### `jekyllBuild` が `installJekyllBundle` を inputs にしていない理由

依存関係としては `dependsOn(installJekyllBundle)` だが、UP-TO-DATE判定コスト削減のため敢えて inputs には含めていない。

### `generateOgImages` の差分スキップ

入力ハッシュで差分スキップする。`-Pregenerate` で強制再生成。

### WSL2環境での `jekyll serve` 制約

`/mnt/` ドライブでは `inotify` が動作しないため、`jekyll serve` の自動リビルドは使用できない。

### SCSSの手動コンパイル

Gradleを経由せずSCSSの変更を素早く確認する方法:

```bash
cp site/src/main/resources/assets/css/main.scss site/build/jekyllSource/assets/css/main.scss && (cd site/build/jekyllSource && BUNDLE_APP_CONFIG="$(pwd)/../bundleConfig" bundle exec jekyll build --destination ../jekyllBuild)
```

## Jekyll

### Sassパーシャルはオーバーライド不能

minimal-mistakesテーマの通常ファイルは `site/src/main/resources/` 内に同じ相対パスで配置すればオーバーライドできるが、`_sass/` のパーシャルは例外。Sassの `@import` がインポート元ファイルのディレクトリを最初に検索するため、テーマのパーシャルが常に優先される。CSSのカスタマイズは `assets/css/main.scss` の `@import "minimal-mistakes"` の後に記述する。

### `syncJekyllSource` の画像配置規則

| ソース配置 | 出力配置 |
| --- | --- |
| `src/main/resources/assets/images/` | そのまま `assets/images/` |
| `src/pages/resources/<ページ名>/` の画像 | `assets/images/<ページ名>/` |
| `src/pages/resources/YYYY-MM-DD-slug/` の画像 | `YYYY/MM/DD/ファイル名`（slug階層なし、`assets/images/` プレフィックスなし、ファイル名衝突チェックあり） |

ブログ記事の `header.teaser` パスは `/YYYY/MM/DD/ファイル名.webp` 形式で書く。インライン画像は相対パス `![](filename.webp)` で参照（記事本体と同階層に出力されるため `relative_url` フィルタは不要）。

### OG画像のベース画像優先順位

`generateOgImages` と `seo.html` が参照する front matter のフィールド:

`header.og_background` > `header.overlay_image` > `header.image` > `header.teaser` > `src/ogImages/assets/default-background.svg`

各画像パスからファイル名を抽出し、.mdファイルと同じディレクトリ内のローカルファイルを参照する。出力パスは `page.url` から `/assets/images/` + url + `.og.webp` として導出される。

### 特殊なビルドパイプラインを持つファイル

- **CHANGELOG** (`src/pages/resources/CHANGELOG/CHANGELOG.md`): JekyllがHTMLに変換し、`buildSite` が `.md` も別途コピーしてmd版も配信
- **Lang Table**: `src/langTable/html/lang_table.html` を `makeLangTable` タスクで展開。`src/pages/resources/lang-table-index/lang-table-index.md` はテーマレイアウトを使った特設ページ
- **posts.json**: `_plugins/posts_generator.rb` がJekyllビルド時に全記事メタを書き出す。劇場レイアウトのJSが読み込む

## レイアウト・CSS

### floatからFlexbox/Gridへの全面移行

minimal-mistakesテーマのfloatベースのレイアウトを `main.scss` で打ち消し、`#main` を `display: flex`、`.page__inner-wrap` を `display: grid` に置き換えている。テーマのclearfix `::after` は除去済み。

### TOCのsticky挙動

テーマの `toc_sticky` 設定は使用していない。代わりに `.toc { position: sticky; }` を子要素に直接適用することで実現している（`.sidebar__right` 側ではなく）。

### greedy-navの明示幅指定

`.site-title__face` と `.site-title__logo` にCSS明示幅を設定しているのは、greedy-navが `outerWidth()` で幅計測してアイテム移動を判定するため。このプロジェクトでは `.site-logo` を使わず `.site-title` 内に画像を2枚配置しているため、テーマの画像ロード待ちが効かず、明示幅で画像ロード前にレイアウトを確定させる必要がある。

### `.greedy-nav a` セレクタの上書き

テーマの広すぎるセレクタを打ち消すため `display: revert; margin: revert;` でリセットし、`.visible-links a` と `.hidden-links a` にのみ再適用している。

## 劇場記事

### 制作スキル

`/ifr25ku-site:theater-creator` で呼び出す。詳細は `site/ifr25ku-site-claude-marketplace/ifr25ku-site/skills/theater-creator/` の原本を参照（MEMORY.md への複写は陳腐化を招くため避ける）。

### 関連記事抽選とタグ設計

劇場レイアウト (`_layouts/theater.html` 末尾のインラインJS) はタグ一致数と訪問履歴 (`ifr25ku:visits:pages`、`_includes/visit-tracker.html` が記録) で重み付けした非復元ランダムサンプリングで関連記事を抽選する。サイドバーと本文下部はそれぞれ独立に抽選する。タグの付け方が関連記事の質に直結する:

- 必須: `ミラージュフェアリー劇場`（カテゴリ分類）
- 機能タグ: 登場する具体的アイテム名。重なりが多い記事ほど関連度が高く抽選される
- メタタグ: `アップデート`, `お知らせ` 等（必要に応じて）

### 世界観テキストの扱い

ゲーム本体のポエム・説明文（lang JSON や Module ファイル内のハードコード）を、劇中キャラがそのまま喋ってはいけない（`site/ifr25ku-site-claude-marketplace/ifr25ku-site/skills/theater-creator/SKILL.md` に明記）。劇中キャラはゲーム内 GUI や公式テキストを知らないため、世界観資料は「発想源」として扱い、キャラクターの視点に翻訳する必要がある。
