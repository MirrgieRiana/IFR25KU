package miragefairy2024.mod.machine

import com.mojang.serialization.MapCodec
import dev.architectury.registry.fuel.FuelRegistry
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.lib.BlockEntityStorage
import miragefairy2024.lib.MachineScreenHandler
import miragefairy2024.lib.ProxyBlockEntityStorage
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.fairybuilding.FairyBuildingCard
import miragefairy2024.mod.materials.BlockMaterialCard
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.poem
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.Model
import miragefairy2024.util.Registration
import miragefairy2024.util.TextureMapping
import miragefairy2024.util.createEmptyModel
import miragefairy2024.util.createItemStack
import miragefairy2024.util.generator
import miragefairy2024.util.get
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.int
import miragefairy2024.util.isIn
import miragefairy2024.util.isNotIn
import miragefairy2024.util.normal
import miragefairy2024.util.on
import miragefairy2024.util.register
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerLootTableGeneration
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.rotateY90
import miragefairy2024.util.times
import miragefairy2024.util.with
import miragefairy2024.util.withHorizontalRotation
import miragefairy2024.util.wrapper
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.models.model.TextureSlot
import net.minecraft.nbt.CompoundTag
import net.minecraft.tags.BlockTags
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.LevelEvent
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.pathfinder.PathComputationType
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

object AthanorCard : SimpleMachineCard<AthanorBlock, AthanorBlockEntity, AthanorScreenHandler, AthanorRecipe>() {
    override fun createIdentifier() = MirageFairy2024.identifier("athanor")
    override fun createName() = EnJa("Athanor", "アタノール")
    override fun createPoemList() = PoemList(2).poem(EnJa("Long-term heating by fuel tower", "天をつらぬく燃料塔。")) // 油脂、灰、そして金属塩。 // 時空に聳えるフロギストン。
    override fun createBlockSettings(): BlockBehaviour.Properties = BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).requiresCorrectToolForDrops().strength(2.0F, 6.0F).noOcclusion().lightLevel(Blocks.litBlockEmission(12))
    override fun createBlock() = AthanorBlock(this)
    override fun createBlockEntityAccessor() = BlockEntityAccessor(::AthanorBlockEntity)
    override fun createScreenHandler(arguments: MachineScreenHandler.Arguments) = AthanorScreenHandler(this, arguments)
    override val guiWidth = 176
    override val guiHeight = 174

    override val inputSlots = listOf(
        SlotConfiguration(40, 17, setOf(Direction.NORTH), setOf()),
        SlotConfiguration(18, 39, setOf(Direction.WEST), setOf()),
        SlotConfiguration(62, 39, setOf(Direction.EAST), setOf()),
        SlotConfiguration(40, 61, setOf(Direction.SOUTH), setOf()),
    )
    val fuelSlot = object : SlotConfiguration(40, 39, setOf(Direction.UP), setOf()) {
        override fun isValid(itemStack: ItemStack) = FuelRegistry.get(itemStack) != 0
    }
    override val outputSlots = listOf(
        SlotConfiguration(120, 30, setOf(), setOf(Direction.DOWN)),
        SlotConfiguration(138, 30, setOf(), setOf(Direction.DOWN)),
        SlotConfiguration(120, 48, setOf(), setOf(Direction.DOWN)),
        SlotConfiguration(138, 48, setOf(), setOf(Direction.DOWN)),
    )
    override val slots = inputSlots + fuelSlot + outputSlots

    val FUEL_PROPERTY = FairyBuildingCard.PropertyConfiguration<AthanorBlockEntity>({ fuel }, { fuel = it })
    val FUEL_MAX_PROPERTY = FairyBuildingCard.PropertyConfiguration<AthanorBlockEntity>({ fuelMax }, { fuelMax = it })
    override val properties = super.properties + FUEL_PROPERTY + FUEL_MAX_PROPERTY

    override val recipeType = AthanorRecipeCard.type

    override fun createAdvancement() = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { MaterialCard.FAIRY_CRYSTAL.advancement!!.await() },
        icon = { item().createItemStack() },
        name = EnJa("Magical Furnace", "魔法の炉"),
        description = EnJa("Craft an Athanor from a Fairy Pot, Fairy Ceramic Bricks, and a few other materials", "妖精のポット、妖精のセラミックレンガ、およびいくつかの素材からアタノールを作る"),
        criterion = AdvancementCard.hasItem(item),
        type = AdvancementCardType.NORMAL,
    )

    context(ModContext)
    override fun init() {
        super.init()

        Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("athanor")) { AthanorBlock.CODEC }.register()

        val main = TextureSlot.create("main")
        registerModelGeneration({ "block/" * identifier * "_lit" }) { Model("block/" * identifier, main) with TextureMapping(main to "block/" * identifier * "_lit") }
        registerModelGeneration({ "block/" * identifier * "_top" }) { createEmptyModel("block/" * BlockMaterialCard.FAIRY_CERAMIC_BRICKS.identifier) }
        registerModelGeneration({ "block/" * identifier * "_top_lit" }) { createEmptyModel("block/" * BlockMaterialCard.FAIRY_CERAMIC_BRICKS.identifier) }

        block.registerCutoutRenderLayer()

        BlockTags.MINEABLE_WITH_PICKAXE.generator.registerChild(block)
        BlockTags.NEEDS_STONE_TOOL.generator.registerChild(block)

        registerShapedRecipeGeneration(item) {
            pattern("BIB")
            pattern("BPB")
            pattern("BFB")
            define('B', BlockMaterialCard.FAIRY_CERAMIC_BRICKS.item)
            define('I', Items.HOPPER)
            define('P', MaterialCard.FAIRY_POT.item)
            define('F', Items.FURNACE)
        } on MaterialCard.FAIRY_POT.item
    }

    context(ModContext)
    override fun registerBlockStateGeneration() {
        block.registerVariantsBlockStateGeneration {
            normal("block/" * block().getIdentifier())
                .withHorizontalRotation(HorizontalDirectionalBlock.FACING)
                .with(AthanorBlock.HALF) { model, entry -> if (entry.value == DoubleBlockHalf.UPPER) model * "_top" else model }
                .with(AthanorBlock.LIT) { model, entry -> if (entry.value) model * "_lit" else model }
        }
    }

    context(ModContext)
    override fun registerBlockLootTableGeneration() {
        block.registerLootTableGeneration { provider, _ ->
            provider.createSinglePropConditionTable(block(), AthanorBlock.HALF, DoubleBlockHalf.LOWER)
        }
    }
}

class AthanorBlock(private val card: AthanorCard) : SimpleMachineBlock(card) {
    companion object {
        val CODEC: MapCodec<AthanorBlock> = simpleCodec { AthanorBlock(AthanorCard) }
        val LIT: BooleanProperty = BlockStateProperties.LIT
        val HALF: EnumProperty<DoubleBlockHalf> = BlockStateProperties.DOUBLE_BLOCK_HALF

        private val LOWER_SHAPES = Shapes.or(
            box(0.0, 0.0, 1.0, 16.0, 7.0, 15.0),
            box(1.0, 7.0, 2.0, 15.0, 13.0, 14.0),
            box(2.0, 13.0, 3.0, 14.0, 15.0, 13.0),
            box(3.0, 15.0, 4.0, 13.0, 16.0, 12.0),
            box(4.0, 0.0, 0.5, 12.0, 8.0, 1.5),
            box(4.0, 13.0, 2.5, 12.0, 16.0, 3.5),
        ).let {
            arrayOf(
                it,
                it.rotateY90(),
                it.rotateY90().rotateY90(),
                it.rotateY90().rotateY90().rotateY90(),
            )
        }
        private val UPPER_SHAPES = Shapes.or(
            box(5.0, 0.0, 5.0, 11.0, 12.0, 11.0),
            box(4.0, 12.0, 4.0, 12.0, 14.0, 12.0),
            box(5.0, 14.0, 5.0, 11.0, 15.0, 11.0),
            box(6.0, 15.0, 6.0, 10.0, 16.0, 10.0),
            box(4.0, 0.0, 2.5, 12.0, 3.0, 3.5),
            box(4.0, 3.0, 3.0, 12.0, 9.0, 4.0),
            box(4.0, 9.0, 3.5, 12.0, 14.0, 4.5),
        ).let {
            arrayOf(
                it,
                it.rotateY90(),
                it.rotateY90().rotateY90(),
                it.rotateY90().rotateY90().rotateY90(),
            )
        }
    }

    override fun codec() = CODEC


    // BlockState

    init {
        registerDefaultState(defaultBlockState().with(LIT, false).with(HALF, DoubleBlockHalf.LOWER))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(LIT)
        builder.add(HALF)
    }


    // Place

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        val pos = context.clickedPos
        val level = context.level
        if (pos.y + 1 >= level.maxBuildHeight) return null
        if (!level.getBlockState(pos.above()).canBeReplaced(context)) return null
        return super.getStateForPlacement(context)
    }

    override fun setPlacedBy(level: Level, pos: BlockPos, state: BlockState, placer: LivingEntity?, stack: ItemStack) {
        level.setBlock(pos.above(), state.with(HALF, DoubleBlockHalf.UPPER), 3)
    }


    // Remove

    @Suppress("OVERRIDE_DEPRECATION")
    override fun updateShape(state: BlockState, direction: Direction, neighborState: BlockState, level: LevelAccessor, pos: BlockPos, neighborPos: BlockPos): BlockState {
        val connectingDirection = when (state[HALF]) {
            DoubleBlockHalf.UPPER -> Direction.DOWN
            DoubleBlockHalf.LOWER -> Direction.UP
        }
        if (direction == connectingDirection) {
            return if (neighborState isIn this && neighborState[HALF] != state[HALF]) {
                neighborState.with(HALF, state[HALF])
            } else {
                Blocks.AIR.defaultBlockState()
            }
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos)
    }

    override fun playerWillDestroy(level: Level, pos: BlockPos, state: BlockState, player: Player): BlockState {
        if (!level.isClientSide && (player.isCreative || !player.hasCorrectToolForDrops(state))) {
            if (state[HALF] == DoubleBlockHalf.UPPER) run {
                val lowerPos = pos.below()
                val lowerBlockState = level.getBlockState(lowerPos)
                if (lowerBlockState isNotIn this) return@run
                if (lowerBlockState[HALF] != DoubleBlockHalf.LOWER) return@run
                level.setBlock(lowerPos, Blocks.AIR.defaultBlockState(), 1 or 2 or 32)
                level.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, lowerPos, getId(lowerBlockState))
            }
        }
        return super.playerWillDestroy(level, pos, state, player)
    }


    // Use

    @Suppress("OVERRIDE_DEPRECATION")
    override fun useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hitResult: BlockHitResult): InteractionResult {
        val actualPos = when (state[HALF]) {
            DoubleBlockHalf.UPPER -> pos.below()
            DoubleBlockHalf.LOWER -> pos
        }
        openMenu(level, actualPos, player)
        return InteractionResult.sidedSuccess(level.isClientSide)
    }


    // Shape

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return when (state[HALF]) {
            DoubleBlockHalf.UPPER -> UPPER_SHAPES[state[FACING].get2DDataValue()]
            DoubleBlockHalf.LOWER -> LOWER_SHAPES[state[FACING].get2DDataValue()]
        }
    }


    // Status

    @Suppress("OVERRIDE_DEPRECATION")
    override fun isPathfindable(state: BlockState, pathComputationType: PathComputationType) = false


    // Move

    override fun <T : BlockEntity> getTicker(world: Level, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T>? {
        return when (state[HALF]) {
            DoubleBlockHalf.UPPER -> null
            DoubleBlockHalf.LOWER -> super.getTicker(world, state, type)
        }
    }

}

class AthanorBlockEntity(private val card: AthanorCard, pos: BlockPos, state: BlockState) : SimpleMachineBlockEntity<AthanorBlockEntity>(card, pos, state) {
    override fun getThis() = this

    override fun createInventory(): BlockEntityStorage {
        return when (blockState[AthanorBlock.HALF]) {
            DoubleBlockHalf.UPPER -> ProxyBlockEntityStorage {
                val level = level ?: return@ProxyBlockEntityStorage null
                val blockEntity = level.getBlockEntity(worldPosition.below()) as? AthanorBlockEntity ?: return@ProxyBlockEntityStorage null
                blockEntity.inventorySlotAccessors.getOrNull(it)
            }

            DoubleBlockHalf.LOWER -> super.createInventory()
        }
    }

    override fun loadAdditional(nbt: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(nbt, registries)
        fuelMax = nbt.wrapper["FuelMax"].int.get() ?: 0
        fuel = nbt.wrapper["Fuel"].int.get() ?: 0
    }

    override fun saveAdditional(nbt: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(nbt, registries)
        nbt.wrapper["FuelMax"].int.set(fuelMax)
        nbt.wrapper["Fuel"].int.set(fuel)
    }

    override fun setChanged() {
        super.setChanged()
        shouldUpdateFuel = true
    }

    var shouldUpdateFuel = true
    var fuelMax = 0
    var fuel = 0

    fun checkFuelInsert(): (() -> Unit)? {
        if (!shouldUpdateFuel) return null
        shouldUpdateFuel = false

        val fuelItemStack = this[card.inventorySlotIndexTable[card.fuelSlot]!!]
        val fuelValue = FuelRegistry.get(fuelItemStack).takeIf { it != 0 }
        if (!(fuelValue != null && fuelItemStack.count >= 1)) return null

        return {
            fuelItemStack.shrink(1)
            fuelMax = fuelValue
            fuel = fuelMax
            setChanged()
        }
    }

    fun setLit(lit: Boolean) {
        val world = level ?: return
        if (blockState[AthanorBlock.LIT] != lit) {
            world.setBlock(worldPosition, blockState.with(AthanorBlock.LIT, lit), Block.UPDATE_ALL)
        }
        // TODO state
    }

    override fun checkRecipe(world: Level): (() -> Unit)? {
        if (!shouldUpdateRecipe) return null
        shouldUpdateRecipe = false

        val inventory = SimpleMachineRecipeInput(card.inputSlots.map { this[card.inventorySlotIndexTable[it]!!] })

        val recipeHolder = card.match(world, inventory) ?: return null
        val recipe = recipeHolder.value()
        val matchResult = recipe.match(inventory, world.random) ?: return null

        return {
            val remainder = matchResult.getRemainingItems()
            craftingInventory += matchResult.craft()
            recipe.outputs.forEach {
                waitingInventory += it.copy()
            }
            waitingInventory += remainder
            progressMax = recipe.duration
            setChanged()
        }
    }

    override fun onRecipeCheck(world: Level, pos: BlockPos, state: BlockState, listeners: MutableList<() -> Unit>): Boolean {
        if (!super.onRecipeCheck(world, pos, state, listeners)) return false
        if (fuel == 0) listeners += checkFuelInsert() ?: return false
        return true
    }

    override fun onCraftingTick(world: Level, pos: BlockPos, state: BlockState, listeners: MutableList<() -> Unit>): Boolean {
        if (!super.onCraftingTick(world, pos, state, listeners)) return false
        if (fuel == 0) listeners += checkFuelInsert() ?: return false
        return true
    }

    override fun onPostServerTick(world: Level, pos: BlockPos, state: BlockState) {
        super.onPostServerTick(world, pos, state)
        val oldFuel = fuel
        if (fuel > 0) fuel--
        setLit(oldFuel > 0)
    }
}

class AthanorScreenHandler(card: AthanorCard, arguments: Arguments) : SimpleMachineScreenHandler(card, arguments) {
    var fuel by Property(AthanorCard.FUEL_PROPERTY)
    var fuelMax by Property(AthanorCard.FUEL_MAX_PROPERTY)
}
