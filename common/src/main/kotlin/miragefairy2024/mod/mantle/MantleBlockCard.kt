package miragefairy2024.mod.mantle

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.common.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.enJa
import miragefairy2024.util.generator
import miragefairy2024.util.register
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerOreLootTableGeneration
import miragefairy2024.util.registerSingletonBlockStateGeneration
import miragefairy2024.util.registerBlockFamily
import miragefairy2024.util.registerLootTableGeneration
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.models.model.TexturedModel
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.util.valueproviders.UniformInt
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument
import net.minecraft.world.level.material.MapColor

/** 焼き石の硬度なのだ～🌱 */
private const val STONE_HARDNESS = 1.5F

/** テラコッタの硬度なのだ～🌱 */
private const val TERRACOTTA_HARDNESS = 1.25F

/** マントルディメンションのブロックの硬度が、元にした岩石の何倍かなのだ～🌱 */
private const val MANTLE_HARDNESS_FACTOR = 100

enum class MantleBlockCard(
    path: String,
    val enName: String,
    val jaName: String,
    val poemList: PoemList?,
    val needsToolTag: TagKey<Block>,
    val hardness: Float,
    val resistance: Float,
    val mapColor: MapColor,
    val dropItem: (() -> Item)? = null,
    val blockCreator: suspend (BlockBehaviour.Properties) -> Block = ::MantleBlock,
    val shape: Shape = Shape.CUBE,
    val parentCardGetter: (() -> MantleBlockCard)? = null,
) {
    BRIDGMANITE(
        "bridgmanite", "Bridgmanite", "ブリッジマナイト",
        PoemList(5).poem(EnJa("Stable only under the weight of a world", "知られることなく、世界を満たす。")),
        NEEDS_TIER4_TOOL_BLOCK_TAG, STONE_HARDNESS * MANTLE_HARDNESS_FACTOR, 6.0F, MapColor.COLOR_BROWN,
    ),
    FERROPERICLASE(
        "ferropericlase", "Ferropericlase", "フェロペリクレース",
        null,
        NEEDS_TIER4_TOOL_BLOCK_TAG, STONE_HARDNESS * MANTLE_HARDNESS_FACTOR, 6.0F, MapColor.TERRACOTTA_RED,
    ),
    MAJORITE(
        "majorite", "Majorite", "マジョライト",
        null,
        NEEDS_TIER4_TOOL_BLOCK_TAG, STONE_HARDNESS * MANTLE_HARDNESS_FACTOR, 6.0F, MapColor.COLOR_PURPLE,
    ),
    AKIMOTOITE(
        "akimotoite", "Akimotoite", "アキモトアイト",
        null,
        NEEDS_TIER4_TOOL_BLOCK_TAG, STONE_HARDNESS * MANTLE_HARDNESS_FACTOR, 6.0F, MapColor.COLOR_BLACK,
    ),
    POST_PEROVSKITE(
        "post_perovskite", "Post-Perovskite", "ポストペロブスカイト",
        null,
        NEEDS_TIER4_TOOL_BLOCK_TAG, STONE_HARDNESS * MANTLE_HARDNESS_FACTOR, 6.0F, MapColor.COLOR_BLUE,
    ),
    WADSLEYITE_ORE(
        "wadsleyite_ore", "Wadsleyite Ore", "ワズレアイト鉱石",
        null,
        NEEDS_TIER4_TOOL_BLOCK_TAG, STONE_HARDNESS * MANTLE_HARDNESS_FACTOR, 6.0F, MapColor.COLOR_BROWN,
        dropItem = { MantleMaterialCard.WADSLEYITE.item() }, blockCreator = { MantleOreBlock(UniformInt.of(5, 12), it) },
    ),
    RINGWOODITE_ORE(
        "ringwoodite_ore", "Ringwoodite Ore", "リングウッダイト鉱石",
        PoemList(6).poem(EnJa("An ocean sealed inside a crystal.", "石の中に海が眠っている。")),
        NEEDS_TIER5_TOOL_BLOCK_TAG, STONE_HARDNESS * MANTLE_HARDNESS_FACTOR, 6.0F, MapColor.COLOR_BLUE,
        dropItem = { MantleMaterialCard.RINGWOODITE.item() }, blockCreator = { MantleOreBlock(UniformInt.of(10, 24), it) },
    ),
    REINFORCED_METAL_BLOCK(
        "reinforced_metal_block", "Reinforced Metal Block", "強化金属ブロック",
        PoemList(5).poem(EnJa("Thermodynamically inert by design.", "大戦より古い、沈黙の合金。")),
        NEEDS_TIER5_TOOL_BLOCK_TAG, TERRACOTTA_HARDNESS * MANTLE_HARDNESS_FACTOR, 4.2F, MapColor.METAL,
    ),
    REINFORCED_METAL_SLAB(
        "reinforced_metal_slab", "Reinforced Metal Slab", "強化金属のハーフブロック",
        PoemList(5).poem(EnJa("Mantle pressure is no match.", "圧力に屈しない、最後の盾。")),
        NEEDS_TIER5_TOOL_BLOCK_TAG, TERRACOTTA_HARDNESS * MANTLE_HARDNESS_FACTOR, 4.2F, MapColor.METAL,
        blockCreator = { MantleSlabBlock(it) }, shape = Shape.SLAB, parentCardGetter = { REINFORCED_METAL_BLOCK },
    ),
    REINFORCED_METAL_STAIRS(
        "reinforced_metal_stairs", "Reinforced Metal Stairs", "強化金属の階段",
        PoemList(5).poem(EnJa("A sanctuary sealed under geologic time.", "地の底で、時間を拒んだ壁。")),
        NEEDS_TIER5_TOOL_BLOCK_TAG, TERRACOTTA_HARDNESS * MANTLE_HARDNESS_FACTOR, 4.2F, MapColor.METAL,
        blockCreator = { MantleStairBlock(REINFORCED_METAL_BLOCK.block.await().defaultBlockState(), it) }, shape = Shape.STAIRS, parentCardGetter = { REINFORCED_METAL_BLOCK },
    ),
    REINFORCED_METAL_TILES(
        "reinforced_metal_tiles", "Reinforced Metal Tiles", "強化金属タイル",
        PoemList(5).poem(EnJa("Ordered not to yield.", "崩壊を拒んだ、意志の金属。")),
        NEEDS_TIER5_TOOL_BLOCK_TAG, TERRACOTTA_HARDNESS * MANTLE_HARDNESS_FACTOR, 4.2F, MapColor.METAL,
    ),
    REINFORCED_METAL_TILE_SLAB(
        "reinforced_metal_tile_slab", "Reinforced Metal Tile Slab", "強化金属タイルのハーフブロック",
        PoemList(5).poem(EnJa("Immovable under geologic pressure.", "億年の圧が刻んだ誓い。")),
        NEEDS_TIER5_TOOL_BLOCK_TAG, TERRACOTTA_HARDNESS * MANTLE_HARDNESS_FACTOR, 4.2F, MapColor.METAL,
        blockCreator = { MantleSlabBlock(it) }, shape = Shape.SLAB, parentCardGetter = { REINFORCED_METAL_TILES },
    ),
    REINFORCED_METAL_TILE_STAIRS(
        "reinforced_metal_tile_stairs", "Reinforced Metal Tile Stairs", "強化金属タイルの階段",
        PoemList(5).poem(EnJa("The wall of a civilization deep below.", "忘れられた砦は、まだ立っている。")),
        NEEDS_TIER5_TOOL_BLOCK_TAG, TERRACOTTA_HARDNESS * MANTLE_HARDNESS_FACTOR, 4.2F, MapColor.METAL,
        blockCreator = { MantleStairBlock(REINFORCED_METAL_TILES.block.await().defaultBlockState(), it) }, shape = Shape.STAIRS, parentCardGetter = { REINFORCED_METAL_TILES },
    ),
    CHISELED_REINFORCED_METAL(
        "chiseled_reinforced_metal", "Chiseled Reinforced Metal", "模様入りの強化金属",
        PoemList(5).poem(EnJa("Relics of the Institute, still warm.", "誰も溶かせなかった研究所の夢。")),
        NEEDS_TIER5_TOOL_BLOCK_TAG, TERRACOTTA_HARDNESS * MANTLE_HARDNESS_FACTOR, 4.2F, MapColor.METAL,
    ),
    INSTITUTE_FLOOR(
        "institute_floor", "Institute Floor", "妖精研究所の床",
        PoemList(5).poem(EnJa("Nobody has walked here for an aeon.", "誰も踏まなくなった、廊下の続き。")),
        NEEDS_TIER5_TOOL_BLOCK_TAG, TERRACOTTA_HARDNESS * MANTLE_HARDNESS_FACTOR, 4.2F, MapColor.COLOR_LIGHT_GRAY,
    ),
    INSTITUTE_FLOOR_SLAB(
        "institute_floor_slab", "Institute Floor Slab", "妖精研究所の床のハーフブロック",
        PoemList(5).poem(EnJa("Half a step, preserved for an aeon.", "半歩分の、途切れた足音。")),
        NEEDS_TIER5_TOOL_BLOCK_TAG, TERRACOTTA_HARDNESS * MANTLE_HARDNESS_FACTOR, 4.2F, MapColor.COLOR_LIGHT_GRAY,
        blockCreator = { MantleSlabBlock(it) }, shape = Shape.SLAB, parentCardGetter = { INSTITUTE_FLOOR },
    ),
    INSTITUTE_FLOOR_STAIRS(
        "institute_floor_stairs", "Institute Floor Stairs", "妖精研究所の床の階段",
        PoemList(5).poem(EnJa("The stairs still lead somewhere.", "まだ、どこかへ続いている。")),
        NEEDS_TIER5_TOOL_BLOCK_TAG, TERRACOTTA_HARDNESS * MANTLE_HARDNESS_FACTOR, 4.2F, MapColor.COLOR_LIGHT_GRAY,
        blockCreator = { MantleStairBlock(INSTITUTE_FLOOR.block.await().defaultBlockState(), it) }, shape = Shape.STAIRS, parentCardGetter = { INSTITUTE_FLOOR },
    ),
    FAIRY_QUEST_GATE(
        "fairy_quest_gate", "Fairy Quest Gate", "フェアリークエストゲート",
        PoemList(5).poem(EnJa("Buried junction of parallel dimensions.", "地の果てへの扉の縁。")),
        NEEDS_TIER5_TOOL_BLOCK_TAG, TERRACOTTA_HARDNESS * MANTLE_HARDNESS_FACTOR, 4.2F, MapColor.COLOR_BLACK,
        blockCreator = { FairyQuestGateFrameBlock(it) },
    ),
    FAIRY_QUEST_GATE_PORTAL(
        "fairy_quest_gate_portal", "Fairy Quest Gate Portal", "フェアリークエストゲートのポータル",
        PoemList(5).poem(EnJa("A window into the molten interior.", "地核へと続く瞳。")),
        NEEDS_TIER5_TOOL_BLOCK_TAG, TERRACOTTA_HARDNESS * MANTLE_HARDNESS_FACTOR, 4.2F, MapColor.COLOR_PURPLE,
        blockCreator = { FairyQuestGatePortalBlock(it.noOcclusion().lightLevel { 11 }) },
    ),
    ;

    val identifier = MirageFairy2024.identifier(path)

    val block = Registration(BuiltInRegistries.BLOCK, identifier) {
        val properties = BlockBehaviour.Properties.of()
            .mapColor(mapColor)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(hardness, resistance)
            .sound(SoundType.DEEPSLATE)
        blockCreator(properties)
    }

    val item = Registration(BuiltInRegistries.ITEM, identifier) { BlockItem(block.await(), Item.Properties()) }

    val texturedModelProvider = TexturedModel.Provider { TexturedModel.CUBE.get(it) }

    /** ブロックの形なのだ～🌱 モデルと戦利品の作り方が、これによって変わるのだ～🌱 */
    enum class Shape { CUBE, SLAB, STAIRS }
}

context(ModContext)
fun initMantleBlockCards() {
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("fairy_quest_gate")) { FairyQuestGateFrameBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("fairy_quest_gate_portal")) { FairyQuestGatePortalBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("mantle_block")) { MantleBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("mantle_ore")) { MantleOreBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("mantle_slab")) { MantleSlabBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("mantle_stair")) { MantleStairBlock.CODEC }.register()

    MantleBlockCard.entries.forEach { card ->

        card.block.register()
        card.item.register()

        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        when (card.shape) {
            MantleBlockCard.Shape.CUBE -> {
                card.block.registerSingletonBlockStateGeneration()
                card.block.registerModelGeneration(TexturedModel.CUBE)
            }

            MantleBlockCard.Shape.SLAB -> {
                card.parentCardGetter!!().let { parent -> registerBlockFamily(parent.texturedModelProvider, parent.block) { it.slab(card.block()) } }
            }

            MantleBlockCard.Shape.STAIRS -> {
                card.parentCardGetter!!().let { parent -> registerBlockFamily(parent.texturedModelProvider, parent.block) { it.stairs(card.block()) } }
            }
        }

        card.block.enJa(EnJa(card.enName, card.jaName))
        if (card.poemList != null) {
            card.item.registerPoem(card.poemList)
            card.item.registerPoemGeneration(card.poemList)
        }

        val dropItem = card.dropItem
        when {
            dropItem != null -> card.block.registerOreLootTableGeneration(dropItem)
            card.shape == MantleBlockCard.Shape.SLAB -> card.block.registerLootTableGeneration { it, _ -> it.createSlabItemTable(card.block()) }
            else -> card.block.registerDefaultLootTableGeneration()
        }

        when (card.shape) {
            MantleBlockCard.Shape.SLAB -> {
                BlockTags.SLABS.generator.registerChild(card.block)
                ItemTags.SLABS.generator.registerChild(card.item)
            }

            MantleBlockCard.Shape.STAIRS -> {
                BlockTags.STAIRS.generator.registerChild(card.block)
                ItemTags.STAIRS.generator.registerChild(card.item)
            }

            else -> Unit
        }

        BlockTags.MINEABLE_WITH_PICKAXE.generator.registerChild(card.block)
        card.needsToolTag.generator.registerChild(card.block)

    }
}
