package miragefairy2024.mod.tree.contents.blockcards

import miragefairy2024.ModContext
import miragefairy2024.mod.tree.TreeBlockConfiguration
import miragefairy2024.mod.tree.contents.HaimeviskaLogBlock
import miragefairy2024.mod.tree.contents.PlasticTreeLogBlock
import miragefairy2024.util.get
import net.minecraft.core.Direction
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.state.BlockBehaviour

abstract class AbstractTreeIncisableLogBlockCard(configuration: TreeBlockConfiguration) : AbstractTreeLogBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = super.createSettings().mapColor { if (it[RotatedPillarBlock.AXIS] === Direction.Axis.Y) configuration.tree.getPlankMapColor() else configuration.tree.getWoodMapColor() }

    context(ModContext)
    override fun init() {
        super.init()
        registerModelGeneration(block) { it.logWithHorizontal(block()) }
    }
}

class TreeIncisableLogBlockCard(configuration: TreeBlockConfiguration) : AbstractTreeIncisableLogBlockCard(configuration) {
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = HaimeviskaLogBlock(properties)
}

class TreeIncisablePlasticTreeLogBlockCard(configuration: TreeBlockConfiguration) : AbstractTreeIncisableLogBlockCard(configuration) {
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = PlasticTreeLogBlock(properties)
}
