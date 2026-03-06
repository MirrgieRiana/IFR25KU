package miragefairy2024.mod.machine

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import miragefairy2024.util.IngredientStack
import miragefairy2024.util.createItemStack
import miragefairy2024.util.list
import mirrg.kotlin.helium.atMost
import mirrg.kotlin.helium.min
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.util.RandomSource
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeSerializer

data class AthanorInput(val ingredientStack: IngredientStack, val consumptionChance: Double) {
    companion object {
        val CODEC: Codec<AthanorInput> = RecordCodecBuilder.create { instance ->
            instance.group(
                IngredientStack.CODEC.fieldOf("ingredient").forGetter { it.ingredientStack },
                Codec.DOUBLE.optionalFieldOf("consumptionChance", 1.0).forGetter { it.consumptionChance },
            ).apply(instance, ::AthanorInput)
        }
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, AthanorInput> = StreamCodec.composite(
            IngredientStack.STREAM_CODEC,
            { it.ingredientStack },
            ByteBufCodecs.DOUBLE,
            { it.consumptionChance },
            ::AthanorInput,
        )
        val EMPTY = AthanorInput(IngredientStack.EMPTY, 1.0)
    }
}

object AthanorRecipeCard : SimpleMachineRecipeCard<AthanorRecipe>() {
    override val identifier = MirageFairy2024.identifier("athanor")
    override fun getIcon() = AthanorCard.item().createItemStack()
    override val recipeClass = AthanorRecipe::class.java
    override fun createRecipe(group: String, inputs: List<IngredientStack>, outputs: List<ItemStack>, duration: Int): AthanorRecipe {
        return AthanorRecipe(this, group, inputs.map { AthanorInput(it, 1.0) }, outputs, duration)
    }

    fun createAthanorRecipe(group: String, athanorInputs: List<AthanorInput>, outputs: List<ItemStack>, duration: Int): AthanorRecipe {
        return AthanorRecipe(this, group, athanorInputs, outputs, duration)
    }

    @Suppress("LeakingThis")
    override val serializer: RecipeSerializer<AthanorRecipe> = object : RecipeSerializer<AthanorRecipe> {
        override fun codec(): MapCodec<AthanorRecipe> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.STRING.fieldOf("group").forGetter { it.group },
                AthanorInput.CODEC.listOf().fieldOf("inputs").forGetter { it.athanorInputs },
                ItemStack.CODEC.listOf().fieldOf("outputs").forGetter { it.outputs },
                Codec.INT.fieldOf("duration").forGetter { it.duration },
            ).apply(instance, ::createAthanorRecipe)
        }

        override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, AthanorRecipe> = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            { it.group },
            AthanorInput.STREAM_CODEC.list(),
            { it.athanorInputs },
            ItemStack.STREAM_CODEC.list(),
            { it.outputs },
            ByteBufCodecs.VAR_INT,
            { it.duration },
            ::createAthanorRecipe,
        )
    }
}

class AthanorRecipe(
    card: AthanorRecipeCard,
    group: String,
    val athanorInputs: List<AthanorInput>,
    outputs: List<ItemStack>,
    duration: Int,
) : SimpleMachineRecipe(
    card,
    group,
    athanorInputs.map { it.ingredientStack },
    outputs,
    duration,
) {

    private data class AthanorConsumption(val athanorInput: AthanorInput, val slotIndex: Int, val count: Int)

    /** 各入力エントリーに対して、どのスロットから何個消費するかを計算する。入力エントリーごとにグループ化して返す。 */
    private fun matchImplGrouped(inventory: SimpleMachineRecipeInput): List<List<AthanorConsumption>>? {
        val virtualCounts = IntArray(inventory.size()) { inventory.getItem(it).count }
        val result = mutableListOf<List<AthanorConsumption>>()
        athanorInputs.forEach { athanorInput ->
            val consumptions = mutableListOf<AthanorConsumption>()
            run inputEntryCompleted@{
                var neededCount = athanorInput.ingredientStack.count
                (0 until inventory.size()).forEach nextSlot@{ slotIndex ->
                    if (virtualCounts[slotIndex] == 0) return@nextSlot
                    if (!athanorInput.ingredientStack.ingredient.test(inventory.getItem(slotIndex))) return@nextSlot
                    val takeCount = neededCount min virtualCounts[slotIndex]
                    virtualCounts[slotIndex] -= takeCount
                    neededCount -= takeCount
                    consumptions += AthanorConsumption(athanorInput, slotIndex, takeCount)
                    if (neededCount == 0) return@inputEntryCompleted
                }
                return null
            }
            result += consumptions
        }
        return result
    }

    /**
     * 消費確率を考慮したマッチングを行う。
     * randomがnullの場合、消費確率は無視される（全消費扱い）。
     * MatchResultのcraft()とgetRemainingItems()は同じランダム判定結果を共有する。
     */
    fun match(inventory: SimpleMachineRecipeInput, random: RandomSource?): MatchResult? {
        val groupedConsumptions = matchImplGrouped(inventory) ?: return null

        // 各入力エントリーについて消費確率を判定
        val consumedGroups = groupedConsumptions.filter { group ->
            val athanorInput = group.first().athanorInput
            val chance = athanorInput.consumptionChance
            chance >= 1.0 || random == null || random.nextDouble() < chance
        }

        val consumptions = consumedGroups.flatten()
        val recipe = this

        return object : MatchResult {
            override fun craft(): List<ItemStack> {
                val result = mutableListOf<ItemStack>()
                consumptions.forEach {
                    result += inventory.getItem(it.slotIndex).split(it.count)
                }
                return result
            }

            override fun getRemainingItems(): List<ItemStack> {
                val list = mutableListOf<ItemStack>()
                consumptions.forEach {
                    val remainder = recipe.getCustomizedRemainder(inventory.getItem(it.slotIndex))
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
        }
    }

    override fun match(inventory: SimpleMachineRecipeInput): MatchResult? {
        return match(inventory, null)
    }
}
