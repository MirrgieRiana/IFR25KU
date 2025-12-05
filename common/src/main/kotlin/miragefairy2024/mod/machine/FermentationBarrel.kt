package miragefairy2024.mod.machine

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.lib.MachineScreenHandler
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.mod.materials.Material
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.materials.Shape
import miragefairy2024.mod.materials.tagOf
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.createItemStack
import miragefairy2024.util.generator
import miragefairy2024.util.on
import miragefairy2024.util.register
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerShapedRecipeGeneration
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument
import net.minecraft.world.level.material.MapColor

object FermentationBarrelCard : SimpleMachineCard<FermentationBarrelBlock, FermentationBarrelBlockEntity, FermentationBarrelScreenHandler, FermentationBarrelRecipe>() {
    override fun createIdentifier() = MirageFairy2024.identifier("fermentation_barrel")
    override val name = EnJa("Fermentation Barrel", "醸造樽")
    override val poem = EnJa("The scent of Haimeviska feel nostalgic", "懐かしき故郷の香り。")
    override val tier = 2
    override fun createBlockSettings(): BlockBehaviour.Properties = BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.BASS).sound(SoundType.WOOD).strength(3.0F).mapColor(MapColor.TERRACOTTA_ORANGE)
    override fun createBlock() = FermentationBarrelBlock(this)
    override fun createBlockEntityAccessor() = BlockEntityAccessor(::FermentationBarrelBlockEntity)
    override fun createScreenHandler(arguments: MachineScreenHandler.Arguments) = FermentationBarrelScreenHandler(this, arguments)
    override val guiWidth = 176
    override val guiHeight = 152

    override val inputSlots = listOf(
        SlotConfiguration(42, 17, setOf(Direction.UP), setOf()),
        SlotConfiguration(31, 39, setOf(Direction.NORTH), setOf()),
        SlotConfiguration(53, 39, setOf(Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.DOWN), setOf()),
    )
    override val outputSlots = listOf(
        SlotConfiguration(111, 28, setOf(), setOf(Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.DOWN)),
        SlotConfiguration(129, 28, setOf(), setOf(Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.DOWN)),
    )
    override val slots = inputSlots + outputSlots

    override val recipeType = FermentationBarrelRecipeCard.type

    override fun createAdvancement() = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { MaterialCard.HAIMEVISKA_ROSIN.advancement!!.await() },
        icon = { item().createItemStack() },
        name = EnJa("The Scent of Haimeviska", "ハイメヴィスカの香り"),
        description = EnJa("Craft a Fermentation Barrel for alcoholic beverages and certain poisons", "アルコール飲料および一部の毒物のための醸造樽を作る"),
        criterion = AdvancementCard.hasItem(item),
        type = AdvancementCardType.NORMAL,
    )

    context(ModContext)
    override fun init() {
        super.init()

        Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("fermentation_barrel")) { FermentationBarrelBlock.CODEC }.register()

        BlockTags.MINEABLE_WITH_AXE.generator.registerChild(block)

        registerShapedRecipeGeneration(item) {
            pattern("ILI")
            pattern("LRL")
            pattern("ILI")
            define('L', HaimeviskaBlockCard.LOG.item())
            define('R', tagOf(Shape.GEM, Material.HAIMEVISKA_ROSIN))
            define('I', tagOf(Shape.NUGGET, Material.IRON))
        } on MaterialCard.HAIMEVISKA_ROSIN.item
    }
}

class FermentationBarrelBlock(card: FermentationBarrelCard) : SimpleMachineBlock(card) {
    companion object {
        val CODEC: MapCodec<FermentationBarrelBlock> = simpleCodec { FermentationBarrelBlock(FermentationBarrelCard) }
    }

    override fun codec() = CODEC
}

class FermentationBarrelBlockEntity(card: FermentationBarrelCard, pos: BlockPos, state: BlockState) : SimpleMachineBlockEntity<FermentationBarrelBlockEntity>(card, pos, state) {
    override fun getThis() = this
}

class FermentationBarrelScreenHandler(card: FermentationBarrelCard, arguments: Arguments) : SimpleMachineScreenHandler(card, arguments)
