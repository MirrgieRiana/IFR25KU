package miragefairy2024.client.mod.fairy

import com.mojang.blaze3d.platform.InputConstants
import io.wispforest.owo.ui.base.BaseOwoHandledScreen
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
import io.wispforest.owo.util.Observable
import miragefairy2024.client.util.ColoredContainer
import miragefairy2024.client.util.CompressionHorizontalFlow
import miragefairy2024.client.util.SlotType
import miragefairy2024.client.util.inventoryNameLabel
import miragefairy2024.client.util.slotContainer
import miragefairy2024.client.util.tooltipContainer
import miragefairy2024.client.util.verticalScroll
import miragefairy2024.client.util.verticalSpace
import miragefairy2024.mod.fairy.FairyItem
import miragefairy2024.mod.fairy.SoulStreamScreenHandler
import miragefairy2024.mod.fairy.getFairyMotif
import miragefairy2024.mod.passiveskill.PassiveSkillEffect
import miragefairy2024.mod.passiveskill.PassiveSkillResult
import miragefairy2024.mod.passiveskill.PassiveSkillSpecification
import miragefairy2024.mod.passiveskill.collect
import miragefairy2024.mod.passiveskill.effects.CollectionPassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.ManaBoostPassiveSkillEffect
import miragefairy2024.mod.passiveskill.findPassiveSkillProviders
import miragefairy2024.mod.passiveskill.passiveSkillEffectRegistry
import miragefairy2024.util.blue
import miragefairy2024.util.darkGray
import miragefairy2024.util.gold
import miragefairy2024.util.invoke
import miragefairy2024.util.red
import miragefairy2024.util.size
import miragefairy2024.util.text
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerListener
import net.minecraft.world.item.ItemStack
import io.wispforest.owo.ui.core.Component as OwoComponent

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

    private val filter: Observable<List<(PassiveSkillSpecification<*>) -> Boolean>> = Observable.of(listOf())

    override fun createAdapter(): OwoUIAdapter<FlowLayout> = OwoUIAdapter.create(this, Containers::verticalFlow)
    override fun build(rootComponent: FlowLayout) {
        rootComponent.apply {
            surface(Surface.VANILLA_TRANSLUCENT)
            verticalAlignment(VerticalAlignment.CENTER)
            horizontalAlignment(HorizontalAlignment.CENTER)

            // GUIパネル外枠
            child(CompressionHorizontalFlow(Sizing.fixed(356)).apply {
                surface(Surface.PANEL)
                padding(Insets.of(7))

                // 絞り込み欄
                child(Containers.verticalFlow(Sizing.expand(30), Sizing.fill()).apply {
                    padding(Insets.of(0, 0, 0, 4))

                    // TODO
                    child(Components.label(text { "条件"().darkGray }))
                    child(Components.label(text { " 条件1"().darkGray }))
                    child(Components.label(text { " 条件2"().darkGray }))
                    child(Components.label(text { " 条件3"().darkGray }))
                    child(Components.label(text { " 条件4"().blue }))
                    child(Components.label(text { " 条件5"().darkGray }))
                    child(Components.label(text { "効果"().darkGray }))
                    child(Components.label(text { " 効果1"().darkGray }))
                    child(Components.label(text { " 効果2"().blue }))
                    child(Components.label(text { " 効果3"().darkGray }))
                    child(Components.label(text { " 効果4"().blue }))
                    child(Components.label(text { " 効果5"().darkGray }).apply {
                        textClickHandler {
                            if (filter.get().isEmpty()) {
                                filter.set(listOf { specification ->
                                    specification.effect == CollectionPassiveSkillEffect
                                })
                            } else {
                                filter.set(listOf())
                            }
                            true
                        }
                    })

                })

                // メインコンテナ
                leader(Containers.verticalFlow(Sizing.fixed(18 * 9 + 18), Sizing.content()).apply {

                    child(inventoryNameLabel(title))

                    child(verticalSpace(3))

                    child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                        surface(Surface.tiled(SlotType.FAIRY.texture, 18, 18))
                        repeat(9) { index ->
                            child(createSlotComponent(9 * 3 + 9 + index))
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
                                    child(createSlotComponent(9 * 3 + 9 + index))
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
                                    child(createSlotComponent(9 * r + c))
                                }
                            })
                        }
                    })
                    child(verticalSpace(4))
                    child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                        surface(Surface.tiled(SlotType.NORMAL.texture, 18, 18))
                        repeat(9) { c ->
                            child(createSlotComponent(9 * 3 + c))
                        }
                    })

                })

                // 効果欄
                child(Containers.verticalFlow(Sizing.expand(70), Sizing.fill()).apply {
                    padding(Insets.of(0, 0, 4, 0))
                    child(tooltipContainer(Sizing.fill(), Sizing.fill()).apply {
                        child(verticalScroll(Sizing.fill(), Sizing.fill(), 5, overlapped = true).apply {
                            child(Containers.verticalFlow(Sizing.fill(), Sizing.content()).apply {
                                val container = this

                                fun refresh() {
                                    container.clearChildren()

                                    val player = Minecraft.getInstance().player ?: return
                                    val passiveSkillProviders = player.findPassiveSkillProviders()
                                    val result = PassiveSkillResult()
                                    result.collect(passiveSkillProviders.passiveSkills, player, ManaBoostPassiveSkillEffect.Value(mapOf()), true)
                                    val manaBoostValue = result[ManaBoostPassiveSkillEffect]
                                    result.collect(passiveSkillProviders.passiveSkills, player, manaBoostValue, false)

                                    if (result.map.isEmpty()) {
                                        container.child(Components.label(text { "パッシブスキル効果なし"().red })) // TODO translate
                                    } else {
                                        result.map.keys.sortedBy { passiveSkillEffectRegistry.getKey(it) }.forEach { effect ->
                                            fun <T : Any> f(effect: PassiveSkillEffect<T>) {
                                                val value = result[effect]
                                                effect.getTexts(value).forEach {
                                                    container.child(Components.label(it.gold).tooltip(it.gold))
                                                }
                                            }
                                            f(effect)
                                        }
                                    }
                                }

                                onUpdateListeners += {
                                    refresh()
                                }
                                refresh()
                            })
                        })
                    })
                })

            })

        }
    }

    private fun createSlotComponent(index: Int): OwoComponent {
        return slotContainer(ColoredContainer(Sizing.fixed(16), Sizing.fixed(16), slotAsComponent(index)).apply {
            box.fill(true)

            fun update() {
                val matches = run {
                    val filter = filter.get()!!
                    if (filter.isEmpty()) return@run false
                    val itemStack = menu.getSlot(index).item
                    if (itemStack.item !is FairyItem) return@run false
                    val motif = itemStack.getFairyMotif() ?: return@run false
                    motif.passiveSkillSpecifications.any { specification -> filter.all { predicate -> predicate(specification) } }
                }
                box.color(if (matches) Color.ofRgb(0xFFFF00) else Color.ofArgb(0x00000000))
            }
            filter.observe {
                update()
            }
            run {
                val listener = object : ContainerListener {
                    override fun slotChanged(containerToSend: AbstractContainerMenu, dataSlotIndex: Int, stack: ItemStack) {
                        update()
                    }

                    override fun dataChanged(containerMenu: AbstractContainerMenu, dataSlotIndex: Int, value: Int) {
                        update()
                    }
                }
                menu.addSlotListener(listener)
                onCloseListeners += {
                    menu.removeSlotListener(listener)
                }
            }
            update()

        }, type = null)
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

    private val onCloseListeners = mutableListOf<() -> Unit>()
    override fun onClose() {
        super.onClose()
        onCloseListeners.forEach {
            it()
        }
    }

}
