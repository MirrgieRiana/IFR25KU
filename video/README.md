# IFR25KU 動画ビルドシステム

VOICEVOX の立ち絵（ずんだもん・春日部つむぎ）が口パク・まばたき・ポーズ変化をしながら会話する、
IFR25KU の解説寸劇動画を、**台本テキストから 1 コマンドで組み立てる**ためのビルドシステムです。

現在の題材は「サラセニア（食虫植物）」の寸劇（1 分ちょっと）ですが、台本（`script.json`）と
シーン定義（`scene.html`）を差し替えれば、別の寸劇にも使えます。

---

## 1. 仕組みの全体像

この動画は、動画編集ソフトの GUI で作るのではなく、**「決定論的フレームレンダリング」** という方式で作ります。

1. 1 枚の HTML（`scene.html`）が「時刻 `t` を渡すと、その瞬間の画面を組み立てる」純粋な関数 `window.seek(t)` を持ちます。
2. ヘッドレス Chromium にその HTML を開かせ、`t = 0/30, 1/30, 2/30, …` と 30fps 刻みで `seek(t)` を呼び、1 コマずつスクリーンショットを撮ります。
3. 撮れた連番画像（`frames/f_00000.png …`）を ffmpeg で映像にし、ナレーション音声と BGM を重ねて mp4 にします。

絵は時刻だけで決まるので、マシンの速さに関係なく尺が正確で、何度ビルドしても同じ結果になります。
連続する同一コマは撮り直さず前のコマを使い回すので、変化の少ない場面は速く進みます。

### データの流れ

```
script.json（台本）
   │
   ├─(synth.py + VOICEVOX)→ audio/line*.wav, moras.json, kana.json, durations.json
   │
   ├─(assemble.py)────────→ full.wav（ナレーション全体）, timeline.json（尺・台詞区間・シーン・アイテム）
   │
   ├─(bake_assets.py)─────→ assets.js（フォント・絵文字・テクスチャ・timeline を焼き込んだ束）
   │       ↑ IFR25KU テクスチャ / resources/font / resources/emoji
   │
resources/psd/*.psd
   └─(extract_tachie.js)──→ tachie/<char>/<id>.png（立ち絵レイヤー画像）

scene.html + assets.js + tachie/
   └─(render.js + Chromium)→ frames/f_%05d.png（連番フレーム）

frames/ + full.wav + resources/bgm/*.flac
   └─(build_video.py + ffmpeg)→ sarracenia.mp4（完成品）
```

---

## 2. ディレクトリとファイル

### コミットされているもの（このビルドシステム本体）

| ファイル | 役割 |
| --- | --- |
| `build.sh` | 下記すべてを順に呼び出すオーケストレーター。**通常はこれを実行するだけ**です。 |
| `script.json` | 台本。台詞・話者・読み（カナ原稿）・字幕・シーン・登場アイテムを定義します。 |
| `synth.py` | VOICEVOX で台詞ごとの音声を合成し、口パク用のモーラ区間も書き出します。 |
| `assemble.py` | 台詞 wav を「タイトル→本編（行間の無音）→クレジット」の順に結合し、タイムラインを算出します。 |
| `bake_assets.py` | `scene.html` が読む `assets.js`（フォント・絵文字・テクスチャ・タイムラインの束）を生成します。 |
| `extract_tachie.js` | 立ち絵 PSD から、必要なレイヤーだけを透過 PNG として切り出します。 |
| `scene.html` | 画面の見た目と `window.seek(t)`（時刻→画面）の本体。字幕・立ち絵・背景・クレジット・サムネを組み立てます。 |
| `render.js` | ヘッドレス Chromium で `scene.html` を 1 コマずつ撮り、`frames/` に書き出します。 |
| `build_video.py` | 連番フレーム＋ナレーション＋BGM を ffmpeg で合成して mp4 にします。 |
| `setup_chromium.js` | `@sparticuz/chromium`（同梱 Chromium）を `/tmp/chromium` に展開します（Chrome が無い環境向け）。 |
| `package.json` / `package-lock.json` | Node の依存関係（puppeteer / ag-psd / pngjs など）。 |
| `resources/**/*.md5` | 外部取得リソースの md5。**「そこに、どの名前で、何を置けばよいか」を保証するための目印**です。 |

### コミットされていないもの（`.gitignore` 対象）

- **外部取得リソースの実体**（`resources/psd/*.psd`, `resources/font/*.ttf`, `resources/emoji/*.svg`, `resources/bgm/*.flac`）
  … 著作権の都合でコミットしません。各 `.md5` を頼りに自分で配置してください（→ [3.](#3-用意する外部取得リソース)）。
- **中間生成物**（`assets.js`, `audio/`, `full.wav`, `timeline.json`, `kana.json`, `moras.json`, `durations.json`, `tachie/`）
- **出力**（`frames/`, `*.mp4`, `*.png`）
- **依存**（`node_modules/`）

---

## 3. 用意する外部取得リソース

以下を `resources/` の所定パスに置いてください。ファイル名と中身は、同じ場所にある `*.md5` と一致している必要があります。
（`cd resources/<dir> && md5sum -c <name>.md5` で照合できます。）

| 置き場所 | 中身 | 入手先 | ライセンス |
| --- | --- | --- | --- |
| `resources/psd/zundamon23.psd` | ずんだもん立ち絵素材 2.3（PSD） | 坂本アヒル 氏配布の立ち絵素材 | 良識の範囲で利用可・改変可（同梱 readme 参照） |
| `resources/psd/tsumugi3.psd` | 春日部つむぎ立ち絵素材 3.0（PSD） | 坂本アヒル 氏配布の立ち絵素材 | 同上（`tsumugi-official.studio.site/rule` の規約に準拠） |
| `resources/font/ZenMaruGothic-Black.ttf` | Zen Maru Gothic Black | Google Fonts「Zen Maru Gothic」 | SIL Open Font License 1.1 |
| `resources/font/ZenMaruGothic-Bold.ttf` | Zen Maru Gothic Bold | 同上 | SIL Open Font License 1.1 |
| `resources/emoji/seedling.svg` | 🌱（seedling）のカラー SVG | Microsoft「Fluent Emoji」 | MIT License |
| `resources/bgm/chopin_op10-4.flac` | ショパン 練習曲 作品10-4「Torrent（激流）」（Edward Neeman 演奏） | Wikimedia Commons / Musopen "Set Chopin Free" | パブリックドメイン |

> 立ち絵 PSD の ID 体系について：`extract_tachie.js` が切り出すレイヤーは、兄弟レイヤーの 1 始まりインデックスを
> `-` で連結した ID で指定します（グループもインデックスを 1 個消費します）。切り出す ID の一覧は `build.sh` の
> `ZUNDA_IDS` / `TSUMUGI_IDS` にあり、これは `scene.html` の `TACHIE` 定義と一致している必要があります。

### IFR25KU リポジトリ由来のテクスチャ（配置不要）

次の 4 枚は IFR25KU リポジトリにコミット済みなので、`bake_assets.py` がリポジトリから直接読みます（自分で置く必要はありません）。

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
- **Chrome / Chromium**（無ければ `setup_chromium.js` が同梱版を展開）

---

## 5. ビルド手順

```sh
# 1) この video/ ディレクトリで依存を入れる
npm install

# 2) VOICEVOX ENGINE を起動しておく（別ターミナルなど）
#    既定では http://127.0.0.1:50021 を使います

# 3) 外部取得リソースを resources/ に配置する（→ 3.）

# 4) ビルド
bash build.sh
```

完成すると `sarracenia.mp4` ができます。中間生成物・連番フレームも `video/` 内に残りますが、すべて `.gitignore` 済みです。

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
アイテムの表示区間などが入り、`scene.html` と `build_video.py` の両方がこれを読みます。

### bake_assets.py（アセットの焼き込み）
`file://` で開いた HTML は外部ファイルを `fetch()` できないため、フォント・絵文字・テクスチャ・タイムラインを
`assets.js` に埋め込みます。画像・フォントは base64 の data-URL に、絵文字 SVG は生の文字列として入ります。

### extract_tachie.js（立ち絵の切り出し）
`ag-psd` で PSD を読み、指定 ID のレイヤーを「全身キャンバスと同じサイズの透過 PNG」として書き出します。
`node-canvas` を入れずに済むよう、`createImageData` だけをシムして動かしています。

### scene.html（画面と seek）
画面の見た目のすべてと、時刻 `t` から全要素の状態を決める `window.seek(t)` が入っています。
背景のシーン切り替え（沼地↔泥沼のクロスフェード）、アイテム枠、立ち絵（口パク・まばたき・眉/腕/汗のポーズ）、
字幕（話者色の縁取り）、キャラ名、クレジット、そして開幕フレーム（＝サムネイル）のデカ字を組み立てます。

### render.js（フレーム撮影）
ヘッドレス Chromium で `scene.html` を開き、フォント読み込み完了を待ってから 30fps で 1 コマずつ撮ります。
画面の状態を表すキーが前のコマと同じなら、撮り直さず前のコマを使い回します。

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

Chrome を入れられない環境では、`package.json` に含まれる `@sparticuz/chromium`（同梱 Chromium）を
`node setup_chromium.js` で `/tmp/chromium` に展開して使えます。この Chromium は一部の共有ライブラリ（NSS 系など）を
別途必要とすることがあります。その場合は、必要な `.so` を集めたディレクトリを用意し、`CHROMIUM_LD_PATH` にそのパスを
指定してください（`build.sh` が render 時に `LD_LIBRARY_PATH` へ前置します）。
