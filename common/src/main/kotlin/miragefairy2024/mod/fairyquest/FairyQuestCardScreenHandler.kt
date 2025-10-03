package miragefairy2024.mod.fairyquest

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.OutputSlot
import miragefairy2024.util.Registration
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.get
import miragefairy2024.util.mergeTo
import miragefairy2024.util.quickMove
import miragefairy2024.util.register
import miragefairy2024.util.set
import miragefairy2024.util.size
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

val fairyQuestCardScreenHandlerType = Registration(BuiltInRegistries.MENU, MirageFairy2024.identifier("fairy_quest_card")) {
    ExtendedScreenHandlerType({ syncId, playerInventory, buf ->
        FairyQuestCardScreenHandler(syncId, playerInventory, fairyQuestRecipeRegistry.get(buf)!!, ContainerLevelAccess.NULL)
    }, ResourceLocation.STREAM_CODEC)
}

val guiFairyQuestCardFullScreenTranslation = Translation({ "gui.${MirageFairy2024.identifier("fairy_quest_card").toLanguageKey()}.fullScreen" }, "Click to full screen", "クリックで全画面表示")

context(ModContext)
fun initFairyQuestCardScreenHandler() {
    fairyQuestCardScreenHandlerType.register()
    guiFairyQuestCardFullScreenTranslation.enJa()
}

class FairyQuestCardScreenHandler(syncId: Int, val playerInventory: Inventory, val recipe: FairyQuestRecipe, val context: ContainerLevelAccess) : AbstractContainerMenu(fairyQuestCardScreenHandlerType(), syncId) {
    private val inputInventory = SimpleContainer(4)
    private var processingInventory = SimpleContainer(0)
    private var resultInventory = SimpleContainer(0)
    private val outputInventory = SimpleContainer(4)
    var progress = 0

    private val propertyDelegate = object : ContainerData {
        override fun get(index: Int) = when (index) {
            0 -> progress
            else -> 0
        }

        override fun set(index: Int, value: Int) = when (index) {
            0 -> progress = value
            else -> Unit
        }

        override fun getCount() = 1
    }

    init {
        repeat(3) { r ->
            repeat(9) { c ->
                addSlot(Slot(playerInventory, 9 + 9 * r + c, 0, 0))
            }
        }
        repeat(9) { c ->
            addSlot(Slot(playerInventory, c, 0, 0))
        }
        repeat(4) { i ->
            addSlot(object : Slot(inputInventory, i, 0, 0) {
                override fun mayPlace(stack: ItemStack): Boolean {
                    val input = recipe.inputs.getOrNull(i) ?: return false
                    return input().ingredient.test(stack)
                }
            })
        }
        repeat(4) { i ->
            addSlot(OutputSlot(outputInventory, i, 0, 0))
        }
        addDataSlots(propertyDelegate)
    }

    override fun stillValid(player: Player) = true

    override fun quickMoveStack(player: Player, slot: Int): ItemStack {
        val playerIndices = 9 * 4 - 1 downTo 0
        val utilityIndices = 9 * 4 until 9 * 4 + 4 // TODO 出力スロットを含めると、出力スロットに既存アイテムがある場合にそこにスタックしてしまう
        val destinationIndices = if (slot in playerIndices) utilityIndices else playerIndices
        return quickMove(slot, destinationIndices)
    }

    override fun broadcastChanges() {

        // リザルトにアイテムが残っている場合、排出を試みる
        if (!resultInventory.isEmpty) {
            val result = resultInventory.mergeTo(outputInventory)
            if (!result.completed) return // リザルトにまだアイテムが残っているので次のクラフトを開始できない
        }
        // この時点でリザルトは空


        // クラフト進行処理
        if (progress == 0) { // クラフトがまだ始まっていない場合、始める

            val onCraftStart = mutableListOf<() -> Unit>()
            val processingItemStacks = mutableListOf<ItemStack>()

            // レシピ判定
            recipe.inputs.forEachIndexed { index, input ->
                val ingredientStack = input()
                if (!ingredientStack.ingredient.test(inputInventory[index])) return
                if (inputInventory[index].count < ingredientStack.count) return
                onCraftStart += {
                    processingItemStacks += inputInventory[index].split(ingredientStack.count)
                }
            }

            // 成立

            onCraftStart.forEach {
                it()
            }

            // 処理中アイテムの格納
            if (processingInventory.size < processingItemStacks.size) processingInventory = SimpleContainer(processingItemStacks.size)
            processingItemStacks.forEachIndexed { index, itemStack ->
                processingInventory[index] = itemStack
            }

            progress = 1

        } else { // クラフトが既に始まっている場合、プログレスを増加
            progress++
        }


        // クラフト完了処理
        if (progress >= recipe.duration) { // プログレスが満了している場合、完成処理

            // 材料の削除
            processingInventory.clearContent()

            // 成果物の生成
            if (resultInventory.size < recipe.outputs.size) resultInventory = SimpleContainer(recipe.outputs.size)
            recipe.outputs.forEachIndexed { index, itemStack ->
                resultInventory[index] = itemStack().copy()
            }

            // リザルト格納
            resultInventory.mergeTo(outputInventory)

            // エフェクト
            context.execute { world, blockPos ->
                world.playSound(null, blockPos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.5F, 0.8F + 0.4F * world.random.nextFloat())
            }

            // リセット
            progress = 0

        }

        super.broadcastChanges()

    }

    override fun removed(player: Player) {
        super.removed(player)
        context.execute { _, _ ->
            clearContainer(player, inputInventory)
            clearContainer(player, processingInventory)
            clearContainer(player, resultInventory)
            clearContainer(player, outputInventory)
        }
    }
}
