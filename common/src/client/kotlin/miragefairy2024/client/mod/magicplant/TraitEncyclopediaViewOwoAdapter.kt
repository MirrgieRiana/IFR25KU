package miragefairy2024.client.mod.magicplant

import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.Color
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.core.VerticalAlignment
import miragefairy2024.client.mod.recipeviewer.ViewOwoAdapter
import miragefairy2024.client.mod.recipeviewer.ViewOwoAdapterContext
import miragefairy2024.client.mod.surface
import miragefairy2024.client.util.ClickableContainer
import miragefairy2024.client.util.OwoComponent
import miragefairy2024.client.util.SlotType
import miragefairy2024.client.util.leftBorderLayout
import miragefairy2024.client.util.topBorderLayout
import miragefairy2024.client.util.verticalScroll
import miragefairy2024.client.util.verticalSpace
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.magicplant.TraitEncyclopediaRecipeViewerCategoryCard
import miragefairy2024.mod.magicplant.TraitEncyclopediaView
import miragefairy2024.mod.magicplant.getName
import miragefairy2024.mod.magicplant.style
import miragefairy2024.mod.magicplant.texture
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.views.CatalystSlotView
import miragefairy2024.mod.recipeviewer.views.noBackground
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.fire
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.register
import miragefairy2024.util.style
import miragefairy2024.util.text
import miragefairy2024.util.toIngredientStack
import mirrg.kotlin.hydrogen.formatAs

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
