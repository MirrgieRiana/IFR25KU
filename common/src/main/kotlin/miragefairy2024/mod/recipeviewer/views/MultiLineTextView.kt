package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.util.ObservableValue
import mirrg.kotlin.helium.CachingEvaluator
import net.minecraft.network.chat.Component

class MultiLineTextView : AbstractView(), PlaceableView {

    val text = ObservableValue<Component>(Component.empty())

    override val sizingX = Sizing.FILL
    override val sizingY = Sizing.WRAP

    var color = ColorPair.DEFAULT
    var shadow = true
    var alignmentX = Alignment.START
    var lineSpacing = 2
    var tooltip: List<Component>? = null

    override fun initSizeCalculation(renderingProxy: RenderingProxy, regionSizeXProvider: () -> Int, regionSizeYProvider: () -> Int) {
        super.initSizeCalculation(renderingProxy, regionSizeXProvider, regionSizeYProvider)
        lines.reset()
    }

    val lines = CachingEvaluator {
        renderingProxy.wrapText(text.value, regionSizeXProvider())
    }

    override fun evaluateContentSizeXImpl() = 0
    override fun evaluateContentSizeYImpl(): Int {
        val lineCount = lines.evaluate().size
        return if (lineCount == 0) 0 else renderingProxy.getTextHeight() * lineCount + lineSpacing * (lineCount - 1)
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, offset.sized(evaluateActualSize()))

}
