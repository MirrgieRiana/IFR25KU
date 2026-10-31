package miragefairy2024.mod.fairy

import com.mojang.serialization.Codec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.clientProxy
import miragefairy2024.mod.common.SlotType
import miragefairy2024.mod.passiveskill.PassiveSkillEffect
import miragefairy2024.mod.passiveskill.PassiveSkillEffectFilter
import miragefairy2024.mod.passiveskill.PassiveSkillResult
import miragefairy2024.mod.passiveskill.collect
import miragefairy2024.mod.passiveskill.effects.ManaBoostPassiveSkillEffect
import miragefairy2024.mod.passiveskill.findPassiveSkillProviders
import miragefairy2024.mod.passiveskill.passiveSkillEffectRegistry
import miragefairy2024.mod.recipeviewer.vanilla.ViewProviderMenu
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.views.AbsoluteView
import miragefairy2024.mod.recipeviewer.views.DynamicView
import miragefairy2024.mod.recipeviewer.views.FilledRectangleView
import miragefairy2024.mod.recipeviewer.views.ImageButtonView
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.ScrollbarStyle
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.TooltipBackgroundView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.XSpaceView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YScrollView
import miragefairy2024.mod.recipeviewer.views.YSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.margin
import miragefairy2024.mod.recipeviewer.views.maxContentSizeX
import miragefairy2024.mod.recipeviewer.views.menuSlotView
import miragefairy2024.mod.recipeviewer.views.minContentSizeX
import miragefairy2024.mod.recipeviewer.views.onClick
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.mod.recipeviewer.views.tooltip
import miragefairy2024.util.Channel
import miragefairy2024.util.EnJa
import miragefairy2024.util.ItemStacks
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.Registration
import miragefairy2024.util.Translation
import miragefairy2024.util.blue
import miragefairy2024.util.bold
import miragefairy2024.util.dummyUnitStreamCodec
import miragefairy2024.util.enJa
import miragefairy2024.util.get
import miragefairy2024.util.getOrCreate
import miragefairy2024.util.getOrDefault
import miragefairy2024.util.gold
import miragefairy2024.util.invoke
import miragefairy2024.util.isIn
import miragefairy2024.util.plus
import miragefairy2024.util.quickMove
import miragefairy2024.util.red
import miragefairy2024.util.register
import miragefairy2024.util.registerServerPacketReceiver
import miragefairy2024.util.registerViewScreen
import miragefairy2024.util.sendToServer
import miragefairy2024.util.set
import miragefairy2024.util.size
import miragefairy2024.util.text
import miragefairy2024.util.toItemTag
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

private val SOUL_STREAM_TRANSLATION = Translation({ "container.${MirageFairy2024.MOD_ID}.soul_stream" }, "Soul Stream", "ソウルストリーム")
val OPEN_SOUL_STREAM_KEY_TRANSLATION = Translation({ "key.${MirageFairy2024.MOD_ID}.open_soul_stream" }, "Open Soul Stream", "ソウルストリームを開く")
val SOUL_STREAM_RESET_HIGHLIGHTS_TRANSLATION = Translation({ "gui.${MirageFairy2024.identifier("soul_stream").toLanguageKey()}.reset_highlights" }, "Reset Highlights", "ハイライトをリセット")
val SOUL_STREAM_GLOBAL_SEARCH_TRANSLATION = Translation({ "gui.${MirageFairy2024.identifier("soul_stream").toLanguageKey()}.global_search" }, "Highlight even outside of Soul Stream", "ソウルストリームの外でもハイライト")
val SOUL_STREAM_PASSIVE_SKILL_EFFECT_TRANSLATION = Translation({ "gui.${MirageFairy2024.identifier("soul_stream").toLanguageKey()}.passive_skill_effect" }, "Passive Skill Effect", "パッシブスキル効果")
val SOUL_STREAM_NO_PASSIVE_SKILL_EFFECTS_TRANSLATION = Translation({ "gui.${MirageFairy2024.identifier("soul_stream").toLanguageKey()}.no_passive_skill_effects" }, "No passive skill effects", "パッシブスキル効果なし")

val SOUL_STREAM_CONTAINABLE_TAG = MirageFairy2024.identifier("soul_stream_containable").toItemTag()

context(ModContext)
fun initSoulStream() {

    // プレイヤー追加データ
    SOUL_STREAM_ATTACHMENT_TYPE.register()

    // ソウルストリームを開く要求パケット
    ModEvents.onInitialize {
        OpenSoulStreamChannel.registerServerPacketReceiver { player, _ ->
            player.openMenu(object : ExtendedScreenHandlerFactory<Unit> {
                override fun createMenu(syncId: Int, playerInventory: Inventory, player: Player): AbstractContainerMenu {
                    return SoulStreamScreenHandler(syncId, playerInventory, player.soulStream.getOrCreate())
                }

                override fun getDisplayName() = text { SOUL_STREAM_TRANSLATION() }

                override fun getScreenOpeningData(player: ServerPlayer) = Unit
            })
        }
    }

    // GUI
    soulStreamScreenHandlerType.register()
    soulStreamScreenHandlerType.registerViewScreen()

    // 翻訳
    SOUL_STREAM_TRANSLATION.enJa()
    OPEN_SOUL_STREAM_KEY_TRANSLATION.enJa()
    SOUL_STREAM_RESET_HIGHLIGHTS_TRANSLATION.enJa()
    SOUL_STREAM_GLOBAL_SEARCH_TRANSLATION.enJa()
    SOUL_STREAM_PASSIVE_SKILL_EFFECT_TRANSLATION.enJa()
    SOUL_STREAM_NO_PASSIVE_SKILL_EFFECTS_TRANSLATION.enJa()
    SOUL_STREAM_CONTAINABLE_TAG.enJa(EnJa("Soul Stream Containable", "ソウルストリームに格納可能"))

}


// 拡張プレイヤーデータ

val SOUL_STREAM_ATTACHMENT_TYPE: AttachmentType<SoulStream> = AttachmentRegistry.create(MirageFairy2024.identifier("soul_stream")) {
    it.persistent(SoulStream.CODEC)
    it.initializer(::SoulStream)
    it.syncWith(SoulStream.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
    it.copyOnDeath()
}

val Entity.soulStream get() = this[SOUL_STREAM_ATTACHMENT_TYPE]

class SoulStream() : SimpleContainer(SLOT_COUNT) {
    companion object {
        const val SLOT_COUNT = 9 * 31
        const val PASSIVE_SKILL_SLOT_COUNT = 9

        val CODEC: Codec<SoulStream> = ItemStacks.CODEC.xmap(::fromItemStacks, ::toItemStacks)
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, SoulStream> = ItemStacks.STREAM_CODEC.map(::fromItemStacks, ::toItemStacks)

        fun fromItemStacks(itemStacks: ItemStacks) = SoulStream(itemStacks.itemStacks)
        fun toItemStacks(soulStream: SoulStream) = ItemStacks(soulStream.items)
    }

    constructor(items: List<ItemStack>) : this() {
        items.forEachIndexed { index, itemStack ->
            if (index < size) this[index] = itemStack
        }
    }
}


// ソウルストリームを開く要求パケット

object OpenSoulStreamChannel : Channel<Unit>(MirageFairy2024.identifier("open_soul_stream")) {
    override fun writeToBuf(buf: RegistryFriendlyByteBuf, packet: Unit) = Unit
    override fun readFromBuf(buf: RegistryFriendlyByteBuf) = Unit
}


// GUI

val enabledPassiveSkillEffectFilters = ObservableValue<Map<ResourceLocation, PassiveSkillEffectFilter<*>>>(mapOf())
val enableGlobalFairyHighlight = ObservableValue(false)

val soulStreamScreenHandlerType = Registration(BuiltInRegistries.MENU, MirageFairy2024.identifier("soul_stream")) {
    ExtendedScreenHandlerType({ syncId, playerInventory, _ ->
        SoulStreamScreenHandler(syncId, playerInventory, clientProxy!!.getClientPlayer()!!.soulStream.getOrCreate())
    }, dummyUnitStreamCodec())
}

class SoulStreamScreenHandler(syncId: Int, val playerInventory: Inventory, val soulStream: Container) : AbstractContainerMenu(soulStreamScreenHandlerType(), syncId), ViewProviderMenu {
    init {
        repeat(3) { r ->
            repeat(9) { c ->
                addSlot(Slot(playerInventory, 9 + 9 * r + c, 0, 0))
            }
        }
        repeat(9) { c ->
            addSlot(Slot(playerInventory, c, 0, 0))
        }
        repeat(soulStream.size) { i ->
            addSlot(object : Slot(soulStream, i, 0, 0) {
                override fun mayPlace(stack: ItemStack) = stack isIn SOUL_STREAM_CONTAINABLE_TAG
            })
        }
    }

    override fun stillValid(player: Player) = true
    override fun quickMoveStack(player: Player, slot: Int): ItemStack {
        val playerIndices = 9 * 4 - 1 downTo 0
        val utilityIndices = 9 * 4 + 9 until 9 * 4 + SoulStream.SLOT_COUNT
        val destinationIndices = if (slot in playerIndices) utilityIndices else playerIndices
        return quickMove(slot, destinationIndices)
    }

    override val closeKeyMappings = listOf(OPEN_SOUL_STREAM_KEY_TRANSLATION.keyGetter())

    private val onViewUpdate = mutableListOf<() -> Unit>()
    private var lastUpdateEventNanoTime = 0L

    override fun tickView() {
        onViewUpdate.forEach {
            it()
        }
    }

    override fun createView(title: Component, playerInventoryTitle: Component): View {
        onViewUpdate.clear()
        lastUpdateEventNanoTime = 0L
        val player = playerInventory.player

        fun slotRowView(indices: Iterable<Int>, type: SlotType) = View {
            view += XListView().configure {
                indices.forEach { index ->
                    view += menuSlotView(slots[index], type)
                }
            }
        }

        fun buttonView(foreground: (Boolean) -> ResourceLocation, valueProvider: () -> Boolean, tooltipProvider: () -> Component, onClick: () -> Unit) = View {
            val value = valueProvider()
            val iconContent = ObservableValue<View>(ImageView(ViewTexture(foreground(value), IntPoint(14, 14))))
            var lastValue = value

            view += AbsoluteView(IntPoint(14, 14)).configure {
                view += ImageButtonView(IntPoint(14, 14)).configure {
                    val buttonView = view
                    fun update() {
                        val value = valueProvider()
                        buttonView.texture = ViewTexture(MirageFairy2024.identifier(if (value) "textures/gui/sprites/button_14/background_on.png" else "textures/gui/sprites/button_14/background.png"), IntPoint(14, 14))
                        buttonView.hoveredTexture = ViewTexture(MirageFairy2024.identifier(if (value) "textures/gui/sprites/button_14/background_on_focused.png" else "textures/gui/sprites/button_14/background_focused.png"), IntPoint(14, 14))
                        if (lastValue != value) {
                            lastValue = value
                            iconContent.value = ImageView(ViewTexture(foreground(value), IntPoint(14, 14)))
                        }
                    }
                    onViewUpdate += ::update
                    update()
                    view.onClick.register {
                        onClick()
                        tickView()
                        true
                    }
                }
                view += DynamicView(iconContent)
            }.tooltip { _, _ -> listOf(tooltipProvider()) }
        }

        fun passiveSkillEffectsView() = View {
            view += YListView().configure {
                view.sizingX = Sizing.FILL

                val passiveSkillProviders = player.findPassiveSkillProviders()
                val result = PassiveSkillResult()
                result.collect(passiveSkillProviders.passiveSkills, player, ManaBoostPassiveSkillEffect.Value(mapOf()), true)
                val manaBoostValue = result[ManaBoostPassiveSkillEffect]
                result.collect(passiveSkillProviders.passiveSkills, player, manaBoostValue, false)

                if (result.map.isEmpty()) {
                    view += MultiLineTextView().configure {
                        view.text.value = text { SOUL_STREAM_NO_PASSIVE_SKILL_EFFECTS_TRANSLATION().red }
                        view.shadow = false
                    }
                } else {
                    result.map.keys.sortedBy { passiveSkillEffectRegistry.getKey(it) }.forEach { effect ->
                        fun <T : Any> effectView(effect: PassiveSkillEffect<T>) = View {
                            view += YListView().configure {
                                view.sizingX = Sizing.FILL

                                effect.getTexts(result[effect]).forEach { text ->
                                    view += MultiLineTextView().configure {
                                        view.text.value = text.gold
                                        view.shadow = false
                                    }.tooltip(text.gold)
                                }
                            }
                        }
                        view += effectView(effect)
                    }
                }
            }
        }

        val passiveSkillEffectsContent = ObservableValue(passiveSkillEffectsView())
        onViewUpdate += {
            val now = System.nanoTime()
            if (now - lastUpdateEventNanoTime >= 100_000_000) {
                lastUpdateEventNanoTime = now
                passiveSkillEffectsContent.value = passiveSkillEffectsView()
            }
        }

        val rootView = View {
            view += StackView().configure {
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL

                view += FilledRectangleView().configure {
                    view.color.value = 0xFFC6C6C6.toInt()
                }
                view += XListView().configure {
                    view.sizingX = Sizing.FILL
                    view.sizingY = Sizing.FILL

                    view += YListView().configure {
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL
                        position.weight = 1.0
                        position.ignoreLayoutX = true

                        view += XListView().configure {
                            view += buttonView(
                                { MirageFairy2024.identifier("textures/gui/sprites/button_14/clear_foreground.png") },
                                { false },
                                { text { SOUL_STREAM_RESET_HIGHLIGHTS_TRANSLATION() } },
                                { enabledPassiveSkillEffectFilters.value = mapOf() },
                            )
                            view += XSpaceView(2)
                            view += buttonView(
                                { MirageFairy2024.identifier("textures/gui/sprites/button_14/global_search_foreground.png") },
                                { enableGlobalFairyHighlight.value },
                                { text { SOUL_STREAM_GLOBAL_SEARCH_TRANSLATION() } },
                                { enableGlobalFairyHighlight.value = !enableGlobalFairyHighlight.value },
                            )
                        }
                        view += YSpaceView(2)
                        view += StackView().configure {
                            view.sizingX = Sizing.FILL
                            view.sizingY = Sizing.FILL
                            position.weight = 1.0
                            position.ignoreLayoutY = true

                            view += TooltipBackgroundView()
                            view += YScrollView().configure {
                                view.scrollbarColor = 0xA0FFFFFF.toInt()

                                view += YListView().configure {
                                    view.sizingX = Sizing.FILL

                                    view += MultiLineTextView().configure {
                                        view.text.value = text { SOUL_STREAM_PASSIVE_SKILL_EFFECT_TRANSLATION().blue.bold }
                                        view.alignmentX = Alignment.CENTER
                                        view.shadow = false
                                    }
                                    passiveSkillEffectRegistry.entrySet().sortedBy { it.key.location() }.forEach { (_, effect) ->
                                        fun <T : Any> filterListView(effect: PassiveSkillEffect<T>) = View {
                                            view += YListView().configure {
                                                view.sizingX = Sizing.FILL

                                                val samples = motifRegistry.entrySet()
                                                    .flatMap { it.value.passiveSkillSpecifications }
                                                    .mapNotNull { if (it.effect == effect) effect.castOrNull(it.valueProvider(10.0)) else null }
                                                effect.getFilters(samples).sortedBy { it.identifier }.forEach { filter ->
                                                    fun labelView(enabled: Boolean) = MultiLineTextView().also {
                                                        it.text.value = if (enabled) filter.text.gold else filter.text
                                                        it.shadow = false
                                                    }
                                                    var enabled = filter.identifier in enabledPassiveSkillEffectFilters.value
                                                    val labelContent = ObservableValue<View>(labelView(enabled))
                                                    onViewUpdate += {
                                                        val newValue = filter.identifier in enabledPassiveSkillEffectFilters.value
                                                        if (enabled != newValue) {
                                                            enabled = newValue
                                                            labelContent.value = labelView(enabled)
                                                        }
                                                    }
                                                    view += DynamicView(labelContent).configure {}.onClick {
                                                        enabledPassiveSkillEffectFilters.value = if (filter.identifier in enabledPassiveSkillEffectFilters.value) {
                                                            enabledPassiveSkillEffectFilters.value - filter.identifier
                                                        } else {
                                                            enabledPassiveSkillEffectFilters.value + (filter.identifier to filter)
                                                        }
                                                        tickView()
                                                        true
                                                    }.tooltip { _, _ ->
                                                        listOf(if (filter.identifier in enabledPassiveSkillEffectFilters.value) filter.text.gold else filter.text)
                                                    }
                                                }
                                            }
                                        }
                                        view += filterListView(effect)
                                    }
                                }
                            }.margin(4)
                        }
                    }.margin(0, 0, 0, 20)
                    view += XSpaceView(4)
                    view += YListView().configure {
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL

                        view += slotRowView(36 until 45, SlotType.FAIRY)
                        view += YSpaceView(4)
                        view += StackView().configure {
                            view.sizingX = Sizing.FILL
                            view.sizingY = Sizing.FILL
                            position.weight = 1.0
                            position.ignoreLayoutY = true

                            view += NinePatchImageView(SlotType.NORMAL.viewTexture.id, 18, 18, 0, 18, 0, 0, 18, 0, true)
                            view += YScrollView().configure {
                                view.scrollbarThickness = 18
                                view.scrollbarStyle = ScrollbarStyle.VANILLA
                                view.scrollStep = 18

                                view += YListView().configure {
                                    (9 until soulStream.size).chunked(9).forEach { indices ->
                                        view += slotRowView(indices.map { 36 + it }, SlotType.NORMAL)
                                    }
                                }
                            }
                        }
                        view += YSpaceView(4)
                        repeat(3) { r ->
                            view += slotRowView(9 * r until 9 * r + 9, SlotType.NORMAL)
                        }
                        view += YSpaceView(4)
                        view += slotRowView(27 until 36, SlotType.NORMAL)
                        view += YSpaceView(5)
                        view += MultiLineTextView().configure {
                            position.ignoreLayoutX = true
                            view.text.value = title
                            view.alignmentX = Alignment.CENTER
                            view.color = ColorPair.DARK_GRAY
                            view.shadow = false
                        }.margin(1, 0)
                        view += YSpaceView(5)
                    }.minContentSizeX(180).maxContentSizeX(180)
                    view += XSpaceView(4)
                    view += YListView().configure {
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL
                        position.weight = 1.0
                        position.ignoreLayoutX = true

                        view += buttonView(
                            { MirageFairy2024.identifier(if (it) "textures/gui/sprites/button_14/collection_enabled_foreground.png" else "textures/gui/sprites/button_14/collection_disabled_foreground.png") },
                            { player.collectionEnabled.getOrDefault() },
                            { text { (if (player.collectionEnabled.getOrDefault()) COLLECTION_ENABLED_TRANSLATION() else COLLECTION_DISABLED_TRANSLATION()) + " ("() + clientProxy!!.getKeyMappingText(TOGGLE_COLLECTION_ENABLED_KEY_TRANSLATION.keyGetter()) + ")"() } },
                            { SetCollectionEnabledChannel.sendToServer(!player.collectionEnabled.getOrDefault()) },
                        )
                        view += YSpaceView(2)
                        view += StackView().configure {
                            view.sizingX = Sizing.FILL
                            view.sizingY = Sizing.FILL
                            position.weight = 1.0
                            position.ignoreLayoutY = true

                            view += TooltipBackgroundView()
                            view += YScrollView().configure {
                                view.scrollbarColor = 0xA0FFFFFF.toInt()
                                view.reserveScrollbarSpace = false

                                view += DynamicView(passiveSkillEffectsContent)
                            }.margin(4)
                        }
                    }.margin(0, 0, 0, 20)
                }.margin(4)
            }
        }
        tickView()
        return rootView
    }
}
