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

### ネットワーク接続性

コーディングエージェントのサンドボックス環境では、以下のホストにアクセス **可能** です：

- `maven.architectury.dev` 、 `maven.fabricmc.net` 、 `maven.minecraftforge.net` （Architectury Loomプラグイン）
- `repo.maven.apache.org` （Maven Central）、 `plugins.gradle.org` （Gradle Plugin Portal）
- `libraries.minecraft.net` 、 `piston-meta.mojang.com` 、 `piston-data.mojang.com` （Minecraft本体）
- `maven.parchmentmc.org` 、 `ldtteam.jfrog.io` 、 `storage.googleapis.com` （Parchmentマッピング、 `maven.parchmentmc.org` から `ldtteam.jfrog.io` 、 `storage.googleapis.com` へのリダイレクトチェーン全体がアクセス可能）
- `maven.wispforest.io` （owo-lib）、 `maven.shedaniel.me` （REI、cloth-config）
- `raw.githubusercontent.com`

以下のホストは **ブロック** されています：

- `maven.terraformersmc.com` 、 `www.cursemaven.com` （EMI、Jade。ただしEMIはローカル `maven/` にミラー済み）
- `maven.neoforged.net` 、 `maven.su5ed.dev` 、 `maven.blamejared.com` （NeoForge関連）
- `launchermeta.mojang.com` 、 `resources.download.minecraft.net`

### genSourcesの実行

`genSources` は **`--configure-on-demand` フラグのみで実行可能** です。Parchmentマッピングを含むすべての依存関係がネットワーク経由で正常に解決されます。

1. `--configure-on-demand` フラグにより、NeoForgeプロジェクトの構成をスキップします（NeoForge関連のMavenリポジトリがブロックされているため）。
2. Java 21が必要です（ `JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64` ）。
3. Parchmentマッピング、owo-lib、REI、cloth-config等のサードパーティmod依存関係はすべてネットワーク経由で正常に解決されます。EMIはローカル `maven/` からの解決です。

実行例：
```
JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64 ./gradlew :common:genSources --no-daemon --configure-on-demand
```

### unpackSourcesの実行

`unpackSources` も同様の条件（ `--configure-on-demand` のみ）で **実行可能** です。Minecraft以外の外部依存関係のソースは正常に展開されます。ただし、Minecraftの逆コンパイル済みソースは標準のsources artifactとして公開されないため、 `[Failed] Could not resolve component: net.minecraft:minecraft:1.21.1` という警告が出ます。

### compileKotlinの実行

`compileKotlin` も同様の条件（ `--configure-on-demand` のみ）で **実行可能** です。サードパーティmod依存関係の実体がネットワーク経由またはローカルmavenから正常に解決されるため、 `:common:compileKotlin` は成功します。

### datagenの実行

datagen（ `:fabric:runDatagen` ）は **追加のワークアラウンドにより実行可能** です。

1. `settings.gradle.kts` の `include("neoforge")` を一時的にコメントアウトします（NeoForge関連のMavenリポジトリがブロックされているため）。
2. `gradle.properties` の `enabled_platforms` を `fabric` のみに変更します。
3. Jade（ `curse.maven:jade-324717` ）のスタブJAR/POMをローカル `maven/` ディレクトリに配置します（ `www.cursemaven.com` がブロックされているため）。
4. `--configure-on-demand` は **使わない** でください。genSources等では必須ですが、datagenでは `--configure-on-demand` を使うとfabricが参照する `:common` の `transformProductionFabric` タスクが見つからずエラーになります。NeoForge除外により `--configure-on-demand` なしでもビルドが通ります。
5. datagen完了後、上記の変更をすべて元に戻してください。

## Zipファイルからのソースコード参照（実験結果）

ソースjar/zipが存在する場合、エージェントはそれらの中身を展開せずにコマンドで直接アクセスできます：

- `jar tf <file.jar>` ― ファイル一覧の表示
- `unzip -l <file.jar>` ― ファイル一覧の表示（サイズ付き）
- `unzip -p <file.jar> <path/to/File.java>` ― 個別ファイルの内容を標準出力に抽出
- `zipgrep "<pattern>" <file.jar>` ― jar内のファイル横断でのパターン検索

ローカルのmavenディレクトリに存在するEMIなどの外部依存関係のソースjarに対して有効です。

# コードスタイル

原則として、あなたが記述するコードと同様なコードをリポジトリ内から探し、それらとのあらゆる観点でのコードスタイルの一貫性を最大限に尊重してください。
最大行長は無限です。行の長さのみを理由にしてコードを折り返してはいけません。

# generatedリソース

generatedリソースは手動ではなく `./gradlew datagen` によって生成してください。
コードを変更したら、作業終了前に必ずgeneratedリソースの再生成をしてください。
generatedリソースに差分が生じたら、仕上げにそれをコミットしてください。

# 絶対に、依存ライブラリなどの第三者の著作物を独断でコミットしてはいけません

それをすると、そのブランチごと削除しなければならなくなります。
