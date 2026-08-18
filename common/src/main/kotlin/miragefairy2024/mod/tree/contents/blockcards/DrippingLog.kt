package miragefairy2024.mod.tree.contents.blockcards

import miragefairy2024.ModContext
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.registerHarvestNotation
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.TreeBlockConfiguration
import miragefairy2024.mod.tree.contents.DrippingHaimeviskaLogBlock
import miragefairy2024.mod.tree.contents.DrippingLogBlock
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
 * 樹液が滴る原木のブロックカードなのだ～🌱
 *
 * [harvestable] が有効な樹種の滴る原木は、使用すると樹液を収穫できて、傷の付いた原木に戻るのだ～🌱
 * これは樹液の採取のループを持つ樹種だけの性質だから、樹種によって分かれるのだ～🌱
 * 無効な樹種では、樹液の雫を垂らすだけの、地形生成の装飾のためのブロックなのだ～🌱
 */
class TreeDrippingLogBlockCard(
    configuration: TreeBlockConfiguration,
    log: () -> TreeBlockCard,
    logsBlockTag: TagKey<Block>,
    logsItemTag: TagKey<Item>,
    private val harvestable: Boolean,
    mapColor: MapColor,
) : TreeHorizontalFacingLogBlockCard(configuration, log, logsBlockTag, logsItemTag, mapColor) {
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = if (harvestable) DrippingHaimeviskaLogBlock(properties) else DrippingLogBlock(properties)

    context(ModContext)
    override fun init() {
        super.init()

        if (!harvestable) {
            // 収穫のループを持たない樹種の滴る原木は、壊すと普通の原木になるのだ～🌱
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
            return
        }

        block.registerLootTableGeneration { provider, registries ->
            LootTable(
                LootPool(ItemLootPoolEntry(item())) {
                    `when`(provider.hasSilkTouch())
                },
                LootPool(ItemLootPoolEntry(log().item())) {
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
