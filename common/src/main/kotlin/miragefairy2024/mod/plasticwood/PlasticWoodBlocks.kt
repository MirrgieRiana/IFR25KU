package miragefairy2024.mod.plasticwood

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.common.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.description
import miragefairy2024.mod.plasticwood.cards.PlasticTreeLeavesBlock
import miragefairy2024.mod.plasticwood.cards.PlasticTreeLeavesBlockCard
import miragefairy2024.mod.plasticwood.cards.PlasticTreeLogBlock
import miragefairy2024.mod.plasticwood.cards.PlasticTreeLogBlockCard
import miragefairy2024.mod.plasticwood.cards.PlasticTreeSaplingBlockCard
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
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument

class PlasticWoodBlockConfiguration(
    val path: String,
    val name: EnJa,
    val poemList: PoemList,
)

abstract class PlasticWoodBlockCard(val configuration: PlasticWoodBlockConfiguration) {
    companion object {
        val entries = mutableListOf<PlasticWoodBlockCard>()
        private operator fun PlasticWoodBlockCard.not() = apply { entries += this }

        val LEAVES = !PlasticWoodBlockConfiguration(
            "plastic_tree_leaves", EnJa("Plastic Tree Leaves", "プラノキの葉"),
            PoemList(1).poem(EnJa("TODO", "TODO")),
        ).let { PlasticTreeLeavesBlockCard(it) }
        val LOG = !PlasticWoodBlockConfiguration(
            "plastic_tree_log", EnJa("Plastic Tree Log", "プラノキの原木"),
            PoemList(1).poem(EnJa("TODO", "TODO")),
        ).let { PlasticTreeLogBlockCard(it) }
        val SAPLING = !PlasticWoodBlockConfiguration(
            "plastic_tree_sapling", EnJa("Plastic Tree Sapling", "プラノキの苗木"),
            PoemList(1).poem(EnJa("TODO", "TODO")),
        ).let { PlasticTreeSaplingBlockCard(it, MirageFairy2024.identifier("plastic_tree")) }
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

// 木材設定のベースを作るのだ
fun createPlasticTreeBaseWoodSetting(sound: Boolean = true): BlockBehaviour.Properties = BlockBehaviour.Properties.of()
    .instrument(NoteBlockInstrument.BASS)
    .let { if (sound) it.sound(SoundType.WOOD) else it }
    .ignitedByLava()


val PLASTIC_TREE_LOGS_BLOCK_TAG = MirageFairy2024.identifier("plastic_tree_logs").toBlockTag()
val PLASTIC_TREE_LOGS_ITEM_TAG = MirageFairy2024.identifier("plastic_tree_logs").toItemTag()

context(ModContext)
fun initPlasticWoodBlocks() {

    PlasticWoodBlockCard.entries.forEach { card ->
        card.init()
    }

    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("plastic_tree_leaves")) { PlasticTreeLeavesBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("plastic_tree_log")) { PlasticTreeLogBlock.CODEC }.register()

    // タグ
    PLASTIC_TREE_LOGS_BLOCK_TAG.enJa(EnJa("Plastic Tree Logs", "プラノキの原木"))
    PLASTIC_TREE_LOGS_ITEM_TAG.enJa(EnJa("Plastic Tree Logs", "プラノキの原木"))
    BlockTags.LOGS_THAT_BURN.generator.registerChild(PLASTIC_TREE_LOGS_BLOCK_TAG)
    ItemTags.LOGS_THAT_BURN.generator.registerChild(PLASTIC_TREE_LOGS_ITEM_TAG)

}
