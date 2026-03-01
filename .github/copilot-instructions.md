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

`maven.blamejared.com` にJEIアーティファクトが存在しない（404）ため、NeoForgeプロジェクトの構成が失敗します。

### 根本解決：JEIのローカルミラー

EMIと同様に、JEIもローカルの `maven/` ディレクトリにミラーリングすることで、 `maven.blamejared.com` が利用不可能な環境でもNeoForgeプロジェクトが正常に構成されます。ユーザーに `mirrorMaven` タスクにJEI座標を追加し実行するよう依頼してください。ミラーリング済みのアーティファクトがコミットされていれば、以下のすべてのGradleタスクはワークアラウンドなしで動作します。

### JEIがミラーリングされていない場合のワークアラウンド

JEIのスタブをローカルの `maven/` ディレクトリに作成することで、NeoForgeプロジェクトを構成可能にできます。以下のコマンドでスタブを作成してください：

```
JEI_VERSION=$(grep '^jei ' gradle/libs.versions.toml | sed 's/.*= *"//;s/"//')
JEI_STUB_TMP=$(mktemp -d)
for artifact in jei-1.21.1-common-api jei-1.21.1-neoforge-api jei-1.21.1-neoforge; do
  dir="maven/mezz/jei/${artifact}/${JEI_VERSION}"
  mkdir -p "$dir"
  cat > "$dir/${artifact}-${JEI_VERSION}.pom" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"><modelVersion>4.0.0</modelVersion><groupId>mezz.jei</groupId><artifactId>${artifact}</artifactId><version>${JEI_VERSION}</version></project>
EOF
  mkdir -p "$JEI_STUB_TMP/META-INF"
  echo "Manifest-Version: 1.0" > "$JEI_STUB_TMP/META-INF/MANIFEST.MF"
  (cd "$JEI_STUB_TMP" && jar cf "$OLDPWD/$dir/${artifact}-${JEI_VERSION}.jar" META-INF/MANIFEST.MF)
done
rm -rf "$JEI_STUB_TMP"
```

スタブを作成すれば、NeoForgeプロジェクトを含むすべてのGradleタスクがワークアラウンドなしで動作します。スタブは `maven/mezz/` ディレクトリに作成されます。 **作業完了後、 `maven/mezz/` ディレクトリを必ず削除してください。スタブ自体は第三者のコードを含みませんが、本物のアーティファクトと誤認されないように清掃が必要です。**

```
JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64 ./gradlew :common:genSources --no-daemon
JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64 ./gradlew :fabric:runDatagen --no-daemon
```

なお、 `unpackSources` では `[Failed] Could not resolve component: net.minecraft:minecraft:1.21.1` という警告が出ますが、Minecraftの逆コンパイル済みソースは標準のsources artifactとして公開されないため、正常な動作です。

# コードスタイル

原則として、あなたが記述するコードと同様なコードをリポジトリ内から探し、それらとのあらゆる観点でのコードスタイルの一貫性を最大限に尊重してください。
最大行長は無限です。行の長さのみを理由にしてコードを折り返してはいけません。

# generatedリソース

generatedリソースは手動ではなく `./gradlew datagen` によって生成してください。
コードを変更したら、作業終了前に必ずgeneratedリソースの再生成をしてください。
generatedリソースに差分が生じたら、仕上げにそれをコミットしてください。

# 絶対に、依存ライブラリなどの第三者の著作物を独断でコミットしてはいけません

それをすると、そのブランチごと削除しなければならなくなります。
