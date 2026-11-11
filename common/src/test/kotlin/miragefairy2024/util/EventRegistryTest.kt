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
            if (old == new) value.value = 3
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
    fun `終了イベントを共有する Remover は再入しても一度ずつ解除するのだ～🌱`() {
        val onClosed = EventRegistry<() -> Unit>()
        val removals = mutableListOf<Int>()
        Remover {
            removals += 1
            assertEquals(1, onClosed.listeners.size)
            onClosed.fire()
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
