package miragefairy2024.mod.wood.cards

import com.mojang.serialization.MapCodec
import miragefairy2024.ModContext
import miragefairy2024.lib.SimpleHorizontalFacingBlock
import miragefairy2024.mod.wood.WoodBlockCard
import miragefairy2024.mod.wood.WoodBlockConfiguration
import miragefairy2024.mod.wood.createBaseWoodSetting
import miragefairy2024.util.ItemLootPoolEntry
import miragefairy2024.util.LootPool
import miragefairy2024.util.LootTable
import miragefairy2024.util.generator
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.normal
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerLootTableGeneration
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.times
import miragefairy2024.util.with
import miragefairy2024.util.withHorizontalRotation
import net.minecraft.data.models.model.ModelTemplates
import net.minecraft.data.models.model.TextureSlot
import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor

// 自然生成時に通常の原木と置き換わる、水平方向の向きを持つ原木系ブロックカードなのだ
// 側面のテクスチャのうち正面だけが差し替えられ、破壊すると元の原木に戻るのだ
open class WoodHorizontalFacingLogBlockCard(configuration: WoodBlockConfiguration, private val logCard: () -> WoodBlockCard, private val logsBlockTag: TagKey<Block>, private val logsItemTag: TagKey<Item>, private val mapColor: MapColor) : WoodBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = createBaseWoodSetting().strength(2.0F).mapColor(mapColor)
    override suspend fun createBlock(properties: BlockBehaviour.Properties): SimpleHorizontalFacingBlock = WoodHorizontalFacingLogBlock(properties)

    // 元の原木以外にも収穫物がある場合は、派生形で差し替えるのだ
    context(ModContext)
    protected open fun initLootTableGeneration() {
        block.registerLootTableGeneration { provider, _ ->
            LootTable(
                LootPool(ItemLootPoolEntry(item())) {
                    `when`(provider.hasSilkTouch())
                },
                LootPool(ItemLootPoolEntry(logCard().item())) {
                    `when`(provider.doesNotHaveSilkTouch())
                },
            ) {
                provider.applyExplosionDecay(block(), this)
            }
        }
    }

    context(ModContext)
    override fun init() {
        super.init()

        // レンダリング
        block.registerVariantsBlockStateGeneration { normal("block/" * block().getIdentifier()).withHorizontalRotation(HorizontalDirectionalBlock.FACING) }
        block.registerModelGeneration {
            ModelTemplates.CUBE_ORIENTABLE.with(
                TextureSlot.TOP to "block/" * logCard().block().getIdentifier() * "_top",
                TextureSlot.SIDE to "block/" * logCard().block().getIdentifier(),
                TextureSlot.FRONT to "block/" * it.getIdentifier(),
            )
        }

        // レシピ
        initLootTableGeneration()

        // 性質
        block.registerFlammable(5, 5)

        // タグ
        BlockTags.OVERWORLD_NATURAL_LOGS.generator.registerChild(block)
        logsBlockTag.generator.registerChild(block)
        logsItemTag.generator.registerChild(item)

    }
}

// 固有の挙動を持たない、水平方向の向きを持つ原木系ブロックなのだ
class WoodHorizontalFacingLogBlock(settings: Properties) : SimpleHorizontalFacingBlock(settings) {
    companion object {
        val CODEC: MapCodec<WoodHorizontalFacingLogBlock> = simpleCodec(::WoodHorizontalFacingLogBlock)
    }

    override fun codec() = CODEC
}
