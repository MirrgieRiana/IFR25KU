package miragefairy2024.mod.tree.contents.blockcards

import miragefairy2024.ModContext
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.TreeBlockConfiguration
import miragefairy2024.mod.tree.contents.unchargedChargeableLeavesTexturedModelFactory
import miragefairy2024.util.Model
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerSingletonBlockStateGeneration
import miragefairy2024.util.times

class TreeLeavesBlockCard(configuration: TreeBlockConfiguration, sapling: () -> TreeBlockCard) : AbstractTreeLeavesBlockCard(configuration, sapling) {
    context(ModContext)
    override fun init() {
        super.init()
        block.registerSingletonBlockStateGeneration()
        registerModelGeneration({ "block/" * block().getIdentifier() }, { unchargedChargeableLeavesTexturedModelFactory.get(block()) })
        item.registerModelGeneration(Model("block/" * identifier))
    }
}
