package miragefairy2024.mod.haimeviska.cards

import com.mojang.serialization.MapCodec
import miragefairy2024.ModContext
import miragefairy2024.mod.haimeviska.HAIMEVISKA_LOGS_BLOCK_TAG
import miragefairy2024.mod.haimeviska.HAIMEVISKA_LOGS_ITEM_TAG
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.mod.wood.WoodBlockConfiguration
import miragefairy2024.mod.wood.cards.AbstractWoodLogBlockCard
import miragefairy2024.mod.wood.cards.WoodLogBlockCard
import miragefairy2024.util.ResourceLocation
import miragefairy2024.util.generator
import miragefairy2024.util.get
import miragefairy2024.util.isNotIn
import miragefairy2024.util.registerChild
import miragefairy2024.util.toBlockTag
import miragefairy2024.util.toItemTag
import miragefairy2024.util.with
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats
import net.minecraft.tags.ItemTags
import net.minecraft.world.InteractionHand
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.phys.BlockHitResult

abstract class AbstractHaimeviskaLogBlockCard(configuration: WoodBlockConfiguration) : AbstractWoodLogBlockCard(configuration, HAIMEVISKA_LOGS_BLOCK_TAG, HAIMEVISKA_LOGS_ITEM_TAG)

class HaimeviskaLogBlockCard(configuration: WoodBlockConfiguration) : WoodLogBlockCard(configuration, HAIMEVISKA_LOGS_BLOCK_TAG, HAIMEVISKA_LOGS_ITEM_TAG, MapColor.RAW_IRON, MapColor.TERRACOTTA_ORANGE) {
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = HaimeviskaLogBlock(properties)
}

class HaimeviskaStrippedLogBlockCard(configuration: WoodBlockConfiguration) : AbstractHaimeviskaLogBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = super.createSettings().mapColor { MapColor.RAW_IRON }
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = RotatedPillarBlock(properties)

    context(ModContext)
    override fun init() {
        super.init()
        registerModelGeneration(block) { it.logWithHorizontal(block()) }
        ResourceLocation("c", "stripped_logs").toBlockTag().generator.registerChild(block)
        ResourceLocation("c", "stripped_logs").toItemTag().generator.registerChild(item)
        initStripped(HaimeviskaBlockCard.LOG.block)
    }
}

class HaimeviskaWoodBlockCard(configuration: WoodBlockConfiguration) : AbstractHaimeviskaLogBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = super.createSettings().mapColor { MapColor.TERRACOTTA_ORANGE }
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = RotatedPillarBlock(properties)

    context(ModContext)
    override fun init() {
        super.init()
        registerModelGeneration(HaimeviskaBlockCard.LOG.block) { it.wood(block()) }
        initWood(HaimeviskaBlockCard.LOG.item)
    }
}

class HaimeviskaStrippedWoodBlockCard(configuration: WoodBlockConfiguration) : AbstractHaimeviskaLogBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = super.createSettings().mapColor { MapColor.RAW_IRON }
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = RotatedPillarBlock(properties)

    context(ModContext)
    override fun init() {
        super.init()
        registerModelGeneration(HaimeviskaBlockCard.STRIPPED_LOG.block) { it.wood(block()) }
        ResourceLocation("c", "stripped_woods").toBlockTag().generator.registerChild(block)
        ResourceLocation("c", "stripped_woods").toItemTag().generator.registerChild(item)
        initStripped(HaimeviskaBlockCard.WOOD.block)
        initWood(HaimeviskaBlockCard.STRIPPED_LOG.item)
    }
}

@Suppress("OVERRIDE_DEPRECATION")
class HaimeviskaLogBlock(settings: Properties) : RotatedPillarBlock(settings) {
    companion object {
        val CODEC: MapCodec<HaimeviskaLogBlock> = simpleCodec(::HaimeviskaLogBlock)
    }

    override fun codec() = CODEC

    override fun useItemOn(stack: ItemStack, state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, hitResult: BlockHitResult): ItemInteractionResult {
        if (state[AXIS] != Direction.Axis.Y) @Suppress("DEPRECATION") return super.useItemOn(stack, state, level, pos, player, hand, hitResult) // 縦方向でなければスルー
        if (stack isNotIn ItemTags.SWORDS) @Suppress("DEPRECATION") return super.useItemOn(stack, state, level, pos, player, hand, hitResult) // 剣でなければスルー
        if (level.isClientSide) return ItemInteractionResult.SUCCESS
        val direction = if (hitResult.direction.axis === Direction.Axis.Y) player.direction.opposite else hitResult.direction

        // 加工
        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand))
        level.setBlock(pos, HaimeviskaBlockCard.INCISED_LOG.block().defaultBlockState().with(HorizontalDirectionalBlock.FACING, direction), UPDATE_ALL or UPDATE_IMMEDIATE)
        player.awardStat(Stats.ITEM_USED.get(stack.item))

        // エフェクト
        level.playSound(null, pos, SoundEvents.PUMPKIN_CARVE, SoundSource.BLOCKS, 1.0F, 1.0F)

        return ItemInteractionResult.CONSUME
    }
}
