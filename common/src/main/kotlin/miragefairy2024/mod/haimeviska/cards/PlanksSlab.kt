package miragefairy2024.mod.haimeviska.cards

import miragefairy2024.ModContext
import miragefairy2024.mod.wood.WoodBlockCard
import miragefairy2024.mod.wood.WoodBlockConfiguration
import miragefairy2024.util.Registration
import miragefairy2024.util.generator
import miragefairy2024.util.registerBlockFamily
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerLootTableGeneration
import net.minecraft.data.models.model.TexturedModel
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.state.BlockBehaviour

class HaimeviskaPlanksSlabBlockCard(configuration: WoodBlockConfiguration, private val baseBlock: () -> Registration<*, out Block>) : WoodBlockCard(configuration) {
    override fun createSettings() = createPlankSettings()
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = SlabBlock(properties)

    context(ModContext)
    override fun init() {
        super.init()

        registerBlockFamily(TexturedModel.CUBE, baseBlock()) { it.slab(block()) }
        block.registerLootTableGeneration { it, _ -> it.createSlabItemTable(block()) }

        // 性質
        block.registerFlammable(5, 20)

        // タグ
        BlockTags.WOODEN_SLABS.generator.registerChild(block)
        ItemTags.WOODEN_SLABS.generator.registerChild(item)

    }
}
