package miragefairy2024.mod.wood.cards

import miragefairy2024.ModContext
import miragefairy2024.mod.wood.WoodBlockCard
import miragefairy2024.mod.wood.WoodBlockConfiguration
import miragefairy2024.util.generator
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.registerBlockGeneratedModelGeneration
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerComposterInput
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerSingletonBlockStateGeneration
import miragefairy2024.util.times
import miragefairy2024.util.with
import net.minecraft.data.models.model.ModelTemplates
import net.minecraft.data.models.model.TextureSlot
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.level.block.SaplingBlock
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.grower.TreeGrower
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.material.PushReaction

open class WoodSaplingBlockCard(configuration: WoodBlockConfiguration, private val treeGrower: () -> TreeGrower) : WoodBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = super.createSettings()
        .mapColor(MapColor.PLANT)
        .noCollission()
        .randomTicks()
        .instabreak()
        .sound(SoundType.GRASS)
        .pushReaction(PushReaction.DESTROY)

    override suspend fun createBlock(properties: BlockBehaviour.Properties) = SaplingBlock(treeGrower(), properties)

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

        // タグ
        BlockTags.SAPLINGS.generator.registerChild(block)
        ItemTags.SAPLINGS.generator.registerChild(item)

    }
}
