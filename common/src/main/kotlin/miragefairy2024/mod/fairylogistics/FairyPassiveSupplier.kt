package miragefairy2024.mod.fairylogistics

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.lib.MachineBlockEntity
import miragefairy2024.lib.MachineScreenHandler
import miragefairy2024.mod.fairy.FairyCard
import miragefairy2024.mod.fairybuilding.FairyFactoryBlockEntity
import miragefairy2024.mod.materials.BlockMaterialCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.generator
import miragefairy2024.util.on
import miragefairy2024.util.register
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerShapedRecipeGeneration
import mirrg.kotlin.hydrogen.floorToInt
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

// TODO WIP
object FairyPassiveSupplierCard : FairyLogisticsCard<FairyPassiveSupplierBlock, FairyPassiveSupplierBlockEntity, FairyPassiveSupplierScreenHandler>() {
    override fun getPath() = "fairy_passive_supplier"
    override val tier = 3
    override val name = EnJa("Fairy Passive Supplier(WIP)", "妖精の郵便屋さん(WIP)") // TODO rename
    override val poem = EnJa("Fairies' Delivery Service", "落ち込んだりもしたけれど、私は元気です。")
    override val description = EnJa("Accepts and delivers orders", "注文を受け付けて配達する")

    override fun createBlockSettings(): BlockBehaviour.Properties = super.createBlockSettings().mapColor(MapColor.COLOR_PINK).sound(SoundType.WOOD)
    override fun createBlock() = FairyPassiveSupplierBlock(this)

    override fun createBlockEntityAccessor() = BlockEntityAccessor(::FairyPassiveSupplierBlockEntity)

    override fun createScreenHandler(arguments: MachineScreenHandler.Arguments) = FairyPassiveSupplierScreenHandler(this, arguments)

    override val guiWidth = 176
    override val guiHeight = 132

    class Slot(
        override val x: Int,
        override val y: Int,
    ) : MachineBlockEntity.InventorySlotConfiguration, MachineScreenHandler.GuiSlotConfiguration {
        override fun isValid(itemStack: ItemStack) = itemStack.`is`(FairyCard.item())
        override fun canInsert(direction: Direction) = true
        override fun canExtract(direction: Direction) = true
        override val isObservable = false
        override val dropItem = true
        override fun getTooltip() = null
    }

    val FAIRY_SLOT = Slot(80, 19)

    context(ModContext)
    override fun init() {
        super.init()

        Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("fairy_passive_supplier")) { FairyPassiveSupplierBlock.CODEC }.register()


        BlockTags.MINEABLE_WITH_AXE.generator.registerChild(block)


        inventorySlotConfigurations += FAIRY_SLOT
        guiSlotConfigurations += FAIRY_SLOT


        registerShapedRecipeGeneration(item) {
            pattern("#A#")
            pattern("DCD")
            pattern("###")
            define('A', BlockMaterialCard.AURA_STONE.item())
            define('#', ItemTags.PLANKS)
            define('C', Items.BARREL)
            define('D', Items.PINK_DYE)
        } on BlockMaterialCard.AURA_STONE.item
    }
}

class FairyPassiveSupplierBlock(card: FairyPassiveSupplierCard) : FairyLogisticsBlock(card) {
    companion object {
        val CODEC: MapCodec<FairyPassiveSupplierBlock> = simpleCodec { FairyPassiveSupplierBlock(FairyPassiveSupplierCard) }
        private val SHAPES: Array<VoxelShape> = arrayOf(
            // UP
            box(2.0, 4.0, 8.0, 14.0, 16.0, 16.0), // SOUTH
            box(0.0, 4.0, 2.0, 8.0, 16.0, 14.0), // WEST
            box(2.0, 4.0, 0.0, 14.0, 16.0, 8.0), // NORTH
            box(8.0, 4.0, 2.0, 16.0, 16.0, 14.0), // EAST

            // SIDE
            box(2.0, 2.0, 8.0, 14.0, 14.0, 16.0), // SOUTH
            box(0.0, 2.0, 2.0, 8.0, 14.0, 14.0), // WEST
            box(2.0, 2.0, 0.0, 14.0, 14.0, 8.0), // NORTH
            box(8.0, 2.0, 2.0, 16.0, 14.0, 14.0), // EAST

            // DOWN
            box(2.0, 0.0, 8.0, 14.0, 12.0, 16.0), // SOUTH
            box(0.0, 0.0, 2.0, 8.0, 12.0, 14.0), // WEST
            box(2.0, 0.0, 0.0, 14.0, 12.0, 8.0), // NORTH
            box(8.0, 0.0, 2.0, 16.0, 12.0, 14.0), // EAST
        )
    }

    override fun codec() = CODEC

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getShape(state: BlockState, world: BlockGetter, pos: BlockPos, context: CollisionContext) = SHAPES[4 * state.getValue(VERTICAL_FACING).id + state.getValue(FACING).get2DDataValue()]
}

class FairyPassiveSupplierBlockEntity(private val card: FairyPassiveSupplierCard, pos: BlockPos, state: BlockState) : FairyLogisticsBlockEntity<FairyPassiveSupplierBlockEntity>(card, pos, state) {
    companion object {
        fun getLogisticsPower(itemStack: ItemStack): Int {
            if (!itemStack.`is`(FairyCard.item())) return 0
            return (FairyFactoryBlockEntity.getFairyLevel(itemStack) * 10.0).floorToInt()
        }
    }

    override fun getThis() = this

    var t = -1
    var logisticsEnergy = 0

    private fun getLogisticsPower(): Int {
        val inventorySlotIndex = card.inventorySlotIndexTable[FairyPassiveSupplierCard.FAIRY_SLOT] ?: return 0
        val fairyItemStack = getItem(inventorySlotIndex)
        return getLogisticsPower(fairyItemStack)
    }

    override fun serverTick(world: Level, pos: BlockPos, state: BlockState) {
        super.serverTick(world, pos, state)

        // 1分に1回発動する
        if (t == -1) t = world.random.nextInt(20 * 60)
        t--
        if (t > 0) return
        t = 20 * 60

        logisticsEnergy = getLogisticsPower()

    }

}

class FairyPassiveSupplierScreenHandler(card: FairyPassiveSupplierCard, arguments: Arguments) : FairyLogisticsScreenHandler(card, arguments)
