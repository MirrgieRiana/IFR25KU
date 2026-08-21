package miragefairy2024.mod.tree.contents.blockcards

import miragefairy2024.ModContext
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.TreeBlockConfiguration
import miragefairy2024.util.registerBlockFamily
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerFlammable
import net.minecraft.data.models.model.TexturedModel
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.FenceGateBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.properties.WoodType

class TreePlanksFenceGateBlockCard(configuration: TreeBlockConfiguration, private val woodType: () -> WoodType, private val parent: () -> Block) : TreeBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = createPlankSettings(sound = false).forceSolidOn()
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = FenceGateBlock(woodType(), properties)

    context(ModContext)
    override fun init() {
        super.init()

        registerBlockFamily(TexturedModel.CUBE, parent) { it.fenceGate(block()) }
        block.registerDefaultLootTableGeneration()

        // 性質
        block.registerFlammable(5, 20)

    }
}
