package miragefairy2024.client.mod

import com.mojang.blaze3d.platform.InputConstants
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.core.VerticalAlignment
import io.wispforest.owo.util.Observable
import miragefairy2024.ModContext
import miragefairy2024.client.util.KeyMappingCard
import miragefairy2024.client.util.SlotType
import miragefairy2024.client.util.horizontalSpace
import miragefairy2024.client.util.tooltipContainer
import miragefairy2024.client.util.verticalScroll
import miragefairy2024.client.util.verticalSpace
import miragefairy2024.mod.OPEN_TOOLTIP_VIEWER_KEY_TRANSLATION
import miragefairy2024.mod.TOOLTIP_VIEWER_EXAMPLE_KEY_TRANSLATION
import miragefairy2024.mod.TOOLTIP_VIEWER_KEY_TRANSLATION
import miragefairy2024.util.get
import miragefairy2024.util.invoke
import miragefairy2024.util.isNotEmpty
import miragefairy2024.util.text
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft

val tooltipViewerKeyMappingCard = KeyMappingCard(
    OPEN_TOOLTIP_VIEWER_KEY_TRANSLATION.keyGetter(),
    InputConstants.UNKNOWN.value,
    KeyMapping.CATEGORY_INVENTORY,
) {
    Minecraft.getInstance().setScreen(TooltipViewerScreen())
}

context(ModContext)
fun initTooltipViewerClientModule() {
    tooltipViewerKeyMappingCard.init()
}

class TooltipViewerScreen() : BaseOwoScreen<FlowLayout>(text { TOOLTIP_VIEWER_KEY_TRANSLATION() }) {
    override fun createAdapter(): OwoUIAdapter<FlowLayout> = OwoUIAdapter.create(this, Containers::verticalFlow)

    private val filter = Observable.of("")

    override fun build(rootComponent: FlowLayout) {
        rootComponent.apply {
            surface(Surface.VANILLA_TRANSLUCENT)
            padding(Insets.of(4))
            verticalAlignment(VerticalAlignment.CENTER)
            horizontalAlignment(HorizontalAlignment.CENTER)

            child(Containers.verticalFlow(Sizing.content(), Sizing.fill()).apply { // 外枠
                surface(Surface.PANEL)
                padding(Insets.of(7))

                child(Containers.verticalFlow(Sizing.fixed(240), Sizing.fill()).apply { // 内枠

                    // 検索欄
                    child(Components.textBox(Sizing.fill()).apply {
                        setHint(text { TOOLTIP_VIEWER_EXAMPLE_KEY_TRANSLATION("0,1,2") })
                        text(filter.get())
                        onChanged().subscribe {
                            filter.set(it)
                        }
                    })

                    child(verticalSpace(2))

                    child(verticalScroll(Sizing.fill(), Sizing.expand(), 5).apply { // スクロール
                        child(Containers.verticalFlow(Sizing.fill(), Sizing.content()).apply { // スクロール内部
                            padding(Insets.of(0, 0, 0, 7))

                            val player = Minecraft.getInstance().player ?: return@apply

                            var firstItemStack = true
                            (0 until 9).forEach { i ->
                                val itemStack = player.inventory[i]

                                if (itemStack.isNotEmpty) {
                                    if (firstItemStack) {
                                        firstItemStack = false
                                    } else {
                                        child(verticalSpace(2))
                                    }
                                    child(Containers.horizontalFlow(Sizing.fill(), Sizing.content()).apply { // アイテムレコード

                                        // アイテムスロット
                                        child(Containers.stack(Sizing.fixed(18), Sizing.fixed(18)).apply {
                                            surface(Surface.tiled(SlotType.NORMAL.texture, 18, 18))
                                            padding(Insets.of(1))

                                            child(Components.item(itemStack).apply {
                                                showOverlay(true)
                                            })
                                        })

                                        child(horizontalSpace(2))

                                        // ツールチップ
                                        child(tooltipContainer(Sizing.expand(100), Sizing.content()).apply {
                                            fun f() {
                                                val allowIndices = filter.get().split(",").mapNotNull { it.toIntOrNull() }.toSet().ifEmpty { null }
                                                fun isAllowedIndex(index: Int) = allowIndices == null || index in allowIndices

                                                clearChildren()
                                                val tooltip = getTooltipFromItem(minecraft!!, itemStack)
                                                tooltip.forEachIndexed { index, text ->
                                                    if (!isAllowedIndex(index)) return@forEachIndexed

                                                    if (index >= 1 && isAllowedIndex(index - 1)) {
                                                        child(verticalSpace(if (index == 1) 3 else 1))
                                                    }
                                                    child(Components.label(text).shadow(true))
                                                }
                                            }
                                            filter.observe {
                                                f()
                                            }
                                            f()
                                        })

                                    })
                                }
                            }
                        })
                    })
                })
            })
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (minecraft!!.options.keyInventory.matches(keyCode, scanCode)) {
            onClose()
            return true
        }
        if (tooltipViewerKeyMappingCard.keyMapping.matches(keyCode, scanCode)) {
            onClose()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }
}
