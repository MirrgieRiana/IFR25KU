# Minecraft 26.1.2 の最小限の環境なのだ～🌱

このディレクトリの直下には、Minecraft 26.1.2 向けのビルド環境を、最小限の MOD を新規に作って実測したときの、ビルドスクリプトの複製があるのだ～🌱

本番の移行の設計の、参照用の資料なのだ～🌱
`ifr-common` には、IFR 本体のソースを 26.1.2 でコンパイルするための独立したビルドがあるのだ～🌱

## ファイルの対応なのだ～🌱

| ここのファイル | 実験環境での位置 |
| --- | --- |
| `build.gradle.kts` | ルート |
| `settings.gradle.kts` | ルート |
| `gradle.properties` | ルート |
| `mirrg.build.gradle.kts` | `mirrg/build.gradle.kts` |
| `common.build.gradle.kts` | `common/build.gradle.kts` |
| `fabric.build.gradle.kts` | `fabric/build.gradle.kts` |
| `neoforge.build.gradle.kts` | `neoforge/build.gradle.kts` |

## 実測で通ったことなのだ～🌱

| 項目 | 結果 |
| --- | --- |
| Fabric 側のビルド | 通ったのだ～🌱 |
| NeoForge 側のビルド | 通ったのだ～🌱 |
| Dedicated Server の起動 | 両方とも通ったのだ～🌱 |
| client と common のソースセットの分割 | 両方とも通ったのだ～🌱 |
| Mixin の読み込み | 両方とも通ったのだ～🌱 |
| 配布用の単一の jar へのまとめ | 両方とも通ったのだ～🌱 |
| Kotlin のパッケージ名の変換 | 両方とも通ったのだ～🌱 1050 個のクラスが移ったのだ～🌱 |
| Architectury API の呼び出し | 両方とも通ったのだ～🌱 common と fabric と neoforge の 3 個から呼べるのだ～🌱 |
| `mirrg` に相当する共用サブプロジェクト | 両方とも通ったのだ～🌱 下の節を見るのだ～🌱 |

## 前提なのだ～🌱

| 項目 | 版 |
| --- | --- |
| JDK | 25 |
| Gradle | 9.8.1 |
| Fabric Loom | `net.fabricmc.fabric-loom` の `1.18.4` |
| ModDevGradle | `net.neoforged.moddev` の `2.0.148` |
| Shadow | `com.gradleup.shadow` の `9.2.2` |
| Kotlin | 2.4.21 |
| Architectury API | 20.1.17 |

マッピングは、26.1 系が難読化を持たないから、1 行も要らないのだ～🌱

## Minecraft が提供するライブラリの版なのだ～🌱

`mirrg.kotlin` のサブプロジェクトが gson と slf4j を使っているから、その版が移行の前後で変わるのだ～🌱

| Minecraft | gson | slf4j-api |
| --- | --- | --- |
| 1.21.1 | `2.10.1` | `2.0.9` |
| 26.1.2 | `2.13.2` | `2.0.17` |

出典は、`https://piston-meta.mojang.com/mc/game/version_manifest_v2.json` から辿った、各版の JSON の `libraries` なのだ～🌱

## `mirrg` に相当する共用サブプロジェクトなのだ～🌱

`mirrg.kotlin` は Minecraft のクラスを 1 個も使わないから、プラットフォームのプラグインを 1 個も要らないのだ～🌱
`kotlin("jvm")` だけのサブプロジェクトとして置いて、`common` から `api` で参照する形で組めるのだ～🌱

| 項目 | 結果 |
| --- | --- |
| gson と slf4j を `compileOnly` で参照するコンパイル | 両方とも通ったのだ～🌱 |
| `common` から関数を呼ぶコンパイル | 両方とも通ったのだ～🌱 |
| 配布用の jar への同梱 | 両方とも入ったのだ～🌱 |
| パッケージ名の変換 | 両方とも `mc26experiment/shadow/mirrg/kotlin/` 配下へ移ったのだ～🌱 |

`.kotlin_module` の扱いを、Gradle 9.8.1 への移行前の Minecraft 1.21.1 向けビルドと、26.1.2 の実験環境で比較したのだ～🌱

| 項目 | 1.21.1 の Shadow 8.1.1 | 26.1.2 の Shadow 9.2.2 |
| --- | --- | --- |
| ファイル名 | `META-INF/mirrg.kotlin.kotlin_module` のまま | `META-INF/net.mc26experiment_mirrg.kotlin_module` のまま |
| 中身のパッケージ名 | 元のまま | 変換先を指すのだ～🌱 |

`gson` と `slf4j` の版は、Minecraft が持ち込むものへ揃えるのだ～🌱
上の「Minecraft が提供するライブラリの版」の節の値を、`gradle.properties` へ置いてあるのだ～🌱

## 踏んだ落とし穴なのだ～🌱

Shadow のプラグインを、サブプロジェクトの `plugins` ブロックで版を書いて適用すると、Fabric Loom がクラスローダーの衝突で落ちるのだ～🌱
ルートで `apply false` を使って宣言して、サブプロジェクトでは版を書かずに適用するのだ～🌱

NeoForge 側では、common のソースセットを `mods { }` へ登録しないと、Mixin の設定ファイルが見つからずに起動が落ちるのだ～🌱

## IFR 本体の common をコンパイルする実験なのだ～🌱

`ifr-common` は、リポジトリ内の `common/src/main` と `mirrg.kotlin/src/main/kotlin` を参照する独立した Gradle ビルドなのだ～🌱 Minecraft 26.1.2 の非リマップ版 Loom を使うのだ～🌱

JDK 25 を `JAVA_HOME` に設定して、リポジトリのルートから次を実行するのだ～🌱 PowerShell では `./gradlew` を `.\gradlew.bat` に置き換えるのだ～🌱

```sh
./gradlew -Dorg.gradle.jvmargs=-Xmx4G -Pkotlin.compiler.execution.strategy=in-process -p docs/mc26-migration/ifr-common :compileKotlin
```

Fabric API、Architectury API、TerraBlender Fabric、REI 26.1.819 を解決し、Gson と SLF4J を `compileOnly` で参照する `mirrg` のコンパイルを通したのだ～🌱 REI 26.1.819 は 26.1.2 向けの Fabric と NeoForge で配布されているのだ～🌱 [REI の配布一覧](https://modrinth.com/mod/rei/versions)

IFR 本体のソースを一部ずつコンパイラーに渡し、`ResourceLocation`、`InteractionResultHolder`、`Tier` の参照が解決できないことを確認したのだぁ…🌧️ 26.1.2 のクラスパスには `Identifier`、`InteractionResult`、`ToolMaterial` があり、`Ingredient.of` が受け取る型も変わっていたのだぁ…🌧️

| IFR のソース | 26.1.2 への移植箇所 |
| --- | --- |
| `util/Identifier.kt` | リソース ID の型とファクトリー呼び出し |
| `util/DebugItem.kt` | アイテム使用時に返す `InteractionResultHolder` |
| `mod/tool/ToolMaterial.kt` | ツール素材を定義する `Tier` |
| `util/Ingredient.kt` | `ItemStack` や `TagKey<Item>` を直接渡している `Ingredient.of` |

### 機能モジュールを外して `util` だけをコンパイルするのだ～🌱

`-PutilitiesOnly` を付けると、 `preparePortedUtilities` が IFR 本体の `common/src/main/kotlin/miragefairy2024/util` から 51 ファイルを `ifr-common/build/portedUtilities` にコピーし、 `git apply` で `util-26.1.2.patch` を適用するのだ～🌱 Kotlin は生成されたコピーをコンパイルし、 `mirrg` は依存するサブプロジェクトとして扱うのだ～🌱 実行には JDK 25 と Git が必要なのだ～🌱

```sh
./gradlew -Dorg.gradle.jvmargs=-Xmx2G -Pkotlin.compiler.execution.strategy=in-process -PutilitiesOnly -p docs/mc26-migration/ifr-common :compileKotlin
```

| 実行条件 | 結果 |
| --- | --- |
| `util` 51 ファイル | 29 秒で終了し、Kotlin は 923 件のエラー診断を返したのだ～🌱 |
| 作業用コピーで `ResourceLocation` を `Identifier` に置換 | 14 ファイルが変わり、811 件になったのだ～🌱 |
| 上記に加えて `Identifier.kt` の `ResourceKey.with` を `<T : Any>` に変更 | 808 件になり、`Identifier.kt` の診断は 0 件になったのだ～🌱 |
| 両方の変更を適用した `Identifier.kt` だけをコンパイル | 成功し、`IdentifierKt.class` が生成されたのだ～🌱 |
| パッチを適用した `util` 51 ファイル | 29 ファイルを修正し、Kotlin のエラー診断が 622 件になったのだ～🌱 |

診断数には、対象から外した `ModContext` や `DataGenerationEvents` への参照、そこから派生する型推論のエラーも含まれるのだ～🌱 `Identifier.kt`、 `ItemStack.kt`、 `NbtWrapper.kt`、 `TextScope.kt`、 `BlockEntity.kt` はパッチ適用後の実行で診断が 0 件だったのだ～🌱 `util` 全体のコンパイルには、 `Ingredient.EMPTY` を使う表示スロット、レシピ・タグ・ルートのデータ生成 API、クライアントの描画・モデル API などの対応も必要なのだぁ…🌧️

### 移行資料なのだ～🌱

- 26.1 系では JDK 25 と非リマップ版 Loom を使い、依存の宣言は `implementation` や `compileOnly` に移すのだ～🌱 今回はクリエイティブタブ、ディメンション移動イベント、燃料登録を、新しい Fabric API に置き換えたのだ～🌱 タグのデータ生成は `FabricPackOutput` と `FabricTagsProvider` の `TagAppender` に合わせる必要があるのだ～🌱 [Fabric の 26.1 移行案内](https://fabricmc.net/2026/03/14/261.html) [Fabric API の移行表](https://docs.fabricmc.net/de_de/develop/porting/26.1/fabric-api)
- 1.21.1 からの累積差分は、NeoForged のバージョン別資料で確認できるのだ～🌱 今回は `Identifier`、型付きのチャットイベント、 `InteractionResult` と、 `HolderGetter` でタグを解決する `Ingredient` の生成を適用したのだ～🌱 空のレシピ入力は `Optional<Ingredient>` で表せるので、 `IngredientStack.EMPTY` の置き換えには空スロットを扱う側も含めて変更が要るのだ～🌱 [識別子の改名](https://docs.neoforged.net/primer/docs/1.21.11/#resourcelocation-to-identifier) [材料と操作結果の移行](https://docs.neoforged.net/primer/docs/1.21.2/) [型付きチャットイベント](https://docs.neoforged.net/primer/docs/1.21.5/)
- 26.1 の進捗・レシピ生成には `ItemStackTemplate` を使う経路があり、レシピシリアライザーは `MapCodec` と `StreamCodec` の組を登録するのだ～🌱 モデル生成には、クライアント側の datagen ソースセットが必要なのだ～🌱 [NeoForged のバニラ API 移行資料](https://docs.neoforged.net/primer/docs/26.1/) [NeoForge 固有の変更](https://docs.neoforged.net/primer/docs/26.1/neo/) [Fabric のデータ生成設定](https://docs.fabricmc.net/develop/data-generation/setup)
