package miragefairy2024.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class EventRegistryTest {

    @Test
    fun `登録時の現在値と変更前後の値を通知し手動解除後は通知しないのだ～🌱`() {
        val value = ObservableValue(0)
        value.value = 2
        val notifications = mutableListOf<Pair<Int, Int>>()

        val remover = value.registerAndInitialize { old, new ->
            notifications += Pair(old, new)
        }

        assertEquals(listOf(Pair(2, 2)), notifications)
        value.value = 3
        value.value = 3
        assertEquals(listOf(Pair(2, 2), Pair(2, 3)), notifications)

        remover.remove()
        value.value = 4
        assertEquals(listOf(Pair(2, 2), Pair(2, 3)), notifications)
        assertTrue(value.listeners.isEmpty())
    }

    @Test
    fun `初期値を反映している間の値変更も通知するのだ～🌱`() {
        val value = ObservableValue(2)
        val notifications = mutableListOf<Pair<Int, Int>>()

        val remover = value.registerAndInitialize { old, new ->
            notifications += Pair(old, new)
            if (old == new) {
                value.value = 3
                assertEquals(3, value.value)
                assertEquals(listOf(Pair(2, 2)), notifications)
            }
        }

        assertEquals(listOf(Pair(2, 2), Pair(2, 3)), notifications)
        assertEquals(3, value.value)
        remover.remove()
        assertTrue(value.listeners.isEmpty())
    }

    @Test
    fun `終了イベントで解除する購読も初期値と変更を通知するのだ～🌱`() {
        val value = ObservableValue(2)
        val onClosed = EventRegistry<() -> Unit>()
        val notifications = mutableListOf<Pair<Int, Int>>()

        value.observeAndInitialize(onClosed) { old, new ->
            notifications += Pair(old, new)
        }

        assertEquals(listOf(Pair(2, 2)), notifications)
        value.value = 3
        assertEquals(listOf(Pair(2, 2), Pair(2, 3)), notifications)

        onClosed.fire()
        value.value = 4
        assertEquals(listOf(Pair(2, 2), Pair(2, 3)), notifications)
        assertTrue(value.listeners.isEmpty())
        assertTrue(onClosed.listeners.isEmpty())
    }

    @Test
    fun `初期値の反映中に終了しても購読を解除するのだ～🌱`() {
        val value = ObservableValue(2)
        val onClosed = EventRegistry<() -> Unit>()
        val notifications = mutableListOf<Pair<Int, Int>>()

        value.observeAndInitialize(onClosed) { old, new ->
            notifications += Pair(old, new)
            onClosed.fire()
        }

        value.value = 3
        assertEquals(listOf(Pair(2, 2)), notifications)
        assertTrue(value.listeners.isEmpty())
        assertTrue(onClosed.listeners.isEmpty())
    }

    @Test
    fun `通知中に値が変わると最後に受け取った値から最新値へ登録順に通知するのだ～🌱`() {
        val value = ObservableValue(10)
        val notifications = mutableListOf<Triple<Int, Int, Int>>()
        (1..3).forEach { index ->
            value.register { old, new ->
                notifications += Triple(index, old, new)
                if (index == 2 && new == 20) {
                    value.value = 25
                    value.value = 30
                    assertEquals(30, value.value)
                    assertEquals(listOf(Triple(1, 10, 20), Triple(2, 10, 20)), notifications)
                }
            }
        }

        value.value = 20

        assertEquals(
            listOf(Triple(1, 10, 20), Triple(2, 10, 20), Triple(1, 20, 30), Triple(2, 20, 30), Triple(3, 10, 30)),
            notifications,
        )
    }

    @Test
    fun `通知中に追加したリスナーは登録時の値を起点に通知するのだ～🌱`() {
        val value = ObservableValue(10)
        val notifications = mutableListOf<Triple<Int, Int, Int>>()
        value.register { _, new ->
            if (new == 20) {
                value.register { old, current ->
                    notifications += Triple(1, old, current)
                }
                value.value = 30
                value.register { old, current ->
                    notifications += Triple(2, old, current)
                }
            }
        }

        value.value = 20
        assertEquals(listOf(Triple(1, 20, 30)), notifications)
        value.value = 40
        assertEquals(listOf(Triple(1, 20, 30), Triple(1, 30, 40), Triple(2, 30, 40)), notifications)
    }

    @Test
    fun `通知前に解除したリスナーを再登録しても古い登録では呼ばないのだ～🌱`() {
        val value = ObservableValue(10)
        val notifications = mutableListOf<Pair<Int, Int>>()
        val listener: (Int, Int) -> Unit = { old, new ->
            notifications += Pair(old, new)
        }
        value.register { _, new ->
            if (new == 20) {
                value.remove(listener)
                value.register(listener)
            }
        }
        val oldRemover = value.register(listener)

        value.value = 20
        assertTrue(notifications.isEmpty())
        oldRemover.remove()
        value.value = 30
        assertEquals(listOf(Pair(20, 30)), notifications)
    }

    @Test
    fun `終了イベントが発火すると Remover を解除するのだ～🌱`() {
        val onClosed = EventRegistry<() -> Unit>()
        val removals = mutableListOf<Int>()
        Remover {
            removals += 1
            assertEquals(1, onClosed.listeners.size)
        }.removeOn(onClosed)
        Remover {
            removals += 2
            assertTrue(onClosed.listeners.isEmpty())
        }.removeOn(onClosed)

        assertTrue(removals.isEmpty())
        assertEquals(2, onClosed.listeners.size)
        onClosed.fire()
        onClosed.fire()
        assertEquals(listOf(1, 2), removals)
        assertTrue(onClosed.listeners.isEmpty())
    }

    @Test
    fun `終了時の解除が例外を送出しても終了イベントの登録は残らないのだ～🌱`() {
        val onClosed = EventRegistry<() -> Unit>()
        var removals = 0
        Remover {
            removals += 1
            throw IllegalStateException("Removal failed")
        }.removeOn(onClosed)

        val exception = assertFailsWith<IllegalStateException> { onClosed.fire() }
        assertEquals("Removal failed", exception.message)
        assertTrue(onClosed.listeners.isEmpty())
        onClosed.fire()
        assertEquals(1, removals)
    }

}
