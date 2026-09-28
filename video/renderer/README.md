# renderer なのだ～🌱

構成 jsonl と HTML のテンプレートから、連番のフレーム画像を撮る、汎用のレンダラーなのだ～🌱
動画の中身は何も知らなくて、このディレクトリだけで完結していて、ほかのパートに依存しないのだ～🌱

## 使い方なのだ～🌱

```sh
bash render.sh <template.html> <frames.jsonl> <outDir>
```

`render.sh` は、依存と Chromium を用意してから、`render.js` を呼ぶのだ～🌱
引数のパスは、呼び出し元のカレントディレクトリを基準にするのだ～🌱

- `template.html` は、描画する HTML のテンプレートなのだ～🌱
- `frames.jsonl` は、1 行が 1 フレームの構成の JSON オブジェクトを並べたファイルなのだ～🌱
- `outDir` は、`f_00000.png` からの連番の画像を書き出す先なのだ～🌱

テンプレートの中の `assets.js` のような相対参照は、テンプレートの場所を基準に解決されるのだ～🌱
構成 jsonl の空行は、読み飛ばすのだ～🌱
`outDir` は、撮る前に丸ごと消して作り直すのだ～🌱
引数が 3 個でないときは、使い方を出して、終了コード 1 で終わるのだ～🌱

`render.sh` は、足りないものを次のように用意するのだ～🌱

- `node_modules` が無ければ、このディレクトリで `npm install` するのだ～🌱
- `CHROMIUM_PATH` の指定が無くて `/tmp/chromium` も無ければ、`setup_chromium.js` を実行するのだ～🌱

依存と Chromium が揃っていれば、`render.js` を直接呼んでも同じなのだ～🌱

```sh
node render.js <template.html> <frames.jsonl> <outDir>
```

## テンプレートに求める約束なのだ～🌱

レンダラーがテンプレートに求めるのは、次の 2 個だけなのだ～🌱

1. 読み込みが終わったら、`window.__ready` を `true` にするのだ～🌱
2. `window.applyFrame(cfg)` を持つのだ～🌱

`cfg` は、構成 jsonl の 1 行で、1 フレーム分の設定のオブジェクトなのだ～🌱
`applyFrame(cfg)` は、その設定のとおりに画面を組み立てるのだ～🌱
同じ `cfg` を渡したときは、同じ見た目にならなきゃだめなのだ～🌱

## 撮り方なのだ～🌱

レンダラーは、構成 jsonl を頭から 1 行ずつ `applyFrame` に渡して、1 行につき 1 枚を撮るのだ～🌱
連続する行の構成を正規化した JSON が同じなら、撮り直さずに、前のコマの画像をコピーするのだ～🌱
正規化では、オブジェクトのキーを辞書順に並べるから、キーの順番だけが違う構成も同じとみなすのだ～🌱
だから、変化の少ない場面ほど、速く撮れるのだ～🌱

## 環境変数なのだ～🌱

| 変数 | 既定 | 読むもの | 説明 |
| --- | --- | --- | --- |
| `CHROMIUM_PATH` | `/tmp/chromium` | `render.sh` と `render.js` | Chrome か Chromium の実行ファイルなのだ～🌱 |
| `CHROMIUM_LD_PATH` | なし | `render.sh` | `render.js` を走らせるときに、`LD_LIBRARY_PATH` へ前置するパスなのだ～🌱 |
| `VIDEO_WIDTH` | `1280` | `render.js` | 画面の幅なのだ～🌱 |
| `VIDEO_HEIGHT` | `720` | `render.js` | 画面の高さなのだ～🌱 |

手元の Chrome を使うときは、そのパスを `CHROMIUM_PATH` に指定するのだ～🌱

## Chromium の用意なのだ～🌱

Chrome が無い環境では、`node setup_chromium.js` で、同梱の Chromium を `/tmp/chromium` に展開できるのだ～🌱
同梱の Chromium は、`@sparticuz/chromium` のパッケージに入っているものなのだ～🌱

root 権限が無くて、NSS 系の共有ライブラリが OS に無い環境では、この Chromium が起動に失敗することがあるのだ～🌱
`libnss3.so` のような共有ライブラリを、見つけられないからなのだ～🌱
その NSS 一式は、`@sparticuz/chromium` が同梱している `bin/al2023.tar.br` を展開すると得られるのだ～🌱
`bin/al2023.tar.br` は、Brotli で圧縮された tar なのだ～🌱
取り出した `.so` を 1 個のディレクトリに集めて、そのパスを `CHROMIUM_LD_PATH` に指定するのだ～🌱

## 依存なのだ～🌱

`npm install` で、`puppeteer-core` と `@sparticuz/chromium` が入るのだ～🌱

## このファイル自体の編集ルールなのだ～🌱

このファイルは、1 行を全角 70 文字までにして、次のスキルを厳守して書くのだ～🌱

- [markdown-max-line-length](https://github.com/MirrgieRiana/MirrgieRiana.github.io/blob/main/.claude/skills/markdown-max-line-length/SKILL.md)
