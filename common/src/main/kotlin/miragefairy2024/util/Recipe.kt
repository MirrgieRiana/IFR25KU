package miragefairy2024.util

import miragefairy2024.DataGenerationEvents
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.recipeGroupRegistry
import miragefairy2024.platformProxy
import mirrg.kotlin.gson.hydrogen.jsonElement
import mirrg.kotlin.gson.hydrogen.jsonObject
import net.fabricmc.fabric.api.loot.v3.LootTableEvents
import net.fabricmc.fabric.api.registry.FuelRegistry
import net.minecraft.advancements.critereon.EnchantmentPredicate
import net.minecraft.advancements.critereon.ItemEnchantmentsPredicate
import net.minecraft.advancements.critereon.ItemPredicate
import net.minecraft.advancements.critereon.ItemSubPredicates
import net.minecraft.advancements.critereon.LocationPredicate
import net.minecraft.advancements.critereon.MinMaxBounds
import net.minecraft.core.HolderLookup
import net.minecraft.core.HolderSet
import net.minecraft.core.NonNullList
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.data.recipes.RecipeBuilder
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.data.recipes.ShapedRecipeBuilder
import net.minecraft.data.recipes.ShapelessRecipeBuilder
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder
import net.minecraft.data.recipes.SpecialRecipeBuilder
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.CraftingBookCategory
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.CustomRecipe
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeInput
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.Level
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction
import net.minecraft.world.level.storage.loot.predicates.LocationCheck
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition
import net.minecraft.world.level.storage.loot.predicates.MatchTool
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator

// Crafting

fun <T : RecipeBuilder> T.criterion(item: Item) = this.also { it.unlockedBy("has_${item.getIdentifier().path}", RecipeProvider.has(item)) }
fun <T : RecipeBuilder> T.criterion(tagKey: TagKey<Item>) = this.also { it.unlockedBy("has_${tagKey.location().path}", RecipeProvider.has(tagKey)) }
fun <T : RecipeBuilder> T.group(item: Item) = this.also { it.group(recipeGroupRegistry[item] ?: "${item.getIdentifier()}") }

class RecipeGenerationSettings<T> {
    val listeners = mutableListOf<(T) -> Unit>()
    val idModifiers = mutableListOf<(ResourceLocation) -> ResourceLocation>()
    var recipeCategory = RecipeCategory.MISC
    var noGroup = false
}

@JvmName("onItem")
infix fun <T : RecipeBuilder> RecipeGenerationSettings<T>.on(item: () -> Item) = this.apply {
    this.listeners += { it.criterion(item()) }
}

@JvmName("onTag")
infix fun <T : RecipeBuilder> RecipeGenerationSettings<T>.on(tag: TagKey<Item>) = this.apply {
    this.listeners += { it.criterion(tag) }
}

infix fun <T> RecipeGenerationSettings<T>.modId(modId: String) = this.apply {
    this.idModifiers += { ResourceLocation(modId, it.path) }
}

infix fun <T> RecipeGenerationSettings<T>.from(item: () -> Item) = this.apply {
    this.idModifiers += { it * "_from_" * item().getIdentifier().path }
}

fun <T> RecipeGenerationSettings<T>.noGroup(noGroup: Boolean = true) = this.apply {
    this.noGroup = noGroup
}

context(ModContext)
fun <T : RecipeBuilder> registerRecipeGeneration(
    creator: (RecipeCategory, Item, Int) -> T,
    item: () -> Item,
    count: Int = 1,
    block: T.() -> Unit = {},
): RecipeGenerationSettings<T> {
    val settings = RecipeGenerationSettings<T>()
    DataGenerationEvents.onGenerateRecipe {
        val builder = creator(settings.recipeCategory, item(), count)
        settings.listeners.forEach { listener ->
            listener(builder)
        }
        if (!settings.noGroup) builder.group(item())
        block(builder)
        val identifier = settings.idModifiers.fold(item().getIdentifier()) { id, idModifier -> idModifier(id) }
        builder.save(it, identifier)
    }
    return settings
}

context(ModContext)
fun registerShapedRecipeGeneration(
    item: () -> Item,
    count: Int = 1,
    block: ShapedRecipeBuilder.() -> Unit = {},
): RecipeGenerationSettings<ShapedRecipeBuilder> = registerRecipeGeneration(ShapedRecipeBuilder::shaped, item, count, block)

context(ModContext)
fun registerShapelessRecipeGeneration(
    item: () -> Item,
    count: Int = 1,
    block: ShapelessRecipeBuilder.() -> Unit = {},
): RecipeGenerationSettings<ShapelessRecipeBuilder> = registerRecipeGeneration(ShapelessRecipeBuilder::shapeless, item, count, block)

context(ModContext)
fun registerSmeltingRecipeGeneration(
    input: () -> Item,
    output: () -> Item,
    experience: Double = 0.0,
    cookingTime: Int = 200,
    count: Int = 1,
    block: SimpleCookingRecipeBuilder.() -> Unit = {},
): RecipeGenerationSettings<SimpleCookingRecipeBuilder> {
    val settings = RecipeGenerationSettings<SimpleCookingRecipeBuilder>()
    DataGenerationEvents.onGenerateRecipe {
        val builder = SimpleCookingRecipeBuilder.smelting(input().toIngredient(), RecipeCategory.MISC, ItemStack(output(), count), experience.toFloat(), cookingTime)
        builder.group(output())
        settings.listeners.forEach { listener ->
            listener(builder)
        }
        block(builder)
        val identifier = settings.idModifiers.fold(output().getIdentifier()) { id, idModifier -> idModifier(id) }
        builder.save(it, identifier)
    }
    return settings
}

context(ModContext)
fun registerBlastingRecipeGeneration(
    input: () -> Item,
    output: () -> Item,
    experience: Double = 0.0,
    cookingTime: Int = 100,
    block: SimpleCookingRecipeBuilder.() -> Unit = {},
): RecipeGenerationSettings<SimpleCookingRecipeBuilder> {
    val settings = RecipeGenerationSettings<SimpleCookingRecipeBuilder>()
    DataGenerationEvents.onGenerateRecipe {
        val builder = SimpleCookingRecipeBuilder.blasting(input().toIngredient(), RecipeCategory.MISC, output(), experience.toFloat(), cookingTime)
        builder.group(output())
        settings.listeners.forEach { listener ->
            listener(builder)
        }
        block(builder)
        val identifier = settings.idModifiers.fold(output().getIdentifier() * "_from_blasting") { id, idModifier -> idModifier(id) }
        builder.save(it, identifier)
    }
    return settings
}


// Special Recipe

context(ModContext)
fun registerSpecialRecipe(
    path: String,
    @Suppress("unused") vararg dummy: Void,
    minSlots: Int? = null,
    minHeight: Int? = null,
    minWidth: Int? = null,
    matcher: (CraftingInput) -> SpecialRecipeResult?,
) {
    val identifier = MirageFairy2024.identifier(path)
    lateinit var serializer: SimpleCraftingRecipeSerializer<*>

    class SpecialCraftingRecipeImpl(category: CraftingBookCategory) : CustomRecipe(category) {
        override fun matches(input: CraftingInput, world: Level) = matcher(input) != null
        override fun assemble(input: CraftingInput, registries: HolderLookup.Provider) = matcher(input)?.craft() ?: EMPTY_ITEM_STACK
        override fun getRemainingItems(input: CraftingInput) = matcher(input)?.getRemainder() ?: getDefaultRemainingItems(input) // interfaceのsuperを呼び出そうとするとNoSuchMethodErrorになる

        override fun canCraftInDimensions(width: Int, height: Int): Boolean {
            if (minSlots != null && width * height < minSlots) return false
            if (minWidth != null && width < minWidth) return false
            if (minHeight != null && height < minHeight) return false
            return true
        }

        override fun getSerializer() = serializer
    }

    serializer = SimpleCraftingRecipeSerializer(::SpecialCraftingRecipeImpl)
    Registration(BuiltInRegistries.RECIPE_SERIALIZER, identifier) { serializer }.register()
    DataGenerationEvents.onGenerateRecipe {
        SpecialRecipeBuilder.special(::SpecialCraftingRecipeImpl).save(it, identifier.string)
    }
}

private fun <T : RecipeInput> getDefaultRemainingItems(input: T): NonNullList<ItemStack> {
    val recipe = object : Recipe<T> {
        override fun matches(input: T, level: Level): Boolean = throw AssertionError()
        override fun assemble(input: T, registries: HolderLookup.Provider): ItemStack? = throw AssertionError()
        override fun canCraftInDimensions(width: Int, height: Int): Boolean = throw AssertionError()
        override fun getResultItem(registries: HolderLookup.Provider): ItemStack? = throw AssertionError()
        override fun getSerializer(): RecipeSerializer<*> = throw AssertionError()
        override fun getType(): RecipeType<*> = throw AssertionError()
    }
    return recipe.getRemainingItems(input)
}

interface SpecialRecipeResult {
    fun craft(): ItemStack
    fun getRemainder(): NonNullList<ItemStack>? = null
}

fun MutableList<ItemStack>.pull(predicate: (ItemStack) -> Boolean): ItemStack? {
    val index = this.indexOfFirst { predicate(it) }
    if (index == -1) return null
    return this.removeAt(index)
}


// Others

context(ModContext)
fun registerCompressionRecipeGeneration(
    lowerItem: () -> Item,
    lowerIngredient: () -> Ingredient,
    higherItem: () -> Item,
    higherIngredient: () -> Ingredient,
    count: Int = 9,
    noGroup: Boolean = false,
) {
    registerShapelessRecipeGeneration(higherItem, count = 1) {
        repeat(count) {
            requires(lowerIngredient())
        }
    }.noGroup(noGroup) on lowerItem from lowerItem
    registerShapelessRecipeGeneration(lowerItem, count = count) {
        requires(higherIngredient())
    }.noGroup(noGroup) on higherItem from higherItem
}

context(ModContext)
fun (() -> Item).registerLootTableModification(lootTableIdGetter: () -> ResourceKey<LootTable>, block: (LootTable.Builder, HolderLookup.Provider) -> Unit) = ModEvents.onInitialize {
    val lootTableId = lootTableIdGetter()
    LootTableEvents.MODIFY.register { id, tableBuilder, source, registries ->
        if (source.isBuiltin) {
            if (id == lootTableId) {
                block(tableBuilder, registries)
            }
        }
    }
}

context(ModContext)
fun (() -> Item).registerGrassDrop(
    amount: Float = 1.0F,
    fortuneMultiplier: Int = 2,
    biome: (() -> ResourceKey<Biome>)? = null,
) = this.registerLootTableModification({ Blocks.SHORT_GRASS.lootTable }) { tableBuilder, registries ->
    tableBuilder.configure {
        withPool(LootPool(AlternativeLootPoolEntry {
            otherwise(EmptyLootPoolEntry {
                `when`(MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.SHEARS)))
            })
            otherwise(ItemLootPoolEntry(this@registerGrassDrop()) {
                `when`(LootItemRandomChanceCondition.randomChance(0.125F * amount))
                if (biome != null) `when`(LocationCheck.checkLocation(LocationPredicate.Builder.location().setBiomes(HolderSet.direct(registries[Registries.BIOME, biome()]))))
                apply(ApplyBonusCount.addUniformBonusCount(registries[Registries.ENCHANTMENT, Enchantments.FORTUNE], fortuneMultiplier))
                apply(ApplyExplosionDecay.explosionDecay())
            })
        }))
    }
}

context(ModContext)
fun (() -> Item).registerExtraOreDrop(
    oreBlock: Block,
    chance: Float = 1.0F,
    fortuneMultiplier: Int = 0,
) = this.registerLootTableModification({ oreBlock.lootTable }) { tableBuilder, registries ->
    tableBuilder.configure {
        withPool(LootPool(AlternativeLootPoolEntry {
            otherwise(EmptyLootPoolEntry {
                `when`(MatchTool.toolMatches(ItemPredicate.Builder.item().withSubPredicate(ItemSubPredicates.ENCHANTMENTS, ItemEnchantmentsPredicate.enchantments(listOf(EnchantmentPredicate(registries[Registries.ENCHANTMENT, Enchantments.SILK_TOUCH], MinMaxBounds.Ints.atLeast(1)))))))
            })
            otherwise(ItemLootPoolEntry(this@registerExtraOreDrop()) {
                if (chance < 1.0F) `when`(LootItemRandomChanceCondition.randomChance(chance))
                if (fortuneMultiplier > 0) apply(ApplyBonusCount.addUniformBonusCount(registries[Registries.ENCHANTMENT, Enchantments.FORTUNE], fortuneMultiplier))
                apply(ApplyExplosionDecay.explosionDecay())
            })
        }))
    }
}

context(ModContext)
fun (() -> Item).registerMobDrop(
    entityType: EntityType<*>,
    onlyKilledByPlayer: Boolean = false,
    dropRate: Pair<Float, Float>? = null,
    amount: NumberProvider? = null,
    fortuneFactor: NumberProvider? = null,
) = this.registerLootTableModification({ entityType.defaultLootTable }) { tableBuilder, registries ->
    tableBuilder.configure {
        withPool(LootPool(ItemLootPoolEntry(this@registerMobDrop()) {
            if (amount != null) apply(SetItemCountFunction.setCount(amount, false))
            if (fortuneFactor != null) apply(EnchantedCountIncreaseFunction.lootingMultiplier(registries, fortuneFactor))
        }) {
            if (onlyKilledByPlayer) `when`(LootItemKilledByPlayerCondition.killedByPlayer())
            if (dropRate != null) `when`(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(registries, dropRate.first, dropRate.second))
        })
    }
}

context(ModContext)
fun (() -> Item).registerSinglePoolChestLoot(
    lootTableIdGetter: () -> ResourceKey<LootTable>,
    weight: Int,
    count: IntRange? = null,
    block: LootPoolSingletonContainer.Builder<*>.() -> Unit = {},
) = this.registerLootTableModification(lootTableIdGetter) { tableBuilder, registries ->
    tableBuilder.modifyPools { lootPool ->
        lootPool.configure {
            add(ItemLootPoolEntry(this@registerSinglePoolChestLoot()) {
                setWeight(weight)
                if (count != null) apply(SetItemCountFunction.setCount(UniformGenerator.between(count.first.toFloat(), count.last.toFloat())))
                block(this)
            })
        }
    }
}

context(ModContext)
fun (() -> Item).registerChestLoot(
    lootTableIdGetter: () -> ResourceKey<LootTable>,
    chance: Float,
    count: IntRange? = null,
    block: LootPoolSingletonContainer.Builder<*>.() -> Unit = {},
) = this.registerLootTableModification(lootTableIdGetter) { tableBuilder, registries ->
    tableBuilder.configure {
        withPool(LootPool {
            add(ItemLootPoolEntry(this@registerChestLoot()) {
                `when`(LootItemRandomChanceCondition.randomChance(chance))
                if (count != null) apply(SetItemCountFunction.setCount(UniformGenerator.between(count.first.toFloat(), count.last.toFloat())))
                block(this)
            })
        })
    }
}

context(ModContext)
fun (() -> Item).registerComposterInput(chance: Float) {
    ModEvents.onInitialize {
        platformProxy!!.registerComposterInput(this(), chance)
    }
    DataGenerationEvents.onGenerateDataMap { it, _ ->
        it.accept(
            Registries.ITEM,
            ResourceLocation("neoforge", "compostables"),
            BuiltInRegistries.ITEM.getResourceKey(this()).get(),
            jsonObject(
                "chance" to chance.jsonElement,
            ),
        )
    }
}

/** @param ticks coal is `200 * 8 = 1600` */
context(ModContext)
fun (() -> Item).registerFuel(ticks: Int) = ModEvents.onInitialize {
    FuelRegistry.INSTANCE.add(this(), ticks)
}

context(ModContext)
fun registerStonecutterRecipeGeneration(input: () -> Item, output: () -> Item, count: Int = 1, category: RecipeCategory = RecipeCategory.BUILDING_BLOCKS) = DataGenerationEvents.onGenerateRecipe {
    RecipeProvider.stonecutterResultFromBase(it, category, output(), input(), count)
}
