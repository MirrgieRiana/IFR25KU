package miragefairy2024.mod.structure

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.common.rootAdvancement
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.placeditem.PlacedItemCard
import miragefairy2024.mod.tool.ToolCard
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
import miragefairy2024.util.get
import miragefairy2024.util.invoke
import miragefairy2024.util.registerArchaeologyLootTableGeneration
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.text
import miragefairy2024.util.times
import miragefairy2024.util.toStructureTag
import miragefairy2024.util.with
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.Pools
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.VerticalAnchor
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.levelgen.structure.StructureSet
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest
import net.minecraft.world.level.levelgen.structure.templatesystem.GravityProcessor
import net.minecraft.world.level.levelgen.structure.templatesystem.PosAlwaysTrueTest
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.AppendLoot
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction
import net.minecraft.world.level.storage.loot.functions.ExplorationMapFunction
import net.minecraft.world.level.storage.loot.functions.SetNameFunction
import java.util.Optional

object WeatheredAncientRemnantsCard {
    val identifier = MirageFairy2024.identifier("weathered_ancient_remnants")
    val key = Registries.STRUCTURE with identifier
    val translation = Translation({ identifier.toLanguageKey("structure") }, "Weathered Ancient Remnants", "風化した旧世代の遺構")

    val onMapsTag = MirageFairy2024.identifier("on_weathered_ancient_remnants_archaeology_maps").toStructureTag()

    val advancement = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { rootAdvancement.await() },
        icon = { Items.POLISHED_ANDESITE.createItemStack() },
        name = EnJa("Spacefaring Humanity's Scars", "宇宙人類の爪痕"),
        description = EnJa("Discover Weathered Ancient Remnants left on the surface", "地上に遺された風化した旧世代の遺構を発見する"),
        criterion = AdvancementCard.visit(key),
        type = AdvancementCardType.TOAST_AND_JEWELS,
    )

    context(ModContext)
    fun init() {

        translation.enJa()
        onMapsTag.enJa(EnJa("On Weathered Ancient Remnants Archaeology Maps", "風化した旧世代の遺構の考古学地図で"))

        val archaeologyLootTable = Registries.LOOT_TABLE with "archaeology/" * identifier
        registerArchaeologyLootTableGeneration(archaeologyLootTable) { registries ->
            LootTable(
                LootPool(
                    ItemLootPoolEntry(Items.RAW_IRON).setWeight(10),
                    ItemLootPoolEntry(Items.RAW_COPPER).setWeight(10),
                    ItemLootPoolEntry(Items.GOLD_NUGGET).setWeight(10),
                    ItemLootPoolEntry(Items.GLASS_PANE).setWeight(5),
                    ItemLootPoolEntry(MaterialCard.XARPITE.item()).setWeight(20),

                    ItemLootPoolEntry(ToolCard.AMETHYST.pickaxe.item()).setWeight(1),
                    ItemLootPoolEntry(ToolCard.AMETHYST.axe.item()).setWeight(1),
                    ItemLootPoolEntry(ToolCard.AMETHYST.shovel.item()).setWeight(1),
                    ItemLootPoolEntry(ToolCard.AMETHYST.hoe.item()).setWeight(1),
                    ItemLootPoolEntry(ToolCard.AMETHYST.sword.item()).setWeight(1),
                    ItemLootPoolEntry(MaterialCard.CHAOS_STONE.item()).setWeight(3),
                    ItemLootPoolEntry(MaterialCard.LILAGIUM_INGOT.item()).setWeight(3),
                    ItemLootPoolEntry(Items.AMETHYST_SHARD).setWeight(3),
                    ItemLootPoolEntry(Items.BOOK).setWeight(10).apply(EnchantRandomlyFunction.randomApplicableEnchantment(registries)),
                    ItemLootPoolEntry(MaterialCard.JEWEL_100.item()).setWeight(3),
                    ItemLootPoolEntry(Items.MAP) {
                        apply(
                            ExplorationMapFunction.makeExplorationMap()
                                .setDestination(onMapsTag)
                                .setMapDecoration(MapDecorationTypes.BROWN_BANNER)
                                .setZoom(3)
                                .setSkipKnownStructures(false)
                        )
                        apply(SetNameFunction.setName(text { MAP_TRANSLATION(DripstoneCavesRuinCard.translation()) }, SetNameFunction.Target.ITEM_NAME))
                    }.setWeight(2),
                ),
            )
        }

        val element = identifier

        val processorListKey = registerDynamicGeneration(Registries.PROCESSOR_LIST, identifier) {
            StructureProcessorList(
                BlockIgnoreProcessor(listOf(Blocks.AIR, Blocks.DIRT, Blocks.GRASS_BLOCK)),
                GravityProcessor(Heightmap.Types.OCEAN_FLOOR_WG, -3),
                RuleStructureProcessor(
                    ProcessorRule(
                        RandomBlockMatchTest(Blocks.GRAVEL, 0.2F),
                        AlwaysTrueTest.INSTANCE,
                        PosAlwaysTrueTest.INSTANCE,
                        Blocks.SUSPICIOUS_GRAVEL.defaultBlockState(),
                        AppendLoot(archaeologyLootTable),
                    ),
                ),
                // これが無いと生成時に即水没してBlockEntityが作れたなったエラーログが大量に出る
                RuleStructureProcessor(
                    ProcessorRule(
                        BlockMatchTest(PlacedItemCard.block()),
                        BlockMatchTest(Blocks.WATER),
                        Blocks.WATER.defaultBlockState(),
                    ),
                ),
            )
        }

        val templatePoolKey = registerDynamicGeneration(Registries.TEMPLATE_POOL, identifier) {
            StructurePool(
                Pools.EMPTY,
                SinglePoolElement(element, processorListKey, StructureTemplatePool.Projection.RIGID) to 1,
            )
        }

        val structureKey = registerDynamicGeneration(Registries.STRUCTURE, identifier) {
            UnlimitedJigsawStructure(
                config = Structure.StructureSettings(
                    Registries.BIOME[ConventionalBiomeTags.IS_OVERWORLD],
                    mapOf(),
                    GenerationStep.Decoration.SURFACE_STRUCTURES,
                    TerrainAdjustment.NONE,
                ),
                startPool = Registries.TEMPLATE_POOL[templatePoolKey],
                size = 1,
                projectStartToHeightmap = Optional.of(Heightmap.Types.WORLD_SURFACE_WG),
                startHeight = ConstantHeight.of(VerticalAnchor.absolute(0)),
                useExpansionHack = false,
            )
        }

        registerDynamicGeneration(Registries.STRUCTURE_SET, identifier) {
            StructureSet(
                listOf(
                    StructureSet.StructureSelectionEntry(Registries.STRUCTURE[structureKey], 1),
                ),
                RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 94857624),
            )
        }

        advancement.init()

    }
}
