package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.GhostItemView
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertSame

class GhostItemViewTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrap() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    @Test
    fun `候補が空なら空のアイテムを返すのだ`() {
        val view = GhostItemView(emptyList())
        assertSame(ItemStack.EMPTY, view.itemStack)
        view.time = 90.0
        assertSame(ItemStack.EMPTY, view.itemStack)
    }

    @Test
    fun `候補は30ティックごとに切り替わって先頭へ戻るのだ`() {
        val first = ItemStack(Items.STONE)
        val second = ItemStack(Items.DIRT)
        val third = ItemStack(Items.GLASS)
        val view = GhostItemView(listOf(first, second, third))
        assertSame(first, view.itemStack)
        view.time = 29.999
        assertSame(first, view.itemStack)
        view.time = 30.0
        assertSame(second, view.itemStack)
        view.time = 60.0
        assertSame(third, view.itemStack)
        view.time = 90.0
        assertSame(first, view.itemStack)
    }

    @Test
    fun `候補が1個なら時間が経っても同じアイテムを返すのだ`() {
        val itemStack = ItemStack(Items.STONE)
        val view = GhostItemView(listOf(itemStack))
        view.time = 1000.0
        assertSame(itemStack, view.itemStack)
    }

    @Test
    fun `アイテム本体を隠してもツールチップ用の候補を返すのだ`() {
        val first = ItemStack(Items.STONE)
        val second = ItemStack(Items.DIRT)
        val view = GhostItemView(listOf(first, second))
        view.showItemStack = false
        view.time = 30.0
        assertSame(second, view.itemStack)
    }
}
