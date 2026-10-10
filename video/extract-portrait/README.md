# extract-portrait なのだ～🌱

立ち絵の PSD から、指定した ID のレイヤーを、透過 PNG として切り出すツールなのだ～🌱

## 使い方なのだ～🌱

```sh
bash extract-portrait.sh <char> <psd> <ids> <outDir>
```

`extract-portrait.sh` は、不足している依存を準備してから、レイヤーを切り出すのだ～🌱
引数のパスは、呼び出し元のカレントディレクトリを基準に解決するのだ～🌱

- `char` は、出力のログに出す名前なのだ～🌱
- `psd` は、読み込む PSD のパスなのだ～🌱
- `ids` は、切り出すレイヤーの ID を `,` で区切って並べたものなのだ～🌱
- `outDir` は、`<id>.png` の形で書き出す先のディレクトリなのだ～🌱

引数が 4 個でないときは、使い方を出して、終了コード 1 で終わるのだ～🌱
`ids` に PSD の中に無い ID があるときは、エラーを出して、終了コード 1 で終わるのだ～🌱

## レイヤー ID の体系なのだ～🌱

ID は、兄弟レイヤーの 1 始まりのインデックスを `-` で連結したものなのだ～🌱
グループも 1 個のインデックスを消費するのだ～🌱
これは、IFR 劇場の `layers.json` と同じ体系なのだ～🌱

## 出力なのだ～🌱

`ids` に並べた ID ごとに、`<outDir>/<id>.png` を書くのだ～🌱
どの PNG も、PSD の全身キャンバスと同じ寸法の、透過 PNG なのだ～🌱
レイヤーは、PSD の中の位置のまま、その寸法のキャンバスへ置かれるのだ～🌱
だから、切り出した PNG どうしは、重ねるときに位置を合わせる必要が無いのだ～🌱

## このファイル自体の編集ルールなのだ～🌱

このファイルは、1 行を全角 70 文字までにして、次のスキルを厳守して書くのだ～🌱

- [markdown-max-line-length](https://github.com/MirrgieRiana/MirrgieRiana.github.io/blob/main/.claude/skills/markdown-max-line-length/SKILL.md)
