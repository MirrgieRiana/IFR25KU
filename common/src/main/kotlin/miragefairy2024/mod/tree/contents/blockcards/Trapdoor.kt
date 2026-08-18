package miragefairy2024.mod.tree.contents.blockcards

import miragefairy2024.ModContext
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.TreeBlockConfiguration
import miragefairy2024.util.generator
import miragefairy2024.util.registerBlockFamily
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerDefaultLootTableGeneration
import net.minecraft.data.models.model.TexturedModel
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.TrapDoorBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.properties.BlockSetType

class TreeTrapdoorBlockCard(configuration: TreeBlockConfiguration, private val blockSetType: () -> BlockSetType, private val parent: () -> Block) : TreeBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = createPlankSettings()
        .strength(3.0F)
        .noOcclusion()
        .isValidSpawn(Blocks::never)

    override suspend fun createBlock(properties: BlockBehaviour.Properties) = TrapDoorBlock(blockSetType(), properties)

    context(ModContext)
    override fun init() {
        super.init()

        registerBlockFamily(TexturedModel.CUBE, parent) { it.trapdoor(block()) }
        block.registerDefaultLootTableGeneration()

        // レンダリング
        block.registerCutoutRenderLayer()

        // タグ
        BlockTags.WOODEN_TRAPDOORS.generator.registerChild(block)
        ItemTags.WOODEN_TRAPDOORS.generator.registerChild(item)

    }
}
