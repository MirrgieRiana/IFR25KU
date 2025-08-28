package miragefairy2024.client.mod.fairy

import com.mojang.blaze3d.platform.InputConstants
import io.wispforest.owo.ui.base.BaseOwoHandledScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.core.VerticalAlignment
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.client.mixins.api.RenderingEvent
import miragefairy2024.client.util.CompressionHorizontalFlow
import miragefairy2024.client.util.KeyMappingCard
import miragefairy2024.client.util.SlotType
import miragefairy2024.client.util.inventoryNameLabel
import miragefairy2024.client.util.registerHandledScreen
import miragefairy2024.client.util.sendToServer
import miragefairy2024.client.util.slotContainer
import miragefairy2024.client.util.tooltipContainer
import miragefairy2024.client.util.verticalScroll
import miragefairy2024.client.util.verticalSpace
import miragefairy2024.mod.fairy.FairyItem
import miragefairy2024.mod.fairy.OPEN_SOUL_STREAM_KEY_TRANSLATION
import miragefairy2024.mod.fairy.OpenSoulStreamChannel
import miragefairy2024.mod.fairy.SoulStreamScreenHandler
import miragefairy2024.mod.fairy.getFairyMotif
import miragefairy2024.mod.fairy.soulStreamScreenHandlerType
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
import miragefairy2024.util.plus
import miragefairy2024.util.red
import miragefairy2024.util.size
import miragefairy2024.util.text
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ImageButton
import net.minecraft.client.gui.components.WidgetSprites
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.client.renderer.RenderType
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import org.lwjgl.glfw.GLFW

val soulStreamKeyMappingCard = KeyMappingCard(
    OPEN_SOUL_STREAM_KEY_TRANSLATION.keyGetter(),
    GLFW.GLFW_KEY_K,
    KeyMapping.CATEGORY_INVENTORY,
) {
    lastMousePositionInInventory = null
    OpenSoulStreamChannel.sendToServer(Unit)
}

var lastMousePositionInInventory: Pair<Double, Double>? = null

var enableGlobalFairyHighlight = false
var fairyHighlightFilter: List<(PassiveSkillSpecification<*>) -> Boolean> = listOf()

context(ModContext)
fun initClientSoulStream() {

    // GUI登録
    soulStreamScreenHandlerType.registerHandledScreen { gui, inventory, title -> SoulStreamScreen(gui, inventory, title) }

    // ソウルストリームのキーバインド
    soulStreamKeyMappingCard.init()

    // インベントリ画面にソウルストリームのボタンを設置
    ScreenEvents.AFTER_INIT.register { _, screen, _, _ ->
        if (screen is InventoryScreen) {

            val onMouseClick = mutableListOf<() -> Unit>()

            // 中央揃えコンテナ
            val uiAdapter = OwoUIAdapter.create(screen, Containers::stack)
            uiAdapter.rootComponent.apply {
                alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)

                // 位置決定用パネル
                child(Containers.stack(Sizing.content(), Sizing.content()).apply {
                    alignment(HorizontalAlignment.RIGHT, VerticalAlignment.TOP)

                    fun updatePosition() {
                        if (!screen.recipeBookComponent.isVisible) {
                            sizing(Sizing.fixed(146), Sizing.fixed(46))
                        } else {
                            sizing(Sizing.fixed(300), Sizing.fixed(46))
                        }
                    }
                    updatePosition()
                    onMouseClick += {
                        updatePosition()
                        uiAdapter.inflateAndMount()
                    }

                    // ボタン
                    val buttonWidgetSprites = WidgetSprites(MirageFairy2024.identifier("soul_stream_button"), MirageFairy2024.identifier("soul_stream_button_highlighted"))
                    child(Components.wrapVanillaWidget(ImageButton(0, 0, 20, 20, buttonWidgetSprites) {
                        lastMousePositionInInventory = Pair(Minecraft.getInstance().mouseHandler.xpos(), Minecraft.getInstance().mouseHandler.ypos())
                        screen.onClose()
                        OpenSoulStreamChannel.sendToServer(Unit)
                    }).apply {
                        tooltip(text { OPEN_SOUL_STREAM_KEY_TRANSLATION() + "("() + Component.keybind(OPEN_SOUL_STREAM_KEY_TRANSLATION.keyGetter()) + ")"() })
                    })

                })

            }
            uiAdapter.inflateAndMount()

            ScreenMouseEvents.afterMouseClick(screen).register { _, _, _, _ ->
                onMouseClick.forEach {
                    it()
                }
            }

        }
    }

    // 妖精検索
    RenderingEvent.RENDER_ITEM_DECORATIONS.register { graphics, font, stack, x, y, text ->
        val screen = Minecraft.getInstance().screen
        if (!(enableGlobalFairyHighlight || screen is SoulStreamScreen)) return@register
        val filter = fairyHighlightFilter
        if (filter.isEmpty()) return@register
        if (stack.item !is FairyItem) return@register
        val motif = stack.getFairyMotif() ?: return@register
        val matched = motif.passiveSkillSpecifications.any { specification -> filter.any { predicate -> predicate(specification) } }
        if (!matched) return@register

        graphics.fill(RenderType.guiOverlay(), x, y, x + 16, y + 2, 0xAAFFFF00.toInt())
        graphics.fill(RenderType.guiOverlay(), x, y + 2, x + 2, y + 14, 0xAAFFFF00.toInt())
        graphics.fill(RenderType.guiOverlay(), x, y + 14, x + 16, y + 16, 0xAAFFFF00.toInt())
        graphics.fill(RenderType.guiOverlay(), x + 14, y + 2, x + 16, y + 14, 0xAAFFFF00.toInt())
    }

}

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
                    child(Components.label(text { " 効果4"().blue }).apply {
                        textClickHandler {
                            enableGlobalFairyHighlight = !enableGlobalFairyHighlight
                            true
                        }
                    })
                    child(Components.label(text { " 効果5"().darkGray }).apply {
                        textClickHandler {
                            if (fairyHighlightFilter.isEmpty()) {
                                fairyHighlightFilter = listOf { specification ->
                                    specification.effect == CollectionPassiveSkillEffect
                                }
                            } else {
                                fairyHighlightFilter = listOf()
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
