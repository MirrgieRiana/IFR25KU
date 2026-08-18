package miragefairy2024.mod.tree.contents.blocks

import miragefairy2024.ModContext
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.TreeBlockConfiguration
import miragefairy2024.mod.tree.contents.HaimeviskaLeavesBlock
import miragefairy2024.mod.tree.contents.chargedHaimeviskaLeavesTexturedModelFactory
import miragefairy2024.mod.tree.contents.unchargedHaimeviskaLeavesTexturedModelFactory
import miragefairy2024.util.BlockStateVariant
import miragefairy2024.util.Model
import miragefairy2024.util.generator
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.propertiesOf
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerComposterInput
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerFoliageColorProvider
import miragefairy2024.util.registerLootTableGeneration
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerRedirectColorProvider
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.times
import miragefairy2024.util.with
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.material.PushReaction

class TreeLeavesBlockCard(configuration: TreeBlockConfiguration) : TreeBlockCard(configuration) {
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

    override suspend fun createBlock(properties: BlockBehaviour.Properties) = HaimeviskaLeavesBlock(properties)

    context(ModContext)
    override fun init() {
        super.init()

        // レンダリング
        block.registerVariantsBlockStateGeneration {
            val normal = BlockStateVariant(model = "block/" * block().getIdentifier())
            listOf(
                propertiesOf(HaimeviskaLeavesBlock.CHARGED with true) with normal.with(model = "block/charged_" * block().getIdentifier()),
                propertiesOf(HaimeviskaLeavesBlock.CHARGED with false) with normal.with(model = "block/uncharged_" * block().getIdentifier()),
            )
        }
        registerModelGeneration({ "block/charged_" * block().getIdentifier() }, { chargedHaimeviskaLeavesTexturedModelFactory.get(block()) })
        registerModelGeneration({ "block/uncharged_" * block().getIdentifier() }, { unchargedHaimeviskaLeavesTexturedModelFactory.get(block()) })
        item.registerModelGeneration(Model("block/charged_" * identifier))
        block.registerCutoutRenderLayer()
        block.registerFoliageColorProvider()
        item.registerRedirectColorProvider()

        // レシピ
        block.registerLootTableGeneration { it, _ ->
            it.createLeavesDrops(block(), SAPLING.block(), 0.05F / 4F, 0.0625F / 4F, 0.083333336F / 4F, 0.1F / 4F)
        }
        item.registerComposterInput(0.3F)

        // 性質
        block.registerFlammable(30, 30)

        // タグ
        BlockTags.LEAVES.generator.registerChild(block)
        ItemTags.LEAVES.generator.registerChild(item)
        BlockTags.MINEABLE_WITH_HOE.generator.registerChild(block)

    }
}
