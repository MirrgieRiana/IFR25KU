package miragefairy2024.client.util

import net.minecraft.client.gui.components.WidgetSprites
import net.minecraft.client.gui.components.events.ContainerEventHandler
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.resources.ResourceLocation

fun WidgetSprites(identifier: ResourceLocation): WidgetSprites = WidgetSprites(identifier, identifier)

/**
 * フォーカスされた子をたどり、末端の要素を返すのだ～🌱
 * @param predicate 各コンテナで評価し、`false` なら探索を中断して `null` を返すのだ～🌱
 */
fun GuiEventListener.getActualFocused(predicate: (ContainerEventHandler) -> Boolean): GuiEventListener? {
    var listener = this
    while (listener is ContainerEventHandler) {
        if (!predicate(listener)) return null
        listener = listener.focused ?: break
    }
    return listener
}
