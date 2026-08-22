package miragefairy2024.mod.tree.contents.blockcards

import miragefairy2024.ModContext
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.TreeBlockConfiguration
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.registerBlockGeneratedModelGeneration
import miragefairy2024.util.registerComposterInput
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerSingletonBlockStateGeneration
import miragefairy2024.util.string
import miragefairy2024.util.times
import miragefairy2024.util.with
import net.minecraft.data.models.model.ModelTemplates
import net.minecraft.data.models.model.TextureSlot
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.SaplingBlock
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.grower.TreeGrower
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.material.PushReaction
import java.util.Optional

class TreeSaplingBlockCard(
    configuration: TreeBlockConfiguration,
    private val treeGrowerName: ResourceLocation,
    private val giantTreeKey: ResourceKey<ConfiguredFeature<*, *>>,
    private val smallTreeKey: ResourceKey<ConfiguredFeature<*, *>>,
) : TreeBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = super.createSettings()
        .mapColor(MapColor.PLANT)
        .noCollission()
        .randomTicks()
        .instabreak()
        .sound(SoundType.GRASS)
        .pushReaction(PushReaction.DESTROY)

    override suspend fun createBlock(properties: BlockBehaviour.Properties) = SaplingBlock(
        TreeGrower(treeGrowerName.string, Optional.of(giantTreeKey), Optional.of(smallTreeKey), Optional.empty()),
        properties,
    )

    context(ModContext)
    override fun init() {
        super.init()

        // レンダリング
        block.registerSingletonBlockStateGeneration()
        block.registerModelGeneration {
            ModelTemplates.CROSS.with(
                TextureSlot.CROSS to "block/" * it.getIdentifier(),
            )
        }
        item.registerBlockGeneratedModelGeneration(block)
        block.registerCutoutRenderLayer()

        // レシピ
        block.registerDefaultLootTableGeneration()
        item.registerComposterInput(0.3F)

    }
}
