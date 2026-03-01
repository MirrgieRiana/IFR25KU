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
- `maven.parchmentmc.org` （Parchmentマッピング、302リダイレクト応答のみ返却）
- `raw.githubusercontent.com`

以下のホストは **ブロック** されています：

- `ldtteam.jfrog.io` （ `maven.parchmentmc.org` のリダイレクト先）
- `maven.wispforest.io` 、 `maven.shedaniel.me` 、 `maven.terraformersmc.com` 、 `www.cursemaven.com` （サードパーティmod依存関係）
- `maven.neoforged.net` 、 `maven.su5ed.dev` 、 `maven.blamejared.com` （NeoForge関連）
- `launchermeta.mojang.com` 、 `resources.download.minecraft.net`

### genSourcesの実行

`genSources` は **`--configure-on-demand` フラグとスタブ依存関係を使用することで実行可能** です。

1. Parchmentマッピングは `maven.parchmentmc.org` → `ldtteam.jfrog.io` のリダイレクト先がブロックされているため、ローカルの `maven/` ディレクトリに最小限の空のParchment ZIPを配置して回避します。
2. サードパーティmod依存関係（owo-lib、REI、cloth-config、Jade等）のMavenリポジトリがブロックされているため、空のスタブJAR/POMをローカルの `maven/` ディレクトリに配置して回避します。
3. `--configure-on-demand` フラグにより、NeoForgeプロジェクトの構成をスキップします（NeoForge userdev jarには実際のメタデータが必要なため）。
4. Java 21が必要です（ `JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64` ）。

実行例（スタブ配置後）：
```
JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64 ./gradlew :common:genSources --no-daemon --configure-on-demand
```

### unpackSourcesの実行

`unpackSources` も同様の条件で **実行可能** です。Minecraft以外の外部依存関係のソースは正常に展開されます。ただし、Minecraftの逆コンパイル済みソースは標準のsources artifactとして公開されないため、 `[Failed] Could not resolve component: net.minecraft:minecraft:1.21.1` という警告が出ます。

### compileKotlinの実行

`compileKotlin` はスタブ依存関係では実際のコードが含まれないため、コンパイルエラーが発生します。実際の依存関係がローカルにミラーされている場合は動作する可能性があります。

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

# コーディングAIはPRにおいてgeneratedな付属的リソースをコミットしてはいけません

datagenするとそのような差分が生じますが、無視してください。
