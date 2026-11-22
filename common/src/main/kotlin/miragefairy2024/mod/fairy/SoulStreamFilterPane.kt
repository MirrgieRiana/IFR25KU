package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.passiveskill.PassiveSkillEffect
import miragefairy2024.mod.passiveskill.PassiveSkillEffectFilter
import miragefairy2024.mod.passiveskill.passiveSkillEffectRegistry
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.plus
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.mod.recipeviewer.views.LayeredImageButtonView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.TooltipBackgroundView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.WrapperView
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.XSpaceView
import miragefairy2024.mod.recipeviewer.views.YFlatScrollbarView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YScrollContainerView
import miragefairy2024.mod.recipeviewer.views.YSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.connectScrollbar
import miragefairy2024.mod.recipeviewer.views.margin
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.mod.recipeviewer.views.tooltip
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.blue
import miragefairy2024.util.bold
import miragefairy2024.util.gold
import miragefairy2024.util.invoke
import miragefairy2024.util.observeAndInitialize
import miragefairy2024.util.register
import miragefairy2024.util.text
import net.minecraft.resources.ResourceLocation

private val BUTTON_14_BACKGROUND = LayeredImageButtonView.Sprites(
    MirageFairy2024.identifier("button_14/background_on"),
    MirageFairy2024.identifier("button_14/background"),
    MirageFairy2024.identifier("button_14/background_on_focused"),
    MirageFairy2024.identifier("button_14/background_focused"),
)

internal class SoulStreamFilterPane(
    private val enabledPassiveSkillEffectFilters: ObservableValue<Map<ResourceLocation, PassiveSkillEffectFilter<*>>>,
    enableGlobalFairyHighlight: ObservableValue<Boolean>,
    private val onClosed: EventRegistry<() -> Unit>,
) {

    val scrollView = YScrollContainerView()

    val view = View {
        view += YListView().configure {
            view.sizingX = Sizing.FILL
            view.sizingY = Sizing.FILL

            view += XListView().configure {
                view += LayeredImageButtonView(IntPoint(14, 14)).configure {
                    view.backgroundSprites = BUTTON_14_BACKGROUND
                    view.foregroundSprites = LayeredImageButtonView.Sprites.of(MirageFairy2024.identifier("button_14/clear_foreground"))
                    view.tooltip.value = text { SOUL_STREAM_RESET_HIGHLIGHTS_TRANSLATION() }
                    view.onClick.register {
                        enabledPassiveSkillEffectFilters.value = emptyMap()
                    }
                }
                view += XSpaceView(2)
                view += LayeredImageButtonView(IntPoint(14, 14)).configure {
                    view.backgroundSprites = BUTTON_14_BACKGROUND
                    view.foregroundSprites = LayeredImageButtonView.Sprites.of(MirageFairy2024.identifier("button_14/global_search_foreground"))
                    view.tooltip.value = text { SOUL_STREAM_GLOBAL_SEARCH_TRANSLATION() }
                    enableGlobalFairyHighlight.observeAndInitialize(onClosed) { _, enabled ->
                        view.selected.value = enabled
                    }
                    view.onClick.register {
                        enableGlobalFairyHighlight.value = !enableGlobalFairyHighlight.value
                    }
                }
            }
            view += YSpaceView(2)
            view += StackView().configure {
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL
                position.weight = 1.0

                view += TooltipBackgroundView()
                view += XListView().configure {
                    view.sizingX = Sizing.FILL
                    view.sizingY = Sizing.FILL

                    view += scrollView.configure {
                        position.weight = 1.0
                        position.ignoreLayoutX = true
                        view += createFilterList()
                    }
                    val scrollbarView = YFlatScrollbarView().also {
                        it.scrollbarColor = 0xA0FFFFFF.toInt()
                    }
                    view += scrollbarView
                    connectScrollbar(scrollView, scrollbarView)
                }.margin(4)
            }
        }
    }

    private fun createFilterList(): View {
        return View {
            view += YListView().configure {
                view.sizingX = Sizing.FILL

                view += TextView(text { SOUL_STREAM_PASSIVE_SKILL_EFFECT_TRANSLATION().blue.bold }).configure {
                    view.sizingX = Sizing.FILL
                    view.alignmentX = Alignment.CENTER
                    view.shadow = false
                }
                passiveSkillEffectRegistry.entrySet().sortedBy { it.key.location() }.forEach { (_, effect) ->
                    fun <T : Any> addFilters(effect: PassiveSkillEffect<T>) {
                        val samples = motifRegistry.entrySet()
                            .flatMap { it.value.passiveSkillSpecifications }
                            .mapNotNull { if (it.effect == effect) effect.castOrNull(it.valueProvider(10.0)) else null }
                        effect.getFilters(samples).sortedBy { it.identifier }.forEach { filter ->
                            fun label(filters: Map<ResourceLocation, PassiveSkillEffectFilter<*>>) = if (filter.identifier in filters) filter.text.gold else filter.text

                            view += SoulStreamFilterView {
                                val filters = enabledPassiveSkillEffectFilters.value
                                enabledPassiveSkillEffectFilters.value = if (filter.identifier in filters) filters - filter.identifier else filters + (filter.identifier to filter)
                            }.configure {
                                view += TextView().configure {
                                    view.shadow = false
                                    enabledPassiveSkillEffectFilters.observeAndInitialize(onClosed) { _, filters ->
                                        view.text.value = label(filters).visualOrderText
                                    }
                                }.tooltip { _, _ -> listOf(label(enabledPassiveSkillEffectFilters.value)) }
                            }
                        }
                    }
                    addFilters(effect)
                }
            }
        }
    }

}

class SoulStreamFilterView(val onClick: () -> Unit) : WrapperView(), PlaceableView {

    override val sizingX = Sizing.FILL

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): ViewController {
        return super.attachTo(offset, viewPlacer) + viewPlacer.place(this, offset.sized(evaluateActualSize()))
    }

}
