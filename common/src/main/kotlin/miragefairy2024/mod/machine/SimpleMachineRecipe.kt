package miragefairy2024.mod.machine

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.DataGenerationEvents
import miragefairy2024.ModContext
import miragefairy2024.util.IngredientStack
import miragefairy2024.util.RecipeGenerationSettings
import miragefairy2024.util.Registration
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.group
import miragefairy2024.util.list
import miragefairy2024.util.register
import miragefairy2024.util.string
import miragefairy2024.util.times
import mirrg.kotlin.helium.atMost
import mirrg.kotlin.helium.min
import net.minecraft.advancements.AdvancementRequirements
import net.minecraft.advancements.AdvancementRewards
import net.minecraft.advancements.Criterion
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger
import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.recipes.RecipeBuilder
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeInput
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level

abstract class SimpleMachineRecipeCard<R : SimpleMachineRecipe> {

    abstract val identifier: ResourceLocation

    abstract fun getIcon(): ItemStack

    val type = object : RecipeType<R> {
        override fun toString() = identifier.string
    }

    @Suppress("LeakingThis")
    val serializer = SimpleMachineRecipe.Serializer(this)

    abstract val recipeClass: Class<R>

    abstract fun createRecipe(group: String, inputs: List<SimpleMachineRecipe.Input>, outputs: List<ItemStack>, duration: Int): R

    context(ModContext)
    fun init() {
        Registration(BuiltInRegistries.RECIPE_TYPE, identifier) { type }.register()
        Registration(BuiltInRegistries.RECIPE_SERIALIZER, identifier) { serializer }.register()
    }

}

class SimpleMachineRecipeInput(private val itemStacks: List<ItemStack>) : RecipeInput {
    override fun getItem(index: Int) = itemStacks[index]
    override fun size() = itemStacks.size
}

open class SimpleMachineRecipe(
    private val card: SimpleMachineRecipeCard<*>,
    private val group: String,
    val inputs: List<Input>,
    val outputs: List<ItemStack>,
    val duration: Int,
) : Recipe<SimpleMachineRecipeInput> {
    init {
        require(outputs.isNotEmpty())
    }

    data class Input(val ingredient: Ingredient, val count: Int) {
        val ingredientStack by lazy { IngredientStack(ingredient, count) }
        companion object {
            val CODEC: Codec<Input> = RecordCodecBuilder.create { instance ->
                instance.group(
                    Ingredient.CODEC.fieldOf("Ingredient").forGetter { it.ingredient },
                    Codec.INT.fieldOf("Amount").forGetter { it.count },
                ).apply(instance, ::Input)
            }
            val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, Input> = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC,
                { it.ingredient },
                ByteBufCodecs.VAR_INT,
                { it.count },
                ::Input,
            )
            val EMPTY = Input(Ingredient.EMPTY, 0)
        }
    }

    override fun getGroup() = group

    interface MatchResult {
        fun craft(): List<ItemStack>
    }

    private data class Consumption(val slotIndex: Int, val count: Int)

    private fun matchImpl(inventory: SimpleMachineRecipeInput): List<Consumption>? {
        val virtualCounts = IntArray(inventory.size()) { inventory.getItem(it).count }
        val result = mutableListOf<List<Consumption>>()
        inputs.forEach { input ->
            val consumptions = mutableListOf<Consumption>()
            run inputEntryCompleted@{
                var neededCount = input.count
                (0 until inventory.size()).forEach nextSlot@{ slotIndex ->
                    if (virtualCounts[slotIndex] == 0) return@nextSlot
                    if (!input.ingredient.test(inventory.getItem(slotIndex))) return@nextSlot
                    val takeCount = neededCount min virtualCounts[slotIndex]
                    virtualCounts[slotIndex] -= takeCount
                    neededCount -= takeCount
                    consumptions += Consumption(slotIndex, takeCount)
                    if (neededCount == 0) return@inputEntryCompleted
                }
                return null
            }
            result += consumptions
        }
        return result.flatten()
    }

    fun match(inventory: SimpleMachineRecipeInput): MatchResult? {
        val consumptions = matchImpl(inventory) ?: return null
        return object : MatchResult {
            override fun craft(): List<ItemStack> {
                val result = mutableListOf<ItemStack>()
                consumptions.forEach {
                    result += inventory.getItem(it.slotIndex).split(it.count)
                }
                return result
            }
        }
    }

    override fun matches(inventory: SimpleMachineRecipeInput, world: Level): Boolean {
        return match(inventory) != null
    }

    open fun getCustomizedRemainder(itemStack: ItemStack): ItemStack = itemStack.item.getRecipeRemainder(itemStack)

    override fun getRemainingItems(inventory: SimpleMachineRecipeInput): NonNullList<ItemStack> {
        val list = NonNullList.create<ItemStack>()
        val consumptions = matchImpl(inventory) ?: return list
        consumptions.forEach {
            val remainder = getCustomizedRemainder(inventory.getItem(it.slotIndex))
            if (remainder.isEmpty) return@forEach

            var totalRemainderCount = remainder.count * it.count
            while (totalRemainderCount > 0) {
                val count = totalRemainderCount atMost remainder.maxStackSize
                list += remainder.copyWithCount(count)
                totalRemainderCount -= count
            }
        }
        return list
    }

    override fun assemble(inventory: SimpleMachineRecipeInput, registries: HolderLookup.Provider): ItemStack = outputs.first().copy()
    override fun canCraftInDimensions(width: Int, height: Int) = width * height >= inputs.size
    override fun getResultItem(registries: HolderLookup.Provider) = outputs.first()
    override fun getToastSymbol() = card.getIcon()
    override fun getSerializer() = card.serializer
    override fun getType() = card.type

    class Serializer<R : SimpleMachineRecipe>(private val card: SimpleMachineRecipeCard<R>) : RecipeSerializer<R> {
        override fun codec(): MapCodec<R> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.STRING.fieldOf("group").forGetter { it.group },
                Input.CODEC.listOf().fieldOf("inputs").forGetter { it.inputs },
                ItemStack.CODEC.listOf().fieldOf("outputs").forGetter { it.outputs },
                Codec.INT.fieldOf("duration").forGetter { it.duration },
            ).apply(instance, card::createRecipe)
        }

        override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, R> = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            { it.group },
            Input.STREAM_CODEC.list(),
            { it.inputs },
            ItemStack.STREAM_CODEC.list(),
            { it.outputs },
            ByteBufCodecs.VAR_INT,
            { it.duration },
            card::createRecipe,
        )
    }

}

context(ModContext)
fun <R : SimpleMachineRecipe> registerSimpleMachineRecipeGeneration(
    card: SimpleMachineRecipeCard<R>,
    inputs: List<() -> IngredientStack>,
    outputs: List<() -> ItemStack>,
    duration: Int,
    block: SimpleMachineRecipeJsonBuilder<R>.() -> Unit = {},
): RecipeGenerationSettings<SimpleMachineRecipeJsonBuilder<R>> {
    require(outputs.isNotEmpty())
    val settings = RecipeGenerationSettings<SimpleMachineRecipeJsonBuilder<R>>()
    DataGenerationEvents.onGenerateRecipe {
        val builder = SimpleMachineRecipeJsonBuilder(card, RecipeCategory.MISC, inputs.map { p -> p().let { SimpleMachineRecipe.Input(it.ingredient, it.count) } }, outputs.map { p -> p() }, duration)
        builder.group(outputs.first()().item)
        settings.listeners.forEach { listener ->
            listener(builder)
        }
        block(builder)
        val identifier = settings.idModifiers.fold(outputs.first()().item.getIdentifier()) { id, idModifier -> idModifier(id) }
        builder.save(it, identifier)
    }
    return settings
}

class SimpleMachineRecipeJsonBuilder<R : SimpleMachineRecipe>(
    private val card: SimpleMachineRecipeCard<R>,
    private val category: RecipeCategory,
    private val inputs: List<SimpleMachineRecipe.Input>,
    private val outputs: List<ItemStack>,
    private val duration: Int,
) : RecipeBuilder {
    init {
        require(outputs.isNotEmpty())
    }

    private val criteria = mutableMapOf<String, Criterion<*>>()
    private var group = ""

    override fun unlockedBy(name: String, condition: Criterion<*>) = this.also { criteria[name] = condition }
    override fun group(string: String?) = this.also { this.group = string ?: "" }
    override fun getResult(): Item = outputs.first().item

    override fun save(recipeOutput: RecipeOutput, recipeId: ResourceLocation) {
        check(criteria.isNotEmpty()) { "No way of obtaining recipe $recipeId" }
        val advancementBuilder = recipeOutput.advancement()
            .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeId))
            .rewards(AdvancementRewards.Builder.recipe(recipeId))
            .requirements(AdvancementRequirements.Strategy.OR)
        criteria.forEach {
            advancementBuilder.addCriterion(it.key, it.value)
        }
        recipeOutput.accept(recipeId, card.createRecipe(group, inputs, outputs, duration), advancementBuilder.build("recipes/${category.folderName}/" * recipeId))
    }
}
