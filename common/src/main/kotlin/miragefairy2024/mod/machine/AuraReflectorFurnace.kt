package miragefairy2024.mod.machine

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.lib.MachineScreenHandler
import miragefairy2024.mod.fairybuilding.FairyBuildingCard
import miragefairy2024.mod.materials.Material
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.materials.Shape
import miragefairy2024.mod.materials.tagOf
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.Model
import miragefairy2024.util.Registration
import miragefairy2024.util.TextureMap
import miragefairy2024.util.createItemStack
import miragefairy2024.util.generator
import miragefairy2024.util.get
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.int
import miragefairy2024.util.normal
import miragefairy2024.util.on
import miragefairy2024.util.register
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.registerVariantsBlockStateGeneration
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
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.material.MapColor

object AuraReflectorFurnaceCard : SimpleMachineCard<AuraReflectorFurnaceBlock, AuraReflectorFurnaceBlockEntity, AuraReflectorFurnaceScreenHandler, AuraReflectorFurnaceRecipe>() {
    override fun createIdentifier() = MirageFairy2024.identifier("aura_reflector_furnace")
    override fun createBlockSettings(): BlockBehaviour.Properties = BlockBehaviour.Properties.of().mapColor(MapColor.NETHER).requiresCorrectToolForDrops().strength(3.0F).lightLevel(Blocks.litBlockEmission(8))
    override fun createBlock() = AuraReflectorFurnaceBlock(this)
    override fun createBlockEntityAccessor() = BlockEntityAccessor(::AuraReflectorFurnaceBlockEntity)
    override fun createScreenHandler(arguments: MachineScreenHandler.Arguments) = AuraReflectorFurnaceScreenHandler(this, arguments)
    override val guiWidth = 176
    override val guiHeight = 166

    override val name = EnJa("Aura Reflector Furnace", "オーラ反射炉")
    override val poem = EnJa("Life is essentially inorganic.", "生命と無機物の境界。")
    override val tier = 2

    override val inputSlots = listOf(
        SlotConfiguration(29, 17, setOf(Direction.EAST), setOf()),
        SlotConfiguration(47, 17, setOf(Direction.UP, Direction.NORTH, Direction.DOWN), setOf()),
        SlotConfiguration(65, 17, setOf(Direction.WEST), setOf()),
    )
    val fuelSlot = object : SlotConfiguration(47, 53, setOf(Direction.SOUTH), setOf()) {
        override fun isValid(itemStack: ItemStack) = AuraReflectorFurnaceRecipe.getFuelValue(itemStack) != null
    }
    override val outputSlots = listOf(
        SlotConfiguration(123, 35, setOf(), setOf(Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.DOWN)),
    )
    override val slots = inputSlots + fuelSlot + outputSlots

    val FUEL_PROPERTY = FairyBuildingCard.PropertyConfiguration<AuraReflectorFurnaceBlockEntity>({ fuel }, { fuel = it })
    val FUEL_MAX_PROPERTY = FairyBuildingCard.PropertyConfiguration<AuraReflectorFurnaceBlockEntity>({ fuelMax }, { fuelMax = it })
    override val properties = super.properties + FUEL_PROPERTY + FUEL_MAX_PROPERTY

    override val recipeType = AuraReflectorFurnaceRecipeCard.type

    override fun createAdvancement() = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { MaterialCard.XARPITE.advancement!!.await() },
        icon = { item().createItemStack() },
        name = EnJa("Polymerization of the Ego", "自我の重合"),
        description = EnJa("Build an Aura Reflector Furnace and feed it with soul fuel", "オーラ反射炉を作って魂の燃料をくべよう"),
        criterion = AdvancementCard.hasItem(item),
        type = AdvancementCardType.NORMAL,
    )

    context(ModContext)
    override fun init() {
        super.init()

        Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("aura_reflector_furnace")) { AuraReflectorFurnaceBlock.CODEC }.register()

        registerModelGeneration({ "block/" * identifier * "_lit" }) { Model("block/" * identifier, TextureSlot.FRONT) with TextureMap(TextureSlot.FRONT to "block/" * identifier * "_front_lit") }

        BlockTags.MINEABLE_WITH_PICKAXE.generator.registerChild(block)
        BlockTags.NEEDS_STONE_TOOL.generator.registerChild(block)

        registerShapedRecipeGeneration(item) {
            pattern("XXX")
            pattern("XFX")
            pattern("XXX")
            define('F', Items.FURNACE)
            define('X', tagOf(Shape.GEM, Material.XARPITE))
        } on MaterialCard.XARPITE.item
    }

    context(ModContext)
    override fun registerBlockStateGeneration() {
        block.registerVariantsBlockStateGeneration {
            normal("block/" * block().getIdentifier())
                .withHorizontalRotation(HorizontalDirectionalBlock.FACING)
                .with(AuraReflectorFurnaceBlock.LIT) { model, entry -> if (entry.value) model * "_lit" else model }
        }
    }
}

class AuraReflectorFurnaceBlock(card: AuraReflectorFurnaceCard) : SimpleMachineBlock(card) {
    companion object {
        val CODEC: MapCodec<AuraReflectorFurnaceBlock> = simpleCodec { AuraReflectorFurnaceBlock(AuraReflectorFurnaceCard) }
        val LIT: BooleanProperty = BlockStateProperties.LIT
    }

    init {
        registerDefaultState(defaultBlockState().with(LIT, false))
    }

    override fun codec() = CODEC

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(LIT)
    }
}

class AuraReflectorFurnaceBlockEntity(private val card: AuraReflectorFurnaceCard, pos: BlockPos, state: BlockState) : SimpleMachineBlockEntity<AuraReflectorFurnaceBlockEntity>(card, pos, state) {
    override fun getThis() = this

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
        val fuelValue = AuraReflectorFurnaceRecipe.getFuelValue(fuelItemStack)
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
        if (blockState[AuraReflectorFurnaceBlock.LIT] != lit) {
            world.setBlock(worldPosition, blockState.with(AuraReflectorFurnaceBlock.LIT, lit), Block.UPDATE_ALL)
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

class AuraReflectorFurnaceScreenHandler(card: AuraReflectorFurnaceCard, arguments: Arguments) : SimpleMachineScreenHandler(card, arguments) {
    var fuel by Property(AuraReflectorFurnaceCard.FUEL_PROPERTY)
    var fuelMax by Property(AuraReflectorFurnaceCard.FUEL_MAX_PROPERTY)
}
