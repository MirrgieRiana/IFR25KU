package miragefairy2024.mod.tree.contents.blocks

import miragefairy2024.ModContext
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.registerHarvestNotation
import miragefairy2024.mod.tree.TreeBlockConfiguration
import miragefairy2024.mod.tree.contents.HollowHaimeviskaLogBlock
import miragefairy2024.util.ItemLootPoolEntry
import miragefairy2024.util.LootPool
import miragefairy2024.util.LootTable
import miragefairy2024.util.get
import miragefairy2024.util.registerLootTableGeneration
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount

class TreeHollowLogBlockCard(configuration: TreeBlockConfiguration) : TreeHorizontalFacingLogBlockCard(configuration) {
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = HollowHaimeviskaLogBlock(properties)

    context(ModContext)
    override fun init() {
        super.init()

        block.registerLootTableGeneration { provider, registries ->
            LootTable(
                LootPool(ItemLootPoolEntry(item())) {
                    `when`(provider.hasSilkTouch())
                },
                LootPool(ItemLootPoolEntry(LOG.item())) {
                    `when`(provider.doesNotHaveSilkTouch())
                },
                LootPool(ItemLootPoolEntry(MaterialCard.FRACTAL_WISP.item()) {
                    apply(ApplyBonusCount.addUniformBonusCount(registries[Registries.ENCHANTMENT, Enchantments.FORTUNE]))
                }) {
                    `when`(provider.doesNotHaveSilkTouch())
                },
            ) {
                provider.applyExplosionDecay(block(), this)
            }
        }
        item.registerHarvestNotation(MaterialCard.FRACTAL_WISP.item)

    }
}
