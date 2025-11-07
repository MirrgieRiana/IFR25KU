package miragefairy2024.util

import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

/** @param itemStack 内部でコピーされるため、破壊されません。 */
fun Entity.obtain(itemStack: ItemStack) {
    val itemEntity = this.spawnAtLocation(itemStack.copy(), 0.5F)
    if (itemEntity != null) {
        itemEntity.setNoPickUpDelay()
        itemEntity.setTarget(this.uuid)
    }
}

val Entity.eyeBlockPos get() = this.eyePosition.toBlockPos()

val InteractionHand.opposite get() = if (this == InteractionHand.MAIN_HAND) InteractionHand.OFF_HAND else InteractionHand.MAIN_HAND

fun Player.removeItemStackFromMainInventoryAndOffhand(searchingItemStack: ItemStack): ItemStack? {
    return ((0 until Inventory.INVENTORY_SIZE) + Inventory.SLOT_OFFHAND)
        .firstNotNullOfOrNull next@{ index ->
            val itemStack = this.inventory[index]
            if (itemStack.isEmpty) return@next null // 空の場合はスキップ
            if (!(itemStack hasSameItemAndComponents searchingItemStack)) return@next null // 種類が一致しない場合はスキップ
            this.inventory[index] = EMPTY_ITEM_STACK
            itemStack
        }
}

fun Player.getSameItemStackCountInMainInventoryAndOffhand(searchingItemStack: ItemStack): Int {
    return ((0 until Inventory.INVENTORY_SIZE) + Inventory.SLOT_OFFHAND)
        .mapNotNull next@{ index ->
            val itemStack = this.inventory[index]
            if (itemStack.isEmpty) return@next null // 空の場合はスキップ
            if (!(itemStack hasSameItemAndComponents searchingItemStack)) return@next null // 種類が一致しない場合はスキップ
            itemStack.count
        }
        .sum()
}
