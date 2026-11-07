package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.XScrollView
import miragefairy2024.mod.recipeviewer.views.XScrollbarView
import miragefairy2024.mod.recipeviewer.views.YScrollbarView
import miragefairy2024.util.register
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ScrollConnectionTest {
    @Test
    fun `接続時に表示領域の値を採用し双方向に同期して解除できるのだ`() {
        val scroll = XScrollView()
        val bar = XScrollbarView()
        scroll.scrollState.setBounds(300, 100)
        scroll.scrollState.value = 80.0
        bar.scrollState.setBounds(1000, 10)
        bar.scrollState.value = 400.0
        val connection = connectScrollbar(scroll, bar)
        Assertions.assertNotSame(scroll.scrollState, bar.scrollState)
        Assertions.assertEquals(300, bar.scrollState.contentLength)
        Assertions.assertEquals(100, bar.scrollState.viewportLength)
        Assertions.assertEquals(80.0, bar.scrollState.value)
        bar.scrollState.value = 150.0
        Assertions.assertEquals(150.0, scroll.scrollState.value)
        scroll.scrollState.value = 999.0
        Assertions.assertEquals(200.0, bar.scrollState.value)
        scroll.scrollState.setBounds(120, 100)
        Assertions.assertEquals(20.0, bar.scrollState.value)
        Assertions.assertEquals(20.0, bar.scrollState.maxValue)
        connection.remove()
        connection.remove()
        scroll.scrollState.value = 0.0
        Assertions.assertEquals(20.0, bar.scrollState.value)
        bar.scrollState.value = 10.0
        Assertions.assertEquals(0.0, scroll.scrollState.value)
        Assertions.assertTrue(scroll.scrollState.onChange.listeners.isEmpty())
        Assertions.assertTrue(bar.scrollState.onChange.listeners.isEmpty())
    }

    @Test
    fun `複数のバーへ現在値とスクロール不要になった範囲を同期するのだ`() {
        val scroll = XScrollView()
        val first = XScrollbarView()
        val second = YScrollbarView()
        val firstConnection = connectScrollbar(scroll, first)
        val secondConnection = connectScrollbar(scroll, second)
        try {
            scroll.scrollState.setBounds(300, 100)
            first.scrollState.value = 160.0
            Assertions.assertEquals(160.0, second.scrollState.value)
            scroll.scrollState.setBounds(50, 100)
            Assertions.assertEquals(0.0, first.scrollState.value)
            Assertions.assertEquals(0.0, second.scrollState.maxValue)
        } finally {
            firstConnection.remove()
            secondConnection.remove()
        }
    }

    @Test
    fun `範囲の縮小を値とまとめて通知し非有限値と負の寸法を拒否するのだ`() {
        val state = ScrollState()
        state.setBounds(300, 100)
        state.value = 200.0
        val values = mutableListOf<Double>()
        state.onChange.register {
            values += state.value
        }
        state.setBounds(120, 100)
        state.setBounds(120, 100)
        Assertions.assertEquals(listOf(20.0), values)
        listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { value ->
            Assertions.assertThrows(IllegalArgumentException::class.java) { state.value = value }
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) { state.setBounds(-1, 100) }
        Assertions.assertThrows(IllegalArgumentException::class.java) { state.setBounds(100, -1) }
        Assertions.assertEquals(20.0, state.value)
    }
}
