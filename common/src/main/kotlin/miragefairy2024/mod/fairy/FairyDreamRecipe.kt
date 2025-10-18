package miragefairy2024.mod.fairy

import com.mojang.serialization.Codec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.clientProxy
import miragefairy2024.mod.materials.MIRAGE_FLOUR_TAG
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.views.CatalystSlotView
import miragefairy2024.mod.recipeviewer.views.OutputSlotView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.XSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.noBackground
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.EnJa
import miragefairy2024.util.IngredientStack
import miragefairy2024.util.darkRed
import miragefairy2024.util.getOrDefault
import miragefairy2024.util.invoke
import miragefairy2024.util.pairCodecOf
import miragefairy2024.util.plus
import miragefairy2024.util.text
import miragefairy2024.util.toIngredient
import miragefairy2024.util.toIngredientStack
import mirrg.kotlin.helium.or
import mirrg.kotlin.helium.unit
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.core.RegistryAccess
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
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
    BlockFairyDreamRecipeRecipeViewerCategoryCard.init()
    EntityTypeFairyDreamRecipeRecipeViewerCategoryCard.init()
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

abstract class FairyDreamRecipeRecipeViewerCategoryCard<T> : RecipeViewerCategoryCard<Pair<Motif, List<T>>>() {
    abstract fun getTypePath(): String
    override fun getId() = MirageFairy2024.identifier("${getTypePath()}_fairy_dream_recipe")
    abstract fun getTypeName(): EnJa
    override fun getName() = EnJa("Fairy Dream: ${getTypeName().en}", "妖精の夢：${getTypeName().ja}")
    abstract fun getIconMotif(): Motif
    override fun getIcon() = getIconMotif().createFairyItemStack()
    override fun getWorkstations() = MIRAGE_FLOUR_TAG.toIngredient().items.toList()
    abstract fun getTypeCodec(): Codec<T>
    override fun getRecipeCodec(registryAccess: RegistryAccess) = pairCodecOf(motifRegistry.byNameCodec(), getTypeCodec().listOf())
    abstract fun getIngredientStack(keys: List<T>): IngredientStack?
    override fun getInputs(recipeEntry: RecipeEntry<Pair<Motif, List<T>>>) = getIngredientStack(recipeEntry.recipe.second)?.let { listOf(Input(it, true)) } ?: listOf()
    override fun getOutputs(recipeEntry: RecipeEntry<Pair<Motif, List<T>>>) = listOf(recipeEntry.recipe.first.createFairyItemStack())

    abstract fun getFairyDreamTable(): FairyDreamTable<T>

    override fun createRecipeEntries(registryAccess: RegistryAccess): Iterable<RecipeEntry<Pair<Motif, List<T>>>> {
        return getFairyDreamTable().getDisplayMap().map { (motif, keys) ->
            RecipeEntry(registryAccess, motif.getIdentifier()!!, Pair(motif, keys.toList()), true)
        }
    }

    abstract fun getName(key: T): Component

    override fun createView(recipeEntry: RecipeEntry<Pair<Motif, List<T>>>) = View {
        view += XListView().configure {
            view.sizingX = Sizing.FILL
            val gained = clientProxy.or { return@configure }.getClientPlayer().or { return@configure }.fairyDreamContainer.getOrDefault()[recipeEntry.recipe.first]
            val text = text { getName(recipeEntry.recipe.second.first()) }
                .let { if (recipeEntry.recipe.second.size > 1) text { it + "..."() } else it }
                .let { if (!gained) it.darkRed else it }
            val ingredientStack = getIngredientStack(recipeEntry.recipe.second)
            if (ingredientStack != null) {
                view += CatalystSlotView(ingredientStack).noBackground()
                view += XSpaceView(2)
            }
            view += TextView(text).configure {
                position.alignmentY = Alignment.CENTER
                position.weight = 1.0
                view.sizingX = Sizing.FILL
                view.color = ColorPair.DARK_GRAY
                view.shadow = false
                view.scroll = true
                view.tooltip = recipeEntry.recipe.second.map { getName(it) }
            }
            view += XSpaceView(2)
            view += OutputSlotView(recipeEntry.recipe.first.createFairyItemStack())
        }
    }
}

object ItemFairyDreamRecipeRecipeViewerCategoryCard : FairyDreamRecipeRecipeViewerCategoryCard<Item>() {
    override fun getTypePath() = "item"
    override fun getTypeName() = EnJa("Item", "アイテム")
    override fun getIconMotif() = MotifCard.CARROT
    override fun getTypeCodec(): Codec<Item> = BuiltInRegistries.ITEM.byNameCodec()
    override fun getIngredientStack(keys: List<Item>) = keys.toIngredientStack()
    override fun getFairyDreamTable() = FairyDreamRecipes.ITEM
    override fun getName(key: Item): Component = key.description
}

object BlockFairyDreamRecipeRecipeViewerCategoryCard : FairyDreamRecipeRecipeViewerCategoryCard<Block>() {
    override fun getTypePath() = "block"
    override fun getTypeName() = EnJa("Block", "ブロック")
    override fun getIconMotif() = MotifCard.MAGENTA_GLAZED_TERRACOTTA
    override fun getTypeCodec(): Codec<Block> = BuiltInRegistries.BLOCK.byNameCodec()
    override fun getIngredientStack(keys: List<Block>) = keys.map { it.asItem() }.toIngredientStack()
    override fun getFairyDreamTable() = FairyDreamRecipes.BLOCK
    override fun getName(key: Block): Component = key.name
}

object EntityTypeFairyDreamRecipeRecipeViewerCategoryCard : FairyDreamRecipeRecipeViewerCategoryCard<EntityType<*>>() {
    override fun getTypePath() = "entity"
    override fun getTypeName() = EnJa("Entity", "エンティティ")
    override fun getIconMotif() = MotifCard.ENDERMAN
    override fun getTypeCodec(): Codec<EntityType<*>> = BuiltInRegistries.ENTITY_TYPE.byNameCodec()
    override fun getIngredientStack(keys: List<EntityType<*>>) = null
    override fun getFairyDreamTable() = FairyDreamRecipes.ENTITY_TYPE
    override fun getName(key: EntityType<*>): Component = key.description
}
