package miragefairy2024.mod.tool.items

import miragefairy2024.ModifyItemEnchantmentsHandler
import miragefairy2024.mod.tool.FairyMiningToolConfiguration
import miragefairy2024.mod.tool.ToolMaterialCard
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.SwordItem
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.ItemEnchantments
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState

open class FairySwordConfiguration(
    override val toolMaterialCard: ToolMaterialCard,
) : FairyMiningToolConfiguration() {
    override fun createItem(properties: Item.Properties) = FairySwordItem(this, properties)

    init {
        this.attackDamage = 3.0F
        this.attackSpeed = -2.4F
        this.miningDamage = 2
        this.miningSpeedMultiplierOverride = 1.5F
        this.tags += ItemTags.SWORDS
        this.superEffectiveBlocks += Blocks.COBWEB
        this.effectiveBlockTags += BlockTags.SWORD_EFFICIENT
    }
}

class FairySwordItem(override val configuration: FairySwordConfiguration, settings: Properties) :
    SwordItem(configuration.toolMaterialCard.toolMaterial, settings.attributes(createAttributes(configuration.toolMaterialCard.toolMaterial, configuration.attackDamage.toInt(), configuration.attackSpeed))),
    FairyToolItem,
    ModifyItemEnchantmentsHandler {

    override fun mineBlock(stack: ItemStack, level: Level, state: BlockState, pos: BlockPos, miner: LivingEntity): Boolean {
        super.mineBlock(stack, level, state, pos, miner)
        postMineImpl(stack, level, state, pos, miner)
        return true
    }

    override fun hurtEnemy(stack: ItemStack, target: LivingEntity, attacker: LivingEntity): Boolean {
        super.hurtEnemy(stack, target, attacker)
        postHitImpl(stack, target, attacker)
        return true
    }

    override fun inventoryTick(stack: ItemStack, level: Level, entity: Entity, slot: Int, selected: Boolean) {
        super.inventoryTick(stack, level, entity, slot, selected)
        inventoryTickImpl(stack, level, entity, slot, selected)
    }

    override fun modifyItemEnchantments(itemStack: ItemStack, mutableItemEnchantments: ItemEnchantments.Mutable, enchantmentLookup: HolderLookup.RegistryLookup<Enchantment>) = modifyItemEnchantmentsImpl(itemStack, mutableItemEnchantments, enchantmentLookup)

    override fun isFoil(stack: ItemStack) = super.isFoil(stack) || hasGlintImpl(stack)

}
