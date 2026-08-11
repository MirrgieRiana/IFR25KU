package miragefairy2024.mod.plasticwood.cards

import com.mojang.serialization.MapCodec
import miragefairy2024.ModContext
import miragefairy2024.lib.SimpleHorizontalFacingBlock
import miragefairy2024.mod.plasticwood.PlasticWoodBlockCard
import miragefairy2024.mod.plasticwood.PlasticWoodBlockConfiguration
import miragefairy2024.util.ItemLootPoolEntry
import miragefairy2024.util.LootPool
import miragefairy2024.util.LootTable
import miragefairy2024.util.registerLootTableGeneration
import net.minecraft.world.level.block.state.BlockBehaviour

// プラノキの樹液が滴る原木カードなのだ
// 装飾の置換ブロックとして使われ、TreeDecoratorが通常原木をこのブロックに置き換えるのだ
class PlasticTreeDrippingLogBlockCard(configuration: PlasticWoodBlockConfiguration) : AbstractPlasticTreeHorizontalFacingLogBlockCard(configuration) {
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = DrippingPlasticTreeLogBlock(properties)

    context(ModContext)
    override fun init() {
        super.init()

        // ルートテーブル：シルクタッチで自身、それ以外で通常原木をドロップするのだ
        block.registerLootTableGeneration { provider, _ ->
            LootTable(
                LootPool(ItemLootPoolEntry(item())) {
                    `when`(provider.hasSilkTouch())
                },
                LootPool(ItemLootPoolEntry(PlasticWoodBlockCard.LOG.item())) {
                    `when`(provider.doesNotHaveSilkTouch())
                },
            ) {
                provider.applyExplosionDecay(block(), this)
            }
        }

    }
}

// プラノキの樹液が滴る原木ブロッククラスなのだ
// HorizontalFacingを持ち、TreeDecoratorが方向を設定してワールド生成で配置するのだ
@Suppress("OVERRIDE_DEPRECATION")
class DrippingPlasticTreeLogBlock(settings: Properties) : SimpleHorizontalFacingBlock(settings) {
    companion object {
        val CODEC: MapCodec<DrippingPlasticTreeLogBlock> = simpleCodec(::DrippingPlasticTreeLogBlock)
    }

    override fun codec() = CODEC
}
