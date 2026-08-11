package miragefairy2024.mod.plasticwood.cards

import com.mojang.serialization.MapCodec
import miragefairy2024.ModContext
import miragefairy2024.mod.plasticwood.PlasticWoodBlockCard
import miragefairy2024.mod.plasticwood.PlasticWoodBlockConfiguration
import miragefairy2024.util.generator
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerComposterInput
import miragefairy2024.util.registerBlockGeneratedModelGeneration
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerFoliageColorProvider
import miragefairy2024.util.registerLootTableGeneration
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerRedirectColorProvider
import miragefairy2024.util.registerSingletonBlockStateGeneration
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.level.block.LeavesBlock
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.material.PushReaction
import net.minecraft.data.models.model.TexturedModel

// プラノキの葉ブロックカードなのだ
class PlasticTreeLeavesBlockCard(configuration: PlasticWoodBlockConfiguration) : PlasticWoodBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = super.createSettings()
        .mapColor(MapColor.PLANT)
        .strength(0.2F)
        .randomTicks()
        .sound(SoundType.GRASS)
        .noOcclusion()
        .isValidSpawn(Blocks::ocelotOrParrot)
        .isSuffocating(Blocks::never)
        .isViewBlocking(Blocks::never)
        .ignitedByLava()
        .pushReaction(PushReaction.DESTROY)
        .isRedstoneConductor(Blocks::never)

    override suspend fun createBlock(properties: BlockBehaviour.Properties) = PlasticTreeLeavesBlock(properties)

    context(ModContext)
    override fun init() {
        super.init()

        // レンダリング
        block.registerSingletonBlockStateGeneration()
        block.registerModelGeneration(TexturedModel.LEAVES)
        item.registerBlockGeneratedModelGeneration(block)
        block.registerCutoutRenderLayer()
        block.registerFoliageColorProvider()
        item.registerRedirectColorProvider()

        // ルートテーブル（葉からの苗木ドロップ確率はハイメヴィスカと同等にするのだ）
        block.registerLootTableGeneration { it, _ ->
            it.createLeavesDrops(block(), PlasticWoodBlockCard.SAPLING.block(), 0.05F / 4F, 0.0625F / 4F, 0.083333336F / 4F, 0.1F / 4F)
        }
        item.registerComposterInput(0.3F)

        // 性質
        block.registerFlammable(30, 30)

        // タグ
        BlockTags.LEAVES.generator.registerChild(block)
        ItemTags.LEAVES.generator.registerChild(item)
        BlockTags.MINEABLE_WITH_HOE.generator.registerChild(block)

    }
}

// プラノキの葉ブロッククラスなのだ。バイオームの葉色（琥珀色）が適用されるのだ
class PlasticTreeLeavesBlock(settings: Properties) : LeavesBlock(settings) {
    companion object {
        val CODEC: MapCodec<PlasticTreeLeavesBlock> = simpleCodec(::PlasticTreeLeavesBlock)
    }

    override fun codec() = CODEC
}
