package miragefairy2024.mod.common

import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.FreezableRegistry
import miragefairy2024.util.set
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

// アイテム側にレシピ残留物を設定できないアイテムのために、外からアイテムごとの残留物を足せるようにするのだ～🌱
object CustomizedRemainderRegistry {
    private val registry = FreezableRegistry<Item, (ItemStack) -> ItemStack>()

    fun register(item: Item, handler: (ItemStack) -> ItemStack) {
        registry[item] = handler
    }

    fun getCustomizedRemainder(itemStack: ItemStack): ItemStack {
        val handler = registry.freezeAndGet()[itemStack.item] ?: return EMPTY_ITEM_STACK
        return handler(itemStack)
    }
}
