package miragefairy2024.mod.magicplant

import com.mojang.serialization.Codec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.magicplant.contents.magicplants.MirageFlowerCard
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.views.ImageButtonView
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.MarginView
import miragefairy2024.mod.recipeviewer.views.MultiLineTextViewGenerator
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.PagingView
import miragefairy2024.mod.recipeviewer.views.SolidView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.XSpaceView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.EnJa
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.createItemStack
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

context(ModContext)
fun initTraitEncyclopedia() {
    TraitEncyclopediaRecipeViewerCategoryCard.init()
}

object TraitEncyclopediaRecipeViewerCategoryCard : RecipeViewerCategoryCard<Trait>() {
    override fun getId() = MirageFairy2024.identifier("trait_encyclopedia")
    override fun getName() = EnJa("Trait Encyclopedia", "特性図鑑")

    // TODO IconItem
    override fun getIcon() = MirageFlowerCard.item().createItemStack()
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

            // 背景
            view += NinePatchImageView(MirageFairy2024.identifier("textures/gui/trait_background.png"), 22, 22, 22, 22, 22, 22)

            view += MarginView(5).configure {
                view += YListView().configure {

                    // 特性名
                    view += TextView(recipeEntry.recipe.getName().style(recipeEntry.recipe.style)).configure {
                        position.alignmentX = Alignment.CENTER
                    }

                    view += YSpaceView(5)

                    // 特性アイコン行
                    view += XListView().configure {

                        // TODO フェアリークエストカードにスクロールテキストを
                        // TODO 加工機械で出力スロットが反応しない
                        // 条件リスト
                        view += XListView().configure {
                            position.alignmentY = Alignment.END
                            position.weight = 1.0
                            view += YListView().configure {
                                recipeEntry.recipe.conditions.forEach {
                                    view += TextView(it.emoji).configure {
                                        position.alignmentX = Alignment.START
                                        view.tooltip = listOf(it.name)
                                    }
                                }
                            }
                            view += XSpaceView(0).configure {
                                position.weight = 1.0
                            }
                        }

                        // 特性アイコン
                        val texture = ViewTexture(recipeEntry.recipe.texture, IntPoint(32, 32), IntRectangle(0, 0, 32, 32))
                        view += ImageView(texture).configure {
                            position.alignmentY = Alignment.END
                        }

                        // 効果リスト
                        view += XListView().configure {
                            position.alignmentY = Alignment.END
                            position.weight = 1.0
                            view += XSpaceView(0).configure {
                                position.weight = 1.0
                            }
                            view += YListView().configure {
                                recipeEntry.recipe.traitEffectKeyEntries.forEach {
                                    view += TextView(text { (it.factor * 100.0 formatAs "%.1f%%")() + " "() + it.traitEffectKey.emoji.style(it.traitEffectKey.style) }).configure {
                                        position.alignmentX = Alignment.END
                                        view.alignmentX = Alignment.END
                                        view.tooltip = listOf(it.traitEffectKey.name)
                                    }
                                }
                            }
                        }

                    }

                    view += YSpaceView(5)

                    // TODO クリックしたら表示欄がでかくなってほしい
                    // 特性ポエム
                    view += PagingView().configure {
                        position.weight = 1.0
                        view += MultiLineTextViewGenerator(recipeEntry.recipe.poem)

                        view.pageCount.register { _, it ->
                            pageCount.value = it
                        }
                        view.pageIndex.register { _, it ->
                            pageIndex.value = it
                        }
                        pageIndex.register { _, it ->
                            view.pageIndex.value = it
                        }
                    }

                    view += YSpaceView(5)

                    // TODO 対応種子

                    // ページ操作ボタン
                    view += XListView().configure {
                        view += XListView().configure {
                            position.alignmentY = Alignment.CENTER
                            position.weight = 1.0
                            view += XSpaceView(0).configure {
                                position.weight = 1.0
                            }
                            view += ImageButtonView(IntPoint(12, 12)).configure {
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
                                }
                            }
                        }
                        view += TextView().configure {
                            position.alignmentY = Alignment.CENTER
                            view.minWidth = 32
                            view.alignmentX = Alignment.CENTER

                            fun update() {
                                view.text.value = text { "${pageIndex.value + 1}"() }.visualOrderText
                            }
                            pageIndex.register { _, _ -> update() }
                            update()
                        }
                        view += XListView().configure {
                            position.alignmentY = Alignment.CENTER
                            position.weight = 1.0
                            view += ImageButtonView(IntPoint(12, 12)).configure {
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
                                }
                            }
                            view += XSpaceView(0).configure {
                                position.weight = 1.0
                            }
                        }
                    }

                }
            }

        }
        // TODO remove
        // view += TraitEncyclopediaView(IntPoint(18 * 9 + 5, 140), recipeEntry.recipe)
    }

    fun getProducerMagicPlantSeedItemStacks(trait: Trait): List<ItemStack> {
        return magicPlantCards
            // TODO デフォルト特性
            .filter { trait in it.randomTraitChances }
            .map { card -> card.item().createItemStack().also { it.setTraitStacks(TraitStacks.of(TraitStack(trait, 1))) } }
    }
}

class TraitEncyclopediaView(size: IntPoint, val trait: Trait) : SolidView(size)
