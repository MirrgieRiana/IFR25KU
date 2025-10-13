package miragefairy2024.mod.magicplant

import com.mojang.serialization.Codec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.Emoji
import miragefairy2024.mod.guiFullScreenTranslation
import miragefairy2024.mod.invoke
import miragefairy2024.mod.magicplant.contents.TraitEffectKeyCard
import miragefairy2024.mod.magicplant.contents.magicplants.MirageFlowerCard
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.views.CatalystSlotView
import miragefairy2024.mod.recipeviewer.views.ImageButtonView
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.MultiLineTextChildrenGenerator
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.PagingView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.XSpaceView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.margin
import miragefairy2024.mod.recipeviewer.views.minContentSizeX
import miragefairy2024.mod.recipeviewer.views.onClick
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.mod.recipeviewer.views.tooltip
import miragefairy2024.util.EnJa
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.Translation
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.fire
import miragefairy2024.util.gold
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.register
import miragefairy2024.util.sortedEntrySet
import miragefairy2024.util.style
import miragefairy2024.util.text
import miragefairy2024.util.toIngredientStack
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.core.RegistryAccess
import net.minecraft.world.item.ItemStack

private val identifier = MirageFairy2024.identifier("trait_encyclopedia")
private val DEFAULT_TRAIT_TRANSLATION = Translation({ identifier.toLanguageKey("gui", "default_trait") }, EnJa("Default Trait", "初期状態で発現"))
private val RANDOM_TRAIT_TRANSLATION = Translation({ identifier.toLanguageKey("gui", "random_trait") }, EnJa("Random Trait", "ランダムで発現"))

context(ModContext)
fun initTraitEncyclopedia() {
    TraitEncyclopediaRecipeViewerCategoryCard.init()

    DEFAULT_TRAIT_TRANSLATION.enJa()
    RANDOM_TRAIT_TRANSLATION.enJa()
}

val onOpenTraitEncyclopediaPageScreen = EventRegistry<(Trait) -> Boolean>()

object TraitEncyclopediaRecipeViewerCategoryCard : RecipeViewerCategoryCard<Trait>() {
    override fun getId() = MirageFairy2024.identifier("trait_encyclopedia")
    override fun getName() = EnJa("Trait Encyclopedia", "特性図鑑")
    override fun getIcon() = MirageFlowerCard.iconItem().createItemStack()
    override fun getRecipeCodec(registryAccess: RegistryAccess): Codec<Trait> = traitRegistry.byNameCodec()
    override fun getInputs(recipeEntry: RecipeEntry<Trait>) = getProducerMagicPlantSeedItemStacks(recipeEntry.recipe).map { Input(it.toIngredientStack(), true) }

    override fun createRecipeEntries(): Iterable<RecipeEntry<Trait>> {
        return traitRegistry.sortedEntrySet.map { (id, trait) ->
            RecipeEntry(id.location(), trait, true)
        }
    }

    override fun createView(recipeEntry: RecipeEntry<Trait>) = View {
        val pageIndex = ObservableValue(0)
        val pageCount = ObservableValue(0)

        view += StackView().configure {
            view.sizingX = Sizing.FILL
            view.sizingY = Sizing.FILL

            // 背景
            view += NinePatchImageView(MirageFairy2024.identifier("textures/gui/trait_background.png"), 22, 22, 22, 22, 22, 22)

            view += YListView().configure {
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL

                // 特性名
                view += TextView(recipeEntry.recipe.getName().style(recipeEntry.recipe.style)).configure {
                    view.sizingX = Sizing.FILL
                    view.alignmentX = Alignment.CENTER
                    view.scroll = true
                    view.tooltip = listOf(recipeEntry.recipe.getName())
                }

                view += YSpaceView(5)

                // 特性アイコン行
                view += XListView().configure {
                    view.sizingX = Sizing.FILL

                    // 条件リスト
                    view += YListView().configure {
                        position.alignmentY = Alignment.END
                        position.weight = 1.0
                        position.ignoreLayoutX = true
                        view.sizingX = Sizing.FILL
                        recipeEntry.recipe.conditions.forEach {
                            view += TextView(it.emoji).configure {
                                view.tooltip = listOf(it.name)
                            }
                        }
                    }

                    // 特性アイコン
                    val texture = ViewTexture(recipeEntry.recipe.texture, IntPoint(32, 32), IntRectangle(0, 0, 32, 32))
                    view += ImageView(texture).configure {
                        position.alignmentY = Alignment.END
                    }

                    // 効果リスト
                    view += YListView().configure {
                        position.alignmentY = Alignment.END
                        position.weight = 1.0
                        position.ignoreLayoutX = true
                        view.sizingX = Sizing.FILL
                        recipeEntry.recipe.traitEffectKeyEntries.forEach {
                            view += TextView(text { (it.factor * 100.0 formatAs "%.1f%%")() + " "() + it.traitEffectKey.emoji.style(it.traitEffectKey.style) }).configure {
                                position.alignmentX = Alignment.END
                                view.tooltip = listOf(it.traitEffectKey.name)
                            }
                        }
                    }

                }

                view += YSpaceView(5)

                // 特性ポエム
                view += PagingView().configure {
                    position.weight = 1.0
                    view += MultiLineTextChildrenGenerator(recipeEntry.recipe.poem) { Alignment.START }

                    view.pageCount.register { _, it ->
                        pageCount.value = it
                    }
                    view.pageIndex.register { _, it ->
                        pageIndex.value = it
                    }
                    pageIndex.register { _, it ->
                        view.pageIndex.value = it
                    }
                }.onClick {
                    onOpenTraitEncyclopediaPageScreen.fire {
                        if (it(recipeEntry.recipe)) return@onClick true
                    }
                    true
                }.tooltip(text { guiFullScreenTranslation() })

                view += YSpaceView(5)

                // ページ操作ボタン
                view += XListView().configure {
                    view.sizingX = Sizing.FILL

                    view += XListView().configure {
                        position.alignmentX = Alignment.START
                        position.weight = 1.0
                        position.ignoreLayoutX = true

                        val defaultTraitProducerMagicPlants = magicPlantCards
                            .mapNotNull { card -> card.defaultTraitBits[recipeEntry.recipe]?.let { Pair(card, it) } }
                        val defaultTraitProducers = defaultTraitProducerMagicPlants
                            .map { pair -> pair.first.item().createItemStack().also { it.setTraitStacks(TraitStacks.of(TraitStack(recipeEntry.recipe, 1))) } }
                            .toIngredientStack()

                        // デフォルト特性所持種子
                        view += CatalystSlotView(defaultTraitProducers).configure {
                            view.drawBackground = false
                            view.margin = 0
                        }

                        view += XSpaceView(2)

                        // デフォルト特性ラベル
                        view += TextView(Emoji.NATURAL().style(TraitEffectKeyCard.LEAVES_PRODUCTION.traitEffectKey.style)).configure {
                            position.alignmentY = Alignment.END
                            view.tooltip = listOf(text { DEFAULT_TRAIT_TRANSLATION().gold }) + defaultTraitProducerMagicPlants.map { text { it.second.toString(2)() + " "() + it.first.block().name } }
                            // TODO 項目数が増えたら省略
                            // TODO クリックで大きな表を表示
                        }

                    }

                    // 左ボタン
                    view += ImageButtonView(IntPoint(12, 12)).configure {
                        position.alignmentY = Alignment.CENTER
                        view.texture = ViewTexture(MirageFairy2024.identifier("textures/gui/sprites/button_14_left.png"), IntPoint(12, 36), IntRectangle(0, 0, 12, 12))
                        view.hoveredTexture = ViewTexture(MirageFairy2024.identifier("textures/gui/sprites/button_14_left.png"), IntPoint(12, 36), IntRectangle(0, 12, 12, 12))
                        view.disabledTexture = ViewTexture(MirageFairy2024.identifier("textures/gui/sprites/button_14_left.png"), IntPoint(12, 36), IntRectangle(0, 24, 12, 12))

                        fun update() {
                            view.enabled.value = pageIndex.value > 0
                        }
                        pageIndex.register { _, _ -> update() }
                        update()

                        view.onClick.register {
                            pageIndex.value -= 1
                            true
                        }
                    }

                    // ページ番号
                    view += TextView().configure {
                        position.alignmentY = Alignment.CENTER
                        view.sizingX = Sizing.FILL
                        view.alignmentX = Alignment.CENTER

                        fun update() {
                            view.text.value = text { "${pageIndex.value + 1}"() }.visualOrderText
                        }
                        pageIndex.register { _, _ -> update() }
                        update()
                    }.minContentSizeX(32)

                    // 右ボタン
                    view += ImageButtonView(IntPoint(12, 12)).configure {
                        position.alignmentY = Alignment.CENTER
                        view.texture = ViewTexture(MirageFairy2024.identifier("textures/gui/sprites/button_14_right.png"), IntPoint(12, 36), IntRectangle(0, 0, 12, 12))
                        view.hoveredTexture = ViewTexture(MirageFairy2024.identifier("textures/gui/sprites/button_14_right.png"), IntPoint(12, 36), IntRectangle(0, 12, 12, 12))
                        view.disabledTexture = ViewTexture(MirageFairy2024.identifier("textures/gui/sprites/button_14_right.png"), IntPoint(12, 36), IntRectangle(0, 24, 12, 12))

                        fun update() {
                            view.enabled.value = pageIndex.value < pageCount.value - 1
                        }
                        pageIndex.register { _, _ -> update() }
                        pageCount.register { _, _ -> update() }
                        update()

                        view.onClick.register {
                            pageIndex.value += 1
                            true
                        }
                    }

                    view += XListView().configure {
                        position.alignmentX = Alignment.END
                        position.weight = 1.0
                        position.ignoreLayoutX = true

                        val randomTraitProducerMagicPlants = magicPlantCards
                            .mapNotNull { card -> card.randomTraitChances[recipeEntry.recipe]?.let { Pair(card, it) } }
                        val randomTraitProducers = randomTraitProducerMagicPlants
                            .map { pair -> pair.first.item().createItemStack().also { it.setTraitStacks(TraitStacks.of(TraitStack(recipeEntry.recipe, 1))) } }
                            .toIngredientStack()

                        // ランダム特性ラベル
                        view += TextView(Emoji.MUTATION().style(TraitEffectKeyCard.MUTATION.traitEffectKey.style)).configure {
                            position.alignmentY = Alignment.END
                            view.tooltip = listOf(text { RANDOM_TRAIT_TRANSLATION().gold }) + randomTraitProducerMagicPlants.map { text { (it.second * 100.0 formatAs "%.0f%%")() + " "() + it.first.block().name } }
                            // TODO 項目数が増えたら省略
                            // TODO クリックで大きな表を表示
                        }

                        view += XSpaceView(2)

                        // ランダム特性種子
                        view += CatalystSlotView(randomTraitProducers).configure {
                            view.drawBackground = false
                            view.margin = 0
                        }

                    }

                }

            }.margin(5)

        }
    }

    fun getProducerMagicPlantSeedItemStacks(trait: Trait): List<ItemStack> {
        return magicPlantCards
            .filter { trait in it.defaultTraitBits || trait in it.randomTraitChances }
            .map { card -> card.item().createItemStack().also { it.setTraitStacks(TraitStacks.of(TraitStack(trait, 1))) } }
    }
}
