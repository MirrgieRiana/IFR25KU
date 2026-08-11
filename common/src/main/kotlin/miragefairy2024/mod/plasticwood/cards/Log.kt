package miragefairy2024.mod.plasticwood.cards

import com.mojang.serialization.MapCodec
import miragefairy2024.DataGenerationEvents
import miragefairy2024.ModContext
import miragefairy2024.mod.plasticwood.PLASTIC_TREE_LOGS_BLOCK_TAG
import miragefairy2024.mod.plasticwood.PLASTIC_TREE_LOGS_ITEM_TAG
import miragefairy2024.mod.plasticwood.PlasticWoodBlockCard
import miragefairy2024.mod.plasticwood.PlasticWoodBlockConfiguration
import miragefairy2024.mod.plasticwood.createPlasticTreeBaseWoodSetting
import miragefairy2024.util.generator
import miragefairy2024.util.get
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerFlammable
import net.minecraft.core.Direction
import net.minecraft.data.models.BlockModelGenerators.WoodProvider
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor

// プラノキ原木系のブロックカードの抽象基底クラスなのだ
abstract class AbstractPlasticTreeLogBlockCard(configuration: PlasticWoodBlockConfiguration) : PlasticWoodBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = createPlasticTreeBaseWoodSetting().strength(2.0F)

    context(ModContext)
    override fun init() {
        super.init()

        // ルートテーブル
        block.registerDefaultLootTableGeneration()

        // 性質
        block.registerFlammable(5, 5)

        // タグ
        PLASTIC_TREE_LOGS_BLOCK_TAG.generator.registerChild(block)
        PLASTIC_TREE_LOGS_ITEM_TAG.generator.registerChild(item)

    }

    context(ModContext)
    protected fun registerModelGeneration(parent: () -> Block, initializer: (WoodProvider) -> WoodProvider) = DataGenerationEvents.onGenerateBlockModel {
        initializer(it.woodProvider(parent()))
    }
}

// 通常のプラノキ原木カードなのだ。樹液の仕組みは次のPRで追加するのだ
class PlasticTreeLogBlockCard(configuration: PlasticWoodBlockConfiguration) : AbstractPlasticTreeLogBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = super.createSettings().mapColor { if (it[RotatedPillarBlock.AXIS] === Direction.Axis.Y) MapColor.SAND else MapColor.COLOR_YELLOW }
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = PlasticTreeLogBlock(properties)

    context(ModContext)
    override fun init() {
        super.init()
        registerModelGeneration(block) { it.logWithHorizontal(block()) }
        BlockTags.OVERWORLD_NATURAL_LOGS.generator.registerChild(block)
    }
}

// プラノキ原木ブロッククラスなのだ。樹液の仕組みは次のPRで追加するのだ
class PlasticTreeLogBlock(settings: Properties) : RotatedPillarBlock(settings) {
    companion object {
        val CODEC: MapCodec<PlasticTreeLogBlock> = simpleCodec(::PlasticTreeLogBlock)
    }

    override fun codec() = CODEC
}
