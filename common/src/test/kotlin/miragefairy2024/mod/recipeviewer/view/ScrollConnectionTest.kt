package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.ScrollBounds
import miragefairy2024.mod.recipeviewer.views.XScrollbarView
import miragefairy2024.mod.recipeviewer.views.XSimpleScrollView
import miragefairy2024.mod.recipeviewer.views.YScrollbarView
import miragefairy2024.mod.recipeviewer.views.connectScrollbar
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.register
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ScrollConnectionTest {
    @Test
    fun `接続時の指定値を丸めず双方向に同期して解除できるのだ`() {
        val scroll = XSimpleScrollView()
        val bar = XScrollbarView()
        val scrollOffset = ObservableValue(80.25)
        val barOffset = ObservableValue(400.0)
        scroll.scrollBounds.value = ScrollBounds(300, 100)
        bar.contentLength = 1000
        bar.viewportLength = 10
        val connection = connectScrollbar(scroll, scrollOffset, bar, barOffset)
        Assertions.assertEquals(300, bar.contentLength)
        Assertions.assertEquals(100, bar.viewportLength)
        Assertions.assertEquals(80.25, barOffset.value)
        barOffset.value = 150.5
        Assertions.assertEquals(150.5, scrollOffset.value)
        scrollOffset.value = 999.75
        Assertions.assertEquals(999.75, barOffset.value)
        scroll.scrollBounds.value = ScrollBounds(120, 100)
        Assertions.assertEquals(120, bar.contentLength)
        Assertions.assertEquals(999.75, barOffset.value)
        scrollOffset.value = -25.5
        Assertions.assertEquals(-25.5, barOffset.value)
        connection.remove()
        connection.remove()
        scrollOffset.value = 0.0
        Assertions.assertEquals(-25.5, barOffset.value)
        barOffset.value = 10.0
        Assertions.assertEquals(0.0, scrollOffset.value)
        Assertions.assertTrue(scroll.scrollBounds.listeners.isEmpty())
        Assertions.assertTrue(scrollOffset.listeners.isEmpty())
        Assertions.assertTrue(barOffset.listeners.isEmpty())
    }

    @Test
    fun `異なる軸を含む複数のバーへ範囲と指定値を同期するのだ`() {
        val scroll = XSimpleScrollView()
        val scrollOffset = ObservableValue(0.0)
        val first = XScrollbarView()
        val firstOffset = ObservableValue(0.0)
        val second = YScrollbarView()
        val secondOffset = ObservableValue(0.0)
        val firstConnection = connectScrollbar(scroll, scrollOffset, first, firstOffset)
        val secondConnection = connectScrollbar(scroll, scrollOffset, second, secondOffset)
        try {
            scroll.scrollBounds.value = ScrollBounds(300, 100)
            firstOffset.value = 160.0
            Assertions.assertEquals(160.0, secondOffset.value)
            scroll.scrollBounds.value = ScrollBounds(50, 100)
            Assertions.assertEquals(160.0, firstOffset.value)
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
        val scrollOffset = ObservableValue(80.0)
        val barOffset = ObservableValue(0.0)
        scroll.scrollBounds.value = ScrollBounds(300, 100)
        val listener = barOffset.register { _, _ ->
            if (scroll.scrollBounds.value.contentLength == 300) {
                scroll.scrollBounds.value = ScrollBounds(600, 100)
                scrollOffset.value = 400.0
            }
        }
        val connection = connectScrollbar(scroll, scrollOffset, bar, barOffset)
        try {
            Assertions.assertEquals(600, bar.contentLength)
            Assertions.assertEquals(100, bar.viewportLength)
            Assertions.assertEquals(400.0, barOffset.value)
            scrollOffset.value = 80.0
            scroll.scrollBounds.value = ScrollBounds(300, 100)
            Assertions.assertEquals(300, bar.contentLength)
            Assertions.assertEquals(80.0, barOffset.value)
        } finally {
            connection.remove()
            listener.remove()
        }
        Assertions.assertTrue(scroll.scrollBounds.listeners.isEmpty())
        Assertions.assertTrue(scrollOffset.listeners.isEmpty())
        Assertions.assertTrue(barOffset.listeners.isEmpty())
    }

    @Test
    fun `バーの通知で変更された表示位置を再同期するのだ`() {
        val scroll = XSimpleScrollView()
        val bar = XScrollbarView()
        val scrollOffset = ObservableValue(0.0)
        val barOffset = ObservableValue(0.0)
        scroll.scrollBounds.value = ScrollBounds(300, 100)
        val connection = connectScrollbar(scroll, scrollOffset, bar, barOffset)
        val listener = barOffset.register { _, new ->
            if (new == 15.0) scrollOffset.value = 20.0
        }
        try {
            scrollOffset.value = 15.0
            Assertions.assertEquals(20.0, scrollOffset.value)
            Assertions.assertEquals(20.0, barOffset.value)
        } finally {
            connection.remove()
            listener.remove()
        }
    }

    @Test
    fun `初期同期の通知が失敗したら接続の購読を残さないのだ`() {
        val scroll = XSimpleScrollView()
        val bar = XScrollbarView()
        val scrollOffset = ObservableValue(80.0)
        val barOffset = ObservableValue(0.0)
        scroll.scrollBounds.value = ScrollBounds(300, 100)
        val exception = IllegalStateException("Listener failed")
        val listener = barOffset.register { _, _ ->
            throw exception
        }
        try {
            Assertions.assertSame(exception, Assertions.assertThrows(IllegalStateException::class.java) { connectScrollbar(scroll, scrollOffset, bar, barOffset) })
            Assertions.assertTrue(scroll.scrollBounds.listeners.isEmpty())
            Assertions.assertTrue(scrollOffset.listeners.isEmpty())
            Assertions.assertEquals(1, barOffset.listeners.size)
        } finally {
            listener.remove()
        }
    }
}
