package miragefairy2024.client.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class UiContainersTest {

    @Test
    fun `verticalFlowは縦向きのコンポーネントを寸法つきで作るのだ～🌱`() {
        val layout = UiContainers.verticalFlow(Sizing.fixed(30), Sizing.fixed(40))
        layout.inflate(Size.of(500, 500))
        assertEquals(30, layout.width)
        assertEquals(40, layout.height)
        layout.child(UiSpacer().also { it.sizing(Sizing.fixed(10), Sizing.fixed(10)) })
        layout.child(UiSpacer().also { it.sizing(Sizing.fixed(10), Sizing.fixed(10)) })
        layout.inflate(Size.of(500, 500))
        // 縦向きなので、2 個目が下へ来るのだ～🌱
        assertEquals(Pair(0, 10), Pair(layout.children[1].x, layout.children[1].y))
    }

    @Test
    fun `horizontalFlowは横向きのコンポーネントを作るのだ～🌱`() {
        val layout = UiContainers.horizontalFlow(Sizing.content(), Sizing.content())
        layout.child(UiSpacer().also { it.sizing(Sizing.fixed(10), Sizing.fixed(10)) })
        layout.child(UiSpacer().also { it.sizing(Sizing.fixed(10), Sizing.fixed(10)) })
        layout.inflate(Size.of(500, 500))
        assertEquals(Pair(10, 0), Pair(layout.children[1].x, layout.children[1].y))
        assertEquals(20, layout.width)
    }

    @Test
    fun `stackは重ねるコンポーネントを作るのだ～🌱`() {
        val layout = UiContainers.stack(Sizing.content(), Sizing.content())
        layout.child(UiSpacer().also { it.sizing(Sizing.fixed(10), Sizing.fixed(10)) })
        layout.child(UiSpacer().also { it.sizing(Sizing.fixed(20), Sizing.fixed(20)) })
        layout.inflate(Size.of(500, 500))
        assertEquals(Pair(0, 0), Pair(layout.children[1].x, layout.children[1].y))
        assertEquals(20, layout.width)
    }
}
