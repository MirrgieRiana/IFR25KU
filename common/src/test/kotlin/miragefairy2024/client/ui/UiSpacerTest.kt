package miragefairy2024.client.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class UiSpacerTest {

    @Test
    fun `horizontalSpaceは横だけを占めるのだ～🌱`() {
        val spacer = horizontalSpace(7)
        spacer.inflate(Size.of(500, 500))
        assertEquals(7, spacer.width)
        assertEquals(0, spacer.height)
    }

    @Test
    fun `verticalSpaceは縦だけを占めるのだ～🌱`() {
        val spacer = verticalSpace(7)
        spacer.inflate(Size.of(500, 500))
        assertEquals(0, spacer.width)
        assertEquals(7, spacer.height)
    }

    @Test
    fun `空白は縦に並べた親の高さへ足されるのだ～🌱`() {
        val layout = UiFlowLayout(UiFlowLayout.Direction.VERTICAL)
        layout.child(verticalSpace(5))
        layout.child(verticalSpace(3))
        layout.inflate(Size.of(500, 500))
        assertEquals(8, layout.height)
    }
}
