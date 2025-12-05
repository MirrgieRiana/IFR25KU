package miragefairy2024.mod.fairybuilding

import miragefairy2024.ModContext
import miragefairy2024.RenderingProxy
import miragefairy2024.lib.HorizontalFacingMachineBlock
import miragefairy2024.lib.MachineBlockEntity
import miragefairy2024.lib.MachineCard
import miragefairy2024.lib.MachineScreenHandler
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.util.generator
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.getOrNull
import miragefairy2024.util.isNotIn
import miragefairy2024.util.normal
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerRenderingProxyBlockEntityRendererFactory
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.times
import miragefairy2024.util.withHorizontalRotation
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.tags.BlockTags
import net.minecraft.util.Mth
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.pathfinder.PathComputationType
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

abstract class FairyBuildingCard<B : FairyBuildingBlock, E : FairyBuildingBlockEntity<E>, H : FairyBuildingScreenHandler> : MachineCard<B, E, H>() {
    companion object {
        fun <E> PropertyConfiguration(
            getter: E.() -> Int,
            setter: E.(Int) -> Unit,
            encoder: (Int) -> Short = { it.toShort() },
            decoder: (Short) -> Int = { it.toInt() },
        ): MachineScreenHandler.PropertyConfiguration<E> {
            return object : MachineScreenHandler.PropertyConfiguration<E> {
                override fun get(blockEntity: E) = getter(blockEntity)
                override fun set(blockEntity: E, value: Int) = setter(blockEntity, value)
                override fun encode(value: Int) = encoder(value)
                override fun decode(data: Short) = decoder(data)
            }
        }

        fun ac(motion: FairyAnimation.Motion, positions: List<FairyAnimation.Position>): FairyAnimation.Configuration {
            return object : FairyAnimation.Configuration {
                override val motion = motion
                override val positions = positions
                override fun getSpeed(blockEntity: FairyBuildingBlockEntity<*>) = if (blockEntity.doMovePosition) 1.0 else 0.0
            }
        }

        val NONE = FairyAnimation.Motion.NONE
        val FAIRY = FairyAnimation.Motion.FAIRY

        fun p(x: Double, y: Double, z: Double, pitch: Float, yaw: Float, duration: Double) = listOf(FairyAnimation.Position(x, y, z, pitch, yaw, duration))
    }


    // Block

    override fun createBlockSettings(): BlockBehaviour.Properties = BlockBehaviour.Properties.of().noOcclusion().strength(2.0F).instrument(NoteBlockInstrument.BASS).sound(SoundType.WOOD).mapColor(MapColor.RAW_IRON)


    // Slot

    class FairyBuildingSlotConfiguration(
        override val x: Int,
        override val y: Int,
        override val dropItem: Boolean = true,
        val insertDirections: Set<Direction> = setOf(),
        val extractDirections: Set<Direction> = setOf(),
        val animation: FairyAnimation.Configuration? = null,
        val tooltipGetter: (() -> List<Component>)? = null,
        val filter: (ItemStack) -> Boolean = { true },
    ) : MachineBlockEntity.InventorySlotConfiguration, MachineScreenHandler.GuiSlotConfiguration {
        override fun isValid(itemStack: ItemStack) = filter(itemStack)
        override fun canInsert(direction: Direction) = direction in insertDirections
        override fun canExtract(direction: Direction) = direction in extractDirections
        override val isObservable = animation != null
        override fun getTooltip() = tooltipGetter?.invoke()
    }

    open fun createSlotConfigurations(): List<FairyBuildingSlotConfiguration> = listOf()


    // Property

    open fun createPropertyConfigurations(): List<MachineScreenHandler.PropertyConfiguration<E>> = listOf()


    context(ModContext)
    override fun init() {
        super.init()

        item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        block.registerVariantsBlockStateGeneration { normal("block/" * block().getIdentifier()).withHorizontalRotation(HorizontalDirectionalBlock.FACING) }
        block.registerCutoutRenderLayer()
        blockEntityType.registerRenderingProxyBlockEntityRendererFactory()

        BlockTags.MINEABLE_WITH_AXE.generator.registerChild(block)
        FAIRY_BUILDING_BLOCK_TAG.generator.registerChild(block)

        block.registerDefaultLootTableGeneration()


        val slotConfigurations = createSlotConfigurations()

        inventorySlotConfigurations += slotConfigurations
        guiSlotConfigurations += slotConfigurations
        propertyConfigurations += createPropertyConfigurations()

        slotConfigurations.forEach {
            if (it.animation != null) {
                animationConfigurations += object : MachineBlockEntity.AnimationConfiguration<E> {
                    override fun createAnimation(): MachineBlockEntity.Animation<E>? {
                        val inventorySlotIndex = inventorySlotIndexTable[it] ?: return null
                        return FairyAnimation(inventorySlotIndex, it.animation)
                    }
                }
            }
        }

    }
}

abstract class FairyBuildingBlock(private val card: FairyBuildingCard<*, *, *>) : HorizontalFacingMachineBlock(card) {
    companion object {
        private val SHAPE = Shapes.or(
            box(0.0, 0.0, 0.0, 16.0, 16.0, 0.1),
            box(0.0, 0.0, 0.0, 16.0, 0.1, 16.0),
            box(0.0, 0.0, 0.0, 0.1, 16.0, 16.0),
            box(0.0, 0.0, 15.9, 16.0, 16.0, 16.0),
            box(0.0, 15.9, 0.0, 16.0, 16.0, 16.0),
            box(15.9, 0.0, 0.0, 16.0, 16.0, 16.0),
        )
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getLightBlock(state: BlockState, world: BlockGetter, pos: BlockPos) = 6

    @Suppress("OVERRIDE_DEPRECATION")
    override fun hasAnalogOutputSignal(state: BlockState) = true

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getAnalogOutputSignal(state: BlockState, world: Level, pos: BlockPos) = card.blockEntityAccessor.castOrNull(world.getBlockEntity(pos))?.getComparatorOutput() ?: 0

    @Suppress("OVERRIDE_DEPRECATION")
    override fun isPathfindable(state: BlockState, pathComputationType: PathComputationType) = false

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getShape(state: BlockState, world: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = SHAPE

}

abstract class FairyBuildingBlockEntity<E : FairyBuildingBlockEntity<E>>(private val card: FairyBuildingCard<*, E, *>, pos: BlockPos, state: BlockState) : MachineBlockEntity<E>(card, pos, state) {

    // Container

    override fun getActualSide(side: Direction) = HorizontalFacingMachineBlock.getActualSide(blockState, side)


    // Move

    open fun getComparatorOutput() = 0


    // Rendering

    open val doMovePosition get() = false

    override fun render(renderingProxy: RenderingProxy, tickDelta: Float, light: Int, overlay: Int) {
        val world = level ?: return
        val blockState = world.getBlockState(worldPosition)
        if (blockState isNotIn card.block()) return
        val direction = blockState.getOrNull(HorizontalDirectionalBlock.FACING) ?: return

        renderingProxy.stack {
            renderingProxy.translate(0.5, 0.5, 0.5)
            renderingProxy.rotateY(-((direction.get2DDataValue() + 2) * 90) / 180F * Math.PI.toFloat())
            renderingProxy.translate(-0.5, -0.5, -0.5)

            renderRotated(renderingProxy, tickDelta, light, overlay)

        }
    }

}

class FairyAnimation(private val inventorySlotIndex: Int, private val animation: Configuration) : MachineBlockEntity.Animation<FairyBuildingBlockEntity<*>> {

    interface Configuration {
        val motion: Motion
        val positions: List<Position>
        fun getSpeed(blockEntity: FairyBuildingBlockEntity<*>): Double
    }

    enum class Motion {
        NONE,
        FAIRY,
    }

    /**
     * @param x 1/16 scale
     * @param y 1/16 scale
     * @param z 1/16 scale
     * @param pitch degree
     * @param yaw degree
     */
    class Position(val x: Double, val y: Double, val z: Double, val pitch: Float, val yaw: Float, val duration: Double)

    init {
        check(animation.positions.isNotEmpty())
    }

    private var index = 0
    private var position = animation.positions[index]
    private var countdown = position.duration

    var ticks = (Math.random() * 1000).toInt()
    var xSpeed = 0.0
    var ySpeed = 0.0
    var zSpeed = 0.0
    var yawSpeed = 0.0F
    var pitchSpeed = 0.0F
    var x = position.x
    var y = position.y
    var z = position.z
    var yaw = position.yaw
    var pitch = position.pitch

    override fun tick(blockEntity: FairyBuildingBlockEntity<*>) {
        val world = blockEntity.level ?: return

        // 定位置の切り替え
        val speed = animation.getSpeed(blockEntity)
        if (speed > 0) {
            countdown -= speed
            if (countdown <= 0) {

                index++
                if (index >= animation.positions.size) index = 0

                position = animation.positions[index]
                countdown = animation.positions[index].duration * (1.0 + world.random.nextDouble() * 0.1)

            }
        }

        // 妖精の移動
        ticks++
        xSpeed = (position.x - x) * 0.1
        ySpeed = (position.y - y) * 0.1
        zSpeed = (position.z - z) * 0.1
        yawSpeed = (position.yaw - yaw) * 0.1F
        pitchSpeed = (position.pitch - pitch) * 0.1F
        x += xSpeed
        y += ySpeed
        z += zSpeed
        yaw += yawSpeed
        pitch += pitchSpeed

    }

    override fun render(blockEntity: FairyBuildingBlockEntity<*>, renderingProxy: RenderingProxy, tickDelta: Float) {
        val cX = x + xSpeed * tickDelta.toDouble()
        val cY = y + ySpeed * tickDelta.toDouble()
        val cZ = z + zSpeed * tickDelta.toDouble()
        val cYaw = yaw + yawSpeed * tickDelta
        val cPitch = pitch + pitchSpeed * tickDelta
        val yawOffset = when (animation.motion) {
            Motion.NONE -> 0F
            Motion.FAIRY -> Mth.sin((ticks.toFloat() + tickDelta) * 0.03F) * 3F
        }
        val pitchOffset = when (animation.motion) {
            Motion.NONE -> 0F
            Motion.FAIRY -> Mth.sin((ticks.toFloat() + tickDelta) * 0.08F) * 5F
        }

        renderingProxy.stack {
            renderingProxy.translate(cX / 16.0, cY / 16.0, cZ / 16.0) // 移動
            renderingProxy.rotateY(-cYaw / 180F * Mth.PI) // 横回転
            renderingProxy.rotateX(-cPitch / 180F * Mth.PI) // 足元を起点にして縦回転
            renderingProxy.scale(0.5F, 0.5F, 0.5F) // 縮小

            when (animation.motion) {
                Motion.NONE -> Unit

                Motion.FAIRY -> {
                    renderingProxy.translate(0.0, 0.25, 0.0)
                    renderingProxy.rotateY(-yawOffset / 180F * Mth.PI) // 横回転
                    renderingProxy.rotateZ(-pitchOffset / 180F * Mth.PI) // 上下回転
                    renderingProxy.translate(0.0, -0.25, 0.0)
                }
            }

            renderingProxy.translate(0.0, 2.0 / 16.0, 0.0) // なぜか4ドット分下に埋まるのを補正
            renderingProxy.renderItemStack(blockEntity.getItem(inventorySlotIndex))
        }
    }
}

open class FairyBuildingScreenHandler(card: FairyBuildingCard<*, *, *>, arguments: Arguments) : MachineScreenHandler(card, arguments)
