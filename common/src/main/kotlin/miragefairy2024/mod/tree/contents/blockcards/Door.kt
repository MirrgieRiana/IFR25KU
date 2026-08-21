package miragefairy2024.mod.tree.contents.blockcards

import miragefairy2024.ModContext
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.TreeBlockConfiguration
import miragefairy2024.util.registerBlockFamily
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerLootTableGeneration
import net.minecraft.data.models.model.TexturedModel
import net.minecraft.world.item.DoubleHighBlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.DoorBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.properties.BlockSetType
import net.minecraft.world.level.material.PushReaction

class TreeDoorBlockCard(configuration: TreeBlockConfiguration, private val blockSetType: () -> BlockSetType, private val parent: () -> Block) : TreeBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = createPlankSettings()
        .strength(3.0F)
        .noOcclusion()
        .pushReaction(PushReaction.DESTROY)

    override suspend fun createBlock(properties: BlockBehaviour.Properties) = DoorBlock(blockSetType(), properties)
    override suspend fun createItem(block: Block, properties: Item.Properties) = DoubleHighBlockItem(block, properties)

    context(ModContext)
    override fun init() {
        super.init()

        registerBlockFamily(TexturedModel.CUBE, parent) { it.door(block()) }
        block.registerLootTableGeneration { it, _ -> it.createDoorTable(block()) }

        // レンダリング
        block.registerCutoutRenderLayer()

    }
}
