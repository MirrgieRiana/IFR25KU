# IFR25KU 動画ビルドシステムなのだ～🌱

VOICEVOX の立ち絵のずんだもんと春日部つむぎが、口パクとまばたきとポーズ変化をしながら会話する、
IFR25KU の解説の劇場動画を、**台本テキストから 1 コマンドで組み立てる**ためのビルドシステムなのだ～🌱

今の題材はサラセニアという食虫植物の劇場で、長さは 1 分ちょっとなのだ～🌱
台本の xa1 とテンプレートの `theater-v1.html` を差し替えれば、別の劇場にも使えるのだ～🌱

---

## 1. 3 つのパートと、仕組みの全体像なのだ～🌱

`video/` は、役割ごとに 3 つのパートに分かれているのだ～🌱

1. **`renderer/`（汎用レンダラー）** … 構成jsonl（1 行 = 1 フレーム）と HTML テンプレートを受け取って、ひたすら対応する画像を撮るだけの、**動画の中身を知らない**カプセル化されたレンダラーなのだ～🌱 単体で完結していて、他のパートに依存しないのだ～🌱
2. **`projects/common/`（劇場の形式）** … 劇場という形式に共通な、テンプレートと、**構成jsonl を作る**処理と、共有のリソースを持つパートなのだ～🌱 動画ごとに違う値は、動画プロジェクトから受け取るのだ～🌱
3. **`video/` 直下（雑多な部分）** … 音声合成と動画合成をして、**1 と 2 を呼び出して**動画を完成させるのだ～🌱 `src/main/xa1/video-plugin.xa1` が、その配線役なのだ～🌱

1 個の動画は、`src/projects/xa1/<yyyy-MM-dd-タイトル>.xa1` の 1 個のファイルへ閉じるのだ～🌱
台本と、その動画に固有のビルドの定義だけを持って、それ以外は全部 1 と 2 と 3 へ任せるのだ～🌱
今あるのは `src/projects/xa1/2026-04-12-sarracenia.xa1` の 1 個なのだ～🌱

この動画は、動画編集ソフトの GUI で作るのではなくて、**「決定論的フレームレンダリング」** という方式で作るのだ～🌱

1. `projects/common/theater-v1.html` が、1 フレーム分の構成 `frame` を渡すとその画面を組み立てる関数 `window.applyFrame(frame)` を持つのだ～🌱 画面の見た目を決める値は、全部 `frame` に入っているのだ～🌱
2. `projects/common/theater/theater-v1.xa1` が、30fps 刻みの構成jsonl（`frames.jsonl`、1 行 = 1 フレーム）を作るのだ～🌱 その劇場に固有の値は、`src/projects/xa1/2026-04-12-sarracenia.xa1` が渡すのだ～🌱
3. `renderer/render.js` がヘッドレス Chromium にテンプレートを開かせて、`frames.jsonl` を頭から 1 行ずつ `applyFrame(frame)` に渡して、1 行につき 1 コマ撮るのだ～🌱
4. 撮れた連番画像の `build/frames/2026-04-12-sarracenia/png/f_00000.png …` を ffmpeg で映像にして、ナレーション音声と BGM を重ねて mp4 にするのだ～🌱

絵は構成jsonl だけで決まるから、マシンの速さに関係なく尺が正確で、何度ビルドしても同じ結果になるのだ～🌱
連続する行の構成が同じなら、撮り直さずに前のコマを使い回すのだ～🌱
今のこの動画は行ごとに時刻が違うから、使い回しは起きないのだ～🌱

### データの流れなのだ～🌱

図の中では、動画プロジェクトの名前の `2026-04-12-sarracenia` を `<proj>` と書くのだ～🌱

```
src/projects/xa1/<proj>.xa1（台本）─(script.xa1)→ build/script/<proj>/script.json
   │
   ├─(audio.xa1 + VOICEVOX)→ build/audio/<proj>/wav/*.wav, moras.json, kana.json, durations.json  ┐ 雑多パート
   │                                                                                   │ (video/ 直下)
   └─(timeline.xa1)───────→ build/timeline/<proj>/full.wav（ナレーション全体）, timeline.json（尺・区間） ┘

timeline.json ─┬─(theater-v1.xa1)──────────→ build/scene/<proj>/assets.js      ┐
               │        ↑ IFR25KU テクスチャ / common のフォントと絵文字  │ 劇場の形式
 common の psd ─(extract-portrait/extract-portrait.sh)→ build/scene/<proj>/portrait/ │（構成を作る）
               └─(theater-v1.xa1)──────────→ build/scene/<proj>/frames.jsonl ┘

projects/common/theater-v1.html + build/scene/<proj>/ の assets.js + portrait/ + frames.jsonl
   └─(renderer/render.js + Chromium)→ build/frames/<proj>/png/f_%05d.png   … 汎用レンダラー

build/frames/<proj>/ + full.wav + projects/common/resources/bgm/*.flac
   └─(movie.xa1 + ffmpeg)→ build/movie/<proj>/<proj>.mp4（完成品）  … 雑多パート
```

各パートの詳しい説明は、それぞれの README にもあるのだ～🌱

- [`renderer/README.md`](renderer/README.md)
- [`extract-portrait/README.md`](extract-portrait/README.md)

---

## 2. ディレクトリとファイルなのだ～🌱

### コミットされているもの（このビルドシステム本体）なのだ～🌱

**`video/` 直下（雑多な部分・1 と 2 を呼び出すパート）**

| ファイル | 役割 |
| --- | --- |
| `athanorw` | ビルドする入口なのだ～🌱 `<サブプロジェクト名>:<タスク名>` を受け取って、そのタスクを呼ぶのだ～🌱 **ふつうはここで `./athanorw 2026-04-12-sarracenia:build` を実行するだけ**なのだ～🌱 |
| `main.xa1` | `video/` の入口なのだ～🌱 サブプロジェクト名を受け取って、`src/projects/xa1/` のそのモジュールを読むのだ～🌱 |
| `src/projects/xa1/2026-04-12-sarracenia.xa1` | 1 個の動画の、台本とビルドの定義なのだ～🌱 `video-plugin` へ自分の名前と台本とシーンを組む関数を渡して、組んだプロジェクトを返すのだ～🌱 この劇場に固有の、立ち絵のキャラとテクスチャとポーズの配列と、シーンとアイテムの不透明度の決め方も、ここが `theater/theater-v1.xa1` へ渡すのだ～🌱 |
| `athanor/athanor.xa1` | タスクを並べてビルドを組むための仕組みなのだ～🌱 タスクを走らせると、`dependsOn` を先にたどってから、自分の動作を呼ぶのだ～🌱 |
| `src/main/xa1/video-plugin.xa1` | サブプロジェクトに要るタスクを、依存の順に並べて渡すのだ～🌱 段と段の順序を、1 か所で持つのだ～🌱 |
| `src/main/xa1/script.xa1` | 動画プロジェクトの台本を `script.json` へ焼くのだ～🌱 |
| `src/main/xa1/audio.xa1` | VOICEVOX で台詞ごとの音声を合成して、口パク用のモーラ区間も書き出すのだ～🌱 |
| `src/main/xa1/timeline.xa1` | 台詞 wav を、タイトル、本編（行間の無音）、クレジットの順に結合して、タイムラインを算出するのだ～🌱 |
| `src/main/xa1/scene.xa1` | 動画プロジェクトの入口を叩いて、テンプレートと構成jsonl を組ませるのだ～🌱 |
| `src/main/xa1/frames.xa1` | 汎用レンダラーを呼んで、連番のフレーム画像を撮らせるのだ～🌱 |
| `src/main/xa1/clean.xa1` | 生成物の `build` ディレクトリを、まとめて捨てるのだ～🌱 |
| `src/main/xa1/common.xa1` | 各段が共有する値と関数（置き場所・外部コマンドの実行・生成の枠組み・丸め・wav の読み出し）なのだ～🌱 |
| `src/main/xa1/movie.xa1` | 連番フレームとナレーションと BGM を ffmpeg で合成して mp4 にするのだ～🌱 |

**`renderer/`（汎用レンダラー・自己完結）**

| ファイル | 役割 |
| --- | --- |
| `render.js` | 構成jsonl と HTML テンプレートを受け取って、1 行 = 1 フレームで Chromium から画像を撮るのだ～🌱 動画の中身を知らないのだ～🌱 |
| `setup_chromium.js` | `@sparticuz/chromium`（同梱 Chromium）を `/tmp/chromium` に展開するのだ～🌱 Chrome が無い環境向けなのだ～🌱 |
| `package.json` / `package-lock.json` | Node の依存関係（`puppeteer-core` / `@sparticuz/chromium`）なのだ～🌱 |

**`extract-portrait/`（立ち絵の切り出し・自己完結）**

| ファイル | 役割 |
| --- | --- |
| `extract-portrait.sh` | Node の依存を用意してから、レイヤーを切り出すのだ～🌱 このツールの入口なのだ～🌱 |
| `extract_portrait.js` | 立ち絵 PSD から、必要なレイヤーだけを透過 PNG として切り出すのだ～🌱 |
| `package.json` / `package-lock.json` | Node の依存関係（`ag-psd` / `pngjs`）なのだ～🌱 |

**`projects/common/`（劇場の形式・動画プロジェクトが共有するもの）**

| ファイル | 役割 |
| --- | --- |
| `theater-v1.html` | 劇場のテンプレートなのだ～🌱 画面の見た目と `window.applyFrame(frame)` を持つのだ～🌱 |
| `theater/theater-v1.xa1` | 台本から劇場の構成を組み上げるシステムなのだ～🌱 `assets.js` と立ち絵の切り出しと構成jsonl を作るのだ～🌱 劇場の動画プロジェクトが、自分に固有の値を渡して呼ぶのだ～🌱 |
| `resources/**/*.md5` | 外部取得リソースの md5 なのだ～🌱 **そこに、どの名前で、何を置けばよいかを保証するための目印**なのだ～🌱 |

### コミットされていないもの（`.gitignore` 対象）なのだ～🌱

- **外部取得リソースの実体**（`projects/common/resources/psd/*.psd`, `.../font/*.ttf`, `.../emoji/*.svg`, `.../bgm/*.flac`）
  … 著作権の都合でコミットしないのだ～🌱 各 `.md5` を頼りに自分で配置するのだ～🌱（→ [3.](#3-用意する外部取得リソースなのだ)）
- **生成物**（`video/build/` の下の、台本と音声とタイムラインと、シーンの材料とフレーム画像と完成品）
- **依存**（各パート配下の `node_modules/`）

---

## 3. 用意する外部取得リソースなのだ～🌱

以下を `projects/common/resources/` の所定パスに置くのだ～🌱
ファイル名と中身は、同じ場所にある `*.md5` と一致している必要があるのだ～🌱
`cd projects/common/resources/<dir> && md5sum -c <name>.md5` で照合できるのだ～🌱

| 置き場所 | 中身 | 入手先 | ライセンス |
| --- | --- | --- | --- |
| `projects/common/resources/psd/zundamon23.psd` | ずんだもん立ち絵素材 2.3（PSD） | 坂本アヒル 氏配布の立ち絵素材 | 良識の範囲で利用可・改変可（同梱 readme 参照） |
| `projects/common/resources/psd/tsumugi3.psd` | 春日部つむぎ立ち絵素材 3.0（PSD） | 坂本アヒル 氏配布の立ち絵素材 | 同上（`tsumugi-official.studio.site/rule` の規約に準拠） |
| `projects/common/resources/font/ZenMaruGothic-Black.ttf` | Zen Maru Gothic Black | Google Fonts「Zen Maru Gothic」 | SIL Open Font License 1.1 |
| `projects/common/resources/font/ZenMaruGothic-Bold.ttf` | Zen Maru Gothic Bold | 同上 | SIL Open Font License 1.1 |
| `projects/common/resources/emoji/seedling.svg` | 🌱（seedling）のカラー SVG | Microsoft「Fluent Emoji」 | MIT License |
| `projects/common/resources/bgm/chopin_op10-4.flac` | ショパン 練習曲 作品10-4「Torrent（激流）」（Edward Neeman 演奏） | Wikimedia Commons / Musopen "Set Chopin Free" | パブリックドメイン |

立ち絵 PSD の ID 体系なのだ～🌱
`extract-portrait/` が切り出すレイヤーは、兄弟レイヤーの 1 始まりインデックスを `-` で連結した ID で指定するのだ～🌱
グループも、インデックスを 1 個消費するのだ～🌱
切り出す ID の一覧は `src/projects/xa1/2026-04-12-sarracenia.xa1` の `ZUNDA_IDS` と `TSUMUGI_IDS` にあるのだ～🌱
これは、テンプレートの `PORTRAIT` 定義と一致している必要があるのだ～🌱

### IFR25KU リポジトリ由来のテクスチャ（配置不要）なのだ～🌱

次の 4 枚は IFR25KU リポジトリにコミット済みだから、`src/projects/xa1/2026-04-12-sarracenia.xa1` がリポジトリから直接読むのだ～🌱
自分で置く必要は無いのだ～🌱

| 用途 | ファイル |
| --- | --- |
| アイテム枠（サラセニア本体） | `common/.../textures/block/magic_plant/sarracenia_age3.png` |
| アイテム枠（サラセニアの葉） | `common/.../textures/item/sarracenia_leaf.png` |
| サムネ左上ロゴ | `common/.../miragefairy2024/icon.png` |
| 背景タイル | `common/.../textures/block/haimeviska_log.png` |

---

## 4. 前提ツールなのだ～🌱

- **Node.js**（18 以降を想定）と **npm**
- **ffmpeg**
- **curl**（`audio.xa1` が VOICEVOX ENGINE と通信するのに使うのだ～🌱）
- **VOICEVOX ENGINE**（音声合成サーバーなのだ～🌱 起動しておくのだ～🌱）
- **Chrome / Chromium**（無ければ `renderer/setup_chromium.js` が同梱版を展開するのだ～🌱）

---

## 5. ビルド手順なのだ～🌱

```sh
# 1) VOICEVOX ENGINE を起動しておくのだ～🌱（別ターミナルなど）
#    既定では http://127.0.0.1:50021 を使うのだ～🌱

# 2) 外部取得リソースを projects/common/resources/ に配置するのだ～🌱（→ 3.）

# 3) ビルドなのだ～🌱（Node 依存は各パートで自動 npm install するのだ～🌱）
./athanorw 2026-04-12-sarracenia:build
```

完成すると `video/build/movie/2026-04-12-sarracenia/2026-04-12-sarracenia.mp4` ができるのだ～🌱
中間生成物と連番フレームも `video/` 内に残るけど、全部 `.gitignore` 済みなのだ～🌱

`./athanorw` の引数で、途中の段だけを走らせることもできるのだ～🌱

タスクの名前は `:` で区切るのだ～🌱 左辺がサブプロジェクト名で、右辺が段の名前なのだ～🌱
以下の表では、左辺を省いて書くのだ～🌱

| 段 | やること |
| --- | --- |
| `generateScript` | 台本を `script.json` へ焼くのだ～🌱 |
| `generateAudio` | 台詞ごとの音声を合成するのだ～🌱 |
| `generateTimeline` | 音声を結合して、タイムラインを算出するのだ～🌱 |
| `generateScene` | 動画プロジェクトの構成（`assets.js`・`portrait/`・`frames.jsonl`）を作るのだ～🌱 |
| `generateFrames` | 連番のフレーム画像を撮るのだ～🌱 |
| `generateMovie` | フレームとナレーションと BGM を合成して mp4 にするのだ～🌱 |
| `build` | 主要なものをビルドするのだ～🌱 |
| `clean` | 生成物の `build` ディレクトリを、まとめて捨てるのだ～🌱 |

`./athanorw` は、`video/` 直下にあるのだ～🌱 段の定義は、`video/src/main/xa1/video-plugin.xa1` が持つのだ～🌱
どの段も、前の段を自分で呼ぶから、`build` だけで最初から通るのだ～🌱
そして、生成物が既にある段は、飛ばすのだ～🌱 作り直したいときは、その段のディレクトリを消すか、`clean` で全部捨てるのだ～🌱

`renderer/` と `extract-portrait/` は Node 依存が別々だから、それぞれのディレクトリで必要なときだけ `npm install` するのだ～🌱
手動で入れるなら、各ディレクトリで `npm install` するのだ～🌱

### 環境変数で差し替えられる設定なのだ～🌱

`./athanorw` は次の環境変数を見るのだ～🌱
無指定なら、既定値なのだ～🌱

| 変数 | 既定 | 説明 |
| --- | --- | --- |
| `VOICEVOX_HOST` | `http://127.0.0.1:50021` | VOICEVOX ENGINE の URL なのだ～🌱 |
| `CHROMIUM_PATH` | `/tmp/chromium` | Chrome か Chromium の実行ファイルなのだ～🌱 手元の Chrome を使うなら、そのパスを指定するのだ～🌱 |
| `CHROMIUM_LD_PATH` | （なし） | render 時に `LD_LIBRARY_PATH` へ前置するパスなのだ～🌱 共有ライブラリを補う必要のある環境向けなのだ～🌱 |
| `FFMPEG` | `ffmpeg` | ffmpeg の実行ファイルなのだ～🌱 |
| `BGM_VOL` | `0.45` | BGM の基準音量なのだ～🌱（0 から 1） |

---

## 6. 各ステップの詳細なのだ～🌱

### audio.xa1（音声合成）なのだ～🌱

台本の各台詞について、VOICEVOX の **カタカナ原稿（`is_kana`）記法** を使って、読みを厳密に指定して合成するのだ～🌱
`script.json` の各行に `kana`（カナ原稿）があればそれを使って、無ければ `text` から自動生成した読みを使うのだ～🌱
口パク用に、各モーラ（音の粒）が wav のどこに位置するか（`moras.json`）も書き出すのだ～🌱

カナ原稿の記法は VOICEVOX 標準で、アクセント核が `'`、アクセント句区切りが `/`、小休止が `、`、語尾上げが `？` なのだ～🌱
長音は、母音を重ねて書くのだ～🌱（例：サトウ→`サトオ`）
平板型は、核をアクセント単位の末尾に置くことで表すのだ～🌱

### timeline.xa1（結合・タイムライン）なのだ～🌱

台詞 wav を、タイトル保持、本編（台詞のあいだに無音の間）、クレジット保持の順に結合して、`full.wav` と `timeline.json` を作るのだ～🌱
`timeline.json` には、各台詞の開始と終了の時刻と、シーン区間と、アイテムの表示区間などが入るのだ～🌱
それを、`theater-v1.xa1` と `movie.xa1` が読むのだ～🌱

### theater-v1.xa1（アセットの焼き込み）なのだ～🌱

`file://` で開いた HTML は外部ファイルを `fetch()` できないから、絵文字とテクスチャのパスとタイムラインを `assets.js` に埋め込むのだ～🌱
テクスチャはテンプレートから見た相対パスの文字列で、絵文字 SVG は生の文字列として入るのだ～🌱
`img` の `src` と CSS の `url()` は `fetch()` を通らないから、テクスチャもフォントも、相対パスのまま `file://` で読めるのだ～🌱

### extract-portrait（立ち絵の切り出し）なのだ～🌱

`ag-psd` で PSD を読んで、指定 ID のレイヤーを、全身キャンバスと同じサイズの透過 PNG として書き出すのだ～🌱
`node-canvas` を入れずに済むように、`createImageData` だけをシムして動かしているのだ～🌱

### theater-v1.xa1（構成jsonl の組み立て）なのだ～🌱

`timeline.json` から総尺を読んで、30fps 刻みの構成jsonl（`frames.jsonl`、1 行 = 1 フレーム）を作るのだ～🌱
各行は `template` と `t` と、画面の各要素の不透明度のキーと、ポーズと口パクとまばたきと字幕の中身を持つのだ～🌱
`template` に入れるテンプレートは、`theater-v1.xa1` の冒頭で決めているのだ～🌱
画面の見た目を決める値は、全部ここで計算して渡すから、テンプレートの側はタイムラインを見ないのだ～🌱
レンダラー側は、この構成の中身が増えても変えなくてよい設計なのだ～🌱

### theater-v1.html（画面と applyFrame）なのだ～🌱

画面の見た目のすべてと、構成 `frame` から画面を決める `window.applyFrame(frame)` が入っているのだ～🌱
`applyFrame` は、構成の各行の値を、そのまま画面の各要素へ当てるのだ～🌱
背景のシーン切り替え（沼地と泥沼のクロスフェード）と、アイテム枠と、立ち絵（口パク・まばたき・眉と腕と汗のポーズ）を組み立てるのだ～🌱
それと、字幕（話者色の縁取り）と、キャラ名と、クレジットと、開幕フレーム（＝サムネイル）のデカ字も組み立てるのだ～🌱

### renderer/render.js（フレーム撮影）なのだ～🌱

ヘッドレス Chromium でテンプレートを開いて、フォント読み込み完了を待つのだ～🌱
それから、構成jsonl を 1 行ずつ `applyFrame(frame)` に渡して、1 行につき 1 コマ撮るのだ～🌱
連続する行の構成を正規化した JSON が同じなら、撮り直さずに前のコマを使い回すのだ～🌱
動画の中身を知らない、汎用レンダラーなのだ～🌱

### movie.xa1（映像・音声の合成）なのだ～🌱

連番フレームを 30fps の映像にして、`full.wav`（ナレーション）と BGM を重ねるのだ～🌱
BGM は、冒頭がやや強め、本編が弱め、クレジットが冒頭と同じ、と音量が線形補間で変化するのだ～🌱
そうやって、ナレーションが常に上に立つようにしているのだ～🌱

---

## 7. クレジット（動画末尾に表示される帰属表記）なのだ～🌱

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

IFR25KU リポジトリのテクスチャのうち、次の 2 種類は使っていないのだ～🌱

- README の「Resources Derived from MirageFairy2019」に挙がるもの（CC BY-SA 3.0）
- Minecraft 由来のもの（Mojang 著作権）

使っているのは、MOD オリジナル素材（「Other Resources」＝ Apache-2.0 / CC BY 3.0 / CC BY 4.0 の選択制）だけなのだ～🌱

---

## 8. 補足：Chrome が無い・共有ライブラリが足りない環境で動かすにはなのだ～🌱

手元に Chrome がある場合は、`CHROMIUM_PATH=/path/to/chrome` を指定するのが、いちばん簡単なのだ～🌱

Chrome を入れられない環境では、`renderer/package.json` に含まれる `@sparticuz/chromium`（同梱 Chromium）を使えるのだ～🌱
`node renderer/setup_chromium.js` で `/tmp/chromium` に展開するのだ～🌱
ふつうの Linux なら、これだけで動くのだ～🌱

ごく限られた環境では、この Chromium が `libnss3.so` などを見つけられずに、起動に失敗することがあるのだぁ…🌧️
root 権限が無くて、NSS 系の共有ライブラリが OS に無い環境などなのだ～🌱
その NSS 一式は、`@sparticuz/chromium` パッケージが同梱している `bin/al2023.tar.br`（Brotli 圧縮された tar）を展開すると得られるのだ～🌱
取り出した `.so` を 1 個のディレクトリに集めて、`CHROMIUM_LD_PATH` にそのパスを指定するのだ～🌱
`renderer/render.sh` が、render 時に `LD_LIBRARY_PATH` へ前置するのだ～🌱
なお、この共有ライブラリの補完は、上記のような特殊な環境でだけ必要で、Chrome のある環境では `CHROMIUM_PATH` を指すだけで済むのだ～🌱

---

## このファイル自体の編集ルールなのだ～🌱

このファイルは、1 行を全角 70 文字までにして、次のスキルを厳守して書くのだ～🌱

- [markdown-max-line-length](https://github.com/MirrgieRiana/MirrgieRiana.github.io/blob/main/.claude/skills/markdown-max-line-length/SKILL.md)
