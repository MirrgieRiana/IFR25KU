package miragefairy2024.mod.haimeviska.cards

import com.mojang.serialization.MapCodec
import miragefairy2024.ModContext
import miragefairy2024.lib.SimpleHorizontalFacingBlock
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.mod.haimeviska.HaimeviskaBlockConfiguration
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.particle.ParticleTypeCard
import miragefairy2024.mod.registerHarvestNotation
import miragefairy2024.util.ItemLootPoolEntry
import miragefairy2024.util.LootPool
import miragefairy2024.util.LootTable
import miragefairy2024.util.createItemStack
import miragefairy2024.util.get
import miragefairy2024.util.randomInt
import miragefairy2024.util.registerLootTableGeneration
import mirrg.kotlin.helium.atMost
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.Registries
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
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition
import net.minecraft.world.phys.BlockHitResult

class HaimeviskaDrippingLogBlockCard(configuration: HaimeviskaBlockConfiguration) : HaimeviskaHorizontalFacingLogBlockCard(configuration) {
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = DrippingHaimeviskaLogBlock(properties)

    context(ModContext)
    override fun init() {
        super.init()

        block.registerLootTableGeneration { provider, registries ->
            LootTable(
                LootPool(ItemLootPoolEntry(item())) {
                    `when`(provider.hasSilkTouch())
                },
                LootPool(ItemLootPoolEntry(LOG.item())) {
                    `when`(provider.doesNotHaveSilkTouch())
                },
                LootPool(ItemLootPoolEntry(MaterialCard.HAIMEVISKA_SAP.item()) {
                    apply(ApplyBonusCount.addUniformBonusCount(registries[Registries.ENCHANTMENT, Enchantments.FORTUNE]))
                }) {
                    `when`(provider.doesNotHaveSilkTouch())
                },
                LootPool(ItemLootPoolEntry(MaterialCard.HAIMEVISKA_ROSIN.item()) {
                    apply(ApplyBonusCount.addUniformBonusCount(registries[Registries.ENCHANTMENT, Enchantments.FORTUNE], 2))
                }) {
                    `when`(provider.doesNotHaveSilkTouch())
                    `when`(LootItemRandomChanceCondition.randomChance(0.01F))
                },
            ) {
                provider.applyExplosionDecay(block(), this)
            }
        }
        item.registerHarvestNotation(MaterialCard.HAIMEVISKA_SAP.item, MaterialCard.HAIMEVISKA_ROSIN.item)

    }
}

@Suppress("OVERRIDE_DEPRECATION")
class DrippingHaimeviskaLogBlock(settings: Properties) : SimpleHorizontalFacingBlock(settings) {
    companion object {
        val CODEC: MapCodec<DrippingHaimeviskaLogBlock> = simpleCodec(::DrippingHaimeviskaLogBlock)
    }

    override fun codec() = CODEC

    override fun useItemOn(stack: ItemStack, state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, hitResult: BlockHitResult): ItemInteractionResult {
        if (level.isClientSide) return ItemInteractionResult.SUCCESS
        val direction = state[FACING]

        // 消費
        level.setBlock(pos, HaimeviskaBlockCard.INCISED_LOG.block().defaultBlockState().setValue(FACING, direction), UPDATE_ALL or UPDATE_IMMEDIATE)

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
        drop(MaterialCard.HAIMEVISKA_SAP.item(), 1.0 + 0.25 * fortune) // ハイメヴィスカの樹液
        drop(MaterialCard.HAIMEVISKA_ROSIN.item(), 0.03 + 0.01 * fortune) // ハイメヴィスカの涙

        // エフェクト
        level.playSound(null, pos, SoundEvents.SLIME_JUMP, SoundSource.BLOCKS, 0.75F, 1.0F + 0.5F * level.random.nextFloat())

        return ItemInteractionResult.CONSUME
    }

    override fun animateTick(state: BlockState, world: Level, pos: BlockPos, random: RandomSource) {
        if (random.nextFloat() >= 0.2F) return

        val direction = state[FACING]
        val destBlockPos = pos.relative(direction)
        val destBlockState = world.getBlockState(destBlockPos)
        val destShape = destBlockState.getCollisionShape(world, destBlockPos)
        val hasSpace = when (direction) {
            Direction.NORTH -> destShape.max(Direction.Axis.Z) < 1.0
            Direction.SOUTH -> destShape.min(Direction.Axis.Z) > 0.0
            Direction.WEST -> destShape.max(Direction.Axis.X) < 1.0
            Direction.EAST -> destShape.min(Direction.Axis.X) > 0.0
            else -> throw IllegalStateException()
        }
        if (!(hasSpace || !destBlockState.isCollisionShapeFullBlock(world, destBlockPos))) return

        val position = random.nextInt(2)
        val x = when (position) {
            0 -> (7.0 + 7.0 * world.random.nextDouble()) / 16.0
            else -> (2.0 + 8.0 * world.random.nextDouble()) / 16.0
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

        world.addParticle(
            ParticleTypeCard.DRIPPING_HAIMEVISKA_SAP.particleType,
            pos.x + x2,
            pos.y + y - 1.0 / 16.0,
            pos.z + z2,
            0.0,
            0.0,
            0.0,
        )
    }
}
