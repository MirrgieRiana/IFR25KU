package miragefairy2024.mod.tool.items

import miragefairy2024.ModContext
import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.util.enJa
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.Tool
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.ItemEnchantments
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

context(ModContext)
fun initFairyToolItem() {
    ScytheItem.DESCRIPTION_TRANSLATION.enJa()
    ShootingStaffItem.NOT_ENOUGH_EXPERIENCE_TRANSLATION.enJa()
    ShootingStaffItem.DESCRIPTION_TRANSLATION.enJa()
    BuildersRodItem.DESCRIPTION_TRANSLATION.enJa()
}


class FairyToolProperties(private val tool: Tool) : Item.Properties() {
    override fun <T : Any> component(component: DataComponentType<T>, value: T): Item.Properties {
        return if (component == DataComponents.TOOL) {
            super.component(DataComponents.TOOL, tool)
        } else {
            super.component(component, value)
        }
    }
}

interface FairyToolItem {
    val configuration: ToolConfiguration
}


fun <I> I.postMineImpl(stack: ItemStack, world: Level, state: BlockState, pos: BlockPos, miner: LivingEntity) where I : Item, I : FairyToolItem {
    configuration.onPostMineListeners.forEach {
        it(this, stack, world, state, pos, miner)
    }
}

fun <I> I.onAfterBreakBlock(world: Level, player: Player, pos: BlockPos, state: BlockState, blockEntity: BlockEntity?, tool: ItemStack) where I : Item, I : FairyToolItem {
    configuration.onAfterBreakBlockListeners.forEach {
        it(this, world, player, pos, state, blockEntity, tool)
    }
}

fun <I> I.postHitImpl(@Suppress("UNUSED_PARAMETER") stack: ItemStack, target: LivingEntity, attacker: LivingEntity) where I : Item, I : FairyToolItem {

}

fun <I> I.onKilled(entity: LivingEntity, attacker: LivingEntity, damageSource: DamageSource) where I : Item, I : FairyToolItem {
    configuration.onKilledListeners.forEach {
        it(this, entity, attacker, damageSource)
    }
}

fun <I> I.inventoryTickImpl(stack: ItemStack, world: Level, entity: Entity, slot: Int, selected: Boolean) where I : Item, I : FairyToolItem {
    configuration.onInventoryTickListeners.forEach {
        it(this, stack, world, entity, slot, selected)
    }
}

fun <I> I.modifyItemEnchantmentsImpl(itemStack: ItemStack, mutableItemEnchantments: ItemEnchantments.Mutable, enchantmentLookup: HolderLookup.RegistryLookup<Enchantment>) where I : Item, I : FairyToolItem {
    configuration.modifyItemEnchantmentsHandlers.forEach {
        it.modifyItemEnchantments(itemStack, mutableItemEnchantments, enchantmentLookup)
    }
}

fun <I> I.hasGlintImpl(stack: ItemStack): Boolean where I : Item, I : FairyToolItem {
    return configuration.hasGlint
}
