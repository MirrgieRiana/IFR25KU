package miragefairy2024.mod.structure

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.entity.ChaosCubeCard
import miragefairy2024.mod.magicplant.contents.magicplants.XarpaLuminariaCard
import miragefairy2024.mod.materials.BlockMaterialCard
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.ItemLootPoolEntry
import miragefairy2024.util.LootPool
import miragefairy2024.util.LootTable
import miragefairy2024.util.RuleStructureProcessor
import miragefairy2024.util.SinglePoolElement
import miragefairy2024.util.StructurePool
import miragefairy2024.util.StructureProcessorList
import miragefairy2024.util.Translation
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.generator
import miragefairy2024.util.get
import miragefairy2024.util.registerChestLootTableGeneration
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.times
import miragefairy2024.util.with
import net.minecraft.core.HolderSet
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.Pools
import net.minecraft.util.random.WeightedRandomList
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.item.Items
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.biome.MobSpawnSettings
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.VerticalAnchor
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.levelgen.structure.StructureSet
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator

object DripstoneCavesRuinCard {
    val identifier = MirageFairy2024.identifier("dripstone_caves_ruin")
    val key = Registries.STRUCTURE with identifier
    val translation = Translation({ identifier.toLanguageKey("structure") }, "Dripstone Cave Ruin", "鍾乳洞の遺跡")

    val advancement = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { WeatheredAncientRemnantsCard.advancement.await() },
        icon = { BlockMaterialCard.CHAOS_STONE_BLOCK.item().createItemStack() },
        name = EnJa("The Ancient Future Civ", "古代の未来文明"),
        description = EnJa("Unearth a map from the Weathered Ancient Remnants and explore the Dripstone Cave Ruin", "風化した旧世代の遺構から地図を発掘し、鍾乳洞の遺跡を訪れる"),
        criterion = AdvancementCard.visit(key),
        type = AdvancementCardType.NORMAL,
    )

    context(_: ModContext)
    fun init() {

        WeatheredAncientRemnantsCard.onMapsTag.generator.registerChild(identifier)
        translation.enJa()


        registerChestLootTableGeneration(Registries.LOOT_TABLE with "chests/" * identifier * "/chest_books") { registries ->
            LootTable(
                LootPool(
                    ItemLootPoolEntry(Items.BOOK).setWeight(10).apply(EnchantRandomlyFunction.randomApplicableEnchantment(registries)),
                    ItemLootPoolEntry(Items.BOOK).setWeight(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 10.0F))),
                ) {
                    setRolls(UniformGenerator.between(5.0F, 15.0F))
                },
            )
        }

        val mainElement = identifier * "/main"
        val roadCrossElement = identifier * "/road_cross"
        val roadEndElement = identifier * "/road_end"
        val roadRoomsElement = identifier * "/road_rooms"
        val roadStairsElement = identifier * "/road_stairs"
        val roadStraightElement = identifier * "/road_straight"
        val roadStraight2Element = identifier * "/road_straight2"
        val roadStraight3Element = identifier * "/road_straight3"
        val roadMobs = identifier * "/road_mobs"
        val roomConferenceElement = identifier * "/room_conference"
        val roomConference2Element = identifier * "/room_conference2"
        val roomEmptyElement = identifier * "/room_empty"
        val roomEndElement = identifier * "/room_end"
        val roomFarmElement = identifier * "/room_farm"
        val roomFarm2Element = identifier * "/room_farm2"
        val roomLaboratoryElement = identifier * "/room_laboratory"
        val roomLibraryElement = identifier * "/room_library"
        val roomPrisonElement = identifier * "/room_prison"
        val roomPrison2Element = identifier * "/room_prison2"
        val roomPrison3Element = identifier * "/room_prison3"
        val roomResidenceElement = identifier * "/room_residence"
        val roomSpawnerElement = identifier * "/room_spawner"
        val stairsBottomElement = identifier * "/stairs_bottom"
        val stairsMiddleElement = identifier * "/stairs_middle"
        val stairsTopElement = identifier * "/stairs_top"
        val mobElement = identifier * "/mob"
        val mobEmptyElement = identifier * "/mob_empty"

        val processorListKey = registerDynamicGeneration(Registries.PROCESSOR_LIST, identifier) {
            StructureProcessorList(
                RuleStructureProcessor(
                    ProcessorRule(RandomBlockMatchTest(Blocks.POLISHED_GRANITE, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.GRANITE.defaultBlockState()),
                    ProcessorRule(RandomBlockMatchTest(Blocks.LANTERN, 0.8F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
                    ProcessorRule(RandomBlockMatchTest(Blocks.REDSTONE_TORCH, 0.05F), AlwaysTrueTest.INSTANCE, XarpaLuminariaCard.block().withAge(3)),
                    ProcessorRule(BlockMatchTest(Blocks.REDSTONE_TORCH), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
                ),
                RuleStructureProcessor(
                    ProcessorRule(AlwaysTrueTest.INSTANCE, BlockMatchTest(Blocks.AIR), Blocks.AIR.defaultBlockState()),
                    ProcessorRule(AlwaysTrueTest.INSTANCE, BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState()),
                    ProcessorRule(AlwaysTrueTest.INSTANCE, BlockMatchTest(Blocks.LAVA), Blocks.LAVA.defaultBlockState()),
                ),
            )
        }

        val mainTemplatePoolKey = registerDynamicGeneration(Registries.TEMPLATE_POOL, identifier * "/main") {
            StructurePool(
                Pools.EMPTY,
                SinglePoolElement(mainElement, processorListKey, StructureTemplatePool.Projection.RIGID) to 1,
            )
        }

        val roadEndTemplatePoolKey = registerDynamicGeneration(Registries.TEMPLATE_POOL, identifier * "/road_end") {
            StructurePool(
                Pools.EMPTY,
                SinglePoolElement(roadEndElement, processorListKey, StructureTemplatePool.Projection.RIGID) to 1,
            )
        }
        registerDynamicGeneration(Registries.TEMPLATE_POOL, identifier * "/road") {
            StructurePool(
                roadEndTemplatePoolKey,
                SinglePoolElement(roadStraightElement, processorListKey, StructureTemplatePool.Projection.RIGID) to 40,
                SinglePoolElement(roadStraight2Element, processorListKey, StructureTemplatePool.Projection.RIGID) to 10,
                SinglePoolElement(roadStraight3Element, processorListKey, StructureTemplatePool.Projection.RIGID) to 5,
                SinglePoolElement(roadRoomsElement, processorListKey, StructureTemplatePool.Projection.RIGID) to 40,
                SinglePoolElement(roadCrossElement, processorListKey, StructureTemplatePool.Projection.RIGID) to 1,
                SinglePoolElement(roadStairsElement, processorListKey, StructureTemplatePool.Projection.RIGID) to 3,
                SinglePoolElement(roadEndElement, processorListKey, StructureTemplatePool.Projection.RIGID) to 5,
                SinglePoolElement(roadMobs, processorListKey, StructureTemplatePool.Projection.RIGID) to 10,
            )
        }
        registerDynamicGeneration(Registries.TEMPLATE_POOL, identifier * "/stairs") {
            StructurePool(
                Pools.EMPTY,
                SinglePoolElement(stairsTopElement, processorListKey, StructureTemplatePool.Projection.RIGID) to 1,
                SinglePoolElement(stairsMiddleElement, processorListKey, StructureTemplatePool.Projection.RIGID) to 2,
                SinglePoolElement(stairsBottomElement, processorListKey, StructureTemplatePool.Projection.RIGID) to 1,
            )
        }
        val roomEndTemplatePoolKey = registerDynamicGeneration(Registries.TEMPLATE_POOL, identifier * "/room_end") {
            StructurePool(
                Pools.EMPTY,
                SinglePoolElement(roomEndElement, processorListKey, StructureTemplatePool.Projection.RIGID) to 1,
            )
        }
        registerDynamicGeneration(Registries.TEMPLATE_POOL, identifier * "/room") {
            StructurePool(
                roomEndTemplatePoolKey,
                SinglePoolElement(roomConferenceElement, processorListKey, StructureTemplatePool.Projection.RIGID) to 10,
                SinglePoolElement(roomConference2Element, processorListKey, StructureTemplatePool.Projection.RIGID) to 10,
                SinglePoolElement(roomResidenceElement, processorListKey, StructureTemplatePool.Projection.RIGID) to 20,
                SinglePoolElement(roomSpawnerElement, processorListKey, StructureTemplatePool.Projection.RIGID) to 2,
                SinglePoolElement(roomPrisonElement, processorListKey, StructureTemplatePool.Projection.RIGID) to 5,
                SinglePoolElement(roomPrison2Element, processorListKey, StructureTemplatePool.Projection.RIGID) to 10,
                SinglePoolElement(roomPrison3Element, processorListKey, StructureTemplatePool.Projection.RIGID) to 2,
                SinglePoolElement(roomLibraryElement, processorListKey, StructureTemplatePool.Projection.RIGID) to 2,
                SinglePoolElement(roomLaboratoryElement, processorListKey, StructureTemplatePool.Projection.RIGID) to 5,
                SinglePoolElement(roomFarmElement, processorListKey, StructureTemplatePool.Projection.RIGID) to 10,
                SinglePoolElement(roomFarm2Element, processorListKey, StructureTemplatePool.Projection.RIGID) to 5,
                SinglePoolElement(roomEmptyElement, processorListKey, StructureTemplatePool.Projection.RIGID) to 1,
                SinglePoolElement(roomEndElement, processorListKey, StructureTemplatePool.Projection.RIGID) to 2,
            )
        }
        registerDynamicGeneration(Registries.TEMPLATE_POOL, identifier * "/mob") {
            StructurePool(
                Pools.EMPTY,
                SinglePoolElement(mobElement, processorListKey, StructureTemplatePool.Projection.RIGID) to 10,
                SinglePoolElement(mobEmptyElement, processorListKey, StructureTemplatePool.Projection.RIGID) to 3,
            )
        }

        val structureKey = registerDynamicGeneration(Registries.STRUCTURE, identifier) {
            UnlimitedJigsawStructure(
                config = Structure.StructureSettings(
                    HolderSet.direct(Registries.BIOME[Biomes.DRIPSTONE_CAVES]),
                    mapOf(
                        MobCategory.MONSTER to StructureSpawnOverride(
                            StructureSpawnOverride.BoundingBoxType.PIECE,
                            WeightedRandomList.create(
                                MobSpawnSettings.SpawnerData(ChaosCubeCard.entityType(), 10, 1, 4),
                            )
                        ),
                    ),
                    GenerationStep.Decoration.UNDERGROUND_STRUCTURES,
                    TerrainAdjustment.BURY,
                ),
                startPool = Registries.TEMPLATE_POOL[mainTemplatePoolKey],
                size = 12,
                startHeight = UniformHeight.of(VerticalAnchor.absolute(-40), VerticalAnchor.absolute(20)),
                useExpansionHack = false,
            )
        }

        registerDynamicGeneration(Registries.STRUCTURE_SET, identifier) {
            StructureSet(
                listOf(
                    StructureSet.StructureSelectionEntry(Registries.STRUCTURE[structureKey], 1),
                ),
                RandomSpreadStructurePlacement(42, 12, RandomSpreadType.LINEAR, 645172983),
            )
        }

        advancement.init()

    }
}
