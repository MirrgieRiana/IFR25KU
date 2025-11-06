package miragefairy2024.mod.tool.items

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModifyItemEnchantmentsHandler
import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.mod.tool.ToolMaterialCard
import miragefairy2024.util.Translation
import miragefairy2024.util.blockVisitor
import miragefairy2024.util.durability
import miragefairy2024.util.invoke
import miragefairy2024.util.notEmptyOrNull
import miragefairy2024.util.text
import miragefairy2024.util.yellow
import net.minecraft.core.BlockBox
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.network.chat.Component
import net.minecraft.stats.Stats
import net.minecraft.tags.ItemTags
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Tier
import net.minecraft.world.item.TieredItem
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.ItemEnchantments
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.HitResult

open class FairyBuildersRodConfiguration(
    override val toolMaterialCard: ToolMaterialCard,
) : ToolConfiguration() {
    override fun createItem(properties: Item.Properties) = FairyBuildersRodItem(this, properties)

    init {
        this.tags += ItemTags.DURABILITY_ENCHANTABLE
        this.miningDamage = 2
    }
}

class FairyBuildersRodItem(override val configuration: FairyBuildersRodConfiguration, settings: Properties) :
    BuildersRodItem(configuration.toolMaterialCard.toolMaterial, settings),
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

open class BuildersRodItem(toolMaterial: Tier, settings: Properties) : TieredItem(toolMaterial, settings) {
    companion object {
        val DESCRIPTION_TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("builders_rod").toLanguageKey()}.description" }, "Place blocks when used", "使用時、ブロックを設置")
    }

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltipComponents: MutableList<Component>, tooltipFlag: TooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)
        tooltipComponents += text { DESCRIPTION_TRANSLATION().yellow }
    }

    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        val toolItemStack = player.getItemInHand(usedHand)

        val blockItemStack = when (usedHand) {
            InteractionHand.MAIN_HAND -> player.offhandItem
            InteractionHand.OFF_HAND -> player.mainHandItem
        }.notEmptyOrNull ?: return InteractionResultHolder.fail(toolItemStack) // 逆の手が空
        val blockItem = blockItemStack.item as? BlockItem ?: return InteractionResultHolder.fail(toolItemStack) // 逆の手がブロックアイテムでない
        if (!blockItem.block.isEnabled(level.enabledFeatures())) return InteractionResultHolder.fail(toolItemStack) // ブロックが無効化されている

        val blockHitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE)
        if (blockHitResult.type != HitResult.Type.BLOCK) return InteractionResultHolder.fail(toolItemStack) // ブロックをタゲっていない

        val targetBlockState = level.getBlockState(blockHitResult.blockPos)
        val frontBlockPos = blockHitResult.blockPos.relative(blockHitResult.direction)
        val wallDirection = blockHitResult.direction.opposite

        val range = 10
        val region = BlockBox.of(
            frontBlockPos.offset(-range, -range, -range),
            frontBlockPos.offset(range, range, range),
        )

        val sequence = blockVisitor(listOf(frontBlockPos)) { _, _, airBlockPos ->
            if (airBlockPos !in region) return@blockVisitor false // 範囲外

            val wallBlockPos = airBlockPos.relative(wallDirection)
            val wallBlockState = level.getBlockState(wallBlockPos)
            if (wallBlockState != targetBlockState) return@blockVisitor false // 壁が対象ブロックでない

            val context = BlockPlaceContext(player, usedHand, blockItemStack, blockHitResult.withPosition(airBlockPos))
            if (!context.canPlace()) return@blockVisitor false // 設置先が埋まっている

            true
        }

        var count = 0
        run finish@{
            sequence.forEach next@{ (_, airBlockPos) ->
                val context = BlockPlaceContext(player, usedHand, blockItemStack, blockHitResult.withPosition(airBlockPos))

                val result = blockItem.place(context)
                if (result == InteractionResult.FAIL || result == InteractionResult.PASS) return@next // 設置失敗

                // 成功

                count++

                // ツールの使用
                toolItemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(usedHand))
                player.awardStat(Stats.ITEM_USED.get(this))

                if (blockItemStack.isEmpty) return@finish false // ブロックが枯渇
                if (toolItemStack.isEmpty || toolItemStack.durability <= 1) return@finish false // ツールの耐久が枯渇
            }
        }

        return if (count > 0) InteractionResultHolder.success(toolItemStack) else InteractionResultHolder.fail(toolItemStack)
    }

    override fun hurtEnemy(stack: ItemStack, target: LivingEntity, attacker: LivingEntity): Boolean {
        return true
    }

    override fun postHurtEnemy(stack: ItemStack, target: LivingEntity, attacker: LivingEntity) {
        stack.hurtAndBreak(2, attacker, EquipmentSlot.MAINHAND)
    }
}
