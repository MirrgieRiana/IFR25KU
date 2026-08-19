package miragefairy2024.mod.tree.contents.blockcards

import miragefairy2024.DataGenerationEvents
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.tree.HAIMEVISKA_LOGS_BLOCK_TAG
import miragefairy2024.mod.tree.HAIMEVISKA_LOGS_ITEM_TAG
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.TreeBlockConfiguration
import miragefairy2024.mod.tree.createBaseWoodSetting
import miragefairy2024.util.ResourceLocation
import miragefairy2024.util.generator
import miragefairy2024.util.get
import miragefairy2024.util.on
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.toBlockTag
import miragefairy2024.util.toItemTag
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry
import net.minecraft.data.models.BlockModelGenerators.WoodProvider
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor

abstract class AbstractTreeLogBlockCard(configuration: TreeBlockConfiguration) : TreeBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = createBaseWoodSetting().strength(2.0F)

    context(ModContext)
    override fun init() {
        super.init()

        // レシピ
        block.registerDefaultLootTableGeneration()

        // 性質
        block.registerFlammable(5, 5)

        // タグ
        HAIMEVISKA_LOGS_BLOCK_TAG.generator.registerChild(block)
        HAIMEVISKA_LOGS_ITEM_TAG.generator.registerChild(item)

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

class TreeStrippedLogBlockCard(configuration: TreeBlockConfiguration) : AbstractTreeLogBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = super.createSettings().mapColor { MapColor.RAW_IRON }
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = RotatedPillarBlock(properties)

    context(ModContext)
    override fun init() {
        super.init()
        registerModelGeneration(block) { it.logWithHorizontal(block()) }
        ResourceLocation("c", "stripped_logs").toBlockTag().generator.registerChild(block)
        ResourceLocation("c", "stripped_logs").toItemTag().generator.registerChild(item)
        initStripped(LOG.block)
    }
}

class TreeWoodBlockCard(configuration: TreeBlockConfiguration) : AbstractTreeLogBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = super.createSettings().mapColor { MapColor.TERRACOTTA_ORANGE }
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = RotatedPillarBlock(properties)

    context(ModContext)
    override fun init() {
        super.init()
        registerModelGeneration(LOG.block) { it.wood(block()) }
        initWood(LOG.item)
    }
}

class TreeStrippedWoodBlockCard(configuration: TreeBlockConfiguration) : AbstractTreeLogBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = super.createSettings().mapColor { MapColor.RAW_IRON }
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = RotatedPillarBlock(properties)

    context(ModContext)
    override fun init() {
        super.init()
        registerModelGeneration(STRIPPED_LOG.block) { it.wood(block()) }
        ResourceLocation("c", "stripped_woods").toBlockTag().generator.registerChild(block)
        ResourceLocation("c", "stripped_woods").toItemTag().generator.registerChild(item)
        initStripped(WOOD.block)
        initWood(STRIPPED_LOG.item)
    }
}
