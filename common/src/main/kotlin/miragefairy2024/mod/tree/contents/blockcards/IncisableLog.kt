package miragefairy2024.mod.tree.contents.blockcards

import miragefairy2024.ModContext
import miragefairy2024.mod.tree.TreeBlockConfiguration
import miragefairy2024.mod.tree.contents.HaimeviskaLogBlock
import miragefairy2024.util.generator
import miragefairy2024.util.registerChild
import net.minecraft.core.Direction
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor

class TreeIncisableLogBlockCard(configuration: TreeBlockConfiguration) : AbstractTreeLogBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = super.createSettings().mapColor { if (it[RotatedPillarBlock.AXIS] === Direction.Axis.Y) MapColor.RAW_IRON else MapColor.TERRACOTTA_ORANGE }
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = HaimeviskaLogBlock(properties)

    context(ModContext)
    override fun init() {
        super.init()
        registerModelGeneration(block) { it.logWithHorizontal(block()) }
        BlockTags.OVERWORLD_NATURAL_LOGS.generator.registerChild(block)
    }
}
