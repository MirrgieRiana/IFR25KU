# Minecraft 26.1.2 の最小限の環境なのだ～🌱

ここにあるのは、Minecraft 26.1.2 向けのビルド環境を、最小限の MOD を新規に作って実測したときの、ビルドスクリプトの複製なのだ～🌱

本番の移行の設計の、参照用の資料なのだ～🌱
ここに置いてあるファイルは、ビルドには 1 個も参加しないのだ～🌱

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

`.kotlin_module` の扱いが、1.21.1 向けの `main` とは変わるのだ～🌱

| 項目 | `main` の Shadow | 26.1.2 の Shadow 9.2.2 |
| --- | --- | --- |
| ファイル名 | `META-INF/mirrg.kotlin.kotlin_module` のまま | `META-INF/net.mc26experiment_mirrg.kotlin_module` のまま |
| 中身のパッケージ名 | 元のまま | 変換先を指すのだ～🌱 |

`gson` と `slf4j` の版は、Minecraft が持ち込むものへ揃えるのだ～🌱
上の「Minecraft が提供するライブラリの版」の節の値を、`gradle.properties` へ置いてあるのだ～🌱

## 踏んだ落とし穴なのだ～🌱

Shadow のプラグインを、サブプロジェクトの `plugins` ブロックで版を書いて適用すると、Fabric Loom がクラスローダーの衝突で落ちるのだ～🌱
ルートで `apply false` を使って宣言して、サブプロジェクトでは版を書かずに適用するのだ～🌱

NeoForge 側では、common のソースセットを `mods { }` へ登録しないと、Mixin の設定ファイルが見つからずに起動が落ちるのだ～🌱
