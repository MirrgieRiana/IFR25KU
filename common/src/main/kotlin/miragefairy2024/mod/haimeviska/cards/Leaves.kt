package miragefairy2024.mod.haimeviska.cards

import com.mojang.serialization.MapCodec
import miragefairy2024.ModContext
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.mod.haimeviska.HaimeviskaBlockConfiguration
import miragefairy2024.mod.haimeviska.chargedHaimeviskaLeavesTexturedModelFactory
import miragefairy2024.mod.haimeviska.unchargedHaimeviskaLeavesTexturedModelFactory
import miragefairy2024.mod.particle.ParticleTypeCard
import miragefairy2024.util.BlockStateVariant
import miragefairy2024.util.Model
import miragefairy2024.util.generator
import miragefairy2024.util.get
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.lightProxy
import miragefairy2024.util.propertiesOf
import miragefairy2024.util.randomBoolean
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
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.util.ParticleUtils
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LeavesBlock
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.material.PushReaction

class HaimeviskaLeavesBlockCard(configuration: HaimeviskaBlockConfiguration) : HaimeviskaBlockCard(configuration) {
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

class HaimeviskaLeavesBlock(settings: Properties) : LeavesBlock(settings) {
    companion object {
        val CODEC: MapCodec<HaimeviskaLeavesBlock> = simpleCodec(::HaimeviskaLeavesBlock)
        val CHARGED: BooleanProperty = BooleanProperty.create("charged")
    }

    override fun codec() = CODEC

    init {
        registerDefaultState(defaultBlockState().with(CHARGED, true))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(CHARGED)
    }

    override fun isRandomlyTicking(state: BlockState) = super.isRandomlyTicking(state) || !state[CHARGED]

    @Suppress("OVERRIDE_DEPRECATION")
    override fun randomTick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {
        super.randomTick(state, world, pos, random)
        if (!state[CHARGED]) {
            if (random.randomBoolean(15, world.lightProxy.getLightLevel(pos))) {
                world.setBlock(pos, state.with(CHARGED, true), UPDATE_CLIENTS)
            }
        }
    }

    override fun animateTick(state: BlockState, world: Level, pos: BlockPos, random: RandomSource) {
        super.animateTick(state, world, pos, random)
        if (random.nextInt(20) == 0) {
            val blockPos = pos.below()
            if (!isFaceFull(world.getBlockState(blockPos).getCollisionShape(world, blockPos), Direction.UP)) {
                ParticleUtils.spawnParticleBelow(world, pos, random, ParticleTypeCard.HAIMEVISKA_BLOSSOM.particleType)
            }
        }
    }
}
