package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.MessageViewScreenDefinition
import miragefairy2024.client.mod.getKeyMapping
import miragefairy2024.mod.common.KeyMappingType
import miragefairy2024.mod.recipeviewer.vanilla.ScreenEventSource
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.views.DynamicView
import miragefairy2024.mod.recipeviewer.views.EmptyView
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.empty
import miragefairy2024.util.fire
import miragefairy2024.util.observe
import miragefairy2024.util.register
import miragefairy2024.util.text
import net.minecraft.client.GameNarrator
import net.minecraft.client.InputType
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics as GuiGraphicsExtractor // MIGRATION2612 ReplaceImport net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class MessageViewScreenTest {

    private fun createMinecraft(): Minecraft {
        val minecraft: Minecraft = Mockito.mock(Minecraft::class.java)
        val narrator: GameNarrator = Mockito.mock(GameNarrator::class.java)
        Mockito.`when`(minecraft.lastInputType).thenReturn(InputType.MOUSE)
        Mockito.`when`(minecraft.narrator).thenReturn(narrator)
        return minecraft
    }

    @Test
    fun `ルート View を再生成しても同じ画面では同じステートを渡すのだ`() {
        Mockito.mockStatic(Screen::class.java).use {
            val minecraft = createMinecraft()
            val createdStates = mutableListOf<Any>()
            val receivedStates = mutableListOf<Any>()
            val definition = MessageViewScreenDefinition(text { empty() }, {
                Any().also { createdStates += it }
            }) { _, state ->
                receivedStates += state
                EmptyView()
            }
            val screen = MessageViewScreen(null, definition)
            try {
                screen.init(minecraft, 200, 200)
                screen.resize(minecraft, 300, 240)

                Assertions.assertSame(createdStates.single(), receivedStates.single())
                Assertions.assertEquals(300, screen.width)
                Assertions.assertEquals(240, screen.height)

                screen.removed()
                screen.init(minecraft, 300, 240)

                Assertions.assertEquals(2, receivedStates.size)
                Assertions.assertSame(createdStates.single(), receivedStates.last())
            } finally {
                screen.removed()
            }

            val otherScreen = MessageViewScreen(null, definition)
            try {
                otherScreen.init(minecraft, 300, 240)

                Assertions.assertEquals(2, createdStates.size)
                Assertions.assertEquals(3, receivedStates.size)
                Assertions.assertSame(createdStates.last(), receivedStates.last())
                Assertions.assertNotSame(receivedStates.first(), receivedStates.last())
            } finally {
                otherScreen.removed()
            }
        }
    }

    @Test
    fun `子 View の差し替えとリサイズをまたいで画面のイベントを購読できるのだ`() {
        Mockito.mockStatic(Screen::class.java).use {
            val minecraft = createMinecraft()
            val externalEvent = EventRegistry<() -> Unit>()
            val screenEventSources = mutableListOf<ScreenEventSource>()
            val calls = mutableListOf<String>()
            val tab = DynamicView(EmptyView())
            val guiGraphics = Mockito.mock(GuiGraphicsExtractor::class.java)
            val definition = MessageViewScreenDefinition(text { empty() }, { Unit }) { screenEventSource, _ ->
                screenEventSources += screenEventSource
                externalEvent.observe(screenEventSource.onDisposing) {
                    calls += "updated"
                }
                screenEventSource.onOpened.observe(screenEventSource.onDisposing) {
                    calls += "opened"
                }
                screenEventSource.onDisposing.observe(screenEventSource.onDisposing) {
                    calls += "disposing"
                }
                screenEventSource.onBeforeRender.observe(screenEventSource.onDisposing) {
                    calls += "render"
                }
                screenEventSource.onTick.observe(screenEventSource.onDisposing) {
                    calls += "tick"
                }
                tab
            }
            val screen = MessageViewScreen(null, definition)
            try {
                screen.init(minecraft, 200, 200)
                externalEvent.fire()
                screen.render(guiGraphics, 0, 0, 0.0F)
                tab.content.value = EmptyView()
                screen.resize(minecraft, 300, 240)
                externalEvent.fire()
                screen.tick()
                screen.render(guiGraphics, 0, 0, 0.0F)
                screen.removed()
                externalEvent.fire()

                screen.init(minecraft, 300, 240)
                externalEvent.fire()
                screen.tick()
                screen.render(guiGraphics, 0, 0, 0.0F)
                screen.removed()
                externalEvent.fire()
            } finally {
                screen.removed()
            }

            Assertions.assertEquals(2, screenEventSources.size)
            Assertions.assertSame(screenEventSources[0], screenEventSources[1])
            Assertions.assertEquals(listOf("opened", "updated", "render", "updated", "tick", "render", "disposing", "opened", "updated", "tick", "render", "disposing"), calls)
        }
    }

    @Test
    fun `最初の生成や配置に失敗した画面を外しても開閉を通知しないのだ`() {
        Mockito.mockStatic(Screen::class.java).use {
            val minecraft = createMinecraft()
            val failure = IllegalStateException("View initialization failed")
            val failingView: View = object : View by EmptyView() {
                override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): ViewController {
                    throw failure
                }
            }
            val createViews = listOf<() -> View>({ throw failure }, { failingView })

            createViews.forEach { createView ->
                val calls = mutableListOf<String>()
                val definition = MessageViewScreenDefinition(text { empty() }, { Unit }) { screenEventSource, _ ->
                    screenEventSource.onOpened.register {
                        calls += "opened"
                    }
                    screenEventSource.onDisposing.register {
                        calls += "disposing"
                    }
                    createView()
                }
                val screen = MessageViewScreen(null, definition)
                try {
                    val thrown = Assertions.assertThrows(IllegalStateException::class.java) {
                        screen.init(minecraft, 200, 200)
                    }
                    Assertions.assertSame(failure, thrown)
                } finally {
                    screen.removed()
                }
                Assertions.assertTrue(calls.isEmpty())
            }
        }
    }

    @Test
    fun `外部クリック終了を無効にした画面は外部をクリックしても閉じないのだ`() {
        Mockito.mockStatic(Screen::class.java).use {
            val minecraft = createMinecraft()
            val parent: Screen = Mockito.mock(Screen::class.java)
            val definition = MessageViewScreenDefinition(text { empty() }, { Unit }) { _, _ -> EmptyView() }
            definition.closeOnOutsideClick = false
            val screen = MessageViewScreen(parent, definition)
            try {
                screen.init(minecraft, 200, 200)

                Assertions.assertFalse(screen.mouseClicked(10.0, 10.0, 0))
                Mockito.verify(minecraft, Mockito.never()).setScreen(parent)
                screen.onClose()
                Mockito.verify(minecraft).setScreen(parent)
            } finally {
                screen.removed()
            }
        }
    }

    @Test
    fun `追加終了キーがある場合だけ未消費のキーを照合して閉じるのだ`() {
        Mockito.mockStatic(Screen::class.java).use {
            Mockito.mockStatic(Class.forName("miragefairy2024.client.mod.CommonClientModuleKt")).use { keyMappings ->
                val minecraft = createMinecraft()
                val parent: Screen = Mockito.mock(Screen::class.java)
                val additionalCloseKey: KeyMapping = Mockito.mock(KeyMapping::class.java)
                Mockito.`when`(additionalCloseKey.matches(70, 10)).thenReturn(true)
                val definition = MessageViewScreenDefinition(text { empty() }, { Unit }) { _, _ -> EmptyView() }
                definition.closeOnInventoryKey = false
                keyMappings.`when`<KeyMapping> { getKeyMapping(KeyMappingType.OPEN_TOOLTIP_VIEWER) }.thenReturn(additionalCloseKey)
                val screen: MessageViewScreen<Unit> = Mockito.spy(MessageViewScreen(parent, definition))
                screen.init(minecraft, 200, 200)
                try {
                    Assertions.assertFalse(screen.keyPressed(70, 10, 2))

                    definition.additionalCloseKey = KeyMappingType.OPEN_TOOLTIP_VIEWER
                    val editBox: EditBox = object : EditBox(null, 80, 80, 40, 20, text { empty() }) {
                        override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int) = true
                    }
                    Mockito.doReturn(listOf(editBox)).`when`(screen).children()
                    screen.focused = editBox
                    Assertions.assertTrue(screen.keyPressed(70, 10, 2))
                    keyMappings.verifyNoInteractions()
                    Mockito.verifyNoInteractions(additionalCloseKey)
                    Mockito.verify(minecraft, Mockito.never()).setScreen(parent)

                    screen.focused = null
                    Assertions.assertFalse(screen.keyPressed(71, 11, 3))
                    Mockito.verify(additionalCloseKey).matches(71, 11)

                    definition.additionalCloseKey = null
                    Assertions.assertFalse(screen.keyPressed(70, 10, 2))
                    Mockito.verifyNoMoreInteractions(additionalCloseKey)
                    Mockito.verify(minecraft, Mockito.never()).setScreen(parent)

                    definition.additionalCloseKey = KeyMappingType.OPEN_TOOLTIP_VIEWER
                    Assertions.assertTrue(screen.keyPressed(70, 10, 2))
                    keyMappings.verify({ getKeyMapping(KeyMappingType.OPEN_TOOLTIP_VIEWER) }, Mockito.times(2))
                    Mockito.verify(additionalCloseKey).matches(70, 10)
                    Mockito.verify(minecraft).setScreen(parent)
                } finally {
                    screen.removed()
                }
            }
        }
    }

    @Test
    fun `子が消費したクリックでは閉じず未処理クリックは設定に従って親画面へ戻るのだ`() {
        Mockito.mockStatic(Screen::class.java).use {
            val minecraft = createMinecraft()
            val parent: Screen = Mockito.mock(Screen::class.java)
            val definition = MessageViewScreenDefinition(text { empty() }, { Unit }) { _, _ -> EmptyView() }
            val screen: MessageViewScreen<Unit> = Mockito.spy(MessageViewScreen(parent, definition))
            screen.init(minecraft, 200, 200)
            try {
                val widget: AbstractWidget = Mockito.mock(AbstractWidget::class.java)
                Mockito.`when`(widget.mouseClicked(10.0, 10.0, 0)).thenReturn(true)
                Mockito.doReturn(listOf(widget)).`when`(screen).children()

                Assertions.assertTrue(screen.mouseClicked(20.0, 20.0, 0))
                Mockito.verify(minecraft).setScreen(parent)

                Assertions.assertTrue(screen.mouseClicked(10.0, 10.0, 0))
                Mockito.verify(minecraft).setScreen(parent)

                definition.closeOnOutsideClick = false
                Assertions.assertFalse(screen.mouseClicked(20.0, 20.0, 0))
                Mockito.verify(minecraft).setScreen(parent)

                definition.closeOnOutsideClick = true
                Assertions.assertTrue(screen.mouseClicked(10.0, 10.0, 0))
                Mockito.verify(minecraft).setScreen(parent)
                Assertions.assertTrue(screen.mouseClicked(20.0, 20.0, 0))
                Mockito.verify(minecraft, Mockito.times(2)).setScreen(parent)
            } finally {
                screen.removed()
            }
        }
    }

}
