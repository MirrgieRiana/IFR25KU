package miragefairy2024.mod.tree

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.common.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.description
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.mod.tree.contents.DrippingHaimeviskaLogBlock
import miragefairy2024.mod.tree.contents.HaimeviskaLeavesBlock
import miragefairy2024.mod.tree.contents.HaimeviskaLogBlock
import miragefairy2024.mod.tree.contents.HollowHaimeviskaLogBlock
import miragefairy2024.mod.tree.contents.IncisedHaimeviskaLogBlock
import miragefairy2024.mod.tree.contents.blockcards.TreeBricksBlockCard
import miragefairy2024.mod.tree.contents.blockcards.TreeChargeableLeavesBlockCard
import miragefairy2024.mod.tree.contents.blockcards.TreeDoorBlockCard
import miragefairy2024.mod.tree.contents.blockcards.TreeDrippingLogBlockCard
import miragefairy2024.mod.tree.contents.blockcards.TreeHollowLogBlockCard
import miragefairy2024.mod.tree.contents.blockcards.TreeIncisableLogBlockCard
import miragefairy2024.mod.tree.contents.blockcards.TreeIncisedLogBlockCard
import miragefairy2024.mod.tree.contents.blockcards.TreeLogBlockCard
import miragefairy2024.mod.tree.contents.blockcards.TreePlanksBlockCard
import miragefairy2024.mod.tree.contents.blockcards.TreePlanksButtonBlockCard
import miragefairy2024.mod.tree.contents.blockcards.TreePlanksFenceBlockCard
import miragefairy2024.mod.tree.contents.blockcards.TreePlanksFenceGateBlockCard
import miragefairy2024.mod.tree.contents.blockcards.TreePlanksPressurePlateBlockCard
import miragefairy2024.mod.tree.contents.blockcards.TreePlanksSlabBlockCard
import miragefairy2024.mod.tree.contents.blockcards.TreePlanksStairsBlockCard
import miragefairy2024.mod.tree.contents.blockcards.TreeSaplingBlockCard
import miragefairy2024.mod.tree.contents.blockcards.TreeStrippedLogBlockCard
import miragefairy2024.mod.tree.contents.blockcards.TreeStrippedWoodBlockCard
import miragefairy2024.mod.tree.contents.blockcards.TreeTrapdoorBlockCard
import miragefairy2024.mod.tree.contents.blockcards.TreeWoodBlockCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.ResourceLocation
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
import net.minecraft.tags.TagKey
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.properties.BlockSetType
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument
import net.minecraft.world.level.block.state.properties.WoodType
import net.minecraft.world.level.material.MapColor

class TreeBlockConfiguration(
    val path: String,
    val name: EnJa,
    val poemList: PoemList,
) {
    val blockTags = mutableListOf<TagKey<Block>>()
    val itemTags = mutableListOf<TagKey<Item>>()
}

@JvmName("blockTag")
fun TreeBlockConfiguration.tag(tag: TagKey<Block>) = this.also { it.blockTags += tag }

@JvmName("itemTag")
fun TreeBlockConfiguration.tag(tag: TagKey<Item>) = this.also { it.itemTags += tag }

@JvmName("blockAndItemTag")
fun TreeBlockConfiguration.tag(blockTag: TagKey<Block>, itemTag: TagKey<Item>) = this.tag(blockTag).tag(itemTag)

abstract class TreeBlockCard(val configuration: TreeBlockConfiguration) {
    companion object {
        val entries = mutableListOf<TreeBlockCard>()
        private operator fun TreeBlockCard.not() = apply { entries += this }

        val LEAVES = !TreeBlockConfiguration(
            "haimeviska_leaves", EnJa("Haimeviska Leaves", "ハイメヴィスカの葉"),
            PoemList(1).poem(EnJa("All original flowers are consumed by ivy", "妖精になれる花、なれない花。")),
        ).tag(BlockTags.LEAVES, ItemTags.LEAVES).tag(BlockTags.MINEABLE_WITH_HOE).let { TreeChargeableLeavesBlockCard(it) }
        val LOG = !TreeBlockConfiguration(
            "haimeviska_log", EnJa("Haimeviska Log", "ハイメヴィスカの原木"),
            PoemList(1)
                .poem(EnJa("Symbiosis with parasitic Mirages", "妖精の滲み込んだ樹。"))
                .description(EnJa("Can be incised with a sword", "剣を使って傷を付けられる")),
        ).tag(HAIMEVISKA_LOGS_BLOCK_TAG, HAIMEVISKA_LOGS_ITEM_TAG).tag(BlockTags.OVERWORLD_NATURAL_LOGS).let { TreeIncisableLogBlockCard(it, MapColor.RAW_IRON, MapColor.TERRACOTTA_ORANGE) }
        val WOOD = !TreeBlockConfiguration(
            "haimeviska_wood", EnJa("Haimeviska Wood", "ハイメヴィスカの木"),
            PoemList(1).poem(EnJa("Hydraulic communication system", "ウィスプたちの集合知。")),
        ).tag(HAIMEVISKA_LOGS_BLOCK_TAG, HAIMEVISKA_LOGS_ITEM_TAG).let { TreeWoodBlockCard(it) }
        val STRIPPED_LOG = !TreeBlockConfiguration(
            "stripped_haimeviska_log", EnJa("Stripped Haimeviska Log", "樹皮を剥いだハイメヴィスカの原木"),
            PoemList(1).poem(EnJa("Something lacking the essence", "ぬぐわれたペルソナ。")),
        ).tag(HAIMEVISKA_LOGS_BLOCK_TAG, HAIMEVISKA_LOGS_ITEM_TAG).tag(ResourceLocation("c", "stripped_logs").toBlockTag(), ResourceLocation("c", "stripped_logs").toItemTag()).let { TreeStrippedLogBlockCard(it) }
        val STRIPPED_WOOD = !TreeBlockConfiguration(
            "stripped_haimeviska_wood", EnJa("Stripped Haimeviska Wood", "樹皮を剥いだハイメヴィスカの木"),
            PoemList(1).poem(EnJa("Loss of self", "寄生蔦からの解放。")),
        ).tag(HAIMEVISKA_LOGS_BLOCK_TAG, HAIMEVISKA_LOGS_ITEM_TAG).tag(ResourceLocation("c", "stripped_woods").toBlockTag(), ResourceLocation("c", "stripped_woods").toItemTag()).let { TreeStrippedWoodBlockCard(it) }
        val INCISED_LOG = !TreeBlockConfiguration(
            "incised_haimeviska_log", EnJa("Incised Haimeviska Log", "傷の付いたハイメヴィスカの原木"),
            PoemList(1)
                .poem(EnJa("Do fairy trees have qualia of pain?", "動物を守るということ。"))
                .description(EnJa("Produces sap over time", "時間経過で樹液を生産")),
        ).tag(HAIMEVISKA_LOGS_BLOCK_TAG, HAIMEVISKA_LOGS_ITEM_TAG).tag(BlockTags.OVERWORLD_NATURAL_LOGS).let { TreeIncisedLogBlockCard(it) }
        val DRIPPING_LOG = !TreeBlockConfiguration(
            "dripping_haimeviska_log", EnJa("Dripping Haimeviska Log", "滴るハイメヴィスカの原木"),
            PoemList(1)
                .poem(EnJa("A spirit named 'glucose'", "霊界より降りしもの。"))
                .description(EnJa("Harvest sap when used", "使用時、樹液を収穫")),
        ).tag(HAIMEVISKA_LOGS_BLOCK_TAG, HAIMEVISKA_LOGS_ITEM_TAG).tag(BlockTags.OVERWORLD_NATURAL_LOGS).let { TreeDrippingLogBlockCard(it) }
        val HOLLOW_LOG = !TreeBlockConfiguration(
            "hollow_haimeviska_log", EnJa("Hollow Haimeviska Log", "ハイメヴィスカの樹洞"),
            PoemList(1).poem(EnJa("Auric conceptual attractor", "限界巡回アステリア。")),
        ).tag(HAIMEVISKA_LOGS_BLOCK_TAG, HAIMEVISKA_LOGS_ITEM_TAG).tag(BlockTags.OVERWORLD_NATURAL_LOGS).let { TreeHollowLogBlockCard(it) }
        val PLANKS = !TreeBlockConfiguration(
            "haimeviska_planks", EnJa("Haimeviska Planks", "ハイメヴィスカの板材"),
            PoemList(1).poem(EnJa("Flexible and friendly, good for interior", "考える、壁。")),
        ).tag(BlockTags.PLANKS, ItemTags.PLANKS).let { TreePlanksBlockCard(it, LOG.item) }
        val SLAB = !TreeBlockConfiguration(
            "haimeviska_slab", EnJa("Haimeviska Slab", "ハイメヴィスカのハーフブロック"),
            PoemList(1).poem(EnJa("Searching for another personality.", "半人前の側頭葉。")),
        ).tag(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS).let { TreePlanksSlabBlockCard(it) { PLANKS.block } }
        val STAIRS = !TreeBlockConfiguration(
            "haimeviska_stairs", EnJa("Haimeviska Stairs", "ハイメヴィスカの階段"),
            PoemList(1).poem(EnJa("Step that pierces the sky", "情緒体を喰らう頂となれ。")),
        ).tag(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS).let { TreePlanksStairsBlockCard(it) { PLANKS.block } }
        val FENCE = !TreeBlockConfiguration(
            "haimeviska_fence", EnJa("Haimeviska Fence", "ハイメヴィスカのフェンス"),
            PoemList(1).poem(EnJa("Personality flowing through the xylem", "樹のなかに住む。")),
        ).tag(BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES).let { TreePlanksFenceBlockCard(it, PLANKS.block) }
        val FENCE_GATE = !TreeBlockConfiguration(
            "haimeviska_fence_gate", EnJa("Haimeviska Fence Gate", "ハイメヴィスカのフェンスゲート"),
            PoemList(1).poem(EnJa("It chose this path of its own will", "知性の邂逅。")),
        ).tag(BlockTags.FENCE_GATES, ItemTags.FENCE_GATES).tag(ResourceLocation("c", "fence_gates/wooden").toBlockTag(), ResourceLocation("c", "fence_gates/wooden").toItemTag()).let { TreePlanksFenceGateBlockCard(it, { HAIMEVISKA_WOOD_TYPE }, PLANKS.block) }
        val BUTTON = !TreeBlockConfiguration(
            "haimeviska_button", EnJa("Haimeviska Button", "ハイメヴィスカのボタン"),
            PoemList(1).poem(EnJa("What is this soft and warm thing?", "指先の感触。")),
        ).tag(BlockTags.WOODEN_BUTTONS, ItemTags.WOODEN_BUTTONS).let { TreePlanksButtonBlockCard(it, { HAIMEVISKA_BLOCK_SET_TYPE }, PLANKS.block) }
        val PRESSURE_PLATE = !TreeBlockConfiguration(
            "haimeviska_pressure_plate", EnJa("Haimeviska Pressure Plate", "ハイメヴィスカの感圧板"),
            PoemList(1).poem(EnJa("Creature with the name of a machine", "反応と感覚の違い。")),
        ).tag(BlockTags.WOODEN_PRESSURE_PLATES, ItemTags.WOODEN_PRESSURE_PLATES).let { TreePlanksPressurePlateBlockCard(it, { HAIMEVISKA_BLOCK_SET_TYPE }, PLANKS.block) }
        val DOOR = !TreeBlockConfiguration(
            "haimeviska_door", EnJa("Haimeviska Door", "ハイメヴィスカのドア"),
            PoemList(1).poem(EnJa("Astral read-only vortex", "遺伝子の水平伝播。")),
        ).tag(BlockTags.WOODEN_DOORS, ItemTags.WOODEN_DOORS).let { TreeDoorBlockCard(it, { HAIMEVISKA_BLOCK_SET_TYPE }, PLANKS.block) }
        val TRAPDOOR = !TreeBlockConfiguration(
            "haimeviska_trapdoor", EnJa("Haimeviska Trapdoor", "ハイメヴィスカのトラップドア"),
            PoemList(1).poem(EnJa("Intermingling astral vortices", "自己認識の防衛線。")),
        ).tag(BlockTags.WOODEN_TRAPDOORS, ItemTags.WOODEN_TRAPDOORS).let { TreeTrapdoorBlockCard(it, { HAIMEVISKA_BLOCK_SET_TYPE }, PLANKS.block) }
        val BRICKS = !TreeBlockConfiguration(
            "haimeviska_bricks", EnJa("Haimeviska Bricks", "ハイメヴィスカレンガ"),
            PoemList(1).poem(EnJa("An ecosystem called 'civilization'", "人がもたらした原生林。")),
        ).tag(BlockTags.PLANKS, ItemTags.PLANKS).let { TreeBricksBlockCard(it, SLAB.item) }
        val BRICKS_SLAB = !TreeBlockConfiguration(
            "haimeviska_bricks_slab", EnJa("Haimeviska Brick Slab", "ハイメヴィスカレンガのハーフブロック"),
            PoemList(1).poem(EnJa("Extremely modularized memory", "ひとまわり細かくなった私。")),
        ).tag(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS).let { TreePlanksSlabBlockCard(it) { BRICKS.block } }
        val BRICKS_STAIRS = !TreeBlockConfiguration(
            "haimeviska_bricks_stairs", EnJa("Haimeviska Brick Stairs", "ハイメヴィスカレンガの階段"),
            PoemList(1).poem(EnJa("Forgotten paths of the technology", "生体工学の歩み。")),
        ).tag(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS).let { TreePlanksStairsBlockCard(it) { BRICKS.block } }
        val SAPLING = !TreeBlockConfiguration(
            "haimeviska_sapling", EnJa("Haimeviska Sapling", "ハイメヴィスカの苗木"),
            PoemList(1).poem(EnJa("Assembling molecules with Ergs", "第二の葉緑体。")),
        ).tag(BlockTags.SAPLINGS, ItemTags.SAPLINGS).let { TreeSaplingBlockCard(it, MirageFairy2024.identifier("haimeviska")) }

        val PLASTIC_TREE_LOG = !TreeBlockConfiguration(
            "plastic_tree_log", EnJa("Plastic Tree Log", "プラノキの原木"),
            PoemList(1)
                .poem(EnJa("TODO", "TODO"))
                .description(EnJa("Can be incised with a sword", "剣を使って傷を付けられる")),
        ).tag(PLASTIC_TREE_LOGS_BLOCK_TAG, PLASTIC_TREE_LOGS_ITEM_TAG).tag(BlockTags.OVERWORLD_NATURAL_LOGS).let { TreeIncisableLogBlockCard(it, MapColor.SAND, MapColor.COLOR_YELLOW) }
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

        // タグ
        configuration.blockTags.forEach { tag ->
            tag.generator.registerChild(block)
        }
        configuration.itemTags.forEach { tag ->
            tag.generator.registerChild(item)
        }

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

val PLASTIC_TREE_LOGS_BLOCK_TAG = MirageFairy2024.identifier("plastic_tree_logs").toBlockTag()
val PLASTIC_TREE_LOGS_ITEM_TAG = MirageFairy2024.identifier("plastic_tree_logs").toItemTag()

context(ModContext)
fun initTreeBlocks() {

    TreeBlockCard.entries.forEach { card ->
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
    PLASTIC_TREE_LOGS_BLOCK_TAG.enJa(EnJa("Plastic Tree Logs", "プラノキの原木"))
    PLASTIC_TREE_LOGS_ITEM_TAG.enJa(EnJa("Plastic Tree Logs", "プラノキの原木"))
    BlockTags.LOGS_THAT_BURN.generator.registerChild(PLASTIC_TREE_LOGS_BLOCK_TAG)
    ItemTags.LOGS_THAT_BURN.generator.registerChild(PLASTIC_TREE_LOGS_ITEM_TAG)

}
