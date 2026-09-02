package miragefairy2024.mod.tree.contents

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.lib.SimpleHorizontalFacingBlock
import miragefairy2024.mod.particle.ParticleTypeCard
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.util.createItemStack
import miragefairy2024.util.get
import miragefairy2024.util.randomInt
import miragefairy2024.util.with
import mirrg.kotlin.helium.atMost
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

@Suppress("OVERRIDE_DEPRECATION")
class DrippingLogBlock(private val incisedLog: () -> TreeBlockCard, private val sap: () -> Item, private val rosin: () -> Item, settings: Properties) : SimpleHorizontalFacingBlock(settings) {
    companion object {
        val CODEC: MapCodec<DrippingLogBlock> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ResourceLocation.CODEC.xmap<() -> TreeBlockCard>(
                    { identifier -> { TreeBlockCard.entries.first { it.identifier == identifier } } },
                    { it().identifier },
                ).fieldOf("incised_log").forGetter { it.incisedLog },
                BuiltInRegistries.ITEM.byNameCodec().xmap<() -> Item>({ item -> { item } }, { it() }).fieldOf("sap").forGetter { it.sap },
                BuiltInRegistries.ITEM.byNameCodec().xmap<() -> Item>({ item -> { item } }, { it() }).fieldOf("rosin").forGetter { it.rosin },
                propertiesCodec(),
            ).apply(instance, ::DrippingLogBlock)
        }
    }

    override fun codec() = CODEC

    override fun useItemOn(stack: ItemStack, state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, hitResult: BlockHitResult): ItemInteractionResult {
        if (level.isClientSide) return ItemInteractionResult.SUCCESS
        val direction = state[FACING]

        // 消費
        level.setBlock(pos, incisedLog().block().defaultBlockState().with(FACING, direction), UPDATE_ALL or UPDATE_IMMEDIATE)

        fun drop(item: Item, count: Double) {
            val actualCount = level.random.randomInt(count) atMost item.defaultMaxStackSize
            if (actualCount <= 0) return
            val itemStack = item.createItemStack(actualCount)
            val itemEntity = ItemEntity(level, pos.x + 0.5 + direction.stepX * 0.65, pos.y + 0.1, pos.z + 0.5 + direction.stepZ * 0.65, itemStack)
            itemEntity.setDeltaMovement(0.05 * direction.stepX + level.random.nextDouble() * 0.02, 0.05, 0.05 * direction.stepZ + level.random.nextDouble() * 0.02)
            level.addFreshEntity(itemEntity)
        }

        // 生産
        val fortune = EnchantmentHelper.getItemEnchantmentLevel(level.registryAccess()[Registries.ENCHANTMENT, Enchantments.FORTUNE], stack)
        drop(sap(), 1.0 + 0.25 * fortune) // 樹液
        drop(rosin(), 0.03 + 0.01 * fortune) // 涙

        // エフェクト
        level.playSound(null, pos, SoundEvents.SLIME_JUMP, SoundSource.BLOCKS, 0.75F, 1.0F + 0.5F * level.random.nextFloat())

        return ItemInteractionResult.CONSUME
    }

    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        if (random.nextFloat() >= 0.2F) return

        val direction = state[FACING]
        val destBlockPos = pos.relative(direction)
        val destBlockState = level.getBlockState(destBlockPos)
        val destShape = destBlockState.getCollisionShape(level, destBlockPos)
        val hasSpace = when (direction) {
            Direction.NORTH -> destShape.max(Direction.Axis.Z) < 1.0
            Direction.SOUTH -> destShape.min(Direction.Axis.Z) > 0.0
            Direction.WEST -> destShape.max(Direction.Axis.X) < 1.0
            Direction.EAST -> destShape.min(Direction.Axis.X) > 0.0
            else -> throw IllegalStateException()
        }
        if (!(hasSpace || !destBlockState.isCollisionShapeFullBlock(level, destBlockPos))) return

        val position = random.nextInt(2)
        val x = when (position) {
            0 -> (7.0 + 7.0 * level.random.nextDouble()) / 16.0
            else -> (2.0 + 8.0 * level.random.nextDouble()) / 16.0
        }
        val y = when (position) {
            0 -> 12.0 / 16.0
            else -> 5.0 / 16.0
        }
        val z = 17.0 / 16.0

        val (x2, z2) = when (direction) {
            Direction.NORTH -> Pair(1.0 - x, 1.0 - z)
            Direction.EAST -> Pair(0.0 + z, 1.0 - x)
            Direction.SOUTH -> Pair(0.0 + x, 0.0 + z)
            Direction.WEST -> Pair(1.0 - z, 0.0 + x)
            else -> throw IllegalStateException()
        }

        level.addParticle(
            ParticleTypeCard.DRIPPING_SAP.particleType,
            pos.x + x2,
            pos.y + y - 1.0 / 16.0,
            pos.z + z2,
            0.0,
            0.0,
            0.0,
        )
    }
}
