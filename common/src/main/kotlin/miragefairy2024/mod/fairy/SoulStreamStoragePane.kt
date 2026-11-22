package miragefairy2024.mod.fairy

import miragefairy2024.mod.common.SlotType
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.YInventoryScrollbarView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YScrollContainerView
import miragefairy2024.mod.recipeviewer.views.YSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.menuSlotView
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.register
import miragefairy2024.util.registerAndInitialize
import net.minecraft.network.chat.Component

internal class SoulStreamStoragePane(menu: SoulStreamScreenHandler, title: Component) {

    val scrollView = YScrollContainerView().also {
        it.scrollStep = SlotType.NORMAL.viewTexture.size.y
    }

    val view = View {
        val columnCount = 9
        val hotbarSlotIndex = columnCount * 3
        val passiveSkillSlotIndex = hotbarSlotIndex + columnCount
        val storageSlotIndex = passiveSkillSlotIndex + SoulStream.PASSIVE_SKILL_SLOT_COUNT

        fun createSlotRow(slotIndices: Iterable<Int>, slotType: SlotType): View {
            return View {
                view += XListView().configure {
                    slotIndices.forEach {
                        view += menuSlotView(menu.slots[it], slotType)
                    }
                }
            }
        }

        view += YListView().configure {
            view.sizingY = Sizing.FILL

            view += createSlotRow(passiveSkillSlotIndex until storageSlotIndex, SlotType.FAIRY)
            view += YSpaceView(4)
            view += XListView().configure {
                view.sizingY = Sizing.FILL
                position.weight = 1.0

                view += scrollView.configure {
                    view += YListView().configure {
                        (storageSlotIndex until menu.slots.size).chunked(columnCount).forEach {
                            view += createSlotRow(it, SlotType.NORMAL)
                        }
                    }
                }
                val scrollbarView = YInventoryScrollbarView().also {
                    it.scrollStep = scrollView.scrollStep
                }
                view += scrollbarView
                scrollView.scrollBounds.registerAndInitialize { _, bounds ->
                    scrollbarView.contentLength = bounds.contentLength
                    scrollbarView.viewportLength = bounds.viewportLength
                }
                scrollView.scrollOffsetY.registerAndInitialize { _, offset ->
                    scrollbarView.scrollOffset.value = offset
                }
                scrollbarView.scrollOffset.register { _, offset ->
                    scrollView.scrollOffsetY.value = offset
                }
            }
            view += YSpaceView(4)
            (0 until hotbarSlotIndex).chunked(columnCount).forEach {
                view += createSlotRow(it, SlotType.NORMAL)
            }
            view += YSpaceView(4)
            view += createSlotRow(hotbarSlotIndex until passiveSkillSlotIndex, SlotType.NORMAL)
            view += YSpaceView(5)
            view += TextView(title).configure {
                position.ignoreLayoutX = true
                view.sizingX = Sizing.FILL
                view.color = ColorPair.DARK_GRAY
                view.shadow = false
                view.alignmentX = Alignment.CENTER
            }
            view += YSpaceView(5)
        }
    }

}
