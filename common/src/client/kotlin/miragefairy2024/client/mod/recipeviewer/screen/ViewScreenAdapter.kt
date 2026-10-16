package miragefairy2024.client.mod.recipeviewer.screen

import miragefairy2024.client.mod.recipeviewer.renderingProxy
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.View
import net.minecraft.client.gui.components.AbstractWidget

/**
 * Viewのツリーを、バニラの画面の子のウィジェットの列へ変換するのだ～🌱
 * [miragefairy2024.client.mod.recipeviewer.emi.EMI_VIEW_PLACER_REGISTRY]がレシピビューワーの枠へ置くのに対して、これはバニラの画面へ置くのだ～🌱
 */
class ViewScreenAdapter(private val rootView: View) {

    private val widgetList = mutableListOf<AbstractWidget>()

    /** [miragefairy2024.client.mod.recipeviewer.screen.ViewScreenAdapter.layout]で置かれたウィジェットなのだ～🌱 画面の子として登録するのだ～🌱 */
    val widgets: List<AbstractWidget> get() = widgetList

    /**
     * [offset]を左上として、[regionSize]の空間へViewのツリーを配置するのだ～🌱
     * [slotOrigin]は、バニラがメニューのスロットの座標の原点として使う点なのだ～🌱
     */
    fun layout(offset: IntPoint, regionSize: IntPoint, slotOrigin: IntPoint = IntPoint.ZERO) {
        widgetList.clear()
        rootView.calculateContentSize(renderingProxy)
        rootView.calculateActualSize(regionSize)
        val context = ScreenViewPlacerContext(widgetList, slotOrigin)
        rootView.attachTo(offset) { view, bounds ->
            SCREEN_VIEW_PLACER_REGISTRY.place(context, view, bounds)
        }
    }

}
