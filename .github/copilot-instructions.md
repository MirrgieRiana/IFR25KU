# 展開されたソースコード

このリポジトリでは、Minecraftおよび、sourcesを提供しているすべての外部依存関係の難読化解除されたソースコードを収集し、展開するGradleタスクを持っています。
出力先などの詳細は、Gradleビルドスクリプトを参照してください。
あなたがこのリポジトリ上でAI Agentとして活動する際、それらが展開済みであることを一旦期待してください。

もしリポジトリに空でない `unpackedSources` ディレクトリが存在する場合、 **Minecraftの難読化解除されたソースコードも必ずその中にあります** 。
このとき、キーワード検索が期待に反してヒットしない場合、その原因は100%の確率であなたの探し方や検索語句が誤っていることにあります。
Minecraftのコードのうち一部が展開されていないという可能性も、決してありません。
あなたの探し方をあなたが工夫してください。資料は既にあります。

もし展開済みでない場合、あなたはそのタスクを、決してツールやコマンド呼び出し等によって直接自分で呼び出す試みをしてはなりません。
代わりに、ユーザーが手動でそのタスクを実行するように、自然言語を使って提案と説明を行ってください。

## Gradleタスクのエージェント環境での動作（実験結果）

コーディングエージェントのサンドボックス環境では、 `genSources` や `unpackSources` などのGradleタスクは **実行できません** 。
Architectury Loomプラグインの解決に必要なMavenリポジトリ（ `maven.architectury.dev` 、 `maven.fabricmc.net` 、 `maven.minecraftforge.net` ）と、標準的なMaven Central（ `repo.maven.apache.org` ）やGradle Plugin Portal（ `plugins.gradle.org` ）はサンドボックスからアクセス可能です。
Minecraftのメタデータ・ライブラリホスト（ `libraries.minecraft.net` 、 `piston-meta.mojang.com` 、 `maven.parchmentmc.org` ）もアクセス可能で、プラグインの解決と依存性の解決は成功します。
しかし、Minecraftクライアントjarのダウンロード元（ `piston-data.mojang.com` ）がブロックされているため、Minecraft本体のセットアップ段階で失敗します。
なお、 `launchermeta.mojang.com` と `resources.download.minecraft.net` もアクセスできません。

## Zipファイルからのソースコード参照（実験結果）

ソースjar/zipが存在する場合、エージェントはそれらの中身を展開せずにコマンドで直接アクセスできます：

- `jar tf <file.jar>` ― ファイル一覧の表示
- `unzip -l <file.jar>` ― ファイル一覧の表示（サイズ付き）
- `unzip -p <file.jar> <path/to/File.java>` ― 個別ファイルの内容を標準出力に抽出
- `zipgrep "<pattern>" <file.jar>` ― jar内のファイル横断でのパターン検索

ただし、 `genSources` が生成するMinecraftの逆コンパイル済みソースjarはサンドボックス内では生成不可能なため、この方法はMinecraftソースのアクセスには使用できません。
ローカルのmavenディレクトリに存在するEMIなどの外部依存関係のソースjarに対しては有効です。

# コードスタイル

原則として、あなたが記述するコードと同様なコードをリポジトリ内から探し、それらとのあらゆる観点でのコードスタイルの一貫性を最大限に尊重してください。
最大行長は無限です。行の長さのみを理由にしてコードを折り返してはいけません。

# コーディングAIはPRにおいてgeneratedな付属的リソースをコミットしてはいけません

datagenするとそのような差分が生じますが、無視してください。
