# sarracenia — サラセニア寸劇の「構成」を作るパートなのだぁ🌱

この動画（サラセニア寸劇）の台本・シーン・**構成jsonl** を作る、動画タイトルを冠したディレクトリです。
単体で完結していて、汎用レンダラー（`../renderer/`）にも、雑多パート（`../`）にも依存しません。

役割は「タイムラインを受け取って、レンダラーに渡す材料（`assets.js`・立ち絵 `tachie/`・構成jsonl `frames.jsonl`）を作る」ことです。

## 使い方

```sh
bash build_scene.sh [timeline.json]
```

- 引数はタイムラインのパス（省略時は `../timeline.json`）。尺・台詞区間・シーン・アイテムが入っています。
- タイムラインの作り手（音声合成・結合）は雑多パートの担当で、ここはその結果を受け取るだけです。
- 出力（このディレクトリ配下）：`assets.js` ／ `tachie/` ／ `frames.jsonl`。

## ファイル

| ファイル | 役割 |
| --- | --- |
| `build_scene.sh` | このパートの入口。このディレクトリへ cd して、`build_scene.xa1` を呼ぶだけです。 |
| `build_scene.xa1` | 処理の本体。リソース確認・`npm install`・`assets.js`（フォント・絵文字・テクスチャ・タイムラインの束）の焼き込み・下記スクリプトの実行と、構成jsonl（`frames.jsonl`、1 行 = 1 フレーム）の生成をまとめます。 |
| `script.json` | 台本。台詞・話者・読み（カナ原稿）・字幕・シーン・登場アイテムを定義します。 |
| `scene.html` | 画面の見た目と `window.applyFrame(cfg)`（構成→画面）の本体。 |
| `extract_tachie.js` | 立ち絵 PSD から、必要なレイヤーだけを透過 PNG として切り出します。 |
| `resources/**/*.md5` | 外部取得リソースの md5（置き場所と中身の目印）。実体は著作権上コミットしません。 |

## 構成jsonl（`frames.jsonl`）について

1 行 = 1 フレームの構成オブジェクトを並べたものです。汎用レンダラーが、これを 1 行ずつ `scene.html` の
`window.applyFrame(cfg)` に渡します。

各行は、`template` と、時刻 `t` と、背景とアイテム枠の不透明度の `bogOp`・`swampOp`・`plantOp`・`leafOp` を持ちます。
`template` に入れるテンプレートは、`build_scene.xa1` の冒頭で決めています。
不透明度の 4 個は `build_scene.xa1` がタイムラインから計算し、`scene.html` の `applyFrame` がそのまま当てます。
残りの要素は、まだ `scene.html` の `seek(t)` がタイムラインを見て組み立てるので、時刻 `t` も渡します。
これから（Issue #179 トピック BP の構想）、各行に「どの要素へどんな CSS・属性・テキストを入れるか」をフラットに
書き込んで、`seek` の計算そのものを構成jsonl 側へ段階的に移していきます。そのとき、レンダラー側（`applyFrame` の
契約）は一切変えなくてよい設計です。

外部取得リソースの入手・配置と、立ち絵 ID 体系については、親ディレクトリの [`../README.md`](../README.md) を見てください。

## 依存

`npm install` で `ag-psd` と `pngjs`（立ち絵切り出し用）が入ります。`build_scene.xa1` は、リポジトリ同梱の xarpite（`../../xarpite/`）で動き、画像とフォントの符号化に `base64` コマンドを使います。
