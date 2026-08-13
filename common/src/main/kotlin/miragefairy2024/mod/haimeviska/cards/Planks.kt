package miragefairy2024.mod.haimeviska.cards

import miragefairy2024.ModContext
import miragefairy2024.mod.wood.WoodBlockCard
import miragefairy2024.mod.wood.WoodBlockConfiguration
import miragefairy2024.mod.wood.createBaseWoodSetting
import miragefairy2024.util.from
import miragefairy2024.util.generator
import miragefairy2024.util.on
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.registerShapelessRecipeGeneration
import miragefairy2024.util.registerSingletonBlockStateGeneration
import net.minecraft.data.models.model.TexturedModel
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor

fun createPlankSettings(sound: Boolean = true): BlockBehaviour.Properties = createBaseWoodSetting(sound = sound)
    .strength(2.0F, 3.0F)
    .mapColor(MapColor.RAW_IRON)

open class AbstractHaimeviskaPlanksBlockCard(configuration: WoodBlockConfiguration) : WoodBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = createBaseWoodSetting()
        .strength(2.0F, 3.0F)
        .mapColor(MapColor.RAW_IRON)

    override suspend fun createBlock(properties: BlockBehaviour.Properties) = Block(properties)

    context(ModContext)
    override fun init() {
        super.init()

        block.registerSingletonBlockStateGeneration()
        block.registerModelGeneration(TexturedModel.CUBE)
        block.registerDefaultLootTableGeneration()

        // 性質
        block.registerFlammable(5, 20)

        // タグ
        BlockTags.PLANKS.generator.registerChild(block)
        ItemTags.PLANKS.generator.registerChild(item)

    }
}

class HaimeviskaPlanksBlockCard(configuration: WoodBlockConfiguration, private val input: () -> Item) : AbstractHaimeviskaPlanksBlockCard(configuration) {
    context(ModContext)
    override fun init() {
        super.init()
        registerShapelessRecipeGeneration(item, 4) {
            requires(input())
        } on input from input
    }
}

class HaimeviskaBricksBlockCard(configuration: WoodBlockConfiguration, private val input: () -> Item) : AbstractHaimeviskaPlanksBlockCard(configuration) {
    context(ModContext)
    override fun init() {
        super.init()
        registerShapedRecipeGeneration(item) {
            pattern("#")
            pattern("#")
            define('#', input())
        } on input from input
    }
}
