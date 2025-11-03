package miragefairy2024.mod.tool.items

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModifyItemEnchantmentsHandler
import miragefairy2024.mod.enchantment.EnchantmentCard
import miragefairy2024.mod.enchantment.SCYTHE_ITEM_TAG
import miragefairy2024.mod.magicplant.MagicPlantBlock
import miragefairy2024.mod.magicplant.PostTryPickHandlerItem
import miragefairy2024.mod.tool.FairyMiningToolConfiguration
import miragefairy2024.mod.tool.ToolMaterialCard
import miragefairy2024.mod.tool.effects.areaMining
import miragefairy2024.mod.tool.effects.enchantment
import miragefairy2024.util.Translation
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import miragefairy2024.util.toRomanText
import miragefairy2024.util.yellow
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.SwordItem
import net.minecraft.world.item.Tier
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.ItemEnchantments
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.CaveVines
import net.minecraft.world.level.block.SweetBerryBushBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

open class FairyScytheConfiguration(
    override val toolMaterialCard: ToolMaterialCard,
    private val range: Int = 1
) : FairyMiningToolConfiguration() {
    override fun createItem(properties: Item.Properties) = FairyScytheItem(this, range, properties)

    init {
        this.attackDamage = 4.0F
        this.attackSpeed = -3.2F
        this.magicMiningDamage = 0.2
        this.areaMining(range, range, range)
        this.enchantment(EnchantmentCard.AREA_MINING_ACCELERATION.key, 3)
        this.tags += ItemTags.SWORDS
        this.tags += SCYTHE_ITEM_TAG
        this.superEffectiveBlocks += Blocks.COBWEB
        this.effectiveBlockTags += BlockTags.SWORD_EFFICIENT
    }
}

class FairyScytheItem(override val configuration: FairyScytheConfiguration, range: Int, settings: Properties) :
    ScytheItem(configuration.toolMaterialCard.toolMaterial, configuration.attackDamage, configuration.attackSpeed, range, settings),
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

open class ScytheItem(material: Tier, attackDamage: Float, attackSpeed: Float, private val range: Int, settings: Properties) : SwordItem(material, settings.attributes(createAttributes(material, attackDamage.toInt(), attackSpeed))), PostTryPickHandlerItem {
    companion object {
        val DESCRIPTION_TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("scythe").toLanguageKey()}.description" }, "Perform area harvesting when used %s", "使用時、範囲収穫 %s")
    }

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltipComponents: MutableList<Component>, tooltipFlag: TooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)
        tooltipComponents += text { DESCRIPTION_TRANSLATION(range.toRomanText()).yellow }
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {

        if (!user.isShiftKeyDown) {
            val itemStack = user.getItemInHand(hand)
            val blockHitResult = getPlayerPOVHitResult(world, user, ClipContext.Fluid.NONE)
            val blockPos = blockHitResult.blockPos
            var effective = false
            // TODO 貫通判定
            (-range..range).forEach { x ->
                (-range..range).forEach { y ->
                    (-range..range).forEach { z ->
                        val targetBlockPos = blockPos.offset(x, y, z)
                        val targetBlockState = world.getBlockState(targetBlockPos)
                        when (val targetBlock = targetBlockState.block) {
                            is MagicPlantBlock -> {
                                val result = targetBlock.tryPick(world, targetBlockPos, user, itemStack, true, false)
                                if (result) effective = true
                            }

                            is SweetBerryBushBlock, is CaveVines -> {
                                val result = targetBlockState.useWithoutItem(world, user, BlockHitResult(blockHitResult.location.add(x.toDouble(), y.toDouble(), z.toDouble()), blockHitResult.direction, targetBlockPos, false))
                                if (result.consumesAction()) effective = true
                            }
                        }
                    }
                }
            }
            if (effective) return InteractionResultHolder.success(itemStack)
        }

        return super.use(world, user, hand)
    }

    override fun postHurtEnemy(stack: ItemStack, target: LivingEntity, attacker: LivingEntity) {
        stack.hurtAndBreak(2, attacker, EquipmentSlot.MAINHAND)
    }

    override fun mineBlock(stack: ItemStack, world: Level, state: BlockState, pos: BlockPos, miner: LivingEntity): Boolean {
        val tool = stack.get(DataComponents.TOOL) ?: return false
        if (!world.isClientSide && tool.damagePerBlock > 0) {
            val damageRate = if (state.getDestroySpeed(world, pos) != 0.0F) 0.5F else 0.1F
            if (miner.random.nextFloat() < damageRate) {
                stack.hurtAndBreak(tool.damagePerBlock, miner, EquipmentSlot.MAINHAND)
            }
        }
        return true
    }

    override fun postTryPick(world: Level, blockPos: BlockPos, player: Player?, itemStack: ItemStack, succeed: Boolean) {
        if (world.isClientSide) return
        if (player?.isShiftKeyDown == true) return
        (-range..range).forEach { x ->
            (-range..range).forEach { y ->
                (-range..range).forEach { z ->
                    if (x != 0 || y != 0 || z != 0) {
                        val targetBlockPos = blockPos.offset(x, y, z)
                        val targetBlockState = world.getBlockState(targetBlockPos)
                        val targetBlock = targetBlockState.block
                        if (targetBlock is MagicPlantBlock) {
                            targetBlock.tryPick(world, targetBlockPos, player, itemStack, true, false)
                        }
                    }
                }
            }
        }
    }
}
