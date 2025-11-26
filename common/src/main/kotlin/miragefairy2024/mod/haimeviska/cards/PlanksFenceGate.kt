package miragefairy2024.mod.haimeviska.cards

import miragefairy2024.ModContext
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.mod.haimeviska.HaimeviskaBlockConfiguration
import miragefairy2024.util.ResourceLocation
import miragefairy2024.util.generator
import miragefairy2024.util.registerBlockFamily
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.toBlockTag
import miragefairy2024.util.toItemTag
import net.minecraft.data.models.model.TexturedModel
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.FenceGateBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.properties.WoodType

class HaimeviskaPlanksFenceGateBlockCard(configuration: HaimeviskaBlockConfiguration, private val woodType: () -> WoodType, private val parent: () -> Block) : HaimeviskaBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = createPlankSettings(sound = false).forceSolidOn()
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = FenceGateBlock(woodType(), properties)

    context(ModContext)
    override fun init() {
        super.init()

        registerBlockFamily(TexturedModel.CUBE, parent) { it.fenceGate(block()) }
        block.registerDefaultLootTableGeneration()

        // 性質
        block.registerFlammable(5, 20)

        // タグ
        BlockTags.FENCE_GATES.generator.registerChild(block)
        ItemTags.FENCE_GATES.generator.registerChild(item)
        ResourceLocation("c", "fence_gates/wooden").toBlockTag().generator.registerChild(block)
        ResourceLocation("c", "fence_gates/wooden").toItemTag().generator.registerChild(item)

    }
}
