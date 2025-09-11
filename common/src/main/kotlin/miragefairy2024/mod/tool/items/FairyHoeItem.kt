package miragefairy2024.mod.tool.items

import miragefairy2024.ModifyItemEnchantmentsHandler
import miragefairy2024.mod.tool.FairyMiningToolConfiguration
import miragefairy2024.mod.tool.ToolMaterialCard
import mirrg.kotlin.helium.atMost
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.HoeItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.Tier
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.ItemEnchantments
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.FarmBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.gameevent.GameEvent
import java.util.function.Consumer
import java.util.function.Predicate

open class FairyHoeConfiguration(
    override val toolMaterialCard: ToolMaterialCard,
) : FairyMiningToolConfiguration() {
    override fun createItem(properties: Item.Properties) = FairyHoeItem(this, properties)

    var tillingRecipe: TillingRecipe? = null

    init {
        this.attackDamage = -toolMaterialCard.toolMaterial.attackDamageBonus
        this.attackSpeed = -4F + (toolMaterialCard.toolMaterial.attackDamageBonus + 1F) atMost 0F
        this.tags += ItemTags.HOES
        this.effectiveBlockTags += BlockTags.MINEABLE_WITH_HOE
    }
}

class FairyHoeItem(override val configuration: FairyHoeConfiguration, settings: Properties) :
    AdvancedHoeItem(configuration.toolMaterialCard.toolMaterial, configuration.tillingRecipe ?: VANILLA_RECIPE, settings.attributes(createAttributes(configuration.toolMaterialCard.toolMaterial, configuration.attackDamage, configuration.attackSpeed))),
    FairyToolItem,
    ModifyItemEnchantmentsHandler {

    override fun mineBlock(stack: ItemStack, world: Level, state: BlockState, pos: BlockPos, miner: LivingEntity): Boolean {
        super.mineBlock(stack, world, state, pos, miner)
        postMineImpl(stack, world, state, pos, miner)
        return true
    }

    override fun hurtEnemy(stack: ItemStack, target: LivingEntity, attacker: LivingEntity): Boolean {
        super.hurtEnemy(stack, target, attacker)
        postHitImpl(stack, target, attacker)
        return true
    }

    override fun inventoryTick(stack: ItemStack, world: Level, entity: Entity, slot: Int, selected: Boolean) {
        super.inventoryTick(stack, world, entity, slot, selected)
        inventoryTickImpl(stack, world, entity, slot, selected)
    }

    override fun modifyItemEnchantments(itemStack: ItemStack, mutableItemEnchantments: ItemEnchantments.Mutable, enchantmentLookup: HolderLookup.RegistryLookup<Enchantment>) = modifyItemEnchantmentsImpl(itemStack, mutableItemEnchantments, enchantmentLookup)

    override fun isFoil(stack: ItemStack) = super.isFoil(stack) || hasGlintImpl(stack)

}

interface TillingRecipe {
    fun test(context: UseOnContext, blockState: BlockState): Consumer<UseOnContext>?
    fun useOnEntity(stack: ItemStack, player: Player, interactionTarget: LivingEntity, usedHand: InteractionHand) = InteractionResult.PASS
}

class MapTillingRecipe(private val map: Map<Block, com.mojang.datafixers.util.Pair<Predicate<UseOnContext>, Consumer<UseOnContext>>>) : TillingRecipe {
    override fun test(context: UseOnContext, blockState: BlockState): Consumer<UseOnContext>? {
        val pair = map[blockState.block] ?: return null
        if (!pair.first.test(context)) return null
        return pair.second
    }
}

open class AdvancedHoeItem(toolMaterial: Tier, private val tillingRecipe: TillingRecipe, settings: Properties) : HoeItem(toolMaterial, settings) {
    companion object {
        val VANILLA_RECIPE = MapTillingRecipe(TILLABLES)
        val ROUGHEN_RECIPE = MapTillingRecipe(
            mapOf(
                Blocks.FARMLAND to com.mojang.datafixers.util.Pair.of(Predicate { true }, changeIntoState(Blocks.DIRT.defaultBlockState())),
                Blocks.GRASS_BLOCK to com.mojang.datafixers.util.Pair.of(Predicate { true }, changeIntoState(Blocks.DIRT.defaultBlockState())),
                Blocks.MYCELIUM to com.mojang.datafixers.util.Pair.of(Predicate { true }, changeIntoState(Blocks.DIRT.defaultBlockState())),
                Blocks.PODZOL to com.mojang.datafixers.util.Pair.of(Predicate { true }, changeIntoState(Blocks.DIRT.defaultBlockState())),
                Blocks.DIRT_PATH to com.mojang.datafixers.util.Pair.of(Predicate { true }, changeIntoState(Blocks.DIRT.defaultBlockState())),
                Blocks.ROOTED_DIRT to com.mojang.datafixers.util.Pair.of(Predicate { true }, changeIntoStateAndDropItem(Blocks.DIRT.defaultBlockState(), Items.HANGING_ROOTS)),
                Blocks.DIRT to com.mojang.datafixers.util.Pair.of(Predicate { true }, changeIntoState(Blocks.COARSE_DIRT.defaultBlockState())),
                Blocks.CRIMSON_NYLIUM to com.mojang.datafixers.util.Pair.of(Predicate { true }, changeIntoState(Blocks.NETHERRACK.defaultBlockState())),
                Blocks.WARPED_NYLIUM to com.mojang.datafixers.util.Pair.of(Predicate { true }, changeIntoState(Blocks.NETHERRACK.defaultBlockState())),
                Blocks.MOSSY_COBBLESTONE to com.mojang.datafixers.util.Pair.of(Predicate { true }, changeIntoState(Blocks.COBBLESTONE.defaultBlockState())),
                Blocks.MOSSY_STONE_BRICKS to com.mojang.datafixers.util.Pair.of(Predicate { true }, changeIntoState(Blocks.STONE_BRICKS.defaultBlockState())),
                Blocks.INFESTED_MOSSY_STONE_BRICKS to com.mojang.datafixers.util.Pair.of(Predicate { true }, changeIntoState(Blocks.INFESTED_STONE_BRICKS.defaultBlockState())),
                Blocks.MUDDY_MANGROVE_ROOTS to com.mojang.datafixers.util.Pair.of(Predicate { true }, changeIntoStateAndDropItem(Blocks.MUD.defaultBlockState(), Items.MANGROVE_ROOTS)),
            )
        )
        val CREATIVE_RECIPE = object : TillingRecipe {
            private val target = Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE)
            override fun test(context: UseOnContext, blockState: BlockState): Consumer<UseOnContext>? {
                if (blockState == target) return null
                return changeIntoState(target)
            }

            override fun useOnEntity(stack: ItemStack, player: Player, interactionTarget: LivingEntity, usedHand: InteractionHand): InteractionResult {
                val level = interactionTarget.level()
                if (level.isClientSide()) return InteractionResult.sidedSuccess(level.isClientSide)
                val blockPos = interactionTarget.blockPosition()
                level.setBlock(blockPos, target, 11)
                level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(player, target))
                interactionTarget.discard()
                level.playSound(null, blockPos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F)
                return InteractionResult.sidedSuccess(level.isClientSide)
            }
        }
    }

    override fun useOn(context: UseOnContext): InteractionResult {
        val player = context.player
        if (player?.isShiftKeyDown == true) return super.useOn(context)
        val level = context.level
        val blockPos = context.clickedPos
        val result = tillingRecipe.test(context, level.getBlockState(blockPos)) ?: return InteractionResult.PASS
        level.playSound(player, blockPos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F)
        if (level.isClientSide) return InteractionResult.sidedSuccess(level.isClientSide)
        result.accept(context)
        if (player != null) context.itemInHand.hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.hand))
        return InteractionResult.sidedSuccess(level.isClientSide)
    }

    override fun interactLivingEntity(stack: ItemStack, player: Player, interactionTarget: LivingEntity, usedHand: InteractionHand): InteractionResult {
        return tillingRecipe.useOnEntity(stack, player, interactionTarget, usedHand)
    }
}
