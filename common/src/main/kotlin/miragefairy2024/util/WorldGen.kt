package miragefairy2024.util

import miragefairy2024.DataGenerationEvents
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.magicplant.MagicPlantCard
import miragefairy2024.mod.magicplant.hasEnvironmentAdaptation
import mirrg.kotlin.helium.floorToInt
import net.fabricmc.fabric.api.biome.v1.BiomeModifications
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors
import net.minecraft.core.Direction
import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.data.worldgen.placement.PlacementUtils
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BiomeTags
import net.minecraft.tags.TagKey
import net.minecraft.util.random.SimpleWeightedRandomList
import net.minecraft.util.valueproviders.ConstantInt
import net.minecraft.util.valueproviders.IntProvider
import net.minecraft.util.valueproviders.WeightedListInt
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.VerticalAnchor
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration
import net.minecraft.world.level.levelgen.placement.BiomeFilter
import net.minecraft.world.level.levelgen.placement.CountPlacement
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement
import net.minecraft.world.level.levelgen.placement.InSquarePlacement
import net.minecraft.world.level.levelgen.placement.PlacedFeature
import net.minecraft.world.level.levelgen.placement.PlacementModifier
import net.minecraft.world.level.levelgen.placement.RandomOffsetPlacement
import net.minecraft.world.level.levelgen.placement.RarityFilter
import net.minecraft.world.level.levelgen.placement.SurfaceWaterDepthFilter
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList
import java.util.function.Predicate
import kotlin.math.roundToInt

infix fun <C : FeatureConfiguration, F : Feature<C>> F.with(config: C): ConfiguredFeature<C, F> = ConfiguredFeature(this, config)
infix fun Holder<ConfiguredFeature<*, *>>.with(placementModifiers: List<PlacementModifier>) = PlacedFeature(this, placementModifiers)


// Init

context(BootstrapContext<*>)
operator fun <T> ResourceKey<Registry<T>>.get(key: ResourceKey<T>): Holder<T> {
    return this@BootstrapContext.lookup(this).getOrThrow(key)
}

context(BootstrapContext<*>)
operator fun <T> ResourceKey<Registry<T>>.get(key: TagKey<T>): HolderSet.Named<T> {
    return this@BootstrapContext.lookup(this).getOrThrow(key)
}

context(ModContext)
fun <T : Any> registerDynamicGeneration(registryKey: ResourceKey<out Registry<T>>, identifier: ResourceLocation, creator: context(BootstrapContext<T>) () -> T): ResourceKey<T> {
    val key = registryKey with identifier
    registerDynamicGeneration(key, creator)
    return key
}

context(ModContext)
fun <T : Any> registerDynamicGeneration(key: ResourceKey<T>, creator: context(BootstrapContext<T>) () -> T) {
    val registryKey = ResourceKey.createRegistryKey<T>(key.registry())
    DataGenerationEvents.onBuildRegistry {
        it.add<T>(registryKey) { context ->
            context.register(key, creator(context))
        }
    }
    DataGenerationEvents.onInitializeDataGenerator {
        DataGenerationEvents.dynamicGenerationRegistries += registryKey
    }
}

context(ModContext)
fun ResourceKey<PlacedFeature>.registerFeature(step: GenerationStep.Decoration, biomeSelectorCreator: BiomeSelectorScope.() -> Predicate<BiomeSelectionContext>) = ModEvents.onInitialize {
    BiomeModifications.addFeature(biomeSelectorCreator(BiomeSelectorScope), step, this)
}

context(ModContext)
fun (() -> EntityType<*>).registerSpawn(spawnGroup: MobCategory, weight: Int, minGroupSize: Int, maxGroupSize: Int, biomeSelectorCreator: BiomeSelectorScope.() -> Predicate<BiomeSelectionContext>) = ModEvents.onInitialize {
    BiomeModifications.addSpawn(biomeSelectorCreator(BiomeSelectorScope), spawnGroup, this(), weight, minGroupSize, maxGroupSize)
}


// BiomeSelectorScope

object BiomeSelectorScope

context(BiomeSelectorScope) val all: Predicate<BiomeSelectionContext> get() = BiomeSelectors.all()
context(BiomeSelectorScope) val overworld: Predicate<BiomeSelectionContext> get() = +BiomeTags.IS_OVERWORLD // BiomeSelectors.foundInOverworld() はTerraBlenderの追加バイオームを無視する
context(BiomeSelectorScope) val nether: Predicate<BiomeSelectionContext> get() = +BiomeTags.IS_NETHER // 上と同じ
context(BiomeSelectorScope) val end: Predicate<BiomeSelectionContext> get() = +BiomeTags.IS_END // 上と同じ
context(BiomeSelectorScope) operator fun ResourceKey<Biome>.unaryPlus(): Predicate<BiomeSelectionContext> = BiomeSelectors.includeByKey(this)
context(BiomeSelectorScope) operator fun TagKey<Biome>.unaryPlus(): Predicate<BiomeSelectionContext> = BiomeSelectors.tag(this)
context(BiomeSelectorScope) operator fun Predicate<BiomeSelectionContext>.not(): Predicate<BiomeSelectionContext> = this.negate()
context(BiomeSelectorScope) operator fun Predicate<BiomeSelectionContext>.times(other: Predicate<BiomeSelectionContext>): Predicate<BiomeSelectionContext> = this.and(other)
context(BiomeSelectorScope) operator fun Predicate<BiomeSelectionContext>.plus(other: Predicate<BiomeSelectionContext>): Predicate<BiomeSelectionContext> = this.or(other)
context(BiomeSelectorScope) operator fun TemperatureCategory.unaryPlus(): Predicate<BiomeSelectionContext> = Predicate { it.biomeRegistryEntry.temperatureCategory == this }
context(BiomeSelectorScope) operator fun HumidityCategory.unaryPlus(): Predicate<BiomeSelectionContext> = Predicate { it.biomeRegistryEntry.humidityCategory == this }
context(BiomeSelectorScope) val cold get() = +TemperatureCategory.LOW
context(BiomeSelectorScope) val warm get() = +TemperatureCategory.MEDIUM
context(BiomeSelectorScope) val hot get() = +TemperatureCategory.HIGH
context(BiomeSelectorScope) val arid get() = +HumidityCategory.LOW
context(BiomeSelectorScope) val mesic get() = +HumidityCategory.MEDIUM
context(BiomeSelectorScope) val humid get() = +HumidityCategory.HIGH
context(MagicPlantCard<*>, BiomeSelectorScope) val defaultTraits get(): Predicate<BiomeSelectionContext> = Predicate { this@MagicPlantCard.hasEnvironmentAdaptation(false, it.biomeRegistryEntry.temperatureCategory, it.biomeRegistryEntry.humidityCategory) }


// PlacementModifier

object PlacementModifiersScope

fun placementModifiers(block: PlacementModifiersScope.() -> List<PlacementModifier>) = block(PlacementModifiersScope)

context(PlacementModifiersScope)
fun randomIntCount(count: Double): List<PlacementModifier> {
    val min = count.floorToInt()
    val rate = ((count - min.toDouble()) * 100).roundToInt()
    if (rate == 0) return count(min)
    if (rate == 100) return count(min + 1)
    val dataPool = SimpleWeightedRandomList.builder<IntProvider>()
        .add(ConstantInt.of(min), 100 - rate)
        .add(ConstantInt.of(min + 1), rate)
        .build()
    val intProvider = WeightedListInt(dataPool)
    return listOf(CountPlacement.of(intProvider))
}

context(PlacementModifiersScope)
fun count(count: Int): List<PlacementModifier> = listOf(CountPlacement.of(count))

context(PlacementModifiersScope)
fun per(chance: Int): List<PlacementModifier> = listOf(RarityFilter.onAverageOnceEvery(chance))

context(PlacementModifiersScope)
fun tree(saplingBlock: Block): List<PlacementModifier> = listOf(
    InSquarePlacement.spread(),
    SurfaceWaterDepthFilter.forMaxDepth(0),
    PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
    BiomeFilter.biome(),
    PlacementUtils.filteredByBlockSurvival(saplingBlock),
)

context(PlacementModifiersScope)
fun uniformOre(minOffset: Int, maxOffset: Int): List<PlacementModifier> = listOf(
    InSquarePlacement.spread(),
    HeightRangePlacement.uniform(VerticalAnchor.absolute(minOffset), VerticalAnchor.absolute(maxOffset)),
    BiomeFilter.biome(),
)

fun interface HorizontalPlacementType {
    fun getPlacementModifiers(): List<PlacementModifier>
}

/** 半径8未満のFeatureまで可能。 */
context(PlacementModifiersScope)
val none
    get() = HorizontalPlacementType { listOf() }

/** 半径8未満のFeatureまで可能。 */
context(PlacementModifiersScope)
val square
    get() = HorizontalPlacementType { listOf(InSquarePlacement.spread()) }

/** 半径16未満のFeatureまで可能。 */
context(PlacementModifiersScope)
val center
    get() = HorizontalPlacementType { listOf(RandomOffsetPlacement.horizontal(ConstantInt.of(8))) }

fun interface VerticalPlacementType {
    fun getPlacementModifiers(): List<PlacementModifier>
}

context(PlacementModifiersScope)
val surface
    get() = VerticalPlacementType { listOf(PlacementUtils.HEIGHTMAP) }

context(PlacementModifiersScope)
val nether
    get() = VerticalPlacementType {
        listOf(
            PlacementUtils.FULL_RANGE,
            EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.solid(), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
            RandomOffsetPlacement.vertical(ConstantInt.of(1)),
        )
    }

context(PlacementModifiersScope)
val underground
    get() = VerticalPlacementType {
        listOf(
            PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT,
            EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.solid(), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
            RandomOffsetPlacement.vertical(ConstantInt.of(1)),
        )
    }

context(PlacementModifiersScope)
fun rangedNether(minY: Int, maxY: Int) = VerticalPlacementType {
    listOf(
        HeightRangePlacement.uniform(VerticalAnchor.absolute(minY), VerticalAnchor.absolute(maxY)),
        EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.solid(), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
        RandomOffsetPlacement.vertical(ConstantInt.of(1)),
    )
}

context(PlacementModifiersScope)
fun flower(horizontal: HorizontalPlacementType, vertical: VerticalPlacementType): List<PlacementModifier> {
    return listOf(
        *horizontal.getPlacementModifiers().toTypedArray(),
        *vertical.getPlacementModifiers().toTypedArray(),
        BiomeFilter.biome(),
    )
}


// Structure

context(BootstrapContext<*>)
fun RuleStructureProcessor(vararg rules: ProcessorRule): RuleProcessor {
    return RuleProcessor(rules.toList())
}

context(BootstrapContext<*>)
fun StructureProcessorList(vararg processors: StructureProcessor): StructureProcessorList {
    return StructureProcessorList(processors.toList())
}

context(BootstrapContext<*>)
fun SinglePoolElement(location: ResourceLocation, processorsKey: ResourceKey<StructureProcessorList>, projection: StructureTemplatePool.Projection): StructurePoolElement {
    return StructurePoolElement.single(location.string, Registries.PROCESSOR_LIST[processorsKey]).apply(projection)
}

context(BootstrapContext<*>)
fun StructurePool(fallbackKey: ResourceKey<StructureTemplatePool>, vararg elements: Pair<StructurePoolElement, Int>): StructureTemplatePool {
    return StructureTemplatePool(Registries.TEMPLATE_POOL[fallbackKey], elements.map { com.mojang.datafixers.util.Pair.of(it.first, it.second) })
}
