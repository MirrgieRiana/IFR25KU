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

`preparePortedSources` は `common`、 `fabric`、 `neoforge` の Java・Kotlin ソースと一部の Mixin 設定を `ifr-common/build/portedSources` にコピーし、 `git apply` で `sources-26.1.2.patch` を適用するのだ～🌱 `-PutilitiesOnly` を付けると、生成したコピーの `util` 51 ファイルをコンパイルするのだ～🌱 `mirrg` は依存するサブプロジェクトとして扱い、実行には JDK 25 と Git が必要なのだ～🌱

```sh
./gradlew -Dorg.gradle.jvmargs=-Xmx2G -Pkotlin.compiler.execution.strategy=in-process -PutilitiesOnly -p docs/mc26-migration/ifr-common :compileKotlin
```

| 実行条件 | 結果 |
| --- | --- |
| `util` 51 ファイル | 29 秒で終了し、Kotlin は 923 件のエラー診断を返したのだ～🌱 |
| 作業用コピーで `ResourceLocation` を `Identifier` に置換 | 14 ファイルが変わり、811 件になったのだ～🌱 |
| 上記に加えて `Identifier.kt` の `ResourceKey.with` を `<T : Any>` に変更 | 808 件になり、`Identifier.kt` の診断は 0 件になったのだ～🌱 |
| 両方の変更を適用した `Identifier.kt` だけをコンパイル | 成功し、`IdentifierKt.class` が生成されたのだ～🌱 |
| `6efbd4e35` のパッチを適用した `util` 51 ファイル | 29 ファイルを修正し、Kotlin のエラー診断が 622 件になったのだ～🌱 |

診断数には、対象から外した `ModContext` や `DataGenerationEvents` への参照、そこから派生する型推論のエラーも含まれるのだ～🌱 `Identifier.kt`、 `ItemStack.kt`、 `NbtWrapper.kt`、 `TextScope.kt`、 `BlockEntity.kt` はパッチ適用後の実行で診断が 0 件だったのだ～🌱 `util` 全体のコンパイルには、 `Ingredient.EMPTY` を使う表示スロット、レシピ・タグ・ルートのデータ生成 API、クライアントの描画・モデル API などの対応も必要なのだぁ…🌧️


### モジュールを順に追加する実験なのだ～🌱

`-PmigrationSources` にパッケージルートからの相対パスやグロブをカンマ区切りで渡すと、選んだソースをコンパイルできるのだ～🌱 `Initialization.kt` と `MirageFairy2024.kt` に集まっていた定義から、全モジュールを初期化する処理とデータ生成のイベントを別ファイルへ移し、実コードの共通初期化だけを選べるようにしたのだ～🌱

```sh
./gradlew -p docs/mc26-migration/ifr-common '-PmigrationSources=miragefairy2024/Initialization.kt,miragefairy2024/MirageFairy2024.kt,miragefairy2024/util/Identifier.kt,miragefairy2024/mod/RecipeGroupModule.kt' :compileKotlin
```

| 実行条件 | 結果 |
| --- | --- |
| 共通初期化と `RecipeGroupModule` | 上のコマンドでコンパイルを確認したのだ～🌱 |
| 上記と NinePatch テクスチャ、パーティクル、ステータス効果、サウンド | 26.1.2 のクライアント側クラスを検証用に加えたクラスパスでコンパイルを確認したのだ～🌱 |
| 上記とタグ、ルートテーブル、データ添付、ブロックとアイテムのモデル生成、空の材料表示 | `-PmigrationClientClasspath` で Loom が処理した Minecraft のクラスを加え、共通コードのコンパイルを確認したのだ～🌱 |

ソース全体に Minecraft と Fabric API のクラスの改名を反映したのだ～🌱 戻り値が旧 `InteractionResultHolder` や `ItemInteractionResult` の実装は `InteractionResult` に移し、クライアントとサーバーで異なる成功結果を返す箇所は分岐を維持したのだ～🌱 パーティクルの座標には `Vec3.STREAM_CODEC` を使い、サウンドパケットは従来どおり識別子を送るのだ～🌱

タグ生成を `FabricTagsProvider` と `TagAppender` に移し、必須要素と任意のタグ参照を区別しているのだ～🌱 Fabric API の推移的なアクセス拡張を有効にして、ルートテーブルの生成メソッドや、Minecraft のクラスに追加されたデータ添付のインターフェースを使っているのだ～🌱

クラフト、精錬、溶鉱炉、石切台、加工機械のレシピ生成を、レジストリを受け取る `RecipeProvider` へ接続したのだ～🌱 精錬と溶鉱炉のレシピ分類は、1.21.1 の各判定を引き継いだのだ～🌱 空の加工機械用入力スロットは、`Ingredient` の番兵値から、材料が存在しない状態に移したのだ～🌱

特殊クラフトレシピでは、26.1.2 で廃止された `canCraftInDimensions` のコンパイルエラーを残しているのだぁ…🌧️ `CraftingInput` は材料の入った矩形を表すため、現在の `matches` に寸法判定を移すと、クラフト欄全体を判定していた旧実装と条件が変わるのだぁ…🌧️

モデル生成でクライアント側の型を参照する箇所には、ローダーごとのデータ生成と実行時クラスパスの検証が必要なのだ～🌱 `common` 全体は、Kotlin の解析中に診断を取得できず、対象を絞って検証を進めているのだぁ…🌧️

### ツール、バイオーム、ブロックエンティティ、Fabric のデータ生成なのだ～🌱

アイテムとブロックの登録時に ID をプロパティへ渡し、ツールの耐久値、採掘規則、修理材料を `ToolMaterial` とデータコンポーネントへ移したのだ～🌱 バイオームの色や燃焼速度は `EnvironmentAttributes` に移し、モブのスポーンの重みは `SpawnerData` とは別に渡すのだ～🌱

設置アイテムとインベントリを含む範囲のコンパイルを確認したのだ～🌱 ブロックエンティティの保存には `ValueInput` と `ValueOutput` を使い、従来の NBT キーとスロット番号を引き継ぐのだ～🌱 26.1.2 の Codec で、既存のブロックステート JSON 130 件を読み込み、スロット 3 と 200 を含む旧形式のインベントリを、同じ NBT に書き戻せることを確認したのだ～🌱

Fabric 側のデータ生成コードは `-PmigrationFabric` でコンパイル対象に含められるのだ～🌱 新しいモデルプロバイダーに対応し、アイテムレポートはレジストリのデフォルトコンポーネントから `reports/items.json` に集計するのだ～🌱 選択コンパイルではモジュールの初期化と `canCraftInDimensions` の診断が残っていて、データ生成の実行には追加の移植が必要なのだぁ…🌧️

| ソース | 診断を残している API の変更 |
| --- | --- |
| バイオームと魔法植物の群生 | `RandomPatchConfiguration` と `Feature.RANDOM_PATCH` ・ `Feature.FLOWER` が削除され、従来の試行回数と位置の選び方を保つには互換実装が必要なのだぁ…🌧️ |
| `ApostleWandItem.kt` | クラフト余剰アイテムの取得は `final` になり、同じアイテム自身を返す処理は旧方式でオーバーライドできなくなったのだぁ…🌧️ |

### 食品、加工機械の燃料、GUI 描画、樹木の配置なのだ～🌱

食品の満腹度と隠し満腹度は `FoodProperties` に置き、使用時間とステータス効果は `Consumable` に移したのだ～🌱 アタノールの燃料判定は、インベントリ、メニュー、レシピ表示のそれぞれでワールドの `FuelValues` を参照するのだ～🌱 カスタム材料は、新しい `CustomIngredient` のコーデックと `SlotDisplay` へ移し、表示に使うスタックに独自のデータコンポーネントを含めているのだ～🌱

`-PmigrationClientSources` にグロブやソースファイルのパスを渡すと、クライアント側も対象を選んで `:compileClientKotlin` と `:compileClientJava` を実行できるのだ～🌱 NinePatch とツールチップ背景の描画、GUI のアイテム装飾コールバックを `GuiGraphicsExtractor` に移したのだ～🌱 旧 View のマウスとキーの入力は、新しいイベント型で受け取るのだ～🌱 [Fabric の GUI 描画資料](https://docs.fabricmc.net/26.1.2/develop/rendering/gui/custom-screens)

ハイメヴィスカとプラスチックの木は、大小 4 種類の幹・葉の配置クラスを `WorldGenLevel` に移したのだ～🌱 幹の直下が置換可能なときにだけ、種類に応じて土か樹脂セメントを置くのだ～🌱

| ソース | 診断を残している API の変更 |
| --- | --- |
| `ViewContainerScreen.kt` | `imageWidth` と `imageHeight` はコンストラクターで決まる `final` フィールドになり、ルート View の計測後に画面寸法を書き換える既存の処理を移せないのだぁ…🌧️ スロットのハイライトも個別の非公開メソッドで描画するため、従来のクリッピングには別の変更が必要なのだぁ…🌧️ |

### 共通モジュールの一括解析とローダー別の検証なのだ～🌱

鉱石の圧縮・解凍レシピは、データ生成側のアイテムレジストリを使って材料タグを解決するのだ～🌱 バッグは新しいインベントリのアクセサーを使い、装備を参照するパッシブスキルは防具 4 スロットを従来の順で調べるのだ～🌱 魔法植物では `VegetationBlock` を継承し、ブロックエンティティの更新パケットには従来の NBT キーで特性とフラグを載せるのだ～🌱

Fabric の選択コンパイルで、エンチャント、ブロックのドロップ、食事の Mixin と、それらが使うプラットフォームプロキシの Kotlin・Java コンパイルを確認したのだ～🌱 エンチャントの照会と採掘のルートコンテキストは `ItemInstance` を受け取り、最後に食べた食品を記録するコールバックは `FoodProperties.onConsume` からスタックの消費前に呼ぶのだ～🌱 Fabric のブロックファミリー生成は、新しい `Variant` を Mixin のアクセサーで渡すのだ～🌱

ModDevGradle の `createMinecraftArtifacts` で NeoForge 26.1.2.117 のパッチ適用済み Minecraft を生成し、`PlatformProxy` と `NeoForgePlatformProxy` をそのクラスパスでコンパイルしたのだ～🌱 登録とクライアント初期化を含む NeoForge 本体のビルドには、共通モジュールとクライアント側の移植も要るのだぁ…🌧️ [NeoForge 26.1 の移行資料](https://docs.neoforged.net/primer/docs/26.1/neo/)

REI 26.1.819 の表示シリアライザーは、永続化用の `MapCodec` と通信の `StreamCodec` を登録するのだ～🌱 永続化時の `RecipeEntry` フィールドと、合成したレシピをロケーションから除外する条件を引き継いだのだ～🌱

`common/src/main` の Kotlin 379 ファイルを一括で解析し、モジュールの依存ファイルを含めた診断を取得できたのだ～🌱 残る箇所には、廃止された `DimensionType.natural` を参照する魔法植物とパッシブスキル、旧 2 色モデルを指定するカオスキューブのスポーンエッグ、26.1.2 の対応版を確認できていない EMI があるのだぁ…🌧️

### クライアントのパーティクル描画なのだ～🌱

樹液、硫黄の煙、花びら、引き寄せ魔法などを `SingleQuadParticle` に移したのだ～🌱 魔法陣は各層を表裏 2 枚の四角形に分け、色、透明度、光量と回転を描画状態に登録するのだ～🌱 スパークルの加算合成は専用の `RenderPipeline` で設定し、旧描画順とブレンド状態を操作していた両ローダーの Mixin を外したのだ～🌱

26.1.2 のクラスパスで、パーティクルのクライアント側コードを含む選択コンパイルが通ったのだ～🌱 ゲーム内での描画結果は未確認なのだぁ…🌧️

### 移行資料なのだ～🌱

- 26.1 系では JDK 25 と非リマップ版 Loom を使い、依存の宣言は `implementation` や `compileOnly` に移すのだ～🌱 今回はクリエイティブタブ、ディメンション移動イベント、燃料登録を、新しい Fabric API に置き換えたのだ～🌱 タグのデータ生成は `FabricPackOutput` と `FabricTagsProvider` の `TagAppender` に合わせる必要があるのだ～🌱 [Fabric の 26.1 移行案内](https://fabricmc.net/2026/03/14/261.html) [Fabric API の移行表](https://docs.fabricmc.net/de_de/develop/porting/26.1/fabric-api)
- 1.21.1 からの累積差分は、NeoForged のバージョン別資料で確認できるのだ～🌱 今回は `Identifier`、型付きのチャットイベント、 `InteractionResult` と、 `HolderGetter` でタグを解決する `Ingredient` の生成を適用したのだ～🌱 空のレシピ入力は `Optional<Ingredient>` で表せるので、 `IngredientStack.EMPTY` の置き換えには空スロットを扱う側も含めて変更が要るのだ～🌱 [識別子の改名](https://docs.neoforged.net/primer/docs/1.21.11/#resourcelocation-to-identifier) [材料と操作結果の移行](https://docs.neoforged.net/primer/docs/1.21.2/) [型付きチャットイベント](https://docs.neoforged.net/primer/docs/1.21.5/)
- 26.1 の進捗・レシピ生成には `ItemStackTemplate` を使う経路があり、レシピシリアライザーは `MapCodec` と `StreamCodec` の組を登録するのだ～🌱 モデル生成には、クライアント側の datagen ソースセットが必要なのだ～🌱 [NeoForged のバニラ API 移行資料](https://docs.neoforged.net/primer/docs/26.1/) [NeoForge 固有の変更](https://docs.neoforged.net/primer/docs/26.1/neo/) [Fabric のデータ生成設定](https://docs.fabricmc.net/develop/data-generation/setup)
