package miragefairy2024.util

import mirrg.kotlin.helium.atLeast
import mirrg.kotlin.helium.atMost
import mirrg.kotlin.helium.unit
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.Container
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import kotlin.experimental.and

operator fun Container.get(slot: Int): ItemStack = this.getItem(slot)
operator fun Container.set(slot: Int, stack: ItemStack) = this.setItem(slot, stack)
val Container.size get() = this.containerSize
val Container.indices get() = 0 until this.size
val Container.itemStacks get() = this.indices.map { this[it] }

class FilteringSlot(inventory: Container, index: Int, x: Int, y: Int) : Slot(inventory, index, x, y) {
    override fun mayPlace(stack: ItemStack) = container.canPlaceItem(index, stack)
}

class OutputSlot(inventory: Container, index: Int, x: Int, y: Int) : Slot(inventory, index, x, y) {
    override fun mayPlace(stack: ItemStack) = false
}


// Merge

/**
 * @param completed 移動が完了したかどうか
 * @param movementTimes アイテムの移動を行った回数
 * @param movedItemCount 移動されたアイテムの個数
 */
class MergeResult(val completed: Boolean, val movementTimes: Int, val movedItemCount: Int) {
    companion object {
        val UP_TO_DATE = MergeResult(true, 0, 0)
        val FAILED = MergeResult(false, 0, 0)
    }
}

/**
 * インベントリのアイテムを別のインベントリに可能な限り移動させる
 * @return すべてのアイテムが完全に移動したかどうか
 */
fun mergeInventory(src: InventoryDelegate, dest: InventoryDelegate, srcSlotIndex: Int, destSlotIndex: Int): MergeResult {

    val srcItemStack = src.getItemStack(srcSlotIndex)
    if (srcItemStack.isEmpty) return MergeResult.UP_TO_DATE // 元から空なので何もする必要はない

    if (!dest.canInsert(destSlotIndex, srcItemStack)) return MergeResult.FAILED // 宛先にこの種類のアイテムが入らない

    val destItemStack = dest.getItemStack(destSlotIndex)
    if (destItemStack.isNotEmpty && !(srcItemStack hasSameItemAndComponents destItemStack)) return MergeResult.FAILED // 宛先に別のアイテムが入っているので何もできない

    // 先が空もしくは元と同じ種類のアイテムが入っているのでマージ

    // 個数計算
    val srcCount = srcItemStack.count
    val oldDestCount = destItemStack.count
    val allCount = srcCount + oldDestCount
    val newDestCount = allCount atMost dest.getMaxCountPerStack(destSlotIndex) atMost srcItemStack.maxStackSize atLeast oldDestCount
    val moveCount = newDestCount - oldDestCount

    if (moveCount == 0) return MergeResult.FAILED // 宛先にこれ以上入らない

    // 移動処理
    if (destItemStack.isEmpty) {
        dest.setItemStack(destSlotIndex, srcItemStack.copyWithCount(newDestCount))
    } else {
        destItemStack.count = newDestCount
    }
    val newSrcCount = allCount - newDestCount
    srcItemStack.count = newSrcCount
    if (newSrcCount == 0) src.setItemStack(srcSlotIndex, EMPTY_ITEM_STACK)

    return MergeResult(newSrcCount == 0, 1, moveCount)
}

/**
 * インベントリのアイテムを別のインベントリに可能な限り移動させる
 * @return すべてのアイテムが完全に移動したかどうか
 */
fun mergeInventory(src: InventoryDelegate, dest: InventoryDelegate, srcIndex: Int, destIndices: Iterable<Int>): MergeResult {
    if (!src.canExtract(srcIndex, src.getItemStack(srcIndex))) return MergeResult.FAILED // 移動元がこのアイテムを出せない
    var movementTimes = 0
    var movedItemCount = 0
    destIndices.forEach { destIndex ->
        val result = mergeInventory(src, dest, srcIndex, destIndex)
        movementTimes += result.movementTimes
        movedItemCount += result.movedItemCount
        if (result.completed) return MergeResult(true, movementTimes, movedItemCount) // すべてマージされたのでこれ以降の判定は不要
    }
    return MergeResult(false, movementTimes, movedItemCount)
}

/**
 * インベントリのアイテムを別のインベントリに可能な限り移動させる
 * @return すべてのアイテムが完全に移動したかどうか
 */
fun mergeInventory(src: InventoryDelegate, dest: InventoryDelegate, srcIndices: Iterable<Int>? = null, destIndices: Iterable<Int>? = null): MergeResult {
    var completed = true
    var movementTimes = 0
    var movedItemCount = 0
    (srcIndices ?: src.getIndices()).forEach { srcIndex ->
        val result = mergeInventory(src, dest, srcIndex, destIndices ?: dest.getIndices())
        if (!result.completed) completed = false
        movementTimes += result.movementTimes
        movedItemCount += result.movedItemCount
    }
    return MergeResult(completed, movementTimes, movedItemCount)
}

interface InventoryDelegate {
    fun getIndices(): Iterable<Int>
    fun getItemStack(index: Int): ItemStack
    fun setItemStack(index: Int, itemStack: ItemStack)
    fun canInsert(index: Int, itemStack: ItemStack): Boolean
    fun canExtract(index: Int, itemStack: ItemStack): Boolean
    fun getMaxCountPerStack(index: Int): Int
    fun markDirty()
}

class MutableListInventoryDelegate(private val inventory: MutableList<ItemStack>) : InventoryDelegate {
    override fun getIndices() = inventory.indices
    override fun getItemStack(index: Int) = inventory[index]
    override fun setItemStack(index: Int, itemStack: ItemStack) = unit { inventory[index] = itemStack }
    override fun canExtract(index: Int, itemStack: ItemStack) = true
    override fun canInsert(index: Int, itemStack: ItemStack) = true
    override fun getMaxCountPerStack(index: Int) = 64
    override fun markDirty() = Unit
}

fun MutableList<ItemStack>.toInventoryDelegate() = MutableListInventoryDelegate(this)

class SimpleInventoryDelegate(private val inventory: Container) : InventoryDelegate {
    override fun getIndices() = inventory.indices
    override fun getItemStack(index: Int) = inventory[index]
    override fun setItemStack(index: Int, itemStack: ItemStack) = unit { inventory[index] = itemStack }
    override fun canExtract(index: Int, itemStack: ItemStack) = true
    override fun canInsert(index: Int, itemStack: ItemStack) = inventory.canPlaceItem(index, itemStack)
    override fun getMaxCountPerStack(index: Int) = inventory.maxStackSize
    override fun markDirty() = inventory.setChanged()
}

fun Container.toInventoryDelegate() = SimpleInventoryDelegate(this)

class SidedInventoryDelegate(private val inventory: Container, private val side: Direction) : InventoryDelegate {
    override fun getIndices() = if (inventory is WorldlyContainer) inventory.getSlotsForFace(side).asIterable() else inventory.indices
    override fun getItemStack(index: Int) = inventory[index]
    override fun setItemStack(index: Int, itemStack: ItemStack) = unit { inventory[index] = itemStack }
    override fun canExtract(index: Int, itemStack: ItemStack) = if (inventory is WorldlyContainer) inventory.canTakeItemThroughFace(index, itemStack, side) else true
    override fun canInsert(index: Int, itemStack: ItemStack) = (if (inventory is WorldlyContainer) inventory.canPlaceItemThroughFace(index, itemStack, side) else true) && inventory.canPlaceItem(index, itemStack)
    override fun getMaxCountPerStack(index: Int) = inventory.maxStackSize
    override fun markDirty() = inventory.setChanged()
}

fun Container.toSidedInventoryDelegate(side: Direction) = SidedInventoryDelegate(this, side)

fun InventoryDelegate.mergeTo(other: InventoryDelegate) = mergeInventory(this, other)

/**
 * インベントリのアイテムを別のインベントリに可能な限り移動させる
 * @return すべてのアイテムが完全に移動したかどうか
 */
fun Container.mergeTo(other: Container) = mergeInventory(this.toInventoryDelegate(), other.toInventoryDelegate())


// Insert

interface InventoryAccessor {
    val size: Int
    fun getItemStack(index: Int): ItemStack
    fun setItemStack(index: Int, itemStack: ItemStack)
    fun getMaxItemCount(index: Int): Int
    fun canInsert(index: Int, itemStack: ItemStack): Boolean
    fun markDirty(index: Int)
}

val AbstractContainerMenu.inventoryAccessor: InventoryAccessor
    get() = object : InventoryAccessor {
        override val size: Int get() = this@inventoryAccessor.slots.size
        override fun getItemStack(index: Int) = this@inventoryAccessor.slots[index].item
        override fun setItemStack(index: Int, itemStack: ItemStack) = unit { this@inventoryAccessor.slots[index].set(itemStack) }
        override fun getMaxItemCount(index: Int) = this@inventoryAccessor.slots[index].maxStackSize
        override fun canInsert(index: Int, itemStack: ItemStack) = this@inventoryAccessor.slots[index].mayPlace(itemStack)
        override fun markDirty(index: Int) = this@inventoryAccessor.slots[index].setChanged()
    }

val Container.inventoryAccessor: InventoryAccessor
    get() = object : InventoryAccessor {
        override val size: Int get() = this@inventoryAccessor.size
        override fun getItemStack(index: Int) = this@inventoryAccessor[index]
        override fun setItemStack(index: Int, itemStack: ItemStack) = unit { this@inventoryAccessor[index] = itemStack }
        override fun getMaxItemCount(index: Int) = this@inventoryAccessor.maxStackSize
        override fun canInsert(index: Int, itemStack: ItemStack) = this@inventoryAccessor.canPlaceItem(index, itemStack)
        override fun markDirty(index: Int) = this@inventoryAccessor.setChanged()
    }

/** @see AbstractContainerMenu.insertItem */
fun InventoryAccessor.insertItem(insertItemStack: ItemStack, indices: Iterable<Int>): Boolean {
    var moved = false

    // 既存スロットへの挿入
    if (insertItemStack.isStackable) run {
        indices.forEach { i ->
            if (insertItemStack.isEmpty) return@run // 挿入完了
            val slotItemStack = this.getItemStack(i)
            if (slotItemStack.isNotEmpty && this.canInsert(i, insertItemStack) && insertItemStack hasSameItemAndComponents slotItemStack) { // 宛先が空でなく、そのアイテムを挿入可能で、既存アイテムとスタック可能な場合
                val moveCount = insertItemStack.count atMost (slotItemStack.maxStackSize - slotItemStack.count atLeast 0)
                if (moveCount > 0) {
                    insertItemStack.count -= moveCount
                    slotItemStack.count += moveCount
                    this.markDirty(i)
                    moved = true
                }
            }
        }
    }

    // 新規スロットへの挿入
    if (!insertItemStack.isEmpty) run {
        indices.forEach { i ->
            val slotItemStack = this.getItemStack(i)
            if (slotItemStack.isEmpty && this.canInsert(i, insertItemStack)) { // 宛先が空っぽかつ、そのアイテムを挿入可能な場合
                val moveCount = insertItemStack.count atMost this.getMaxItemCount(i)
                this.setItemStack(i, insertItemStack.split(moveCount))
                this.markDirty(i)
                moved = true
                return@run // 新規スロットへの挿入は一度に1スロット分しかしない
            }
        }
    }

    return moved
}


// MutableList

fun MutableList<ItemStack>.reset() = this.replaceAll { EMPTY_ITEM_STACK }

fun MutableList<ItemStack>.readFromNbt(nbt: CompoundTag, registries: HolderLookup.Provider) {
    nbt.wrapper["Items"].list.get()?.let { items ->
        items.forEach { item ->
            item.wrapper.compound.get()?.let { itemCompound ->
                val slotIndex = (itemCompound.wrapper["Slot"].byte.orDefault { 0 }.get() and 255.toByte()).toInt()
                if (slotIndex in this.indices) this[slotIndex] = itemCompound.toItemStack(registries) ?: EMPTY_ITEM_STACK
            }
        }
    }
}

fun List<ItemStack>.writeToNbt(nbt: CompoundTag, registries: HolderLookup.Provider) {
    val nbtList = NbtList()
    this.forEachIndexed { slotIndex, itemStack ->
        if (itemStack.isNotEmpty) {
            var itemCompound = CompoundTag()
            itemCompound.wrapper["Slot"].byte.set(slotIndex.toByte())
            itemCompound = itemStack.save(registries, itemCompound) as CompoundTag
            nbtList.add(itemCompound)
        }
    }
    nbt.put("Items", nbtList)
}

fun List<ItemStack>.writeToNbt(registries: HolderLookup.Provider) = CompoundTag().also { this.writeToNbt(it, registries) }
