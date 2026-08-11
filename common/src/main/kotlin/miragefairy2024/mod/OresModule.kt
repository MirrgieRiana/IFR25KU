package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.lib.OreBlock
import miragefairy2024.mod.biome.DeepFairyForestBiomeCard
import miragefairy2024.mod.common.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.enchantment.contents.STICKY_MINING_BLOCK_TAG
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.util.BiomeSelectorScope
import miragefairy2024.util.EnJa
import miragefairy2024.util.Model
import miragefairy2024.util.ModelData
import miragefairy2024.util.ModelElementData
import miragefairy2024.util.ModelElementsData
import miragefairy2024.util.ModelFaceData
import miragefairy2024.util.ModelFacesData
import miragefairy2024.util.ModelTexturesData
import miragefairy2024.util.Registration
import miragefairy2024.util.ResourceLocation
import miragefairy2024.util.enJa
import miragefairy2024.util.generator
import miragefairy2024.util.nether
import miragefairy2024.util.overworld
import miragefairy2024.util.plus
import miragefairy2024.util.randomIntCount
import miragefairy2024.util.register
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerConfiguredFeature
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerFeature
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerOreLootTableGeneration
import miragefairy2024.util.registerPlacedFeature
import miragefairy2024.util.registerSingletonBlockStateGeneration
import miragefairy2024.util.string
import miragefairy2024.util.times
import miragefairy2024.util.unaryPlus
import miragefairy2024.util.uniformOre
import miragefairy2024.util.with
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.models.model.TextureSlot
import net.minecraft.data.models.model.TexturedModel
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagKey
import net.minecraft.util.valueproviders.UniformInt
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest
import net.minecraft.world.level.material.MapColor
import java.util.function.Predicate

enum class BaseStoneType(val target: () -> RuleTest, val baseStoneTexture: ResourceLocation, val mineableTag: TagKey<Block>, val needsToolTag: TagKey<Block>?) {
    STONE({ TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES) }, ResourceLocation("minecraft", "block/stone"), BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_STONE_TOOL),
    DEEPSLATE({ TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES) }, ResourceLocation("minecraft", "block/deepslate"), BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_STONE_TOOL),
    SANDSTONE({ BlockMatchTest(Blocks.SANDSTONE) }, ResourceLocation("minecraft", "block/sandstone_top"), BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_STONE_TOOL),
    DIRT({ BlockMatchTest(Blocks.DIRT) }, ResourceLocation("minecraft", "block/dirt"), BlockTags.MINEABLE_WITH_SHOVEL, null),
    NETHERRACK({ BlockMatchTest(Blocks.NETHERRACK) }, ResourceLocation("minecraft", "block/netherrack"), BlockTags.MINEABLE_WITH_PICKAXE, null),
    BLACKSTONE({ BlockMatchTest(Blocks.BLACKSTONE) }, ResourceLocation("minecraft", "block/blackstone"), BlockTags.MINEABLE_WITH_PICKAXE, null),
}

enum class OreCard(
    path: String,
    val enName: String,
    val jaName: String,
    val poemList: PoemList?,
    val baseStoneType: BaseStoneType,
    texturePath: String,
    val dropItem: () -> Item,
    experience: Pair<Int, Int>,
    val tags: List<TagKey<Block>> = emptyList(),
) {
    MAGNETITE_ORE(
        "magnetite_ore", "Magnetite Ore", "磁鉄鉱鉱石",
        null,
        BaseStoneType.STONE, "magnetite_ore", MaterialCard.MAGNETITE.item, 2 to 5,
        tags = listOf(STICKY_MINING_BLOCK_TAG),
    ),
    DEEPSLATE_MAGNETITE_ORE(
        "deepslate_magnetite_ore", "Deepslate Magnetite Ore", "深層磁鉄鉱鉱石",
        null,
        BaseStoneType.DEEPSLATE, "magnetite_ore", MaterialCard.MAGNETITE.item, 2 to 5,
        tags = listOf(STICKY_MINING_BLOCK_TAG),
    ),
    NETHER_MAGNETITE_ORE(
        "nether_magnetite_ore", "Nether Magnetite Ore", "ネザー磁鉄鉱鉱石",
        null,
        BaseStoneType.NETHERRACK, "magnetite_ore", MaterialCard.MAGNETITE.item, 2 to 5,
        tags = listOf(STICKY_MINING_BLOCK_TAG),
    ),
    FLUORITE_ORE(
        "fluorite_ore", "Fluorite Ore", "蛍石鉱石",
        null,
        BaseStoneType.STONE, "fluorite_ore", MaterialCard.FLUORITE.item, 2 to 5,
    ),
    DEEPSLATE_FLUORITE_ORE(
        "deepslate_fluorite_ore", "Deepslate Fluorite Ore", "深層蛍石鉱石",
        null,
        BaseStoneType.DEEPSLATE, "fluorite_ore", MaterialCard.FLUORITE.item, 2 to 5,
    ),
    SALTPETER_ORE(
        "saltpeter_ore", "Saltpeter Ore", "硝石鉱石",
        null,
        BaseStoneType.STONE, "saltpeter_ore", MaterialCard.SALTPETER.item, 2 to 5,
    ),
    DEEPSLATE_SALTPETER_ORE(
        "deepslate_saltpeter_ore", "Deepslate Saltpeter Ore", "深層硝石鉱石",
        null,
        BaseStoneType.DEEPSLATE, "saltpeter_ore", MaterialCard.SALTPETER.item, 2 to 5,
    ),
    DIRT_SALTPETER_ORE(
        "dirt_saltpeter_ore", "Dirt Saltpeter Ore", "土硝石鉱石",
        null,
        BaseStoneType.DIRT, "saltpeter_ore", MaterialCard.SALTPETER.item, 2 to 5,
    ),
    SANDSTONE_SALTPETER_ORE(
        "sandstone_saltpeter_ore", "Sandstone Saltpeter Ore", "砂岩硝石鉱石",
        null,
        BaseStoneType.SANDSTONE, "saltpeter_ore", MaterialCard.SALTPETER.item, 2 to 5,
    ),
    SULFUR_ORE(
        "sulfur_ore", "Sulfur Ore", "硫黄鉱石",
        null,
        BaseStoneType.STONE, "sulfur_ore", MaterialCard.SULFUR.item, 2 to 5,
    ),
    DEEPSLATE_SULFUR_ORE(
        "deepslate_sulfur_ore", "Deepslate Sulfur Ore", "深層硫黄鉱石",
        null,
        BaseStoneType.DEEPSLATE, "sulfur_ore", MaterialCard.SULFUR.item, 2 to 5,
    ),
    NETHER_SULFUR_ORE(
        "nether_sulfur_ore", "Nether Sulfur Ore", "ネザー硫黄鉱石",
        null,
        BaseStoneType.NETHERRACK, "sulfur_ore", MaterialCard.SULFUR.item, 2 to 5,
    ),
    BLACKSTONE_SULFUR_ORE(
        "blackstone_sulfur_ore", "Blackstone Sulfur Ore", "ブラックストーン硫黄鉱石",
        null,
        BaseStoneType.BLACKSTONE, "sulfur_ore", MaterialCard.SULFUR.item, 2 to 5,
    ),
    NEPHRITE_ORE(
        "nephrite_ore", "Nephrite Ore", "ネフライト鉱石",
        null,
        BaseStoneType.STONE, "nephrite_ore", MaterialCard.NEPHRITE.item, 2 to 5,
    ),
    DEEPSLATE_NEPHRITE_ORE(
        "deepslate_nephrite_ore", "Deepslate Nephrite Ore", "深層ネフライト鉱石",
        null,
        BaseStoneType.DEEPSLATE, "nephrite_ore", MaterialCard.NEPHRITE.item, 2 to 5,
    ),
    MIRANAGITE_ORE(
        "miranagite_ore", "Miranagite Ore", "蒼天石鉱石",
        PoemList(2).poem("What lies beyond a Garden of Eden?", "秩序の石は楽園の先に何を見るのか？"),
        BaseStoneType.STONE, "miranagite_ore", MaterialCard.MIRANAGITE.item, 2 to 5,
    ),
    DEEPSLATE_MIRANAGITE_ORE(
        "deepslate_miranagite_ore", "Deepslate Miranagite Ore", "深層蒼天石鉱石",
        PoemList(2).poem("Singularities built by the Creator", "楽園が楽園であるための奇跡。"),
        BaseStoneType.DEEPSLATE, "miranagite_ore", MaterialCard.MIRANAGITE.item, 2 to 5,
    ),
    DIRT_FAIRY_PLASTIC_ORE(
        "dirt_fairy_plastic_ore", "Dirt Fairy Plastic Ore", "土妖精のプラスチック鉱石",
        PoemList(4).poem("An asteric condensation chamber.", "千年越しに見る光。"),
        BaseStoneType.DIRT, "fairy_plastic_ore", MaterialCard.FAIRY_PLASTIC.item, 10 to 20,
    ),
    ;

    val identifier = MirageFairy2024.identifier(path)
    val block = Registration(BuiltInRegistries.BLOCK, identifier) {
        val settings = when (baseStoneType) {
            BaseStoneType.STONE -> BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops()
                .strength(3.0F, 3.0F)

            BaseStoneType.DEEPSLATE -> BlockBehaviour.Properties.of()
                .mapColor(MapColor.DEEPSLATE)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops()
                .strength(4.5F, 3.0F)
                .sound(SoundType.DEEPSLATE)

            BaseStoneType.SANDSTONE -> BlockBehaviour.Properties.of()
                .mapColor(MapColor.SAND)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops()
                .strength(1.0F, 1.0F)

            BaseStoneType.DIRT -> BlockBehaviour.Properties.of()
                .mapColor(MapColor.DIRT)
                .strength(1.0F, 1.0F)
                .sound(SoundType.GRAVEL)

            BaseStoneType.NETHERRACK -> BlockBehaviour.Properties.of()
                .mapColor(MapColor.NETHER)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops()
                .strength(3.0F, 3.0F)
                .sound(SoundType.NETHER_ORE)

            BaseStoneType.BLACKSTONE -> BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BLACK)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops()
                .strength(3.0F, 3.0F)
        }
        OreBlock(UniformInt.of(experience.first, experience.second), settings)
    }
    val item = Registration(BuiltInRegistries.ITEM, identifier) { BlockItem(block.await(), Item.Properties()) }
    val texturedModelFactory = TexturedModel.Provider {
        OreModelCard.model.with(
            TextureSlot.BACK to baseStoneType.baseStoneTexture,
            TextureSlot.FRONT to "block/" * MirageFairy2024.identifier(texturePath),
        )
    }
}

object OreModelCard {
    val parentModel = createOreModel()
    val identifier = MirageFairy2024.identifier("block/ore")
    val model = Model(identifier, TextureSlot.BACK, TextureSlot.FRONT)
}

context(ModContext)
fun initOresModule() {

    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("ore")) { OreBlock.CODEC }.register()

    registerModelGeneration({ OreModelCard.identifier }) { OreModelCard.parentModel.with() }

    OreCard.entries.forEach { card ->

        card.block.register()
        card.item.register()

        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        card.block.registerSingletonBlockStateGeneration()
        card.block.registerModelGeneration(card.texturedModelFactory)
        card.block.registerCutoutRenderLayer()

        card.block.enJa(EnJa(card.enName, card.jaName))
        if (card.poemList != null) {
            card.item.registerPoem(card.poemList)
            card.item.registerPoemGeneration(card.poemList)
        }

        card.block.registerOreLootTableGeneration(card.dropItem)

        card.baseStoneType.mineableTag.generator.registerChild(card.block)
        card.baseStoneType.needsToolTag?.generator?.registerChild(card.block)
        ConventionalBlockTags.ORES.generator.registerChild(card.block)
        ConventionalItemTags.ORES.generator.registerChild(card.item)

        card.tags.forEach {
            it.generator.registerChild(card.block)
        }

    }

    /**
     * @param countPerCube
     * 目安:
     * バニラでは上層・下層や露出・埋没によるバリエーションがあり、固定ではない。
     * また、収量としては各バリエーションが重複する。
     * - ORE_LAPIS: 2 / 4
     * - ORE_LAPIS_BURIED: 4 / 8
     * - ORE_EMERALD: 100 / 31
     * バニラのソースコード見た方が早い。
     * @see [net.minecraft.data.worldgen.features.OreFeatures], [net.minecraft.data.worldgen.placement.OrePlacements]
     */
    fun worldGen(
        range: IntRange,
        countPerCube: Double,
        size: Int,
        discardChanceOnAirExposure: Double,
        card: OreCard,
        step: GenerationStep.Decoration,
        suffix: String? = null,
        biomePredicate: BiomeSelectorScope.() -> Predicate<BiomeSelectionContext> = { overworld },
    ) {
        Feature.ORE.generator(card.identifier) {
            registerConfiguredFeature(suffix) {
                val targets = listOf(OreConfiguration.target(card.baseStoneType.target(), card.block().defaultBlockState()))
                OreConfiguration(targets, size, discardChanceOnAirExposure.toFloat())
            }.generator {
                registerPlacedFeature(suffix) { randomIntCount(countPerCube * (range.last - range.first + 1).toDouble() / 16.0) + uniformOre(range.first, range.last) }.registerFeature(step) { biomePredicate() }
            }
        }
    }
    worldGen(16 until 128, 1.6, 12, 0.0, OreCard.MAGNETITE_ORE, GenerationStep.Decoration.UNDERGROUND_ORES)
    worldGen(16 until 128, 1.6, 12, 0.0, OreCard.DEEPSLATE_MAGNETITE_ORE, GenerationStep.Decoration.UNDERGROUND_ORES)
    worldGen(10 until 118, 0.6, 12, 0.0, OreCard.NETHER_MAGNETITE_ORE, GenerationStep.Decoration.UNDERGROUND_DECORATION) { nether }
    worldGen(0 until 64, 1.2, 8, 0.0, OreCard.FLUORITE_ORE, GenerationStep.Decoration.UNDERGROUND_ORES)
    worldGen(0 until 64, 1.2, 8, 0.0, OreCard.DEEPSLATE_FLUORITE_ORE, GenerationStep.Decoration.UNDERGROUND_ORES)
    worldGen(48 until 128, 8.0, 4, 0.0, OreCard.SALTPETER_ORE, GenerationStep.Decoration.UNDERGROUND_ORES) { +ConventionalBiomeTags.IS_DESERT + +ConventionalBiomeTags.IS_SAVANNA }
    worldGen(48 until 128, 8.0, 4, 0.0, OreCard.DEEPSLATE_SALTPETER_ORE, GenerationStep.Decoration.UNDERGROUND_ORES) { +ConventionalBiomeTags.IS_DESERT + +ConventionalBiomeTags.IS_SAVANNA }
    worldGen(48 until 128, 16.0, 4, 0.0, OreCard.SANDSTONE_SALTPETER_ORE, GenerationStep.Decoration.UNDERGROUND_ORES) { +ConventionalBiomeTags.IS_DESERT + +ConventionalBiomeTags.IS_SAVANNA }
    worldGen(-64 until 64, 4.0, 4, 0.0, OreCard.SALTPETER_ORE, GenerationStep.Decoration.UNDERGROUND_ORES, "dripstone_caves") { +Biomes.DRIPSTONE_CAVES }
    worldGen(-64 until 64, 4.0, 4, 0.0, OreCard.DEEPSLATE_SALTPETER_ORE, GenerationStep.Decoration.UNDERGROUND_ORES, "dripstone_caves") { +Biomes.DRIPSTONE_CAVES }
    worldGen(-64 until 64, 8.0, 4, 0.0, OreCard.SANDSTONE_SALTPETER_ORE, GenerationStep.Decoration.UNDERGROUND_ORES, "dripstone_caves") { +Biomes.DRIPSTONE_CAVES }
    worldGen(48 until 128, 0.2, 4, 1.0, OreCard.DIRT_SALTPETER_ORE, GenerationStep.Decoration.UNDERGROUND_ORES)
    worldGen(48 until 128, 2.0, 4, 1.0, OreCard.DIRT_SALTPETER_ORE, GenerationStep.Decoration.UNDERGROUND_ORES, "savanna") { +ConventionalBiomeTags.IS_SAVANNA }
    worldGen(-64 until 0, 2.0, 8, 0.0, OreCard.SULFUR_ORE, GenerationStep.Decoration.UNDERGROUND_ORES)
    worldGen(-64 until 0, 2.0, 8, 0.0, OreCard.DEEPSLATE_SULFUR_ORE, GenerationStep.Decoration.UNDERGROUND_ORES)
    worldGen(10 until 43, 4.0, 8, 0.0, OreCard.NETHER_SULFUR_ORE, GenerationStep.Decoration.UNDERGROUND_DECORATION) { nether }
    worldGen(0 until 128, 4.0, 8, 0.0, OreCard.BLACKSTONE_SULFUR_ORE, GenerationStep.Decoration.UNDERGROUND_DECORATION) { nether }
    worldGen(-64 until 64, 1.0, 4, 1.0, OreCard.NEPHRITE_ORE, GenerationStep.Decoration.UNDERGROUND_ORES)
    worldGen(-64 until 64, 0.3, 4, 1.0, OreCard.DEEPSLATE_NEPHRITE_ORE, GenerationStep.Decoration.UNDERGROUND_ORES)
    worldGen(-64 until 128, 0.6, 12, 0.0, OreCard.MIRANAGITE_ORE, GenerationStep.Decoration.UNDERGROUND_ORES)
    worldGen(-64 until 128, 0.6, 12, 0.0, OreCard.DEEPSLATE_MIRANAGITE_ORE, GenerationStep.Decoration.UNDERGROUND_ORES)
    worldGen(48 until 128, 0.2, 4, 1.0, OreCard.DIRT_FAIRY_PLASTIC_ORE, GenerationStep.Decoration.UNDERGROUND_ORES) { +DeepFairyForestBiomeCard.key }

}

fun createOreModel() = Model {
    ModelData(
        parent = ResourceLocation("minecraft", "block/block"),
        textures = ModelTexturesData(
            TextureSlot.PARTICLE.id to TextureSlot.BACK.string,
        ),
        elements = ModelElementsData(
            ModelElementData(
                from = listOf(0, 0, 0),
                to = listOf(16, 16, 16),
                faces = ModelFacesData(
                    down = ModelFaceData(texture = TextureSlot.BACK.string, cullface = "down"),
                    up = ModelFaceData(texture = TextureSlot.BACK.string, cullface = "up"),
                    north = ModelFaceData(texture = TextureSlot.BACK.string, cullface = "north"),
                    south = ModelFaceData(texture = TextureSlot.BACK.string, cullface = "south"),
                    west = ModelFaceData(texture = TextureSlot.BACK.string, cullface = "west"),
                    east = ModelFaceData(texture = TextureSlot.BACK.string, cullface = "east"),
                ),
            ),
            ModelElementData(
                from = listOf(0, 0, 0),
                to = listOf(16, 16, 16),
                faces = ModelFacesData(
                    down = ModelFaceData(texture = TextureSlot.FRONT.string, cullface = "down"),
                    up = ModelFaceData(texture = TextureSlot.FRONT.string, cullface = "up"),
                    north = ModelFaceData(texture = TextureSlot.FRONT.string, cullface = "north"),
                    south = ModelFaceData(texture = TextureSlot.FRONT.string, cullface = "south"),
                    west = ModelFaceData(texture = TextureSlot.FRONT.string, cullface = "west"),
                    east = ModelFaceData(texture = TextureSlot.FRONT.string, cullface = "east"),
                ),
            ),
        ),
    )
}
