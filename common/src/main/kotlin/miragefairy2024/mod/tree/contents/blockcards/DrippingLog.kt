package miragefairy2024.mod.tree.contents.blockcards

import miragefairy2024.ModContext
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.registerHarvestNotation
import miragefairy2024.mod.tree.TreeBlockConfiguration
import miragefairy2024.mod.tree.contents.DrippingHaimeviskaLogBlock
import miragefairy2024.util.ItemLootPoolEntry
import miragefairy2024.util.LootPool
import miragefairy2024.util.LootTable
import miragefairy2024.util.get
import miragefairy2024.util.registerLootTableGeneration
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition

class TreeDrippingLogBlockCard(configuration: TreeBlockConfiguration) : TreeHorizontalFacingLogBlockCard(configuration) {
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = DrippingHaimeviskaLogBlock(properties)

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
                LootPool(ItemLootPoolEntry(MaterialCard.HAIMEVISKA_SAP.item()) {
                    apply(ApplyBonusCount.addUniformBonusCount(registries[Registries.ENCHANTMENT, Enchantments.FORTUNE]))
                }) {
                    `when`(provider.doesNotHaveSilkTouch())
                },
                LootPool(ItemLootPoolEntry(MaterialCard.HAIMEVISKA_ROSIN.item()) {
                    apply(ApplyBonusCount.addUniformBonusCount(registries[Registries.ENCHANTMENT, Enchantments.FORTUNE], 2))
                }) {
                    `when`(provider.doesNotHaveSilkTouch())
                    `when`(LootItemRandomChanceCondition.randomChance(0.01F))
                },
            ) {
                provider.applyExplosionDecay(block(), this)
            }
        }
        item.registerHarvestNotation(MaterialCard.HAIMEVISKA_SAP.item, MaterialCard.HAIMEVISKA_ROSIN.item)

    }
}
