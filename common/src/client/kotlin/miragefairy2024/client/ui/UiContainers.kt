package miragefairy2024.client.ui

/**
 * 子を持つコンポーネントを、寸法を指定して作るのだ～🌱
 *
 * owo の `Containers` に当たるのだ～🌱
 * 画面の移植で、呼び出しの形をそのまま保てるように、同じ順序の引数にしてあるのだ～🌱
 */
object UiContainers {
    /** 子を縦に並べるコンポーネントを作るのだ～🌱 */
    fun verticalFlow(horizontalSizing: Sizing, verticalSizing: Sizing) = UiFlowLayout(UiFlowLayout.Direction.VERTICAL).also { it.sizing(horizontalSizing, verticalSizing) }

    /** 子を横に並べるコンポーネントを作るのだ～🌱 */
    fun horizontalFlow(horizontalSizing: Sizing, verticalSizing: Sizing) = UiFlowLayout(UiFlowLayout.Direction.HORIZONTAL).also { it.sizing(horizontalSizing, verticalSizing) }

    /** 子を同じ場所へ重ねるコンポーネントを作るのだ～🌱 */
    fun stack(horizontalSizing: Sizing, verticalSizing: Sizing) = UiStackLayout().also { it.sizing(horizontalSizing, verticalSizing) }
}
