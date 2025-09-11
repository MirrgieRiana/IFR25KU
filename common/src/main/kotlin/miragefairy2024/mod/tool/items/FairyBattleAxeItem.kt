package miragefairy2024.mod.tool.items

import miragefairy2024.ModifyItemEnchantmentsHandler
import miragefairy2024.mod.tool.FairyMiningToolConfiguration
import miragefairy2024.mod.tool.ToolMaterialCard
import miragefairy2024.mod.tool.axeAttackDamageBonus
import mirrg.kotlin.helium.atLeast
import mirrg.kotlin.helium.atMost
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.AxeItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Tier
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.ItemEnchantments
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

open class FairyBattleAxeConfiguration(
    override val toolMaterialCard: ToolMaterialCard,
) : FairyMiningToolConfiguration() {
    override fun createItem(properties: Item.Properties) = FairyBattleAxeItem(this, properties)

    init {
        this.attackDamage = -toolMaterialCard.toolMaterial.attackDamageBonus + 7F + toolMaterialCard.toolMaterial.axeAttackDamageBonus
        // 素材の採掘速度が4のとき-3.2、8のとき-3.0で線形補間
        // それ以外は平坦
        val a = ((toolMaterialCard.toolMaterial.speed atLeast 4F atMost 8F) - 4F) / 4F
        this.attackSpeed = -3.2F + 0.2F * a
        this.miningDamage = 2
        this.tags += ItemTags.AXES
        this.effectiveBlockTags += BlockTags.MINEABLE_WITH_AXE
    }
}

class FairyBattleAxeItem(override val configuration: FairyBattleAxeConfiguration, settings: Properties) :
    BattleAxeItem(configuration.toolMaterialCard.toolMaterial, configuration.attackDamage, configuration.attackSpeed, settings),
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

open class BattleAxeItem(toolMaterial: Tier, attackDamage: Float, attackSpeed: Float, settings: Properties) : AxeItem(toolMaterial, settings.attributes(createAttributes(toolMaterial, attackDamage, attackSpeed))) {
    override fun postHurtEnemy(stack: ItemStack, target: LivingEntity, attacker: LivingEntity) {
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND)
    }
}
