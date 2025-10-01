package miragefairy2024.client.mod.fairyquest

import io.wispforest.owo.ui.base.BaseOwoHandledScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Color
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.OwoUIDrawContext
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.core.VerticalAlignment
import miragefairy2024.MirageFairy2024
import miragefairy2024.client.mod.surface
import miragefairy2024.client.util.ClickableContainer
import miragefairy2024.client.util.GhostItemComponent
import miragefairy2024.client.util.inventoryNameLabel
import miragefairy2024.client.util.slotContainer
import miragefairy2024.client.util.verticalScroll
import miragefairy2024.client.util.verticalSpace
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.fairyquest.FairyQuestCardScreenHandler
import miragefairy2024.mod.fairyquest.guiFairyQuestCardFullScreenTranslation
import miragefairy2024.util.invoke
import miragefairy2024.util.isNotEmpty
import miragefairy2024.util.orEmpty
import miragefairy2024.util.register
import miragefairy2024.util.text
import miragefairy2024.util.toItemStacks
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory

class FairyQuestCardScreen(handler: FairyQuestCardScreenHandler, private val playerInventory: Inventory, title: Component) : BaseOwoHandledScreen<FlowLayout, FairyQuestCardScreenHandler>(handler, playerInventory, title) {
    override fun createAdapter(): OwoUIAdapter<FlowLayout> = OwoUIAdapter.create(this, Containers::verticalFlow)

    private val onScreenUpdate = mutableListOf<() -> Unit>()
    private val onDrawTooltip = mutableListOf<(vanillaContext: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) -> Unit>()

    override fun build(rootComponent: FlowLayout) {
        rootComponent.apply {
            surface(Surface.VANILLA_TRANSLUCENT)
            verticalAlignment(VerticalAlignment.CENTER)
            horizontalAlignment(HorizontalAlignment.CENTER)

            // GUI外枠描画用
            child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
                surface(Surface.PANEL)
                padding(Insets.of(7))

                // 横幅固定メインコンテナ
                child(Containers.verticalFlow(Sizing.fixed(18 * 9), Sizing.content()).apply {

                    child(inventoryNameLabel(menu.recipe.title, HorizontalAlignment.CENTER))

                    child(verticalSpace(3))

                    // 本文
                    run {

                        // クリックしたらメッセージを全画面で表示する
                        child(ClickableContainer(Sizing.fill(100), Sizing.fixed(11 * 7 + 5)).apply {

                            onClick.register {
                                minecraft!!.setScreen(FairyQuestMessageScreen(this@FairyQuestCardScreen, menu.recipe.title, menu.recipe.message, menu.recipe.client, menu.recipe.title))
                                true
                            }

                            // 外枠装飾用パネル
                            child(Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100)).apply {
                                surface(NinePatchTextureCard.FAIRY_QUEST_CARD_MESSAGE.surface)
                                padding(Insets.of(11, 11, 11, 6))

                                // スクロールコンテナ
                                child(verticalScroll(Sizing.fill(100), Sizing.fill(100), 5).apply {

                                    // 罫線装飾用パネル
                                    child().child(Containers.verticalFlow(Sizing.fill(100), Sizing.content()).apply {
                                        surface(Surface.tiled(MirageFairy2024.identifier("textures/gui/fairy_quest_card_line.png"), 11, 11))
                                        padding(Insets.of(0, 1, 0, 0))

                                        // メッセージテキストラベル
                                        child(Components.label(menu.recipe.message).apply {
                                            sizing(Sizing.fill(100), Sizing.content())
                                            color(Color.ofRgb(0x6B472E))
                                        })

                                    })

                                })

                            })

                        }.apply {
                            tooltip(text { guiFairyQuestCardFullScreenTranslation() })
                        })

                    }

                    child(verticalSpace(3))

                    // 取引欄
                    child(Containers.horizontalFlow(Sizing.fill(100), Sizing.content()).apply {

                        repeat(4) { i ->
                            child(slotContainer(Containers.stack(Sizing.fixed(16), Sizing.fixed(16)).apply {
                                allowOverflow(true)
                                val index = 9 + 9 * 3 + i
                                child(slotAsComponent(index))
                                val input = menu.recipe.inputs.getOrNull(i)
                                val inputItemStacks = input?.invoke()?.toItemStacks() ?: listOf()
                                child(GhostItemComponent(inputItemStacks).apply {
                                    onScreenUpdate += { showItemStack = menu.getSlot(index).item.isEmpty }
                                    overlayColor = 0x20FF0000
                                    onScreenUpdate += {
                                        showOverlay = when {
                                            input == null -> false
                                            !input().ingredient.test(menu.getSlot(index).item) -> true
                                            menu.getSlot(index).item.count < input().count -> true
                                            else -> false
                                        }
                                    }
                                    onDrawTooltip += { vanillaContext: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float ->
                                        drawGhostTooltip(OwoUIDrawContext.of(vanillaContext), mouseX, mouseY, delta, Minecraft.getInstance().timer.realtimeDeltaTicks)
                                    }
                                })
                            }))
                        }

                        child(FairyQuestProgress().apply {
                            onScreenUpdate += {
                                setProgress(menu.progress / menu.recipe.duration.toDouble())
                            }
                        }.component)

                        repeat(4) { i ->
                            child(slotContainer(Containers.stack(Sizing.fixed(16), Sizing.fixed(16)).apply {
                                allowOverflow(true)
                                val index = 9 + 9 * 3 + 4 + i
                                child(slotAsComponent(index))
                                val outputItemStack = menu.recipe.outputs.getOrNull(i)
                                child(GhostItemComponent(outputItemStack?.invoke()?.let { listOf(it) } ?: listOf()).apply {
                                    onScreenUpdate += { showItemStack = menu.getSlot(index).item.isEmpty }
                                    overlayColor = 0x2000FF00
                                    onScreenUpdate += { showOverlay = outputItemStack?.invoke().orEmpty.isNotEmpty && menu.getSlot(index).item.isEmpty }
                                    onDrawTooltip += { vanillaContext: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float ->
                                        drawGhostTooltip(OwoUIDrawContext.of(vanillaContext), mouseX, mouseY, delta, Minecraft.getInstance().timer.realtimeDeltaTicks)
                                    }
                                })
                            }))
                        }

                    })

                    child(verticalSpace(3))

                    child(inventoryNameLabel(playerInventory.name))

                    child(verticalSpace(1))

                    // プレイヤーインベントリ
                    repeat(3) { r ->
                        child(Containers.horizontalFlow(Sizing.fill(100), Sizing.content()).apply {
                            repeat(9) { c ->
                                child(slotContainer(slotAsComponent(9 * r + c)))
                            }
                        })
                    }
                    child(verticalSpace(4))
                    child(Containers.horizontalFlow(Sizing.fill(100), Sizing.content()).apply {
                        repeat(9) { c ->
                            child(slotContainer(slotAsComponent(9 * 3 + c)))
                        }
                    })

                })

            })

        }
        onScreenUpdate.forEach {
            it()
        }
    }

    override fun containerTick() {
        super.containerTick()
        onScreenUpdate.forEach {
            it()
        }
    }

    override fun render(vanillaContext: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(vanillaContext, mouseX, mouseY, delta)
        onDrawTooltip.forEach {
            it(vanillaContext, mouseX, mouseY, delta)
        }
    }
}
