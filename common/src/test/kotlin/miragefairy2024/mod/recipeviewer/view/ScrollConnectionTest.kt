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
        scroll.scrollOffsetX.value = 15.25
        scroll.scrollBounds.value = ScrollBounds(100, 50)

        val connection = connectScrollbar(scroll, bar)
        try {
            Assertions.assertEquals(15.25, bar.scrollOffsetX.value)
            Assertions.assertEquals(100, bar.contentLength)
            Assertions.assertEquals(50, bar.viewportLength)

            scroll.scrollOffsetX.value = -30.25
            Assertions.assertEquals(-30.25, bar.scrollOffsetX.value)
            bar.scrollOffsetX.value = 120.5
            scroll.scrollBounds.value = ScrollBounds(200, 100)
            Assertions.assertEquals(120.5, scroll.scrollOffsetX.value)
            Assertions.assertEquals(200, bar.contentLength)
            Assertions.assertEquals(100, bar.viewportLength)
        } finally {
            connection.remove()
        }

        connection.remove()
        scroll.scrollBounds.value = ScrollBounds(300, 200)
        Assertions.assertEquals(120.5, scroll.scrollOffsetX.value)
        Assertions.assertEquals(120.5, bar.scrollOffsetX.value)
        Assertions.assertEquals(200, bar.contentLength)
        Assertions.assertEquals(100, bar.viewportLength)
        Assertions.assertTrue(scroll.scrollBounds.toList().isEmpty())
        Assertions.assertTrue(scroll.scrollOffsetX.toList().isEmpty())
        Assertions.assertTrue(bar.scrollOffsetX.toList().isEmpty())
        connectScrollbar(scroll, bar).remove()
        scroll.scrollOffsetX.value = 80.0
        Assertions.assertEquals(120.5, bar.scrollOffsetX.value)
    }

    @Test
    fun `一つの領域に二本のバーを接続して個別に解除するのだ`() {
        val scroll = XSimpleScrollView()
        val firstBar = XScrollbarView()
        val secondBar = XScrollbarView()
        scroll.scrollOffsetX.value = 10.25
        scroll.scrollBounds.value = ScrollBounds(300, 100)

        val firstConnection = connectScrollbar(scroll, firstBar)
        try {
            val secondConnection = connectScrollbar(scroll, secondBar)
            try {
                Assertions.assertEquals(10.25, firstBar.scrollOffsetX.value)
                Assertions.assertEquals(10.25, secondBar.scrollOffsetX.value)

                firstBar.scrollOffsetX.value = 60.5
                Assertions.assertEquals(60.5, scroll.scrollOffsetX.value)
                Assertions.assertEquals(60.5, secondBar.scrollOffsetX.value)

                secondBar.scrollOffsetX.value = 120.75
                Assertions.assertEquals(120.75, scroll.scrollOffsetX.value)
                Assertions.assertEquals(120.75, firstBar.scrollOffsetX.value)

                scroll.scrollBounds.value = ScrollBounds(400, 150)
                listOf(firstBar, secondBar).forEach { bar ->
                    Assertions.assertEquals(400, bar.contentLength)
                    Assertions.assertEquals(150, bar.viewportLength)
                }

                firstConnection.remove()
                firstBar.scrollOffsetX.value = -15.25
                Assertions.assertEquals(120.75, scroll.scrollOffsetX.value)
                Assertions.assertEquals(120.75, secondBar.scrollOffsetX.value)

                secondBar.scrollOffsetX.value = 50.25
                scroll.scrollBounds.value = ScrollBounds(500, 200)
                Assertions.assertEquals(50.25, scroll.scrollOffsetX.value)
                Assertions.assertEquals(-15.25, firstBar.scrollOffsetX.value)
                Assertions.assertEquals(400, firstBar.contentLength)
                Assertions.assertEquals(150, firstBar.viewportLength)
                Assertions.assertEquals(500, secondBar.contentLength)
                Assertions.assertEquals(200, secondBar.viewportLength)
            } finally {
                secondConnection.remove()
            }
        } finally {
            firstConnection.remove()
        }
    }

    @Test
    fun `一つのバーに二つの領域を接続して個別に解除するのだ`() {
        val bar = XScrollbarView()
        val firstScroll = XSimpleScrollView()
        val secondScroll = XSimpleScrollView()
        firstScroll.scrollOffsetX.value = 10.25
        secondScroll.scrollOffsetX.value = 20.5
        firstScroll.scrollBounds.value = ScrollBounds(300, 100)
        secondScroll.scrollBounds.value = ScrollBounds(400, 150)

        val firstConnection = connectScrollbar(firstScroll, bar)
        try {
            val secondConnection = connectScrollbar(secondScroll, bar)
            try {
                Assertions.assertEquals(20.5, bar.scrollOffsetX.value)
                Assertions.assertEquals(20.5, firstScroll.scrollOffsetX.value)
                Assertions.assertEquals(400, bar.contentLength)
                Assertions.assertEquals(150, bar.viewportLength)

                firstScroll.scrollOffsetX.value = 30.75
                Assertions.assertEquals(30.75, bar.scrollOffsetX.value)
                Assertions.assertEquals(30.75, secondScroll.scrollOffsetX.value)

                bar.scrollOffsetX.value = 60.0
                Assertions.assertEquals(60.0, firstScroll.scrollOffsetX.value)
                Assertions.assertEquals(60.0, secondScroll.scrollOffsetX.value)

                firstScroll.scrollBounds.value = ScrollBounds(500, 200)
                Assertions.assertEquals(500, bar.contentLength)
                Assertions.assertEquals(200, bar.viewportLength)

                firstConnection.remove()
                firstScroll.scrollOffsetX.value = -15.25
                firstScroll.scrollBounds.value = ScrollBounds(600, 250)
                Assertions.assertEquals(60.0, bar.scrollOffsetX.value)
                Assertions.assertEquals(500, bar.contentLength)

                secondScroll.scrollOffsetX.value = 55.25
                secondScroll.scrollBounds.value = ScrollBounds(450, 125)
                Assertions.assertEquals(55.25, bar.scrollOffsetX.value)
                Assertions.assertEquals(-15.25, firstScroll.scrollOffsetX.value)
                Assertions.assertEquals(450, bar.contentLength)
                Assertions.assertEquals(125, bar.viewportLength)
            } finally {
                secondConnection.remove()
            }
        } finally {
            firstConnection.remove()
        }
    }

    @Test
    fun `バーの通知中に更新された領域の位置を反映するのだ`() {
        val scroll = XSimpleScrollView()
        val bar = XScrollbarView()
        val listener = bar.scrollOffsetX.register { _, position ->
            if (position == 15.0) scroll.scrollOffsetX.value = 20.0
        }
        val connection = connectScrollbar(scroll, bar)
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
    fun `共有位置が元の値に戻った場合も操作元へ反映するのだ`() {
        val scroll = XSimpleScrollView()
        val bar = XScrollbarView()
        val listener = scroll.scrollOffsetX.register { _, position ->
            if (position == 15.0) scroll.scrollOffsetX.value = 0.0
        }
        val connection = connectScrollbar(scroll, bar)
        try {
            bar.scrollOffsetX.value = 15.0
            Assertions.assertEquals(0.0, scroll.scrollOffsetX.value)
            Assertions.assertEquals(0.0, bar.scrollOffsetX.value)
        } finally {
            connection.remove()
            listener.remove()
        }
    }

    @Test
    fun `初期同期の通知が失敗したら位置と寸法の購読を解除するのだ`() {
        val scroll = XSimpleScrollView()
        val bar = XScrollbarView()
        scroll.scrollOffsetX.value = 80.0
        bar.scrollOffsetX.value = 20.0
        val exception = IllegalStateException("Listener failed")
        val listener = bar.scrollOffsetX.register { _, offset ->
            if (offset == 80.0) throw exception
        }
        try {
            Assertions.assertSame(exception, Assertions.assertThrows(IllegalStateException::class.java) { connectScrollbar(scroll, bar) })
            Assertions.assertTrue(scroll.scrollBounds.toList().isEmpty())
            Assertions.assertTrue(scroll.scrollOffsetX.toList().isEmpty())
            Assertions.assertEquals(1, bar.scrollOffsetX.toList().size)
            Assertions.assertEquals(80.0, scroll.scrollOffsetX.value)
            Assertions.assertEquals(20.0, bar.scrollOffsetX.value)
        } finally {
            listener.remove()
        }
    }
}
