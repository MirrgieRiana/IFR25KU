package miragefairy2024.mod.placeditem

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.RenderingProxy
import miragefairy2024.RenderingProxyBlockEntity
import miragefairy2024.util.BlockEntityType
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.Model
import miragefairy2024.util.ModelData
import miragefairy2024.util.ModelElementsData
import miragefairy2024.util.ModelTexturesData
import miragefairy2024.util.Registration
import miragefairy2024.util.compound
import miragefairy2024.util.createItemStack
import miragefairy2024.util.double
import miragefairy2024.util.generator
import miragefairy2024.util.get
import miragefairy2024.util.isNotIn
import miragefairy2024.util.register
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerRenderingProxyBlockEntityRendererFactory
import miragefairy2024.util.registerSingletonBlockStateGeneration
import miragefairy2024.util.string
import miragefairy2024.util.toItemStack
import miragefairy2024.util.toNbt
import miragefairy2024.util.with
import miragefairy2024.util.wrapper
import mirrg.kotlin.hydrogen.atLeast
import mirrg.kotlin.hydrogen.atMost
import mirrg.kotlin.hydrogen.castOrNull
import mirrg.kotlin.hydrogen.max
import mirrg.kotlin.hydrogen.min
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.models.model.TextureSlot
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.util.Mth
import net.minecraft.world.item.Items
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.PushReaction
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

object PlacedItemCard {
    val identifier = MirageFairy2024.identifier("placed_item")
    val block = Registration(BuiltInRegistries.BLOCK, identifier) { PlacedItemBlock(BlockBehaviour.Properties.of().noCollission().strength(0.2F).pushReaction(PushReaction.DESTROY)) }
    val blockEntityType = Registration(BuiltInRegistries.BLOCK_ENTITY_TYPE, identifier) { BlockEntityType(::PlacedItemBlockEntity, setOf(block.await())) }
}

context(ModContext)
fun initPlacedItemBlock() {
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("placed_item")) { PlacedItemBlock.CODEC }.register()

    PlacedItemCard.let { card ->
        card.block.register()
        card.blockEntityType.register()

        card.block.registerSingletonBlockStateGeneration()
        card.block.registerModelGeneration {
            Model {
                ModelData(
                    parent = ResourceLocation.fromNamespaceAndPath("minecraft", "block/block"),
                    textures = ModelTexturesData(
                        TextureSlot.PARTICLE.id to ResourceLocation.fromNamespaceAndPath("minecraft", "block/glass").string,
                    ),
                    elements = ModelElementsData(),
                )
            }.with()
        }
        card.blockEntityType.registerRenderingProxyBlockEntityRendererFactory()

        BlockTags.MINEABLE_WITH_HOE.generator.registerChild(card.block)
    }
}

@Suppress("OVERRIDE_DEPRECATION")
class PlacedItemBlock(settings: Properties) : Block(settings), EntityBlock {
    companion object {
        val CODEC: MapCodec<PlacedItemBlock> = simpleCodec(::PlacedItemBlock)
    }

    override fun codec() = CODEC

    override fun newBlockEntity(pos: BlockPos, state: BlockState) = PlacedItemBlockEntity(pos, state)

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun triggerEvent(state: BlockState, world: Level, pos: BlockPos, type: Int, data: Int): Boolean {
        super.triggerEvent(state, world, pos, type, data)
        val blockEntity = world.getBlockEntity(pos) ?: return false
        return blockEntity.triggerEvent(type, data)
    }

    // レンダリング
    override fun getRenderShape(state: BlockState) = RenderShape.ENTITYBLOCK_ANIMATED
    override fun getShape(state: BlockState, world: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        val blockEntity = world.getBlockEntity(pos) as? PlacedItemBlockEntity ?: return Shapes.block()
        return blockEntity.shapeCache ?: Shapes.block()
    }

    // 格納されているアイテムをドロップする
    override fun getCloneItemStack(world: LevelReader, pos: BlockPos, state: BlockState) = world.getBlockEntity(pos).castOrNull<PlacedItemBlockEntity>()?.itemStack ?: EMPTY_ITEM_STACK
    override fun onRemove(state: BlockState, world: Level, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (state isNotIn newState.block) run {
            val blockEntity = world.getBlockEntity(pos) as? PlacedItemBlockEntity ?: return@run
            popResource(world, pos, blockEntity.itemStack)
        }
        @Suppress("DEPRECATION")
        super.onRemove(state, world, pos, newState, moved)
    }

}

// TODO 右クリックで立てたりする
class PlacedItemBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(PlacedItemCard.blockEntityType(), pos, state), RenderingProxyBlockEntity {
    companion object {
        private val INVALID_ITEM_STACK = Items.BARRIER.createItemStack()
    }


    var itemStack = EMPTY_ITEM_STACK
    var itemX = 8.0 / 16.0
    var itemY = 0.5 / 16.0
    var itemZ = 8.0 / 16.0
    var itemRotateX = -Mth.TWO_PI * 0.25
    var itemRotateY = 0.0
    var shapeCache: VoxelShape? = null


    override fun saveAdditional(nbt: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(nbt, registries)
        nbt.wrapper["ItemStack"].set(itemStack.toNbt(registries))
        nbt.wrapper["ItemX"].double.set(itemX)
        nbt.wrapper["ItemY"].double.set(itemY)
        nbt.wrapper["ItemZ"].double.set(itemZ)
        nbt.wrapper["ItemRotateX"].double.set(itemRotateX)
        nbt.wrapper["ItemRotateY"].double.set(itemRotateY)
    }

    override fun loadAdditional(nbt: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(nbt, registries)
        itemStack = nbt.wrapper["ItemStack"].compound.get()?.toItemStack(registries) ?: EMPTY_ITEM_STACK
        itemX = nbt.wrapper["ItemX"].double.get() ?: 0.0
        itemY = nbt.wrapper["ItemY"].double.get() ?: 0.0
        itemZ = nbt.wrapper["ItemZ"].double.get() ?: 0.0
        itemRotateX = nbt.wrapper["ItemRotateX"].double.get() ?: 0.0
        itemRotateY = nbt.wrapper["ItemRotateY"].double.get() ?: 0.0
        updateShapeCache()
    }

    fun updateShapeCache() {
        shapeCache = run {

            var minX = 0.0
            var minY = 0.0
            var minZ = 0.0
            var maxX = 0.0
            var maxY = 0.0
            var maxZ = 0.0

            fun extend(x: Double, y: Double, z: Double) {
                var x2 = x
                var y2 = y
                var z2 = z

                run {
                    val y3 = Mth.sin(-itemRotateX.toFloat()).toDouble() * z2 + Mth.cos(-itemRotateX.toFloat()).toDouble() * y2
                    val z3 = Mth.cos(-itemRotateX.toFloat()).toDouble() * z2 - Mth.sin(-itemRotateX.toFloat()).toDouble() * y2
                    y2 = y3
                    z2 = z3
                }

                run {
                    val x3 = Mth.sin(itemRotateY.toFloat()).toDouble() * z2 + Mth.cos(itemRotateY.toFloat()).toDouble() * x2
                    val z3 = Mth.cos(itemRotateY.toFloat()).toDouble() * z2 - Mth.sin(itemRotateY.toFloat()).toDouble() * x2
                    x2 = x3
                    z2 = z3
                }

                minX = minX min x2
                minY = minY min y2
                minZ = minZ min z2
                maxX = maxX max x2
                maxY = maxY max y2
                maxZ = maxZ max z2
            }

            extend(-5.0, -5.0, -0.5)
            extend(-5.0, -5.0, +1.5)
            extend(-5.0, +5.0, -0.5)
            extend(-5.0, +5.0, +1.5)
            extend(+5.0, -5.0, -0.5)
            extend(+5.0, -5.0, +1.5)
            extend(+5.0, +5.0, -0.5)
            extend(+5.0, +5.0, +1.5)

            Block.box(
                itemX * 16.0 + minX atLeast 0.0,
                itemY * 16.0 + minY atLeast 0.0,
                itemZ * 16.0 + minZ atLeast 0.0,
                itemX * 16.0 + maxX atMost 16.0,
                itemY * 16.0 + maxY atMost 16.0,
                itemZ * 16.0 + maxZ atMost 16.0,
            )
        }
    }

    override fun getUpdateTag(registries: HolderLookup.Provider): CompoundTag = saveWithoutMetadata(registries)
    override fun getUpdatePacket(): Packet<ClientGamePacketListener>? = ClientboundBlockEntityDataPacket.create(this)


    override fun render(renderingProxy: RenderingProxy, tickDelta: Float, light: Int, overlay: Int) {
        renderingProxy.stack {
            renderingProxy.translate(itemX, itemY, itemZ)
            renderingProxy.rotateY(itemRotateY.toFloat())
            renderingProxy.rotateX(itemRotateX.toFloat())
            renderingProxy.scale(0.5F, 0.5F, 0.5F)
            renderingProxy.rotateY(Mth.PI)
            renderingProxy.renderFixedItemStack(if (itemStack.isEmpty) INVALID_ITEM_STACK else itemStack)
        }
    }

}
