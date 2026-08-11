package miragefairy2024.mod.plasticwood.cards

import miragefairy2024.ModContext
import miragefairy2024.mod.plasticwood.PLASTIC_TREE_LOGS_BLOCK_TAG
import miragefairy2024.mod.plasticwood.PLASTIC_TREE_LOGS_ITEM_TAG
import miragefairy2024.mod.plasticwood.PlasticWoodBlockCard
import miragefairy2024.mod.plasticwood.PlasticWoodBlockConfiguration
import miragefairy2024.mod.plasticwood.createPlasticTreeBaseWoodSetting
import miragefairy2024.util.generator
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.normal
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.times
import miragefairy2024.util.with
import miragefairy2024.util.withHorizontalRotation
import net.minecraft.data.models.model.ModelTemplates
import net.minecraft.data.models.model.TextureSlot
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor

// 水平方向を向いたプラノキ原木系（傷・滴る）の抽象基底クラスなのだ
abstract class PlasticTreeHorizontalFacingLogBlockCard(configuration: PlasticWoodBlockConfiguration) : PlasticWoodBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = createPlasticTreeBaseWoodSetting().strength(2.0F).mapColor(MapColor.SAND)

    context(ModContext)
    override fun init() {
        super.init()

        // レンダリング
        block.registerVariantsBlockStateGeneration { normal("block/" * block().getIdentifier()).withHorizontalRotation(HorizontalDirectionalBlock.FACING) }
        block.registerModelGeneration {
            ModelTemplates.CUBE_ORIENTABLE.with(
                TextureSlot.TOP to "block/" * LOG.block().getIdentifier() * "_top",
                TextureSlot.SIDE to "block/" * LOG.block().getIdentifier(),
                TextureSlot.FRONT to "block/" * it.getIdentifier(),
            )
        }

        // 性質
        block.registerFlammable(5, 5)

        // タグ
        BlockTags.OVERWORLD_NATURAL_LOGS.generator.registerChild(block)
        PLASTIC_TREE_LOGS_BLOCK_TAG.generator.registerChild(block)
        PLASTIC_TREE_LOGS_ITEM_TAG.generator.registerChild(item)

    }
}
