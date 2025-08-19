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
import miragefairy2024.client.util.ClickableContainer
import miragefairy2024.client.util.inventoryNameLabel
import miragefairy2024.client.util.registerHandledScreen
import miragefairy2024.client.util.topBorderLayout
import miragefairy2024.client.util.verticalScroll
import miragefairy2024.client.util.verticalSpace
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.magicplant.MagicPlantSeedItem
import miragefairy2024.mod.magicplant.TraitEffectKey
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
import miragefairy2024.util.darkGray
import miragefairy2024.util.invoke
import miragefairy2024.util.join
import miragefairy2024.util.plus
import miragefairy2024.util.style
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.ChatFormatting
import net.minecraft.client.renderer.RenderType
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import io.wispforest.owo.ui.core.Component as OwoComponent
import net.minecraft.client.Minecraft as MinecraftClient

context(ModContext)
fun initMagicPlantClientModule() {
    traitListScreenHandlerType.registerHandledScreen { gui, inventory, title -> TraitListScreen(gui, inventory, title) }

    RenderingEvent.RENDER_ITEM_DECORATIONS.register { graphics, font, stack, x, y, text ->
        if (stack.item !is MagicPlantSeedItem) return@register

        val player = MinecraftClient.getInstance().player ?: return@register
        val otherItemStack = player.mainHandItem
        if (otherItemStack === stack) return@register

        val traitStacks = stack.getTraitStacks() ?: return@register
        val otherTraitStacks = if (otherItemStack.item is MagicPlantSeedItem) otherItemStack.getTraitStacks() ?: return@register else return@register
        val plusBitCount = (traitStacks - otherTraitStacks).positiveBitCount + (traitStacks - otherTraitStacks).negativeBitCount
        val minusBitCount = (otherTraitStacks - traitStacks).positiveBitCount + (otherTraitStacks - traitStacks).negativeBitCount

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
                    child(Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(18 * 4)).apply { // 特性リスト
                        surface(Surface.TOOLTIP)
                        padding(Insets.of(5))

                        child(verticalScroll(Sizing.fill(100), Sizing.fill(100), 5).apply {
                            scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xA0FFFFFF.toInt())))

                            child().child(Containers.verticalFlow(Sizing.fill(100), Sizing.content()).apply {
                                val player = MinecraftClient.getInstance().player!!
                                val level = player.level()
                                val blockEntity = level.getMagicPlantBlockEntity(menu.blockPos)

                                menu.traitStacks.traitStackList.forEach { traitStack ->
                                    val totalConditionFactor = traitStack.trait.conditions
                                        .map { it.getFactor(level, menu.blockPos, blockEntity) }
                                        .fold(1.0) { a, b -> a * b }

                                    child(ClickableContainer(Sizing.fill(100), Sizing.content(), { // 特性
                                        setTraitCardContent(createTraitCardContent(traitStack))
                                        true
                                    }) {
                                        Components.label(text {
                                            val texts = mutableListOf<Component>()
                                            val styleFunction: (Component) -> Component = { if (totalConditionFactor == 0.0) it.darkGray else it.style(traitStack.trait.style) }
                                            texts += styleFunction(traitStack.trait.getName())
                                            texts += styleFunction(traitStack.level.toString(2)())
                                            if (traitStack.trait.conditions.isNotEmpty()) texts += traitStack.trait.conditions.map { it.emoji }.join() + " →"()
                                            if (traitStack.trait.traitEffectKeyEntries.isNotEmpty()) texts += traitStack.trait.traitEffectKeyEntries.map { it.traitEffectKey.emoji.style(it.traitEffectKey.style) }.join()
                                            texts.join(" "())
                                        })
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
                    val player = MinecraftClient.getInstance().player!!
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
