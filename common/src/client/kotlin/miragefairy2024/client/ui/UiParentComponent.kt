package miragefairy2024.client.ui

/** 子を持つコンポーネントに共通する、子の管理と当たり判定なのだ～🌱 */
abstract class UiParentComponent : UiComponent() {
    private val mutableChildren = mutableListOf<UiComponent>()

    val children: List<UiComponent> get() = mutableChildren

    /** 子を置ける範囲を、自分の縁から内側へ狭める長さなのだ～🌱 */
    var padding = Insets.of(0)
        private set

    var horizontalAlignment = Alignment.START
        private set

    var verticalAlignment = Alignment.START
        private set

    /** 子を並べ終えた範囲の横の長さなのだ～🌱 */
    protected var contentWidth = 0

    /** 子を並べ終えた範囲の縦の長さなのだ～🌱 */
    protected var contentHeight = 0

    fun child(child: UiComponent) {
        mutableChildren += child
        child.parent = this
    }

    fun clearChildren() {
        mutableChildren.forEach { it.parent = null }
        mutableChildren.clear()
    }

    fun padding(padding: Insets) {
        this.padding = padding
    }

    fun alignment(horizontalAlignment: Alignment, verticalAlignment: Alignment) {
        this.horizontalAlignment = horizontalAlignment
        this.verticalAlignment = verticalAlignment
    }

    final override fun determineContentWidth() = contentWidth + padding.horizontal

    final override fun determineContentHeight() = contentHeight + padding.vertical

    final override fun inflate(space: Size) {
        // 子へ渡す空間を決めるのに自分の寸法が要るため、先に暫定の寸法を出すのだ～🌱
        super.inflate(space)
        layoutChildren(childSpace(space), mutableChildren)
        // 子を並べ終えた範囲が決まったので、自分の寸法を決め直すのだ～🌱
        super.inflate(space)
        alignChildren(mutableChildren)
    }

    private fun childSpace(space: Size) = Size.of(
        childSpace(horizontalSizing, width, space.width - margins.horizontal, padding.horizontal),
        childSpace(verticalSizing, height, space.height - margins.vertical, padding.vertical),
    )

    /** 子の寸法を確定して、親の中身の始点から数えた位置へ置き、[contentWidth] と [contentHeight] を決めるのだ～🌱 */
    protected abstract fun layoutChildren(childSpace: Size, children: List<UiComponent>)

    /** 余った空間の分だけ、子の位置を寄せるのだ～🌱 */
    protected abstract fun alignChildren(children: List<UiComponent>)

    /**
     * 画面の左上から数えた座標の下にある、最も手前の子孫を探すのだ～🌱
     *
     * 後から足した子ほど手前に描かれるため、逆の順で探すのだ～🌱
     */
    fun hitTest(x: Int, y: Int): UiComponent? {
        mutableChildren.asReversed().forEach { child ->
            if (!child.containsPoint(x, y)) return@forEach
            if (child is UiParentComponent) {
                val descendant = child.hitTest(x, y)
                if (descendant != null) return descendant
            }
            return child
        }
        return null
    }
}
