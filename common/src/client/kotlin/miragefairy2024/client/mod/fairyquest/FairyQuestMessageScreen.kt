package miragefairy2024.client.mod.fairyquest

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
import miragefairy2024.MirageFairy2024
import miragefairy2024.client.mod.surface
import miragefairy2024.client.util.horizontalSpace
import miragefairy2024.client.util.verticalScroll
import miragefairy2024.client.util.verticalSpace
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.common.guiBackToGameTranslation
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component

class FairyQuestMessageScreen(private val parent: Screen, private val fairyQuestTitle: Component, private val fairyQuestMessage: Component, private val fairyQuestClient: Component, title: Component) : BaseOwoScreen<FlowLayout>(title) {
    override fun createAdapter(): OwoUIAdapter<FlowLayout> = OwoUIAdapter.create(this, Containers::verticalFlow)

    override fun build(rootComponent: FlowLayout) {
        rootComponent.apply {
            surface(Surface.VANILLA_TRANSLUCENT)
            padding(Insets.of(20, 4, 0, 0))
            verticalAlignment(VerticalAlignment.CENTER)
            horizontalAlignment(HorizontalAlignment.CENTER)

            // スクロールコンテナ
            child(verticalScroll(Sizing.fixed(18 * 15), Sizing.expand(), 10, overlapped = true).apply {
                scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xA0FFFFFF.toInt())))

                // 外枠装飾用パネル
                child().child(Containers.verticalFlow(Sizing.fill(100), Sizing.content()).apply {
                    surface(NinePatchTextureCard.FAIRY_QUEST_CARD_MESSAGE.surface)
                    padding(Insets.of(11))

                    // タイトル
                    child(Components.label(fairyQuestTitle).apply {
                        sizing(Sizing.fill(100), Sizing.content())
                        horizontalTextAlignment(HorizontalAlignment.CENTER)
                        color(Color.ofRgb(0x6B472E))
                    })

                    child(verticalSpace(11))

                    // 罫線装飾用パネル
                    child(Containers.verticalFlow(Sizing.fill(100), Sizing.content()).apply {
                        surface(Surface.tiled(MirageFairy2024.identifier("textures/gui/fairy_quest_card_line.png"), 11, 11))
                        padding(Insets.of(0, 1, 0, 0))

                        // メッセージテキストラベル
                        child(Components.label(fairyQuestMessage).apply {
                            sizing(Sizing.fill(100), Sizing.content())
                            color(Color.ofRgb(0x6B472E))
                        })

                    })

                    child(verticalSpace(11))

                    // 依頼人
                    child(Components.label(fairyQuestClient).apply {
                        sizing(Sizing.fill(100), Sizing.content())
                        horizontalTextAlignment(HorizontalAlignment.RIGHT)
                        color(Color.ofRgb(0x6B472E))
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

    override fun onClose() {
        minecraft!!.setScreen(parent)
    }
}
