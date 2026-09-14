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
import net.minecraft.world.item.ShovelItem
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.ItemEnchantments
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

open class FairyShovelConfiguration(
    override val toolMaterialCard: ToolMaterialCard,
) : FairyMiningToolConfiguration() {
    override fun createItem(properties: Item.Properties) = FairyShovelItem(this, properties)

    init {
        this.attackDamage = 1.5F
        this.attackSpeed = -3.0F
        this.tags += ItemTags.SHOVELS
        this.effectiveBlockTags += BlockTags.MINEABLE_WITH_SHOVEL
    }
}

class FairyShovelItem(override val configuration: FairyShovelConfiguration, settings: Properties) :
    ShovelItem(configuration.toolMaterialCard.toolMaterial, settings.attributes(createAttributes(configuration.toolMaterialCard.toolMaterial, configuration.attackDamage, configuration.attackSpeed))),
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
