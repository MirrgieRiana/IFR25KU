package miragefairy2024.mod.tree.contents.blockcards

import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.TreeBlockConfiguration
import miragefairy2024.mod.tree.contents.IncisedPlasticTreeLogBlock
import net.minecraft.world.level.block.state.BlockBehaviour

class TreeIncisedPlasticTreeLogBlockCard(configuration: TreeBlockConfiguration, sourceLog: () -> TreeBlockCard) : AbstractTreeIncisedLogBlockCard(configuration, sourceLog) {
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = IncisedPlasticTreeLogBlock(properties)
}
