package miragefairy2024.mod.tree.contents.blockcards

import miragefairy2024.ModContext
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.TreeBlockConfiguration
import miragefairy2024.mod.tree.createBaseWoodSetting
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
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor

abstract class TreeHorizontalFacingLogBlockCard(
    configuration: TreeBlockConfiguration,
    protected val log: () -> TreeBlockCard,
    private val logsBlockTag: TagKey<Block>,
    private val logsItemTag: TagKey<Item>,
    private val mapColor: MapColor,
) : TreeBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = createBaseWoodSetting().strength(2.0F).mapColor(mapColor)

    context(ModContext)
    override fun init() {
        super.init()

        // レンダリング
        block.registerVariantsBlockStateGeneration { normal("block/" * block().getIdentifier()).withHorizontalRotation(HorizontalDirectionalBlock.FACING) }
        block.registerModelGeneration {
            ModelTemplates.CUBE_ORIENTABLE.with(
                TextureSlot.TOP to "block/" * log().block().getIdentifier() * "_top",
                TextureSlot.SIDE to "block/" * log().block().getIdentifier(),
                TextureSlot.FRONT to "block/" * it.getIdentifier(),
            )
        }

        // 性質
        block.registerFlammable(5, 5)

        // タグ
        BlockTags.OVERWORLD_NATURAL_LOGS.generator.registerChild(block)
        logsBlockTag.generator.registerChild(block)
        logsItemTag.generator.registerChild(item)

    }
}
