package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.clientProxy
import miragefairy2024.mod.recipeviewer.Alignment
import miragefairy2024.mod.recipeviewer.CatalystSlotView
import miragefairy2024.mod.recipeviewer.ColorPair
import miragefairy2024.mod.recipeviewer.OutputSlotView
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.TextView
import miragefairy2024.mod.recipeviewer.View
import miragefairy2024.mod.recipeviewer.XListView
import miragefairy2024.mod.recipeviewer.XSpaceView
import miragefairy2024.mod.recipeviewer.noBackground
import miragefairy2024.mod.recipeviewer.plusAssign
import miragefairy2024.util.EnJa
import miragefairy2024.util.darkRed
import miragefairy2024.util.getOrDefault
import miragefairy2024.util.invoke
import miragefairy2024.util.pairCodecOf
import miragefairy2024.util.plus
import miragefairy2024.util.text
import miragefairy2024.util.toIngredientStack
import mirrg.kotlin.helium.or
import mirrg.kotlin.helium.unit
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.core.RegistryAccess
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import kotlin.jvm.optionals.getOrElse

interface FairyDreamProviderItem {
    fun getFairyDreamMotifs(itemStack: ItemStack): List<Motif>
}

interface FairyDreamProviderBlock {
    fun getFairyDreamMotifs(world: Level, blockPos: BlockPos): List<Motif>
}

object FairyDreamRecipes {
    val ITEM = FairyDreamTable<Item>(BuiltInRegistries.ITEM)
    val BLOCK = FairyDreamTable<Block>(BuiltInRegistries.BLOCK)
    val ENTITY_TYPE = FairyDreamTable<EntityType<*>>(BuiltInRegistries.ENTITY_TYPE)
}

context(ModContext)
fun initFairyDreamRecipe() {
    ItemFairyDreamRecipeRecipeViewerCategoryCard.init()
}

class FairyDreamTable<T>(val registry: Registry<T>) {
    private val entries = mutableListOf<Pair<T, Motif>>()
    private val tagEntries = mutableListOf<Pair<TagKey<T>, Motif>>()

    private val mapPair: Pair<Map<T, Set<Motif>>, Map<Motif, Set<T>>> by lazy {
        val map = mutableMapOf<T, MutableSet<Motif>>()
        val reverseMap = mutableMapOf<Motif, MutableSet<T>>()
        entries.forEach { (key, motif) ->
            map.getOrPut(key) { mutableSetOf() } += motif
            reverseMap.getOrPut(motif) { mutableSetOf() } += key
        }
        tagEntries.forEach { (tag, motif) ->
            registry.getTag(tag).getOrElse { return@forEach }.map { it.value() }.forEach { key ->
                map.getOrPut(key) { mutableSetOf() } += motif
                reverseMap.getOrPut(motif) { mutableSetOf() } += key
            }
        }
        Pair(map, reverseMap)
    }

    fun register(key: T, motif: Motif) = unit { entries += Pair(key, motif) }
    fun registerFromTag(tag: TagKey<T>, motif: Motif) = unit { tagEntries += Pair(tag, motif) }

    fun test(key: T) = mapPair.first.getOrElse(key) { setOf() }
    fun getDisplayMap() = mapPair.second
}

object ItemFairyDreamRecipeRecipeViewerCategoryCard : RecipeViewerCategoryCard<Pair<Motif, List<Item>>>() {
    override fun getId() = MirageFairy2024.identifier("item_fairy_dream_recipe")
    override fun getName() = EnJa("Fairy Dream: Item", "妖精の夢：アイテム")
    override fun getIcon() = MotifCard.CARROT.createFairyItemStack()
    override fun getWorkstations() = listOf<ItemStack>()
    override fun getRecipeCodec(registryAccess: RegistryAccess) = pairCodecOf(motifRegistry.byNameCodec(), BuiltInRegistries.ITEM.byNameCodec().listOf())
    override fun getInputs(recipeEntry: RecipeEntry<Pair<Motif, List<Item>>>) = listOf(Input(recipeEntry.recipe.second.toIngredientStack(), true))
    override fun getOutputs(recipeEntry: RecipeEntry<Pair<Motif, List<Item>>>) = listOf(recipeEntry.recipe.first.createFairyItemStack())

    override fun createRecipeEntries(): Iterable<RecipeEntry<Pair<Motif, List<Item>>>> {
        return FairyDreamRecipes.ITEM.getDisplayMap().map { (motif, items) ->
            RecipeEntry(motif.getIdentifier()!!, Pair(motif, items.toList()), true)
        }
    }

    override fun createView(recipeEntry: RecipeEntry<Pair<Motif, List<Item>>>) = View {
        this += XListView {
            val gained = clientProxy.or { return@XListView }.getClientPlayer().or { return@XListView }.fairyDreamContainer.getOrDefault()[recipeEntry.recipe.first]
            val text = text { recipeEntry.recipe.second.first().description }
                .let { if (recipeEntry.recipe.second.size > 1) text { it + "..."() } else it }
                .let { if (!gained) it.darkRed else it }
            this += CatalystSlotView(recipeEntry.recipe.second.toIngredientStack()).noBackground()
            this += XSpaceView(2)
            this += Alignment.CENTER to TextView(text).apply {
                minWidth = 112
                color = ColorPair.DARK_GRAY
                shadow = false
                tooltip = recipeEntry.recipe.second.map { it.description }
            }
            this += OutputSlotView(recipeEntry.recipe.first.createFairyItemStack())
        }
    }
}
