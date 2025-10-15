package miragefairy2024.mod.magicplant

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.magicplant.contents.TraitConditionCard
import miragefairy2024.mod.magicplant.contents.TraitEffectKeyCard
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.recipeviewer.registerIdentificationDataComponentTypes
import miragefairy2024.mod.registerHarvestNotation
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.BlockEntityType
import miragefairy2024.util.EnJa
import miragefairy2024.util.HumidityCategory
import miragefairy2024.util.Registration
import miragefairy2024.util.TemperatureCategory
import miragefairy2024.util.enJa
import miragefairy2024.util.generator
import miragefairy2024.util.register
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerComposterInput
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerGeneratedModelGeneration
import miragefairy2024.util.registerItemGroup
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.material.PushReaction

@Suppress("LeakingThis") // ブートストラップ問題のため解決不可能なので妥協する
abstract class MagicPlantCard<B : MagicPlantBlock> {
    companion object {
        fun createCommonSettings(): BlockBehaviour.Properties = BlockBehaviour.Properties.of().noCollission().randomTicks().pushReaction(PushReaction.DESTROY)
    }

    abstract fun getBlockPath(): String
    val blockIdentifier = MirageFairy2024.identifier(getBlockPath())
    abstract val blockName: EnJa
    abstract val ageProperty: IntegerProperty
    abstract val blockCodec: MapCodec<B>
    abstract fun createBlock(): B
    val blockType = Registration(BuiltInRegistries.BLOCK_TYPE, blockIdentifier) { blockCodec }
    val block = Registration(BuiltInRegistries.BLOCK, blockIdentifier) { createBlock() }

    abstract fun getItemPath(): String
    val itemIdentifier = MirageFairy2024.identifier(getItemPath())
    abstract val itemName: EnJa
    val item = Registration(BuiltInRegistries.ITEM, itemIdentifier) { MagicPlantSeedItem(block.await(), Item.Properties()) }

    private fun createBlockEntity(blockPos: BlockPos, blockState: BlockState) = MagicPlantBlockEntity(this, blockPos, blockState)
    val blockEntityType = Registration(BuiltInRegistries.BLOCK_ENTITY_TYPE, blockIdentifier) { BlockEntityType(::createBlockEntity, setOf(block.await())) }

    abstract val tier: Int
    abstract val poem: EnJa
    abstract val classification: EnJa

    abstract val family: ResourceLocation
    open val defaultTraitBits: Map<Trait, Int> = mapOf()
    open val randomTraitChances: Map<Trait, Double> = mapOf()

    open val baseGrowth = 0.03
    abstract val drops: List<() -> Item>

    open fun createAdvancement(identifier: ResourceLocation): AdvancementCard? = null
    val advancement = createAdvancement(blockIdentifier)

    context(ModContext)
    open fun init() {

        // 登録
        blockType.register()
        block.register()
        blockEntityType.register()
        item.register()

        // 分類
        item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        // 見た目
        block.registerCutoutRenderLayer()
        item.registerGeneratedModelGeneration()

        // 翻訳
        block.enJa(blockName)
        item.enJa(itemName)
        val seedPoemList = PoemList(tier)
            .poem(poem)
            .poem("classification", classification)
        item.registerPoem(seedPoemList)
        item.registerPoemGeneration(seedPoemList)

        // 性質
        item.registerIdentificationDataComponentTypes { listOf() }
        //block.registerTagGenerate(BlockTags.SMALL_FLOWERS) // これをやるとエンダーマンが勝手に引っこ抜いていく
        BlockTags.MAINTAINS_FARMLAND.generator.registerChild(block)
        BlockTags.CROPS.generator.registerChild(block)

        // レシピ
        item.registerComposterInput(0.3F) // 種はコンポスターに投入可能
        item.registerHarvestNotation(drops)

        // 進捗
        advancement?.init()

    }
}

fun MagicPlantCard<*>.hasEnvironmentAdaptation(includeRandomTraits: Boolean, temperature: TemperatureCategory, humidity: HumidityCategory): Boolean {
    fun isAvailableIn(conditions: Set<TraitCondition>, environment: Set<TraitCondition>): Boolean {
        val remainingConditions = conditions - environment
        if (TraitConditionCard.LOW_HUMIDITY.traitCondition in remainingConditions) return false
        if (TraitConditionCard.MEDIUM_HUMIDITY.traitCondition in remainingConditions) return false
        if (TraitConditionCard.HIGH_HUMIDITY.traitCondition in remainingConditions) return false
        if (TraitConditionCard.LOW_TEMPERATURE.traitCondition in remainingConditions) return false
        if (TraitConditionCard.MEDIUM_TEMPERATURE.traitCondition in remainingConditions) return false
        if (TraitConditionCard.HIGH_TEMPERATURE.traitCondition in remainingConditions) return false
        return true
    }

    fun getEffects(traits: Set<Trait>, environment: Set<TraitCondition>): Set<TraitEffectKey<*>> {
        return traits.toList()
            .filter { isAvailableIn(it.conditions.toSet(), environment) }
            .flatMap { it.traitEffectKeyEntries.map { effectStack -> effectStack.traitEffectKey } }
            .toSet()
    }

    val traits = if (includeRandomTraits) {
        defaultTraitBits.map { it.key }.toSet() + randomTraitChances.map { it.key }.toSet()
    } else {
        defaultTraitBits.map { it.key }.toSet()
    }

    val temperatureTraitCondition = when (temperature) {
        TemperatureCategory.LOW -> TraitConditionCard.LOW_TEMPERATURE.traitCondition
        TemperatureCategory.MEDIUM -> TraitConditionCard.MEDIUM_TEMPERATURE.traitCondition
        TemperatureCategory.HIGH -> TraitConditionCard.HIGH_TEMPERATURE.traitCondition
    }
    val humidityTraitCondition = when (humidity) {
        HumidityCategory.LOW -> TraitConditionCard.LOW_HUMIDITY.traitCondition
        HumidityCategory.MEDIUM -> TraitConditionCard.MEDIUM_HUMIDITY.traitCondition
        HumidityCategory.HIGH -> TraitConditionCard.HIGH_HUMIDITY.traitCondition
    }

    val effects = getEffects(traits, setOf(temperatureTraitCondition, humidityTraitCondition))
    return TraitEffectKeyCard.TEMPERATURE.traitEffectKey in effects && TraitEffectKeyCard.HUMIDITY.traitEffectKey in effects
}
