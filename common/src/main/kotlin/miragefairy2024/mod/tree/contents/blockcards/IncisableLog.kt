package miragefairy2024.mod.tree.contents.blockcards

import miragefairy2024.mod.tree.TreeBlockConfiguration
import miragefairy2024.mod.tree.contents.HaimeviskaLogBlock
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor

/** 剣で傷を付けて樹液を採取できる原木のブロックカードなのだ～🌱 これは樹液の採取のループを持つ樹種だけの性質なのだ～🌱 */
class TreeIncisableLogBlockCard(
    configuration: TreeBlockConfiguration,
    logsBlockTag: TagKey<Block>,
    logsItemTag: TagKey<Item>,
    verticalMapColor: MapColor,
    horizontalMapColor: MapColor,
) : TreeLogBlockCard(configuration, logsBlockTag, logsItemTag, verticalMapColor, horizontalMapColor) {
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = HaimeviskaLogBlock(properties)
}
