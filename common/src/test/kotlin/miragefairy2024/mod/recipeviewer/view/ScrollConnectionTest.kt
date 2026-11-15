package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.ScrollBounds
import miragefairy2024.mod.recipeviewer.views.XScrollbarView
import miragefairy2024.mod.recipeviewer.views.XSimpleScrollView
import miragefairy2024.mod.recipeviewer.views.YScrollbarView
import miragefairy2024.mod.recipeviewer.views.connectScrollbarBounds
import miragefairy2024.util.ObservableValue
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ScrollConnectionTest {
    @Test
    fun `領域と複数のバーで同じ指定位置を保持するのだ`() {
        val scrollOffset = ObservableValue(80.25)
        val scroll = XSimpleScrollView(scrollOffset)
        val first = XScrollbarView(scrollOffset)
        val second = YScrollbarView(scrollOffset)

        Assertions.assertSame(scrollOffset, scroll.scrollOffset)
        Assertions.assertSame(scrollOffset, first.scrollOffset)
        Assertions.assertSame(scrollOffset, second.scrollOffset)
        first.scrollOffset.value = -25.5
        Assertions.assertEquals(-25.5, scroll.scrollOffset.value)
        Assertions.assertEquals(-25.5, second.scrollOffset.value)
        Assertions.assertTrue(scrollOffset.toList().isEmpty())
    }

    @Test
    fun `領域の寸法を初期同期して解除まで変更を伝播するのだ`() {
        val scrollOffset = ObservableValue(80.25)
        val scroll = XSimpleScrollView(scrollOffset)
        val bar = XScrollbarView(scrollOffset)
        scroll.scrollBounds.value = ScrollBounds(300, 100)
        val connection = connectScrollbarBounds(scroll, bar)
        try {
            Assertions.assertEquals(300, bar.contentLength)
            Assertions.assertEquals(100, bar.viewportLength)

            scroll.scrollBounds.value = ScrollBounds(75, 50)
            Assertions.assertEquals(75, bar.contentLength)
            Assertions.assertEquals(50, bar.viewportLength)
            Assertions.assertEquals(80.25, scrollOffset.value)
        } finally {
            connection.remove()
        }

        scroll.scrollBounds.value = ScrollBounds(900, 100)
        Assertions.assertEquals(75, bar.contentLength)
        Assertions.assertEquals(50, bar.viewportLength)
        Assertions.assertTrue(scroll.scrollBounds.toList().isEmpty())
    }
}
