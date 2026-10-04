# 2026-04-12-sarracenia — サラセニア寸劇の「構成」を作る動画プロジェクトなのだ～🌱

この動画の台本とシーンと **構成jsonl** を作る、動画のタイトルを冠したディレクトリなのだ～🌱
ここだけで完結していて、汎用レンダラー（`../../renderer/`）にも、雑多パート（`../../`）にも依存しないのだ～🌱

役割は、タイムラインを受け取って、レンダラーに渡す材料を作ることなのだ～🌱
材料は、`assets.js` と、立ち絵の `portrait/` と、構成jsonl の `frames.jsonl` なのだ～🌱

## 使い方なのだ～🌱

```sh
../../../xarpite/xarpite -A 5 -q -e 'USE("$PWD/main").buildScene()' [timeline.json]
```

- 引数はタイムラインのパスで、省略すると `../../timeline.json` を見るのだ～🌱 中には、尺と台詞の区間とシーンとアイテムが入っているのだ～🌱
- タイムラインを作るのは雑多パートの担当で、ここはその結果を受け取るだけなのだ～🌱
- 出力は、このディレクトリの下の `assets.js` と `portrait/` と `frames.jsonl` なのだ～🌱

## ファイルなのだ～🌱

| ファイル | 役割 |
| --- | --- |
| `main.xa1` | このパートの入口なのだ～🌱`build-scene.xa1` を呼ぶ関数を返すだけなのだ～🌱 |
| `build-scene.xa1` | 処理の本体なのだ～🌱 リソースの確認と、絵文字とテクスチャのパスとタイムラインを束ねた `assets.js` の焼き込みと、`extract-portrait/` の呼び出しと、1 行 = 1 フレームの構成jsonl（`frames.jsonl`）の生成をまとめるのだ～🌱 |
| `script.xa1` | 台本なのだ～🌱 台詞と話者と読み（カナ原稿）と字幕とシーンと登場アイテムを定義して、`script.json` として焼かれるのだ～🌱 |
| `scene.html` | 画面の見た目と `window.applyFrame(frame)`（構成→画面）の本体なのだ～🌱 |

## 構成jsonl（`frames.jsonl`）のことなのだ～🌱

1 行 = 1 フレームの構成オブジェクトを並べたものなのだ～🌱
汎用レンダラーが、これを 1 行ずつ `scene.html` の `window.applyFrame(frame)` へ渡すのだ～🌱

各行は、`template` と、時刻の `t` と、画面の各要素の不透明度と、ポーズと口パクとまばたきと字幕の中身を持つのだ～🌱
`template` に入れるテンプレートは、`build-scene.xa1` の冒頭で決めているのだ～🌱
不透明度は `build-scene.xa1` がタイムラインから計算して、`scene.html` の `applyFrame` がそのまま当てるのだ～🌱
内訳は、背景の `bogOp` と `swampOp`、アイテム枠の `plantOp` と `leafOp`、開幕のサムネの `openOp`、
クレジットの `creditOp`、立ち絵の `portraitOp`、字幕の横帯の `bandOp`、字幕とキャラ名の `subOp`、
字幕の下敷きの `scrimOp` なのだ～🌱
ポーズは `tPose` と `zPose`、口パクは `zMouth` と `tMouth`、まばたきは `zBlink` と `tBlink`、
字幕の中身は `subLine` なのだ～🌱
どれも `build-scene.xa1` がタイムラインから決めるから、`scene.html` の側はタイムラインを見ないのだ～🌱
レンダラーの側の `applyFrame` の約束は、この構成の中身が増えても一切変えなくていい作りなのだ～🌱

外部取得リソースの入手と配置と、立ち絵の ID 体系は、雑多パートの [`../../README.md`](../../README.md) にあるのだ～🌱

## 依存なのだ～🌱

`npm install` で、立ち絵の切り出しに使う `ag-psd` と `pngjs` が入るのだ～🌱
`build-scene.xa1` は、リポジトリ同梱の xarpite（`../../../xarpite/`）で動くのだ～🌱
読み書きするパスは、`main.xa1` と同じく、自分の `LOCATION` から解決した絶対パスを基準にするから、どのディレクトリから呼んでもいいのだ～🌱
