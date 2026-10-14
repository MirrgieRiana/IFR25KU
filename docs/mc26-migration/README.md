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

## 前提なのだ～🌱

| 項目 | 版 |
| --- | --- |
| JDK | 25 |
| Gradle | 9.8.1 |
| Fabric Loom | `net.fabricmc.fabric-loom` の `1.18.4` |
| ModDevGradle | `net.neoforged.moddev` の `2.0.148` |
| Shadow | `com.gradleup.shadow` の `9.2.2` |
| Kotlin | 2.4.21 |

マッピングは、26.1 系が難読化を持たないから、1 行も要らないのだ～🌱

## 踏んだ落とし穴なのだ～🌱

Shadow のプラグインを、サブプロジェクトの `plugins` ブロックで版を書いて適用すると、Fabric Loom がクラスローダーの衝突で落ちるのだ～🌱
ルートで `apply false` を使って宣言して、サブプロジェクトでは版を書かずに適用するのだ～🌱

NeoForge 側では、common のソースセットを `mods { }` へ登録しないと、Mixin の設定ファイルが見つからずに起動が落ちるのだ～🌱
