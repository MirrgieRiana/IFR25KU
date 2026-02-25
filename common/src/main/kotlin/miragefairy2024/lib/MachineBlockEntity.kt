package miragefairy2024.lib

import miragefairy2024.RenderingProxy
import miragefairy2024.RenderingProxyBlockEntity
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.readFromNbt
import miragefairy2024.util.reset
import miragefairy2024.util.toNonNullList
import miragefairy2024.util.writeToNbt
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.Container
import net.minecraft.world.Containers
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity
import net.minecraft.world.level.block.state.BlockState

abstract class MachineBlockEntity<E : MachineBlockEntity<E>>(private val card: MachineCard<*, E, *>, pos: BlockPos, state: BlockState) : BaseContainerBlockEntity(card.blockEntityType(), pos, state), WorldlyContainer, RenderingProxyBlockEntity {

    interface InventorySlotConfiguration {
        fun isValid(itemStack: ItemStack): Boolean
        fun canInsert(direction: Direction): Boolean
        fun canExtract(direction: Direction): Boolean
        val isObservable: Boolean
        val dropItem: Boolean
    }

    abstract fun getThis(): E


    // Data

    override fun loadAdditional(nbt: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(nbt, registries)
        inventory.load(nbt, registries)
    }

    override fun saveAdditional(nbt: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(nbt, registries)
        inventory.save(nbt, registries)
    }

    override fun getUpdateTag(registries: HolderLookup.Provider): CompoundTag = saveWithoutMetadata(registries) // TODO スロットの更新はカスタムパケットに分けるのでこちらはオーバーライドしない

    override fun getUpdatePacket(): Packet<ClientGamePacketListener>? = ClientboundBlockEntityDataPacket.create(this) // TODO スロットの更新はカスタムパケットに分けるのでこちらはオーバーライドしない


    // Container

    private val inventory = BlockEntityInventory(card.inventorySlotConfigurations.size, object : BlockEntityInventory.Callback {
        override fun onDataChanged() {
            // TODO スロットアップデートのための軽量カスタムパケット
            level?.sendBlockUpdated(worldPosition, blockState, blockState, Block.UPDATE_ALL)
        }

        override fun onViewChanged() {
            setChanged()
        }
    })

    private val inventorySlotAccessors = (0 until card.inventorySlotConfigurations.size).map {
        inventory.getInventorySlotAccessor(it, card.inventorySlotConfigurations[it])
    }

    override fun getContainerSize() = inventorySlotAccessors.size

    override fun isEmpty() = inventorySlotAccessors.all { it.get().isEmpty }

    override fun getItem(slot: Int): ItemStack = inventorySlotAccessors.getOrNull(slot)?.get() ?: EMPTY_ITEM_STACK

    override fun getItems() = inventorySlotAccessors.map { it.get() }.toNonNullList()

    override fun setItem(slot: Int, stack: ItemStack) {
        val inventorySlotAccessor = inventorySlotAccessors.getOrNull(slot) ?: return
        inventorySlotAccessor.set(stack)
        inventorySlotAccessor.onChanged()
    }

    override fun setItems(items: NonNullList<ItemStack>) {
        items.forEachIndexed { accessorIndex, itemStack ->
            setItem(accessorIndex, itemStack)
        }
    }

    override fun removeItem(slot: Int, amount: Int): ItemStack {
        val inventorySlotAccessor = inventorySlotAccessors.getOrNull(slot) ?: return EMPTY_ITEM_STACK
        val itemStack = inventorySlotAccessor.get()
        if (itemStack.isEmpty) return EMPTY_ITEM_STACK
        if (amount <= 0) return EMPTY_ITEM_STACK
        val result = itemStack.split(amount)
        inventorySlotAccessor.set(itemStack)
        inventorySlotAccessor.onChanged()
        return result
    }

    override fun removeItemNoUpdate(slot: Int): ItemStack {
        val inventorySlotAccessor = inventorySlotAccessors.getOrNull(slot) ?: return EMPTY_ITEM_STACK
        val itemStack = inventorySlotAccessor.get()
        inventorySlotAccessor.set(EMPTY_ITEM_STACK)
        inventorySlotAccessor.onChanged()
        return itemStack
    }

    override fun canPlaceItem(slot: Int, stack: ItemStack) = inventorySlotAccessors.getOrNull(slot)?.isValid(stack) ?: false

    abstract fun getActualSide(side: Direction): Direction

    private val actualSideToInventorySlotAccessorIndicesTable by lazy {
        Direction.entries.map { actualSide ->
            inventorySlotAccessors.withIndex()
                .filter { (_, it) -> it.canInsert(actualSide) || it.canExtract(actualSide) }
                .map { it.index }
                .toIntArray()
        }.toTypedArray()
    }

    override fun getSlotsForFace(side: Direction) = actualSideToInventorySlotAccessorIndicesTable[getActualSide(side).get3DDataValue()]

    override fun canPlaceItemThroughFace(slot: Int, stack: ItemStack, dir: Direction?): Boolean {
        val inventorySlotAccessor = inventorySlotAccessors.getOrNull(slot) ?: return false
        val isBlockedBySlot = dir != null && !inventorySlotAccessor.canInsert(getActualSide(dir))
        return !isBlockedBySlot && canPlaceItem(slot, stack)
    }

    override fun canTakeItemThroughFace(slot: Int, stack: ItemStack, dir: Direction): Boolean {
        val inventorySlotAccessor = inventorySlotAccessors.getOrNull(slot) ?: return false
        return inventorySlotAccessor.canExtract(getActualSide(dir))
    }

    override fun clearContent() {
        inventorySlotAccessors.forEach {
            it.set(EMPTY_ITEM_STACK)
        }
        inventorySlotAccessors.forEach {
            it.onChanged()
        }
    }

    open fun dropItems() {
        val level = level ?: return
        inventorySlotAccessors.forEach {
            if (it.dropItem) {
                val itemStack = it.get()
                it.set(EMPTY_ITEM_STACK)
                Containers.dropItemStack(level, worldPosition.x.toDouble(), worldPosition.y.toDouble(), worldPosition.z.toDouble(), itemStack)
            }
        }
        inventorySlotAccessors.forEach {
            if (it.dropItem) {
                it.onChanged()
            }
        }
    }


    // Move

    open fun serverTick(world: Level, pos: BlockPos, state: BlockState) = Unit


    // Rendering

    interface AnimationConfiguration<in E> {
        fun createAnimation(): Animation<E>?
    }

    interface Animation<in E> {
        fun tick(blockEntity: E)
        fun render(blockEntity: E, renderingProxy: RenderingProxy, tickDelta: Float)
    }

    private val animations = card.animationConfigurations.mapNotNull { it.createAnimation() }

    open fun clientTick(world: Level, pos: BlockPos, state: BlockState) {
        animations.forEach {
            it.tick(getThis())
        }
    }

    override fun render(renderingProxy: RenderingProxy, tickDelta: Float, light: Int, overlay: Int) {
        renderRotated(renderingProxy, tickDelta, light, overlay)
    }

    open fun renderRotated(renderingProxy: RenderingProxy, tickDelta: Float, light: Int, overlay: Int) {
        animations.forEach {
            it.render(getThis(), renderingProxy, tickDelta)
        }
    }


    // Gui

    private val propertyDelegate = object : ContainerData {
        override fun getCount() = card.propertyConfigurations.size
        override fun get(index: Int) = card.propertyConfigurations.getOrNull(index)?.let { it.encode(it.get(getThis())).toInt() } ?: 0
        override fun set(index: Int, value: Int) = card.propertyConfigurations.getOrNull(index)?.let { it.set(getThis(), it.decode(value.toShort())) } ?: Unit
    }

    override fun stillValid(player: Player) = Container.stillValidBlockEntity(this, player)

    override fun getDefaultName(): Component = card.block().name

    override fun createMenu(syncId: Int, playerInventory: Inventory): AbstractContainerMenu {
        val arguments = MachineScreenHandler.Arguments(
            syncId,
            playerInventory,
            this,
            propertyDelegate,
            ContainerLevelAccess.create(level!!, worldPosition),
        )
        return card.createScreenHandler(arguments)
    }

}

class BlockEntityInventory(size: Int, private val callback: Callback) {
    interface Callback {
        fun onDataChanged()
        fun onViewChanged()
    }

    private val inventory = MutableList(size) { EMPTY_ITEM_STACK }
    private var isDataChanged = false
    private var isViewChanged = false

    fun save(nbt: CompoundTag, registries: HolderLookup.Provider) {
        inventory.writeToNbt(nbt, registries)
    }

    fun load(nbt: CompoundTag, registries: HolderLookup.Provider) {
        inventory.reset()
        inventory.readFromNbt(nbt, registries)
    }

    fun getInventorySlotAccessor(index: Int, configuration: MachineBlockEntity.InventorySlotConfiguration): InventorySlotAccessor {
        return object : InventorySlotAccessor {
            override fun get() = inventory[index]

            override fun set(itemStack: ItemStack) {
                inventory[index] = itemStack
                isDataChanged = true
                if (configuration.isObservable) isViewChanged = true
            }

            override fun onChanged() {
                if (isDataChanged) {
                    isDataChanged = false
                    callback.onDataChanged()
                }
                if (isViewChanged) {
                    isViewChanged = false
                    callback.onViewChanged()
                }
            }

            override fun isValid(itemStack: ItemStack) = configuration.isValid(itemStack)

            override fun canInsert(actualSide: Direction) = configuration.canInsert(actualSide)

            override fun canExtract(actualSide: Direction) = configuration.canExtract(actualSide)

            override val dropItem: Boolean get() = configuration.dropItem
        }
    }
}

interface InventorySlotAccessor {
    fun get(): ItemStack
    fun set(itemStack: ItemStack)
    fun onChanged()
    fun isValid(itemStack: ItemStack): Boolean
    fun canInsert(actualSide: Direction): Boolean
    fun canExtract(actualSide: Direction): Boolean
    val dropItem: Boolean
}
