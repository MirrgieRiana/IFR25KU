package miragefairy2024.client.mod

import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.Color
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.core.VerticalAlignment
import miragefairy2024.client.util.horizontalSpace
import miragefairy2024.client.util.verticalScroll
import miragefairy2024.client.util.verticalSpace
import miragefairy2024.mod.IfrEncyclopediaEntryCard
import miragefairy2024.mod.IfrEncyclopediaRecipeViewerCategoryCard
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.common.guiBackToGameTranslation
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import net.minecraft.network.chat.CommonComponents

class IfrEncyclopediaPageScreen(private val card: IfrEncyclopediaEntryCard) : BaseOwoScreen<FlowLayout>(text { IfrEncyclopediaRecipeViewerCategoryCard.translation() }) {
    override fun createAdapter(): OwoUIAdapter<FlowLayout> = OwoUIAdapter.create(this, Containers::verticalFlow)

    override fun build(rootComponent: FlowLayout) {
        rootComponent.apply {
            surface(Surface.VANILLA_TRANSLUCENT)
            padding(Insets.of(20, 4, 0, 0))
            verticalAlignment(VerticalAlignment.CENTER)
            horizontalAlignment(HorizontalAlignment.CENTER)

            child(Containers.verticalFlow(Sizing.fixed(200), Sizing.expand()).apply { // カード外枠
                surface(NinePatchTextureCard.IFR_ENCYCLOPEDIA_BACKGROUND.surface)
                padding(Insets.of(5))

                // 掲げられたアイテム
                child(Containers.stack(Sizing.fill(), Sizing.content()).apply {
                    horizontalAlignment(HorizontalAlignment.CENTER)
                    card.itemStacksGetter().firstOrNull()?.let { child(Components.item(it)) }
                })

                child(verticalSpace(5))

                // 本文
                child(verticalScroll(Sizing.fill(), Sizing.expand(), 5, overlapped = true).apply {
                    scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xA0FFFFFF.toInt())))
                    child().child(Components.label(text { card.textTranslation() }).apply {
                        sizing(Sizing.fill(), Sizing.content())
                        horizontalTextAlignment(HorizontalAlignment.LEFT)
                    })
                })

            })

            child(verticalSpace(10))

            child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                child(Components.button(text { guiBackToGameTranslation() }) {
                    minecraft!!.setScreen(null)
                }.apply {
                    horizontalSizing(Sizing.fixed(80))
                })
                child(horizontalSpace(20))
                child(Components.button(CommonComponents.GUI_BACK) {
                    onClose()
                }.apply {
                    horizontalSizing(Sizing.fixed(80))
                })
            })

        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (super.keyPressed(keyCode, scanCode, modifiers)) return true
        if (minecraft!!.options.keyInventory.matches(keyCode, scanCode)) {
            onClose()
            return true
        }
        return false
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (super.mouseClicked(mouseX, mouseY, button)) return true
        onClose()
        return true
    }
}
