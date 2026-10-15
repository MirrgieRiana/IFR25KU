package miragefairy2024.client.ui

/**
 * 子を縦または横の一方向へ並べるコンポーネントなのだ～🌱
 *
 * owo の `FlowLayout` のうち、寸法と位置を決める部分に当たるのだ～🌱
 */
class UiFlowLayout(private val direction: Direction) : UiComponent() {
    /** 子を並べる向きなのだ～🌱 */
    enum class Direction {
        VERTICAL,
        HORIZONTAL,
    }

    private val mutableChildren = mutableListOf<UiComponent>()

    val children: List<UiComponent> get() = mutableChildren

    /** 子と子の間に空ける長さなのだ～🌱 */
    var gap = 0
        private set

    /** 子を置ける範囲を、自分の縁から内側へ狭める長さなのだ～🌱 */
    var padding = Insets.of(0)
        private set

    var horizontalAlignment = Alignment.START
        private set

    var verticalAlignment = Alignment.START
        private set

    /** 子を並べ終えた範囲の横の長さなのだ～🌱 */
    private var contentWidth = 0

    /** 子を並べ終えた範囲の縦の長さなのだ～🌱 */
    private var contentHeight = 0

    fun child(child: UiComponent) {
        mutableChildren += child
    }

    fun clearChildren() {
        mutableChildren.clear()
    }

    fun gap(gap: Int) {
        this.gap = gap
    }

    fun padding(padding: Insets) {
        this.padding = padding
    }

    fun alignment(horizontalAlignment: Alignment, verticalAlignment: Alignment) {
        this.horizontalAlignment = horizontalAlignment
        this.verticalAlignment = verticalAlignment
    }

    override fun determineContentWidth() = contentWidth + padding.horizontal

    override fun determineContentHeight() = contentHeight + padding.vertical

    override fun inflate(space: Size) {
        // 子へ渡す空間を決めるのに自分の寸法が要るため、先に暫定の寸法を出すのだ～🌱
        super.inflate(space)
        layoutChildren(space)
        // 子を並べ終えた範囲が決まったので、自分の寸法を決め直すのだ～🌱
        super.inflate(space)
        alignChildren()
    }

    private fun layoutChildren(space: Size) {
        val childSpace = Size.of(
            childSpace(horizontalSizing, width, space.width - margins.horizontal, padding.horizontal),
            childSpace(verticalSizing, height, space.height - margins.vertical, padding.vertical),
        )
        val vertical = direction == Direction.VERTICAL
        val alongSpace = if (vertical) childSpace.height else childSpace.width
        val acrossSpace = if (vertical) childSpace.width else childSpace.height
        inflateWithExpand(
            mutableChildren,
            alongSpace,
            gap,
            { if (vertical) it.verticalSizing else it.horizontalSizing },
            { child, length ->
                // 並べる方向と垂直な方向は、どの子も同じ空間を受け取るのだ～🌱
                child.inflate(if (vertical) Size.of(acrossSpace, length) else Size.of(length, acrossSpace))
                if (vertical) child.fullHeight else child.fullWidth
            },
        )
        val result = flowAlong(mutableChildren.map { if (vertical) it.fullHeight else it.fullWidth }, gap)
        mutableChildren.forEachIndexed { index, child ->
            val along = result.offsets[index]
            if (vertical) {
                child.moveTo(padding.left + child.margins.left, padding.top + child.margins.top + along)
            } else {
                child.moveTo(padding.left + child.margins.left + along, padding.top + child.margins.top)
            }
        }
        contentWidth = if (vertical) mutableChildren.maxOfOrNull { it.fullWidth } ?: 0 else result.length
        contentHeight = if (vertical) result.length else mutableChildren.maxOfOrNull { it.fullHeight } ?: 0
    }

    private fun alignChildren() {
        val vertical = direction == Direction.VERTICAL
        val alongAlignment = if (vertical) verticalAlignment else horizontalAlignment
        val acrossAlignment = if (vertical) horizontalAlignment else verticalAlignment
        val alongSpace = if (vertical) height - padding.vertical else width - padding.horizontal
        val acrossSpace = if (vertical) width - padding.horizontal else height - padding.vertical
        val alongOffset = alignAlong(alongAlignment, if (vertical) contentHeight else contentWidth, alongSpace)
        mutableChildren.forEach { child ->
            val acrossOffset = acrossAlignment.align(if (vertical) child.fullWidth else child.fullHeight, acrossSpace)
            if (vertical) {
                child.moveTo(child.x + acrossOffset, child.y + alongOffset)
            } else {
                child.moveTo(child.x + alongOffset, child.y + acrossOffset)
            }
        }
    }
}
