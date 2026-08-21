package miragefairy2024.mod.tree.contents.blockcards

import miragefairy2024.ModContext
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.TreeBlockConfiguration
import miragefairy2024.mod.tree.contents.IncisedHaimeviskaLogBlock
import miragefairy2024.util.ItemLootPoolEntry
import miragefairy2024.util.LootPool
import miragefairy2024.util.LootTable
import miragefairy2024.util.registerLootTableGeneration
import net.minecraft.world.level.block.state.BlockBehaviour

abstract class AbstractTreeIncisedLogBlockCard(configuration: TreeBlockConfiguration, sourceLog: () -> TreeBlockCard) : TreeHorizontalFacingLogBlockCard(configuration, sourceLog) {
    context(ModContext)
    override fun init() {
        super.init()

        block.registerLootTableGeneration { provider, _ ->
            LootTable(
                LootPool(ItemLootPoolEntry(item())) {
                    `when`(provider.hasSilkTouch())
                },
                LootPool(ItemLootPoolEntry(sourceLog().item())) {
                    `when`(provider.doesNotHaveSilkTouch())
                },
            ) {
                provider.applyExplosionDecay(block(), this)
            }
        }

    }
}

class TreeIncisedLogBlockCard(configuration: TreeBlockConfiguration, sourceLog: () -> TreeBlockCard) : AbstractTreeIncisedLogBlockCard(configuration, sourceLog) {
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = IncisedHaimeviskaLogBlock(properties)
}
