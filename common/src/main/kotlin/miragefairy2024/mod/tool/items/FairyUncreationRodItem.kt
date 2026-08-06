package miragefairy2024.mod.tool.items

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModifyItemEnchantmentsHandler
import miragefairy2024.mod.common.RenderBlockPosesOutlineContext
import miragefairy2024.mod.common.RenderBlockPosesOutlineListenerItem
import miragefairy2024.mod.enchantment.EnchantmentCard
import miragefairy2024.mod.enchantment.UNCREATION_ROD_ITEM_TAG
import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.mod.tool.ToolMaterialCard
import miragefairy2024.util.Translation
import miragefairy2024.util.blockVisitor
import miragefairy2024.util.breakBlockByMagic
import miragefairy2024.util.durability
import miragefairy2024.util.get
import miragefairy2024.util.getLevel
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import miragefairy2024.util.yellow
import net.minecraft.core.BlockBox
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.stats.Stats
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Tier
import net.minecraft.world.item.TieredItem
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.ItemEnchantments
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult

open class FairyUncreationRodConfiguration(
    override val toolMaterialCard: ToolMaterialCard,
    val range: Int,
) : ToolConfiguration() {
    override fun createItem(properties: Item.Properties) = FairyUncreationRodItem(this, properties)

    init {
        this.tags += UNCREATION_ROD_ITEM_TAG
        this.miningDamage = 2
    }
}

class FairyUncreationRodItem(override val configuration: FairyUncreationRodConfiguration, settings: Properties) :
    UncreationRodItem(configuration.toolMaterialCard.toolMaterial, configuration.range, settings),
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

open class UncreationRodItem(toolMaterial: Tier, private val range: Int, settings: Properties) : TieredItem(toolMaterial, settings), RenderBlockPosesOutlineListenerItem {
    companion object {
        val DESCRIPTION_TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("uncreation_rod").toLanguageKey()}.description" }, "Break blocks when used", "使用時、ブロックを破壊")
        val DESCRIPTION_SNEAKING_USE_TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("uncreation_rod").toLanguageKey()}.description.sneaking_use" }, "While sneaking: Ignore block states", "スニーク中は状態の違いを無視")
    }

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltipComponents: MutableList<Component>, tooltipFlag: TooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)
        tooltipComponents += text { DESCRIPTION_TRANSLATION().yellow }
        tooltipComponents += text { DESCRIPTION_SNEAKING_USE_TRANSLATION().yellow }
    }

    /**
     * 与えられた面と地続きになっている、同種のブロックの位置を返すのだ～🌱
     * 同種の判定は通常はblockstateの完全一致だけど、[player]がスニーク中はブロックの種類だけを見るのだ～🌱
     * 探索できる範囲は、[toolItemStack]に付いた側方範囲採掘のレベルの分だけ広がるのだ～🌱
     *
     * 探索のノードは面の手前側のマスで、そこから奥のブロックを見るのだ～🌱
     * 判定に落ちたマスは[blockVisitor]の探索済み集合に入らず、世界が変化すると再び判定にかけられてしまうから、
     * 破壊しながら探索を進めるのではなく、先にすべての対象を確定させるのだ～🌱
     */
    fun getDestinationBlockPoses(level: Level, player: Player, toolItemStack: ItemStack, blockHitResult: BlockHitResult): Sequence<BlockPos> {

        val targetBlockState = level.getBlockState(blockHitResult.blockPos)
        val frontBlockPos = blockHitResult.blockPos.relative(blockHitResult.direction)
        val wallDirection = blockHitResult.direction.opposite
        val ignoresBlockStateProperties = player.isShiftKeyDown // スニーク中は向きや雪の有無などの違いで面が途切れないのだ～🌱
        val lateralLevel = level.registryAccess()[Registries.ENCHANTMENT, EnchantmentCard.LATERAL_AREA_MINING.key].getLevel(toolItemStack)
        val actualRange = range + lateralLevel

        val region = when (blockHitResult.direction) {
            Direction.WEST, Direction.EAST -> BlockBox.of(
                frontBlockPos.offset(0, -actualRange, -actualRange),
                frontBlockPos.offset(0, actualRange, actualRange),
            )

            Direction.DOWN, Direction.UP -> BlockBox.of(
                frontBlockPos.offset(-actualRange, 0, -actualRange),
                frontBlockPos.offset(actualRange, 0, actualRange),
            )

            Direction.NORTH, Direction.SOUTH -> BlockBox.of(
                frontBlockPos.offset(-actualRange, -actualRange, 0),
                frontBlockPos.offset(actualRange, actualRange, 0),
            )
        }

        return blockVisitor(listOf(frontBlockPos)) { _, _, airBlockPos ->
            if (airBlockPos !in region) return@blockVisitor false // 範囲外

            val wallBlockPos = airBlockPos.relative(wallDirection)
            val wallBlockState = level.getBlockState(wallBlockPos)
            val isTargetBlock = if (ignoresBlockStateProperties) wallBlockState.block === targetBlockState.block else wallBlockState == targetBlockState
            if (!isTargetBlock) return@blockVisitor false // 壁が対象ブロックでない

            if (level.getBlockState(airBlockPos).isSolidRender(level, airBlockPos)) return@blockVisitor false // 壁の手前が塞がっている

            true
        }.map { it.second.relative(wallDirection) }
    }

    override fun getBlockPoses(hand: InteractionHand, context: RenderBlockPosesOutlineContext): Pair<BlockPos, Set<BlockPos>>? {

        val toolItemStack = context.player.getItemInHand(hand)

        val blockHitResult = getPlayerPOVHitResult(context.level, context.player, ClipContext.Fluid.NONE)
        if (blockHitResult.type != HitResult.Type.BLOCK) return null // ブロックをタゲっていない

        val sequence = getDestinationBlockPoses(context.level, context.player, toolItemStack, blockHitResult)

        return Pair(
            blockHitResult.blockPos.relative(blockHitResult.direction),
            sequence.toSet(),
        )
    }

    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        val toolItemStack = player.getItemInHand(usedHand)

        val blockHitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE)
        if (blockHitResult.type != HitResult.Type.BLOCK) return InteractionResultHolder.fail(toolItemStack) // ブロックをタゲっていない

        val sequence = getDestinationBlockPoses(level, player, toolItemStack, blockHitResult)

        if (player !is ServerPlayer) return InteractionResultHolder.success(toolItemStack) // 破壊はサーバー側でのみ行う

        var count = 0
        run finish@{
            sequence.forEach next@{ targetBlockPos ->

                if (!breakBlockByMagic(toolItemStack, level, targetBlockPos, player)) return@next // 破壊失敗

                // 成功

                count++

                // ツールの使用
                toolItemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(usedHand))
                player.awardStat(Stats.ITEM_USED.get(this))

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
