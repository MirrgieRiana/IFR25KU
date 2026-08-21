package miragefairy2024.mod.tree.contents.blockcards

import miragefairy2024.mod.tree.TreeBlockConfiguration
import miragefairy2024.mod.tree.contents.HaimeviskaLogBlock
import miragefairy2024.mod.tree.contents.PlasticTreeLogBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor

class TreeIncisableLogBlockCard(
    configuration: TreeBlockConfiguration,
    verticalMapColor: MapColor,
    horizontalMapColor: MapColor,
) : TreeLogBlockCard(configuration, verticalMapColor, horizontalMapColor) {
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = HaimeviskaLogBlock(properties)
}

class TreeIncisablePlasticTreeLogBlockCard(
    configuration: TreeBlockConfiguration,
    verticalMapColor: MapColor,
    horizontalMapColor: MapColor,
) : TreeLogBlockCard(configuration, verticalMapColor, horizontalMapColor) {
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = PlasticTreeLogBlock(properties)
}
