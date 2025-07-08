package miragefairy2024.mod.haimeviska.cards

import miragefairy2024.ModContext
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.mod.haimeviska.HaimeviskaBlockConfiguration
import miragefairy2024.mod.haimeviska.registerBlockFamily
import miragefairy2024.util.Registration
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerItemTagGeneration
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.StairBlock
import net.minecraft.world.level.block.state.BlockBehaviour

class HaimeviskaPlanksStairsBlockCard(configuration: HaimeviskaBlockConfiguration, private val baseBlock: () -> Registration<*, out Block>) : HaimeviskaBlockCard(configuration) {
    override fun createSettings() = createPlankSettings()
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = StairBlock(baseBlock().await().defaultBlockState(), properties)

    context(ModContext)
    override fun init() {
        super.init()

        registerBlockFamily(baseBlock()) { it.stairs(block()) }
        block.registerDefaultLootTableGeneration()

        // 性質
        block.registerFlammable(5, 20)

        // タグ
        block.registerBlockTagGeneration { BlockTags.WOODEN_STAIRS }
        item.registerItemTagGeneration { ItemTags.WOODEN_STAIRS }

    }
}
