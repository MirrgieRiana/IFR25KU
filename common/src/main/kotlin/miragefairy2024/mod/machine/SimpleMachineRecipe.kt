package miragefairy2024.mod.machine

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.DataGenerationEvents
import miragefairy2024.ModContext
import miragefairy2024.util.RecipeGenerationSettings
import miragefairy2024.util.Registration
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.group
import miragefairy2024.util.list
import miragefairy2024.util.register
import miragefairy2024.util.string
import miragefairy2024.util.times
import mirrg.kotlin.helium.atMost
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

    abstract fun createRecipe(group: String, inputs: List<Pair<Ingredient, Int>>, output: ItemStack, duration: Int): R

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
    val inputs: List<Pair<Ingredient, Int>>,
    val output: ItemStack,
    val duration: Int,
) : Recipe<SimpleMachineRecipeInput> {

    override fun getGroup() = group

    // TODO 順不同
    override fun matches(inventory: SimpleMachineRecipeInput, world: Level): Boolean {
        inputs.forEachIndexed { index, input ->
            if (!input.first.test(inventory.getItem(index))) return false
            if (inventory.getItem(index).count < input.second) return false
        }
        return true
    }

    open fun getCustomizedRemainder(itemStack: ItemStack): ItemStack = itemStack.item.getRecipeRemainder(itemStack)

    override fun getRemainingItems(inventory: SimpleMachineRecipeInput): NonNullList<ItemStack> {
        val list = NonNullList.create<ItemStack>()
        inputs.forEachIndexed { index, input ->
            val remainder = getCustomizedRemainder(inventory.getItem(index))
            if (remainder.isEmpty) return@forEachIndexed

            var totalRemainderCount = remainder.count * input.second
            while (totalRemainderCount > 0) {
                val count = totalRemainderCount atMost remainder.maxStackSize
                list += remainder.copyWithCount(count)
                totalRemainderCount -= count
            }
        }
        return list
    }

    override fun assemble(inventory: SimpleMachineRecipeInput, registries: HolderLookup.Provider): ItemStack = output.copy()
    override fun canCraftInDimensions(width: Int, height: Int) = width * height >= inputs.size
    override fun getResultItem(registries: HolderLookup.Provider) = output
    override fun getToastSymbol() = card.getIcon()
    override fun getSerializer() = card.serializer
    override fun getType() = card.type

    class Serializer<R : SimpleMachineRecipe>(private val card: SimpleMachineRecipeCard<R>) : RecipeSerializer<R> {
        companion object {
            private val INPUT_CODEC: MapCodec<Pair<Ingredient, Int>> = RecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter { it.first },
                    Codec.INT.fieldOf("count").forGetter { it.second },
                ).apply(instance, ::Pair)
            }
            private val INPUT_STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, Pair<Ingredient, Int>> = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC,
                { it.first },
                ByteBufCodecs.VAR_INT,
                { it.second },
                ::Pair,
            )
        }

        override fun codec(): MapCodec<R> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.STRING.fieldOf("group").forGetter { it.group },
                INPUT_CODEC.codec().listOf().fieldOf("inputs").forGetter { it.inputs },
                ItemStack.CODEC.fieldOf("output").forGetter { it.output },
                Codec.INT.fieldOf("duration").forGetter { it.duration },
            ).apply(instance, card::createRecipe)
        }

        override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, R> = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            { it.group },
            INPUT_STREAM_CODEC.list(),
            { it.inputs },
            ItemStack.STREAM_CODEC,
            { it.output },
            ByteBufCodecs.VAR_INT,
            { it.duration },
            card::createRecipe,
        )
    }

}

context(ModContext)
fun <R : SimpleMachineRecipe> registerSimpleMachineRecipeGeneration(
    card: SimpleMachineRecipeCard<R>,
    inputs: List<Pair<() -> Ingredient, Int>>,
    output: () -> ItemStack,
    duration: Int,
    block: SimpleMachineRecipeJsonBuilder<R>.() -> Unit = {},
): RecipeGenerationSettings<SimpleMachineRecipeJsonBuilder<R>> {
    val settings = RecipeGenerationSettings<SimpleMachineRecipeJsonBuilder<R>>()
    DataGenerationEvents.onGenerateRecipe {
        val builder = SimpleMachineRecipeJsonBuilder(card, RecipeCategory.MISC, inputs.map { p -> Pair(p.first(), p.second) }, output(), duration)
        builder.group(output().item)
        settings.listeners.forEach { listener ->
            listener(builder)
        }
        block(builder)
        val identifier = settings.idModifiers.fold(output().item.getIdentifier()) { id, idModifier -> idModifier(id) }
        builder.save(it, identifier)
    }
    return settings
}

class SimpleMachineRecipeJsonBuilder<R : SimpleMachineRecipe>(
    private val card: SimpleMachineRecipeCard<R>,
    private val category: RecipeCategory,
    private val inputs: List<Pair<Ingredient, Int>>,
    private val output: ItemStack,
    private val duration: Int,
) : RecipeBuilder {
    private val criteria = mutableMapOf<String, Criterion<*>>()
    private var group = ""

    override fun unlockedBy(name: String, condition: Criterion<*>) = this.also { criteria[name] = condition }
    override fun group(string: String?) = this.also { this.group = string ?: "" }
    override fun getResult(): Item = output.item

    override fun save(recipeOutput: RecipeOutput, recipeId: ResourceLocation) {
        check(criteria.isNotEmpty()) { "No way of obtaining recipe $recipeId" }
        val advancementBuilder = recipeOutput.advancement()
            .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeId))
            .rewards(AdvancementRewards.Builder.recipe(recipeId))
            .requirements(AdvancementRequirements.Strategy.OR)
        criteria.forEach {
            advancementBuilder.addCriterion(it.key, it.value)
        }
        recipeOutput.accept(recipeId, card.createRecipe(group, inputs, output, duration), advancementBuilder.build("recipes/${category.folderName}/" * recipeId))
    }
}
