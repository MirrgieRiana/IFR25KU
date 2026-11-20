package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntRectangle
import net.minecraft.world.inventory.Slot
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class MenuSlotPlacementRegistryTest {

    @Test
    fun `未登録とクリップなしの配置と解除済みを区別するのだ`() {
        val registry = MenuSlotPlacementRegistry()
        val slot: Slot = Slot(null, 0, 10, 20)

        Assertions.assertFalse(registry.isManaged(slot))
        Assertions.assertNull(registry.getPlacement(slot))

        val remover = registry.register(slot) { null }

        Assertions.assertTrue(registry.isManaged(slot))
        Assertions.assertNotNull(registry.getPlacement(slot))
        Assertions.assertNull(registry.getPlacement(slot)!!.clipBounds())

        remover.remove()

        Assertions.assertTrue(registry.isManaged(slot))
        Assertions.assertNull(registry.getPlacement(slot))
        Assertions.assertEquals(10, slot.x)
        Assertions.assertEquals(20, slot.y)
    }

    @Test
    fun `矩形の変更と空の矩形を取得時に反映するのだ`() {
        val registry = MenuSlotPlacementRegistry()
        val slot: Slot = Slot(null, 0, 0, 0)
        var bounds: IntRectangle? = IntRectangle(10, 20, 30, 40)
        var calls = 0
        registry.register(slot) {
            calls += 1
            bounds
        }

        Assertions.assertEquals(0, calls)
        Assertions.assertEquals(bounds, registry.getPlacement(slot)!!.clipBounds())
        bounds = IntRectangle(30, 40, 0, 0)
        Assertions.assertEquals(bounds, registry.getPlacement(slot)!!.clipBounds())
        bounds = null
        Assertions.assertNull(registry.getPlacement(slot)!!.clipBounds())
        Assertions.assertEquals(3, calls)
    }

    @Test
    fun `同じ Slot の再登録後に古い解除処理が新しい配置を消さないのだ`() {
        val registry = MenuSlotPlacementRegistry()
        val slot: Slot = Slot(null, 0, 0, 0)
        val otherSlot: Slot = Slot(null, 1, 0, 0)
        val oldRemover = registry.register(slot) { IntRectangle(10, 20, 30, 40) }
        registry.register(otherSlot) { null }
        val bounds = IntRectangle(50, 60, 70, 80)
        val remover = registry.register(slot) { bounds }

        oldRemover.remove()

        Assertions.assertEquals(bounds, registry.getPlacement(slot)!!.clipBounds())
        Assertions.assertNotNull(registry.getPlacement(otherSlot))

        remover.remove()

        Assertions.assertTrue(registry.isManaged(slot))
        Assertions.assertNull(registry.getPlacement(slot))
        Assertions.assertNotNull(registry.getPlacement(otherSlot))
    }

    @Test
    fun `クリアで解除済みも忘れ再初期化後の登録を古い解除処理から区別するのだ`() {
        val registry = MenuSlotPlacementRegistry()
        val slot: Slot = Slot(null, 0, 0, 0)
        val detachedSlot: Slot = Slot(null, 1, 0, 0)
        val oldRemover = registry.register(slot) { null }
        registry.register(detachedSlot) { null }.remove()

        registry.clear()

        Assertions.assertFalse(registry.isManaged(slot))
        Assertions.assertNull(registry.getPlacement(slot))
        Assertions.assertFalse(registry.isManaged(detachedSlot))
        Assertions.assertNull(registry.getPlacement(detachedSlot))

        val remover = registry.register(slot) { null }
        oldRemover.remove()

        Assertions.assertNotNull(registry.getPlacement(slot))

        remover.remove()

        Assertions.assertNull(registry.getPlacement(slot))
    }

}
