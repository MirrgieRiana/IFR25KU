package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.XScrollbarView
import miragefairy2024.mod.recipeviewer.views.XSimpleScrollView
import miragefairy2024.mod.recipeviewer.views.YScrollbarView
import miragefairy2024.util.register
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ScrollConnectionTest {
    @Test
    fun `接続時の指定値を丸めず双方向に同期して解除できるのだ`() {
        val scroll = XSimpleScrollView()
        val bar = XScrollbarView()
        scroll.updateScrollBounds(300, 100)
        scroll.scrollOffsetX.value = 80.25
        bar.contentLength = 1000
        bar.viewportLength = 10
        bar.scrollOffsetX.value = 400.0
        val connection = connectScrollbar(scroll, bar)
        Assertions.assertNotSame(scroll.scrollOffsetX, bar.scrollOffsetX)
        Assertions.assertEquals(300, bar.contentLength)
        Assertions.assertEquals(100, bar.viewportLength)
        Assertions.assertEquals(80.25, bar.scrollOffsetX.value)
        bar.scrollOffsetX.value = 150.5
        Assertions.assertEquals(150.5, scroll.scrollOffsetX.value)
        scroll.scrollOffsetX.value = 999.75
        Assertions.assertEquals(999.75, bar.scrollOffsetX.value)
        scroll.updateScrollBounds(120, 100)
        Assertions.assertEquals(120, bar.contentLength)
        Assertions.assertEquals(999.75, bar.scrollOffsetX.value)
        connection.remove()
        connection.remove()
        scroll.scrollOffsetX.value = 0.0
        Assertions.assertEquals(999.75, bar.scrollOffsetX.value)
        bar.scrollOffsetX.value = 10.0
        Assertions.assertEquals(0.0, scroll.scrollOffsetX.value)
        Assertions.assertTrue(scroll.onScrollBoundsChanged.listeners.isEmpty())
        Assertions.assertTrue(scroll.scrollOffsetX.listeners.isEmpty())
        Assertions.assertTrue(bar.scrollOffsetX.listeners.isEmpty())
    }

    @Test
    fun `異なる軸を含む複数のバーへ範囲と指定値を同期するのだ`() {
        val scroll = XSimpleScrollView()
        val first = XScrollbarView()
        val second = YScrollbarView()
        val firstConnection = connectScrollbar(scroll, first)
        val secondConnection = connectScrollbar(scroll, scroll.scrollOffsetX, second, second.scrollOffsetY)
        try {
            scroll.updateScrollBounds(300, 100)
            first.scrollOffsetX.value = 160.0
            Assertions.assertEquals(160.0, second.scrollOffsetY.value)
            scroll.updateScrollBounds(50, 100)
            Assertions.assertEquals(160.0, first.scrollOffsetX.value)
            Assertions.assertEquals(50, second.contentLength)
            Assertions.assertEquals(100, second.viewportLength)
        } finally {
            firstConnection.remove()
            secondConnection.remove()
        }
    }

    @Test
    fun `バーの通知中に表示領域の範囲が変わっても最新の状態まで同期するのだ`() {
        val scroll = XSimpleScrollView()
        val bar = XScrollbarView()
        scroll.updateScrollBounds(300, 100)
        scroll.scrollOffsetX.value = 80.0
        val listener = bar.scrollOffsetX.register { _, _ ->
            if (scroll.contentLength == 300) {
                scroll.updateScrollBounds(600, 100)
                scroll.scrollOffsetX.value = 400.0
            }
        }
        val connection = connectScrollbar(scroll, bar)
        try {
            Assertions.assertEquals(600, bar.contentLength)
            Assertions.assertEquals(100, bar.viewportLength)
            Assertions.assertEquals(400.0, bar.scrollOffsetX.value)
            scroll.scrollOffsetX.value = 80.0
            scroll.updateScrollBounds(300, 100)
            Assertions.assertEquals(300, bar.contentLength)
            Assertions.assertEquals(80.0, bar.scrollOffsetX.value)
        } finally {
            connection.remove()
            listener.remove()
        }
        Assertions.assertTrue(scroll.onScrollBoundsChanged.listeners.isEmpty())
        Assertions.assertTrue(scroll.scrollOffsetX.listeners.isEmpty())
        Assertions.assertTrue(bar.scrollOffsetX.listeners.isEmpty())
    }

    @Test
    fun `バーの通知で変更された表示位置を再同期するのだ`() {
        val scroll = XSimpleScrollView()
        val bar = XScrollbarView()
        scroll.updateScrollBounds(300, 100)
        val connection = connectScrollbar(scroll, bar)
        val listener = bar.scrollOffsetX.register { _, new ->
            if (new == 15.0) scroll.scrollOffsetX.value = 20.0
        }
        try {
            scroll.scrollOffsetX.value = 15.0
            Assertions.assertEquals(20.0, scroll.scrollOffsetX.value)
            Assertions.assertEquals(20.0, bar.scrollOffsetX.value)
        } finally {
            connection.remove()
            listener.remove()
        }
    }

    @Test
    fun `初期同期の通知が失敗したら接続の購読を残さないのだ`() {
        val scroll = XSimpleScrollView()
        val bar = XScrollbarView()
        scroll.updateScrollBounds(300, 100)
        scroll.scrollOffsetX.value = 80.0
        val exception = IllegalStateException("Listener failed")
        val listener = bar.scrollOffsetX.register { _, _ -> throw exception }
        try {
            Assertions.assertSame(exception, Assertions.assertThrows(IllegalStateException::class.java) { connectScrollbar(scroll, bar) })
            Assertions.assertTrue(scroll.onScrollBoundsChanged.listeners.isEmpty())
            Assertions.assertTrue(scroll.scrollOffsetX.listeners.isEmpty())
            Assertions.assertEquals(1, bar.scrollOffsetX.listeners.size)
        } finally {
            listener.remove()
        }
    }
}
