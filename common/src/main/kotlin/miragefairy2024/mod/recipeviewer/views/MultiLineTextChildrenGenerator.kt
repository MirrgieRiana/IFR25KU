package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.ChildrenGenerator
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import net.minecraft.network.chat.Component

class MultiLineTextChildrenGenerator<P>(val text: Component, val positionFunction: (TextView) -> P) : ChildrenGenerator<P> {
    override fun generateChildren(renderingProxy: RenderingProxy, regionSize: IntPoint): List<Child<P, *>> {
        return renderingProxy.wrapText(text, regionSize.x)
            .map { TextView(it) }
            .map { Child(positionFunction(it), it) }
    }
}
