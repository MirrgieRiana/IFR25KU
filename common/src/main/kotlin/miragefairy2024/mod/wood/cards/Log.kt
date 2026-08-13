package miragefairy2024.mod.wood.cards

import miragefairy2024.DataGenerationEvents
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.wood.WoodBlockCard
import miragefairy2024.mod.wood.WoodBlockConfiguration
import miragefairy2024.mod.wood.createBaseWoodSetting
import miragefairy2024.util.generator
import miragefairy2024.util.get
import miragefairy2024.util.on
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerShapedRecipeGeneration
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry
import net.minecraft.core.Direction
import net.minecraft.data.models.BlockModelGenerators.WoodProvider
import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor

abstract class AbstractWoodLogBlockCard(configuration: WoodBlockConfiguration, private val logsBlockTag: TagKey<Block>, private val logsItemTag: TagKey<Item>) : WoodBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = createBaseWoodSetting().strength(2.0F)

    context(ModContext)
    override fun init() {
        super.init()

        // レシピ
        block.registerDefaultLootTableGeneration()

        // 性質
        block.registerFlammable(5, 5)

        // タグ
        logsBlockTag.generator.registerChild(block)
        logsItemTag.generator.registerChild(item)

    }

    context(ModContext)
    protected fun registerModelGeneration(parent: () -> Block, initializer: (WoodProvider) -> WoodProvider) = DataGenerationEvents.onGenerateBlockModel {
        initializer(it.woodProvider(parent()))
    }

    context(ModContext)
    protected fun initWood(input: () -> Item) {
        registerShapedRecipeGeneration(item, 3) {
            pattern("##")
            pattern("##")
            define('#', input())
        } on input
    }

    context(ModContext)
    protected fun initStripped(input: () -> Block) {
        ModEvents.onInitialize {
            StrippableBlockRegistry.register(input(), block())
        }
    }
}

// 縦方向と横方向で異なるマップ色を持つ、通常の原木カードなのだ
open class WoodLogBlockCard(configuration: WoodBlockConfiguration, logsBlockTag: TagKey<Block>, logsItemTag: TagKey<Item>, private val topMapColor: MapColor, private val sideMapColor: MapColor) : AbstractWoodLogBlockCard(configuration, logsBlockTag, logsItemTag) {
    override fun createSettings(): BlockBehaviour.Properties = super.createSettings().mapColor { if (it[RotatedPillarBlock.AXIS] === Direction.Axis.Y) topMapColor else sideMapColor }
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = RotatedPillarBlock(properties)

    context(ModContext)
    override fun init() {
        super.init()
        registerModelGeneration(block) { it.logWithHorizontal(block()) }
        BlockTags.OVERWORLD_NATURAL_LOGS.generator.registerChild(block)
    }
}
