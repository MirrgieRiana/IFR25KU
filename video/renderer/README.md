# renderer — 汎用フレームレンダラーなのだぁ🌱

構成jsonl（1 行 = 1 フレーム）と HTML テンプレートを受け取って、ひたすら対応する画像を撮るだけの、
**動画の中身を知らない**カプセル化されたレンダラーです。単体で完結していて、他のパートに依存しません。

## 使い方

```sh
bash render.sh <template.html> <frames.jsonl> <outDir>
```

`render.sh` は、`node_modules` が無ければ `npm install` を、`CHROMIUM_PATH` の指定が無く `/tmp/chromium` も無ければ `setup_chromium.js` を実行してから、`render.js` を呼びます。
依存と Chromium が揃っていれば、`node render.js <template.html> <frames.jsonl> <outDir>` と直接呼んでも同じです。

- `template.html` … レンダリング対象の HTML テンプレート。相対参照（`assets.js` など）は、この HTML の場所を基準に解決されます。
- `frames.jsonl` … 1 行 = 1 フレームの構成（JSON オブジェクト）を並べたファイル。
- `outDir` … 連番画像 `f_00000.png …` を書き出す先（丸ごと作り直します）。

## テンプレートに求める 2 つの約束

レンダラーがテンプレートに期待するのは、次の 2 点だけです。

1. 読み込みが終わったら `window.__ready` を `true` にすること。
2. `window.applyFrame(cfg)` を持つこと。
   - `cfg` は構成jsonl の 1 行（＝1 フレーム分の設定オブジェクト）です。
   - `applyFrame(cfg)` は、その設定どおりに画面を組み立てて、**「この画面の見た目を一意に表す文字列（再利用キー）」を返します**。

レンダラーは構成jsonl を頭から 1 行ずつ `applyFrame` に渡し、1 行につき 1 枚の画像を撮ります。
連続する行の再利用キーが同じなら、撮り直さず前のコマを使い回します（変化の少ない場面が速くなります）。

## 環境変数

| 変数 | 既定 | 説明 |
| --- | --- | --- |
| `CHROMIUM_PATH` | `/tmp/chromium` | Chrome/Chromium 実行ファイル。手元の Chrome を使うならそのパスを指定 |
| `CHROMIUM_LD_PATH` | （なし） | `render.sh` が `render.js` を走らせるときに `LD_LIBRARY_PATH` へ前置するパス。共有ライブラリを補う必要のある環境向け |
| `VIDEO_WIDTH` | `1280` | 画面幅 |
| `VIDEO_HEIGHT` | `720` | 画面高さ |

## Chromium の用意

Chrome が無い環境では、`node setup_chromium.js` で同梱 Chromium（`@sparticuz/chromium`）を `/tmp/chromium` に展開できます。

ごく限られた環境（root 権限が無く、NSS 系の共有ライブラリが OS に無いなど）では、この Chromium が
`libnss3.so` などを見つけられず起動に失敗することがあります。その NSS 一式は、`@sparticuz/chromium` パッケージが
同梱している `bin/al2023.tar.br`（Brotli 圧縮された tar）を展開すると得られます。取り出した `.so` を 1 つのディレクトリに
集め、`CHROMIUM_LD_PATH` にそのパスを指定してください（`render.sh` が `LD_LIBRARY_PATH` へ前置します）。

## 依存

`npm install` で `puppeteer-core` と `@sparticuz/chromium` が入ります。
