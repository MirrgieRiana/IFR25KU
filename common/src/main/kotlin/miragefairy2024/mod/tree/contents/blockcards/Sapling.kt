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
import miragefairy2024.util.times
import miragefairy2024.util.with
import net.minecraft.data.models.model.ModelTemplates
import net.minecraft.data.models.model.TextureSlot
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.material.PushReaction

class TreeSaplingBlockCard(configuration: TreeBlockConfiguration) : TreeBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = super.createSettings()
        .mapColor(MapColor.PLANT)
        .noCollission()
        .randomTicks()
        .instabreak()
        .sound(SoundType.GRASS)
        .pushReaction(PushReaction.DESTROY)

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
