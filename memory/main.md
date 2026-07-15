# リポジトリの俯瞰と暗黙知なのだぁ🌱✨

このファイルは、ぴょこ（Claude Fairy）が自由に編集できる、コミットされる永続的なメモ帳なのだぁ〜🌱♪
ソースコード上のドキュメントやコメントからは読み取れない暗黙知と、リポジトリのファイル構造の俯瞰を、ここにためこんでいくのだぁ✨

## ファイル構造なのだぁ🌱

### サイト関連 (`site/`) なのだぁ🌱

- `build.gradle.kts` — Jekyllビルド・OG画像生成・サイト配信のタスク定義なのだぁ🌱
- `src/main/resources/` — Jekyllのテーマオーバーライドの配置先なのだぁ（`_layouts/`, `_includes/`, `_plugins/`, `_data/`, `_config.yml`, `assets/`）
- `src/main/bundle/` — Gemfile等なのだぁ🌱
- `src/pages/resources/<name>/<name>.md` — 全ページ・記事のソースなのだぁ✨ `syncJekyllSource` がJekyll形式に変換するのだぁ
- `src/external/resources/` — 外部リソースなのだぁ🌱
- `src/ogImages/` — OG画像生成のソース・出力先・デフォルト背景SVGなのだぁ🌱
- `src/langTable/html/lang_table.html` — `<%= trs %>` を `makeLangTable` タスクで展開するテンプレートなのだぁ✨
- `scripts/` — `bundle-install.sh`, `build-site.sh`, `convert-to-webp.sh` に加えて、`serveSite` の本体になる `serve-site.sh` と `serve-site.main.kts`（Ktor Netty サーバー）もここなのだぁ🌱
- `build/site/` — CIの最終出力先なのだぁ🌱
- `build/bundleVendor/` — minimal-mistakesテーマを含む gem のインストール先なのだぁ🌱
- `ifr25ku-site-claude-marketplace/ifr25ku-site/` — Claude Code プラグイン `ifr25ku-site` の本体なのだぁ✨ `skills/theater-creator/` は劇場記事制作スキル、`skills/zundamon-persona/` と `skills/kasukabe-tsumugi-persona/` は登場人物のペルソナ定義なのだぁ🌱

### 世界観テキスト (`common/`) なのだぁ🌱

劇場記事を書くときの発想源として参照するところなのだぁ〜🌱♪

- `src/generated/resources/assets/miragefairy2024/lang/ja_jp.json` — 全テキストの集約先で、コードから自動生成されるのだぁ🌱 アイテム名・ブロック名は `item.miragefairy2024.<item>` / `block.miragefairy2024.<block>` みたいに、`.name` を付けないキーそのものに値が入っているのだぁ✨ ポエムや説明文は `item.miragefairy2024.<item>.poem` / `.description`、進捗は `advancements.miragefairy2024.<item>.title` / `.description` みたいなキーパターンなのだぁ🌱
- `src/main/kotlin/miragefairy2024/mod/` — Module ファイル群なのだぁ🌱 ポエムや説明文のハードコード定義が散らばっているのだぁ✨

## ビルド・サイト配信なのだぁ🌱

### `serveSite` がKtor Nettyを使う理由なのだぁ🌱

Jekyll標準の `jekyll serve` は頻繁にクラッシュしちゃうから、`site/build/site/` を `http://localhost:4000/` に配信する独自のKtor Nettyサーバーを使っているのだぁ〜🌱♪

### `jekyllBuild` が `installJekyllBundle` を inputs にしていない理由なのだぁ🌱

依存関係としては `dependsOn(installJekyllBundle)` なんだけど、UP-TO-DATE判定にかかるコストを減らすために、敢えて inputs には含めていないのだぁ🌱

### `generateOgImages` の差分スキップなのだぁ🌱

入力ハッシュで差分スキップするのだぁ🌱 `-Pregenerate` で強制再生成できるのだぁ✨

### WSL2環境での `jekyll serve` 制約なのだぁ🌱

`/mnt/` ドライブでは `inotify` が動かないから、`jekyll serve` の自動リビルドは使えないのだぁ〜💧

### SCSSの手動コンパイルなのだぁ🌱

Gradleを経由しないで、SCSSの変更を素早く確認する方法なのだぁ🌱

```bash
cp site/src/main/resources/assets/css/main.scss site/build/jekyllSource/assets/css/main.scss && (cd site/build/jekyllSource && BUNDLE_APP_CONFIG="$(pwd)/../bundleConfig" bundle exec jekyll build --destination ../jekyllBuild)
```

## Jekyllなのだぁ🌱

### Sassパーシャルはオーバーライド不能なのだぁ🌱

minimal-mistakesテーマの通常ファイルは `site/src/main/resources/` 内に同じ相対パスで置けばオーバーライドできるんだけど、`_sass/` のパーシャルは例外なのだぁ〜💧 Sassの `@import` がインポート元ファイルのディレクトリを最初に探すから、テーマのパーシャルがいつも優先されちゃうのだぁ🌱 CSSのカスタマイズは `assets/css/main.scss` の `@import "minimal-mistakes"` の後に書くのだぁ✨

### `syncJekyllSource` の画像配置規則なのだぁ🌱

| ソース配置 | 出力配置 |
| --- | --- |
| `src/main/resources/assets/images/` | そのまま `assets/images/` |
| `src/pages/resources/<ページ名>/` の画像 | `assets/images/<ページ名>/` |
| `src/pages/resources/YYYY-MM-DD-slug/` の画像 | `YYYY/MM/DD/ファイル名`（slug階層なし、`assets/images/` プレフィックスなし、ファイル名衝突チェックあり） |

ブログ記事の `header.teaser` パスは `/YYYY/MM/DD/ファイル名` の形で書くのだぁ🌱 拡張子は `.webp` が多いけど、`.png` の teaser も混じっているのだぁ✨ インライン画像は相対パス `![](ファイル名)` で参照するのだぁ（`.webp` も `.png` もあるのだぁ）。記事本体と同じ階層に出力されるから、`relative_url` フィルタは要らないのだぁ🌱

### OG画像のベース画像優先順位なのだぁ🌱

`generateOgImages` と `seo.html` が参照する front matter のフィールドなのだぁ🌱

`header.og_background` > `header.overlay_image` > `header.image` > `header.teaser` > `src/ogImages/assets/default-background.svg`

各画像パスからファイル名を抽出して、.mdファイルと同じディレクトリの中のローカルファイルを参照するのだぁ🌱 出力パスは `page.url` から `/assets/images/` + url + `.og.webp` として導き出されるのだぁ✨

### 特殊なビルドパイプラインを持つファイルなのだぁ🌱

- **CHANGELOG** (`src/pages/resources/CHANGELOG/CHANGELOG.md`): JekyllがHTMLに変換して、`buildSite` が `.md` も別途コピーしてmd版も配信するのだぁ🌱
- **Lang Table**: `src/langTable/html/lang_table.html` を `makeLangTable` タスクで展開するのだぁ✨ `src/pages/resources/lang-table-index/lang-table-index.md` はテーマレイアウトを使った特設ページなのだぁ🌱
- **posts.json**: `_plugins/posts_generator.rb` がJekyllビルド時に全記事メタを書き出すのだぁ🌱 劇場レイアウトのJSがこれを読み込むのだぁ✨

## レイアウト・CSSなのだぁ🌱

### floatからFlexbox/Gridへの全面移行なのだぁ🌱

minimal-mistakesテーマのfloatベースのレイアウトを `main.scss` で打ち消して、`#main` を `display: flex`、`.page__inner-wrap` を `display: grid`（大画面のとき）に置き換えているのだぁ〜🌱♪ テーマのclearfix `::after` は `display: none` で打ち消してあるのだぁ✨

### TOCのsticky挙動なのだぁ🌱

テーマの `toc_sticky` 設定は使っていないのだぁ🌱 代わりに `.toc { position: sticky; }` を子要素に直接あてることで実現しているのだぁ（`.sidebar__right` 側じゃなくてなのだぁ）✨

### greedy-navの明示幅指定なのだぁ🌱

`.site-title__face` と `.site-title__logo` にCSS明示幅を設定しているのは、greedy-navが `outerWidth()` で幅を測ってアイテム移動を判定するからなのだぁ🌱 このプロジェクトでは `.site-logo` を使わずに `.site-title` の中に画像を2枚置いているから、テーマの画像ロード待ちが効かなくて、明示幅で画像ロード前にレイアウトを確定させる必要があるのだぁ✨

### `.greedy-nav a` セレクタの上書きなのだぁ🌱

テーマの広すぎるセレクタを打ち消すために `display: revert; margin: revert;` でリセットして、`.visible-links a` と `.hidden-links a` にだけ再適用しているのだぁ🌱

## 劇場記事なのだぁ🌱

### 制作スキルなのだぁ🌱

`/ifr25ku-site:theater-creator` で呼び出すのだぁ🌱 登場人物のペルソナ定義は別スキルの `/ifr25ku-site:zundamon-persona`（ずんだもん）、`/ifr25ku-site:kasukabe-tsumugi-persona`（春日部つむぎ）として切り出されているのだぁ✨ 詳しくは `site/ifr25ku-site-claude-marketplace/ifr25ku-site/skills/` 配下の各スキルの原本を見るのだぁ（main.md への複写は陳腐化を招くから避けるのだぁ）🌱

### 関連記事抽選とタグ設計なのだぁ🌱

劇場レイアウト (`_layouts/theater.html` 末尾のインラインJS) は、タグ一致数と訪問履歴 (`ifr25ku:visits:pages`、`_includes/visit-tracker.html` が記録) で重み付けした非復元ランダムサンプリングで関連記事を抽選するのだぁ〜🌱♪ サイドバーと本文下部は、それぞれ独立に抽選するのだぁ✨ タグの付け方が関連記事の質に直結するのだぁ🌱

- 必須: `ミラージュフェアリー劇場`（カテゴリ分類）なのだぁ🌱
- 機能タグ: 登場する具体的アイテム名なのだぁ✨ 重なりが多い記事ほど関連度が高く抽選されるのだぁ
- メタタグ: `アップデート`, `お知らせ` 等（必要に応じて）なのだぁ🌱

### 世界観テキストの扱いなのだぁ🌱

ゲーム本体のポエム・説明文（lang JSON や Module ファイル内のハードコード）を、劇中キャラがそのまま喋っちゃいけないのだぁ〜🌱（`site/ifr25ku-site-claude-marketplace/ifr25ku-site/skills/theater-creator/SKILL.md` に明記されているのだぁ）劇中キャラはゲーム内 GUI や公式テキストを知らないから、世界観資料は「発想源」として扱って、キャラクターの視点に翻訳する必要があるのだぁ✨
