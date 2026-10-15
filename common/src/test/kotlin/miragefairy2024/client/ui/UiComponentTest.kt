package miragefairy2024.client.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class UiComponentTest {

    /** 中身の寸法が決まっている、試験用のコンポーネントなのだ～🌱 */
    private class FixedContentComponent(private val contentWidth: Int, private val contentHeight: Int) : UiComponent() {
        override fun determineContentWidth() = contentWidth
        override fun determineContentHeight() = contentHeight
    }

    @Test
    fun `inflateは縦と横をそれぞれのSizingで決めるのだ～🌱`() {
        val component = FixedContentComponent(30, 40)
        component.sizing(Sizing.fixed(100), Sizing.content())
        component.inflate(Size.of(500, 500))
        assertEquals(100, component.width)
        assertEquals(40, component.height)
    }

    @Test
    fun `inflateはマージンを引いた空間でSizingを計算するのだ～🌱`() {
        val component = FixedContentComponent(0, 0)
        component.sizing(Sizing.fill(), Sizing.fill())
        component.margins(Insets.of(1, 2, 3, 4))
        component.inflate(Size.of(100, 100))
        // 横は左右の合計 7 を、縦は上下の合計 3 を引くのだ～🌱
        assertEquals(93, component.width)
        assertEquals(97, component.height)
    }

    @Test
    fun `fullWidthとfullHeightはマージンを含めた長さなのだ～🌱`() {
        val component = FixedContentComponent(0, 0)
        component.sizing(Sizing.fixed(50), Sizing.fixed(60))
        component.margins(Insets.of(1, 2, 3, 4))
        component.inflate(Size.of(100, 100))
        assertEquals(57, component.fullWidth)
        assertEquals(63, component.fullHeight)
    }

    @Test
    fun `contentのSizingは中身の寸法をそのまま使うのだ～🌱`() {
        val component = FixedContentComponent(30, 40)
        component.sizing(Sizing.content(), Sizing.content())
        component.inflate(Size.of(500, 500))
        assertEquals(30, component.width)
        assertEquals(40, component.height)
    }

    @Test
    fun `contentのSizingは余白を両端それぞれへ足すのだ～🌱`() {
        val component = FixedContentComponent(30, 40)
        component.sizing(Sizing.content(5), Sizing.content(5))
        component.inflate(Size.of(500, 500))
        assertEquals(40, component.width)
        assertEquals(50, component.height)
    }

    @Test
    fun `既定のSizingはcontentなのだ～🌱`() {
        val component = FixedContentComponent(30, 40)
        component.inflate(Size.of(500, 500))
        assertEquals(30, component.width)
        assertEquals(40, component.height)
    }

    @Test
    fun `中身の寸法を持たないコンポーネントは既定のSizingで0になるのだ～🌱`() {
        val component = object : UiComponent() {}
        component.inflate(Size.of(500, 500))
        assertEquals(0, component.width)
        assertEquals(0, component.height)
    }

    @Test
    fun `moveToは親の中身の始点から数えた位置を覚えるのだ～🌱`() {
        val component = FixedContentComponent(0, 0)
        assertEquals(0, component.x)
        assertEquals(0, component.y)
        component.moveTo(12, 34)
        assertEquals(12, component.x)
        assertEquals(34, component.y)
    }
}
