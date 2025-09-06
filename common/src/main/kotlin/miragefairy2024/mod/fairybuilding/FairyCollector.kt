package miragefairy2024.mod.fairybuilding

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.lib.MachineScreenHandler
import miragefairy2024.mod.fairy.FairyCard
import miragefairy2024.mod.fairy.MotifCard
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.collectItem
import miragefairy2024.util.createItemStack
import miragefairy2024.util.get
import miragefairy2024.util.int
import miragefairy2024.util.invoke
import miragefairy2024.util.isIn
import miragefairy2024.util.mergeInventory
import miragefairy2024.util.on
import miragefairy2024.util.register
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.set
import miragefairy2024.util.text
import miragefairy2024.util.toInventoryDelegate
import miragefairy2024.util.wrapper
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.structure.BoundingBox

object FairyCollectorCard : FairyFactoryCard<FairyCollectorBlock, FairyCollectorBlockEntity, FairyCollectorScreenHandler>() {
    override fun getPath() = "fairy_collector"
    override val tier = 2
    override val name = EnJa("Fairy Collector", "いたずら妖精エンデルマーニャの隠れ家")
    override val poem = EnJa("An attractor of curiosity", "あれ？ここにあったリモコン知らない？")

    override fun createBlock() = FairyCollectorBlock(this)

    override fun createBlockEntityAccessor() = BlockEntityAccessor(::FairyCollectorBlockEntity)

    override fun createScreenHandler(arguments: MachineScreenHandler.Arguments) = FairyCollectorScreenHandler(this, arguments)

    override val guiWidth = 176
    override val guiHeight = 162

    override fun createSlotConfigurations(): List<FairyBuildingSlotConfiguration> {
        val extractDirections = setOf(Direction.UP, Direction.DOWN, Direction.SOUTH, Direction.WEST, Direction.EAST)
        return super.createSlotConfigurations() + listOf(
            FairyBuildingSlotConfiguration(15, 35, tooltipGetter = { listOf(text { SPECIFIED_FAIRY_SLOT_TRANSLATION(MotifCard.CARRY.displayName) }) }) { isFairy(it, MotifCard.CARRY) }, // 回収妖精 // TODO 妖精パーティクル
            FairyBuildingSlotConfiguration(37 + 18 * 0, 17 + 18 * 0, animation = ac(NONE, p(11.5, 1.5, 2.5, 0.0F, 180.0F, 200.0))), // 机
            FairyBuildingSlotConfiguration(81, 35, animation = ac(FAIRY, buildList {
                this += p(11.5, 0.1, 6.0, 0.0F, 90.0F, 40.0)
                this += p(7.0, 0.1, 8.0, 0.0F, 275.0F, 40.0)
                this += p(12.0, 0.1, 8.0, 0.0F, 265.0F, 40.0)
                this += p(8.0, 0.1, 6.0, 0.0F, 20.0F, 40.0)
            })) { it isIn FairyCard.item() }, // 仕分け妖精
            FairyBuildingSlotConfiguration(106 + 18 * 0, 26 + 18 * 0, extractDirections = extractDirections, animation = ac(NONE, p(4.0, 2.0, 4.5, 0.0F, 270.0F, 200.0))), // 箱
            FairyBuildingSlotConfiguration(106 + 18 * 1, 26 + 18 * 0, extractDirections = extractDirections), // 箱
            FairyBuildingSlotConfiguration(106 + 18 * 2, 26 + 18 * 0, extractDirections = extractDirections), // 箱
            FairyBuildingSlotConfiguration(106 + 18 * 0, 26 + 18 * 1, extractDirections = extractDirections), // 箱
            FairyBuildingSlotConfiguration(106 + 18 * 1, 26 + 18 * 1, extractDirections = extractDirections), // 箱
            FairyBuildingSlotConfiguration(106 + 18 * 2, 26 + 18 * 1, extractDirections = extractDirections), // 箱
            FairyBuildingSlotConfiguration(37 + 18 * 1, 17 + 18 * 0), // 机
            FairyBuildingSlotConfiguration(37 + 18 * 0, 17 + 18 * 1), // 机
            FairyBuildingSlotConfiguration(37 + 18 * 1, 17 + 18 * 1), // 机
        )
    }

    val TABLE_SLOT_INDICES = listOf(1, 9, 10, 11)
    val CHEST_SLOT_INDICES = 3..8

    val COLLECTION_PROGRESS_PROPERTY = PropertyConfiguration<FairyCollectorBlockEntity>({ collectionProgress }, { collectionProgress = it })
    val SORT_PROGRESS_PROPERTY = PropertyConfiguration<FairyCollectorBlockEntity>({ sortProgress }, { sortProgress = it })
    val COLLECTION_SPEED_PROPERTY = PropertyConfiguration<FairyCollectorBlockEntity>({ collectionSpeed }, { collectionSpeed = it })
    val SORT_SPEED_PROPERTY = PropertyConfiguration<FairyCollectorBlockEntity>({ sortSpeed }, { sortSpeed = it })
    override fun createPropertyConfigurations() = super.createPropertyConfigurations() + listOf(
        COLLECTION_PROGRESS_PROPERTY,
        SORT_PROGRESS_PROPERTY,
        COLLECTION_SPEED_PROPERTY,
        SORT_SPEED_PROPERTY,
    )

    override val collectingFolia = 10_000
    override val maxFolia = 20_000

    override fun createAdvancement() = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { FairyHouseCard.advancement!!.await() },
        icon = { item().createItemStack() },
        name = EnJa("Cleaner Driven by Curiosity", "好奇心の収集家"),
        description = EnJa("Place four chests in the Fairy House", "妖精の家にチェストを置く"),
        criterion = AdvancementCard.hasItem { item() },
        type = AdvancementCardType.NORMAL,
    )

    context(ModContext)
    override fun init() {
        super.init()
        Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("fairy_collector")) { FairyCollectorBlock.CODEC }.register()
        registerShapedRecipeGeneration(item) {
            pattern(" C ")
            pattern("C#C")
            pattern(" C ")
            define('#', FairyHouseCard.item())
            define('C', Items.CHEST)
        } on FairyHouseCard.item
    }
}

class FairyCollectorBlock(card: FairyCollectorCard) : FairyFactoryBlock(card) {
    companion object {
        val CODEC: MapCodec<FairyCollectorBlock> = simpleCodec { FairyCollectorBlock(FairyCollectorCard) }
    }

    override fun codec() = CODEC
}

class FairyCollectorBlockEntity(card: FairyCollectorCard, pos: BlockPos, state: BlockState) : FairyFactoryBlockEntity<FairyCollectorBlockEntity>(card, pos, state) {

    override fun getThis() = this


    override fun loadAdditional(nbt: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(nbt, registries)
        collectionProgress = nbt.wrapper["CollectionProgress"].int.get() ?: 0
        sortProgress = nbt.wrapper["SortProgress"].int.get() ?: 0
        updateCache()
    }

    override fun saveAdditional(nbt: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(nbt, registries)
        nbt.wrapper["CollectionProgress"].int.set(collectionProgress)
        nbt.wrapper["SortProgress"].int.set(sortProgress)
    }


    var collectionProgress = 0
    var sortProgress = 0
    var collectionSpeed = 0
    var sortSpeed = 0


    override fun setChanged() {
        super.setChanged()
        updateCache()
    }

    private fun updateCache() {
        collectionSpeed = (10000.0 / (20.0 * 3.0) * (getFairyLevel(this[0]) / 10.0)).toInt()
        sortSpeed = (10000.0 / (20.0 * 2.0) * (getFairyLevel(this[2]) / 10.0)).toInt()
    }


    override fun serverTick(world: Level, pos: BlockPos, state: BlockState) {
        super.serverTick(world, pos, state)

        if (folia < 3_000) {
            setStatus(FairyFactoryBlock.Status.OFFLINE)
            return
        }

        setChanged()

        folia -= 10

        collectionProgress += collectionSpeed
        if (collectionProgress >= 10000) {
            collectionProgress = 0

            val indices = FairyCollectorCard.TABLE_SLOT_INDICES.filter { this[it].isEmpty }.toCollection(ArrayDeque())
            if (indices.isNotEmpty()) {

                folia -= 1000

                val region = BoundingBox(pos.x - 10, pos.y - 4, pos.z - 10, pos.x + 10, pos.y, pos.z + 10)
                collectItem(world, pos, region = region, ignoreOriginalWall = true) {

                    folia -= 500 + 30 * it.item.count

                    val index = indices.removeFirst()
                    this[index] = it.item.copy()
                    it.discard()
                    // TODO パーティクル

                    indices.isNotEmpty()
                }

            }
        }

        sortProgress += sortSpeed
        if (sortProgress >= 10000) {
            sortProgress = 0

            folia -= 200

            val result = mergeInventory(
                this.toInventoryDelegate(),
                this.toInventoryDelegate(),
                srcIndices = FairyCollectorCard.TABLE_SLOT_INDICES,
                destIndices = FairyCollectorCard.CHEST_SLOT_INDICES
            )
            folia -= 20 * result.movedItemCount

        }

        setStatus(FairyFactoryBlock.Status.PROCESSING)
    }

}

class FairyCollectorScreenHandler(card: FairyCollectorCard, arguments: Arguments) : FairyFactoryScreenHandler(card, arguments) {
    var collectionProgress by Property(FairyCollectorCard.COLLECTION_PROGRESS_PROPERTY)
    var sortProgress by Property(FairyCollectorCard.SORT_PROGRESS_PROPERTY)
    var collectionSpeed by Property(FairyCollectorCard.COLLECTION_SPEED_PROPERTY)
    var sortSpeed by Property(FairyCollectorCard.SORT_SPEED_PROPERTY)
}
