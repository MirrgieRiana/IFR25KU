package miragefairy2024.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class EventRegistryTest {

    @Test
    fun `発火中に解除したリスナーには同じ発火で通知しないのだ～🌱`() {
        val event = EventRegistry<() -> Unit>()
        val notifications = mutableListOf<Int>()
        val addedListener: () -> Unit = { notifications += 4 }
        lateinit var remover: Remover
        event.register {
            notifications += 1
            remover.remove()
            event.add(addedListener)
        }
        remover = event.register { notifications += 2 }
        event.register { notifications += 3 }

        event.fire()
        assertEquals(listOf(1, 3), notifications)

        event.fire()
        assertEquals(listOf(1, 3, 1, 3, 4), notifications)
    }

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
        assertTrue(value.toList().isEmpty())
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
        assertTrue(value.toList().isEmpty())
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
        assertTrue(value.toList().isEmpty())
        assertTrue(onClosed.toList().isEmpty())
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
        assertTrue(value.toList().isEmpty())
        assertTrue(onClosed.toList().isEmpty())
    }

    @Test
    fun `通知中に変わった値はリスナーごとの前回値から最新値へ通知するのだ～🌱`() {
        val value = ObservableValue(10)
        val notifications = mutableListOf<Triple<Int, Int, Int>>()
        value.register { old, new ->
            notifications += Triple(1, old, new)
            if (new == 20) {
                value.value = 25
                value.value = 30
                assertEquals(listOf(Triple(1, 10, 20)), notifications)
            }
        }
        value.register { old, new ->
            notifications += Triple(2, old, new)
        }

        value.value = 20

        assertEquals(listOf(Triple(1, 10, 20), Triple(1, 20, 30), Triple(2, 10, 30)), notifications)
    }

    @Test
    fun `通知中に元の値に戻ったら中間値を受け取っていないリスナーにも通知するのだ～🌱`() {
        val value = ObservableValue(10)
        val first = mutableListOf<Pair<Int, Int>>()
        val second = mutableListOf<Pair<Int, Int>>()
        value.register { old, new ->
            first += Pair(old, new)
            if (new == 20) value.value = 10
        }
        value.register { old, new ->
            second += Pair(old, new)
        }

        value.value = 20

        assertEquals(listOf(Pair(10, 20), Pair(20, 10)), first)
        assertEquals(listOf(Pair(10, 10)), second)
    }

    @Test
    fun `通知中に登録したリスナーには登録時の値から再配送するのだ～🌱`() {
        val value = ObservableValue(10)
        val notifications = mutableListOf<Pair<Int, Int>>()
        value.register { _, new ->
            if (new == 20) {
                value.registerAndInitialize { old, latest -> notifications += Pair(old, latest) }
                value.value = 30
            }
        }

        value.value = 20

        assertEquals(listOf(Pair(20, 20), Pair(20, 30)), notifications)
    }

    @Test
    fun `通知中に解除した後続のリスナーを同じ周回で呼ばないのだ～🌱`() {
        val value = ObservableValue(10)
        val notifications = mutableListOf<Pair<Int, Int>>()
        lateinit var remover: Remover
        value.register { _, _ -> remover.remove() }
        remover = value.register { old, new -> notifications += Pair(old, new) }

        value.value = 20

        assertTrue(notifications.isEmpty())
    }

    @Test
    fun `通知中に自身を解除したリスナーを再配送で呼ばないのだ～🌱`() {
        val value = ObservableValue(10)
        val notifications = mutableListOf<Pair<Int, Int>>()
        lateinit var remover: Remover
        remover = value.register { old, new ->
            notifications += Pair(old, new)
            if (new == 20) {
                remover.remove()
                value.value = 30
            }
        }

        value.value = 20

        assertEquals(listOf(Pair(10, 20)), notifications)
    }

    @Test
    fun `通知中に再登録したリスナーには再登録時の値から通知するのだ～🌱`() {
        val value = ObservableValue(10)
        val notifications = mutableListOf<Pair<Int, Int>>()
        lateinit var remover: Remover
        lateinit var listener: (Int, Int) -> Unit
        listener = { old, new ->
            notifications += Pair(old, new)
            if (new == 20) {
                remover.remove()
                value.value = 30
                remover = value.registerAndInitialize(listener)
            }
        }
        remover = value.register(listener)

        value.value = 20
        value.value = 40

        assertEquals(listOf(Pair(10, 20), Pair(30, 30), Pair(30, 40)), notifications)
    }

    @Test
    fun `通知中に値を変えて例外を送出しても次の代入を通知するのだ～🌱`() {
        val value = ObservableValue(10)
        val remover = value.register { _, _ ->
            value.value = 30
            throw IllegalStateException("Listener failed")
        }
        val notifications = mutableListOf<Pair<Int, Int>>()
        value.register { old, new ->
            notifications += Pair(old, new)
        }

        assertFailsWith<IllegalStateException> { value.value = 20 }
        assertTrue(notifications.isEmpty())
        remover.remove()
        value.value = 40

        assertEquals(listOf(Pair(30, 40)), notifications)
    }

    @Test
    fun `同値の別インスタンスは格納して通知を省くのだ～🌱`() {
        val value = ObservableValue(Pair(1, 2))
        val notifications = mutableListOf<Pair<Int, Int>>()
        value.register { _, new ->
            notifications += new
        }

        val replacement = Pair(1, 2)
        value.value = replacement

        assertSame(replacement, value.value)
        assertTrue(notifications.isEmpty())
    }

    @Test
    fun `終了イベントが発火すると Remover を解除するのだ～🌱`() {
        val onClosed = EventRegistry<() -> Unit>()
        val removals = mutableListOf<Int>()
        Remover {
            removals += 1
            assertEquals(1, onClosed.toList().size)
        }.removeOn(onClosed)
        Remover {
            removals += 2
            assertTrue(onClosed.toList().isEmpty())
        }.removeOn(onClosed)

        assertTrue(removals.isEmpty())
        assertEquals(2, onClosed.toList().size)
        onClosed.fire()
        onClosed.fire()
        assertEquals(listOf(1, 2), removals)
        assertTrue(onClosed.toList().isEmpty())
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
        assertTrue(onClosed.toList().isEmpty())
        onClosed.fire()
        assertEquals(1, removals)
    }

}
