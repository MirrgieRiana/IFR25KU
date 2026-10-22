package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.AbstractView
import miragefairy2024.mod.recipeviewer.views.Child
import miragefairy2024.mod.recipeviewer.views.MarginView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.util.Remover
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

private val TEST_RENDERING_PROXY = object : RenderingProxy {
    override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
    override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
    override fun getTextHeight() = throw UnsupportedOperationException()
    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = throw UnsupportedOperationException()
}

private class RecordingActualSizeView : AbstractView() {
    val events = mutableListOf<String>()

    override var sizingX = Sizing.WRAP
        get() {
            events += "sizingX"
            return field
        }
    override var sizingY = Sizing.WRAP
        get() {
            events += "sizingY"
            return field
        }

    override fun calculateContentSize() = IntPoint(10, 20)

    override fun calculateChildrenActualSize() {
        events += "children:${actualSizeX}x$actualSizeY"
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = Remover.NONE
}

class AxisWiseActualSizeTest {

    @Test
    fun `X成分はY成分を計算せず同期的に確定するのだ`() {
        val view = RecordingActualSizeView()
        view.calculateContentSize(TEST_RENDERING_PROXY)

        view.calculateActualSizeX(100)
        assertEquals(listOf("sizingX"), view.events)
        assertEquals(10, view.actualSizeX)
        assertThrows(IllegalStateException::class.java) { view.actualSizeY }
        assertEquals(listOf("sizingX"), view.events)
    }

    @Test
    fun `Y成分はX成分を計算せず同期的に確定するのだ`() {
        val view = RecordingActualSizeView()
        view.calculateContentSize(TEST_RENDERING_PROXY)

        view.calculateActualSizeY(100)
        assertEquals(listOf("sizingY"), view.events)
        assertEquals(20, view.actualSizeY)
        assertThrows(IllegalStateException::class.java) { view.actualSizeX }
        assertEquals(listOf("sizingY"), view.events)
    }

    @Test
    fun `FILLはどちらの軸からでも渡された領域で確定するのだ`() {
        listOf(true, false).forEach { xFirst ->
            val view = RecordingActualSizeView()
            view.sizingX = Sizing.FILL
            view.sizingY = Sizing.FILL

            if (xFirst) {
                view.calculateActualSizeX(120)
                assertEquals(listOf("sizingX"), view.events)
                assertEquals(120, view.actualSizeX)
                assertThrows(IllegalStateException::class.java) { view.actualSizeY }
                view.calculateActualSizeY(500)
            } else {
                view.calculateActualSizeY(500)
                assertEquals(listOf("sizingY"), view.events)
                assertEquals(500, view.actualSizeY)
                assertThrows(IllegalStateException::class.java) { view.actualSizeX }
                view.calculateActualSizeX(120)
            }
            val expectedEvents = (if (xFirst) listOf("sizingX", "sizingY") else listOf("sizingY", "sizingX")) + "children:120x500"
            assertEquals(expectedEvents, view.events)
            assertEquals(120, view.actualSizeX)
            assertEquals(500, view.actualSizeY)
            assertEquals(IntPoint(120, 500), view.actualSize)
            assertEquals(expectedEvents, view.events)
        }
    }

    @Test
    fun `WRAPとFILLの組合せを保って両軸確定後に子を計算するのだ`() {
        Sizing.entries.forEach { sizingX ->
            Sizing.entries.forEach { sizingY ->
                val view = RecordingActualSizeView()
                view.sizingX = sizingX
                view.sizingY = sizingY
                view.calculateContentSize(TEST_RENDERING_PROXY)
                val expectedX = if (sizingX == Sizing.FILL) 100 else 10
                val expectedY = if (sizingY == Sizing.FILL) 200 else 20

                view.calculateActualSizeX(100)
                view.calculateActualSizeY(200)

                assertEquals(listOf("sizingX", "sizingY", "children:${expectedX}x$expectedY"), view.events)
                assertEquals(expectedX, view.actualSizeX)
                assertEquals(expectedY, view.actualSizeY)
                assertEquals(listOf("sizingX", "sizingY", "children:${expectedX}x$expectedY"), view.events)
            }
        }
    }

    @Test
    fun `既存のコンテナーとラッパーは割り当て領域を子へ伝播するのだ`() {
        val childView = RecordingActualSizeView()
        childView.sizingX = Sizing.FILL
        childView.sizingY = Sizing.FILL
        val marginView = MarginView(2, 3, 5, 7)
        marginView.add(Child(Unit, childView))
        val stackView = StackView()
        stackView.sizingX = Sizing.FILL
        stackView.sizingY = Sizing.FILL
        stackView.add(Child(Unit, marginView))
        val otherChildView = RecordingActualSizeView()
        stackView.add(Child(Unit, otherChildView))
        stackView.calculateContentSize(TEST_RENDERING_PROXY)

        stackView.calculateActualSizeX(100)
        stackView.calculateActualSizeY(200)

        assertEquals(100, stackView.actualSizeX)
        assertEquals(200, stackView.actualSizeY)
        assertEquals(100, marginView.actualSizeX)
        assertEquals(200, marginView.actualSizeY)
        assertEquals(95, childView.actualSizeX)
        assertEquals(188, childView.actualSizeY)
        assertEquals(listOf("sizingX", "sizingY", "sizingX", "sizingY", "children:95x188"), childView.events)
        assertEquals(10, otherChildView.actualSizeX)
        assertEquals(20, otherChildView.actualSizeY)
        assertEquals(listOf("sizingX", "sizingY", "children:10x20"), otherChildView.events)
    }

}
