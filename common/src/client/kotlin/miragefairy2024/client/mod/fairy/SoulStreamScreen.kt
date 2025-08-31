package miragefairy2024.client.mod.fairy

import com.mojang.blaze3d.platform.InputConstants
import io.wispforest.owo.ui.base.BaseOwoHandledScreen
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.core.VerticalAlignment
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.client.util.KeyMappingCard
import miragefairy2024.client.util.SlotType
import miragefairy2024.client.util.inventoryNameLabel
import miragefairy2024.client.util.registerHandledScreen
import miragefairy2024.client.util.sendToServer
import miragefairy2024.client.util.slotContainer
import miragefairy2024.client.util.verticalScroll
import miragefairy2024.client.util.verticalSpace
import miragefairy2024.mod.fairy.OPEN_SOUL_STREAM_KEY_TRANSLATION
import miragefairy2024.mod.fairy.OpenSoulStreamChannel
import miragefairy2024.mod.fairy.SoulStreamScreenHandler
import miragefairy2024.mod.fairy.soulStreamScreenHandlerType
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.fire
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.size
import miragefairy2024.util.text
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ImageButton
import net.minecraft.client.gui.components.WidgetSprites
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import org.lwjgl.glfw.GLFW

val soulStreamKeyMappingCard = KeyMappingCard(
    OPEN_SOUL_STREAM_KEY_TRANSLATION.keyGetter(),
    GLFW.GLFW_KEY_K,
    KeyMapping.CATEGORY_INVENTORY,
) {
    lastMousePositionInInventory = null
    OpenSoulStreamChannel.sendToServer(Unit)
}

var lastMousePositionInInventory: Pair<Double, Double>? = null

context(ModContext)
fun initSoulStreamClientModule() {

    // GUI登録
    soulStreamScreenHandlerType.registerHandledScreen { gui, inventory, title -> SoulStreamScreen(gui, inventory, title) }

    // ソウルストリームのキーバインド
    soulStreamKeyMappingCard.init()

    // インベントリ画面にソウルストリームのボタンを設置
    ScreenEvents.AFTER_INIT.register { _, screen, _, _ ->
        if (screen is InventoryScreen) {

            val onMouseClick = mutableListOf<() -> Unit>()

            // 中央揃えコンテナ
            val uiAdapter = OwoUIAdapter.create(screen, Containers::stack)
            uiAdapter.rootComponent.apply {
                alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)

                // 位置決定用パネル
                child(Containers.stack(Sizing.content(), Sizing.content()).apply {
                    alignment(HorizontalAlignment.RIGHT, VerticalAlignment.TOP)

                    fun updatePosition() {
                        if (!screen.recipeBookComponent.isVisible) {
                            sizing(Sizing.fixed(146), Sizing.fixed(46))
                        } else {
                            sizing(Sizing.fixed(300), Sizing.fixed(46))
                        }
                    }
                    updatePosition()
                    onMouseClick += {
                        updatePosition()
                        uiAdapter.inflateAndMount()
                    }

                    // ボタン
                    val buttonWidgetSprites = WidgetSprites(MirageFairy2024.identifier("soul_stream_button"), MirageFairy2024.identifier("soul_stream_button_highlighted"))
                    child(ImageButton(0, 0, 20, 20, buttonWidgetSprites) {
                        lastMousePositionInInventory = Pair(Minecraft.getInstance().mouseHandler.xpos(), Minecraft.getInstance().mouseHandler.ypos())
                        screen.onClose()
                        OpenSoulStreamChannel.sendToServer(Unit)
                    }.apply {
                        tooltip(text { OPEN_SOUL_STREAM_KEY_TRANSLATION() + "("() + Component.keybind(OPEN_SOUL_STREAM_KEY_TRANSLATION.keyGetter()) + ")"() })
                    })

                })

            }
            uiAdapter.inflateAndMount()

            ScreenMouseEvents.afterMouseClick(screen).register { _, _, _, _ ->
                onMouseClick.forEach {
                    it()
                }
            }

        }
    }

}

class SoulStreamScreen(handler: SoulStreamScreenHandler, playerInventory: Inventory, title: Component) : BaseOwoHandledScreen<FlowLayout, SoulStreamScreenHandler>(handler, playerInventory, title) {

    // カーソルをインベントリ画面での位置に戻す
    private var isFirst = true
    override fun render(vanillaContext: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (isFirst) {
            isFirst = false
            if (lastMousePositionInInventory != null) {
                InputConstants.grabOrReleaseMouse(this.minecraft!!.window.window, InputConstants.CURSOR_NORMAL, lastMousePositionInInventory!!.first, lastMousePositionInInventory!!.second)
            }
        }
        super.render(vanillaContext, mouseX, mouseY, delta)
    }

    override fun createAdapter(): OwoUIAdapter<FlowLayout> = OwoUIAdapter.create(this, Containers::horizontalFlow)
    override fun build(rootComponent: FlowLayout) {
        rootComponent.apply {
            surface(Surface.flat(0xFFC6C6C6.toInt()))
            allowOverflow(true) // expandの端数処理の不具合で1pxだけはみ出すことがある
            padding(Insets.of(4))
            gap(4)

            // 左ペイン
            child(Containers.verticalFlow(Sizing.expand(50), Sizing.fill()).apply {

                // TODO

            })

            // メインコンテナ
            child(Containers.verticalFlow(Sizing.fixed(18 * 9 + 18), Sizing.fill()).apply {

                child(inventoryNameLabel(title))

                child(verticalSpace(3))

                child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                    surface(Surface.tiled(SlotType.FAIRY.texture, 18, 18))
                    repeat(9) { index ->
                        child(slotContainer(slotAsComponent(9 * 3 + 9 + index), type = null))
                    }
                })

                child(verticalSpace(4))

                child(verticalScroll(Sizing.fill(100), Sizing.expand(), 18).apply {
                    surface(Surface.tiled(SlotType.NORMAL.texture, 18, 18))
                    scrollbar(ScrollContainer.Scrollbar.vanilla())
                    scrollStep(18)
                    (9 until menu.soulStream.size).chunked(9).forEach { indices ->
                        child().child(Containers.horizontalFlow(Sizing.fill(100), Sizing.content()).apply {
                            indices.forEach { index ->
                                child(slotContainer(slotAsComponent(9 * 3 + 9 + index), type = null))
                            }
                        })
                    }
                })

                child(verticalSpace(3))

                child(inventoryNameLabel(menu.playerInventory.name))

                child(verticalSpace(1))

                // プレイヤーインベントリ
                child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
                    surface(Surface.tiled(SlotType.NORMAL.texture, 18, 18))
                    repeat(3) { r ->
                        child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                            repeat(9) { c ->
                                child(slotContainer(slotAsComponent(9 * r + c), type = null))
                            }
                        })
                    }
                })
                child(verticalSpace(4))
                child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                    surface(Surface.tiled(SlotType.NORMAL.texture, 18, 18))
                    repeat(9) { c ->
                        child(slotContainer(slotAsComponent(9 * 3 + c), type = null))
                    }
                })

            })

            // 右ペイン
            child(Containers.verticalFlow(Sizing.expand(50), Sizing.fill()).apply {

                // TODO

            })

        }
    }

    override fun renderLabels(context: GuiGraphics, mouseX: Int, mouseY: Int) = Unit

    private val onUpdateListeners = mutableListOf<() -> Unit>()
    private var lastUpdateEventNanoTime = 0L
    override fun containerTick() {
        super.containerTick()

        val now = System.nanoTime()
        if (now - lastUpdateEventNanoTime >= 100_000_000) {
            lastUpdateEventNanoTime = now
            onUpdateListeners.forEach {
                it()
            }
        }
    }

    // キー入力で閉じる
    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (soulStreamKeyMappingCard.keyMapping.matches(keyCode, scanCode)) {
            onClose()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    val onClose = EventRegistry<() -> Unit>()
    override fun onClose() {
        super.onClose()
        onClose.fire()
    }

}
