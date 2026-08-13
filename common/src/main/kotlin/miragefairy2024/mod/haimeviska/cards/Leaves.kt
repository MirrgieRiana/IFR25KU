package miragefairy2024.mod.haimeviska.cards

import com.mojang.serialization.MapCodec
import miragefairy2024.ModContext
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.mod.haimeviska.chargedHaimeviskaLeavesTexturedModelFactory
import miragefairy2024.mod.haimeviska.unchargedHaimeviskaLeavesTexturedModelFactory
import miragefairy2024.mod.particle.ParticleTypeCard
import miragefairy2024.mod.wood.WoodBlockConfiguration
import miragefairy2024.mod.wood.cards.WoodLeavesBlockCard
import miragefairy2024.util.BlockStateVariant
import miragefairy2024.util.Model
import miragefairy2024.util.get
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.lightProxy
import miragefairy2024.util.propertiesOf
import miragefairy2024.util.randomBoolean
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.times
import miragefairy2024.util.with
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.ParticleUtils
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.LeavesBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty

class HaimeviskaLeavesBlockCard(configuration: WoodBlockConfiguration) : WoodLeavesBlockCard(configuration, { HaimeviskaBlockCard.SAPLING }) {
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = HaimeviskaLeavesBlock(properties)

    context(ModContext)
    override fun initModelGeneration() {
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
