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

`maven.blamejared.com` にJEIアーティファクトが存在しない（404）ため、NeoForgeプロジェクトの構成が失敗します。以下の各Gradleタスクはこの制約のワークアラウンドを含みます。

### genSources / unpackSources / compileKotlinの実行

`--configure-on-demand` フラグにより、JEI依存の解決に失敗するNeoForgeプロジェクトの構成をスキップして実行できます。

```
JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64 ./gradlew :common:genSources --no-daemon --configure-on-demand
```

なお、 `unpackSources` では `[Failed] Could not resolve component: net.minecraft:minecraft:1.21.1` という警告が出ますが、Minecraftの逆コンパイル済みソースは標準のsources artifactとして公開されないため、正常な動作です。

### datagenの実行

`-Penabled_platforms=fabric` フラグにより、NeoForgeプロジェクトを除外して実行できます。 `--configure-on-demand` は **使わない** でください。

```
JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64 ./gradlew :fabric:runDatagen --no-daemon -Penabled_platforms=fabric
```

# コードスタイル

原則として、あなたが記述するコードと同様なコードをリポジトリ内から探し、それらとのあらゆる観点でのコードスタイルの一貫性を最大限に尊重してください。
最大行長は無限です。行の長さのみを理由にしてコードを折り返してはいけません。

# generatedリソース

generatedリソースは手動ではなく `./gradlew datagen` によって生成してください。
コードを変更したら、作業終了前に必ずgeneratedリソースの再生成をしてください。
generatedリソースに差分が生じたら、仕上げにそれをコミットしてください。

# 絶対に、依存ライブラリなどの第三者の著作物を独断でコミットしてはいけません

それをすると、そのブランチごと削除しなければならなくなります。
