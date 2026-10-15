package miragefairy2024.client.ui

/**
 * 子を同じ場所へ重ねるコンポーネントなのだ～🌱
 *
 * owo の `StackLayout` のうち、寸法と位置を決める部分に当たるのだ～🌱
 */
class UiStackLayout : UiParentComponent() {
    override fun layoutChildren(childSpace: Size, children: List<UiComponent>) {
        // どの子も、重なるため、同じ空間を受け取るのだ～🌱
        children.forEach { child ->
            child.inflate(childSpace)
            child.moveTo(padding.left + child.margins.left, padding.top + child.margins.top)
        }
        contentWidth = children.maxOfOrNull { it.fullWidth } ?: 0
        contentHeight = children.maxOfOrNull { it.fullHeight } ?: 0
    }

    override fun alignChildren(children: List<UiComponent>) {
        // 子が繋がっていないため、縦も横も、子ごとに寄せるのだ～🌱
        children.forEach { child ->
            val horizontalOffset = horizontalAlignment.align(child.fullWidth, width - padding.horizontal)
            val verticalOffset = verticalAlignment.align(child.fullHeight, height - padding.vertical)
            child.moveTo(child.x + horizontalOffset, child.y + verticalOffset)
        }
    }
}
