package miragefairy2024.mod.machine

import miragefairy2024.ModContext
import miragefairy2024.lib.HorizontalFacingMachineBlock
import miragefairy2024.lib.MachineBlockEntity
import miragefairy2024.lib.MachineCard
import miragefairy2024.lib.MachineScreenHandler
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.fairybuilding.FairyBuildingCard
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.EnJa
import miragefairy2024.util.compound
import miragefairy2024.util.enJa
import miragefairy2024.util.get
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.int
import miragefairy2024.util.mergeInventory
import miragefairy2024.util.normal
import miragefairy2024.util.readFromNbt
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.reset
import miragefairy2024.util.times
import miragefairy2024.util.toInventoryDelegate
import miragefairy2024.util.withHorizontalRotation
import miragefairy2024.util.wrapper
import miragefairy2024.util.writeToNbt
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.Containers
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.state.BlockState
import kotlin.jvm.optionals.getOrNull

abstract class SimpleMachineCard<B : SimpleMachineBlock, E : SimpleMachineBlockEntity<E>, H : SimpleMachineScreenHandler, R : SimpleMachineRecipe> : MachineCard<B, E, H>() {
    companion object {
        val PROGRESS_PROPERTY = FairyBuildingCard.PropertyConfiguration<SimpleMachineBlockEntity<*>>({ progress }, { progress = it })
        val PROGRESS_MAX_PROPERTY = FairyBuildingCard.PropertyConfiguration<SimpleMachineBlockEntity<*>>({ progressMax }, { progressMax = it })
    }

    abstract val name: EnJa
    abstract val poem: EnJa
    abstract val tier: Int

    open class SlotConfiguration(
        override val x: Int,
        override val y: Int,
        private val insertDirections: Set<Direction> = setOf(),
        private val extractDirections: Set<Direction> = setOf(),
    ) : MachineBlockEntity.InventorySlotConfiguration, MachineScreenHandler.GuiSlotConfiguration {
        override fun isValid(itemStack: ItemStack) = true
        override fun getTooltip() = null
        override fun canInsert(direction: Direction) = direction in insertDirections
        override fun canExtract(direction: Direction) = direction in extractDirections
        override val isObservable = false
        override val dropItem = true
    }

    abstract val inputSlots: List<SlotConfiguration>
    abstract val outputSlots: List<SlotConfiguration>
    abstract val slots: List<SlotConfiguration>

    open val properties: List<MachineScreenHandler.PropertyConfiguration<E>> = listOf(PROGRESS_PROPERTY, PROGRESS_MAX_PROPERTY)

    abstract val recipeType: RecipeType<R>

    fun match(world: Level, inventory: SimpleMachineRecipeInput) = world.recipeManager.getRecipeFor(recipeType, inventory, world).getOrNull()

    context(ModContext)
    override fun init() {
        super.init()

        item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        registerBlockStateGeneration()

        block.enJa(name)
        val poemList = PoemList(tier).poem(poem)
        item.registerPoem(poemList)
        item.registerPoemGeneration(poemList)

        block.registerDefaultLootTableGeneration()

        inventorySlotConfigurations += slots
        guiSlotConfigurations += slots
        propertyConfigurations += properties

    }

    context(ModContext)
    open fun registerBlockStateGeneration() {
        block.registerVariantsBlockStateGeneration { normal("block/" * block().getIdentifier()).withHorizontalRotation(HorizontalDirectionalBlock.FACING) }
    }
}

abstract class SimpleMachineBlock(card: SimpleMachineCard<*, *, *, *>) : HorizontalFacingMachineBlock(card)

abstract class SimpleMachineBlockEntity<E : SimpleMachineBlockEntity<E>>(private val card: SimpleMachineCard<*, E, *, *>, pos: BlockPos, state: BlockState) : MachineBlockEntity<E>(card, pos, state) {

    override fun loadAdditional(nbt: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(nbt, registries)
        craftingInventory.reset()
        nbt.wrapper["CraftingInventory"].compound.get()?.let { craftingInventory.readFromNbt(it, registries) }
        waitingInventory.reset()
        nbt.wrapper["WaitingInventory"].compound.get()?.let { waitingInventory.readFromNbt(it, registries) }
        progressMax = nbt.wrapper["ProgressMax"].int.get() ?: 0
        progress = nbt.wrapper["Progress"].int.get() ?: 0
    }

    override fun saveAdditional(nbt: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(nbt, registries)
        nbt.wrapper["CraftingInventory"].compound.set(craftingInventory.writeToNbt(registries))
        nbt.wrapper["WaitingInventory"].compound.set(waitingInventory.writeToNbt(registries))
        nbt.wrapper["ProgressMax"].int.set(progressMax)
        nbt.wrapper["Progress"].int.set(progress)
    }

    override fun setChanged() {
        super.setChanged()
        shouldUpdateRecipe = true
        shouldUpdateWaiting = true
    }

    override fun getActualSide(side: Direction) = HorizontalFacingMachineBlock.getActualSide(blockState, side)

    val craftingInventory = mutableListOf<ItemStack>()
    val waitingInventory = mutableListOf<ItemStack>()

    override fun clearContent() {
        super.clearContent()
        craftingInventory.clear()
        waitingInventory.clear()
    }

    override fun dropItems() {
        super.dropItems()
        val level = level ?: return
        craftingInventory.forEach {
            Containers.dropItemStack(level, worldPosition.x.toDouble(), worldPosition.y.toDouble(), worldPosition.z.toDouble(), it)
        }
    }

    var shouldUpdateRecipe = true
    var shouldUpdateWaiting = true
    var progressMax = 0
    var progress = 0

    fun checkRecipe(world: Level): (() -> Unit)? {
        if (!shouldUpdateRecipe) return null
        shouldUpdateRecipe = false

        // TODO 順不同
        val inventory = SimpleMachineRecipeInput(card.inputSlots.map { this[card.inventorySlotIndexTable[it]!!] })

        val recipeHolder = card.match(world, inventory) ?: return null
        val recipe = recipeHolder.value()
        if (recipe.inputs.size > inventory.size()) return null

        return {
            val remainder = recipe.getRemainingItems(inventory)
            (0 until recipe.inputs.size).forEach { index ->
                craftingInventory += inventory.getItem(index).split(recipe.inputs[index].count)
            }
            waitingInventory += recipe.output.copy()
            waitingInventory += remainder
            progressMax = recipe.duration
            setChanged()
        }
    }

    open fun onRecipeCheck(world: Level, pos: BlockPos, state: BlockState, listeners: MutableList<() -> Unit>): Boolean {
        listeners += checkRecipe(world) ?: return false
        return true
    }

    open fun onCraftingTick(world: Level, pos: BlockPos, state: BlockState, listeners: MutableList<() -> Unit>): Boolean {
        return true
    }

    open fun onPostServerTick(world: Level, pos: BlockPos, state: BlockState) {

    }

    override fun serverTick(world: Level, pos: BlockPos, state: BlockState) {
        super.serverTick(world, pos, state)

        // クラフトが開始されていなければ、開始を試みる
        if (progressMax == 0) run {
            val listeners = mutableListOf<() -> Unit>()
            if (!onRecipeCheck(world, pos, state, listeners)) return@run
            listeners.forEach {
                it()
            }
        }

        // クラフトが開始されていれば、クラフトの進行を試みる
        if (progressMax > 0) {

            // クラフトが完了していなければ、プログレスの進行を試みる
            if (progress < progressMax) run success@{
                run fail@{
                    val listeners = mutableListOf<() -> Unit>()
                    if (!onCraftingTick(world, pos, state, listeners)) return@fail
                    listeners.forEach {
                        it()
                    }

                    progress++
                    setChanged()
                    return@success
                }

                progress = 0
                setChanged()
            }

            // プログレスが完了していれば、クラフトの完了を試みる
            if (progress >= progressMax) {
                if (shouldUpdateWaiting) {
                    shouldUpdateWaiting = false

                    val result = mergeInventory(
                        waitingInventory.toInventoryDelegate(),
                        this.toInventoryDelegate(),
                        destIndices = card.outputSlots.map { card.inventorySlotIndexTable[it]!! },
                    )
                    if (result.movementTimes > 0) setChanged()
                    if (result.completed) {
                        progress = 0
                        progressMax = 0
                        craftingInventory.clear()
                        setChanged()
                    }

                }
            }

        }

        onPostServerTick(world, pos, state)

    }
}

// TODO レシピブック対応
open class SimpleMachineScreenHandler(card: SimpleMachineCard<*, *, *, *>, arguments: Arguments) : MachineScreenHandler(card, arguments) {
    var progress by Property(SimpleMachineCard.PROGRESS_PROPERTY)
    var progressMax by Property(SimpleMachineCard.PROGRESS_MAX_PROPERTY)
}
