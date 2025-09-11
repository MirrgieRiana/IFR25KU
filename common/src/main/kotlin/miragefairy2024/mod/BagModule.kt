package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.magicplant.MagicPlantSeedItem
import miragefairy2024.mod.magicplant.contents.magicplants.MirageFlowerCard
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.EnJa
import miragefairy2024.util.FilteringSlot
import miragefairy2024.util.Registration
import miragefairy2024.util.Translation
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.generator
import miragefairy2024.util.get
import miragefairy2024.util.hasSameItemAndComponentsAndCount
import miragefairy2024.util.invoke
import miragefairy2024.util.isIn
import miragefairy2024.util.isNotEmpty
import miragefairy2024.util.itemStacks
import miragefairy2024.util.mergeTo
import miragefairy2024.util.on
import miragefairy2024.util.plus
import miragefairy2024.util.quickMove
import miragefairy2024.util.register
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerGeneratedModelGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.set
import miragefairy2024.util.text
import miragefairy2024.util.toItemTag
import mirrg.kotlin.helium.castOrNull
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.tags.BlockTags
import net.minecraft.world.Container
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.SlotAccess
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ClickAction
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.ItemContainerContents
import net.minecraft.world.level.Level
import kotlin.math.roundToInt

enum class BagCard(
    path: String,
    val itemName: EnJa,
    val tier: Int,
    val poem: EnJa,
    val inventoryWidth: Int,
    val inventoryHeight: Int,
    private val filter: (ItemStack) -> Boolean,
) {
    PLANT_BAG(
        "plant_bag", EnJa("Plant Bag", "植物カバン"),
        1, EnJa("Basket wall composed of uneven stems", "人間が手掛ける、初級レベルの藁細工。"),
        5, 3,
        { it.item.castOrNull<BlockItem>()?.let { blockItem -> BuiltInRegistries.BLOCK.isIn(blockItem.block, BlockTags.SWORD_EFFICIENT) } ?: false },
    ),
    SEED_BAG(
        "seed_bag", EnJa("Seed Bag", "種子カバン"),
        3, EnJa("Maintains the freshness of plants", "両手に、花。"),
        17, 6,
        { it.item is MagicPlantSeedItem },
    ),
    ;

    companion object {
        val screenHandlerType = Registration(BuiltInRegistries.MENU, MirageFairy2024.identifier("bag")) {
            ExtendedScreenHandlerType({ syncId, playerInventory, buf ->
                createBagScreenHandler(syncId, playerInventory, buf)
            }, ByteBufCodecs.INT)
        }

        val DESCRIPTION1_TRANSLATION = Translation({ MirageFairy2024.identifier("bag").toLanguageKey("item", "description1") }, "Display GUI when used", "使用時、GUIを表示")
        val DESCRIPTION2_TRANSLATION = Translation({ MirageFairy2024.identifier("bag").toLanguageKey("item", "description2") }, "Store to inventory when right-clicked", "インベントリ上で右クリックで収納")

        val BAG_ITEM_TAG = MirageFairy2024.identifier("bag").toItemTag()

        val bagAdvancement = AdvancementCard(
            identifier = MirageFairy2024.identifier("bag"),
            context = AdvancementCard.Sub { MirageFlowerCard.advancement!!.await() },
            icon = { PLANT_BAG.item().createItemStack() },
            name = EnJa("Explore the Overworld!!", "インベに余裕があるなら探検しよう！！"),
            description = EnJa("Craft a bag that can store a decent number of specific items", "特定のアイテムをそこそこ収納できるカバンを作成する"),
            criterion = AdvancementCard.hasItemTag { BAG_ITEM_TAG },
            type = AdvancementCardType.GOAL,
        )
    }

    init {
        /** @see ItemContainerContents.CODEC */
        check(inventoryWidth * inventoryHeight <= 256) { "see ItemContainerContents.CODEC" }
    }

    val identifier = MirageFairy2024.identifier(path)
    val item = Registration(BuiltInRegistries.ITEM, identifier) { BagItem(this, Item.Properties().stacksTo(1)) }
    val inventorySize = inventoryWidth * inventoryHeight
    fun isValid(itemStack: ItemStack) = filter(itemStack)
}


context(ModContext)
fun initBagModule() {
    BagCard.BAG_ITEM_TAG.enJa(EnJa("Bag", "カバン"))

    BagCard.entries.forEach { card ->
        card.item.register()
        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)
        card.item.registerGeneratedModelGeneration()
        card.item.enJa(card.itemName)
        val poemList = PoemList(card.tier)
            .poem(card.poem)
            .translation(PoemType.DESCRIPTION, BagCard.DESCRIPTION1_TRANSLATION)
            .translation(PoemType.DESCRIPTION, BagCard.DESCRIPTION2_TRANSLATION)
        card.item.registerPoem(poemList)
        card.item.registerPoemGeneration(poemList)
        BagCard.BAG_ITEM_TAG.generator.registerChild(card.item)
    }


    BagCard.screenHandlerType.register()

    BagCard.DESCRIPTION1_TRANSLATION.enJa()
    BagCard.DESCRIPTION2_TRANSLATION.enJa()

    BagCard.bagAdvancement.init()


    registerShapedRecipeGeneration(BagCard.PLANT_BAG.item) {
        pattern(" S ")
        pattern("L L")
        pattern("LLL")
        define('S', MaterialCard.MIRAGE_STEM.item())
        define('L', MaterialCard.MIRAGE_LEAVES.item())
    } on MaterialCard.MIRAGE_LEAVES.item

    registerShapedRecipeGeneration(BagCard.SEED_BAG.item) {
        pattern(" S ")
        pattern("L L")
        pattern("LLL")
        define('S', MaterialCard.MIRAGE_STEM.item())
        define('L', MaterialCard.PHANTOM_LEAVES.item())
    } on MaterialCard.PHANTOM_LEAVES.item
}


class BagItem(val card: BagCard, settings: Properties) : Item(settings) {

    override fun getName(stack: ItemStack): Component {
        val bagInventory = stack.getBagInventory() ?: return super.getName(stack)
        val count = bagInventory.itemStacks.count { it.isNotEmpty }
        return text { super.getName(stack) + (if (count > 0) " ($count / ${card.inventorySize})"() else ""()) }
    }

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltipComponents: MutableList<Component>, tooltipFlag: TooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)

        val inventory = stack.getBagInventory() ?: return
        var first = true
        var itemCount = 0
        inventory.itemStacks
            .filter { it.isNotEmpty }
            .groupBy { it.hoverName }
            .map { Pair(it.key, it.value.sumOf { itemStack -> itemStack.count }) }
            .forEach { (name, count) ->
                itemCount++
                if (itemCount <= 10) {
                    if (first) {
                        first = false
                        tooltipComponents += text { ""() }
                    }
                    tooltipComponents += text { name + (if (count > 1) " x ${count}"() else ""()) }
                }
            }
        if (itemCount > 10) tooltipComponents += text { "... ${itemCount - 10}"() }
    }

    override fun isBarVisible(stack: ItemStack): Boolean {
        val bagInventory = stack.getBagInventory() ?: return false
        val count = bagInventory.itemStacks.count { it.isNotEmpty }
        return count > 0
    }

    override fun getBarWidth(stack: ItemStack): Int {
        val bagInventory = stack.getBagInventory() ?: return 0
        val count = bagInventory.itemStacks.count { it.isNotEmpty }
        return (13.0 * count.toDouble() / card.inventorySize.toDouble()).roundToInt()
    }

    override fun getBarColor(stack: ItemStack): Int {
        val bagInventory = stack.getBagInventory() ?: return 0
        val count = bagInventory.itemStacks.count { it.isNotEmpty }
        return if (count >= card.inventorySize) 0xFF0000 else 0x00FF00
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val itemStack = user.getItemInHand(hand)
        if (world.isClientSide) return InteractionResultHolder.success(itemStack)
        val slotIndex = if (hand == InteractionHand.MAIN_HAND) {
            val selectedSlot = user.inventory.selected
            if (!Inventory.isHotbarSlot(selectedSlot)) return InteractionResultHolder.fail(itemStack)
            selectedSlot
        } else {
            -1
        }
        user.openMenu(object : ExtendedScreenHandlerFactory<Int> {
            override fun createMenu(syncId: Int, playerInventory: Inventory, player: Player): AbstractContainerMenu {
                return createBagScreenHandler(syncId, playerInventory, slotIndex)
            }

            override fun getDisplayName() = itemStack.hoverName

            override fun getScreenOpeningData(player: ServerPlayer) = slotIndex
        })
        return InteractionResultHolder.consume(itemStack)
    }

    // カバンを持って種子のスロットを右クリックした場合の処理
    // slot = 種子のスロット（操作後、マージが完了した場合は除去、そうでない場合は部分的除去が行われる）
    // カバンは常にカーソルが保持しているので、アイテムの入出力制限は無い
    override fun overrideStackedOnOther(stack: ItemStack, slot: Slot, clickType: ClickAction, player: Player): Boolean {
        if (clickType != ClickAction.SECONDARY) return false

        if (!slot.mayPickup(player)) return false // そもそもスロットからアイテムを回収できない場合はキャンセル

        // シミュレーション用のインベントリを作成
        val srcInventory = SimpleContainer(1)
        srcInventory[0] = slot.item.copy()
        val destInventory = stack.getBagInventory() ?: return false

        // マージをシミュレートする
        val result = srcInventory.mergeTo(destInventory)

        if (!result.completed && !slot.allowModification(player)) return false // マージが半端である場合、スロットが部分的な回収を受け付けない場合はキャンセル

        // 成功

        if (result.movementTimes > 0) {
            player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F)
        }

        // シミュレートした結果を適用する
        slot.set(srcInventory[0])
        stack.setBagInventory(destInventory)

        return true
    }

    // 種子を持ってカバンに突っ込んだ場合の処理
    // slot = カバンのスロット（操作後、変更が行われる）
    // 種子は常にカーソルが保持しているので、アイテムの入出力制限は無い
    override fun overrideOtherStackedOnMe(stack: ItemStack, otherStack: ItemStack, slot: Slot, clickType: ClickAction, player: Player, cursorStackReference: SlotAccess): Boolean {
        if (clickType != ClickAction.SECONDARY) return false

        if (!slot.allowModification(player)) return false // そもそもカバンのスロットが変更を受け付けない場合はキャンセル

        // シミュレーション用のインベントリを作成
        val srcInventory = SimpleContainer(1)
        srcInventory[0] = cursorStackReference.get().copy()
        val destInventory = stack.getBagInventory() ?: return false

        // マージをシミュレートする
        val result = srcInventory.mergeTo(destInventory)

        // 成功

        if (result.movementTimes > 0) {
            player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F)
        }

        // シミュレートした結果を適用する
        cursorStackReference.set(srcInventory[0])
        stack.setBagInventory(destInventory)

        return true
    }

    override fun canFitInsideContainerItems() = false

    override fun onDestroyed(entity: ItemEntity) {
        val world = entity.level()
        if (world.isClientSide) return
        val bagInventory = entity.item.getBagInventory() ?: return
        bagInventory.items.forEach { itemStack ->
            world.addFreshEntity(ItemEntity(world, entity.x, entity.y, entity.z, itemStack))
        }
    }

}

class BagInventory(private val card: BagCard) : SimpleContainer(card.inventorySize) {
    override fun canPlaceItem(slot: Int, stack: ItemStack) = card.isValid(stack) && stack.item.canFitInsideContainerItems()
}

fun ItemStack.getBagInventory(): BagInventory? {
    val item = this.item as? BagItem ?: return null
    val inventory = BagInventory(item.card)
    val itemContainerContents = this.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
    itemContainerContents.copyInto(inventory.items)
    return inventory
}

fun ItemStack.setBagInventory(inventory: BagInventory) {
    if (inventory.isEmpty) {
        this.set(DataComponents.CONTAINER, null)
    } else {
        val itemContainerContents = ItemContainerContents.fromItems(inventory.items)
        this.set(DataComponents.CONTAINER, itemContainerContents)
    }
}


fun createBagScreenHandler(syncId: Int, playerInventory: Inventory, slotIndex: Int): BagScreenHandler {
    val itemStackInstance = if (slotIndex == -1) playerInventory.offhand[0] else playerInventory.items[slotIndex]
    var expectedItemStack = itemStackInstance.copy()
    val item = itemStackInstance.item as? BagItem ?: return BagScreenHandler(syncId)
    val bagInventory = itemStackInstance.getBagInventory() ?: return BagScreenHandler(syncId)
    val inventoryDelegate = object : Container {
        override fun clearContent() = bagInventory.clearContent()
        override fun getContainerSize() = bagInventory.containerSize
        override fun isEmpty() = bagInventory.isEmpty()
        override fun getItem(slot: Int) = bagInventory.getItem(slot)
        override fun removeItem(slot: Int, amount: Int) = bagInventory.removeItem(slot, amount)
        override fun removeItemNoUpdate(slot: Int) = bagInventory.removeItemNoUpdate(slot)
        override fun setItem(slot: Int, stack: ItemStack) = bagInventory.setItem(slot, stack)
        override fun getMaxStackSize() = bagInventory.maxStackSize
        override fun setChanged() {
            bagInventory.setChanged()
            itemStackInstance.setBagInventory(bagInventory)
            expectedItemStack = itemStackInstance.copy()
        }

        override fun stillValid(player: Player) = bagInventory.stillValid(player)
        override fun startOpen(player: Player) = bagInventory.startOpen(player)
        override fun stopOpen(player: Player) = bagInventory.stopOpen(player)
        override fun canPlaceItem(slot: Int, stack: ItemStack) = bagInventory.canPlaceItem(slot, stack)
        override fun canTakeItem(hopperInventory: Container, slot: Int, stack: ItemStack) = bagInventory.canTakeItem(hopperInventory, slot, stack)
    }

    return object : BagScreenHandler(syncId) {
        init {
            repeat(3) { r ->
                repeat(9) { c ->
                    addSlot(Slot(playerInventory, 9 + 9 * r + c, 0, 0))
                }
            }
            repeat(9) { c ->
                addSlot(Slot(playerInventory, c, 0, 0))
            }
            repeat(item.card.inventorySize) { i ->
                addSlot(FilteringSlot(inventoryDelegate, i, 0, 0))
            }
        }

        override fun stillValid(player: Player): Boolean {
            val itemStack = if (slotIndex >= 0) playerInventory.items[slotIndex] else playerInventory.offhand[0]
            return itemStack === itemStackInstance && itemStack hasSameItemAndComponentsAndCount expectedItemStack
        }

        override fun quickMoveStack(player: Player, slot: Int): ItemStack {
            val playerIndices = 9 * 4 - 1 downTo 0
            val utilityIndices = 9 * 4 until slots.size
            val destinationIndices = if (slot in playerIndices) utilityIndices else playerIndices
            return quickMove(slot, destinationIndices)
        }

        override val card = item.card
    }
}

open class BagScreenHandler(syncId: Int) : AbstractContainerMenu(BagCard.screenHandlerType(), syncId) {
    override fun stillValid(player: Player) = false
    override fun quickMoveStack(player: Player, slot: Int) = EMPTY_ITEM_STACK
    open val card: BagCard? = null
}
