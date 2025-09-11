package miragefairy2024.util

import mirrg.kotlin.helium.atMost
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.Tag
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import kotlin.jvm.optionals.getOrNull

fun Item.createItemStack(count: Int = 1) = ItemStack(this, count atMost this.defaultMaxStackSize)

val EMPTY_ITEM_STACK: ItemStack get() = ItemStack.EMPTY
val ItemStack?.orEmpty get() = this ?: EMPTY_ITEM_STACK
val ItemStack.isNotEmpty get() = !this.isEmpty

fun ItemStack.toNbt(registries: HolderLookup.Provider): Tag = this.save(registries)
fun Tag.toItemStack(registries: HolderLookup.Provider) = ItemStack.parse(registries, this).getOrNull()

infix fun ItemStack.hasSameItem(other: ItemStack) = this.item == other.item
infix fun ItemStack.hasSameItemAndComponents(other: ItemStack) = this hasSameItem other && this.components == other.components
infix fun ItemStack.hasSameItemAndComponentsAndCount(other: ItemStack) = this hasSameItemAndComponents other && this.count == other.count

fun ItemStack.repair(amount: Int) {
    if (amount <= 0) return
    if (!this.isDamageableItem) return
    val actualAmount = amount atMost this.damageValue
    if (actualAmount <= 0) return
    this.damageValue -= actualAmount
}

infix fun ItemStack.isIn(item: Item) = this.`is`(item)
infix fun ItemStack.isNotIn(item: Item) = !(this isIn item)
infix fun ItemStack.isIn(tag: TagKey<Item>) = this.`is`(tag)
infix fun ItemStack.isNotIn(tag: TagKey<Item>) = !(this isIn tag)
