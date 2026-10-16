package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.util.EventRegistry
import net.minecraft.network.chat.Component

/**
 * 文字の書かれた、押せるボタンのViewなのだ～🌱
 * ボタンのウィジェットそのものは、このViewを受け取ったバックエンドが用意するのだ～🌱
 */
class TextButtonView(size: IntPoint, val message: Component) : FixedWidgetView(size) {
    val onPress = EventRegistry<() -> Unit>()
}
