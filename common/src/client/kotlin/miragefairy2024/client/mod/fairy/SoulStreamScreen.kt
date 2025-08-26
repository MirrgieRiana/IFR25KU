package miragefairy2024.client.mod.fairy

import com.mojang.blaze3d.platform.InputConstants
import io.wispforest.owo.ui.base.BaseOwoHandledScreen
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.core.VerticalAlignment
import miragefairy2024.client.util.SlotType
import miragefairy2024.client.util.inventoryNameLabel
import miragefairy2024.client.util.slotContainer
import miragefairy2024.client.util.verticalScroll
import miragefairy2024.client.util.verticalSpace
import miragefairy2024.mod.fairy.SoulStreamScreenHandler
import miragefairy2024.util.size
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory

class SoulStreamScreen(handler: SoulStreamScreenHandler, playerInventory: Inventory, title: Component) : BaseOwoHandledScreen<FlowLayout, SoulStreamScreenHandler>(handler, playerInventory, title) {

    // カーソルをインベントリ画面での位置に戻す
    private var isFirst = true
    override fun render(vanillaContext: GuiGraphics?, mouseX: Int, mouseY: Int, delta: Float) {
        if (isFirst) {
            isFirst = false
            if (lastMousePositionInInventory != null) {
                InputConstants.grabOrReleaseMouse(this.minecraft!!.window.window, InputConstants.CURSOR_NORMAL, lastMousePositionInInventory!!.first, lastMousePositionInInventory!!.second)
            }
        }
        super.render(vanillaContext, mouseX, mouseY, delta)
    }

    override fun createAdapter(): OwoUIAdapter<FlowLayout> = OwoUIAdapter.create(this, Containers::verticalFlow)
    override fun build(rootComponent: FlowLayout) {
        rootComponent.apply {
            surface(Surface.VANILLA_TRANSLUCENT)
            verticalAlignment(VerticalAlignment.CENTER)
            horizontalAlignment(HorizontalAlignment.CENTER)

            // GUIパネル外枠
            child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
                surface(Surface.PANEL)
                padding(Insets.of(7))

                // メインコンテナ
                child(Containers.verticalFlow(Sizing.fixed(18 * 9 + 18), Sizing.content()).apply {

                    child(inventoryNameLabel(title))

                    child(verticalSpace(3))

                    child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                        surface(Surface.tiled(SlotType.FAIRY.texture, 18, 18))
                        repeat(9) { index ->
                            child(slotContainer(slotAsComponent(9 * 3 + 9 + index), type = null))
                        }
                    })

                    child(verticalSpace(4))

                    child(verticalScroll(Sizing.fill(100), Sizing.fixed(18 * 5), 18).apply {
                        surface(Surface.tiled(SlotType.NORMAL.texture, 18, 18))
                        scrollbar(ScrollContainer.Scrollbar.vanilla())
                        scrollStep(18)
                        (9 until menu.soulStream.size).chunked(9).forEach { indices ->
                            child().child(Containers.horizontalFlow(Sizing.fill(100), Sizing.content()).apply {
                                indices.forEach { index ->
                                    child(slotContainer(slotAsComponent(9 * 3 + 9 + index), type = null))
                                }
                            })
                        }
                    })

                    child(verticalSpace(3))

                    child(inventoryNameLabel(menu.playerInventory.name))

                    child(verticalSpace(1))

                    // プレイヤーインベントリ
                    child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
                        surface(Surface.tiled(SlotType.NORMAL.texture, 18, 18))
                        repeat(3) { r ->
                            child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                                repeat(9) { c ->
                                    child(slotContainer(slotAsComponent(9 * r + c), type = null))
                                }
                            })
                        }
                    })
                    child(verticalSpace(4))
                    child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                        surface(Surface.tiled(SlotType.NORMAL.texture, 18, 18))
                        repeat(9) { c ->
                            child(slotContainer(slotAsComponent(9 * 3 + c), type = null))
                        }
                    })

                })

            })

        }
    }

    override fun renderLabels(context: GuiGraphics, mouseX: Int, mouseY: Int) = Unit

    private val onUpdateListeners = mutableListOf<() -> Unit>()
    private var lastUpdateEventNanoTime = 0L
    override fun containerTick() {
        super.containerTick()

        val now = System.nanoTime()
        if (now - lastUpdateEventNanoTime >= 100_000_000) {
            lastUpdateEventNanoTime = now
            onUpdateListeners.forEach {
                it()
            }
        }
    }

    // キー入力で閉じる
    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (soulStreamKeyMappingCard.keyMapping.matches(keyCode, scanCode)) {
            onClose()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

}
