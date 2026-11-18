package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.ScrollBounds
import miragefairy2024.mod.recipeviewer.views.XScrollbarView
import miragefairy2024.mod.recipeviewer.views.XSimpleScrollView
import miragefairy2024.mod.recipeviewer.views.connectScrollbar
import miragefairy2024.util.register
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ScrollConnectionTest {
    @Test
    fun `両側で指定位置を共有し寸法を反映するのだ`() {
        val scroll = XSimpleScrollView()
        val bar = XScrollbarView()
        scroll.scrollBounds.value = ScrollBounds(100, 50)

        val connection = connectScrollbar(scroll, bar)
        try {
            val sharedOffset = checkNotNull(scroll.connectedScrollOffset.value)
            Assertions.assertSame(sharedOffset, bar.connectedScrollOffset.value)
            Assertions.assertEquals(0.0, sharedOffset.value)
            Assertions.assertEquals(100, bar.contentLength)
            Assertions.assertEquals(50, bar.viewportLength)

            checkNotNull(bar.connectedScrollOffset.value).value = 120.5
            scroll.scrollBounds.value = ScrollBounds(200, 100)
            Assertions.assertEquals(120.5, sharedOffset.value)
            Assertions.assertEquals(200, bar.contentLength)
            Assertions.assertEquals(100, bar.viewportLength)
        } finally {
            connection.remove()
        }

        connection.remove()
        scroll.scrollBounds.value = ScrollBounds(300, 200)
        Assertions.assertEquals(200, bar.contentLength)
        Assertions.assertEquals(100, bar.viewportLength)
        Assertions.assertNull(scroll.connectedScrollOffset.value)
        Assertions.assertNull(bar.connectedScrollOffset.value)
        Assertions.assertTrue(scroll.scrollBounds.toList().isEmpty())
    }

    @Test
    fun `接続の通知が失敗したら位置の参照と寸法の購読を解除するのだ`() {
        val scroll = XSimpleScrollView()
        val bar = XScrollbarView()
        val exception = IllegalStateException("Listener failed")
        val listener = bar.connectedScrollOffset.register { _, offset ->
            if (offset != null) throw exception
        }
        try {
            Assertions.assertSame(exception, Assertions.assertThrows(IllegalStateException::class.java) { connectScrollbar(scroll, bar) })
            Assertions.assertNull(scroll.connectedScrollOffset.value)
            Assertions.assertNull(bar.connectedScrollOffset.value)
            Assertions.assertTrue(scroll.scrollBounds.toList().isEmpty())
        } finally {
            listener.remove()
        }
    }
}
