package miragefairy2024.mod.fairyquest

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.common.SlotType
import miragefairy2024.mod.common.guiFullScreenTranslation
import miragefairy2024.mod.recipeviewer.vanilla.ScreenEventSource
import miragefairy2024.mod.recipeviewer.vanilla.ViewProviderMenu
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.views.GhostRecipeItemView
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import miragefairy2024.mod.recipeviewer.views.ProgressView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.YFlatScrollbarView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YScrollContainerView
import miragefairy2024.mod.recipeviewer.views.YSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.connectScrollbar
import miragefairy2024.mod.recipeviewer.views.createView
import miragefairy2024.mod.recipeviewer.views.margin
import miragefairy2024.mod.recipeviewer.views.menuSlotView
import miragefairy2024.mod.recipeviewer.views.minContentSizeY
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.mod.recipeviewer.views.tooltip
import miragefairy2024.mod.recipeviewer.views.wrapClickable
import miragefairy2024.util.OutputSlot
import miragefairy2024.util.Registration
import miragefairy2024.util.get
import miragefairy2024.util.invoke
import miragefairy2024.util.isNotEmpty
import miragefairy2024.util.mergeTo
import miragefairy2024.util.observe
import miragefairy2024.util.quickMove
import miragefairy2024.util.register
import miragefairy2024.util.registerViewScreen
import miragefairy2024.util.set
import miragefairy2024.util.size
import miragefairy2024.util.text
import miragefairy2024.util.toItemStacks
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType as ExtendedMenuType // MIGRATION2612 ReplaceImport net.fabricmc.fabric.api.menu.v1.ExtendedMenuType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation as Identifier // MIGRATION2612 ReplaceImport net.minecraft.resources.Identifier
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
    ExtendedMenuType({ syncId, playerInventory, buf ->
        FairyQuestCardScreenHandler(syncId, playerInventory, fairyQuestRecipeRegistry.get(buf)!!, ContainerLevelAccess.NULL)
    }, Identifier.STREAM_CODEC)
}

context(_: ModContext)
fun initFairyQuestCardScreenHandler() {
    fairyQuestCardScreenHandlerType.register()
    fairyQuestCardScreenHandlerType.registerViewScreen()
}

class FairyQuestCardScreenHandler(syncId: Int, val playerInventory: Inventory, val recipe: FairyQuestRecipe, val context: ContainerLevelAccess) : AbstractContainerMenu(fairyQuestCardScreenHandlerType(), syncId), ViewProviderMenu<Unit> {
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

    override fun createScreenState() = Unit

    override fun createRootView(title: Component, playerInventoryTitle: Component, screenEventSource: ScreenEventSource, state: Unit) = FairyQuestCardScreenContent(this, title, playerInventoryTitle, screenEventSource).view

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
}

private class FairyQuestCardScreenContent(menu: FairyQuestCardScreenHandler, title: Component, playerInventoryTitle: Component, screenEventSource: ScreenEventSource) {

    val view = run {
        val playerInventoryWidth = 9
        val playerInventoryHeight = 3
        val tradeSlotCount = 4
        val hotbarSlotIndexOffset = playerInventoryWidth * playerInventoryHeight
        val inputSlotIndexOffset = hotbarSlotIndexOffset + playerInventoryWidth
        val outputSlotIndexOffset = inputSlotIndexOffset + tradeSlotCount

        val panelMargin = 7
        val sectionSpace = 3
        val playerInventoryNameSpace = 1
        val hotbarSpace = 4
        val inventoryNameMarginX = 1

        View {
            view += StackView().configure { // 外枠なのだ～🌱
                view += NinePatchTextureCard.PANEL.createView()
                view += YListView().configure { // 内枠なのだ～🌱

                    view += MultiLineTextView().configure {
                        view.text.value = title
                        view.alignmentX = Alignment.CENTER
                        view.color = ColorPair.DARK_GRAY
                        view.shadow = false
                    }.margin(inventoryNameMarginX, 0)

                    view += YSpaceView(sectionSpace)

                    // 依頼本文なのだ～🌱
                    view += StackView().configure {
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL

                        // 外枠装飾用パネルなのだ～🌱
                        view += NinePatchTextureCard.FAIRY_QUEST_CARD_MESSAGE.createView()

                        // スクロールコンテナなのだ～🌱
                        view += XListView().configure {
                            view.sizingX = Sizing.FILL
                            view.sizingY = Sizing.FILL
                            val scrollView = YScrollContainerView().configure {
                                position.weight = 1.0
                                view += StackView().configure {
                                    view.sizingX = Sizing.FILL

                                    // 罫線装飾用パネルなのだ～🌱
                                    view += NinePatchTextureCard.FAIRY_QUEST_CARD_LINE.createView()

                                    // メッセージテキストラベルなのだ～🌱
                                    view += MultiLineTextView().configure {
                                        view.text.value = menu.recipe.message
                                        view.color = ColorPair(0xFF6B472E.toInt(), 0xFF6B472E.toInt())
                                        view.shadow = false
                                    }.margin(0, 0, 0, 1)

                                }
                            }
                            view += scrollView
                            val scrollbarView = YFlatScrollbarView().configure {
                                view.keyboardFocusable = false
                            }
                            view += scrollbarView
                            connectScrollbar(scrollView.view, scrollbarView.view)
                        }.margin(11, 6, 11, 11)

                    }.minContentSizeY(11 * 7 + 5).wrapClickable {
                        view.hoverOverlayColor = 0x00000000
                        // クリックしたらメッセージを全画面で表示するのだ～🌱
                        view.onClick.register {
                            menu.recipe.openMessageScreen()
                            true
                        }
                    }.tooltip(text { guiFullScreenTranslation() })

                    view += YSpaceView(sectionSpace)

                    // 取引欄なのだ～🌱
                    view += XListView().configure {

                        repeat(tradeSlotCount) { i ->
                            view += StackView().configure {
                                val slot = menu.slots[inputSlotIndexOffset + i]
                                val input = menu.recipe.inputs.getOrNull(i)?.invoke()

                                view += menuSlotView(slot, SlotType.NORMAL)
                                view += GhostRecipeItemView(input?.toItemStacks().orEmpty()).configure {
                                    view.overlayArgb = 0x20FF0000
                                    screenEventSource.onBeforeRender.observe(screenEventSource.onDisposing) {
                                        val itemStack: ItemStack = slot.item
                                        view.showItemStack = itemStack.isEmpty
                                        view.showOverlay = input != null && (!input.ingredient.test(itemStack) || itemStack.count < input.count)
                                    }
                                }.margin(SlotType.NORMAL.margin.x, SlotType.NORMAL.margin.y)
                            }
                        }

                        view += ProgressView(
                            ViewTexture(MirageFairy2024.identifier("textures/gui/fairy_quest_progress_background.png"), IntPoint(16, 16)),
                            ViewTexture(MirageFairy2024.identifier("textures/gui/fairy_quest_progress_overlay.png"), IntPoint(16, 16)),
                        ).configure {
                            screenEventSource.onBeforeRender.observe(screenEventSource.onDisposing) {
                                view.progress = menu.progress / menu.recipe.duration.toDouble()
                            }
                        }.margin(1)

                        repeat(tradeSlotCount) { i ->
                            view += StackView().configure {
                                val slot = menu.slots[outputSlotIndexOffset + i]
                                val output = menu.recipe.outputs.getOrNull(i)?.invoke()

                                view += menuSlotView(slot, SlotType.NORMAL)
                                view += GhostRecipeItemView(listOfNotNull(output)).configure {
                                    view.overlayArgb = 0x2000FF00
                                    screenEventSource.onBeforeRender.observe(screenEventSource.onDisposing) {
                                        val itemStack: ItemStack = slot.item
                                        view.showItemStack = itemStack.isEmpty
                                        view.showOverlay = itemStack.isEmpty && output != null && output.isNotEmpty
                                    }
                                }.margin(SlotType.NORMAL.margin.x, SlotType.NORMAL.margin.y)
                            }
                        }

                    }

                    view += YSpaceView(sectionSpace)

                    view += MultiLineTextView().configure {
                        view.text.value = playerInventoryTitle
                        view.color = ColorPair.DARK_GRAY
                        view.shadow = false
                    }.margin(inventoryNameMarginX, 0)

                    view += YSpaceView(playerInventoryNameSpace)

                    // プレイヤーのインベントリなのだ～🌱
                    repeat(playerInventoryHeight) { r ->
                        view += XListView().configure {
                            repeat(playerInventoryWidth) { c ->
                                view += menuSlotView(menu.slots[playerInventoryWidth * r + c], SlotType.NORMAL)
                            }
                        }
                    }
                    view += YSpaceView(hotbarSpace)
                    view += XListView().configure {
                        repeat(playerInventoryWidth) { c ->
                            view += menuSlotView(menu.slots[hotbarSlotIndexOffset + c], SlotType.NORMAL)
                        }
                    }

                }.margin(panelMargin)
            }
        }
    }

}
