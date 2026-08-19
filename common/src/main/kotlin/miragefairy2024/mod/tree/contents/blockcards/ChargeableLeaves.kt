package miragefairy2024.mod.tree.contents.blockcards

import miragefairy2024.ModContext
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.TreeBlockConfiguration
import miragefairy2024.mod.tree.contents.HaimeviskaLeavesBlock
import miragefairy2024.mod.tree.contents.chargedHaimeviskaLeavesTexturedModelFactory
import miragefairy2024.mod.tree.contents.unchargedHaimeviskaLeavesTexturedModelFactory
import miragefairy2024.util.BlockStateVariant
import miragefairy2024.util.Model
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.propertiesOf
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.times
import miragefairy2024.util.with
import net.minecraft.world.level.block.state.BlockBehaviour

class TreeChargeableLeavesBlockCard(configuration: TreeBlockConfiguration, sapling: () -> TreeBlockCard) : AbstractTreeLeavesBlockCard(configuration, sapling) {
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = HaimeviskaLeavesBlock(properties)

    context(ModContext)
    override fun initRendering() {
        block.registerVariantsBlockStateGeneration {
            val normal = BlockStateVariant(model = "block/" * block().getIdentifier())
            listOf(
                propertiesOf(HaimeviskaLeavesBlock.CHARGED with true) with normal.with(model = "block/charged_" * block().getIdentifier()),
                propertiesOf(HaimeviskaLeavesBlock.CHARGED with false) with normal.with(model = "block/uncharged_" * block().getIdentifier()),
            )
        }
        registerModelGeneration({ "block/charged_" * block().getIdentifier() }, { chargedHaimeviskaLeavesTexturedModelFactory.get(block()) })
        registerModelGeneration({ "block/uncharged_" * block().getIdentifier() }, { unchargedHaimeviskaLeavesTexturedModelFactory.get(block()) })
        item.registerModelGeneration(Model("block/charged_" * identifier))
    }
}
