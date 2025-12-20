# mfku-wiki ダンプ概要

- 参照リポジトリ: https://github.com/MirrgieRiana/mfku-wiki（`main` を浅い clone）
- 利用ダンプ: `dumps/d3e9ff84-dce7-4b4a-a71c-dedc68b508dd.tar.gz`

## ビルドパイプライン
- `unpackWikiDump`: ダンプを展開（EUC-JP をタイトル・本文ともにデコード）。
- `generateDumpText`: 全ページを連結した `all.wiki.txt` を生成。
- `generateDumpJson`: タイトル→本文のマップを `all.wiki.json` として生成（整形 JSON）。
- `dumpWikiDirectory` → `generateWikiDirectory`: ページごとに UTF-8 の `wiki/<タイトル>.wiki.txt` を出力。タイトルに `:` で始まる特別ページはスキップし、ファイル名は禁止文字をエスケープ。

## コミット済み Wiki データの概要
- ページ数: 188
- 主なカテゴリ
  - フロント・メニュー類: FrontPage, MenuBar, SideMenu, InterWiki*
  - プロジェクト概要: IFR25KU, IFR25KU Server, MF24KU, MirageFairy2019/2023/2024, 「MirageFairyのすべてが分かるページ」
  - ゲームプレイ/データ: Tier, Glossary、各種アイテム・素材・装置（例: アカーシャ系道具、オーラ反射炉、ウィスプ など）
  - ロア/組織: みらなぎ聖騎士団、アストラル、エーテル
  - ユーティリティ: RecentChanges/Created/Deleted, SandBox ほか
- 本文は PukiWiki 構文のまま保持され、タイトルが2文字以上のページは明示的な wiki 記法がなくてもシステム側で自動リンクされる。
