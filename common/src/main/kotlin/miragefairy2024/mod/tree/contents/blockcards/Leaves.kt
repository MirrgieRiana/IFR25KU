package miragefairy2024.mod.tree.contents.blockcards

import miragefairy2024.ModContext
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.TreeBlockConfiguration
import miragefairy2024.util.Model
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerSingletonBlockStateGeneration
import miragefairy2024.util.times
import net.minecraft.data.models.model.TexturedModel

class TreeLeavesBlockCard(configuration: TreeBlockConfiguration, sapling: () -> TreeBlockCard) : AbstractTreeLeavesBlockCard(configuration, sapling) {
    context(ModContext)
    override fun init() {
        super.init()
        block.registerSingletonBlockStateGeneration()
        block.registerModelGeneration(TexturedModel.LEAVES)
        item.registerModelGeneration(Model("block/" * identifier))
    }
}
