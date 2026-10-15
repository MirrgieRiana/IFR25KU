package miragefairy2024.client.ui

/**
 * 子を縦または横の一方向へ並べるコンポーネントなのだ～🌱
 *
 * owo の `FlowLayout` のうち、寸法と位置を決める部分に当たるのだ～🌱
 */
class UiFlowLayout(private val direction: Direction) : UiParentComponent() {
    /** 子を並べる向きなのだ～🌱 */
    enum class Direction {
        VERTICAL,
        HORIZONTAL,
    }

    private val vertical get() = direction == Direction.VERTICAL

    /** 子と子の間に空ける長さなのだ～🌱 */
    var gap = 0
        private set

    fun gap(gap: Int) {
        this.gap = gap
    }

    override fun layoutChildren(childSpace: Size, children: List<UiComponent>): Size {
        val alongSpace = if (vertical) childSpace.height else childSpace.width
        val acrossSpace = if (vertical) childSpace.width else childSpace.height
        inflateWithExpand(
            children,
            alongSpace,
            gap,
            { if (vertical) it.verticalSizing else it.horizontalSizing },
            { child, length ->
                // 並べる方向と垂直な方向は、どの子も同じ空間を受け取るのだ～🌱
                child.inflate(if (vertical) Size.of(acrossSpace, length) else Size.of(length, acrossSpace))
                if (vertical) child.fullHeight else child.fullWidth
            },
        )
        val result = flowAlong(children.map { if (vertical) it.fullHeight else it.fullWidth }, gap)
        children.forEachIndexed { index, child ->
            val along = result.offsets[index]
            if (vertical) {
                child.moveTo(padding.left + child.margins.left, padding.top + child.margins.top + along)
            } else {
                child.moveTo(padding.left + child.margins.left + along, padding.top + child.margins.top)
            }
        }
        return if (vertical) {
            Size.of(children.maxOfOrNull { it.fullWidth } ?: 0, result.length)
        } else {
            Size.of(result.length, children.maxOfOrNull { it.fullHeight } ?: 0)
        }
    }

    override fun alignChildren(children: List<UiComponent>) {
        val alongAlignment = if (vertical) verticalAlignment else horizontalAlignment
        val acrossAlignment = if (vertical) horizontalAlignment else verticalAlignment
        val alongSpace = if (vertical) height - padding.vertical else width - padding.horizontal
        val acrossSpace = if (vertical) width - padding.horizontal else height - padding.vertical
        // 並べる方向では子がひと続きに繋がっているため、全体を 1 個の塊として寄せるのだ～🌱
        val alongOffset = alignAlong(alongAlignment, if (vertical) contentSize.height else contentSize.width, alongSpace)
        children.forEach { child ->
            val acrossOffset = acrossAlignment.align(if (vertical) child.fullWidth else child.fullHeight, acrossSpace)
            if (vertical) {
                child.moveTo(child.x + acrossOffset, child.y + alongOffset)
            } else {
                child.moveTo(child.x + alongOffset, child.y + acrossOffset)
            }
        }
    }
}
