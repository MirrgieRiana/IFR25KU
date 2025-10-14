package miragefairy2024.client.mod

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
import miragefairy2024.ModContext
import miragefairy2024.client.mixins.api.RenderingEvent
import miragefairy2024.client.mod.recipeviewer.ViewOwoAdapter
import miragefairy2024.client.mod.recipeviewer.ViewOwoAdapterContext
import miragefairy2024.client.mod.recipeviewer.ViewOwoAdapterRegistry
import miragefairy2024.client.util.ClickableContainer
import miragefairy2024.client.util.OwoComponent
import miragefairy2024.client.util.SlotType
import miragefairy2024.client.util.inventoryNameLabel
import miragefairy2024.client.util.leftBorderLayout
import miragefairy2024.client.util.registerHandledScreen
import miragefairy2024.client.util.tooltipContainer
import miragefairy2024.client.util.topBorderLayout
import miragefairy2024.client.util.verticalScroll
import miragefairy2024.client.util.verticalSpace
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.magicplant.MagicPlantSeedItem
import miragefairy2024.mod.magicplant.TraitEffectKey
import miragefairy2024.mod.magicplant.TraitEncyclopediaRecipeViewerCategoryCard
import miragefairy2024.mod.magicplant.TraitEncyclopediaView
import miragefairy2024.mod.magicplant.TraitListScreenHandler
import miragefairy2024.mod.magicplant.TraitStack
import miragefairy2024.mod.magicplant.contents.getTraitPower
import miragefairy2024.mod.magicplant.getMagicPlantBlockEntity
import miragefairy2024.mod.magicplant.getName
import miragefairy2024.mod.magicplant.getTraitStacks
import miragefairy2024.mod.magicplant.minus
import miragefairy2024.mod.magicplant.negativeBitCount
import miragefairy2024.mod.magicplant.positiveBitCount
import miragefairy2024.mod.magicplant.style
import miragefairy2024.mod.magicplant.texture
import miragefairy2024.mod.magicplant.traitListScreenHandlerType
import miragefairy2024.mod.magicplant.traitListScreenTranslation
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.views.CatalystSlotView
import miragefairy2024.mod.recipeviewer.views.noBackground
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.darkGray
import miragefairy2024.util.fire
import miragefairy2024.util.invoke
import miragefairy2024.util.join
import miragefairy2024.util.plus
import miragefairy2024.util.register
import miragefairy2024.util.style
import miragefairy2024.util.text
import miragefairy2024.util.toIngredientStack
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderType
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory

context(ModContext)
fun initMagicPlantClientModule() {
    traitListScreenHandlerType.registerHandledScreen { gui, inventory, title -> TraitListScreen(gui, inventory, title) }

    RenderingEvent.RENDER_ITEM_DECORATIONS.register { graphics, font, stack, x, y, text ->
        if (stack.item !is MagicPlantSeedItem) return@register

        val player = Minecraft.getInstance().player ?: return@register
        val otherItemStack = player.mainHandItem
        if (otherItemStack === stack) return@register

        val traitStacks = stack.getTraitStacks() ?: return@register
        val otherTraitStacks = if (otherItemStack.item is MagicPlantSeedItem) otherItemStack.getTraitStacks() ?: return@register else return@register
        val plusBitCount = (traitStacks - otherTraitStacks).positiveBitCount + (otherTraitStacks - traitStacks).negativeBitCount
        val minusBitCount = (otherTraitStacks - traitStacks).positiveBitCount + (traitStacks - otherTraitStacks).negativeBitCount

        graphics.pose().pushPose()
        try {
            graphics.fill(RenderType.guiOverlay(), x, y, x + 16, y + 8, 0x888B8B8B.toInt())
            graphics.pose().translate(0.0F, 0.0F, 200.0F)
            if (plusBitCount > 0) graphics.drawString(font, "$plusBitCount", x, y, ChatFormatting.GREEN.color!!, false)
            if (minusBitCount > 0) graphics.drawString(font, "$minusBitCount", x + 19 - 2 - font.width("$minusBitCount"), y, ChatFormatting.DARK_RED.color!!, false)
        } finally {
            graphics.pose().popPose()
        }
    }

    ViewOwoAdapterRegistry.register(TraitEncyclopediaView::class.java, TraitEncyclopediaViewOwoAdapter)
}

class TraitListScreen(handler: TraitListScreenHandler, playerInventory: Inventory, title: Component) : BaseOwoHandledScreen<FlowLayout, TraitListScreenHandler>(handler, playerInventory, title) {
    private lateinit var traitCardContainer: FlowLayout

    private fun setTraitCardContent(component: OwoComponent?) {
        traitCardContainer.clearChildren()
        if (component != null) traitCardContainer.child(component)
        uiAdapter.inflateAndMount()
    }

    override fun createAdapter(): OwoUIAdapter<FlowLayout> = OwoUIAdapter.create(this, Containers::verticalFlow)
    override fun build(rootComponent: FlowLayout) {
        rootComponent.apply {
            surface(Surface.VANILLA_TRANSLUCENT)
            verticalAlignment(VerticalAlignment.CENTER)
            horizontalAlignment(HorizontalAlignment.CENTER)

            child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply { // 外枠
                surface(Surface.PANEL)
                padding(Insets.of(7))

                child(Containers.verticalFlow(Sizing.fixed(18 * 9), Sizing.content()).apply { // 内枠
                    child(inventoryNameLabel(text { traitListScreenTranslation() }, HorizontalAlignment.CENTER)) // GUI名
                    child(verticalSpace(3))
                    traitCardContainer = Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(18 * 6)).apply { // 特性カードコンテナ
                        surface(NinePatchTextureCard.TRAIT_BACKGROUND.surface)
                        padding(Insets.of(5))
                    }
                    child(traitCardContainer)
                    child(verticalSpace(4))
                    child(tooltipContainer(Sizing.fill(100), Sizing.fixed(18 * 4)).apply { // 特性リスト
                        child(verticalScroll(Sizing.fill(100), Sizing.fill(100), 5).apply {
                            scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xA0FFFFFF.toInt())))

                            child().child(Containers.verticalFlow(Sizing.fill(100), Sizing.content()).apply {
                                val player = Minecraft.getInstance().player!!
                                val level = player.level()
                                val blockEntity = level.getMagicPlantBlockEntity(menu.blockPos)

                                menu.traitStacks.traitStackList.forEach { traitStack ->
                                    val totalConditionFactor = traitStack.trait.conditions
                                        .map { it.getFactor(level, menu.blockPos, blockEntity) }
                                        .fold(1.0) { a, b -> a * b }

                                    child(ClickableContainer(Sizing.fill(100), Sizing.content()).apply { // 特性
                                        onClick.register {
                                            setTraitCardContent(createTraitCardContent(traitStack))
                                            true
                                        }
                                        child(Components.label(text {
                                            val texts = mutableListOf<Component>()
                                            val styleFunction: (Component) -> Component = { if (totalConditionFactor == 0.0) it.darkGray else it.style(traitStack.trait.style) }
                                            texts += styleFunction(traitStack.trait.getName())
                                            texts += styleFunction(traitStack.level.toString(2)())
                                            if (traitStack.trait.conditions.isNotEmpty()) texts += traitStack.trait.conditions.map { it.emoji }.join() + " →"()
                                            if (traitStack.trait.traitEffectKeyEntries.isNotEmpty()) texts += traitStack.trait.traitEffectKeyEntries.map { it.traitEffectKey.emoji.style(it.traitEffectKey.style) }.join()
                                            texts.join(" "())
                                        }))
                                    })
                                }
                            })
                        })
                    })
                })
            })
        }

        val traitStack = menu.traitStacks.traitStackList.firstOrNull()
        if (traitStack != null) setTraitCardContent(createTraitCardContent(traitStack))
    }

    private fun createTraitCardContent(traitStack: TraitStack): OwoComponent {
        return topBorderLayout(Sizing.fill(100), Sizing.fill(100)).apply {
            gap = 5

            child1(Containers.verticalFlow(Sizing.fill(100), Sizing.content()).apply {
                child(Components.label(text { traitStack.trait.getName().style(traitStack.trait.style) }).apply { // 特性名
                    sizing(Sizing.fill(100), Sizing.content())
                    horizontalTextAlignment(HorizontalAlignment.CENTER)
                })
                child(verticalSpace(5))
                child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(32)).apply { // 特性アイコン欄
                    val player = Minecraft.getInstance().player!!
                    val level = player.level()
                    val blockEntity = level.getMagicPlantBlockEntity(menu.blockPos)

                    child(Containers.verticalFlow(Sizing.expand(50), Sizing.fill(100)).apply { // 条件
                        verticalAlignment(VerticalAlignment.BOTTOM)

                        traitStack.trait.conditions.forEach { condition ->
                            val factor = condition.getFactor(level, menu.blockPos, blockEntity)
                            val text = text { condition.emoji + " "() + (factor * 100.0 formatAs "%.1f%%")() }
                            child(Components.label(text).tooltip(condition.name))
                        }
                    })
                    child(Containers.verticalFlow(Sizing.content(), Sizing.fill(100)).apply { // 特性アイコン
                        child(Components.texture(traitStack.trait.texture, 0, 0, 32, 32, 32, 32))
                    })
                    child(Containers.verticalFlow(Sizing.expand(50), Sizing.fill(100)).apply { // 効果
                        horizontalAlignment(HorizontalAlignment.RIGHT)
                        verticalAlignment(VerticalAlignment.BOTTOM)

                        val totalConditionFactor = traitStack.trait.conditions
                            .map { it.getFactor(level, menu.blockPos, blockEntity) }
                            .fold(1.0) { a, b -> a * b }

                        traitStack.trait.traitEffectKeyEntries.forEach {
                            fun <T : Any> render(traitEffectKey: TraitEffectKey<T>, traitEffectFactor: Double) {
                                val value = traitEffectKey.getValue(traitEffectFactor * getTraitPower(traitStack.level) * totalConditionFactor)
                                val text = text { traitEffectKey.renderValue(value) + " "() + traitEffectKey.emoji.style(traitEffectKey.style) }
                                child(Components.label(text).tooltip(traitEffectKey.name))
                            }
                            render(it.traitEffectKey, it.factor)
                        }
                    })
                })
            })
            child2(verticalScroll(Sizing.fill(100), Sizing.fill(100), 5, overlapped = true).apply { // 特性ポエム
                scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xA0FFFFFF.toInt())))

                child().child(Components.label(text { traitStack.trait.poem }).apply {
                    sizing(Sizing.fill(100), Sizing.content())
                    horizontalTextAlignment(HorizontalAlignment.LEFT)
                })
            })
        }
    }
}

object TraitEncyclopediaViewOwoAdapter : ViewOwoAdapter<TraitEncyclopediaView> {
    private enum class ViewMode {
        SEPARATED,
        CARD,
    }

    private var viewMode = ViewMode.SEPARATED

    override fun createOwoComponent(view: TraitEncyclopediaView, context: ViewOwoAdapterContext): OwoComponent {
        val onViewModeChanged = EventRegistry<() -> Unit>()

        val separatedView = topBorderLayout(Sizing.fill(100), Sizing.fill(100)).apply { // カード・レシピセパレーション
            gap = 2

            child1(ClickableContainer(Sizing.fill(100), Sizing.content()).apply {
                onClick.register {
                    viewMode = ViewMode.CARD
                    onViewModeChanged.fire()
                    true
                }
                child(Containers.verticalFlow(Sizing.fill(100), Sizing.content()).apply { // カード
                    surface(NinePatchTextureCard.TRAIT_BACKGROUND.surface)
                    padding(Insets.of(5))

                    child(Components.label(text { view.trait.getName().style(view.trait.style) }).apply { // 特性名
                        sizing(Sizing.fill(100), Sizing.content())
                        horizontalTextAlignment(HorizontalAlignment.CENTER)
                    })
                    child(verticalSpace(5))
                    child(leftBorderLayout(Sizing.fill(100), Sizing.fixed(43)).apply { // カードの特性名以外の部分
                        gap = 5

                        child1(Containers.stack(Sizing.fixed(46), Sizing.fill(100)).apply { // 特性アイコン欄
                            child(Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100)).apply { // 条件
                                horizontalAlignment(HorizontalAlignment.LEFT)
                                verticalAlignment(VerticalAlignment.BOTTOM)

                                view.trait.conditions.forEach {
                                    child(Components.label(it.emoji).tooltip(it.name))
                                }
                            })
                            child(Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100)).apply { // 特性アイコン
                                horizontalAlignment(HorizontalAlignment.CENTER)
                                verticalAlignment(VerticalAlignment.CENTER)

                                child(Components.texture(view.trait.texture, 0, 0, 32, 32, 32, 32))
                            })
                            child(Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100)).apply { // 効果
                                horizontalAlignment(HorizontalAlignment.RIGHT)
                                verticalAlignment(VerticalAlignment.BOTTOM)

                                view.trait.traitEffectKeyEntries.forEach {
                                    val text = text { it.traitEffectKey.emoji.style(it.traitEffectKey.style) }
                                    val tooltip = text { it.traitEffectKey.name + " "() + (it.factor * 100.0 formatAs "%.1f%%")() }
                                    child(Components.label(text).tooltip(tooltip))
                                }
                            })
                        })
                        child2(verticalScroll(Sizing.fill(100), Sizing.fill(100), 5, overlapped = true).apply { // 特性ポエム
                            scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xA0FFFFFF.toInt())))

                            child().child(Components.label(text { view.trait.poem }).apply {
                                sizing(Sizing.fill(100), Sizing.content())
                                horizontalTextAlignment(HorizontalAlignment.LEFT)
                            })
                        })
                    })
                })
            })
            child2(verticalScroll(Sizing.fill(100), Sizing.fill(100), 5).apply { // レシピ
                child().child(Containers.verticalFlow(Sizing.fill(100), Sizing.content()).apply { // 種子欄
                    surface(Surface.tiled(SlotType.NORMAL.texture, 18, 18))

                    val inputItemStacks = TraitEncyclopediaRecipeViewerCategoryCard.getProducerMagicPlantSeedItemStacks(view.trait)
                    inputItemStacks.chunked(9).forEach { chunk ->
                        child(Containers.horizontalFlow(Sizing.fill(100), Sizing.content()).apply { // 種子の行
                            horizontalAlignment(HorizontalAlignment.LEFT)

                            chunk.forEach { inputItemStack ->
                                child(context.wrap(CatalystSlotView(inputItemStack.toIngredientStack()).noBackground(), IntPoint(18, 18))) // 種子
                            }
                        })
                    }
                })
                child().child(verticalSpace(2))
            })
        }
        val cardView = ClickableContainer(Sizing.fill(100), Sizing.fill(100)).apply {
            onClick.register {
                viewMode = ViewMode.SEPARATED
                onViewModeChanged.fire()
                true
            }
            child(topBorderLayout(Sizing.fill(100), Sizing.fill(100)).apply { // カード
                surface(NinePatchTextureCard.TRAIT_BACKGROUND.surface)
                padding(Insets.of(5))
                gap = 5

                child1(Containers.verticalFlow(Sizing.fill(100), Sizing.content()).apply {
                    child(Components.label(text { view.trait.getName().style(view.trait.style) }).apply { // 特性名
                        sizing(Sizing.fill(100), Sizing.content())
                        horizontalTextAlignment(HorizontalAlignment.CENTER)
                    })
                    child(verticalSpace(5))
                    child(Containers.stack(Sizing.fill(100), Sizing.fixed(32)).apply { // 特性アイコン欄
                        child(Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100)).apply { // 条件
                            horizontalAlignment(HorizontalAlignment.LEFT)
                            verticalAlignment(VerticalAlignment.BOTTOM)

                            view.trait.conditions.forEach {
                                child(Components.label(it.emoji).tooltip(it.name))
                            }
                        })
                        child(Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100)).apply { // 特性アイコン
                            horizontalAlignment(HorizontalAlignment.CENTER)
                            verticalAlignment(VerticalAlignment.CENTER)

                            child(Components.texture(view.trait.texture, 0, 0, 32, 32, 32, 32))
                        })
                        child(Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100)).apply { // 効果
                            horizontalAlignment(HorizontalAlignment.RIGHT)
                            verticalAlignment(VerticalAlignment.BOTTOM)

                            view.trait.traitEffectKeyEntries.forEach {
                                val text = text { (it.factor * 100.0 formatAs "%.1f%%")() + " "() + it.traitEffectKey.emoji.style(it.traitEffectKey.style) }
                                val tooltip = text { it.traitEffectKey.name }
                                child(Components.label(text).tooltip(tooltip))
                            }
                        })
                    })
                })
                child2(verticalScroll(Sizing.fill(100), Sizing.fill(100), 5, overlapped = true).apply { // 特性ポエム
                    scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xA0FFFFFF.toInt())))

                    child().child(Components.label(text { view.trait.poem }).apply {
                        sizing(Sizing.fill(100), Sizing.content())
                        horizontalTextAlignment(HorizontalAlignment.LEFT)
                    })
                })
            })
        }
        val container = Containers.stack(Sizing.fill(100), Sizing.fill(100)).apply {
            onViewModeChanged.register {
                clearChildren()
                val child = when (viewMode) {
                    ViewMode.SEPARATED -> separatedView
                    ViewMode.CARD -> cardView
                }
                child(child)
                context.prepare()
            }
        }

        onViewModeChanged.fire()

        return container
    }
}
