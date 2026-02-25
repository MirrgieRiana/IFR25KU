package miragefairy2024.lib

import miragefairy2024.ModContext
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.BlockEntityType
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.dummyUnitStreamCodec
import miragefairy2024.util.enJa
import miragefairy2024.util.register
import miragefairy2024.util.times
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.SimpleContainer
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.inventory.SimpleContainerData
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState

@Suppress("LeakingThis") // ブートストラップ問題のため解決不可能なので妥協する
abstract class MachineCard<B : Block, E : MachineBlockEntity<E>, H : MachineScreenHandler> {
    companion object {
        context(C)
        inline fun <C, reified E> BlockEntityAccessor(crossinline creator: (card: C, blockPos: BlockPos, blockState: BlockState) -> E) = object : BlockEntityAccessor<E> {
            override fun create(blockPos: BlockPos, blockState: BlockState) = creator(this@C, blockPos, blockState)
            override fun castOrThrow(blockEntity: BlockEntity?) = blockEntity as E
            override fun castOrNull(blockEntity: BlockEntity?) = blockEntity as? E
        }
    }


    // Specification

    abstract fun createIdentifier(): ResourceLocation
    val identifier = createIdentifier()

    abstract fun createName(): EnJa
    abstract fun createPoemList(): PoemList?


    // Block

    abstract fun createBlockSettings(): BlockBehaviour.Properties
    abstract fun createBlock(): B
    val block = Registration(BuiltInRegistries.BLOCK, identifier) { createBlock() }


    // BlockEntity

    val inventorySlotConfigurations = mutableListOf<MachineBlockEntity.InventorySlotConfiguration>()

    val inventorySlotIndexTable by lazy {
        inventorySlotConfigurations.withIndex().associate { (index, it) -> it to index }
    }

    val animationConfigurations = mutableListOf<MachineBlockEntity.AnimationConfiguration<E>>()

    interface BlockEntityAccessor<E> {
        fun create(blockPos: BlockPos, blockState: BlockState): E
        fun castOrThrow(blockEntity: BlockEntity?): E
        fun castOrNull(blockEntity: BlockEntity?): E?
    }

    abstract fun createBlockEntityAccessor(): BlockEntityAccessor<E>
    val blockEntityAccessor = createBlockEntityAccessor()
    val blockEntityType = Registration(BuiltInRegistries.BLOCK_ENTITY_TYPE, identifier) { BlockEntityType(blockEntityAccessor::create, setOf(block.await())) }


    // Item

    val item = Registration(BuiltInRegistries.ITEM, identifier) { BlockItem(block.await(), Item.Properties()) }


    // ScreenHandler

    abstract fun createScreenHandler(arguments: MachineScreenHandler.Arguments): H
    val screenHandlerType = Registration(BuiltInRegistries.MENU, identifier) {
        ExtendedScreenHandlerType({ syncId, playerInventory, _ ->
            val arguments = MachineScreenHandler.Arguments(
                syncId,
                playerInventory,
                SimpleContainer(inventorySlotConfigurations.size),
                SimpleContainerData(propertyConfigurations.size),
                ContainerLevelAccess.NULL,
            )
            createScreenHandler(arguments)
        }, dummyUnitStreamCodec())
    }


    // Gui

    abstract val guiWidth: Int
    abstract val guiHeight: Int

    val backgroundTexture = "textures/gui/container/" * identifier * ".png"
    val backgroundTextureSize = IntPoint(256, 256)

    val guiSlotConfigurations = mutableListOf<MachineScreenHandler.GuiSlotConfiguration>()

    val guiSlotIndexTable by lazy {
        inventorySlotConfigurations.withIndex().associate { (index, it) -> it to 9 * 4 + index } // TODO プレイヤーインベントリの扱い
    }

    val propertyConfigurations = mutableListOf<MachineScreenHandler.PropertyConfiguration<E>>()

    val propertyIndexTable by lazy {
        propertyConfigurations.withIndex().associate { (index, it) -> it to index }
    }


    // Advancement

    open fun createAdvancement(): AdvancementCard? = null
    val advancement = createAdvancement()


    context(ModContext)
    open fun init() {

        block.register()
        block.enJa(createName())

        blockEntityType.register()

        item.register()
        val poemList = createPoemList()
        if (poemList != null) {
            item.registerPoem(poemList)
            item.registerPoemGeneration(poemList)
        }

        screenHandlerType.register()

        advancement?.init()

    }
}
