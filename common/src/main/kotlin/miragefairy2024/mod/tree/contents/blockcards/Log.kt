package miragefairy2024.mod.tree.contents.blockcards

import miragefairy2024.DataGenerationEvents
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.TreeBlockConfiguration
import miragefairy2024.mod.tree.createBaseWoodSetting
import miragefairy2024.util.on
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerShapedRecipeGeneration
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry
import net.minecraft.data.models.BlockModelGenerators.WoodProvider
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour

abstract class AbstractTreeLogBlockCard(configuration: TreeBlockConfiguration) : TreeBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = createBaseWoodSetting().strength(2.0F)

    context(ModContext)
    override fun init() {
        super.init()

        // レシピ
        block.registerDefaultLootTableGeneration()

        // 性質
        block.registerFlammable(5, 5)

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
    override fun createSettings(): BlockBehaviour.Properties = super.createSettings().mapColor { configuration.tree.getPlankMapColor() }

    context(ModContext)
    override fun init() {
        super.init()
        registerModelGeneration(block) { it.logWithHorizontal(block()) }
        initStripped(LOG.block)
    }
}

class TreeWoodBlockCard(configuration: TreeBlockConfiguration) : AbstractTreeLogBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = super.createSettings().mapColor { configuration.tree.getWoodMapColor() }

    context(ModContext)
    override fun init() {
        super.init()
        registerModelGeneration(LOG.block) { it.wood(block()) }
        initWood(LOG.item)
    }
}

class TreeStrippedWoodBlockCard(configuration: TreeBlockConfiguration) : AbstractTreeLogBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = super.createSettings().mapColor { configuration.tree.getPlankMapColor() }

    context(ModContext)
    override fun init() {
        super.init()
        registerModelGeneration(STRIPPED_LOG.block) { it.wood(block()) }
        initStripped(WOOD.block)
        initWood(STRIPPED_LOG.item)
    }
}
