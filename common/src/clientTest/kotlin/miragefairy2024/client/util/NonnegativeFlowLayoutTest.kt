package miragefairy2024.client.util

import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.Size
import io.wispforest.owo.ui.core.Sizing
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NonnegativeFlowLayoutTest {

    @Test
    fun `余白より小さい領域で子の幅と高さを 0 にするのだ`() {
        val child = Containers.verticalFlow(Sizing.fill(), Sizing.fill())
        val view = nonnegativeVerticalFlow(Sizing.fill(), Sizing.fill())
        view.padding(Insets.of(4))
        view.child(child)

        view.inflate(Size.of(5, 7))
        assertEquals(0, child.width())
        assertEquals(0, child.height())

        view.inflate(Size.of(30, 40))
        assertEquals(22, child.width())
        assertEquals(32, child.height())
    }

}
