package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.GuideItemView
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertSame

class GuideItemViewTest {
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
        val view = GuideItemView(emptyList())
        assertSame(ItemStack.EMPTY, view.getItemStack(0.0))
        assertSame(ItemStack.EMPTY, view.getItemStack(90.0))
    }

    @Test
    fun `候補は30ティックごとに切り替わって先頭へ戻るのだ`() {
        val first = ItemStack(Items.STONE)
        val second = ItemStack(Items.DIRT)
        val third = ItemStack(Items.GLASS)
        val view = GuideItemView(listOf(first, second, third))
        assertSame(first, view.getItemStack(0.0))
        assertSame(first, view.getItemStack(29.999))
        assertSame(second, view.getItemStack(30.0))
        assertSame(third, view.getItemStack(60.0))
        assertSame(first, view.getItemStack(90.0))
    }

    @Test
    fun `候補が1個なら時間が経っても同じアイテムを返すのだ`() {
        val itemStack = ItemStack(Items.STONE)
        val view = GuideItemView(listOf(itemStack))
        assertSame(itemStack, view.getItemStack(1000.0))
    }

    @Test
    fun `アイテム本体を隠してもツールチップ用の候補を返すのだ`() {
        val first = ItemStack(Items.STONE)
        val second = ItemStack(Items.DIRT)
        val view = GuideItemView(listOf(first, second))
        view.showItemStack = false
        assertSame(second, view.getItemStack(30.0))
    }
}
