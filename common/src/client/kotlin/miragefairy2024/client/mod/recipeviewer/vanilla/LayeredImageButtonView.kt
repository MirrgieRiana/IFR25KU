package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.ObservableValue
import net.minecraft.client.gui.components.WidgetSprites

/**
 * 背景と前景の 2 枚のスプライトを重ねた、押せるボタンの View なのだ～🌱
 * ボタンのウィジェットそのものは、この View を受け取ったバックエンドが用意するのだ～🌱
 */
class LayeredImageButtonView(
    size: IntPoint,
    val backgroundSprites: WidgetSprites,
    val foregroundSprites: WidgetSprites,
) : FixedWidgetView(size) {

    /** 押すごとに切り替わる状態を持つかなのだ～🌱 持つ場合は、スプライトもこの状態で選ぶのだ～🌱 */
    var toggle = false

    /** [miragefairy2024.client.mod.recipeviewer.vanilla.LayeredImageButtonView.toggle] が有効な場合の、現在の状態なのだ～🌱 */
    val value = ObservableValue(false)

    val onPress = EventRegistry<() -> Unit>()

}
