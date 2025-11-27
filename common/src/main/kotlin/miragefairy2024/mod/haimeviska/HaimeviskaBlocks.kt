package miragefairy2024.mod.haimeviska

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.description
import miragefairy2024.mod.haimeviska.cards.DrippingHaimeviskaLogBlock
import miragefairy2024.mod.haimeviska.cards.HaimeviskaBricksBlockCard
import miragefairy2024.mod.haimeviska.cards.HaimeviskaDoorBlockCard
import miragefairy2024.mod.haimeviska.cards.HaimeviskaDrippingLogBlockCard
import miragefairy2024.mod.haimeviska.cards.HaimeviskaHollowLogBlockCard
import miragefairy2024.mod.haimeviska.cards.HaimeviskaIncisedLogBlockCard
import miragefairy2024.mod.haimeviska.cards.HaimeviskaLeavesBlock
import miragefairy2024.mod.haimeviska.cards.HaimeviskaLeavesBlockCard
import miragefairy2024.mod.haimeviska.cards.HaimeviskaLogBlock
import miragefairy2024.mod.haimeviska.cards.HaimeviskaLogBlockCard
import miragefairy2024.mod.haimeviska.cards.HaimeviskaPlanksBlockCard
import miragefairy2024.mod.haimeviska.cards.HaimeviskaPlanksButtonBlockCard
import miragefairy2024.mod.haimeviska.cards.HaimeviskaPlanksFenceBlockCard
import miragefairy2024.mod.haimeviska.cards.HaimeviskaPlanksFenceGateBlockCard
import miragefairy2024.mod.haimeviska.cards.HaimeviskaPlanksPressurePlateBlockCard
import miragefairy2024.mod.haimeviska.cards.HaimeviskaPlanksSlabBlockCard
import miragefairy2024.mod.haimeviska.cards.HaimeviskaPlanksStairsBlockCard
import miragefairy2024.mod.haimeviska.cards.HaimeviskaSaplingBlockCard
import miragefairy2024.mod.haimeviska.cards.HaimeviskaStrippedLogBlockCard
import miragefairy2024.mod.haimeviska.cards.HaimeviskaStrippedWoodBlockCard
import miragefairy2024.mod.haimeviska.cards.HaimeviskaTrapdoorBlockCard
import miragefairy2024.mod.haimeviska.cards.HaimeviskaWoodBlockCard
import miragefairy2024.mod.haimeviska.cards.HollowHaimeviskaLogBlock
import miragefairy2024.mod.haimeviska.cards.IncisedHaimeviskaLogBlock
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.enJa
import miragefairy2024.util.generator
import miragefairy2024.util.register
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.toBlockTag
import miragefairy2024.util.toItemTag
import net.fabricmc.fabric.api.`object`.builder.v1.block.type.BlockSetTypeBuilder
import net.fabricmc.fabric.api.`object`.builder.v1.block.type.WoodTypeBuilder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.properties.BlockSetType
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument
import net.minecraft.world.level.block.state.properties.WoodType

class HaimeviskaBlockConfiguration(
    val path: String,
    val name: EnJa,
    val poemList: PoemList,
)

abstract class HaimeviskaBlockCard(val configuration: HaimeviskaBlockConfiguration) {
    companion object {
        val entries = mutableListOf<HaimeviskaBlockCard>()
        private operator fun HaimeviskaBlockCard.not() = apply { entries += this }

        val LEAVES = !HaimeviskaBlockConfiguration(
            "haimeviska_leaves", EnJa("Haimeviska Leaves", "ハイメヴィスカの葉"),
            PoemList(1).poem(EnJa("All original flowers are consumed by ivy", "妖精になれる花、なれない花。")),
        ).let { HaimeviskaLeavesBlockCard(it) }
        val LOG = !HaimeviskaBlockConfiguration(
            "haimeviska_log", EnJa("Haimeviska Log", "ハイメヴィスカの原木"),
            PoemList(1)
                .poem(EnJa("Symbiosis with parasitic Mirages", "妖精の滲み込んだ樹。"))
                .description(EnJa("Can be incised with a sword", "剣を使って傷を付けられる")),
        ).let { HaimeviskaLogBlockCard(it) }
        val WOOD = !HaimeviskaBlockConfiguration(
            "haimeviska_wood", EnJa("Haimeviska Wood", "ハイメヴィスカの木"),
            PoemList(1).poem(EnJa("Hydraulic communication system", "ウィスプたちの集合知。")),
        ).let { HaimeviskaWoodBlockCard(it) }
        val STRIPPED_LOG = !HaimeviskaBlockConfiguration(
            "stripped_haimeviska_log", EnJa("Stripped Haimeviska Log", "樹皮を剥いだハイメヴィスカの原木"),
            PoemList(1).poem(EnJa("Something lacking the essence", "ぬぐわれたペルソナ。")),
        ).let { HaimeviskaStrippedLogBlockCard(it) }
        val STRIPPED_WOOD = !HaimeviskaBlockConfiguration(
            "stripped_haimeviska_wood", EnJa("Stripped Haimeviska Wood", "樹皮を剥いだハイメヴィスカの木"),
            PoemList(1).poem(EnJa("Loss of self", "寄生蔦からの解放。")),
        ).let { HaimeviskaStrippedWoodBlockCard(it) }
        val INCISED_LOG = !HaimeviskaBlockConfiguration(
            "incised_haimeviska_log", EnJa("Incised Haimeviska Log", "傷の付いたハイメヴィスカの原木"),
            PoemList(1)
                .poem(EnJa("Do fairy trees have qualia of pain?", "動物を守るということ。"))
                .description(EnJa("Produces sap over time", "時間経過で樹液を生産")),
        ).let { HaimeviskaIncisedLogBlockCard(it) }
        val DRIPPING_LOG = !HaimeviskaBlockConfiguration(
            "dripping_haimeviska_log", EnJa("Dripping Haimeviska Log", "滴るハイメヴィスカの原木"),
            PoemList(1)
                .poem(EnJa("A spirit named 'glucose'", "霊界より降りしもの。"))
                .description(EnJa("Harvest sap when used", "使用時、樹液を収穫")),
        ).let { HaimeviskaDrippingLogBlockCard(it) }
        val HOLLOW_LOG = !HaimeviskaBlockConfiguration(
            "hollow_haimeviska_log", EnJa("Hollow Haimeviska Log", "ハイメヴィスカの樹洞"),
            PoemList(1).poem(EnJa("Auric conceptual attractor", "限界巡回アステリア。")),
        ).let { HaimeviskaHollowLogBlockCard(it) }
        val PLANKS = !HaimeviskaBlockConfiguration(
            "haimeviska_planks", EnJa("Haimeviska Planks", "ハイメヴィスカの板材"),
            PoemList(1).poem(EnJa("Flexible and friendly, good for interior", "考える、壁。")),
        ).let { HaimeviskaPlanksBlockCard(it, LOG.item) }
        val SLAB = !HaimeviskaBlockConfiguration(
            "haimeviska_slab", EnJa("Haimeviska Slab", "ハイメヴィスカのハーフブロック"),
            PoemList(1).poem(EnJa("Searching for another personality.", "半人前の側頭葉。")),
        ).let { HaimeviskaPlanksSlabBlockCard(it) { PLANKS.block } }
        val STAIRS = !HaimeviskaBlockConfiguration(
            "haimeviska_stairs", EnJa("Haimeviska Stairs", "ハイメヴィスカの階段"),
            PoemList(1).poem(EnJa("Step that pierces the sky", "情緒体を喰らう頂となれ。")),
        ).let { HaimeviskaPlanksStairsBlockCard(it) { PLANKS.block } }
        val FENCE = !HaimeviskaBlockConfiguration(
            "haimeviska_fence", EnJa("Haimeviska Fence", "ハイメヴィスカのフェンス"),
            PoemList(1).poem(EnJa("Personality flowing through the xylem", "樹のなかに住む。")),
        ).let { HaimeviskaPlanksFenceBlockCard(it, PLANKS.block) }
        val FENCE_GATE = !HaimeviskaBlockConfiguration(
            "haimeviska_fence_gate", EnJa("Haimeviska Fence Gate", "ハイメヴィスカのフェンスゲート"),
            PoemList(1).poem(EnJa("It chose this path of its own will", "知性の邂逅。")),
        ).let { HaimeviskaPlanksFenceGateBlockCard(it, { HAIMEVISKA_WOOD_TYPE }, PLANKS.block) }
        val BUTTON = !HaimeviskaBlockConfiguration(
            "haimeviska_button", EnJa("Haimeviska Button", "ハイメヴィスカのボタン"),
            PoemList(1).poem(EnJa("What is this soft and warm thing?", "指先の感触。")),
        ).let { HaimeviskaPlanksButtonBlockCard(it, { HAIMEVISKA_BLOCK_SET_TYPE }, PLANKS.block) }
        val PRESSURE_PLATE = !HaimeviskaBlockConfiguration(
            "haimeviska_pressure_plate", EnJa("Haimeviska Pressure Plate", "ハイメヴィスカの感圧板"),
            PoemList(1).poem(EnJa("Creature with the name of a machine", "反応と感覚の違い。")),
        ).let { HaimeviskaPlanksPressurePlateBlockCard(it, { HAIMEVISKA_BLOCK_SET_TYPE }, PLANKS.block) }
        val DOOR = !HaimeviskaBlockConfiguration(
            "haimeviska_door", EnJa("Haimeviska Door", "ハイメヴィスカのドア"),
            PoemList(1).poem(EnJa("Astral read-only vortex", "遺伝子の水平伝播。")),
        ).let { HaimeviskaDoorBlockCard(it, { HAIMEVISKA_BLOCK_SET_TYPE }, PLANKS.block) }
        val TRAPDOOR = !HaimeviskaBlockConfiguration(
            "haimeviska_trapdoor", EnJa("Haimeviska Trapdoor", "ハイメヴィスカのトラップドア"),
            PoemList(1).poem(EnJa("Intermingling astral vortices", "自己認識の防衛線。")),
        ).let { HaimeviskaTrapdoorBlockCard(it, { HAIMEVISKA_BLOCK_SET_TYPE }, PLANKS.block) }
        val BRICKS = !HaimeviskaBlockConfiguration(
            "haimeviska_bricks", EnJa("Haimeviska Bricks", "ハイメヴィスカレンガ"),
            PoemList(1).poem(EnJa("An ecosystem called 'civilization'", "人がもたらした原生林。")),
        ).let { HaimeviskaBricksBlockCard(it, SLAB.item) }
        val BRICKS_SLAB = !HaimeviskaBlockConfiguration(
            "haimeviska_bricks_slab", EnJa("Haimeviska Brick Slab", "ハイメヴィスカレンガのハーフブロック"),
            PoemList(1).poem(EnJa("Extremely modularized memory", "ひとまわり細かくなった私。")),
        ).let { HaimeviskaPlanksSlabBlockCard(it) { BRICKS.block } }
        val BRICKS_STAIRS = !HaimeviskaBlockConfiguration(
            "haimeviska_bricks_stairs", EnJa("Haimeviska Brick Stairs", "ハイメヴィスカレンガの階段"),
            PoemList(1).poem(EnJa("Forgotten paths of the technology", "生体工学の歩み。")),
        ).let { HaimeviskaPlanksStairsBlockCard(it) { BRICKS.block } }
        val SAPLING = !HaimeviskaBlockConfiguration(
            "haimeviska_sapling", EnJa("Haimeviska Sapling", "ハイメヴィスカの苗木"),
            PoemList(1).poem(EnJa("Assembling molecules with Ergs", "第二の葉緑体。")),
        ).let { HaimeviskaSaplingBlockCard(it, MirageFairy2024.identifier("haimeviska")) }
    }

    val identifier = MirageFairy2024.identifier(configuration.path)
    open fun createSettings(): BlockBehaviour.Properties = BlockBehaviour.Properties.of()
    abstract suspend fun createBlock(properties: BlockBehaviour.Properties): Block
    val block = Registration(BuiltInRegistries.BLOCK, identifier) { createBlock(createSettings()) }
    open suspend fun createItem(block: Block, properties: Item.Properties) = BlockItem(block, properties)
    val item = Registration(BuiltInRegistries.ITEM, identifier) { createItem(block.await(), Item.Properties()) }

    context(ModContext)
    open fun init() {

        // 登録
        block.register()
        item.register()

        // カテゴリ
        item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        // テキスト
        block.enJa(configuration.name)
        item.registerPoem(configuration.poemList)
        item.registerPoemGeneration(configuration.poemList)

    }
}

fun createBaseWoodSetting(sound: Boolean = true): BlockBehaviour.Properties = BlockBehaviour.Properties.of()
    .instrument(NoteBlockInstrument.BASS)
    .let { if (sound) it.sound(SoundType.WOOD) else it }
    .ignitedByLava()


lateinit var HAIMEVISKA_BLOCK_SET_TYPE: BlockSetType
lateinit var HAIMEVISKA_WOOD_TYPE: WoodType

val HAIMEVISKA_LOGS_BLOCK_TAG = MirageFairy2024.identifier("haimeviska_logs").toBlockTag()
val HAIMEVISKA_LOGS_ITEM_TAG = MirageFairy2024.identifier("haimeviska_logs").toItemTag()

context(ModContext)
fun initHaimeviskaBlocks() {

    HaimeviskaBlockCard.entries.forEach { card ->
        card.init()
    }

    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("haimeviska_leaves")) { HaimeviskaLeavesBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("haimeviska_log")) { HaimeviskaLogBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("incised_haimeviska_log")) { IncisedHaimeviskaLogBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("dripping_haimeviska_log")) { DrippingHaimeviskaLogBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("hollow_haimeviska_log")) { HollowHaimeviskaLogBlock.CODEC }.register()

    // Wood Type
    HAIMEVISKA_BLOCK_SET_TYPE = BlockSetTypeBuilder().register(MirageFairy2024.identifier("haimeviska"))
    HAIMEVISKA_WOOD_TYPE = WoodTypeBuilder().register(MirageFairy2024.identifier("haimeviska"), HAIMEVISKA_BLOCK_SET_TYPE)

    // タグ
    HAIMEVISKA_LOGS_BLOCK_TAG.enJa(EnJa("Haimeviska Logs", "ハイメヴィスカの原木"))
    HAIMEVISKA_LOGS_ITEM_TAG.enJa(EnJa("Haimeviska Logs", "ハイメヴィスカの原木"))
    BlockTags.LOGS_THAT_BURN.generator.registerChild(HAIMEVISKA_LOGS_BLOCK_TAG)
    ItemTags.LOGS_THAT_BURN.generator.registerChild(HAIMEVISKA_LOGS_ITEM_TAG)

}
