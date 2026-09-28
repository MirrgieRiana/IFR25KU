# renderer なのだ～🌱

構成 jsonl と HTML のテンプレートから、連番のフレーム画像をレンダリングする、汎用のレンダラーなのだ～🌱
特定の動画の内容には依存しなくて、コードと依存は、このディレクトリの中で完結しているのだ～🌱

## 使い方なのだ～🌱

```sh
bash render.sh <template.html> <frames.jsonl> <outDir>
```

`render.sh` は、不足している依存と Chromium を準備してから、フレーム画像をレンダリングするのだ～🌱
引数のパスは、呼び出し元のカレントディレクトリを基準に解決するのだ～🌱

- `template.html` は、レンダリングする HTML のテンプレートなのだ～🌱
- `frames.jsonl` は、1 フレーム分の構成の JSON オブジェクトを、1 行に 1 個ずつ並べたファイルなのだ～🌱
- `outDir` は、`f_00000.png` から始まる連番の PNG 画像の出力先のディレクトリなのだ～🌱

テンプレートの中の `assets.js` のような相対パスは、テンプレートのあるディレクトリを基準に解決されるのだ～🌱
構成 jsonl の空行は、無視するのだ～🌱
`outDir` が、空のディレクトリでも、存在しないパスでもないときは、エラーを出して止まるのだ～🌱
引数が 3 個でないときは、使い方を出して、終了コード 1 で終わるのだ～🌱

`render.sh` は、不足しているものを、次のように準備するのだ～🌱

- `node_modules` が無ければ、このディレクトリで `npm install` を実行するのだ～🌱
- `CHROMIUM_PATH` が指定されていなくて、`/tmp/chromium` も無ければ、`setup_chromium.js` を実行するのだ～🌱

## テンプレートが満たすべき要件なのだ～🌱

レンダラーがテンプレートに求めるのは、次の 2 個だけなのだ～🌱

1. 読み込みが終わったら、`window.__ready` を `true` にするのだ～🌱
2. `window.applyFrame(cfg)` という関数を持つのだ～🌱

`cfg` は、構成 jsonl の 1 行を解析した、1 フレーム分の構成のオブジェクトなのだ～🌱
`applyFrame(cfg)` は、その構成のとおりに、ページの DOM とスタイルを書き換えるのだ～🌱
同じ `cfg` を渡したときは、ページが同じ見た目にならなきゃだめなのだ～🌱

## レンダリングの流れなのだ～🌱

レンダラーは、構成 jsonl を先頭から 1 行ずつ `applyFrame` に渡して、1 行につき 1 枚のスクリーンショットを撮るのだ～🌱
連続する行の構成を正規化した JSON が同じなら、スクリーンショットを撮らずに、前のフレームの画像をコピーするのだ～🌱
正規化では、オブジェクトのキーを辞書順に並べるから、キーの順番だけが違う構成も同じとみなすのだ～🌱
だから、変化の少ない場面ほど、レンダリングが速く終わるのだ～🌱

## 環境変数なのだ～🌱

| 変数 | 既定 | 説明 |
| --- | --- | --- |
| `CHROMIUM_PATH` | `/tmp/chromium` | Chrome か Chromium の実行ファイルのパスなのだ～🌱 |
| `CHROMIUM_LD_PATH` | なし | Chromium を起動するときに、`LD_LIBRARY_PATH` の先頭に加えるパスなのだ～🌱 |
| `VIDEO_WIDTH` | `1280` | ビューポートの幅のピクセル数なのだ～🌱 |
| `VIDEO_HEIGHT` | `720` | ビューポートの高さのピクセル数なのだ～🌱 |

インストール済みの Chrome を使うときは、その実行ファイルのパスを `CHROMIUM_PATH` に指定するのだ～🌱

## Chromium の準備なのだ～🌱

Chrome が無い環境では、`node setup_chromium.js` で、Chromium を `/tmp/chromium` に展開できるのだ～🌱
展開する Chromium は、`npm install` でインストールされる `@sparticuz/chromium` のパッケージに含まれているものなのだ～🌱

root 権限が無くて、NSS 系の共有ライブラリが OS に無い環境では、この Chromium の起動が失敗することがあるのだ～🌱
`libnss3.so` のような共有ライブラリが、見つからないからなのだ～🌱
NSS 系の共有ライブラリは、`@sparticuz/chromium` のパッケージに含まれる `bin/al2023.tar.br` を展開すると得られるのだ～🌱
`bin/al2023.tar.br` は、Brotli で圧縮された tar アーカイブなのだ～🌱
展開した `.so` ファイルを 1 個のディレクトリに集めて、そのパスを `CHROMIUM_LD_PATH` に指定するのだ～🌱

## 依存なのだ～🌱

`npm install` で、`puppeteer-core` と `@sparticuz/chromium` がインストールされるのだ～🌱

## このファイル自体の編集ルールなのだ～🌱

このファイルは、1 行を全角 70 文字までにして、次のスキルを厳守して書くのだ～🌱

- [markdown-max-line-length](https://github.com/MirrgieRiana/MirrgieRiana.github.io/blob/main/.claude/skills/markdown-max-line-length/SKILL.md)
