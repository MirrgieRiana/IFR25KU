package miragefairy2024.mod.fairylogistics

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.lib.MachineBlockEntity
import miragefairy2024.lib.MachineScreenHandler
import miragefairy2024.mod.materials.BlockMaterialCard
import miragefairy2024.mod.materials.Material
import miragefairy2024.mod.materials.Shape
import miragefairy2024.mod.materials.tagOf
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.createItemStack
import miragefairy2024.util.get
import miragefairy2024.util.hasSameItemAndComponents
import miragefairy2024.util.mergeInventory
import miragefairy2024.util.on
import miragefairy2024.util.register
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.toInventoryDelegate
import miragefairy2024.util.toSidedInventoryDelegate
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.SectionPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.projectile.Arrow
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

// TODO WIP
object FairyActiveConsumerCard : FairyLogisticsCard<FairyActiveConsumerBlock, FairyActiveConsumerBlockEntity, FairyActiveConsumerScreenHandler>() {
    override fun getPath() = "fairy_active_consumer"
    override val tier = 3
    override val name = EnJa("Fairy Active Consumer(WIP)", "妖精の郵便受け(WIP)") // TODO rename
    override val poem = EnJa("Tonight, I'll Be Eating...", "焼き鯖だよ――")
    override val description = EnJa("The ordered items are delivered", "注文したアイテムが搬入される")

    override fun createBlockSettings(): BlockBehaviour.Properties = super.createBlockSettings().mapColor(MapColor.ICE).sound(SoundType.METAL)
    override fun createBlock() = FairyActiveConsumerBlock(this)

    override fun createBlockEntityAccessor() = BlockEntityAccessor(::FairyActiveConsumerBlockEntity)

    override fun createScreenHandler(arguments: MachineScreenHandler.Arguments) = FairyActiveConsumerScreenHandler(this, arguments)

    override val guiWidth = 176
    override val guiHeight = 168

    class Slot(
        override val x: Int,
        override val y: Int,
    ) : MachineBlockEntity.InventorySlotConfiguration, MachineScreenHandler.GuiSlotConfiguration {
        override fun isValid(itemStack: ItemStack) = true
        override fun canInsert(direction: Direction) = true
        override fun canExtract(direction: Direction) = true
        override val isObservable = false
        override val dropItem = true
        override fun getTooltip() = null
    }

    val SLOTS = (0 until 3).flatMap { c ->
        (0 until 9).map { r ->
            Slot(8 + 18 * r, 19 + 18 * c)
        }
    }
    val FILTER_SLOT = SLOTS.last()
    val CONTAINER_SLOTS = SLOTS.filter { it != FILTER_SLOT }

    context(ModContext)
    override fun init() {
        super.init()

        Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("fairy_active_consumer")) { FairyActiveConsumerBlock.CODEC }.register()


        inventorySlotConfigurations += SLOTS
        guiSlotConfigurations += SLOTS


        registerShapedRecipeGeneration(item) {
            pattern("#A#")
            pattern("DCD")
            pattern("###")
            define('A', BlockMaterialCard.AURA_STONE.item())
            define('#', tagOf(Shape.INGOT, Material.IRON))
            define('C', Items.ITEM_FRAME)
            define('D', Items.LIGHT_BLUE_DYE)
        } on BlockMaterialCard.AURA_STONE.item
    }
}

class FairyActiveConsumerBlock(card: FairyActiveConsumerCard) : FairyLogisticsBlock(card) {
    companion object {
        val CODEC: MapCodec<FairyActiveConsumerBlock> = simpleCodec { FairyActiveConsumerBlock(FairyActiveConsumerCard) }
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
    override fun getShape(state: BlockState, world: BlockGetter, pos: BlockPos, context: CollisionContext) = SHAPES[4 * state[VERTICAL_FACING].id + state[FACING].get2DDataValue()]
}

class FairyActiveConsumerBlockEntity(private val card: FairyActiveConsumerCard, pos: BlockPos, state: BlockState) : FairyLogisticsBlockEntity<FairyActiveConsumerBlockEntity>(card, pos, state) {
    companion object {
        private fun getSquaredDistance(a: BlockPos, b: BlockPos): Int {
            val dx = a.x - b.x
            val dy = a.y - b.y
            val dz = a.z - b.z
            return dx * dx + dy * dy + dz * dz
        }
    }

    override fun getThis() = this

    private var t = -1

    override fun serverTick(world: Level, pos: BlockPos, state: BlockState) {
        super.serverTick(world, pos, state)

        // 1分に1回発動する
        if (t == -1) t = world.random.nextInt(20 * 60)
        t--
        if (t > 0) return
        t = 20 * 60

        // 一旦すべてのアイテムを配送
        run {
            val (targetInventory, targetSide) = getTarget() ?: return@run
            val result = mergeInventory(
                this.toInventoryDelegate(),
                targetInventory.toSidedInventoryDelegate(targetSide),
                srcIndices = FairyActiveConsumerCard.CONTAINER_SLOTS.mapNotNull { card.inventorySlotIndexTable[it] },
            )
            if (result.movedItemCount > 0) setChanged()
        }

        // フィルタを取得
        val filterItemStack = getItem(card.inventorySlotIndexTable[FairyActiveConsumerCard.FILTER_SLOT]!!)
        if (filterItemStack.isEmpty) return

        // 空きスロットを列挙
        val availableDestIndices = FairyActiveConsumerCard.CONTAINER_SLOTS
            .mapNotNull { card.inventorySlotIndexTable[it] }
            .filter { getItem(it).isEmpty }
            .toMutableList()
        if (availableDestIndices.isEmpty()) return

        // 対象範囲の配送所を列挙する
        val centerChunkX = SectionPos.blockToSectionCoord(pos.x)
        val centerChunkZ = SectionPos.blockToSectionCoord(pos.z)
        val neighbourChunksSuppliers = (centerChunkX - 1..centerChunkX + 1).flatMap { chunkX ->
            (centerChunkZ - 1..centerChunkZ + 1).flatMap { chunkZ ->
                world.getChunk(chunkX, chunkZ).blockEntities.values.mapNotNull { it as? FairyPassiveSupplierBlockEntity } // TODO interface
            }
        }
        val reachingSuppliers = neighbourChunksSuppliers.filter { getSquaredDistance(pos, it.blockPos) <= 16 * 16 }

        // 視線判定
        val posD = pos.center
        val entity = Arrow(world, posD.x, posD.y, posD.z, Items.ARROW.createItemStack(), null)
        val unblockedSuppliers = reachingSuppliers.filter { supplier ->
            val supplierPosD = supplier.blockPos.center
            val hitResult = world.clip(ClipContext(posD, supplierPosD, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity))
            hitResult.type == HitResult.Type.MISS
        }

        // すべてのSupplierに対して、フィルタにマッチするアイテムを片っ端から輸送させる
        // ただし残りの輸送速度を超えない
        var changed = false
        run finishSuppliers@{
            unblockedSuppliers.forEach { supplier ->
                if (supplier.logisticsEnergy == 0) return@forEach
                val (supplierTargetInventory, supplierTargetSide) = supplier.getTarget() ?: return@forEach
                val src = supplierTargetInventory.toSidedInventoryDelegate(supplierTargetSide)

                var srcChanged = false
                run finishSrcIndices@{
                    src.getIndices().forEach nextSrcIndex@{ srcIndex ->
                        val srcItemStack = src.getItemStack(srcIndex)
                        if (srcItemStack.isEmpty) return@nextSrcIndex
                        if (!(srcItemStack hasSameItemAndComponents filterItemStack)) return@nextSrcIndex
                        if (!src.canExtract(srcIndex, srcItemStack)) return@nextSrcIndex
                        val destIndex = availableDestIndices.removeFirst()
                        src.setItemStack(srcIndex, EMPTY_ITEM_STACK)
                        srcChanged = true
                        setItem(destIndex, srcItemStack)
                        changed = true
                        if (availableDestIndices.isEmpty()) return@finishSrcIndices
                    }
                }
                if (srcChanged) src.markDirty()

                if (availableDestIndices.isEmpty()) return@finishSuppliers
            }
        }
        if (changed) setChanged()

    }
}

class FairyActiveConsumerScreenHandler(card: FairyActiveConsumerCard, arguments: Arguments) : FairyLogisticsScreenHandler(card, arguments)
