package miragefairy2024.mod.magicplant

import com.mojang.serialization.Codec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.magicplant.contents.magicplants.MirageFlowerCard
import miragefairy2024.mod.recipeviewer.Alignment
import miragefairy2024.mod.recipeviewer.ImageView
import miragefairy2024.mod.recipeviewer.IntPoint
import miragefairy2024.mod.recipeviewer.IntRectangle
import miragefairy2024.mod.recipeviewer.MarginView
import miragefairy2024.mod.recipeviewer.NinePatchImageView
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.SolidView
import miragefairy2024.mod.recipeviewer.StackView
import miragefairy2024.mod.recipeviewer.TextView
import miragefairy2024.mod.recipeviewer.View
import miragefairy2024.mod.recipeviewer.XListView
import miragefairy2024.mod.recipeviewer.XSpaceView
import miragefairy2024.mod.recipeviewer.YListView
import miragefairy2024.mod.recipeviewer.YSpaceView
import miragefairy2024.mod.recipeviewer.plusAssign
import miragefairy2024.util.EnJa
import miragefairy2024.util.createItemStack
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
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
        this += StackView {

            // 背景
            this += NinePatchImageView(MirageFairy2024.identifier("textures/gui/trait_background.png"), 22, 22, 22, 22, 22, 22)

            this += MarginView(5) {
                this += YListView {

                    // TODO 対応種子

                    // 特性名
                    this += Alignment.CENTER to TextView(recipeEntry.recipe.getName().style(recipeEntry.recipe.style))

                    this += YSpaceView(5)

                    // 特性アイコン行
                    this += XListView {

                        // 条件リスト
                        this += Pair(Alignment.END, 1.0) to XListView {
                            this += YListView {
                                recipeEntry.recipe.conditions.forEach {
                                    this += Alignment.START to TextView(it.emoji).apply {
                                        tooltip = listOf(it.name)
                                    }
                                }
                            }
                            this += 1.0 to XSpaceView(0)
                        }

                        // 特性アイコン
                        this += Alignment.END to ImageView(recipeEntry.recipe.texture, IntRectangle(0, 0, 32, 32), IntPoint(32, 32))

                        // 効果リスト
                        this += Pair(Alignment.END, 1.0) to XListView {
                            this += 1.0 to XSpaceView(0)
                            this += YListView {
                                recipeEntry.recipe.traitEffectKeyEntries.forEach {
                                    this += Alignment.END to TextView(it.traitEffectKey.emoji.style(it.traitEffectKey.style)).apply {
                                        xAlignment = Alignment.END
                                        tooltip = listOf(text { it.traitEffectKey.name + " "() + (it.factor * 100.0 formatAs "%.1f%%")() })
                                    }
                                }
                            }
                        }

                    }

                    this += YSpaceView(5)

                    // 特性ポエム
                    this += 1.0 to TextView(recipeEntry.recipe.poem).apply {
                        // TODO multiline
                    }

                }
            }

        }
        // this += TraitEncyclopediaView(IntPoint(18 * 9 + 5, 140), recipeEntry.recipe)
    }

    fun getProducerMagicPlantSeedItemStacks(trait: Trait): List<ItemStack> {
        return magicPlantCards
            // TODO デフォルト特性
            .filter { trait in it.randomTraitChances }
            .map { card -> card.item().createItemStack().also { it.setTraitStacks(TraitStacks.of(TraitStack(trait, 1))) } }
    }
}

class TraitEncyclopediaView(size: IntPoint, val trait: Trait) : SolidView(size)
