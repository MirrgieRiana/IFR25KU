package miragefairy2024.mod.recipeviewer.view

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ViewBeforeRenderEventTest {
    @Test
    fun `描画前通知中に差し替えた View は同じ描画の前に更新するのだ`() {
        val event = ViewBeforeRenderEvent()
        val calls = mutableListOf<String>()
        var replaceChild = {}
        event.register {
            calls += "parent"
            replaceChild()
        }
        val oldChild = event.register { calls += "old child" }
        replaceChild = {
            oldChild.remove()
            event.register { calls += "new child" }
            replaceChild = {}
        }

        event.fire()
        event.fire()

        assertEquals(listOf("parent", "new child", "parent", "new child"), calls)
    }
}
