# slide

スフィアクラフト紹介スライドのビルド一式なのだぁ〜🌱✨

## ビルドのしかたなのだぁ〜🌱♪

ビルドは、次のコマンドでできちゃうのだぁ🌱：

```sh
./build.sh
```

---

このスクリプトがやってくれることなのだぁ🌱♪：

- `src/site/resources/` の素材を `build/site/` 直下に平坦にコピーするのだぁ🌱
- `src/main/xa1/global.xa1` を読み込んで、共有の土台（テンプレート・ページ生成関数・CSS行）を用意するのだぁ🌱
- `src/plugins/xa1/*.xa1` を読み込んで、プラグインのテンプレートとCSS行を登録するのだぁ🌱
- `src/pages/xa1/*.xa1` を読み込んで、ページ生成関数を登録するのだぁ🌱
- 登録されたページ生成関数を回して、 `build/site/` に HTML を書き出すのだぁ🌱
- 各プラグインが積んだ CSS 行をつないで、 `build/site/style.css` に書き出すのだぁ🌱♪

xarpite ランタイムは、リポジトリに同梱されている `../xarpite` を相対パスで使うのだぁ〜🌱

## ディレクトリ構成なのだぁ〜🌱♪

- `build.sh` - ビルドのエントリポイントなのだぁっ🌱♪
- `src/main/xa1/global.xa1` - 共有の土台（`templates`・`pageGenerators`・`cssLines` などの登録簿とヘルパー）なのだぁ🌱
- `src/plugins/xa1/*.xa1` - パーツ（素材・矢印・見出し・レイアウトなど）なのだぁ🌱。`templates` と `cssLines` へ登録するのだぁ〜
- `src/pages/xa1/*.xa1` - 出力ページなのだぁ🌱。`pageGenerators` へ「出力ファイル名 → 内容を返す関数」を登録するのだぁ〜。スライドは `sphere-crafting.xa1` に4枚まとめて入っているのだぁ🌱
- `src/site/resources/` - 素材なのだぁ🌱。ライセンスごとにサブディレクトリに仕分けてあるのだぁ〜
  - `src/site/resources/item-texture/` - アイテムテクスチャ由来の png なのだぁ🌱
  - `src/site/resources/fluentui-emoji/` - 絵文字の svg なのだぁ🌱
  - `src/site/resources/fairy_face.png` - マスコット画像なのだぁ🌱
- `build/` - 生成物なのだぁ〜🌱。素材は `build.sh` が `src/site/resources/` から `build/site/` 直下へ平坦にコピーするので、同名のファイルが並ぶのだぁ〜🌱

## ライセンスのことなのだぁ〜🌱♪

`build/` の成果物や `src/site/resources/` の素材を含めて、**外部ファイルをコミットするときは、そのファイルのライセンス条項を満たすように書かなきゃだめ**なのだぁ🌱
新しい素材を足すときは、必ず下の一覧に、その出典・ライセンス・遵守に必要な表記を書き足してほしいのだぁ〜🌱

以下は、このディレクトリに入っている、ぜんぶの外部リソースの一覧なのだぁ🌱♪

### 画像テクスチャ（IFR25KU 自身のアイテムテクスチャ由来）なのだぁ🌱

`src/site/resources/item-texture/` の png は、`common/src/main/resources/assets/miragefairy2024/textures/item/` にある同名のドット絵を、改変せずそのまま複製したものなのだぁ🌱♪
どれも本リポジトリ自身のリソースを流用したものだから、ルート README の「Other Resources」などの規定でカバーされるのだぁ〜🌱
ルート README で名指し除外されている MirageFairy2019 由来（CC BY-SA 3.0）のテクスチャは、ひとつも含んでいないのだぁ🌱

### マスコット画像（IFR25KU のマスコット原画）なのだぁ🌱

`src/site/resources/fairy_face.png` は、`site/src/main/resources/favicon.png`（IFR25KU のマスコット原画）を、改変せずそのまま複製したものなのだぁ🌱♪
これも本リポジトリ自身のリソースだから、ルート README の規定でカバーされるのだぁ〜🌱

### 絵文字 SVG（Fluent UI Emoji, MIT）なのだぁ🌱

`src/site/resources/fluentui-emoji/` の svg は、Microsoft の Fluent UI Emoji（MIT ライセンス）由来なのだぁ🌱♪
入手元は、次のリンクなのだぁ🌱

- https://github.com/microsoft/fluentui-emoji

MIT ライセンスは、著作権表示と許諾表示を複製物に含めることを求めているので、その全文を以下に同封するのだぁ🌱

```
    MIT License

    Copyright (c) Microsoft Corporation.

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE
```

### フォント（Zen Maru Gothic, コミットせず CDN 取得）なのだぁ🌱

本文フォントの Zen Maru Gothic（SIL Open Font License 1.1）は、**リポジトリにはコミットしない**のだぁ🌱♪
代わりに、`slides.html` の `<head>` から Google Fonts の CDN で動的に読み込むのだぁ〜🌱
再配布物として同封しないので、このディレクトリには入っていないのだぁ🌱
フォントの入手先は、次のリンクなのだぁ🌱

- Zen Maru Gothic: https://fonts.google.com/specimen/Zen+Maru+Gothic
