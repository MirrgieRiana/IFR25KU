package miragefairy2024.mod.tree.cards

import miragefairy2024.ModContext
import miragefairy2024.mod.tree.HaimeviskaBlockConfiguration
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.util.Registration
import miragefairy2024.util.generator
import miragefairy2024.util.registerBlockFamily
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerFlammable
import net.minecraft.data.models.model.TexturedModel
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.StairBlock
import net.minecraft.world.level.block.state.BlockBehaviour

class TreePlanksStairsBlockCard(configuration: HaimeviskaBlockConfiguration, private val baseBlock: () -> Registration<*, out Block>) : TreeBlockCard(configuration) {
    override fun createSettings() = createPlankSettings()
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = StairBlock(baseBlock().await().defaultBlockState(), properties)

    context(ModContext)
    override fun init() {
        super.init()

        registerBlockFamily(TexturedModel.CUBE, baseBlock()) { it.stairs(block()) }
        block.registerDefaultLootTableGeneration()

        // 性質
        block.registerFlammable(5, 20)

        // タグ
        BlockTags.WOODEN_STAIRS.generator.registerChild(block)
        ItemTags.WOODEN_STAIRS.generator.registerChild(item)

    }
}
