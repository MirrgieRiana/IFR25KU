package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.ScrollBounds
import miragefairy2024.mod.recipeviewer.views.XFlatScrollbarView
import miragefairy2024.mod.recipeviewer.views.XScrollContainerView
import miragefairy2024.mod.recipeviewer.views.YFlatScrollbarView
import miragefairy2024.mod.recipeviewer.views.YScrollContainerView
import miragefairy2024.mod.recipeviewer.views.connectScrollbar
import miragefairy2024.util.register
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ScrollConnectionTest {
    @Test
    fun `両側で指定位置を共有し寸法を反映するのだ`() {
        val scroll = XScrollContainerView()
        val bar = XFlatScrollbarView()
        scroll.scrollOffsetX.value = 15.25
        scroll.scrollBounds.value = ScrollBounds(100, 50)

        val connection = connectScrollbar(scroll, bar)
        try {
            Assertions.assertEquals(15.25, bar.scrollOffset.value)
            Assertions.assertEquals(100, bar.contentLength)
            Assertions.assertEquals(50, bar.viewportLength)

            scroll.scrollOffsetX.value = -30.25
            Assertions.assertEquals(-30.25, bar.scrollOffset.value)
            bar.scrollOffset.value = 120.5
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
        Assertions.assertEquals(120.5, bar.scrollOffset.value)
        Assertions.assertEquals(200, bar.contentLength)
        Assertions.assertEquals(100, bar.viewportLength)
        Assertions.assertTrue(scroll.scrollBounds.toList().isEmpty())
        Assertions.assertTrue(scroll.scrollOffsetX.toList().isEmpty())
        Assertions.assertTrue(bar.scrollOffset.toList().isEmpty())
        connectScrollbar(scroll, bar).remove()
        scroll.scrollOffsetX.value = 80.0
        Assertions.assertEquals(120.5, bar.scrollOffset.value)
    }

    @Test
    fun `一つの領域に二本のバーを接続して個別に解除するのだ`() {
        val scroll = XScrollContainerView()
        val firstBar = XFlatScrollbarView()
        val secondBar = XFlatScrollbarView()
        scroll.scrollOffsetX.value = 10.25
        scroll.scrollBounds.value = ScrollBounds(300, 100)

        val firstConnection = connectScrollbar(scroll, firstBar)
        try {
            val secondConnection = connectScrollbar(scroll, secondBar)
            try {
                Assertions.assertEquals(10.25, firstBar.scrollOffset.value)
                Assertions.assertEquals(10.25, secondBar.scrollOffset.value)

                firstBar.scrollOffset.value = 60.5
                Assertions.assertEquals(60.5, scroll.scrollOffsetX.value)
                Assertions.assertEquals(60.5, secondBar.scrollOffset.value)

                secondBar.scrollOffset.value = 120.75
                Assertions.assertEquals(120.75, scroll.scrollOffsetX.value)
                Assertions.assertEquals(120.75, firstBar.scrollOffset.value)

                scroll.scrollBounds.value = ScrollBounds(400, 150)
                listOf(firstBar, secondBar).forEach { bar ->
                    Assertions.assertEquals(400, bar.contentLength)
                    Assertions.assertEquals(150, bar.viewportLength)
                }

                firstConnection.remove()
                firstBar.scrollOffset.value = -15.25
                Assertions.assertEquals(120.75, scroll.scrollOffsetX.value)
                Assertions.assertEquals(120.75, secondBar.scrollOffset.value)

                secondBar.scrollOffset.value = 50.25
                scroll.scrollBounds.value = ScrollBounds(500, 200)
                Assertions.assertEquals(50.25, scroll.scrollOffsetX.value)
                Assertions.assertEquals(-15.25, firstBar.scrollOffset.value)
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
        val bar = XFlatScrollbarView()
        val firstScroll = XScrollContainerView()
        val secondScroll = XScrollContainerView()
        firstScroll.scrollOffsetX.value = 10.25
        secondScroll.scrollOffsetX.value = 20.5
        firstScroll.scrollBounds.value = ScrollBounds(300, 100)
        secondScroll.scrollBounds.value = ScrollBounds(400, 150)

        val firstConnection = connectScrollbar(firstScroll, bar)
        try {
            val secondConnection = connectScrollbar(secondScroll, bar)
            try {
                Assertions.assertEquals(20.5, bar.scrollOffset.value)
                Assertions.assertEquals(20.5, firstScroll.scrollOffsetX.value)
                Assertions.assertEquals(400, bar.contentLength)
                Assertions.assertEquals(150, bar.viewportLength)

                firstScroll.scrollOffsetX.value = 30.75
                Assertions.assertEquals(30.75, bar.scrollOffset.value)
                Assertions.assertEquals(30.75, secondScroll.scrollOffsetX.value)

                bar.scrollOffset.value = 60.0
                Assertions.assertEquals(60.0, firstScroll.scrollOffsetX.value)
                Assertions.assertEquals(60.0, secondScroll.scrollOffsetX.value)

                firstScroll.scrollBounds.value = ScrollBounds(500, 200)
                Assertions.assertEquals(500, bar.contentLength)
                Assertions.assertEquals(200, bar.viewportLength)

                firstConnection.remove()
                firstScroll.scrollOffsetX.value = -15.25
                firstScroll.scrollBounds.value = ScrollBounds(600, 250)
                Assertions.assertEquals(60.0, bar.scrollOffset.value)
                Assertions.assertEquals(500, bar.contentLength)

                secondScroll.scrollOffsetX.value = 55.25
                secondScroll.scrollBounds.value = ScrollBounds(450, 125)
                Assertions.assertEquals(55.25, bar.scrollOffset.value)
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
        val scroll = XScrollContainerView()
        val bar = XFlatScrollbarView()
        val listener = bar.scrollOffset.register { _, position ->
            if (position == 15.0) scroll.scrollOffsetX.value = 20.0
        }
        val connection = connectScrollbar(scroll, bar)
        try {
            scroll.scrollOffsetX.value = 15.0
            Assertions.assertEquals(20.0, scroll.scrollOffsetX.value)
            Assertions.assertEquals(20.0, bar.scrollOffset.value)
        } finally {
            connection.remove()
            listener.remove()
        }
    }

    @Test
    fun `共有位置が元の値に戻った場合も操作元へ反映するのだ`() {
        val scroll = XScrollContainerView()
        val bar = XFlatScrollbarView()
        val listener = scroll.scrollOffsetX.register { _, position ->
            if (position == 15.0) scroll.scrollOffsetX.value = 0.0
        }
        val connection = connectScrollbar(scroll, bar)
        try {
            bar.scrollOffset.value = 15.0
            Assertions.assertEquals(0.0, scroll.scrollOffsetX.value)
            Assertions.assertEquals(0.0, bar.scrollOffset.value)
        } finally {
            connection.remove()
            listener.remove()
        }
    }

    @Test
    fun `バー自身の補正をリスナーの登録順によらず両側へ反映するのだ`() {
        listOf(false, true).forEach { registerBeforeConnection ->
            listOf(0.0, 20.0).forEach { correctedOffset ->
                val scroll = XScrollContainerView()
                val bar = XFlatScrollbarView()
                val listener: (Double, Double) -> Unit = { _, position ->
                    if (position == 15.0) bar.scrollOffset.value = correctedOffset
                }
                if (registerBeforeConnection) bar.scrollOffset.add(listener)
                val connection = connectScrollbar(scroll, bar)
                if (!registerBeforeConnection) bar.scrollOffset.add(listener)
                try {
                    scroll.scrollOffsetX.value = 15.0
                    Assertions.assertEquals(correctedOffset, scroll.scrollOffsetX.value)
                    Assertions.assertEquals(correctedOffset, bar.scrollOffset.value)
                } finally {
                    connection.remove()
                    bar.scrollOffset.remove(listener)
                }
            }
        }
    }

    @Test
    fun `初期同期でバーが補正した位置を領域へ反映するのだ`() {
        listOf(0.0, 20.0).forEach { correctedOffset ->
            val scroll = XScrollContainerView()
            val bar = XFlatScrollbarView()
            scroll.scrollOffsetX.value = 15.0
            val listener = bar.scrollOffset.register { _, position ->
                if (position == 15.0) bar.scrollOffset.value = correctedOffset
            }
            val connection = connectScrollbar(scroll, bar)
            try {
                Assertions.assertEquals(correctedOffset, scroll.scrollOffsetX.value)
                Assertions.assertEquals(correctedOffset, bar.scrollOffset.value)
            } finally {
                connection.remove()
                listener.remove()
            }
        }
    }

    @Test
    fun `バーの通知中に表示領域の範囲が変わっても最新の状態まで同期するのだ`() {
        val scroll = XScrollContainerView()
        val bar = XFlatScrollbarView()
        scroll.scrollBounds.value = ScrollBounds(300, 100)
        scroll.scrollOffsetX.value = 80.0
        val listener = bar.scrollOffset.register { _, _ ->
            if (scroll.scrollBounds.value.contentLength == 300) {
                scroll.scrollBounds.value = ScrollBounds(600, 100)
                scroll.scrollOffsetX.value = 400.0
            }
        }
        val connection = connectScrollbar(scroll, bar)
        try {
            Assertions.assertEquals(600, bar.contentLength)
            Assertions.assertEquals(100, bar.viewportLength)
            Assertions.assertEquals(400.0, bar.scrollOffset.value)
            scroll.scrollOffsetX.value = 80.0
            scroll.scrollBounds.value = ScrollBounds(300, 100)
            Assertions.assertEquals(300, bar.contentLength)
            Assertions.assertEquals(80.0, bar.scrollOffset.value)
        } finally {
            connection.remove()
            listener.remove()
        }
        Assertions.assertTrue(scroll.scrollBounds.toList().isEmpty())
        Assertions.assertTrue(scroll.scrollOffsetX.toList().isEmpty())
        Assertions.assertTrue(bar.scrollOffset.toList().isEmpty())
    }

    @Test
    fun `縦横の View を接続して寸法と指定位置を同期するのだ`() {
        listOf(false, true).forEach { horizontal ->
            val scroll = if (horizontal) XScrollContainerView() else YScrollContainerView()
            val bar = if (horizontal) XFlatScrollbarView() else YFlatScrollbarView()
            val source = if (horizontal) (scroll as XScrollContainerView).scrollOffsetX else (scroll as YScrollContainerView).scrollOffsetY
            val target = bar.scrollOffset
            scroll.scrollBounds.value = ScrollBounds(300, 100)
            source.value = 80.25
            val connection = if (horizontal) connectScrollbar(scroll as XScrollContainerView, bar as XFlatScrollbarView) else connectScrollbar(scroll as YScrollContainerView, bar as YFlatScrollbarView)
            try {
                Assertions.assertEquals(300, bar.contentLength)
                Assertions.assertEquals(100, bar.viewportLength)
                Assertions.assertEquals(80.25, target.value)
                target.value = 120.5
                Assertions.assertEquals(120.5, source.value)
            } finally {
                connection.remove()
            }
        }
    }

    @Test
    fun `初期同期の通知が失敗したら位置と寸法の購読を解除するのだ`() {
        val scroll = XScrollContainerView()
        val bar = XFlatScrollbarView()
        scroll.scrollOffsetX.value = 80.0
        bar.scrollOffset.value = 20.0
        val exception = IllegalStateException("Listener failed")
        val listener = bar.scrollOffset.register { _, offset ->
            if (offset == 80.0) throw exception
        }
        try {
            Assertions.assertSame(exception, Assertions.assertThrows(IllegalStateException::class.java) { connectScrollbar(scroll, bar) })
            Assertions.assertTrue(scroll.scrollBounds.toList().isEmpty())
            Assertions.assertTrue(scroll.scrollOffsetX.toList().isEmpty())
            Assertions.assertEquals(1, bar.scrollOffset.toList().size)
            Assertions.assertEquals(80.0, scroll.scrollOffsetX.value)
            Assertions.assertEquals(20.0, bar.scrollOffset.value)
        } finally {
            listener.remove()
        }
    }
}
