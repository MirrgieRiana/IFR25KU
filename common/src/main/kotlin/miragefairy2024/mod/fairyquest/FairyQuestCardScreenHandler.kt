package miragefairy2024.mod.fairyquest

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.clientProxy
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.common.SlotType
import miragefairy2024.mod.common.guiFullScreenTranslation
import miragefairy2024.mod.recipeviewer.vanilla.ViewProviderMenu
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.views.GhostItemView
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.ProgressView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YScrollView
import miragefairy2024.mod.recipeviewer.views.YSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.itemTooltip
import miragefairy2024.mod.recipeviewer.views.margin
import miragefairy2024.mod.recipeviewer.views.maxContentSizeX
import miragefairy2024.mod.recipeviewer.views.maxContentSizeY
import miragefairy2024.mod.recipeviewer.views.menuSlotView
import miragefairy2024.mod.recipeviewer.views.minContentSizeX
import miragefairy2024.mod.recipeviewer.views.minContentSizeY
import miragefairy2024.mod.recipeviewer.views.onClick
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.mod.recipeviewer.views.tooltip
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.OutputSlot
import miragefairy2024.util.Registration
import miragefairy2024.util.get
import miragefairy2024.util.invoke
import miragefairy2024.util.isNotEmpty
import miragefairy2024.util.mergeTo
import miragefairy2024.util.orEmpty
import miragefairy2024.util.quickMove
import miragefairy2024.util.register
import miragefairy2024.util.registerViewScreen
import miragefairy2024.util.set
import miragefairy2024.util.size
import miragefairy2024.util.text
import miragefairy2024.util.toItemStacks
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
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

context(ModContext)
fun initFairyQuestCardScreenHandler() {
    fairyQuestCardScreenHandlerType.register()
    fairyQuestCardScreenHandlerType.registerViewScreen()
}

class FairyQuestCardScreenHandler(syncId: Int, val playerInventory: Inventory, val recipe: FairyQuestRecipe, val context: ContainerLevelAccess) : AbstractContainerMenu(fairyQuestCardScreenHandlerType(), syncId), ViewProviderMenu {
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
            context.execute { level, blockPos ->
                level.playSound(null, blockPos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.5F, 0.8F + 0.4F * level.random.nextFloat())
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

    private val onViewUpdate = mutableListOf<() -> Unit>()

    override fun tickView() {
        onViewUpdate.forEach {
            it()
        }
    }

    override fun createView(title: Component, playerInventoryTitle: Component): View {
        onViewUpdate.clear()

        fun inventoryNameView(name: Component) = MultiLineTextView().also {
            it.text.value = name
            it.color = ColorPair.DARK_GRAY
            it.shadow = false
        }

        fun slotRowView(slotIndexFunction: (Int) -> Int) = View {
            view += XListView().configure {
                repeat(9) { c ->
                    view += menuSlotView(slots[slotIndexFunction(c)], SlotType.NORMAL)
                }
            }
        }

        fun tradeSlotView(index: Int, ghostItemView: GhostItemView) = View {
            view += StackView().configure {
                view += menuSlotView(slots[index], SlotType.NORMAL)
                view += ghostItemView.configure {}.itemTooltip {
                    if (ghostItemView.showItemStack) ghostItemView.itemStack else EMPTY_ITEM_STACK
                }.margin(1)
            }
        }

        val rootView = View {
            view += StackView().configure {
                view += NinePatchImageView(MirageFairy2024.identifier("textures/gui/panel.png"), 16, 16, 5, 5, 5, 5, 5, 5, false)
                view += YListView().configure {
                    view.sizingX = Sizing.FILL

                    view += inventoryNameView(recipe.title).configure {
                        view.alignmentX = Alignment.CENTER
                    }.margin(1, 0)
                    view += YSpaceView(3)
                    view += StackView().configure {
                        view.sizingX = Sizing.FILL

                        view += NinePatchImageView(NinePatchTextureCard.FAIRY_QUEST_CARD_MESSAGE.texture, 66, 66, 22, 22, 22, 22, 22, 22, true)
                        view += YScrollView().configure {
                            view += StackView().configure {
                                view.sizingX = Sizing.FILL

                                view += NinePatchImageView(MirageFairy2024.identifier("textures/gui/fairy_quest_card_line.png"), 11, 11, 0, 11, 0, 0, 11, 0, true)
                                view += MultiLineTextView().configure {
                                    view.text.value = recipe.message
                                    view.color = ColorPair(0xFF6B472E.toInt(), 0xFF6B472E.toInt())
                                    view.shadow = false
                                }.margin(0, 0, 0, 1)
                            }
                        }.margin(11, 6, 11, 11)
                    }.minContentSizeY(82).maxContentSizeY(82).onClick {
                        clientProxy!!.openViewScreen(FairyQuestMessageScreen(recipe.title, recipe.message, recipe.client, recipe.title))
                        true
                    }.tooltip(text { guiFullScreenTranslation() })
                    view += YSpaceView(3)
                    view += XListView().configure {
                        repeat(4) { i ->
                            val index = 36 + i
                            val input = recipe.inputs.getOrNull(i)
                            val itemStacks = if (input == null) listOf() else input().toItemStacks()
                            val ghostItemView = GhostItemView(itemStacks).also {
                                it.overlayArgb = 0x20FF0000
                                onViewUpdate += {
                                    val itemStack = slots[index].item
                                    it.showItemStack = itemStack.isEmpty
                                    it.showOverlay = when {
                                        input == null -> false
                                        !input().ingredient.test(itemStack) -> true
                                        itemStack.count < input().count -> true
                                        else -> false
                                    }
                                }
                            }
                            view += tradeSlotView(index, ghostItemView)
                        }
                        view += ProgressView(
                            ViewTexture(MirageFairy2024.identifier("textures/gui/fairy_quest_progress_background.png"), IntPoint(16, 16)),
                            ViewTexture(MirageFairy2024.identifier("textures/gui/fairy_quest_progress_overlay.png"), IntPoint(16, 16)),
                        ).configure {
                            val progressView = view
                            onViewUpdate += {
                                progressView.progress = progress / recipe.duration.toDouble()
                            }
                        }.margin(1)
                        repeat(4) { i ->
                            val index = 40 + i
                            val output = recipe.outputs.getOrNull(i)
                            val ghostItemView = GhostItemView(if (output == null) listOf() else listOf(output())).also {
                                it.overlayArgb = 0x2000FF00
                                onViewUpdate += {
                                    it.showItemStack = slots[index].item.isEmpty
                                    it.showOverlay = output?.invoke().orEmpty.isNotEmpty && slots[index].item.isEmpty
                                }
                            }
                            view += tradeSlotView(index, ghostItemView)
                        }
                    }
                    view += YSpaceView(3)
                    view += inventoryNameView(playerInventoryTitle).configure {}.margin(1, 0)
                    view += YSpaceView(1)
                    repeat(3) { r ->
                        view += slotRowView { c -> 9 * r + c }
                    }
                    view += YSpaceView(4)
                    view += slotRowView { c -> 27 + c }
                }.minContentSizeX(162).maxContentSizeX(162).margin(7)
            }
        }
        tickView()
        return rootView
    }
}
