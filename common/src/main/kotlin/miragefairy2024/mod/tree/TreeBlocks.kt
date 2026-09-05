package miragefairy2024.mod.tree

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.common.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.description
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.mod.tree.contents.DrippingLogBlock
import miragefairy2024.mod.tree.contents.ChargeableLeavesBlock
import miragefairy2024.mod.tree.contents.IncisableLogBlock
import miragefairy2024.mod.tree.contents.HollowLogBlock
import miragefairy2024.mod.tree.contents.IncisedLogBlock
import miragefairy2024.mod.tree.contents.blockcards.TreeBricksBlockCard
import miragefairy2024.mod.tree.contents.blockcards.TreeChargeableLeavesBlockCard
import miragefairy2024.mod.tree.contents.blockcards.TreeDoorBlockCard
import miragefairy2024.mod.tree.contents.blockcards.TreeDrippingLogBlockCard
import miragefairy2024.mod.tree.contents.blockcards.TreeHollowLogBlockCard
import miragefairy2024.mod.tree.contents.blockcards.TreeIncisableLogBlockCard
import miragefairy2024.mod.tree.contents.blockcards.TreeIncisedLogBlockCard
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
import miragefairy2024.util.string
import miragefairy2024.util.toBlockTag
import miragefairy2024.util.toItemTag
import net.fabricmc.fabric.api.`object`.builder.v1.block.type.BlockSetTypeBuilder
import net.fabricmc.fabric.api.`object`.builder.v1.block.type.WoodTypeBuilder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.ButtonBlock
import net.minecraft.world.level.block.DoorBlock
import net.minecraft.world.level.block.FenceBlock
import net.minecraft.world.level.block.FenceGateBlock
import net.minecraft.world.level.block.PressurePlateBlock
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.SaplingBlock
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.StairBlock
import net.minecraft.world.level.block.TrapDoorBlock
import net.minecraft.world.level.block.grower.TreeGrower
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.properties.BlockSetType
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument
import net.minecraft.world.level.block.state.properties.WoodType
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature
import net.minecraft.world.level.material.MapColor
import java.util.Optional

interface TreeConfiguration {
    fun getWoodMapColor(): MapColor
    fun getPlankMapColor(): MapColor
    fun getBlockTag(): TagKey<Block>
    fun getItemTag(): TagKey<Item>
    fun getBlockSetType(): BlockSetType
    fun getWoodType(): WoodType
    fun getTreeGrowerName(): ResourceLocation
}

class TreeBlockConfiguration(
    val tree: TreeConfiguration,
    val path: String,
    val name: EnJa,
    val poemList: PoemList,
) {
    val blockCreatorConverters = mutableListOf<(suspend (BlockBehaviour.Properties) -> Block) -> suspend (BlockBehaviour.Properties) -> Block>()
    val blockTags = mutableListOf<TagKey<Block>>()
    val itemTags = mutableListOf<TagKey<Item>>()
}

@JvmName("blockTag")
fun TreeBlockConfiguration.tag(tag: TagKey<Block>) = this.also { it.blockTags += tag }

@JvmName("itemTag")
fun TreeBlockConfiguration.tag(tag: TagKey<Item>) = this.also { it.itemTags += tag }

@JvmName("blockAndItemTag")
fun TreeBlockConfiguration.tag(blockTag: TagKey<Block>, itemTag: TagKey<Item>) = this.tag(blockTag).tag(itemTag)

fun TreeBlockConfiguration.block(blockCreatorConverter: (suspend (BlockBehaviour.Properties) -> Block) -> suspend (BlockBehaviour.Properties) -> Block) = this.also { it.blockCreatorConverters += blockCreatorConverter }

private fun TreeBlockConfiguration.logBase() = this.tag(this.tree.getBlockTag(), this.tree.getItemTag()).tag(BlockTags.OVERWORLD_NATURAL_LOGS)
private fun TreeBlockConfiguration.woodBase() = this.tag(this.tree.getBlockTag(), this.tree.getItemTag())

private fun TreeBlockConfiguration.leaves(sapling: () -> TreeBlockCard) = this.tag(BlockTags.LEAVES, ItemTags.LEAVES).tag(BlockTags.MINEABLE_WITH_HOE).block { { ChargeableLeavesBlock(it) } }.let { TreeChargeableLeavesBlockCard(it, sapling) }
private fun TreeBlockConfiguration.log() = this.logBase().block { { IncisableLogBlock(it) } }.let { TreeIncisableLogBlockCard(it) }
private fun TreeBlockConfiguration.wood(log: () -> TreeBlockCard) = this.woodBase().block { { RotatedPillarBlock(it) } }.let { TreeWoodBlockCard(it, log) }
private fun TreeBlockConfiguration.strippedLog(log: () -> TreeBlockCard) = this.woodBase().tag(ResourceLocation("c", "stripped_logs").toBlockTag(), ResourceLocation("c", "stripped_logs").toItemTag()).block { { RotatedPillarBlock(it) } }.let { TreeStrippedLogBlockCard(it, log) }
private fun TreeBlockConfiguration.strippedWood(strippedLog: () -> TreeBlockCard, wood: () -> TreeBlockCard) = this.woodBase().tag(ResourceLocation("c", "stripped_woods").toBlockTag(), ResourceLocation("c", "stripped_woods").toItemTag()).block { { RotatedPillarBlock(it) } }.let { TreeStrippedWoodBlockCard(it, strippedLog, wood) }
private fun TreeBlockConfiguration.incisedLog(log: () -> TreeBlockCard) = this.logBase().block { { IncisedLogBlock(it) } }.let { TreeIncisedLogBlockCard(it, log) }
private fun TreeBlockConfiguration.drippingLog(log: () -> TreeBlockCard, sap: () -> Item, rosin: () -> Item) = this.logBase().block { { DrippingLogBlock(it) } }.let { TreeDrippingLogBlockCard(it, log, sap, rosin) }
private fun TreeBlockConfiguration.hollowLog(log: () -> TreeBlockCard, wisp: () -> Item) = this.logBase().block { { HollowLogBlock(it) } }.let { TreeHollowLogBlockCard(it, log, wisp) }
private fun TreeBlockConfiguration.planks(input: () -> TreeBlockCard) = this.tag(BlockTags.PLANKS, ItemTags.PLANKS).block { { Block(it) } }.let { TreePlanksBlockCard(it) { input().item() } }
private fun TreeBlockConfiguration.slab(base: () -> TreeBlockCard) = this.tag(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS).block { { SlabBlock(it) } }.let { TreePlanksSlabBlockCard(it) { base().block } }
private fun TreeBlockConfiguration.stairs(base: () -> TreeBlockCard) = this.tag(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS).block { { StairBlock(base().block.await().defaultBlockState(), it) } }.let { TreePlanksStairsBlockCard(it) { base().block } }
private fun TreeBlockConfiguration.fence(parent: () -> TreeBlockCard) = this.tag(BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES).block { { FenceBlock(it) } }.let { TreePlanksFenceBlockCard(it) { parent().block() } }
private fun TreeBlockConfiguration.fenceGate(parent: () -> TreeBlockCard) = this.tag(BlockTags.FENCE_GATES, ItemTags.FENCE_GATES).tag(ResourceLocation("c", "fence_gates/wooden").toBlockTag(), ResourceLocation("c", "fence_gates/wooden").toItemTag()).block { { FenceGateBlock(this.tree.getWoodType(), it) } }.let { TreePlanksFenceGateBlockCard(it) { parent().block() } }
private fun TreeBlockConfiguration.button(parent: () -> TreeBlockCard) = this.tag(BlockTags.WOODEN_BUTTONS, ItemTags.WOODEN_BUTTONS).block { { ButtonBlock(this.tree.getBlockSetType(), 30, it) } }.let { TreePlanksButtonBlockCard(it) { parent().block() } }
private fun TreeBlockConfiguration.pressurePlate(parent: () -> TreeBlockCard) = this.tag(BlockTags.WOODEN_PRESSURE_PLATES, ItemTags.WOODEN_PRESSURE_PLATES).block { { PressurePlateBlock(this.tree.getBlockSetType(), it) } }.let { TreePlanksPressurePlateBlockCard(it) { parent().block() } }
private fun TreeBlockConfiguration.door(parent: () -> TreeBlockCard) = this.tag(BlockTags.WOODEN_DOORS, ItemTags.WOODEN_DOORS).block { { DoorBlock(this.tree.getBlockSetType(), it) } }.let { TreeDoorBlockCard(it) { parent().block() } }
private fun TreeBlockConfiguration.trapdoor(parent: () -> TreeBlockCard) = this.tag(BlockTags.WOODEN_TRAPDOORS, ItemTags.WOODEN_TRAPDOORS).block { { TrapDoorBlock(this.tree.getBlockSetType(), it) } }.let { TreeTrapdoorBlockCard(it) { parent().block() } }
private fun TreeBlockConfiguration.bricks(input: () -> TreeBlockCard) = this.tag(BlockTags.PLANKS, ItemTags.PLANKS).block { { Block(it) } }.let { TreeBricksBlockCard(it) { input().item() } }
private fun TreeBlockConfiguration.sapling(giantTree: ResourceKey<ConfiguredFeature<*, *>>, smallTree: ResourceKey<ConfiguredFeature<*, *>>) = this.tag(BlockTags.SAPLINGS, ItemTags.SAPLINGS).block { { SaplingBlock(TreeGrower(this.tree.getTreeGrowerName().string, Optional.of(giantTree), Optional.of(smallTree), Optional.empty()), it) } }.let { TreeSaplingBlockCard(it) }

abstract class TreeBlockCard(val configuration: TreeBlockConfiguration) {
    companion object {
        val entries = mutableListOf<TreeBlockCard>()
        private operator fun TreeBlockCard.not() = apply { entries += this }

        val HAIMEVISKA_TREE_CONFIGURATION = object : TreeConfiguration {
            override fun getWoodMapColor() = MapColor.TERRACOTTA_ORANGE
            override fun getPlankMapColor() = MapColor.RAW_IRON
            override fun getBlockTag() = HAIMEVISKA_LOGS_BLOCK_TAG
            override fun getItemTag() = HAIMEVISKA_LOGS_ITEM_TAG
            override fun getBlockSetType() = HAIMEVISKA_BLOCK_SET_TYPE
            override fun getWoodType() = HAIMEVISKA_WOOD_TYPE
            override fun getTreeGrowerName() = MirageFairy2024.identifier("haimeviska")
        }

        val LEAVES = !TreeBlockConfiguration(
            HAIMEVISKA_TREE_CONFIGURATION, "haimeviska_leaves", EnJa("Haimeviska Leaves", "ハイメヴィスカの葉"),
            PoemList(1).poem(EnJa("All original flowers are consumed by ivy", "妖精になれる花、なれない花。")),
        ).leaves { SAPLING }
        val LOG = !TreeBlockConfiguration(
            HAIMEVISKA_TREE_CONFIGURATION, "haimeviska_log", EnJa("Haimeviska Log", "ハイメヴィスカの原木"),
            PoemList(1)
                .poem(EnJa("Symbiosis with parasitic Mirages", "妖精の滲み込んだ樹。"))
                .description(EnJa("Can be incised with a sword", "剣を使って傷を付けられる")),
        ).log()
        val WOOD = !TreeBlockConfiguration(
            HAIMEVISKA_TREE_CONFIGURATION, "haimeviska_wood", EnJa("Haimeviska Wood", "ハイメヴィスカの木"),
            PoemList(1).poem(EnJa("Hydraulic communication system", "ウィスプたちの集合知。")),
        ).wood { LOG }
        val STRIPPED_LOG = !TreeBlockConfiguration(
            HAIMEVISKA_TREE_CONFIGURATION, "stripped_haimeviska_log", EnJa("Stripped Haimeviska Log", "樹皮を剥いだハイメヴィスカの原木"),
            PoemList(1).poem(EnJa("Something lacking the essence", "ぬぐわれたペルソナ。")),
        ).strippedLog { LOG }
        val STRIPPED_WOOD = !TreeBlockConfiguration(
            HAIMEVISKA_TREE_CONFIGURATION, "stripped_haimeviska_wood", EnJa("Stripped Haimeviska Wood", "樹皮を剥いだハイメヴィスカの木"),
            PoemList(1).poem(EnJa("Loss of self", "寄生蔦からの解放。")),
        ).strippedWood({ STRIPPED_LOG }, { WOOD })
        val INCISED_LOG = !TreeBlockConfiguration(
            HAIMEVISKA_TREE_CONFIGURATION, "incised_haimeviska_log", EnJa("Incised Haimeviska Log", "傷の付いたハイメヴィスカの原木"),
            PoemList(1)
                .poem(EnJa("Do fairy trees have qualia of pain?", "動物を守るということ。"))
                .description(EnJa("Produces sap over time", "時間経過で樹液を生産")),
        ).incisedLog { LOG }
        val DRIPPING_LOG = !TreeBlockConfiguration(
            HAIMEVISKA_TREE_CONFIGURATION, "dripping_haimeviska_log", EnJa("Dripping Haimeviska Log", "滴るハイメヴィスカの原木"),
            PoemList(1)
                .poem(EnJa("A spirit named 'glucose'", "霊界より降りしもの。"))
                .description(EnJa("Harvest sap when used", "使用時、樹液を収穫")),
        ).drippingLog({ LOG }, { MaterialCard.HAIMEVISKA_SAP.item() }, { MaterialCard.HAIMEVISKA_ROSIN.item() })
        val HOLLOW_LOG = !TreeBlockConfiguration(
            HAIMEVISKA_TREE_CONFIGURATION, "hollow_haimeviska_log", EnJa("Hollow Haimeviska Log", "ハイメヴィスカの樹洞"),
            PoemList(1).poem(EnJa("Auric conceptual attractor", "限界巡回アステリア。")),
        ).hollowLog({ LOG }, { MaterialCard.FRACTAL_WISP.item() })
        val PLANKS = !TreeBlockConfiguration(
            HAIMEVISKA_TREE_CONFIGURATION, "haimeviska_planks", EnJa("Haimeviska Planks", "ハイメヴィスカの板材"),
            PoemList(1).poem(EnJa("Flexible and friendly, good for interior", "考える、壁。")),
        ).planks { LOG }
        val SLAB = !TreeBlockConfiguration(
            HAIMEVISKA_TREE_CONFIGURATION, "haimeviska_slab", EnJa("Haimeviska Slab", "ハイメヴィスカのハーフブロック"),
            PoemList(1).poem(EnJa("Searching for another personality.", "半人前の側頭葉。")),
        ).slab { PLANKS }
        val STAIRS = !TreeBlockConfiguration(
            HAIMEVISKA_TREE_CONFIGURATION, "haimeviska_stairs", EnJa("Haimeviska Stairs", "ハイメヴィスカの階段"),
            PoemList(1).poem(EnJa("Step that pierces the sky", "情緒体を喰らう頂となれ。")),
        ).stairs { PLANKS }
        val FENCE = !TreeBlockConfiguration(
            HAIMEVISKA_TREE_CONFIGURATION, "haimeviska_fence", EnJa("Haimeviska Fence", "ハイメヴィスカのフェンス"),
            PoemList(1).poem(EnJa("Personality flowing through the xylem", "樹のなかに住む。")),
        ).fence { PLANKS }
        val FENCE_GATE = !TreeBlockConfiguration(
            HAIMEVISKA_TREE_CONFIGURATION, "haimeviska_fence_gate", EnJa("Haimeviska Fence Gate", "ハイメヴィスカのフェンスゲート"),
            PoemList(1).poem(EnJa("It chose this path of its own will", "知性の邂逅。")),
        ).fenceGate { PLANKS }
        val BUTTON = !TreeBlockConfiguration(
            HAIMEVISKA_TREE_CONFIGURATION, "haimeviska_button", EnJa("Haimeviska Button", "ハイメヴィスカのボタン"),
            PoemList(1).poem(EnJa("What is this soft and warm thing?", "指先の感触。")),
        ).button { PLANKS }
        val PRESSURE_PLATE = !TreeBlockConfiguration(
            HAIMEVISKA_TREE_CONFIGURATION, "haimeviska_pressure_plate", EnJa("Haimeviska Pressure Plate", "ハイメヴィスカの感圧板"),
            PoemList(1).poem(EnJa("Creature with the name of a machine", "反応と感覚の違い。")),
        ).pressurePlate { PLANKS }
        val DOOR = !TreeBlockConfiguration(
            HAIMEVISKA_TREE_CONFIGURATION, "haimeviska_door", EnJa("Haimeviska Door", "ハイメヴィスカのドア"),
            PoemList(1).poem(EnJa("Astral read-only vortex", "遺伝子の水平伝播。")),
        ).door { PLANKS }
        val TRAPDOOR = !TreeBlockConfiguration(
            HAIMEVISKA_TREE_CONFIGURATION, "haimeviska_trapdoor", EnJa("Haimeviska Trapdoor", "ハイメヴィスカのトラップドア"),
            PoemList(1).poem(EnJa("Intermingling astral vortices", "自己認識の防衛線。")),
        ).trapdoor { PLANKS }
        val BRICKS = !TreeBlockConfiguration(
            HAIMEVISKA_TREE_CONFIGURATION, "haimeviska_bricks", EnJa("Haimeviska Bricks", "ハイメヴィスカレンガ"),
            PoemList(1).poem(EnJa("An ecosystem called 'civilization'", "人がもたらした原生林。")),
        ).bricks { SLAB }
        val BRICKS_SLAB = !TreeBlockConfiguration(
            HAIMEVISKA_TREE_CONFIGURATION, "haimeviska_bricks_slab", EnJa("Haimeviska Brick Slab", "ハイメヴィスカレンガのハーフブロック"),
            PoemList(1).poem(EnJa("Extremely modularized memory", "ひとまわり細かくなった私。")),
        ).slab { BRICKS }
        val BRICKS_STAIRS = !TreeBlockConfiguration(
            HAIMEVISKA_TREE_CONFIGURATION, "haimeviska_bricks_stairs", EnJa("Haimeviska Brick Stairs", "ハイメヴィスカレンガの階段"),
            PoemList(1).poem(EnJa("Forgotten paths of the technology", "生体工学の歩み。")),
        ).stairs { BRICKS }
        val SAPLING = !TreeBlockConfiguration(
            HAIMEVISKA_TREE_CONFIGURATION, "haimeviska_sapling", EnJa("Haimeviska Sapling", "ハイメヴィスカの苗木"),
            PoemList(1).poem(EnJa("Assembling molecules with Ergs", "第二の葉緑体。")),
        ).sapling(GIANT_HAIMEVISKA_CONFIGURED_FEATURE_KEY, SMALL_HAIMEVISKA_CONFIGURED_FEATURE_KEY)
    }

    val identifier = MirageFairy2024.identifier(configuration.path)

    open fun createSettings(): BlockBehaviour.Properties = BlockBehaviour.Properties.of()
    val block = Registration(BuiltInRegistries.BLOCK, identifier) {
        val initial: suspend (BlockBehaviour.Properties) -> Block = { it: BlockBehaviour.Properties -> Block(it) }
        configuration.blockCreatorConverters.fold(initial) { a, b -> b(a) }(createSettings())
    }

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

context(ModContext)
fun initTreeBlocks() {

    TreeBlockCard.entries.forEach { card ->
        card.init()
    }

    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("haimeviska_leaves")) { ChargeableLeavesBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("haimeviska_log")) { IncisableLogBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("incised_haimeviska_log")) { IncisedLogBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("dripping_haimeviska_log")) { DrippingLogBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("hollow_haimeviska_log")) { HollowLogBlock.CODEC }.register()

    // Wood Type
    HAIMEVISKA_BLOCK_SET_TYPE = BlockSetTypeBuilder().register(MirageFairy2024.identifier("haimeviska"))
    HAIMEVISKA_WOOD_TYPE = WoodTypeBuilder().register(MirageFairy2024.identifier("haimeviska"), HAIMEVISKA_BLOCK_SET_TYPE)

    // タグ
    HAIMEVISKA_LOGS_BLOCK_TAG.enJa(EnJa("Haimeviska Logs", "ハイメヴィスカの原木"))
    HAIMEVISKA_LOGS_ITEM_TAG.enJa(EnJa("Haimeviska Logs", "ハイメヴィスカの原木"))
    BlockTags.LOGS_THAT_BURN.generator.registerChild(HAIMEVISKA_LOGS_BLOCK_TAG)
    ItemTags.LOGS_THAT_BURN.generator.registerChild(HAIMEVISKA_LOGS_ITEM_TAG)

}
