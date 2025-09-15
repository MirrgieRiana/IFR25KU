package ifr25ku.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * IFR25KU 用のビルドロジックプラグイン。
 * いまはクラスパス提供のみ（buildSrc 相当）で、特別な設定は行いません。
 */
class BuildLogicPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // ここでは特に何もしません。
        // build-logic のユーティリティ（トップレベル関数やクラス）を
        // ビルドスクリプトから参照できるようにする目的です。
    }
}
