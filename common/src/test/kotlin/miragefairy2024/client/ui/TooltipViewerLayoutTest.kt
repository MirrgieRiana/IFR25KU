package miragefairy2024.client.ui

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * `TooltipViewerClientModule.kt` の画面の骨組みを、新しいコンポーネントで組み直せるかを確かめるのだ～🌱
 *
 * あの画面は owo の参照が最も少ないため、移植の最初の候補なのだ～🌱
 * まだ無いコンポーネントは、同じ寸法を返す置き換えで代えているのだ～🌱
 */
class TooltipViewerLayoutTest {

    /** 中身の寸法が決まっている、試験用のコンポーネントなのだ～🌱 */
    private class FixedContentComponent(private val contentWidth: Int, private val contentHeight: Int) : UiComponent() {
        override fun determineContentWidth() = contentWidth
        override fun determineContentHeight() = contentHeight
    }

    /** 画面の寸法なのだ～🌱 */
    private val screen = Size.of(400, 300)

    /** 検索欄の高さは、バニラのテキスト入力と同じなのだ～🌱 */
    private val textBoxHeight = 20

    /** 実際の画面と同じ入れ子を組むのだ～🌱 */
    private fun buildRoot(scrollContentHeight: Int): Triple<UiFlowLayout, UiFlowLayout, UiComponent> {
        val root = UiFlowLayout(UiFlowLayout.Direction.VERTICAL)
        root.sizing(Sizing.fill(), Sizing.fill())
        root.padding(Insets.of(4))
        root.alignment(Alignment.CENTER, Alignment.CENTER)

        val outer = UiFlowLayout(UiFlowLayout.Direction.VERTICAL)
        outer.sizing(Sizing.content(), Sizing.fill())
        outer.padding(Insets.of(7))

        val inner = UiFlowLayout(UiFlowLayout.Direction.VERTICAL)
        inner.sizing(Sizing.fixed(240), Sizing.fill())

        val textBox = FixedContentComponent(0, textBoxHeight)
        textBox.sizing(Sizing.fill(), Sizing.fixed(textBoxHeight))

        val scroll = FixedContentComponent(0, scrollContentHeight)
        scroll.sizing(Sizing.fill(), Sizing.expand())

        inner.child(textBox)
        inner.child(verticalSpace(2))
        inner.child(scroll)
        outer.child(inner)
        root.child(outer)
        root.inflate(screen)
        return Triple(root, inner, scroll)
    }

    @Test
    fun `内枠の横幅は固定の240になるのだ～🌱`() {
        val (_, inner, _) = buildRoot(1000)
        assertEquals(240, inner.width)
    }

    @Test
    fun `外枠は内枠に余白を足した横幅になるのだ～🌱`() {
        val (root, _, _) = buildRoot(1000)
        val outer = root.children.single()
        // 内枠の 240 に、左右の余白 7 ずつを足すのだ～🌱
        assertEquals(254, outer.width)
    }

    @Test
    fun `外枠の縦は画面から外側の余白を引いた高さいっぱいになるのだ～🌱`() {
        val (root, _, _) = buildRoot(1000)
        val outer = root.children.single()
        // 画面の 300 から、根の余白 4 ずつを引くのだ～🌱
        assertEquals(292, outer.height)
    }

    @Test
    fun `スクロール領域は検索欄と空白を引いた残りを受け取るのだ～🌱`() {
        val (_, inner, scroll) = buildRoot(1000)
        // 画面の 300 から、根の余白 8 と外枠の余白 14 と検索欄の 20 と空白の 2 を引くのだ～🌱
        assertEquals(256, scroll.height)
        assertEquals(278, inner.height)
    }

    @Test
    fun `中身が短くてもスクロール領域は残りいっぱいに広がるのだ～🌱`() {
        val (_, _, shortScroll) = buildRoot(10)
        val (_, _, longScroll) = buildRoot(1000)
        assertEquals(longScroll.height, shortScroll.height)
    }

    @Test
    fun `外枠は画面の中央へ寄るのだ～🌱`() {
        val (root, _, _) = buildRoot(1000)
        val outer = root.children.single()
        // 画面の 400 から、根の余白 8 と外枠の 254 を引いた残り 138 の半分に、根の左の余白 4 を足すのだ～🌱
        assertEquals(73, outer.x)
    }
}
