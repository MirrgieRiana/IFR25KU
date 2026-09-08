package miragefairy2024.mod.tree.contents.blockcards

import miragefairy2024.ModContext
import miragefairy2024.mod.tree.TreeBlockConfiguration
import miragefairy2024.util.get
import net.minecraft.core.Direction
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.state.BlockBehaviour

class TreeIncisableLogBlockCard(configuration: TreeBlockConfiguration) : AbstractTreeLogBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = super.createSettings().mapColor { if (it[RotatedPillarBlock.AXIS] === Direction.Axis.Y) configuration.tree.getPlankMapColor() else configuration.tree.getWoodMapColor() }

    context(ModContext)
    override fun init() {
        super.init()
        registerModelGeneration(block) { it.logWithHorizontal(block()) }
    }
}
