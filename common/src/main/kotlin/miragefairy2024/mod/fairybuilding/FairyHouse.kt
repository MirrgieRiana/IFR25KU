package miragefairy2024.mod.fairybuilding

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.lib.MachineScreenHandler
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.fairy.FairyCard
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.mod.materials.Material
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.materials.Shape
import miragefairy2024.mod.materials.tagOf
import miragefairy2024.mod.poem
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.createItemStack
import miragefairy2024.util.isIn
import miragefairy2024.util.on
import miragefairy2024.util.register
import miragefairy2024.util.registerShapedRecipeGeneration
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

object FairyHouseCard : FairyFactoryCard<FairyHouseBlock, FairyHouseBlockEntity, FairyHouseScreenHandler>() {
    override fun createIdentifier() = MirageFairy2024.identifier("fairy_house")
    override fun createName() = EnJa("Fairy House", "妖精の家")
    override fun createPoemList() = PoemList(2).poem(EnJa("Home sweet home", "あたたかいおうち"))

    override fun createBlock() = FairyHouseBlock(this)

    override fun createBlockEntityAccessor() = BlockEntityAccessor(::FairyHouseBlockEntity)

    override fun createScreenHandler(arguments: MachineScreenHandler.Arguments) = FairyHouseScreenHandler(this, arguments)

    override val guiWidth = 176
    override val guiHeight = 178

    override fun createSlotConfigurations(): List<FairyBuildingSlotConfiguration> {
        return super.createSlotConfigurations() + listOf(
            FairyBuildingSlotConfiguration(8, 39, animation = ac(NONE, p(10.0, 9.5, 14.5, 0.0F, 180.0F, 200.0))), // 本棚の上
            FairyBuildingSlotConfiguration(61, 17, animation = ac(FAIRY, p(4.5, 2.2, 14.0, 90.0F, 270.0F, 200.0))) { it isIn FairyCard.item() }, // ベッド
            FairyBuildingSlotConfiguration(61, 41, animation = ac(FAIRY, p(4.5, 7.2, 14.0, 90.0F, 270.0F, 200.0))) { it isIn FairyCard.item() }, // ベッド
            FairyBuildingSlotConfiguration(61, 65, animation = ac(FAIRY, p(4.5, 12.2, 14.0, 90.0F, 270.0F, 200.0))) { it isIn FairyCard.item() }, // ベッド
            FairyBuildingSlotConfiguration(82, 48, animation = ac(NONE, p(1.5, 5.5, 2.0, 0.0F, 270.0F, 200.0))), // タンスの上
            FairyBuildingSlotConfiguration(104, 27, animation = ac(FAIRY, p(12.0, 0.1, 3.0, 0.0F, 280.0F, 200.0))) { it isIn FairyCard.item() }, // テーブル
            FairyBuildingSlotConfiguration(128, 17, animation = ac(NONE, p(11.5, 1.5, 7.5, 0.0F, 270.0F, 200.0))), // テーブルの上
            FairyBuildingSlotConfiguration(152, 17, animation = ac(FAIRY, p(13.0, 0.1, 12.0, 0.0F, 70.0F, 200.0))) { it isIn FairyCard.item() }, // テーブル
            FairyBuildingSlotConfiguration(152, 37, animation = ac(FAIRY, p(9.0, 0.1, 12.0, 0.0F, 110.0F, 200.0))) { it isIn FairyCard.item() }, // テーブル
            FairyBuildingSlotConfiguration(108, 60, animation = ac(FAIRY, buildList {
                this += p(2.0, 0.1, 10.0, 0.0F, 110.0F, 140.0)
                this += p(6.0, 0.1, 10.0, 0.0F, 60.0F, 140.0)
            })) { it isIn FairyCard.item() }, // 床（左下）
            FairyBuildingSlotConfiguration(
                128, 60,
                animation = ac(NONE, p(4.0, 0.1, 7.0, 0.0F, 90.0F, 200.0)),
                insertDirections = setOf(Direction.UP, Direction.WEST),
                extractDirections = setOf(Direction.DOWN, Direction.EAST),
            ), // 床の上
            FairyBuildingSlotConfiguration(148, 60, animation = ac(FAIRY, buildList {
                this += p(5.0, 0.1, 4.0, 0.0F, 290.0F, 180.0)
                this += p(7.0, 0.1, 6.0, 0.0F, 320.0F, 180.0)
            })) { it isIn FairyCard.item() }, // 床（右上）
        )
    }

    override val collectingFolia = 2_000
    override val maxFolia = 4_000

    override fun createAdvancement() = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { MaterialCard.FAIRY_CRYSTAL.advancement!!.await() },
        icon = { item().createItemStack() },
        name = EnJa("My New Home", "新しい我が家"),
        description = EnJa("Hollow out a log to create a home for fairies", "原木をくりぬいて妖精の住み家を作ろう"),
        criterion = AdvancementCard.hasItem { item() },
        type = AdvancementCardType.NORMAL,
    )

    context(ModContext)
    override fun init() {
        super.init()
        Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("fairy_house")) { FairyHouseBlock.CODEC }.register()
        registerShapedRecipeGeneration(item) {
            pattern("#U#")
            pattern("L*R")
            pattern("#D#")
            define('#', HaimeviskaBlockCard.LOG.item())
            define('U', Items.LANTERN)
            define('D', ItemTags.WOOL_CARPETS)
            define('L', ConventionalItemTags.GLASS_PANES)
            define('R', ItemTags.WOODEN_DOORS)
            define('*', tagOf(Shape.GEM, Material.FAIRY_CRYSTAL))
        } on MaterialCard.FAIRY_CRYSTAL.item
    }
}

class FairyHouseBlock(card: FairyHouseCard) : FairyFactoryBlock(card) {
    companion object {
        val CODEC: MapCodec<FairyHouseBlock> = simpleCodec { FairyHouseBlock(FairyHouseCard) }
    }

    override fun codec() = CODEC
}

class FairyHouseBlockEntity(card: FairyHouseCard, pos: BlockPos, state: BlockState) : FairyFactoryBlockEntity<FairyHouseBlockEntity>(card, pos, state) {
    override fun getThis() = this
    override fun serverTick(world: Level, pos: BlockPos, state: BlockState) {
        super.serverTick(world, pos, state)

        if (folia < 1_000) {
            setStatus(FairyFactoryBlock.Status.OFFLINE)
            return
        }

        folia -= 10

        setStatus(FairyFactoryBlock.Status.PROCESSING)
    }
}

class FairyHouseScreenHandler(card: FairyHouseCard, arguments: Arguments) : FairyFactoryScreenHandler(card, arguments)
