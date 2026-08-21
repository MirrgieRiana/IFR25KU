package miragefairy2024.mod.tree.contents.blockcards

import miragefairy2024.ModContext
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.TreeBlockConfiguration
import miragefairy2024.mod.tree.contents.unchargedHaimeviskaLeavesTexturedModelFactory
import miragefairy2024.util.Model
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.registerComposterInput
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerFoliageColorProvider
import miragefairy2024.util.registerLootTableGeneration
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerRedirectColorProvider
import miragefairy2024.util.registerSingletonBlockStateGeneration
import miragefairy2024.util.times
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LeavesBlock
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.material.PushReaction

abstract class AbstractTreeLeavesBlockCard(configuration: TreeBlockConfiguration, private val sapling: () -> TreeBlockCard) : TreeBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = super.createSettings()
        .mapColor(MapColor.PLANT)
        .strength(0.2F)
        .randomTicks()
        .sound(SoundType.GRASS)
        .noOcclusion()
        .isValidSpawn(Blocks::ocelotOrParrot)
        .isSuffocating(Blocks::never)
        .isViewBlocking(Blocks::never)
        .ignitedByLava()
        .pushReaction(PushReaction.DESTROY)
        .isRedstoneConductor(Blocks::never)

    context(ModContext)
    override fun init() {
        super.init()

        // レンダリング
        initRendering()
        block.registerCutoutRenderLayer()
        block.registerFoliageColorProvider()
        item.registerRedirectColorProvider()

        // レシピ
        block.registerLootTableGeneration { it, _ ->
            it.createLeavesDrops(block(), sapling().block(), 0.05F / 4F, 0.0625F / 4F, 0.083333336F / 4F, 0.1F / 4F)
        }
        item.registerComposterInput(0.3F)

        // 性質
        block.registerFlammable(30, 30)

    }

    context(ModContext)
    protected abstract fun initRendering()
}

class TreeLeavesBlockCard(configuration: TreeBlockConfiguration, sapling: () -> TreeBlockCard) : AbstractTreeLeavesBlockCard(configuration, sapling) {
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = LeavesBlock(properties)

    context(ModContext)
    override fun initRendering() {
        block.registerSingletonBlockStateGeneration()
        registerModelGeneration({ "block/" * block().getIdentifier() }, { unchargedHaimeviskaLeavesTexturedModelFactory.get(block()) })
        item.registerModelGeneration(Model("block/" * identifier))
    }
}
