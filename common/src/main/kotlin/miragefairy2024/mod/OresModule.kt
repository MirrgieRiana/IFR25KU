package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.Model
import miragefairy2024.util.ModelData
import miragefairy2024.util.ModelElementData
import miragefairy2024.util.ModelElementsData
import miragefairy2024.util.ModelFaceData
import miragefairy2024.util.ModelFacesData
import miragefairy2024.util.ModelTexturesData
import miragefairy2024.util.Registration
import miragefairy2024.util.enJa
import miragefairy2024.util.generator
import miragefairy2024.util.get
import miragefairy2024.util.overworld
import miragefairy2024.util.placementModifiers
import miragefairy2024.util.randomIntCount
import miragefairy2024.util.register
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.registerFeature
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerOreLootTableGeneration
import miragefairy2024.util.registerSingletonBlockStateGeneration
import miragefairy2024.util.string
import miragefairy2024.util.times
import miragefairy2024.util.uniformOre
import miragefairy2024.util.with
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.data.models.model.TextureSlot
import net.minecraft.data.models.model.TexturedModel
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.util.valueproviders.UniformInt
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.DropExperienceBlock
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest
import net.minecraft.world.level.material.MapColor

enum class BaseStoneType {
    STONE,
    DEEPSLATE,
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
) {
    MAGNETITE_ORE(
        "magnetite_ore", "Magnetite Ore", "磁鉄鉱鉱石",
        null,
        BaseStoneType.STONE, "magnetite_ore", MaterialCard.MAGNETITE.item, 2 to 5,
    ),
    DEEPSLATE_MAGNETITE_ORE(
        "deepslate_magnetite_ore", "Deepslate Magnetite Ore", "深層磁鉄鉱鉱石",
        null,
        BaseStoneType.DEEPSLATE, "magnetite_ore", MaterialCard.MAGNETITE.item, 2 to 5,
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
    MIRANAGITE_ORE(
        "miranagite_ore", "Miranagite Ore", "蒼天石鉱石",
        PoemList(1).poem("What lies beyond a Garden of Eden?", "秩序の石は楽園の先に何を見るのか？"),
        BaseStoneType.STONE, "miranagite_ore", MaterialCard.MIRANAGITE.item, 2 to 5,
    ),
    DEEPSLATE_MIRANAGITE_ORE(
        "deepslate_miranagite_ore", "Deepslate Miranagite Ore", "深層蒼天石鉱石",
        PoemList(1).poem("Singularities built by the Creator", "楽園が楽園であるための奇跡。"),
        BaseStoneType.DEEPSLATE, "miranagite_ore", MaterialCard.MIRANAGITE.item, 2 to 5,
    ),
    ;

    val identifier = MirageFairy2024.identifier(path)
    val block = Registration(BuiltInRegistries.BLOCK, identifier) {
        val settings = when (baseStoneType) {
            BaseStoneType.STONE -> FabricBlockSettings.create()
                .mapColor(MapColor.STONE)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .requiresTool()
                .strength(3.0F, 3.0F)

            BaseStoneType.DEEPSLATE -> FabricBlockSettings.create()
                .mapColor(MapColor.DEEPSLATE)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .requiresTool()
                .strength(4.5F, 3.0F)
                .sound(SoundType.DEEPSLATE)
        }
        DropExperienceBlock(UniformInt.of(experience.first, experience.second), settings)
    }
    val item = Registration(BuiltInRegistries.ITEM, identifier) { BlockItem(block.await(), Item.Properties()) }
    val texturedModelFactory = TexturedModel.Provider {
        val baseStoneTexture = when (baseStoneType) {
            BaseStoneType.STONE -> ResourceLocation.fromNamespaceAndPath("minecraft", "block/stone")
            BaseStoneType.DEEPSLATE -> ResourceLocation.fromNamespaceAndPath("minecraft", "block/deepslate")
        }
        OreModelCard.model.with(
            TextureSlot.BACK to baseStoneTexture,
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

        BlockTags.MINEABLE_WITH_PICKAXE.generator.registerChild(card.block)
        BlockTags.NEEDS_STONE_TOOL.generator.registerChild(card.block)
        ConventionalBlockTags.ORES.generator.registerChild(card.block)

    }

    fun worldGen(range: IntRange, countPerCube: Double, size: Int, card: OreCard) {

        val configuredKey = registerDynamicGeneration(Registries.CONFIGURED_FEATURE, card.identifier) {
            val targets = when (card.baseStoneType) {
                BaseStoneType.STONE -> listOf(OreConfiguration.target(TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), card.block().defaultBlockState()))
                BaseStoneType.DEEPSLATE -> listOf(OreConfiguration.target(TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), card.block().defaultBlockState()))
            }
            Feature.ORE with OreConfiguration(targets, size)
        }

        registerDynamicGeneration(Registries.PLACED_FEATURE, card.identifier) {
            val placementModifiers = placementModifiers { randomIntCount(countPerCube * (range.last - range.first + 1).toDouble() / 16.0) + uniformOre(range.first, range.last) }
            Registries.CONFIGURED_FEATURE[configuredKey] with placementModifiers
        }.also {
            it.registerFeature(GenerationStep.Decoration.UNDERGROUND_ORES) { overworld }
        }

    }
    worldGen(16 until 128, 1.6, 12, OreCard.MAGNETITE_ORE)
    worldGen(16 until 128, 1.6, 12, OreCard.DEEPSLATE_MAGNETITE_ORE)
    worldGen(0 until 64, 1.2, 8, OreCard.FLUORITE_ORE)
    worldGen(0 until 64, 1.2, 8, OreCard.DEEPSLATE_FLUORITE_ORE)
    worldGen(-64 until 128, 0.6, 12, OreCard.MIRANAGITE_ORE)
    worldGen(-64 until 128, 0.6, 12, OreCard.DEEPSLATE_MIRANAGITE_ORE)

}

fun createOreModel() = Model {
    ModelData(
        parent = ResourceLocation.fromNamespaceAndPath("minecraft", "block/block"),
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
