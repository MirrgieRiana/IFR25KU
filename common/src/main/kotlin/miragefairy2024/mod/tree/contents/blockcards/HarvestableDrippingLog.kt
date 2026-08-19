package miragefairy2024.mod.tree.contents.blockcards

import miragefairy2024.ModContext
import miragefairy2024.mod.registerHarvestNotation
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.TreeBlockConfiguration
import miragefairy2024.mod.tree.contents.DrippingHaimeviskaLogBlock
import miragefairy2024.util.ItemLootPoolEntry
import miragefairy2024.util.LootPool
import miragefairy2024.util.LootTable
import miragefairy2024.util.get
import miragefairy2024.util.registerLootTableGeneration
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition
import net.minecraft.world.level.material.MapColor

/**
 * 使用すると樹液を収穫できて、傷の付いた原木に戻る、樹液が滴る原木のブロックカードなのだ～🌱
 *
 * これは樹液の採取のループを持つ樹種だけの性質なのだ～🌱
 * 稀に [rosin] も一緒に採れるのだ～🌱
 */
class TreeHarvestableDrippingLogBlockCard(
    configuration: TreeBlockConfiguration,
    log: () -> TreeBlockCard,
    logsBlockTag: TagKey<Block>,
    logsItemTag: TagKey<Item>,
    sap: () -> Item,
    private val rosin: () -> Item,
    mapColor: MapColor,
) : TreeDrippingLogBlockCard(configuration, log, logsBlockTag, logsItemTag, sap, mapColor) {
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = DrippingHaimeviskaLogBlock(properties)

    context(ModContext)
    override fun init() {
        super.init()

        block.registerLootTableGeneration { provider, registries ->
            LootTable(
                LootPool(ItemLootPoolEntry(item())) {
                    `when`(provider.hasSilkTouch())
                },
                LootPool(ItemLootPoolEntry(log().item())) {
                    `when`(provider.doesNotHaveSilkTouch())
                },
                LootPool(ItemLootPoolEntry(sap()) {
                    apply(ApplyBonusCount.addUniformBonusCount(registries[Registries.ENCHANTMENT, Enchantments.FORTUNE]))
                }) {
                    `when`(provider.doesNotHaveSilkTouch())
                },
                LootPool(ItemLootPoolEntry(rosin()) {
                    apply(ApplyBonusCount.addUniformBonusCount(registries[Registries.ENCHANTMENT, Enchantments.FORTUNE], 2))
                }) {
                    `when`(provider.doesNotHaveSilkTouch())
                    `when`(LootItemRandomChanceCondition.randomChance(0.01F))
                },
            ) {
                provider.applyExplosionDecay(block(), this)
            }
        }
        item.registerHarvestNotation(sap, rosin)

    }
}
