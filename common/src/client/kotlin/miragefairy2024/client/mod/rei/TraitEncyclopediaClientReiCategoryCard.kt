package miragefairy2024.client.mod.rei

import io.wispforest.owo.compat.rei.ReiUIAdapter
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.container.StackLayout
import io.wispforest.owo.ui.core.Color
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.core.VerticalAlignment
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import miragefairy2024.client.mod.surface
import miragefairy2024.client.util.ClickableContainer
import miragefairy2024.client.util.SlotType
import miragefairy2024.client.util.leftBorderLayout
import miragefairy2024.client.util.topBorderLayout
import miragefairy2024.client.util.verticalScroll
import miragefairy2024.client.util.verticalSpace
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.magicplant.TraitStack
import miragefairy2024.mod.magicplant.TraitStacks
import miragefairy2024.mod.magicplant.contents.magicplants.MirageFlowerCard
import miragefairy2024.mod.magicplant.getName
import miragefairy2024.mod.magicplant.magicPlantCards
import miragefairy2024.mod.magicplant.setTraitStacks
import miragefairy2024.mod.magicplant.style
import miragefairy2024.mod.magicplant.texture
import miragefairy2024.mod.magicplant.traitRegistry
import miragefairy2024.mod.rei.TraitEncyclopediaReiCategoryCard
import miragefairy2024.util.createItemStack
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.register
import miragefairy2024.util.sortedEntrySet
import miragefairy2024.util.style
import miragefairy2024.util.text
import miragefairy2024.util.toEntryIngredient
import miragefairy2024.util.toEntryStack
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.network.chat.Component
import io.wispforest.owo.ui.core.Component as OwoComponent

object TraitEncyclopediaClientReiCategoryCard : ClientReiCategoryCard<TraitEncyclopediaReiCategoryCard.Display>(TraitEncyclopediaReiCategoryCard) {
    override fun registerDisplays(registry: DisplayRegistry) {
        traitRegistry.sortedEntrySet.forEach { (_, trait) ->
            val seedItemStacks = magicPlantCards.filter { trait in it.randomTraitChances }.map { card ->
                card.item().createItemStack().also {
                    it.setTraitStacks(TraitStacks.of(TraitStack(trait, 1)))
                }
            }
            registry.add(TraitEncyclopediaReiCategoryCard.Display(seedItemStacks.map { it.toEntryStack().toEntryIngredient() }, trait))
        }
    }

    var currentViewMode = ViewMode.SEPARATED

    enum class ViewMode {
        SEPARATED,
        CARD,
    }

    override fun createCategory() = object : DisplayCategory<TraitEncyclopediaReiCategoryCard.Display> {
        override fun getCategoryIdentifier() = TraitEncyclopediaReiCategoryCard.identifier.first
        override fun getTitle(): Component = text { TraitEncyclopediaReiCategoryCard.translation() }
        override fun getIcon(): Renderer = MirageFlowerCard.item().createItemStack().toEntryStack()
        override fun getDisplayWidth(display: TraitEncyclopediaReiCategoryCard.Display) = 5 + 18 * 9 + 5 + 5
        override fun getDisplayHeight() = 140
        override fun setupDisplay(display: TraitEncyclopediaReiCategoryCard.Display, bounds: Rectangle): List<Widget> {
            lateinit var container: StackLayout
            lateinit var separatedView: OwoComponent
            lateinit var cardView: OwoComponent
            return listOf(
                Widgets.createRecipeBase(bounds), // 外枠
                ReiUIAdapter(bounds, Containers::verticalFlow).also { adapter ->

                    fun updateViewMode() {
                        container.clearChildren()
                        val component = when (currentViewMode) {
                            ViewMode.SEPARATED -> separatedView
                            ViewMode.CARD -> cardView
                        }
                        container.child(component)
                        adapter.prepare()
                    }

                    adapter.rootComponent().apply { // ルートパディング
                        padding(Insets.of(5))
                        allowOverflow(true)

                        separatedView = topBorderLayout(Sizing.fill(100), Sizing.fill(100)).apply { // カード・レシピセパレーション
                            gap = 2

                            child1(ClickableContainer(Sizing.fill(100), Sizing.content()).apply {
                                onClick.register {
                                    currentViewMode = ViewMode.CARD
                                    updateViewMode()
                                    true
                                }
                                child(Containers.verticalFlow(Sizing.fill(100), Sizing.content()).apply { // カード
                                    surface(NinePatchTextureCard.TRAIT_BACKGROUND.surface)
                                    padding(Insets.of(5))

                                    child(Components.label(text { display.trait.getName().style(display.trait.style) }).apply { // 特性名
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

                                                display.trait.conditions.forEach {
                                                    child(Components.label(it.emoji).tooltip(it.name))
                                                }
                                            })
                                            child(Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100)).apply { // 特性アイコン
                                                horizontalAlignment(HorizontalAlignment.CENTER)
                                                verticalAlignment(VerticalAlignment.CENTER)

                                                child(Components.texture(display.trait.texture, 0, 0, 32, 32, 32, 32))
                                            })
                                            child(Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100)).apply { // 効果
                                                horizontalAlignment(HorizontalAlignment.RIGHT)
                                                verticalAlignment(VerticalAlignment.BOTTOM)

                                                display.trait.traitEffectKeyEntries.forEach {
                                                    val text = text { it.traitEffectKey.emoji.style(it.traitEffectKey.style) }
                                                    val tooltip = text { it.traitEffectKey.name + " "() + (it.factor * 100.0 formatAs "%.1f%%")() }
                                                    child(Components.label(text).tooltip(tooltip))
                                                }
                                            })
                                        })
                                        child2(verticalScroll(Sizing.fill(100), Sizing.fill(100), 5, overlapped = true).apply { // 特性ポエム
                                            scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xA0FFFFFF.toInt())))

                                            child().child(Components.label(text { display.trait.poem }).apply {
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

                                    display.inputEntries.chunked(9).forEach { inputs ->
                                        child(Containers.horizontalFlow(Sizing.fill(100), Sizing.content()).apply { // 種子の行
                                            horizontalAlignment(HorizontalAlignment.LEFT)

                                            inputs.forEach { input ->
                                                child(adapter.wrap(Widgets.createSlot(Point(0, 0)).entries(input).disableBackground().markInput())) // 種子
                                            }
                                        })
                                    }
                                })
                                child().child(verticalSpace(2))
                                /*
                                display.trait.spawnSpecs.forEach { spawnSpec -> // 出現条件
                                    child().child(Containers.stack(Sizing.fill(100), Sizing.content()).apply {
                                        child(Components.label(text { spawnSpec.level.toString(2)() }).color(Color.ofRgb(0xFF404040.toInt())).shadow(false).apply { // レベル
                                            sizing(Sizing.fixed(30), Sizing.content())
                                            margins(Insets.of(0, 0, 0, 0))
                                            horizontalTextAlignment(HorizontalAlignment.RIGHT)
                                        })
                                        val rarityText = when (spawnSpec.rarity) {
                                            TraitSpawnRarity.ALWAYS -> text { "100%"() }
                                            TraitSpawnRarity.COMMON -> text { ">99%"() }
                                            TraitSpawnRarity.NORMAL -> text { "<90%"() }
                                            TraitSpawnRarity.RARE -> text { "<8%"() }
                                            TraitSpawnRarity.S_RARE -> text { "<1%"() }
                                        }
                                        child(Components.label(rarityText).color(Color.ofRgb(0xFF404040.toInt())).shadow(false).apply { // 出現頻度
                                            sizing(Sizing.fixed(40), Sizing.content())
                                            margins(Insets.of(0, 0, 30, 0))
                                            horizontalTextAlignment(HorizontalAlignment.CENTER)
                                        })
                                        child(Components.label(spawnSpec.condition.description).color(Color.ofRgb(0xFF404040.toInt())).shadow(false).apply { // 条件
                                            sizing(Sizing.fill(100), Sizing.content())
                                            margins(Insets.of(0, 0, 70, 0))
                                            horizontalTextAlignment(HorizontalAlignment.LEFT)
                                        })
                                    })
                                }
                                */
                            })
                        }
                        cardView = ClickableContainer(Sizing.fill(100), Sizing.fill(100)).apply {
                            onClick.register {
                                currentViewMode = ViewMode.SEPARATED
                                updateViewMode()
                                true
                            }
                            child(topBorderLayout(Sizing.fill(100), Sizing.fill(100)).apply { // カード
                                surface(NinePatchTextureCard.TRAIT_BACKGROUND.surface)
                                padding(Insets.of(5))
                                gap = 5

                                child1(Containers.verticalFlow(Sizing.fill(100), Sizing.content()).apply {
                                    child(Components.label(text { display.trait.getName().style(display.trait.style) }).apply { // 特性名
                                        sizing(Sizing.fill(100), Sizing.content())
                                        horizontalTextAlignment(HorizontalAlignment.CENTER)
                                    })
                                    child(verticalSpace(5))
                                    child(Containers.stack(Sizing.fill(100), Sizing.fixed(32)).apply { // 特性アイコン欄
                                        child(Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100)).apply { // 条件
                                            horizontalAlignment(HorizontalAlignment.LEFT)
                                            verticalAlignment(VerticalAlignment.BOTTOM)

                                            display.trait.conditions.forEach {
                                                child(Components.label(it.emoji).tooltip(it.name))
                                            }
                                        })
                                        child(Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100)).apply { // 特性アイコン
                                            horizontalAlignment(HorizontalAlignment.CENTER)
                                            verticalAlignment(VerticalAlignment.CENTER)

                                            child(Components.texture(display.trait.texture, 0, 0, 32, 32, 32, 32))
                                        })
                                        child(Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100)).apply { // 効果
                                            horizontalAlignment(HorizontalAlignment.RIGHT)
                                            verticalAlignment(VerticalAlignment.BOTTOM)

                                            display.trait.traitEffectKeyEntries.forEach {
                                                val text = text { (it.factor * 100.0 formatAs "%.1f%%")() + " "() + it.traitEffectKey.emoji.style(it.traitEffectKey.style) }
                                                val tooltip = text { it.traitEffectKey.name }
                                                child(Components.label(text).tooltip(tooltip))
                                            }
                                        })
                                    })
                                })
                                child2(verticalScroll(Sizing.fill(100), Sizing.fill(100), 5, overlapped = true).apply { // 特性ポエム
                                    scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xA0FFFFFF.toInt())))

                                    child().child(Components.label(text { display.trait.poem }).apply {
                                        sizing(Sizing.fill(100), Sizing.content())
                                        horizontalTextAlignment(HorizontalAlignment.LEFT)
                                    })
                                })
                            })
                        }
                        container = Containers.stack(Sizing.fill(100), Sizing.fill(100))
                        child(container)
                    }

                    updateViewMode()

                    adapter.prepare()
                },
            )
        }
    }
}
