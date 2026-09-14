# リポジトリの俯瞰と暗黙知なのだ～🌱✨

このファイルは、ぴょこ（Claude Fairy）が自由に編集できる、コミットされる永続的なメモ帳なのだ～🌱♪
ソースコード上のドキュメントやコメントからは読み取れない暗黙知と、リポジトリのファイル構造の俯瞰を、ここにためこんでいくのだぁ✨

## ファイル構造なのだ～🌱

### サイト関連 (`site/`) なのだ～🌱

- `build.gradle.kts` — Jekyllビルド・OG画像生成・サイト配信のタスク定義なのだ～🌱
- `src/main/resources/` — Jekyllのテーマオーバーライドの配置先なのだ～🌱（`_layouts/`, `_includes/`, `_plugins/`, `_data/`, `_config.yml`, `assets/`）
- `src/main/bundle/` — Gemfile等なのだ～🌱
- `src/pages/resources/<name>/<name>.md` — 全ページ・記事のソースなのだぁ✨ `syncJekyllSource` がJekyll形式に変換するのだ～🌱
- `src/external/resources/` — 外部リソースなのだ～🌱
- `src/ogImages/` — OG画像生成のソースとデフォルト背景SVGなのだ～🌱 生成された画像は `build/ogImages/` に出るのだぁ✨
- `src/langTable/html/lang_table.html` — `<%= trs %>` を `makeLangTable` タスクで展開するテンプレートなのだぁ✨
- `scripts/` — `bundle-install.sh`, `build-site.sh`, `convert-to-webp.sh` に加えて、`serveSite` の本体になる `serve-site.sh` と `serve-site.main.kts`（Ktor Netty サーバー）もここなのだ～🌱
- `build/site/` — CIの最終出力先なのだ～🌱
- `build/bundleVendor/` — minimal-mistakesテーマを含む gem のインストール先なのだ～🌱

### 世界観テキスト (`common/`) なのだ～🌱

劇場記事を書くときの発想源として参照するところなのだ～🌱♪

- `src/generated/resources/assets/miragefairy2024/lang/ja_jp.json` — 全テキストの集約先で、コードから自動生成されるのだ～🌱 アイテム名・ブロック名は `item.miragefairy2024.<item>` / `block.miragefairy2024.<block>` みたいに、`.name` を付けないキーそのものに値が入っているのだぁ✨ ポエムや説明文は `item.miragefairy2024.<item>.poem` / `.description`、進捗は `advancements.miragefairy2024.<item>.title` / `.description` みたいなキーパターンなのだ～🌱
- `src/main/kotlin/miragefairy2024/mod/` — Module ファイル群なのだ～🌱 ポエムや説明文のハードコード定義が散らばっているのだぁ✨

## ビルド・サイト配信なのだ～🌱

### 足りないものの置き場所は `~/.claude_tmp/` の直下なのだ～🌱

ぴょこの環境に足りないものを `sudo` 無しで用意する手順では、用意したものを、必ず `~/.claude_tmp/` の **直下** に置くのだ～🌱 ここは複数のセッションで共用される場所だから、次のセッションのぴょこも、そのまま使い回せるのだぁ✨

`~/.claude_tmp/<セッションID>/` の方は、セッション専用の作業ディレクトリなのだ～🌱 一時的なファイルはそっちに作るのが原則なんだけど、ここで用意するものは、その原則の対象外なのだ～🌱 セッション専用の方に置いちゃうと、次のぴょこから見ると他人の作業ディレクトリになって、読むことはできても触っちゃだめな場所になるから、結局は入れ直しになっちゃうのだぁ…🌧️

### ぴょこの環境で `bundle` を用意するのだ～🌱

サイトのビルドには `bundle` が要るんだけど、ぴょこの環境には Ruby と `gem` しか入っていないのだぁ…🌧️ しかも `/var/lib/gems/` にも `/usr/local/bin/` にも書き込めないから、`gem install bundler` は `--user-install` を付けないと通らないのだ～🌱

そのうえ、`~/.local/` はセッションが終わるとまっさらに戻るから、毎回入れ直しになっちゃうのだぁ…🌧️ そこで、実体を `~/.claude_tmp/` の直下に置いて、`gem` が見に行く場所からシンボリックリンクで飛ばすのだぁ✨ こうすると、2回目からは `gem install` そのものが要らなくなるのだ～🌱

```bash
mkdir -p ~/.claude_tmp/shared/gem/ruby/3.1.0 ~/.local/share/gem/ruby
ln -sfn ~/.claude_tmp/shared/gem/ruby/3.1.0 ~/.local/share/gem/ruby/3.1.0
export PATH="$HOME/.local/share/gem/ruby/3.1.0/bin:$PATH"
command -v bundle > /dev/null || gem install bundler --user-install
```

パスの `3.1.0` は Ruby のマイナーバージョンなのだ～🌱 `gem env` の `USER INSTALLATION DIRECTORY` が、そのときの正解を教えてくれるから、食い違ったらそっちに合わせるのだぁ✨

Gemfile のほうの gem は、`site/build/bundleVendor` に入るのだ～🌱 `.gitignore` の `build/` に該当するから、こっちはセッションをまたいで残るのだぁ✨ だから毎回用意しなきゃいけないのは `bundle` の1本だけなのだ～🌱

ネイティブ拡張を持つ gem もいくつか混じっているんだけど、gcc と make は最初から入っているから、apt は1つも要らないのだぁ✨ そもそも `/var/lib/apt/lists/` が空だから、apt を使うなら `apt-get update` からになるのだぁ…🌧️

なお、`bundle` を手で叩くときは、必ず `site/scripts/bundle-install.sh`（Gradleなら `:site:installJekyllBundle`）を経由するのだ～🌱 このスクリプトが `BUNDLE_APP_CONFIG` を `site/build/bundleConfig` に逃がしてくれるから、リポジトリの中に `.bundle/config` を作らずに済むのだぁ✨

### ぴょこの環境で Chromium を動かすのだ～🌱

OG画像の生成タスクは、Playwright 経由で Chromium を立ち上げるのだ～🌱 でも、ぴょこの環境で素のまま走らせると、共有ライブラリが足りなくて起動に失敗するのだぁ…🌧️ しかも `sudo` が無いから、`apt-get install` も `playwright install-deps` も通らないのだぁ…🌧️

回避の要は3つなのだ～🌱 apt のステートをユーザー領域へ逃がすこと、`.deb` を `dpkg-deb -x` で展開して `LD_LIBRARY_PATH` に載せること、そして日本語フォントを自前で登録することなのだぁ✨ 展開先を `~/.claude_tmp/` の直下に置けば、2回目以降はダウンロードごと省けるのだ～🌱

```bash
ROOT=~/.claude_tmp/aptroot
DEST=~/.claude_tmp/chrome-libs
APT_OPTS=(-o "Dir::State=$ROOT/var/lib/apt" -o "Dir::State::status=$ROOT/var/lib/dpkg/status" -o "Dir::Cache=$ROOT/var/cache/apt" -o "Dir::Etc::preferences.d=$ROOT/etc/apt/preferences.d")

# apt を sudo 無しで使うための下ごしらえなのだ～🌱
mkdir -p "$ROOT"/var/lib/apt/lists/partial "$ROOT"/var/cache/apt/archives/partial "$ROOT"/etc/apt/preferences.d "$ROOT"/var/lib/dpkg
touch "$ROOT/var/lib/dpkg/status"
apt-get "${APT_OPTS[@]}" update

# Chromium が要求する共有ライブラリなのだ～🌱
mkdir -p "$DEST/flat" ~/.claude_tmp/chrome-libs-work && cd ~/.claude_tmp/chrome-libs-work
apt-get "${APT_OPTS[@]}" download libnspr4 libnss3 libxcomposite1 libxdamage1 libxfixes3 libxrandr2 libatk1.0-0 libatk-bridge2.0-0 libatspi2.0-0 libdbus-1-3 libgbm1 libxkbcommon0 libdrm2 libwayland-server0
for deb in *.deb; do dpkg-deb -x "$deb" "$DEST"; done
find "$DEST" -path "$DEST/flat" -prune -o \( -name '*.so*' -type f -print \) | while read f; do cp -n "$f" "$DEST/flat/"; done

# 日本語フォントなのだ～🌱
apt-get "${APT_OPTS[@]}" download fonts-noto-cjk
dpkg-deb -x fonts-noto-cjk*.deb ~/.claude_tmp/fonts-extract
mkdir -p ~/.local/share/fonts
find ~/.claude_tmp/fonts-extract \( -name '*.otf' -o -name '*.ttc' -o -name '*.ttf' \) | while read f; do ln -sf "$f" ~/.local/share/fonts/; done
fc-cache -f

# 走らせるときは、展開したライブラリの場所を教えてあげるのだ～🌱
export LD_LIBRARY_PATH="$DEST/flat"
```

`dpkg-deb` が展開するパスは `usr/lib/x86_64-linux-gnu/` みたいに深いところに散らばるから、`flat/` へ集めて `LD_LIBRARY_PATH` を1本にまとめているのだ～🌱 パッケージの一覧は、`playwright install-deps chromium` が入れようとするもののうち、この環境に足りていなかった分なのだぁ✨

ブラウザ本体は入れなくていいのだ～🌱 `generateOgImages` が依存する `installPlaywrightBrowsers` タスクが、`~/.cache/ms-playwright/` へ調達してくれるのだぁ✨ だから `playwright install` を手で叩く必要は無いのだ～🌱 `PLAYWRIGHT_BROWSERS_PATH` を指定すれば、置き場所を移すこともできるのだ～🌱

日本語フォントを飛ばすと、生成された画像の日本語が全部豆腐になっちゃうのだぁ…🌧️ OG画像を組み立てる HTML が `font-family` で `Noto Sans CJK JP` を名指ししているから、`fonts-ipafont-gothic` みたいな別の和文フォントで代用すると、今までの画像と見た目が変わっちゃうのだ～🌱

`~/.local/` はセッションをまたぐと消えるけど、`~/.claude_tmp/` と `~/.cache/` は残るのだ～🌱 だから2回目以降に要るのは、フォントのシンボリックリンクの張り直しと `fc-cache` と、`LD_LIBRARY_PATH` の設定だけなのだぁ✨

### `serveSite` がKtor Nettyを使う理由なのだ～🌱

Jekyll標準の `jekyll serve` は頻繁にクラッシュしちゃうから、`site/build/site/` を `http://localhost:4000/` に配信する独自のKtor Nettyサーバーを使っているのだ～🌱♪

### `jekyllBuild` が `installJekyllBundle` を inputs にしていない理由なのだ～🌱

依存関係としては `dependsOn(installJekyllBundle)` なんだけど、UP-TO-DATE判定にかかるコストを減らすために、敢えて inputs には含めていないのだ～🌱

### `generateOgImages` の差分スキップなのだ～🌱

入力ハッシュで差分スキップするのだ～🌱 `-Pregenerate` で強制再生成できるのだぁ✨

### WSL2環境での `jekyll serve` 制約なのだ～🌱

`/mnt/` ドライブでは `inotify` が動かないから、`jekyll serve` の自動リビルドは使えないのだぁ…🌧️

### SCSSの手動コンパイルなのだ～🌱

Gradleを経由しないで、SCSSの変更を素早く確認する方法なのだ～🌱

```bash
cp site/src/main/resources/assets/css/main.scss site/build/jekyllSource/assets/css/main.scss && (cd site/build/jekyllSource && BUNDLE_APP_CONFIG="$(pwd)/../bundleConfig" bundle exec jekyll build --destination ../jekyllBuild)
```

## Jekyllなのだ～🌱

### Sassパーシャルはオーバーライド不能なのだ～🌱

minimal-mistakesテーマの通常ファイルは `site/src/main/resources/` 内に同じ相対パスで置けばオーバーライドできるんだけど、`_sass/` のパーシャルは例外なのだぁ…🌧️ Sassの `@import` がインポート元ファイルのディレクトリを最初に探すから、テーマのパーシャルがいつも優先されちゃうのだ～🌱 CSSのカスタマイズは `assets/css/main.scss` の `@import "minimal-mistakes"` の後に書くのだぁ✨

### `syncJekyllSource` の画像配置規則なのだ～🌱

| ソース配置 | 出力配置 |
| --- | --- |
| `src/main/resources/assets/images/` | そのまま `assets/images/` |
| `src/pages/resources/<ページ名>/` の画像 | `assets/images/<ページ名>/` |
| `src/pages/resources/YYYY-MM-DD-slug/` の画像 | `YYYY/MM/DD/ファイル名`（slug階層なし、`assets/images/` プレフィックスなし、ファイル名衝突チェックあり） |

ブログ記事の `header.teaser` パスは `/YYYY/MM/DD/ファイル名` の形で書くのだ～🌱 拡張子は `.webp` が多いけど、`.png` の teaser も混じっているのだぁ✨ インライン画像は相対パス `![](ファイル名)` で参照するのだ～🌱（`.webp` も `.png` もあるのだ～🌱）。記事本体と同じ階層に出力されるから、`relative_url` フィルタは要らないのだ～🌱

### OG画像のベース画像優先順位なのだ～🌱

`generateOgImages` と `seo.html` が参照する front matter のフィールドなのだ～🌱

`header.og_background` > `header.overlay_image` > `header.image` > `header.teaser` > `src/ogImages/assets/default-background.svg`

各画像パスからファイル名を抽出して、.mdファイルと同じディレクトリの中のローカルファイルを参照するのだ～🌱 出力パスは `page.url` から `/assets/images/` + url + `.og.webp` として導き出されるのだぁ✨

### 特殊なビルドパイプラインを持つファイルなのだ～🌱

- **CHANGELOG** (`src/pages/resources/CHANGELOG/CHANGELOG.md`): JekyllがHTMLに変換して、`buildSite` が `.md` も別途コピーしてmd版も配信するのだ～🌱
- **Lang Table**: `src/langTable/html/lang_table.html` を `makeLangTable` タスクで展開するのだぁ✨ `src/pages/resources/lang-table-index/lang-table-index.md` はテーマレイアウトを使った特設ページなのだ～🌱
- **posts.json**: `_plugins/posts_generator.rb` がJekyllビルド時に全記事メタを書き出すのだ～🌱 劇場レイアウトのJSがこれを読み込むのだぁ✨

## レイアウト・CSSなのだ～🌱

### floatからFlexbox/Gridへの全面移行なのだ～🌱

minimal-mistakesテーマのfloatベースのレイアウトを `main.scss` で打ち消して、`#main` を `display: flex`、`.page__inner-wrap` を `display: grid`（大画面のとき）に置き換えているのだ～🌱♪ テーマのclearfix `::after` は `display: none` で打ち消してあるのだぁ✨

### TOCのsticky挙動なのだ～🌱

テーマの `toc_sticky` 設定は使っていないのだ～🌱 代わりに `.toc { position: sticky; }` を子要素に直接あてることで実現しているのだ～✨（`.sidebar__right` 側じゃなくてなのだ～🌱）

### greedy-navの明示幅指定なのだ～🌱

`.site-title__face` と `.site-title__logo` にCSS明示幅を設定しているのは、greedy-navが `outerWidth()` で幅を測ってアイテム移動を判定するからなのだ～🌱 このプロジェクトでは `.site-logo` を使わずに `.site-title` の中に画像を2枚置いているから、テーマの画像ロード待ちが効かなくて、明示幅で画像ロード前にレイアウトを確定させる必要があるのだぁ✨

### `.greedy-nav a` セレクタの上書きなのだ～🌱

テーマの広すぎるセレクタを打ち消すために `display: revert; margin: revert;` でリセットして、`.visible-links a` と `.hidden-links a` にだけ再適用しているのだ～🌱

## 劇場記事なのだ～🌱

### 制作スキルなのだ～🌱

`/theater-creator` で呼び出すのだ～🌱 本体は `.claude/skills/theater-creator/` なのだぁ✨ 登場人物のペルソナ定義は `MirrgieRiana/MirrgieRiana.github.io` の `.claude/skills/` にある `zundamon-persona`（ずんだもん）、`kasukabe-tsumugi-persona`（春日部つむぎ）で、このリポジトリには置かれていないのだ～🌱 詳しくは各スキルの原本を見るのだ～🌱（main.md へのコピーは陳腐化を招くから避けるのだ～🌱）

### 関連記事抽選とタグ設計なのだ～🌱

劇場レイアウト (`_layouts/theater.html` 末尾のインラインJS) は、タグ一致数と訪問履歴 (`ifr25ku:visits:pages`、`_includes/visit-tracker.html` が記録) で重み付けした非復元ランダムサンプリングで関連記事を抽選するのだ～🌱♪ サイドバーと本文下部は、それぞれ独立に抽選するのだぁ✨ タグの付け方が関連記事の質に直結するのだ～🌱

- 必須: `ミラージュフェアリー劇場`（カテゴリ分類）なのだ～🌱
- 機能タグ: 登場する具体的アイテム名なのだぁ✨ 重なりが多い記事ほど関連度が高く抽選されるのだ～🌱
- メタタグ: `アップデート`, `お知らせ` 等（必要に応じて）なのだ～🌱

### 世界観テキストの扱いなのだ～🌱

ゲーム本体のポエム・説明文（lang JSON や Module ファイル内のハードコード）を、劇中キャラがそのまま喋っちゃいけないのだ～🌱（`.claude/skills/theater-creator/SKILL.md` に明記されているのだ～🌱）劇中キャラはゲーム内 GUI や公式テキストを知らないから、世界観資料は「発想源」として扱って、キャラクターの視点に翻訳する必要があるのだぁ✨
