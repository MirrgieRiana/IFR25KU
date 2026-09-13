# IFR25KU 動画ビルドシステム

VOICEVOX の立ち絵（ずんだもん・春日部つむぎ）が口パク・まばたき・ポーズ変化をしながら会話する、
IFR25KU の解説寸劇動画を、**台本テキストから 1 コマンドで組み立てる**ためのビルドシステムです。

現在の題材は「サラセニア（食虫植物）」の寸劇（1 分ちょっと）ですが、台本（`script.json`）と
シーン定義（`scene.html`）を差し替えれば、別の寸劇にも使えます。

---

## 1. 3 つのパートと、仕組みの全体像

`video/` は、役割ごとに 3 つのパートに分かれています。

1. **`renderer/`（汎用レンダラー）** … 「構成jsonl（1 行 = 1 フレーム）と HTML テンプレートを受け取って、ひたすら対応する画像を撮る」だけの、**動画の中身を知らない**カプセル化されたレンダラーです。単体で完結していて、他のパートに依存しません。
2. **`sarracenia/`（動画タイトルのディレクトリ）** … この寸劇動画の台本・シーン・**構成jsonl を作る**パートです。単体で完結していて、他のパートに依存しません。
3. **`video/` 直下（雑多な部分）** … 音声合成・動画合成などを行い、**1 と 2 を呼び出して**動画を完成させます（`build.sh` がその配線役です）。

この動画は、動画編集ソフトの GUI で作るのではなく、**「決定論的フレームレンダリング」** という方式で作ります。

1. `sarracenia/scene.html` が「1 フレーム分の構成 `cfg` を渡すと、その画面を組み立てて再利用キーを返す」関数 `window.applyFrame(cfg)` を持ちます。今のこの動画では `cfg = {t: 秒}` で、内部の `seek(t)` が時刻から全要素を組み立てます。
2. `sarracenia/compose.py` が、30fps 刻みの構成jsonl（`frames.jsonl`、1 行 = 1 フレーム）を作ります。
3. `renderer/render.js` がヘッドレス Chromium に `scene.html` を開かせ、`frames.jsonl` を頭から 1 行ずつ `applyFrame(cfg)` に渡し、1 行につき 1 コマ撮ります。
4. 撮れた連番画像（`sarracenia/frames/f_00000.png …`）を ffmpeg で映像にし、ナレーション音声と BGM を重ねて mp4 にします。

絵は構成jsonl だけで決まるので、マシンの速さに関係なく尺が正確で、何度ビルドしても同じ結果になります。
連続する同一コマ（再利用キーが同じ行）は撮り直さず前のコマを使い回すので、変化の少ない場面は速く進みます。

### データの流れ

```
sarracenia/script.json（台本）
   │
   ├─(synth.py + VOICEVOX)→ audio/line*.wav, moras.json, kana.json, durations.json   ┐ 雑多パート
   │                                                                                   │ (video/ 直下)
   └─(assemble.py)────────→ full.wav（ナレーション全体）, timeline.json（尺・区間） ┘

timeline.json ─┬─(sarracenia/bake_assets.py)──→ sarracenia/assets.js   ┐
               │        ↑ IFR25KU テクスチャ / resources/font,emoji     │ sarracenia
 resources/psd/*.psd ─(sarracenia/extract_tachie.js)→ sarracenia/tachie/│ （構成を作る）
               └─(sarracenia/compose.py)─────→ sarracenia/frames.jsonl  ┘

sarracenia/scene.html + assets.js + tachie/ + frames.jsonl
   └─(renderer/render.js + Chromium)→ sarracenia/frames/f_%05d.png   … 汎用レンダラー

sarracenia/frames/ + full.wav + sarracenia/resources/bgm/*.flac
   └─(build_video.py + ffmpeg)→ sarracenia.mp4（完成品）              … 雑多パート
```

各パートの詳しい説明は、それぞれの README にもあります（[`renderer/README.md`](renderer/README.md) ／ [`sarracenia/README.md`](sarracenia/README.md)）。

---

## 2. ディレクトリとファイル

### コミットされているもの（このビルドシステム本体）

**`video/` 直下（雑多な部分・1 と 2 を呼び出すパート）**

| ファイル | 役割 |
| --- | --- |
| `build.sh` | 下記すべてを順に呼び出すオーケストレーター。**通常はこれを実行するだけ**です。 |
| `synth.py` | VOICEVOX で台詞ごとの音声を合成し、口パク用のモーラ区間も書き出します。 |
| `assemble.py` | 台詞 wav を「タイトル→本編（行間の無音）→クレジット」の順に結合し、タイムラインを算出します。 |
| `build_video.py` | 連番フレーム＋ナレーション＋BGM を ffmpeg で合成して mp4 にします。 |

**`renderer/`（汎用レンダラー・自己完結）**

| ファイル | 役割 |
| --- | --- |
| `render.js` | 構成jsonl と HTML テンプレートを受け取り、1 行 = 1 フレームで Chromium から画像を撮ります。動画の中身を知りません。 |
| `setup_chromium.js` | `@sparticuz/chromium`（同梱 Chromium）を `/tmp/chromium` に展開します（Chrome が無い環境向け）。 |
| `package.json` / `package-lock.json` | Node の依存関係（`puppeteer-core` / `@sparticuz/chromium`）。 |

**`sarracenia/`（動画タイトルのディレクトリ・構成jsonl を作るパート・自己完結）**

| ファイル | 役割 |
| --- | --- |
| `build_scene.sh` | タイムラインを受け取り、`assets.js`・`tachie/`・`frames.jsonl` を作る、このパートの入口です。 |
| `script.json` | 台本。台詞・話者・読み（カナ原稿）・字幕・シーン・登場アイテムを定義します。 |
| `scene.html` | 画面の見た目と `window.applyFrame(cfg)`（構成→画面）の本体。字幕・立ち絵・背景・クレジット・サムネを組み立てます。 |
| `compose.py` | タイムラインから、1 行 = 1 フレームの構成jsonl（`frames.jsonl`）を作ります。 |
| `bake_assets.py` | `scene.html` が読む `assets.js`（フォント・絵文字・テクスチャ・タイムラインの束）を生成します。 |
| `extract_tachie.js` | 立ち絵 PSD から、必要なレイヤーだけを透過 PNG として切り出します。 |
| `package.json` / `package-lock.json` | Node の依存関係（`ag-psd` / `pngjs`）。 |
| `resources/**/*.md5` | 外部取得リソースの md5。**「そこに、どの名前で、何を置けばよいか」を保証するための目印**です。 |

### コミットされていないもの（`.gitignore` 対象）

- **外部取得リソースの実体**（`sarracenia/resources/psd/*.psd`, `.../font/*.ttf`, `.../emoji/*.svg`, `.../bgm/*.flac`）
  … 著作権の都合でコミットしません。各 `.md5` を頼りに自分で配置してください（→ [3.](#3-用意する外部取得リソース)）。
- **音声の中間生成物**（`video/` 直下の `audio/`, `full.wav`, `timeline.json`, `kana.json`, `moras.json`, `durations.json`）
- **scene の中間生成物**（`sarracenia/assets.js`, `sarracenia/tachie/`, `sarracenia/frames.jsonl`）
- **出力**（`sarracenia/frames/`, `*.mp4`, `*.png`）
- **依存**（各パート配下の `node_modules/`）

---

## 3. 用意する外部取得リソース

以下を `sarracenia/resources/` の所定パスに置いてください。ファイル名と中身は、同じ場所にある `*.md5` と一致している必要があります。
（`cd sarracenia/resources/<dir> && md5sum -c <name>.md5` で照合できます。）

| 置き場所 | 中身 | 入手先 | ライセンス |
| --- | --- | --- | --- |
| `sarracenia/resources/psd/zundamon23.psd` | ずんだもん立ち絵素材 2.3（PSD） | 坂本アヒル 氏配布の立ち絵素材 | 良識の範囲で利用可・改変可（同梱 readme 参照） |
| `sarracenia/resources/psd/tsumugi3.psd` | 春日部つむぎ立ち絵素材 3.0（PSD） | 坂本アヒル 氏配布の立ち絵素材 | 同上（`tsumugi-official.studio.site/rule` の規約に準拠） |
| `sarracenia/resources/font/ZenMaruGothic-Black.ttf` | Zen Maru Gothic Black | Google Fonts「Zen Maru Gothic」 | SIL Open Font License 1.1 |
| `sarracenia/resources/font/ZenMaruGothic-Bold.ttf` | Zen Maru Gothic Bold | 同上 | SIL Open Font License 1.1 |
| `sarracenia/resources/emoji/seedling.svg` | 🌱（seedling）のカラー SVG | Microsoft「Fluent Emoji」 | MIT License |
| `sarracenia/resources/bgm/chopin_op10-4.flac` | ショパン 練習曲 作品10-4「Torrent（激流）」（Edward Neeman 演奏） | Wikimedia Commons / Musopen "Set Chopin Free" | パブリックドメイン |

> 立ち絵 PSD の ID 体系について：`sarracenia/extract_tachie.js` が切り出すレイヤーは、兄弟レイヤーの 1 始まりインデックスを
> `-` で連結した ID で指定します（グループもインデックスを 1 個消費します）。切り出す ID の一覧は `sarracenia/build_scene.sh` の
> `ZUNDA_IDS` / `TSUMUGI_IDS` にあり、これは `scene.html` の `TACHIE` 定義と一致している必要があります。

### IFR25KU リポジトリ由来のテクスチャ（配置不要）

次の 4 枚は IFR25KU リポジトリにコミット済みなので、`sarracenia/bake_assets.py` がリポジトリから直接読みます（自分で置く必要はありません）。

| 用途 | ファイル |
| --- | --- |
| アイテム枠（サラセニア本体） | `common/.../textures/block/magic_plant/sarracenia_age3.png` |
| アイテム枠（サラセニアの葉） | `common/.../textures/item/sarracenia_leaf.png` |
| サムネ左上ロゴ | `common/.../miragefairy2024/icon.png` |
| 背景タイル | `common/.../textures/block/haimeviska_log.png` |

---

## 4. 前提ツール

- **Node.js**（18 以降を想定）と **npm**
- **Python 3**（標準ライブラリのみ使用。追加パッケージ不要）
- **ffmpeg**
- **VOICEVOX ENGINE**（音声合成サーバー。起動しておく）
- **Chrome / Chromium**（無ければ `renderer/setup_chromium.js` が同梱版を展開）

---

## 5. ビルド手順

```sh
# 1) VOICEVOX ENGINE を起動しておく（別ターミナルなど）
#    既定では http://127.0.0.1:50021 を使います

# 2) 外部取得リソースを sarracenia/resources/ に配置する（→ 3.）

# 3) ビルド（Node 依存は build.sh が各パートで自動 npm install します）
bash build.sh
```

完成すると `video/sarracenia.mp4` ができます。中間生成物・連番フレームも `video/` 内に残りますが、すべて `.gitignore` 済みです。

（`renderer/` と `sarracenia/` は Node 依存が別々なので、`build.sh` はそれぞれのディレクトリで必要なときだけ `npm install` します。手動で入れるなら各ディレクトリで `npm install` してください。）

### 環境変数で差し替えられる設定

`build.sh` は次の環境変数を見ます（無指定なら既定値）。

| 変数 | 既定 | 説明 |
| --- | --- | --- |
| `VOICEVOX_HOST` | `http://127.0.0.1:50021` | VOICEVOX ENGINE の URL |
| `CHROMIUM_PATH` | `/tmp/chromium` | Chrome/Chromium の実行ファイル。手元の Chrome を使うならそのパスを指定 |
| `CHROMIUM_LD_PATH` | （なし） | render 時に `LD_LIBRARY_PATH` へ前置するパス。共有ライブラリを補う必要のある環境向け |
| `FFMPEG` | `ffmpeg` | ffmpeg 実行ファイル |
| `BGM_VOL` | `0.45` | BGM の基準音量（0〜1） |

---

## 6. 各ステップの詳細

### synth.py（音声合成）
台本の各台詞について、VOICEVOX の **カタカナ原稿（`is_kana`）記法** を使って読みを厳密に指定して合成します。
`script.json` の各行に `kana`（カナ原稿）があればそれを使い、無ければ `text` から自動生成した読みを使います。
口パク用に、各モーラ（音の粒）が wav のどこに位置するか（`moras.json`）も書き出します。

- カナ原稿の記法（VOICEVOX 標準）：アクセント核 `'`／アクセント句区切り `/`／小休止 `、`／語尾上げ `？`。
  長音は母音を重ねて書きます（例：サトウ→`サトオ`）。平板型は「核をアクセント単位の末尾に置く」ことで表します。

### assemble.py（結合・タイムライン）
台詞 wav を「タイトル保持 → 本編（台詞のあいだに無音の“間”）→ クレジット保持」の順に結合し、
`full.wav` と `timeline.json` を作ります。`timeline.json` には、各台詞の開始・終了時刻、シーン区間、
アイテムの表示区間などが入り、`sarracenia/`（`bake_assets.py`・`compose.py`）と `build_video.py` がこれを読みます。

### bake_assets.py（アセットの焼き込み）
`file://` で開いた HTML は外部ファイルを `fetch()` できないため、フォント・絵文字・テクスチャ・タイムラインを
`assets.js` に埋め込みます。画像・フォントは base64 の data-URL に、絵文字 SVG は生の文字列として入ります。

### extract_tachie.js（立ち絵の切り出し）
`ag-psd` で PSD を読み、指定 ID のレイヤーを「全身キャンバスと同じサイズの透過 PNG」として書き出します。
`node-canvas` を入れずに済むよう、`createImageData` だけをシムして動かしています。

### sarracenia/compose.py（構成jsonl の組み立て）
`timeline.json` から総尺を読み、30fps 刻みの構成jsonl（`frames.jsonl`、1 行 = 1 フレーム）を作ります。
今のこの動画では、フレームは時刻だけで決まるので各行は `{"t": 秒}` です。将来は、この各行に「どの要素へ
どんな CSS・属性・テキストを入れるか」をフラットに書き込んで、`seek` の計算そのものを構成jsonl 側へ
追い出すこともできます（そのときレンダラー側は変えなくてよい設計です）。

### sarracenia/scene.html（画面と applyFrame）
画面の見た目のすべてと、構成 `cfg` から画面を決める `window.applyFrame(cfg)` が入っています。
`applyFrame({t})` は内部の `window.seek(t)`（時刻→画面）を呼び、その画面の見た目を一意に表す再利用キーを返します。
背景のシーン切り替え（沼地↔泥沼のクロスフェード）、アイテム枠、立ち絵（口パク・まばたき・眉/腕/汗のポーズ）、
字幕（話者色の縁取り）、キャラ名、クレジット、そして開幕フレーム（＝サムネイル）のデカ字を組み立てます。

### renderer/render.js（フレーム撮影）
ヘッドレス Chromium でテンプレート（`scene.html`）を開き、フォント読み込み完了を待ってから、構成jsonl を
1 行ずつ `applyFrame(cfg)` に渡して 1 行につき 1 コマ撮ります。`applyFrame` の返す再利用キーが前のコマと
同じなら、撮り直さず前のコマを使い回します。動画の中身を知らない汎用レンダラーです。

### build_video.py（映像・音声の合成）
連番フレームを 30fps の映像に、`full.wav`（ナレーション）と BGM を重ねます。
BGM は「冒頭＝やや強め → 本編＝弱め → クレジット＝冒頭と同じ」と音量が線形補間で変化し、
ナレーションが常に上に立つようにしています。

---

## 7. クレジット（動画末尾に表示される帰属表記）

| 項目 | 名称（作者・権利者） | ライセンス |
| --- | --- | --- |
| 声 | VOICEVOX：ずんだもん ／ VOICEVOX：春日部つむぎ | （VOICEVOX 規約の指定表記） |
| 立ち絵 | ずんだもん立ち絵素材 ／ 春日部つむぎ立ち絵素材（坂本アヒル） | （クレジット任意） |
| 画像 | IFR25KU テクスチャ（© 2025 The Developer of MirageFairy, Generation 7 ／ Yoruno Kakera） | CC BY 4.0 |
| 音楽 | ショパン 練習曲 作品10 第4番（演奏 Edward Neeman） | パブリックドメイン |
| 絵文字 | Fluent Emoji（Microsoft） | MIT |
| フォント | Zen Maru Gothic（The Zen Maru Gothic Project Authors） | SIL OFL |
| 世界観 | MirageFairy（MirageFairy Server 運営） ／ IFRKU（夜のかけら） | （表現物の利用なし） |
| 制作 | Claude Code ／ Claude Fairy | |

> IFR25KU リポジトリのテクスチャのうち、README の「Resources Derived from MirageFairy2019」に挙がるもの（CC BY-SA 3.0）と、
> Minecraft 由来のもの（Mojang 著作権）は使っていません。使っているのは MOD オリジナル素材（「Other Resources」＝
> Apache-2.0 / CC BY 3.0 / CC BY 4.0 の選択制）だけです。

---

## 8. 補足：Chrome が無い・共有ライブラリが足りない環境で動かすには

手元に Chrome がある場合は `CHROMIUM_PATH=/path/to/chrome` を指定するのが一番簡単です。

Chrome を入れられない環境では、`renderer/package.json` に含まれる `@sparticuz/chromium`（同梱 Chromium）を
`node renderer/setup_chromium.js` で `/tmp/chromium` に展開して使えます。ふつうの Linux なら、これだけで動きます。

ごく限られた環境（root 権限が無く、NSS 系の共有ライブラリが OS に無いなど）では、この Chromium が
`libnss3.so` などを見つけられず起動に失敗することがあります。その NSS 一式は、`@sparticuz/chromium` パッケージが
同梱している `bin/al2023.tar.br`（Brotli 圧縮された tar）を展開すると得られます。取り出した `.so` を 1 つのディレクトリに
集め、`CHROMIUM_LD_PATH` にそのパスを指定してください（`build.sh` が render 時に `LD_LIBRARY_PATH` へ前置します）。
なお、この共有ライブラリの補完は上記のような特殊な環境でだけ必要で、Chrome のある環境では `CHROMIUM_PATH` を指すだけで済みます。
