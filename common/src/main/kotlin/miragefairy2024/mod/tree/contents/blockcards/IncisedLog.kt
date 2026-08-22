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

class TreeIncisedLogBlockCard(
    configuration: TreeBlockConfiguration,
    log: () -> TreeBlockCard,
) : TreeHorizontalFacingLogBlockCard(configuration, log) {
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = IncisedHaimeviskaLogBlock(properties)

    context(ModContext)
    override fun init() {
        super.init()

        block.registerLootTableGeneration { provider, _ ->
            LootTable(
                LootPool(ItemLootPoolEntry(item())) {
                    `when`(provider.hasSilkTouch())
                },
                LootPool(ItemLootPoolEntry(log().item())) {
                    `when`(provider.doesNotHaveSilkTouch())
                },
            ) {
                provider.applyExplosionDecay(block(), this)
            }
        }

    }
}
