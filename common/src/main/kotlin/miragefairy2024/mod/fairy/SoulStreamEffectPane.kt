package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.AttachmentChangedEvent
import miragefairy2024.mod.passiveskill.PassiveSkillEffect
import miragefairy2024.mod.passiveskill.PassiveSkillResult
import miragefairy2024.mod.passiveskill.collect
import miragefairy2024.mod.passiveskill.effects.ManaBoostPassiveSkillEffect
import miragefairy2024.mod.passiveskill.findPassiveSkillProviders
import miragefairy2024.mod.passiveskill.passiveSkillEffectRegistry
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.views.AlignView
import miragefairy2024.mod.recipeviewer.views.DynamicView
import miragefairy2024.mod.recipeviewer.views.LayeredImageButtonView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.TooltipBackgroundView
import miragefairy2024.mod.recipeviewer.views.View
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
import miragefairy2024.util.getOrDefault
import miragefairy2024.util.gold
import miragefairy2024.util.invoke
import miragefairy2024.util.observe
import miragefairy2024.util.plus
import miragefairy2024.util.red
import miragefairy2024.util.register
import miragefairy2024.util.text
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player

private val BUTTON_14_BACKGROUND = LayeredImageButtonView.Sprites(
    MirageFairy2024.identifier("button_14/background_on"),
    MirageFairy2024.identifier("button_14/background"),
    MirageFairy2024.identifier("button_14/background_on_focused"),
    MirageFairy2024.identifier("button_14/background_focused"),
)

internal class SoulStreamEffectPane(
    private val player: Player,
    collectionToggleKeyText: Component,
    setCollectionEnabled: (Boolean) -> Unit,
    onClosed: EventRegistry<() -> Unit>,
) {

    private var effects = collectPassiveSkillTexts()
    private val effectList = DynamicView(createEffectList(effects))

    val scrollView = YScrollContainerView().also {
        it += effectList
    }

    val view = View {
        view += YListView().configure {
            view.sizingX = Sizing.FILL
            view.sizingY = Sizing.FILL

            view += LayeredImageButtonView(IntPoint(14, 14)).configure {
                view.backgroundSprites = BUTTON_14_BACKGROUND
                view.foregroundSprites = LayeredImageButtonView.Sprites(
                    MirageFairy2024.identifier("button_14/collection_enabled_foreground"),
                    MirageFairy2024.identifier("button_14/collection_disabled_foreground"),
                    MirageFairy2024.identifier("button_14/collection_enabled_foreground"),
                    MirageFairy2024.identifier("button_14/collection_disabled_foreground"),
                )
                fun updateButton() {
                    val enabled = player.collectionEnabled.getOrDefault()
                    view.selected.value = enabled
                    view.tooltip.value = text {
                        val message = if (enabled) COLLECTION_ENABLED_TRANSLATION() else COLLECTION_DISABLED_TRANSLATION()
                        message + " ("() + collectionToggleKeyText + ")"()
                    }
                }
                AttachmentChangedEvent.eventRegistry.observe(onClosed) { identifier ->
                    if (identifier == COLLECTION_ENABLED_ATTACHMENT_TYPE.identifier()) {
                        updateButton()
                    }
                }
                updateButton()
                view.onClick.register {
                    val enabled = !view.selected.value
                    view.selected.value = enabled
                    if (player.collectionEnabled.getOrDefault() != enabled) setCollectionEnabled(enabled)
                }
            }
            view += YSpaceView(2)
            view += StackView().configure {
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL
                position.weight = 1.0

                view += TooltipBackgroundView()
                view += StackView().configure {
                    view.sizingX = Sizing.FILL
                    view.sizingY = Sizing.FILL

                    view += scrollView
                    val scrollbarView = YFlatScrollbarView().also {
                        it.scrollbarColor = 0xA0FFFFFF.toInt()
                    }
                    view += AlignView().configure {
                        view.alignmentX = Alignment.END
                        view += scrollbarView
                    }
                    connectScrollbar(scrollView, scrollbarView)
                }.margin(4)
            }
        }
    }

    fun updateEffects() {
        val newEffects = collectPassiveSkillTexts()
        if (effects == newEffects) return
        effects = newEffects
        effectList.content.value = createEffectList(effects)
        scrollView.updateScrollBounds()
        scrollView.clampScrollOffset()
    }

    private fun createEffectList(effects: List<Component>): View {
        return View {
            view += YListView().configure {
                if (effects.isEmpty()) {
                    view += TextView(text { SOUL_STREAM_NO_PASSIVE_SKILL_EFFECTS_TRANSLATION().red }).configure {
                        view.shadow = false
                    }
                } else {
                    effects.forEach {
                        view += TextView(it).configure {
                            view.shadow = false
                        }.tooltip(it)
                    }
                }
            }
        }
    }

    private fun collectPassiveSkillTexts(): List<Component> {
        val passiveSkillProviders = player.findPassiveSkillProviders()
        val result = PassiveSkillResult()
        result.collect(passiveSkillProviders.passiveSkills, player, ManaBoostPassiveSkillEffect.Value(mapOf()), true)
        val manaBoostValue = result[ManaBoostPassiveSkillEffect]
        result.collect(passiveSkillProviders.passiveSkills, player, manaBoostValue, false)
        return result.map.keys.sortedBy { passiveSkillEffectRegistry.getKey(it) }.flatMap { effect ->
            fun <T : Any> getTexts(effect: PassiveSkillEffect<T>) = effect.getTexts(result[effect]).map { it.gold }
            getTexts(effect)
        }
    }

}
