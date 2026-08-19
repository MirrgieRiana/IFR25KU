package miragefairy2024.mod.tree.contents.blockcards

import miragefairy2024.mod.tree.TreeBlockConfiguration
import miragefairy2024.mod.tree.contents.HaimeviskaLogBlock
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor

class TreeIncisableLogBlockCard(
    configuration: TreeBlockConfiguration,
    logsBlockTag: TagKey<Block>,
    logsItemTag: TagKey<Item>,
    verticalMapColor: MapColor,
    horizontalMapColor: MapColor,
) : TreeLogBlockCard(configuration, logsBlockTag, logsItemTag, verticalMapColor, horizontalMapColor) {
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = HaimeviskaLogBlock(properties)
}
