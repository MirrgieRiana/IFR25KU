package miragefairy2024.mod.haimeviska.cards

import com.mojang.serialization.MapCodec
import miragefairy2024.ModContext
import miragefairy2024.lib.SimpleHorizontalFacingBlock
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.mod.haimeviska.HaimeviskaBlockConfiguration
import miragefairy2024.util.ItemLootPoolEntry
import miragefairy2024.util.LootPool
import miragefairy2024.util.LootTable
import miragefairy2024.util.get
import miragefairy2024.util.registerLootTableGeneration
import miragefairy2024.util.with
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState

class HaimeviskaIncisedLogBlockCard(configuration: HaimeviskaBlockConfiguration) : HaimeviskaHorizontalFacingLogBlockCard(configuration) {
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = IncisedHaimeviskaLogBlock(properties)

    context(ModContext)
    override fun init() {
        super.init()

        block.registerLootTableGeneration { provider, _ ->
            LootTable(
                LootPool(ItemLootPoolEntry(item())) {
                    `when`(provider.hasSilkTouch())
                },
                LootPool(ItemLootPoolEntry(LOG.item())) {
                    `when`(provider.doesNotHaveSilkTouch())
                },
            ) {
                provider.applyExplosionDecay(block(), this)
            }
        }

    }
}

@Suppress("OVERRIDE_DEPRECATION")
class IncisedHaimeviskaLogBlock(settings: Properties) : SimpleHorizontalFacingBlock(settings) {
    companion object {
        val CODEC: MapCodec<IncisedHaimeviskaLogBlock> = simpleCodec(::IncisedHaimeviskaLogBlock)
    }

    override fun codec() = CODEC

    override fun isRandomlyTicking(state: BlockState) = true
    override fun randomTick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {
        if (random.nextInt(100) == 0) {
            world.setBlock(pos, HaimeviskaBlockCard.DRIPPING_LOG.block().defaultBlockState().with(FACING, state[FACING]), UPDATE_ALL)
        }
    }
}
