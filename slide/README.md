# slide

スライド生成ツールのビルド一式なのだぁ～🌱✨

## ビルド方法なのだぁ～🌱

ビルドの実行は、以下のコマンドで行うのだぁ🌱：

```sh
./build.sh
```

---

このスクリプトがやってることなのだぁ🌱：

- `src/site/resources/` の素材を `build/site/` にコピーするのだぁ🌱
- `src/main/xa1/global.xa1` を読み込んで、共有の土台（テンプレート・ページ生成関数・CSS行）を登録するのだぁ🌱
- `src/plugins/xa1/*.xa1` を読み込んで、プラグインのテンプレートとCSS行を登録するのだぁ🌱
- `src/pages/xa1/*.xa1` を読み込んで、ページ生成関数を登録するのだぁ🌱
- 登録されたページ生成関数を回して、 `build/site/` に HTML を書き出すのだぁ🌱
- 組み上げた CSS を `build/site/style.css` に書き出すのだぁ🌱

xarpite ランタイムは、リポジトリに同梱されている `../xarpite` を相対パスで使うのだぁ～🌱

## ディレクトリ構成

- `build.sh` - ビルドのエントリポイントなのだぁっ🌱♪
- `src/main/xa1/global.xa1` - 共有の土台（`templates`・`pageGenerators`・`cssLines` などの登録簿とヘルパー）なのだぁ🌱
- `src/plugins/xa1/*.xa1` - パーツ（素材・矢印・見出しなど）なのだぁ🌱。`templates` と `cssLines` へ登録するのだぁ🌱
- `src/pages/xa1/*.xa1` - 出力ページ。`pageGenerators` へ「出力ファイル名 → 内容を返す関数」を登録するのだぁ🌱。スライドは `slides.xa1` に4枚まとめて入っているのだぁ🌱
- `src/site/resources/` - 素材（テクスチャ png・絵文字 svg）なのだぁ🌱。
- `build/` - 生成物と、そこへ配置された素材・レンダリング済みスライド画像なのだぁ～🌱

## ライセンスについて

`build/` の成果物や `src/site/resources/` の素材を含め、**外部ファイルをコミットする際は、そのファイルのライセンス条項を満たすように記述しなければならない**のだぁ🌱
新しい素材を足すときは、必ず下の一覧に、その出典・ライセンス・遵守に必要な表記を追記するのだぁ🌱

以下は、このディレクトリに含まれるすべての外部リソースの一覧なのだぁ🌱。

### 画像テクスチャ（IFR25KU 自身のアイテムテクスチャ由来）

次のテクスチャは、`common/src/main/resources/assets/miragefairy2024/textures/item/` のドット絵を、16×16のまま素材としてそのままコピーしたものなのだぁ🌱
表示のときに、CSSで補間なしに拡大して使うのだぁ〜🌱

| ファイル | 由来 | ライセンス |
| --- | --- | --- |
| `src/site/resources/sphere.png` | `sphere_base.png` | 本リポジトリの [ルート README](../README.md) の規定に従う |
| `src/site/resources/fluorite.png` | `fluorite.png` | 同上 |
| `src/site/resources/nephrite_builders_rod.png` | `nephrite_builders_rod.png` | 同上 |

これらは本リポジトリ自身のリソースを流用したもので、ルート README の「Other Resources」等の規定でカバーされるのだぁ🌱
ルート README で名指し除外されている MirageFairy2019 由来（CC BY-SA 3.0）のテクスチャは、いずれも含んでいないのだぁ🌱
`build/site/` 内の同名 png は、ビルドがこれらをコピーしたものなのだぁ🌱

### マスコット画像（IFR25KU のマスコット原画）

`src/site/resources/fairy_face.png` は、`site/src/main/resources/favicon.png`（IFR25KU のマスコット原画）を、192×192のままそのままコピーしたものなのだぁ🌱
これも本リポジトリ自身のリソースなので、ルート README の規定でカバーされるのだぁ〜🌱
`build/site/fairy_face.png` は、ビルドがこれをコピーしたものなのだぁ🌱

### 絵文字 SVG（Fluent UI Emoji, MIT）

`src/site/resources/` の次の SVG は、Microsoft の [Fluent UI Emoji](https://github.com/microsoft/fluentui-emoji)（MIT ライセンス）由来なのだぁ🌱

- `fire.svg`
- `gem.svg`
- `green_circle.svg`
- `magic_wand.svg`
- `seedling.svg`
- `sparkles.svg`
- `tulip.svg`

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

### フォント（Zen Maru Gothic, コミットせず CDN 取得）

本文フォントの Zen Maru Gothic（SIL Open Font License 1.1）は、**リポジトリにはコミットしない**のだぁ🌱
代わりに、`slides.html` の `<head>` から Google Fonts の CDN で動的に読み込むのだぁ〜🌱
再配布物として同封しないので、このディレクトリには含まれていないのだぁ🌱
フォントの入手先は、次のリンクなのだぁ🌱

- Zen Maru Gothic: https://fonts.google.com/specimen/Zen+Maru+Gothic
